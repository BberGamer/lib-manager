<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="users" scope="request" />
<c:set var="pageTitle" value="Quản lý người dùng – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/user-list.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<% List<User> users = (List<User>) request.getAttribute("users");
        Integer totalRecords = (Integer) request.getAttribute("totalRecords");
        Integer totalPages = (Integer) request.getAttribute("totalPages");
        Integer currentPageNum = (Integer) request.getAttribute("currentPageNum");
        String q = (String) request.getAttribute("q");
        String roleFilter = (String) request.getAttribute("roleFilter");
        Integer activeFilter = (Integer) request.getAttribute("activeFilter");
        String sortField = (String) request.getAttribute("sortField");
        String sortOrder = (String) request.getAttribute("sortOrder");

        // Read session messages (pattern consistent with author/category)
        String sessionSuccess = (String) session.getAttribute("successMsg");
        if (sessionSuccess != null) { request.setAttribute("successMsg", sessionSuccess);
        session.removeAttribute("successMsg"); }
        String sessionError = (String) session.getAttribute("errorMsg");
        if (sessionError != null) { request.setAttribute("errorMsg", sessionError);
        session.removeAttribute("errorMsg"); }

        String successMsg = (String) request.getAttribute("successMsg");
        String errorMsg = (String) request.getAttribute("errorMsg");
        // Also support legacy request params from AdminUserServlet redirects
        if (successMsg == null) successMsg = (String) request.getAttribute("success");
        if (errorMsg == null) errorMsg = (String) request.getAttribute("error");

        User logged = (User) session.getAttribute("loggedUser");
        boolean isAdmin = (logged != null && logged.isAdmin());
        String ctx = request.getContextPath();
        String currentPath = (String) request.getAttribute("currentServletPath");
        if (currentPath == null || currentPath.isEmpty()) {
        currentPath = "/admin/users";
        }

        if (totalRecords == null) totalRecords = (users != null ? users.size() : 0);
        if (totalPages == null) totalPages = 1;
        if (currentPageNum == null) currentPageNum = 1;
        if (q == null) q = "";
        if (sortField == null) sortField = "username";
        if (sortOrder == null) sortOrder = "ASC";

        String nextOrder = "ASC".equals(sortOrder) ? "DESC" : "ASC";
%>

<main class="page-wrapper">

    <!-- ===== PAGE HEADER ===== -->
    <div class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow user-hero-eyebrow">
                        <i class="fa-solid fa-users"></i> Người dùng
                    </div>
                    <h1 class="books-page-title">Quản lý Người dùng</h1>
                    <p class="books-page-subtitle">Xem, tìm kiếm và quản lý tài khoản trong
                        hệ thống thư viện</p>
                </div>
                <div class="books-page-stats">
                    <div class="bps-item">
                        <span class="bps-num">
                            <%= totalRecords %>
                        </span>
                        <span class="bps-lbl">Người dùng</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="container user-list-container">

        <%-- Form Tìm kiếm, Lọc theo Vai trò, Trạng thái và Phân trang (gửi theo phương thức
            GET tới UserListServlet) --%>
        <form id="searchForm" action="<%= ctx %><%= currentPath %>" method="get">
            <input type="hidden" name="sort" value="<%= sortField %>">
            <input type="hidden" name="order" value="<%= sortOrder %>">
            <input type="hidden" name="page" value="1">

            <div class="search-bar-wrapper">
                <div class="search-bar-inner">
                    <%-- Ô nhập từ khóa tìm kiếm theo username, họ tên hoặc email --%>
                    <div class="search-field user-search-keyword">
                        <label for="keywordInput">Tìm kiếm người dùng</label>
                        <div class="search-input-wrap">
                            <i class="fa-solid fa-magnifying-glass search-icon"></i>
                            <input type="text" id="keywordInput" name="q"
                                   class="form-control"
                                   placeholder="Tìm theo tên đăng nhập, họ tên, email..."
                                   value="<%= q %>" maxlength="200" autocomplete="off">
                        </div>
                    </div>

                    <%-- Bộ lọc Vai trò (Role Filter: ADMIN, LIBRARIAN, READER) --%>
                    <div class="search-field select-field">
                        <label for="roleSelect">Vai trò</label>
                        <select id="roleSelect" name="role" class="form-select">
                            <option value="">-- Tất cả vai trò --</option>
                            <option value="ADMIN" <%="ADMIN" .equals(roleFilter)
                                                                        ? "selected" : "" %>>ADMIN</option>
                            <option value="LIBRARIAN" <%="LIBRARIAN"
                                                                        .equals(roleFilter) ? "selected" : "" %>
                                    >LIBRARIAN</option>
                            <option value="READER" <%="READER"
                                                                        .equals(roleFilter) ? "selected" : "" %>>READER
                            </option>
                        </select>
                    </div>

                    <%-- Bộ lọc Trạng thái tài khoản (Active Filter: Active=1,
                        Locked=0) --%>
                    <div class="search-field select-field">
                        <label for="activeSelect">Trạng thái</label>
                        <select id="activeSelect" name="active"
                                class="form-select">
                            <option value="">-- Tất cả --</option>
                            <option value="1"
                                    <%=Integer.valueOf(1).equals(activeFilter)
                                                                            ? "selected" : "" %>>Active</option>
                            <option value="0"
                                    <%=Integer.valueOf(0).equals(activeFilter)
                                                                            ? "selected" : "" %>>Locked</option>
                        </select>
                    </div>

                    <div class="user-search-buttons">
                        <button type="submit" class="btn btn-primary"
                                id="searchBtn">
                            <i class="fa-solid fa-search"></i> Tìm
                        </button>
                        <a href="<%= ctx %><%= currentPath %>"
                           class="btn btn-outline" title="Xóa bộ lọc">
                            <i class="fa-solid fa-rotate-right"></i>
                        </a>
                    </div>
                </div>
            </div>
        </form>

        <%-- Khối hiển thị Thông báo Thành công hoặc Lỗi phản hồi từ Controller --%>
        <% if (successMsg !=null) { %>
        <div class="alert alert-success">
            <i class="fa-solid fa-circle-check"></i>
            <%= successMsg %>
        </div>
        <% } %>
        <% if (errorMsg !=null) { %>
        <div class="alert alert-danger">
            <i class="fa-solid fa-circle-exclamation"></i>
            <%= errorMsg %>
        </div>
        <% } %>

        <!-- ===== TOPBAR ===== -->
        <div class="books-topbar user-topbar">
            <div class="results-info user-results-info">
                <% if (!q.isEmpty() || roleFilter !=null &&
                    !roleFilter.isEmpty() || activeFilter !=null) {
                %>
                <i
                    class="fa-solid fa-filter fa-xs user-icon-primary"></i>
                Kết quả: <strong>
                    <%= totalRecords %>
                </strong> người dùng
                <% } else { %>
                <i
                    class="fa-solid fa-users fa-xs user-icon-primary-space"></i>
                Tổng cộng <strong>
                    <%= totalRecords %>
                </strong> người dùng
                <% } %>
            </div>

            <div class="user-topbar-actions">
                <!-- Sort -->
                <div class="sort-group">
                    <span class="sort-label"><i class="fa-solid fa-arrow-up-wide-short"></i> Sắp xếp:</span>
                    <%
                        String[][] sortOptions = {
                            {"username", "Tên đăng nhập"},
                            {"full_name", "Họ tên"},
                            {"role", "Vai trò"},
                            {"active", "Trạng thái"}
                        };
                        for (String[] so : sortOptions) {
                            String sf = so[0], sl = so[1];
                            boolean active = sf.equals(sortField);
                            String thisOrder = active ? nextOrder : "ASC";
                            String icon = active ? ("ASC".equals(sortOrder) ? " ▲" : " ▼") : "";
                            String encodedQ = java.net.URLEncoder.encode(q != null ? q : "", "UTF-8");
                    %>
                    <a href="<%= ctx %><%= currentPath %>?q=<%= encodedQ %>&role=<%= roleFilter != null ? roleFilter : "" %>&active=<%= activeFilter != null ? activeFilter : "" %>&sort=<%= sf %>&order=<%= thisOrder %>&page=1"
                       class="sort-btn <%= active ? "sort-btn-active" : "" %>">
                        <%= sl %><%= icon %>
                    </a>
                    <% } %>
                </div>

                <!-- Admin: Thêm người dùng button -->
                <% if (isAdmin) { %>
                <button type="button"
                        class="btn btn-primary btn-sm"
                        onclick="document.getElementById('createUserModal').style.display = 'flex'">
                    <i class="fa-solid fa-plus"></i> Thêm người
                    dùng
                </button>
                <% } %>
            </div>
        </div>

        <!-- ===== USERS TABLE CARD ===== -->
        <div class="admin-card user-card">
            <div class="admin-section-head user-section-head">
                <h2 class="user-section-title">Danh sách người dùng
                </h2>
                <span class="user-section-count">
                    <%= totalRecords %> bản ghi
                </span>
            </div>

            <div class="admin-table-wrap user-table-wrap">
                <table class="admin-table user-table">
                    <thead>
                        <tr class="user-table-head-row">
                            <th class="user-th-id">ID</th>
                            <th class="user-th">Tên đăng nhập</th>
                            <th class="user-th">Họ tên</th>
                            <th class="user-th">Email</th>
                            <th class="user-th">Vai trò</th>
                            <th class="user-th">Trạng thái</th>
                            <th class="user-th-action">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (users !=null && !users.isEmpty()) {
                            for (User u : users) { String
                            roleClass="ADMIN" .equals(u.getRole())
                            ? "badge-danger" : "LIBRARIAN"
                            .equals(u.getRole()) ? "badge-warning"
                            : "badge-primary" ; %>
                        <tr class="copy-row user-tr">
                            <td class="user-td-id">
                                <%= u.getId() %>
                            </td>
                            <td class="user-td-username">
                                <a href="<%= ctx %>/user/profile?id=<%= u.getId() %>"
                                   class="user-username-link">
                                    <%= u.getUsername() %>
                                </a>
                            </td>
                            <td class="user-td-fullname">
                                <%= u.getFullName() !=null ?
                                                                                            u.getFullName() : "—" %>
                            </td>
                            <td class="user-td-email">
                                <%= u.getEmail() !=null ?
                                                                                            u.getEmail() : "—" %>
                            </td>
                            <td class="user-td-role">
                                <span
                                    class="badge <%= roleClass %>">
                                    <%= u.getRole() %>
                                </span>
                            </td>
                            <td class="user-td-status">
                                <% if (u.getActive()==1) { %>
                                <span
                                    class="user-status-badge-active">
                                    <i
                                        class="fa-solid fa-circle user-status-dot"></i>
                                    Active
                                </span>
                                <% } else { %>
                                <span
                                    class="user-status-badge-locked">
                                    <i
                                        class="fa-solid fa-circle user-status-dot"></i>
                                    Locked
                                </span>
                                <% } %>
                            </td>
                            <td class="user-td-actions">
                                <div class="admin-row-actions user-row-actions">
                                    <a href="<%= ctx %>/user/profile?id=<%= u.getId() %>" class="user-action-view">
                                        <i class="fa-solid fa-eye"></i> Xem
                                    </a>
                                    <% if (logged != null && logged.isAdmin()) { 
                                        boolean isAct = u.getActive() == 1;
                                    %>
                                    <form method="post" action="<%= ctx %><%= currentPath %>" class="user-action-form">
                                        <input type="hidden" name="action" value="<%= isAct ? "lock" : "unlock" %>">
                                        <input type="hidden" name="id" value="<%= u.getId() %>">
                                        <button type="submit" class="<%= isAct ? "user-action-lock" : "user-action-unlock" %>">
                                            <i class="fa-solid <%= isAct ? "fa-lock" : "fa-lock-open" %>"></i> <%= isAct ? "Khóa" : "Mở khóa" %>
                                        </button>
                                    </form>
                                    <%-- Form Xóa mềm người dùng (POST action=delete, kiểm tra các giao dịch dở dang và chuyển is_deleted = 1) --%>
                                    <form method="post" action="<%= ctx %>/users" class="user-action-form"
                                          onsubmit="return confirm('Xác nhận xóa người dùng <%= u.getUsername().replace("'", "\\'") %> (tài khoản sẽ chuyển sang trạng thái Xóa mềm)?')">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="<%= u.getId() %>">
                                        <button type="submit" class="user-action-delete">
                                            <i class="fa-solid fa-trash"></i> Xóa
                                        </button>
                                    </form>
                                    <% } %>
                                </div>
                            </td>
                        </tr>
                        <% } } else { %>
                        <tr>
                            <td colspan="7"
                                class="user-empty-td">
                                <div class="empty-state">
                                    <div
                                        class="user-empty-icon">
                                        👤</div>
                                    <h3
                                        class="user-empty-title">
                                        Không tìm thấy người
                                        dùng</h3>
                                    <p
                                        class="user-empty-desc">
                                        Hãy thử tìm kiếm với
                                        từ khóa khác.</p>
                                </div>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>

            <!-- ===== PAGINATION ===== -->
            <% if (totalPages > 1) { 
                String encQ = java.net.URLEncoder.encode(q != null ? q : "", "UTF-8");
                String rf = roleFilter != null ? roleFilter : "";
                String af = activeFilter != null ? String.valueOf(activeFilter) : "";
                String baseUrl = ctx + currentPath + "?q=" + encQ + "&role=" + rf + "&active=" + af + "&sort=" + sortField + "&order=" + sortOrder + "&page=";
            %>
            <div class="user-pagination-wrap">
                <nav aria-label="Phân trang">
                    <ul class="pagination user-pagination-ul">
                        <!-- Prev -->
                        <li class="page-item <%= currentPageNum <= 1 ? "disabled" : "" %>">
                            <a class="page-link" href="<%= baseUrl %><%= currentPageNum - 1 %>">
                                <i class="fa-solid fa-chevron-left fa-xs"></i>
                            </a>
                        </li>

                        <%-- Pagging --%>

                        
                           <% if (totalPages <= 7) { 
                            // Nếu ít hơn 7 trang: in hết từ 1 -> totalPages
                                for (int pg = 1; pg <= totalPages; pg++) { %>
                                    <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                        <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                                    </li>
                                <% } 
                            } else { 
                            // Nếu nhiều hơn 7 trang: in 2 trang đầu (1, 2)
                                for (int pg = 1; pg <= 2; pg++) { %>
                                    <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                        <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                                    </li>
                                <% } 
                                if (currentPageNum <= 4) { 
                                    for (int pg = 3; pg <= 5; pg++) { %>
                                        <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                            <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                                        </li>
                                    <% } %>
                                    <%-- In dấu ba chấm (...) ở giữa --%>
                                    <li class="page-item disabled"><span class="page-link">…</span></li>
                                    
                            <%-- In 2 trang cuối (totalPages - 1, totalPages) --%>
                                <% } else if (currentPageNum >= totalPages - 3) { %>
                                    <li class="page-item disabled"><span class="page-link">…</span></li>
                                    <% for (int pg = totalPages - 4; pg <= totalPages - 2; pg++) { %>
                                        <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                            <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                                        </li>
                                    <% } 
                                } else { %>
                                
                                
                                
                                    <li class="page-item disabled"><span class="page-link">…</span></li>
                                    
                                    <% for (int pg = currentPageNum - 1; pg <= currentPageNum + 1; pg++) { %>
                                        <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                            <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                                        </li>
                                    <% } %>
                                    <li class="page-item disabled"><span class="page-link">…</span></li>
                                <% } 
                                for (int pg = totalPages - 1; pg <= totalPages; pg++) { %>
                                    <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                        <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                                    </li> 
                        
                        
                        
                                <% } 
                            } %>    
                        
                        
                        
                    <%--     <% for (int pg = 1; pg <= Math.min(2, totalPages); pg++) { %>
                        <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                            <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                        </li>
                        <% } %>
                        <% if (totalPages > 2) { %>
                        <li class="page-item disabled"><span class="page-link">…</span></li>
                            <% } %>   --%>

                    
                    
                    
                        <%--  for :int pg = currentPageNum, Math.min(2, totalPages) thành Math.min(currentPageNum + 1, totalPages)
                              if: currentPageNum + 1 < totalPages.
                        
                             --%>

                        <!-- Next -->
                        <li class="page-item <%= currentPageNum >= totalPages ? "disabled" : "" %>">
                            <a class="page-link" href="<%= baseUrl %><%= currentPageNum + 1 %>">
                                <i class="fa-solid fa-chevron-right fa-xs"></i>
                            </a>
                        </li>
                    </ul>
                </nav>
            </div>
            <% } %>
        </div>
    </div>
</main>

<!-- ===== CREATE USER MODAL (Admin only) ===== -->
<% if (isAdmin) { %>
<div id="createUserModal" class="user-modal-overlay">
    <div class="user-modal-content">
        <div class="user-modal-top-bar"></div>
        <div class="user-modal-header">
            <h3 class="user-modal-title">
                <i class="fa-solid fa-user-plus user-modal-title-icon"></i> Tạo người
                dùng mới
            </h3>
            <button
                onclick="document.getElementById('createUserModal').style.display = 'none'"
                class="user-modal-close-btn">×</button>
        </div>
        <form method="post" action="<%= ctx %><%= currentPath %>">
            <input type="hidden" name="action" value="create">
            <div class="user-modal-form-grid">
                <div>
                    <label class="user-modal-label">Tên đăng nhập *</label>
                    <input id="cu_username" name="username" required
                           placeholder="Nhập username (chữ cái, số, dấu _)..."
                           maxlength="50" autocomplete="off" class="form-control">
                </div>
                <div>
                    <label class="user-modal-label">Mật khẩu</label>
                    <input id="cu_password" name="password" type="password"
                           placeholder="Để trống = mặc định 'password'" minlength="5"
                           maxlength="100" class="form-control">
                </div>
                <div>
                    <label class="user-modal-label">Họ và tên</label>
                    <input id="cu_fullName" name="fullName" placeholder="Nhập họ tên..."
                           maxlength="100" class="form-control">
                </div>
                <div>
                    <label class="user-modal-label">Email</label>
                    <input id="cu_email" name="email" type="email"
                           placeholder="example@email.com" maxlength="100"
                           class="form-control">
                </div>
                <div>
                    <label class="user-modal-label">Số điện thoại</label>
                    <input id="cu_phone" name="phone" placeholder="0xxxxxxxxx"
                           maxlength="15" class="form-control">
                </div>
                <div>
                    <label class="user-modal-label">Mã sinh viên</label>
                    <input id="cu_studentId" name="studentId"
                           placeholder="Ví dụ: HA123456" maxlength="20"
                           class="form-control">
                </div>
                <div>
                    <label class="user-modal-label-dark">Vai trò</label>
                    <select name="role" class="user-modal-select">
                        <option value="READER">READER</option>
                        <option value="LIBRARIAN">LIBRARIAN</option>
                        <option value="ADMIN">ADMIN</option>
                    </select>
                </div>
            </div>
            <div class="user-modal-actions">
                <button type="button"
                        onclick="document.getElementById('createUserModal').style.display = 'none'"
                        class="user-modal-btn-cancel">
                    Hủy
                </button>
                <button type="submit" class="user-modal-btn-submit">
                    <i class="fa-solid fa-plus"></i> Tạo người dùng
                </button>
            </div>
        </form>
    </div>
</div>
<script>
    document.getElementById('createUserModal').addEventListener('click', function (e) {
        if (e.target === this)
            this.style.display = 'none';
    });
</script>
<% } %>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>






<%-- 1. In 2 trang đầu (Trang 1 và Trang 2) 
<% for (int pg = 1; pg <= Math.min(2, totalPages); pg++) { %>
    <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
        <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
    </li>
<% } %>

<%-- 2. In dấu ba chấm (...) ở giữa 
<% if (totalPages > 3) { %>
    <li class="page-item disabled"><span class="page-link">…</span></li>
<% } %>

<%-- 3. In trang cuối cùng (Trang 6) 
<% if (totalPages > 2) { %>
    <li class="page-item <%= currentPageNum == totalPages ? "active" : "" %>">
        <a class="page-link" href="<%= baseUrl %><%= totalPages %>"><%= totalPages %></a>
    </li>
<% } %> --%>




<%-- 1. In 2 trang đầu (Trang 1 và 2) 
<% for (int pg = 1; pg <= Math.min(2, totalPages); pg++) { %>
    <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
        <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
    </li>
<% } %>

<%-- 2. Dấu 3 chấm (Đã sửa > 3 thành > 4) 
<% if (totalPages > 4) { %>
    <li class="page-item disabled"><span class="page-link">…</span></li>
<% } %>

<%-- 3. In 2 trang cuối (Đã sửa từ if sang for) 
<% for (int pg = Math.max(3, totalPages - 1); pg <= totalPages; pg++) { %>
    <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
        <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
    </li>
<% } %> --%>
