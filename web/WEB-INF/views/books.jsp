<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Book, model.User, service.BookExcelService.ImportResult, java.util.List" %>
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

    String sessionSuccess = (String) session.getAttribute("successMsg");
    if (sessionSuccess != null) { session.removeAttribute("successMsg"); }
    String sessionDbError = (String) session.getAttribute("dbError");
    if (sessionDbError != null) { session.removeAttribute("dbError"); dbError = sessionDbError; }
    ImportResult importResult = (ImportResult) session.getAttribute("importResult");
    if (importResult != null) { session.removeAttribute("importResult"); }

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
    String booksBaseUrl  = ctx + (rolePathBooks != null ? rolePathBooks : "") + "/books";
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
    <form id="searchForm" action="<%= booksBaseUrl %>" method="get" novalidate>
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
                    <a href="<%= booksBaseUrl %>" class="btn btn-outline" title="Xóa bộ lọc">
                        <i class="fa-solid fa-rotate-right"></i>
                    </a>
                </div>
            </div>
        </div>
    </form>

    <% if (sessionSuccess != null) { %>
        <div class="alert alert-success">
            <i class="fa-solid fa-circle-check"></i> <%= sessionSuccess %>
        </div>
    <% } %>
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

    <% if (importResult != null) { %>
        <div class="card" style="margin-bottom: 20px; border-left: 4px solid <%= importResult.getFailureCount() > 0 ? "#e11d48" : "#16a34a" %>; background: #ffffff; padding: 18px 22px; border-radius: var(--radius-md); box-shadow: var(--shadow-sm);">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
                <div>
                    <h3 style="font-size: 1.05rem; font-weight: 700; color: var(--text-primary); margin: 0 0 4px 0;">
                        <i class="fa-solid fa-file-import" style="color: <%= importResult.getFailureCount() > 0 ? "#e11d48" : "#16a34a" %>;"></i>
                        Kết quả Nhập dữ liệu sách từ Excel
                    </h3>
                    <p style="font-size: 0.88rem; color: var(--text-muted); margin: 0;">
                        Tổng số dòng: <strong><%= importResult.getTotalRows() %></strong> | 
                        Thành công: <strong style="color: #16a34a;"><%= importResult.getSuccessCount() %></strong> | 
                        Thất bại: <strong style="color: #e11d48;"><%= importResult.getFailureCount() %></strong>
                    </p>
                </div>
                <% if (!importResult.getErrorList().isEmpty() || !importResult.getSuccessList().isEmpty()) { %>
                    <button type="button" class="btn btn-sm btn-outline" onclick="toggleImportDetails()" id="btnToggleImportDetails">
                        <i class="fa-solid fa-chevron-down"></i> Xem chi tiết
                    </button>
                <% } %>
            </div>

            <div id="importDetailsContainer" style="display: none; margin-top: 14px; padding-top: 14px; border-top: 1px dashed var(--border);">
                <% if (!importResult.getErrorList().isEmpty()) { %>
                    <div style="margin-bottom: 12px;">
                        <strong style="font-size: 0.85rem; color: #e11d48; text-transform: uppercase; letter-spacing: 0.5px;">
                            <i class="fa-solid fa-circle-exclamation"></i> Danh sách lỗi (<%= importResult.getErrorList().size() %> dòng)
                        </strong>
                        <ul style="margin: 6px 0 0 18px; padding: 0; font-size: 0.86rem; color: #b91c1c; line-height: 1.5;">
                            <% for (String err : importResult.getErrorList()) { %>
                                <li><%= err %></li>
                            <% } %>
                        </ul>
                    </div>
                <% } %>

                <% if (!importResult.getSuccessList().isEmpty()) { %>
                    <div>
                        <strong style="font-size: 0.85rem; color: #16a34a; text-transform: uppercase; letter-spacing: 0.5px;">
                            <i class="fa-solid fa-circle-check"></i> Danh sách sách đã thêm (<%= importResult.getSuccessList().size() %> sách)
                        </strong>
                        <ul style="margin: 6px 0 0 18px; padding: 0; font-size: 0.86rem; color: #15803d; line-height: 1.5; max-height: 150px; overflow-y: auto;">
                            <% for (String succ : importResult.getSuccessList()) { %>
                                <li><%= succ %></li>
                            <% } %>
                        </ul>
                    </div>
                <% } %>
            </div>
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
                    <a href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sf %>&order=<%= thisOrder %>&page=1&view=<%= viewMode %>"
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

            <!-- Excel Export / Import buttons for Admin & Librarian -->
            <% if (isAdminLib) { %>
                <a href="<%= ctx %><%= rolePathBooks != null ? rolePathBooks : "" %>/book/export?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>"
                   class="btn btn-outline btn-sm" title="Xuất danh sách sách ra file Excel (CSV UTF-8)">
                    <i class="fa-solid fa-file-excel" style="color: #16a34a;"></i> Xuất Excel
                </a>
                <button type="button" class="btn btn-outline btn-sm" onclick="openBookImportModal()" title="Nhập danh sách sách từ file Excel (CSV UTF-8)">
                    <i class="fa-solid fa-file-import" style="color: #0284c7;"></i> Nhập từ Excel
                </button>
            <% } %>

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
                            <a href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=title&order=<%= "title".equals(sortField) ? nextOrder : "ASC" %>&page=1&view=table">
                                Tên sách <i class="fa-solid <%= "title".equals(sortField) ? ("ASC".equals(sortOrder)?"fa-sort-up":"fa-sort-down") : "fa-sort" %> sort-icon fa-xs"></i>
                            </a>
                        </th>
                        <th>Danh mục</th>
                        <th>
                            <a href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=publish_year&order=<%= "publish_year".equals(sortField) ? nextOrder : "DESC" %>&page=1&view=table">
                                Năm XB <i class="fa-solid <%= "publish_year".equals(sortField) ? ("ASC".equals(sortOrder)?"fa-sort-up":"fa-sort-down") : "fa-sort" %> sort-icon fa-xs"></i>
                            </a>
                        </th>
                        <th>
                            <a href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=available&order=<%= "available".equals(sortField) ? nextOrder : "DESC" %>&page=1&view=table">
                                Trạng thái <i class="fa-solid <%= "available".equals(sortField) ? ("ASC".equals(sortOrder)?"fa-sort-up":"fa-sort-down") : "fa-sort" %> sort-icon fa-xs"></i>
                            </a>
                        </th>
                        <th>
                            <a href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=price&order=<%= "price".equals(sortField) ? nextOrder : "ASC" %>&page=1&view=table">
                                Giá <i class="fa-solid <%= "price".equals(sortField) ? ("ASC".equals(sortOrder)?"fa-sort-up":"fa-sort-down") : "fa-sort" %> sort-icon fa-xs"></i>
                            </a>
                        </th>
                        <% if (isAdminLib || loggedUser != null) { %><th style="width:160px; text-align:center;">Thao tác</th><% } %>
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
                                <a href="<%= booksBaseUrl %>?category=<%= java.net.URLEncoder.encode(b.getCategory() != null ? b.getCategory() : "","UTF-8") %>&view=table"
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
                            <% if (isAdminLib || loggedUser != null) { %>
                            <td style="text-align:center;">
                                <div style="display:flex; gap:6px; justify-content:center;">
                                    <% if (isAdminLib) { %>
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
                                    <% } else { %>
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
                            <a href="<%= booksBaseUrl %>?category=<%= java.net.URLEncoder.encode(b.getCategory() != null ? b.getCategory() : "","UTF-8") %>&view=grid"
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
                       href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= currentPageNum - 1 %>&view=<%= viewMode %>">
                        <i class="fa-solid fa-chevron-left fa-xs"></i>
                    </a>
                </li>

                <% 
                   if (totalPages <= 7) {
                       for (int pg = 1; pg <= totalPages; pg++) { %>
                           <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                               <a class="page-link" href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                           </li>
                       <% }
                   } else {
                       // Show first 2 pages
                       for (int pg = 1; pg <= 2; pg++) { %>
                           <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                               <a class="page-link" href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                           </li>
                       <% }

                       if (currentPageNum <= 4) {
                           // Current page is near the start
                           for (int pg = 3; pg <= 5; pg++) { %>
                               <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                   <a class="page-link" href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% } else if (currentPageNum >= totalPages - 3) { %>
                           <%-- Current page is near the end --%>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = totalPages - 4; pg <= totalPages - 2; pg++) { %>
                               <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                   <a class="page-link" href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                               </li>
                           <% }
                       } else { %>
                           <%-- Current page is in the middle --%>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = currentPageNum - 1; pg <= currentPageNum + 1; pg++) { %>
                               <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                   <a class="page-link" href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% }

                       // Show last 2 pages
                       for (int pg = totalPages - 1; pg <= totalPages; pg++) { %>
                           <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                               <a class="page-link" href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= pg %>&view=<%= viewMode %>"><%= pg %></a>
                           </li>
                       <% }
                   }
                %>

                <!-- Next -->
                <li class="page-item <%= currentPageNum >= totalPages ? "disabled" : "" %>">
                    <a class="page-link"
                       href="<%= booksBaseUrl %>?keyword=<%= java.net.URLEncoder.encode(keyword,"UTF-8") %>&category=<%= java.net.URLEncoder.encode(selectedCategory,"UTF-8") %>&sort=<%= sortField %>&order=<%= sortOrder %>&page=<%= currentPageNum + 1 %>&view=<%= viewMode %>">
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

<!-- ==================== IMPORT EXCEL MODAL ==================== -->
<div id="bookImportModal" style="display:none; position:fixed; inset:0; background:rgba(15,23,42,0.6); z-index:9999; align-items:center; justify-content:center; backdrop-filter:blur(4px); padding:20px;">
    <div style="background:#ffffff; border-radius:16px; max-width:540px; width:100%; box-shadow:0 20px 40px rgba(0,0,0,0.15); overflow:hidden; border:1px solid #e2e8f0; animation:modalFadeIn 0.2s ease-out;">
        <!-- Header -->
        <div style="display:flex; justify-content:space-between; align-items:center; padding:18px 24px; border-bottom:1px solid #edf2f7; background:linear-gradient(135deg, #fffcf9 0%, #fff5eb 100%);">
            <div style="display:flex; align-items:center; gap:10px;">
                <div style="width:36px; height:36px; border-radius:8px; background:rgba(244,121,32,0.12); display:flex; align-items:center; justify-content:center; color:var(--primary); font-size:1.1rem;">
                    <i class="fa-solid fa-file-excel"></i>
                </div>
                <div>
                    <h3 style="margin:0; font-size:1.1rem; font-weight:800; color:var(--text-primary); font-family:'Be Vietnam Pro',sans-serif;">Nhập sách từ Excel (CSV)</h3>
                    <p style="margin:2px 0 0 0; font-size:0.8rem; color:var(--text-muted);">Thêm hàng loạt đầu sách và tự động tạo bản sao</p>
                </div>
            </div>
            <button type="button" onclick="closeBookImportModal()" style="background:none; border:none; color:#94a3b8; font-size:1.2rem; cursor:pointer; padding:4px 8px; border-radius:6px;" title="Đóng">
                <i class="fa-solid fa-xmark"></i>
            </button>
        </div>

        <!-- Body Form -->
        <form action="<%= ctx %><%= rolePathBooks != null ? rolePathBooks : "" %>/book/import" method="post" enctype="multipart/form-data" id="formImportBooks" style="padding:22px 24px;" onsubmit="return handleImportSubmit();">
            <!-- Template Download Banner -->
            <div style="background:#f8fafc; border:1px dashed #cbd5e1; border-radius:10px; padding:14px 16px; margin-bottom:20px; display:flex; justify-content:space-between; align-items:center; gap:12px;">
                <div>
                    <div style="font-weight:700; font-size:0.88rem; color:#334155;">Chưa có file mẫu chuẩn?</div>
                    <div style="font-size:0.78rem; color:#64748b; margin-top:2px;">Tải file mẫu có sẵn dữ liệu minh họa để điền</div>
                </div>
                <a href="<%= ctx %><%= rolePathBooks != null ? rolePathBooks : "" %>/book/template" class="btn btn-outline btn-sm" style="font-size:0.82rem; white-space:nowrap; padding:6px 12px;">
                    <i class="fa-solid fa-download"></i> Tải file mẫu
                </a>
            </div>

            <!-- File Drop Area -->
            <div style="margin-bottom:18px;">
                <label style="display:block; font-size:0.86rem; font-weight:700; color:#334155; margin-bottom:8px;">
                    Chọn file dữ liệu (.csv) <span style="color:#ef4444;">*</span>
                </label>
                <div id="dropZone" style="border:2px dashed #cbd5e1; border-radius:12px; padding:24px 16px; text-align:center; background:#fafafa; cursor:pointer; transition:all 0.2s ease;" onclick="document.getElementById('excelFileInput').click();">
                    <i class="fa-solid fa-cloud-arrow-up" style="font-size:2.2rem; color:var(--primary); margin-bottom:8px; display:block;"></i>
                    <span id="dropZoneText" style="font-size:0.9rem; font-weight:600; color:#475569; display:block;">Nhấn để chọn file hoặc kéo thả vào đây</span>
                    <span style="font-size:0.76rem; color:#94a3b8; display:block; margin-top:4px;">Chấp nhận file định dạng .csv chuẩn UTF-8 (tối đa 10MB)</span>
                    <input type="file" id="excelFileInput" name="excelFile" accept=".csv,text/csv,text/plain" style="display:none;" onchange="handleFileChosen(this);">
                </div>
            </div>

            <!-- Notes list -->
            <div style="background:#fffbeb; border-radius:8px; padding:12px 14px; margin-bottom:22px; font-size:0.8rem; color:#92400e; line-height:1.5;">
                <div style="font-weight:700; margin-bottom:4px;"><i class="fa-solid fa-circle-info"></i> Lưu ý khi nhập dữ liệu:</div>
                <ul style="margin:0; padding-left:18px;">
                    <li>Mã <strong>ISBN</strong> và <strong>Tên sách</strong> là 2 cột bắt buộc.</li>
                    <li>Nhiều tác giả phân cách bằng dấu chấm phẩy (<code>;</code>) hoặc phẩy (<code>,</code>).</li>
                    <li>Hệ thống sẽ tự động tạo các bản sao (mã <code>BC...</code>) theo số lượng đã nhập.</li>
                </ul>
            </div>

            <!-- Actions -->
            <div style="display:flex; justify-content:flex-end; gap:10px;">
                <button type="button" class="btn btn-outline" onclick="closeBookImportModal()">Hủy bỏ</button>
                <button type="submit" class="btn btn-primary" id="btnSubmitImport">
                    <i class="fa-solid fa-file-import"></i> Bắt đầu Nhập sách
                </button>
            </div>
        </form>
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
        '<%= ctx %><%= rolePathBooks != null ? rolePathBooks : "" %>/book/delete?id=' + bookId;
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

// ---- Book Import Modal ----
function openBookImportModal() {
    document.getElementById('bookImportModal').style.display = 'flex';
}
function closeBookImportModal() {
    document.getElementById('bookImportModal').style.display = 'none';
}
document.getElementById('bookImportModal').addEventListener('click', function(e) {
    if (e.target === this) closeBookImportModal();
});

function handleFileChosen(input) {
    if (input.files && input.files[0]) {
        var file = input.files[0];
        document.getElementById('dropZoneText').innerHTML = 'Đã chọn: <strong style="color:var(--primary);">' + file.name + '</strong> (' + (file.size / 1024).toFixed(1) + ' KB)';
        document.getElementById('dropZone').style.borderColor = 'var(--primary)';
        document.getElementById('dropZone').style.background = '#fffaf5';
    }
}

function handleImportSubmit() {
    var input = document.getElementById('excelFileInput');
    if (!input.files || input.files.length === 0) {
        alert('Vui lòng chọn một file .csv trước khi nhấn Nhập sách.');
        return false;
    }
    var btn = document.getElementById('btnSubmitImport');
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';
    return true;
}

// Drag & drop handlers
var dropZone = document.getElementById('dropZone');
if (dropZone) {
    ['dragenter', 'dragover'].forEach(function(eventName) {
        dropZone.addEventListener(eventName, function(e) {
            e.preventDefault();
            e.stopPropagation();
            dropZone.style.borderColor = 'var(--primary)';
            dropZone.style.background = '#fffaf5';
        }, false);
    });
    ['dragleave', 'drop'].forEach(function(eventName) {
        dropZone.addEventListener(eventName, function(e) {
            e.preventDefault();
            e.stopPropagation();
        }, false);
    });
    dropZone.addEventListener('drop', function(e) {
        var dt = e.dataTransfer;
        var files = dt.files;
        if (files && files.length > 0) {
            document.getElementById('excelFileInput').files = files;
            handleFileChosen(document.getElementById('excelFileInput'));
        }
    }, false);
}

// Toggle import details
function toggleImportDetails() {
    var container = document.getElementById('importDetailsContainer');
    var btn = document.getElementById('btnToggleImportDetails');
    if (!container) return;
    if (container.style.display === 'none') {
        container.style.display = 'block';
        btn.innerHTML = '<i class="fa-solid fa-chevron-up"></i> Thu gọn';
    } else {
        container.style.display = 'none';
        btn.innerHTML = '<i class="fa-solid fa-chevron-down"></i> Xem chi tiết';
    }
}
</script>

