<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Book, model.User, java.util.List" %>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    List<Book>   books            = (List<Book>)   request.getAttribute("books");
    List<String> categories       = (List<String>) request.getAttribute("categories");
    Integer      totalRecords     = (Integer)       request.getAttribute("totalRecords");
    Integer      totalPages       = (Integer)       request.getAttribute("totalPages");
    Integer      currentPageNum   = (Integer)       request.getAttribute("currentPageNum");
    String       keyword          = (String)        request.getAttribute("keyword");
    String       selectedCategory = (String)        request.getAttribute("selectedCategory");
    String       sortField        = (String)        request.getAttribute("sortField");
    String       sortOrder        = (String)        request.getAttribute("sortOrder");
    String       viewMode         = (String)        request.getAttribute("viewMode");
    String       dbError          = (String)        request.getAttribute("dbError");

    User loggedUser = (User) session.getAttribute("loggedUser");
    boolean isAdmin    = (loggedUser != null && loggedUser.isAdmin());
    boolean isAdminLib = (loggedUser != null && loggedUser.isAdminOrLibrarian());

    String ctx         = request.getContextPath();
    if (totalRecords     == null) totalRecords     = 0;
    if (totalPages       == null) totalPages       = 1;
    if (currentPageNum   == null) currentPageNum   = 1;
    if (keyword          == null) keyword          = "";
    if (selectedCategory == null) selectedCategory = "";
    if (sortField        == null) sortField        = "title";
    if (sortOrder        == null) sortOrder        = "ASC";
    if (viewMode         == null) viewMode         = "grid";

    String nextOrder = "ASC".equals(sortOrder) ? "DESC" : "ASC";

    // Detect context via rolePath attribute set by BookListServlet
    String rolePathBooks = (String) request.getAttribute("rolePath");
    String detailBase    = ctx + ("/librarian".equals(rolePathBooks) ? "/librarian/book/detail" :
                           ("/admin".equals(rolePathBooks) ? "/admin/book/detail" : "/book/detail"));
%>

<main class="page-wrapper">

<!-- ===== BOOKS PAGE HEADER ===== -->
<div class="books-page-header">
    <div class="container">
        <div class="books-page-header-inner">
            <div>
                <div class="hero-eyebrow">
                    <i class="fa-solid fa-book"></i> Kho sách
                </div>
                <h1 class="books-page-title">Danh sách sách</h1>
                <p class="books-page-subtitle">Tra cứu, tìm kiếm và lọc đầu sách trong Thư viện FPT University</p>
            </div>
            <div class="books-page-stats">
                <div class="bps-item">
                    <span class="bps-num"><%= totalRecords %></span>
                    <span class="bps-lbl">Đầu sách</span>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="container" style="padding-top:28px;">

    <!-- ==================== SEARCH & FILTER BAR ==================== -->
    <form id="searchForm" action="<%= ctx %>/books" method="get" novalidate>
        <input type="hidden" name="view"  value="<%= viewMode %>">
        <input type="hidden" name="sort"  value="<%= sortField %>">
        <input type="hidden" name="order" value="<%= sortOrder %>">
        <input type="hidden" name="page"  value="1">

        <div class="search-bar-wrapper">
            <div class="search-bar-inner">
                <!-- Keyword -->
                <div class="search-field" style="flex:2;">
                    <label for="keywordInput">Tìm kiếm</label>
                    <div class="search-input-wrap">
                        <i class="fa-solid fa-magnifying-glass search-icon"></i>
                        <input type="text" id="keywordInput" name="keyword"
                               class="form-control"
                               placeholder="Nhập tên sách, ISBN, tác giả..."
                               value="<%= keyword %>"
                               maxlength="200"
                               autocomplete="off">
                    </div>
                </div>

                <!-- Category Filter -->
                <div class="search-field select-field">
                    <label for="categorySelect">Danh mục</label>
                    <select id="categorySelect" name="category" class="form-select">
                        <option value="">-- Tất cả danh mục --</option>
                        <% if (categories != null) {
                            for (String cat : categories) {
                                String sel = cat.equals(selectedCategory) ? "selected" : "";
                        %>
                            <option value="<%= cat %>" <%= sel %>><%= cat %></option>
                        <% }} %>
                    </select>
                </div>

                <!-- Buttons -->
                <div style="display:flex; gap:8px; align-items:flex-end;">
                    <button type="submit" class="btn btn-primary" id="searchBtn">
                        <i class="fa-solid fa-search"></i> Tìm
                    </button>
                    <% if (rolePathBooks == null || rolePathBooks.isEmpty()) { %>
                    <a href="<%= ctx %>/books" class="btn btn-outline" title="Xóa bộ lọc">
                        <i class="fa-solid fa-rotate-right"></i>
                    </a>
                    <% } %>
                </div>
            </div>
        </div>
    </form>

    <% if (dbError != null) { %>
        <div class="alert alert-danger">
            <i class="fa-solid fa-circle-xmark"></i> <%= dbError %>
        </div>
    <% } %>
    <% if ("deleted".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">
            <i class="fa-solid fa-circle-check"></i> Xóa sách thành công!
        </div>
    <% } %>

    <!-- ==================== TOPBAR ==================== -->
    <div class="books-topbar">
        <div class="results-info" style="margin-bottom:0;">
            <% if (!keyword.isEmpty() || !selectedCategory.isEmpty()) { %>
                <i class="fa-solid fa-filter fa-xs" style="color:var(--primary);"></i>
                Kết quả: <strong><%= totalRecords %></strong> sách
                <% if (!keyword.isEmpty()) { %> cho “<strong><%= keyword %></strong>”<% } %>
                <% if (!selectedCategory.isEmpty()) { %> trong “<strong><%= selectedCategory %></strong>”<% } %>
            <% } else { %>
                <i class="fa-solid fa-books fa-xs" style="color:var(--primary); margin-right:4px;"></i>
                Tổng cộng <strong><%= totalRecords %></strong> đầu sách
            <% } %>
        </div>

        <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
            <!-- Sort -->
            <div class="sort-group">
                <span class="sort-label"><i class="fa-solid fa-arrow-up-wide-short"></i> Sắp xếp:</span>
                <%
                    String[][] sortOptions = {
                        {"title",        "Tên sách"},
                        {"publish_year", "Năm XB"},
                        {"available",    "Còn sách"},
                        {"price",        "Giá"}
                    };
                    for (String[] so : sortOptions) {
                        String sf = so[0], sl = so[1];
                        boolean active = sf.equals(sortField);
                        String thisOrder = active ? nextOrder : "ASC";
                        String icon = active ? ("ASC".equals(sortOrder) ? " ▲" : " ▼") : "";
                %>
                    <a href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sf %>&order=<%= thisOrder %>&page=1&view=<%= viewMode %>"
                       class="sort-btn <%= active ? "sort-btn-active" : "" %>">
                        <%= sl %><%= icon %>
                    </a>
                <% } %>
            </div>

            <!-- View Toggle -->
            <div class="view-toggle">
                <button type="button" id="viewGrid" class="view-toggle-btn <%= "grid".equals(viewMode) ? "active" : "" %>"
                        title="Dạng lưới" onclick="setView('grid')">
                    <i class="fa-solid fa-grip"></i>
                </button>
                <button type="button" id="viewTable" class="view-toggle-btn <%= "table".equals(viewMode) ? "active" : "" %>"
                        title="Dạng bảng" onclick="setView('table')">
                    <i class="fa-solid fa-list"></i>
                </button>
            </div>

            <!-- Admin add button -->
            <% if (isAdmin) { %>
                <a href="<%= ctx %><%= rolePathBooks != null ? rolePathBooks : "" %>/book/add" class="btn btn-primary btn-sm">
                    <i class="fa-solid fa-plus"></i> Thêm sách
                </a>
            <% } %>
        </div>
    </div>

    <!-- ==================== BOOK LIST ==================== -->
    <% if (books == null || books.isEmpty()) { %>
        <div class="empty-state" style="padding:80px 24px;">
            <div class="empty-icon"><i class="fa-solid fa-magnifying-glass"></i></div>
            <h3>Không tìm thấy sách nào</h3>
            <p>Thử thay đổi từ khóa hoặc bộ lọc danh mục.</p>
            <a href="<%= ctx %><%= rolePathBooks != null ? rolePathBooks : "" %>/books" class="btn btn-outline" style="margin-top:16px;">
                <i class="fa-solid fa-rotate-right"></i> Xóa bộ lọc
            </a>
        </div>

    <% } else if ("table".equals(viewMode)) { %>
        <!-- ===== TABLE VIEW ===== -->
        <div class="data-table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width:50px;">#</th>
                        <th style="width:48px;"></th>
                        <th>
                            <a href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=title&order=<%= "title".equals(sortField) ? nextOrder : "ASC" %>&page=1&view=table">
                                Tên sách <i class="fa-solid <%= "title".equals(sortField) ? ("ASC".equals(sortOrder)?"fa-sort-up":"fa-sort-down") : "fa-sort" %> sort-icon fa-xs"></i>
                            </a>
                        </th>
                        <th>Danh mục</th>
                        <th>
                            <a href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=publish_year&order=<%= "publish_year".equals(sortField) ? nextOrder : "DESC" %>&page=1&view=table">
                                Năm XB <i class="fa-solid <%= "publish_year".equals(sortField) ? ("ASC".equals(sortOrder)?"fa-sort-up":"fa-sort-down") : "fa-sort" %> sort-icon fa-xs"></i>
                            </a>
                        </th>
                        <th>
                            <a href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=available&order=<%= "available".equals(sortField) ? nextOrder : "DESC" %>&page=1&view=table">
                                Trạng thái <i class="fa-solid <%= "available".equals(sortField) ? ("ASC".equals(sortOrder)?"fa-sort-up":"fa-sort-down") : "fa-sort" %> sort-icon fa-xs"></i>
                            </a>
                        </th>
                        <th>
                            <a href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=price&order=<%= "price".equals(sortField) ? nextOrder : "ASC" %>&page=1&view=table">
                                Giá <i class="fa-solid <%= "price".equals(sortField) ? ("ASC".equals(sortOrder)?"fa-sort-up":"fa-sort-down") : "fa-sort" %> sort-icon fa-xs"></i>
                            </a>
                        </th>
                        <% if (isAdmin || (loggedUser != null && !isAdminLib)) { %><th style="width:140px; text-align:center;">Thao tác</th><% } %>
                    </tr>
                </thead>
                <tbody>
                    <% int rowNum = (currentPageNum - 1) * 12 + 1;
                       for (Book b : books) { %>
                        <tr>
                            <td style="color:var(--text-muted); font-size:0.82rem;"><%= rowNum++ %></td>
                            <td>
                                <% if (b.getCoverImage() != null && !b.getCoverImage().isEmpty()) { %>
                                    <img src="<%= utils.UploadUtility.resolveUrl(b.getCoverImage(), request.getContextPath()) %>" class="book-thumb"
                                         alt="<%= b.getTitle() %>"
                                         onerror="this.src=''; this.style.background='var(--bg-surface)';">
                                <% } else { %>
                                    <div class="book-thumb" style="background:var(--bg-surface);display:flex;align-items:center;justify-content:center;color:var(--text-muted);font-size:1rem;">
                                        <i class="fa-solid fa-book"></i>
                                    </div>
                                <% } %>
                            </td>
                            <td class="book-info-cell">
                                <a href="<%= detailBase %>?id=<%= b.getId() %>" class="book-title-link" title="<%= b.getTitle() %>"><%= b.getTitle() %></a>
                                <span class="book-isbn"><%= b.getIsbn() %></span>
                            </td>
                            <td>
                                <a href="<%= ctx %>/books?category=<%= java.net.URLEncoder.encode(b.getCategory() != null ? b.getCategory() : "","UTF-8") %>&view=table"
                                   class="badge badge-primary">
                                    <%= b.getCategory() != null ? b.getCategory() : "—" %>
                                </a>
                            </td>
                            <td><%= b.getPublishYear() != null ? b.getPublishYear() : "—" %></td>
                            <td>
                                <span class="badge <%= b.getAvailable() > 0 ? "badge-success" : (b.getQuantity() > 0 ? "badge-warning" : "badge-danger") %>">
                                    <%= b.getStatusLabel() %>
                                </span>
                                <% if (loggedUser != null) { %>
                                    <span style="font-size:0.75rem;color:var(--text-muted);display:block;margin-top:2px;">
                                        <%= b.getAvailable() %>/<%= b.getQuantity() %> bản
                                    </span>
                                <% } %>
                            </td>
                            <td style="font-weight:600; color:var(--accent);"><%= b.getFormattedPrice() %></td>
                            <% if (isAdmin || (loggedUser != null && !isAdminLib)) { %>
                            <td style="text-align:center;">
                                <div style="display:flex; gap:6px; justify-content:center;">
                                    <% if (isAdmin) { %>
                                    <a href="<%= detailBase %>?id=<%= b.getId() %>"
                                       class="btn btn-outline btn-sm" title="Xem chi tiết">
                                        <i class="fa-solid fa-eye"></i>
                                    </a>
                                    <a href="<%= ctx %><%= rolePathBooks != null ? rolePathBooks : "" %>/book/edit?id=<%= b.getId() %>"
                                       class="btn btn-outline btn-sm" title="Chỉnh sửa">
                                        <i class="fa-solid fa-pen"></i> 
                                    </a>
                                    <button type="button"
                                            class="btn btn-danger btn-sm"
                                            title="Xóa sách"
                                            onclick="confirmDelete(<%= b.getId() %>, '<%= b.getTitle().replace("'", "\\'") %>')">
                                        <i class="fa-solid fa-trash"></i>
                                    </button>
                                    <% } %>
                                    <% if (loggedUser != null && !isAdminLib) { %>
                                    <a href="<%= ctx %>/book/detail?id=<%= b.getId() %>"
                                       class="btn btn-outline btn-sm" title="Xem chi tiết">
                                        <i class="fa-solid fa-eye"></i>
                                    </a>
                                    <form method="post" action="<%= ctx %>/reservation/create" style="display:inline;margin:0;">
                                        <input type="hidden" name="bookId" value="<%= b.getId() %>">
                                        <button type="submit" class="btn btn-sm" title="Đặt trước">
                                            <i class="fa-solid fa-bookmark"></i> Đặt trước
                                        </button>
                                    </form>
                                    <% } %>
                                </div>
                            </td>
                            <% } %>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

    <% } else { %>
        <!-- ===== GRID VIEW ===== -->
        <div class="books-grid">
            <% for (Book b : books) { %>
                <div class="book-card">
                    <a href="<%= detailBase %>?id=<%= b.getId() %>" class="book-cover">
                        <% if (b.getCoverImage() != null && !b.getCoverImage().trim().isEmpty()) { %>
                            <img src="<%= utils.UploadUtility.resolveUrl(b.getCoverImage(), request.getContextPath()) %>"
                                 alt="<%= b.getTitle() %>"
                                 onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
                            <div class="book-cover-placeholder" style="display:none;">
                                <i class="fa-solid fa-book-open"></i>
                                <span><%= b.getTitle() %></span>
                            </div>
                        <% } else { %>
                            <div class="book-cover-placeholder">
                                <i class="fa-solid fa-book-open"></i>
                                <span><%= b.getTitle() %></span>
                            </div>
                        <% } %>
                        <span class="book-status-tag <%= b.getStatusClass() %>">
                            <%= b.getStatusLabel() %>
                        </span>
                    </a>
                    <div class="book-body">
                        <div class="book-category">
                            <a href="<%= ctx %>/books?category=<%= java.net.URLEncoder.encode(b.getCategory() != null ? b.getCategory() : "","UTF-8") %>&view=grid"
                               style="color:var(--primary); text-decoration:none;">
                                <%= b.getCategory() != null ? b.getCategory() : "—" %>
                            </a>
                        </div>
                        <a href="<%= detailBase %>?id=<%= b.getId() %>" class="book-title" title="<%= b.getTitle() %>"><%= b.getTitle() %></a>
                        <% if (b.getPublisher() != null) { %>
                            <div class="book-publisher">
                                <i class="fa-solid fa-building fa-xs"></i>
                                <%= b.getPublisher() %>
                                <% if (b.getPublishYear() != null) { %> · <%= b.getPublishYear() %><% } %>
                            </div>
                        <% } %>
                        <div class="book-price"><%= b.getFormattedPrice() %></div>
                    </div>
                    <div class="book-footer">
                        <% if (loggedUser != null) { %>
                            <span style="font-size:0.78rem;color:var(--text-muted);">
                                <i class="fa-solid fa-layer-group fa-xs"></i>
                                <%= b.getAvailable() %>/<%= b.getQuantity() %> còn
                            </span>
                        <% } else { %>
                            <span></span>
                        <% } %>
                        <div style="display:flex; gap:6px;">
                            <a href="<%= detailBase %>?id=<%= b.getId() %>" class="btn btn-outline btn-sm" title="Xem chi tiết">
                                <i class="fa-solid fa-eye"></i>
                            </a>
                            <% if (loggedUser != null && !isAdminLib) { %>
                            <% if (b.getAvailable() > 0) { %><form method="post" action="<%= ctx %>/borrow/create" class="book-borrow-form">
                                <input type="hidden" name="bookId" value="<%= b.getId() %>">
                                <button type="submit" class="btn btn-sm"
                                        title="Gửi yêu cầu mượn sách"
                                        style="background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;border:none;padding:5px 10px;border-radius:6px;font-size:0.78rem;cursor:pointer;font-weight:600;white-space:nowrap;">
                                    <i class="fa-solid fa-book-open"></i> Mượn sách
                                </button>
                            </form><% } else { %><a class="btn btn-sm" href="<%= ctx %>/reservation/create?bookId=<%= b.getId() %>"><i class="fa-solid fa-bookmark"></i> Đặt trước</a><% } %>
                            <% } %>
                            <% if (isAdmin) { %>
                            <a href="<%= ctx %><%= rolePathBooks != null ? rolePathBooks : "" %>/book/edit?id=<%= b.getId() %>" class="btn btn-outline btn-sm" title="Chỉnh sửa">
                                <i class="fa-solid fa-pen"></i>
                            </a>
                            <button type="button"
                                    class="btn btn-danger btn-sm"
                                    title="Xóa sách"
                                    onclick="confirmDelete(<%= b.getId() %>, '<%= b.getTitle().replace("'", "\\'") %>')">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                            <% } %>
                        </div>
                    </div>
                </div>
            <% } %>
        </div>
    <% } %>

    <!-- ==================== PAGINATION ==================== -->
    <% if (totalPages > 1) { %>
        <nav aria-label="Phân trang">
            <ul class="pagination">
                <!-- Prev -->
                <li class="page-item <%= currentPageNum <= 1 ? "disabled" : "" %>">
                    <a class="page-link"
                       href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= currentPageNum - 1 %>&view=<%= viewMode %>">
                        <i class="fa-solid fa-chevron-left fa-xs"></i>
                    </a>
                </li>

                <% 
                   if (totalPages <= 7) {
                       for (int pg = 1; pg <= totalPages; pg++) { %>
                           <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                               <a class="page-link" href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                           </li>
                       <% }
                   } else {
                       // Show first 2 pages
                       for (int pg = 1; pg <= 2; pg++) { %>
                           <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                               <a class="page-link" href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                           </li>
                       <% }

                       if (currentPageNum <= 4) {
                           // Current page is near the start
                           for (int pg = 3; pg <= 5; pg++) { %>
                               <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                   <a class="page-link" href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% } else if (currentPageNum >= totalPages - 3) { %>
                           <%-- Current page is near the end --%>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = totalPages - 4; pg <= totalPages - 2; pg++) { %>
                               <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                   <a class="page-link" href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                               </li>
                           <% }
                       } else { %>
                           <%-- Current page is in the middle --%>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = currentPageNum - 1; pg <= currentPageNum + 1; pg++) { %>
                               <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                   <a class="page-link" href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% }

                       // Show last 2 pages
                       for (int pg = totalPages - 1; pg <= totalPages; pg++) { %>
                           <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                               <a class="page-link" href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                           </li>
                       <% }
                   }
                %>

                <!-- Next -->
                <li class="page-item <%= currentPageNum >= totalPages ? "disabled" : "" %>">
                    <a class="page-link"
                       href="<%= ctx %>/books?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= currentPageNum + 1 %>&view=<%= viewMode %>">
                        <i class="fa-solid fa-chevron-right fa-xs"></i>
                    </a>
                </li>
            </ul>
        </nav>
    <% } %>

</div><!-- /container -->
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>

<!-- ==================== DELETE MODAL ==================== -->
<div id="deleteModal" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.7); z-index:9999; align-items:center; justify-content:center; backdrop-filter:blur(4px);">
    <div style="background:var(--bg-card); border:1px solid var(--border-light); border-radius:var(--radius-lg); padding:36px; max-width:440px; width:90%; box-shadow:var(--shadow-lg); position:relative;">
        <div style="font-size:2.5rem; margin-bottom:14px; text-align:center;">🗑️</div>
        <h3 style="font-size:1.15rem; font-weight:700; color:var(--text-primary); margin-bottom:10px; text-align:center;">Xác nhận xóa sách</h3>
        <p style="color:var(--text-secondary); font-size:0.9rem; margin-bottom:28px; text-align:center; line-height:1.6;" id="deleteBookTitle"></p>
        <div style="display:flex; gap:12px; justify-content:flex-end;">
            <button onclick="closeDeleteModal()" class="btn btn-outline">Hủy</button>
            <a id="deleteConfirmBtn" href="#" class="btn btn-danger">
                <i class="fa-solid fa-trash"></i> Xóa
            </a>
        </div>
    </div>
</div>

<script>
// ---- View toggle ----
function setView(mode) {
    var url = new URL(window.location.href);
    url.searchParams.set('view', mode);
    url.searchParams.set('page', '1');
    window.location.href = url.toString();
}

// ---- Delete modal ----
function confirmDelete(bookId, bookTitle) {
    document.getElementById('deleteBookTitle').textContent =
        'Bạn có chắc muốn xóa/ẩn sách: "' + bookTitle + '"?';
    document.getElementById('deleteConfirmBtn').href =
        '<%= ctx %>/book/delete?id=' + bookId;
    document.getElementById('deleteModal').style.display = 'flex';
}
function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
}
document.getElementById('deleteModal').addEventListener('click', function(e) {
    if (e.target === this) closeDeleteModal();
});

// ---- Search validation ----
document.getElementById('searchForm').addEventListener('submit', function(e) {
    var kw = document.getElementById('keywordInput').value.trim();
    if (kw.length > 200) {
        alert('Từ khóa không được vượt quá 200 ký tự.');
        e.preventDefault();
    }
});
</script>
