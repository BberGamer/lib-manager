<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Event" %>
<%@ page import="model.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="${not empty sessionScope.loggedUser and sessionScope.loggedUser.adminOrLibrarian}" scope="request" />
<c:set var="activePage" value="events" scope="request" />
<c:set var="pageTitle" value="Quản lý sự kiện – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/user-list.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/event.css">

<%
    List<Event> events = (List<Event>) request.getAttribute("events");
    Integer totalRecords = (Integer) request.getAttribute("totalRecords");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    Integer currentPageNum = (Integer) request.getAttribute("currentPageNum");
    String q = (String) request.getAttribute("q");
    String statusFilter = (String) request.getAttribute("statusFilter");
    String sortField = (String) request.getAttribute("sortField");
    String sortOrder = (String) request.getAttribute("sortOrder");

    // Đọc thông báo flash từ session
    String sessionSuccess = (String) session.getAttribute("successMsg");
    if (sessionSuccess != null) {
        request.setAttribute("successMsg", sessionSuccess);
        session.removeAttribute("successMsg");
    }
    String sessionError = (String) session.getAttribute("errorMsg");
    if (sessionError != null) {
        request.setAttribute("errorMsg", sessionError);
        session.removeAttribute("errorMsg");
    }

    String successMsg = (String) request.getAttribute("successMsg");
    String errorMsg = (String) request.getAttribute("errorMsg");
    if (successMsg == null) successMsg = (String) request.getAttribute("success");
    if (errorMsg == null) errorMsg = (String) request.getAttribute("error");

    User logged = (User) session.getAttribute("loggedUser");
    boolean canManage = (logged != null && logged.isAdminOrLibrarian());
    String ctx = request.getContextPath();
    String currentPath = (String) request.getAttribute("currentPath");
    if (currentPath == null || currentPath.isEmpty()) {
        currentPath = "/events";
    }

    if (totalRecords == null) totalRecords = (events != null ? events.size() : 0);
    if (totalPages == null) totalPages = 1;
    if (currentPageNum == null) currentPageNum = 1;
    if (q == null) q = "";
    if (statusFilter == null) statusFilter = "";
    if (sortField == null) sortField = "start_time";
    if (sortOrder == null) sortOrder = "ASC";

    String nextOrder = "ASC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
%>

<main class="page-wrapper">

    <!-- ===== PAGE HEADER BANNER ===== -->
    <div class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow user-hero-eyebrow">
                        <i class="fa-solid fa-calendar-days"></i> Sự kiện
                    </div>
                    <h1 class="books-page-title">Quản lý Sự kiện</h1>
                    <p class="books-page-subtitle">Xem, tìm kiếm và quản lý các sự kiện, trưng bày sách tại Thư viện FPT</p>
                </div>
                <div class="books-page-stats">
                    <div class="bps-item">
                        <span class="bps-num"><%= totalRecords %></span>
                        <span class="bps-lbl">Sự kiện</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="container user-list-container">

        <%-- Form Tìm kiếm, Lọc theo Trạng thái và Phân trang (gửi GET tới EventServlet) --%>
        <form id="searchForm" action="<%= ctx %><%= currentPath %>" method="get">
            <input type="hidden" name="sort" value="<%= sortField %>">
            <input type="hidden" name="order" value="<%= sortOrder %>">
            <input type="hidden" name="page" value="1">

            <div class="search-bar-wrapper">
                <div class="search-bar-inner">

                    <%-- Ô nhập từ khóa tìm kiếm sự kiện --%>
                    <div class="search-field user-search-keyword">
                        <label for="keywordInput">Tìm kiếm sự kiện</label>
                        <div class="search-input-wrap">
                            <i class="fa-solid fa-magnifying-glass search-icon"></i>
                            <input type="text" id="keywordInput" name="q" class="form-control"
                                   placeholder="Nhập tiêu đề sự kiện..."
                                   value="<%= q %>" maxlength="200" autocomplete="off">
                        </div>
                    </div>

                    <%-- Bộ lọc Trạng thái sự kiện --%>
                    <div class="search-field select-field">
                        <label for="statusSelect">Trạng thái</label>
                        <select id="statusSelect" name="status" class="form-select">
                            <option value="">-- Tất cả trạng thái --</option>
                            <option value="UPCOMING" <%= "UPCOMING".equals(statusFilter) ? "selected" : "" %>>Sắp diễn ra</option>
                            <option value="ONGOING" <%= "ONGOING".equals(statusFilter) ? "selected" : "" %>>Đang diễn ra</option>
                            <option value="ENDED" <%= "ENDED".equals(statusFilter) ? "selected" : "" %>>Đã kết thúc</option>
                            <option value="CANCELLED" <%= "CANCELLED".equals(statusFilter) ? "selected" : "" %>>Đã hủy</option>
                        </select>
                    </div>

                    <div class="user-search-buttons">
                        <button type="submit" class="btn btn-primary" id="searchBtn">
                            <i class="fa-solid fa-magnifying-glass"></i> Tìm
                        </button>
                        <a href="<%= ctx %><%= currentPath %>" class="btn btn-outline" title="Xóa bộ lọc">
                            <i class="fa-solid fa-rotate-right"></i>
                        </a>
                    </div>

                </div>
            </div>
        </form>

        <%-- Thông báo thành công hoặc lỗi --%>
        <% if (successMsg != null) { %>
            <div class="alert alert-success" style="margin-top: 15px;">
                <i class="fa-solid fa-circle-check"></i>
                <%= successMsg %>
            </div>
        <% } %>
        <% if (errorMsg != null) { %>
            <div class="alert alert-danger" style="margin-top: 15px;">
                <i class="fa-solid fa-circle-exclamation"></i>
                <%= errorMsg %>
            </div>
        <% } %>

        <!-- ===== TOPBAR ===== -->
        <div class="books-topbar user-topbar">
            <div class="results-info user-results-info">
                <% if (!q.isEmpty() || (statusFilter != null && !statusFilter.isEmpty())) { %>
                    <i class="fa-solid fa-filter fa-xs user-icon-primary"></i>
                    Kết quả lọc: <strong><%= totalRecords %></strong> sự kiện
                <% } else { %>
                    <i class="fa-solid fa-calendar-days fa-xs user-icon-primary-space"></i>
                    Tổng cộng <strong><%= totalRecords %></strong> sự kiện
                <% } %>
            </div>

            <div class="user-topbar-actions">
                <!-- Nút Sắp xếp Pills giống hệt user_list.jsp -->
                <div class="sort-group">
                    <span class="sort-label"><i class="fa-solid fa-arrow-up-wide-short"></i> Sắp xếp:</span>
                    <%
                        String[][] sortOptions = {
                            {"start_time", "Thời gian bắt đầu"},
                            {"title", "Tiêu đề sự kiện"},
                            {"status", "Trạng thái"}
                        };
                        for (String[] so : sortOptions) {
                            String sf = so[0], sl = so[1];
                            boolean active = sf.equals(sortField);
                            String thisOrder = active ? nextOrder : "ASC";
                            String icon = active ? ("ASC".equalsIgnoreCase(sortOrder) ? " ▲" : " ▼") : "";
                            String encodedQ = java.net.URLEncoder.encode(q != null ? q : "", "UTF-8");
                            String encodedStatus = java.net.URLEncoder.encode(statusFilter != null ? statusFilter : "", "UTF-8");
                    %>
                        <a href="<%= ctx %><%= currentPath %>?q=<%= encodedQ %>&status=<%= encodedStatus %>&sort=<%= sf %>&order=<%= thisOrder %>&page=1"
                           class="sort-btn <%= active ? "sort-btn-active" : "" %>">
                            <%= sl %><%= icon %>
                        </a>
                    <% } %>
                </div>

                <% if (canManage) { %>
                    <button type="button" class="btn btn-primary btn-sm" data-action="open-add-modal">
                        <i class="fa-solid fa-plus"></i> Thêm sự kiện mới
                    </button>
                <% } %>
            </div>
        </div>

        <!-- ===== EVENTS TABLE CARD ===== -->
        <div class="admin-card user-card">
            <div class="admin-section-head user-section-head">
                <h2 class="user-section-title">Danh sách sự kiện</h2>
                <span class="user-section-count">Trang <%= currentPageNum %> / <%= totalPages %></span>
            </div>

            <div class="admin-table-wrap user-table-wrap">
                <table class="admin-table user-table">
                    <thead>
                        <tr class="user-table-head-row">
                            <th class="user-th-id">STT</th>
                            <th class="user-th">Tiêu đề sự kiện</th>
                            <th class="user-th">Thời gian bắt đầu</th>
                            <th class="user-th">Thời gian kết thúc</th>
                            <th class="user-th">Trạng thái</th>
                            <% if (canManage) { %>
                                <th class="user-th">Người tạo</th>
                            <% } %>
                            <th class="user-th-action">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (events != null && !events.isEmpty()) {
                            int idx = (currentPageNum - 1) * 10 + 1;
                            for (Event item : events) {
                                String displayStatus = item.getDisplayStatus();
                                String startTimeStr = item.getStartTime() != null ? item.getStartTime().toString().replace("T", " ") : "—";
                                String endTimeStr = item.getEndTime() != null ? item.getEndTime().toString().replace("T", " ") : "—";
                        %>
                            <tr class="copy-row user-tr">
                                <td class="user-td-id"><%= idx++ %></td>
                                <td class="user-td-username">
                                    <span class="user-username-link" style="color: #0f172a;"><%= item.getTitle() %></span>
                                </td>
                                <td class="user-td-fullname"><%= startTimeStr %></td>
                                <td class="user-td-fullname"><%= endTimeStr %></td>
                                <td class="user-td-status">
                                    <% if ("UPCOMING".equals(displayStatus)) { %>
                                        <span class="user-status-badge-active" style="background:#fff7ed; color:#c2410c;">Sắp diễn ra</span>
                                    <% } else if ("ONGOING".equals(displayStatus)) { %>
                                        <span class="user-status-badge-active" style="background:#d1fae5; color:#065f46;">Đang diễn ra</span>
                                    <% } else if ("ENDED".equals(displayStatus)) { %>
                                        <span class="user-status-badge-locked" style="background:#f3f4f6; color:#4b5563;">Đã kết thúc</span>
                                    <% } else { %>
                                        <span class="user-status-badge-locked" style="background:#fee2e2; color:#991b1b;">Đã hủy</span>
                                    <% } %>
                                </td>
                                <% if (canManage) { %>
                                    <td class="user-td-email"><%= item.getCreatedBy() != null ? item.getCreatedBy() : "—" %></td>
                                <% } %>
                                <td class="user-td-actions">
                                    <div class="admin-row-actions user-row-actions">
                                        <!-- Nút Xem chi tiết -->
                                        <button type="button" class="user-action-view"
                                                data-action="open-view-modal"
                                                data-id="<%= item.getId() %>"
                                                data-title="<%= item.getTitle() %>"
                                                data-description="<%= item.getDescription() != null ? item.getDescription() : "" %>"
                                                data-start-time="<%= startTimeStr %>"
                                                data-end-time="<%= endTimeStr %>"
                                                data-display-status="<%= displayStatus %>"
                                                data-created-by="<%= item.getCreatedBy() != null ? item.getCreatedBy() : "" %>"
                                                data-updated-by="<%= item.getUpdatedBy() != null ? item.getUpdatedBy() : "" %>"
                                                title="Xem chi tiết">
                                            <i class="fa-solid fa-eye"></i> Xem
                                        </button>

                                        <% if (canManage) { %>
                                            <!-- Nút Sửa -->
                                            <button type="button" class="user-action-view" style="color:#c2410c; border-color:#fcd34d; background:#fff7ed;"
                                                    data-action="open-edit-modal"
                                                    data-id="<%= item.getId() %>"
                                                    data-title="<%= item.getTitle() %>"
                                                    data-description="<%= item.getDescription() != null ? item.getDescription() : "" %>"
                                                    data-start-time-raw="<%= item.getStartTime() != null ? item.getStartTime().toString() : "" %>"
                                                    data-end-time-raw="<%= item.getEndTime() != null ? item.getEndTime().toString() : "" %>"
                                                    data-status="<%= item.getStatus() %>"
                                                    title="Chỉnh sửa sự kiện">
                                                <i class="fa-solid fa-pen-to-square"></i> Sửa
                                            </button>

                                            <!-- Nút Xóa -->
                                            <button type="button" class="user-action-delete"
                                                    data-action="open-delete-modal"
                                                    data-id="<%= item.getId() %>"
                                                    data-title="<%= item.getTitle() %>"
                                                    title="Xóa sự kiện">
                                                <i class="fa-solid fa-trash-can"></i> Xóa
                                            </button>
                                        <% } %>
                                    </div>
                                </td>
                            </tr>
                        <% } } else { %>
                            <tr>
                                <td colspan="<%= canManage ? 7 : 6 %>" class="user-empty-td">
                                    <div class="user-empty-icon">
                                        <i class="fa-solid fa-calendar-xmark" style="color: #cbd5e1;"></i>
                                    </div>
                                    <h3 class="user-empty-title">Không tìm thấy sự kiện nào</h3>
                                    <p class="user-empty-desc">Thử thay đổi từ khóa hoặc bộ lọc tìm kiếm của bạn.</p>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>

            <!-- ===== PAGINATION  ===== -->
            <% if (totalPages > 1) { 
                String encQ = java.net.URLEncoder.encode(q != null ? q : "", "UTF-8");
                String sf = statusFilter != null ? statusFilter : "";
                String baseUrl = ctx + currentPath + "?q=" + encQ + "&status=" + sf + "&sort=" + sortField + "&order=" + sortOrder + "&page=";
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
                                for (int pg = 1; pg <= totalPages; pg++) { %>
                                    <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                        <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                                    </li>
                                <% } 
                            } else { 
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
                                    <li class="page-item disabled"><span class="page-link">…</span></li>
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
                            <% } %>  --%> 

                    
                    
                    
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

<!-- ========================================================================== -->
<!-- MODAL 1: XEM CHI TIẾT SỰ KIỆN                                               -->
<!-- ========================================================================== -->
<div id="viewEventModal" class="modal-backdrop">
    <div class="modal-dialog">
        <div class="modal-header">
            <h3><i class="fa-solid fa-circle-info"></i> Chi tiết sự kiện</h3>
            <button type="button" class="modal-close-btn" data-action="close-modal">&times;</button>
        </div>
        <div class="modal-body">
            <div class="detail-group">
                <div class="detail-label">Tiêu đề sự kiện</div>
                <div id="viewTitle" class="detail-value" style="font-weight: 700; font-size: 1.1rem;"></div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Trạng thái</div>
                <div class="detail-value">
                    <span id="viewDisplayStatus"></span>
                </div>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                <div class="detail-group">
                    <div class="detail-label">Bắt đầu</div>
                    <div id="viewStartTime" class="detail-value"></div>
                </div>
                <div class="detail-group">
                    <div class="detail-label">Kết thúc</div>
                    <div id="viewEndTime" class="detail-value"></div>
                </div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Mô tả sự kiện</div>
                <div id="viewDescription" class="detail-value" style="white-space: pre-wrap;"></div>
            </div>
            <% if (canManage) { %>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 16px; border-top: 1px dashed #e2e8f0; padding-top: 12px;">
                    <div class="detail-group" style="margin:0;">
                        <div class="detail-label">Tài khoản tạo</div>
                        <div id="viewCreatedBy" class="detail-value"></div>
                    </div>
                    <div class="detail-group" style="margin:0;">
                        <div class="detail-label">Cập nhật gần nhất</div>
                        <div id="viewUpdatedBy" class="detail-value"></div>
                    </div>
                </div>
            <% } %>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-outline" data-action="close-modal">Đóng</button>
        </div>
    </div>
</div>

<% if (canManage) { %>
    <!-- ========================================================================== -->
    <!-- MODAL 2: THÊM SỰ KIỆN MỚI                                                  -->
    <!-- ========================================================================== -->
    <div id="addEventModal" class="modal-backdrop">
        <div class="modal-dialog">
            <form action="<%= ctx %><%= currentPath %>" method="post">
                <input type="hidden" name="action" value="create">
                <input type="hidden" name="status" value="ACTIVE">
                <div class="modal-header">
                    <h3><i class="fa-solid fa-calendar-plus"></i> Thêm sự kiện mới</h3>
                    <button type="button" class="modal-close-btn" data-action="close-modal">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="event-form-group" style="margin-bottom: 16px;">
                        <label for="addTitle">Tiêu đề sự kiện</label>
                        <input type="text" id="addTitle" name="title" class="form-control"
                               placeholder="Nhập tiêu đề sự kiện (3 - 200 ký tự)">
                    </div>
                    <div class="event-form-group" style="margin-bottom: 16px;">
                        <label for="addDescription">Mô tả sự kiện</label>
                        <textarea id="addDescription" name="description" class="form-control" rows="4"
                                  placeholder="Nhập nội dung mô tả sự kiện (tối đa 1000 ký tự)"></textarea>
                    </div>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
                        <div class="event-form-group">
                            <label for="addStartTime">Thời gian bắt đầu</label>
                            <input type="datetime-local" id="addStartTime" name="startTime" class="form-control">
                        </div>
                        <div class="event-form-group">
                            <label for="addStartTime">Thời gian kết thúc</label>
                            <input type="datetime-local" id="addEndTime" name="endTime" class="form-control">
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" data-action="close-modal">Hủy</button>
                    <button type="submit" class="btn btn-primary">
                        <i class="fa-solid fa-floppy-disk"></i> Lưu sự kiện
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- ========================================================================== -->
    <!-- MODAL 3: CHỈNH SỬA SỰ KIỆN                                                 -->
    <!-- ========================================================================== -->
    <div id="editEventModal" class="modal-backdrop">
        <div class="modal-dialog">
            <form action="<%= ctx %><%= currentPath %>" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" id="editId" name="id" value="">
                <div class="modal-header">
                    <h3><i class="fa-solid fa-pen-to-square"></i> Cập nhật sự kiện</h3>
                    <button type="button" class="modal-close-btn" data-action="close-modal">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="event-form-group" style="margin-bottom: 16px;">
                        <label for="editTitle">Tiêu đề sự kiện</label>
                        <input type="text" id="editTitle" name="title" class="form-control"
                               placeholder="Nhập tiêu đề sự kiện (3 - 200 ký tự)">
                    </div>
                    <div class="event-form-group" style="margin-bottom: 16px;">
                        <label for="editDescription">Mô tả sự kiện</label>
                        <textarea id="editDescription" name="description" class="form-control" rows="4"
                                  placeholder="Nhập nội dung mô tả sự kiện (tối đa 1000 ký tự)"></textarea>
                    </div>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
                        <div class="event-form-group">
                            <label for="editStartTime">Thời gian bắt đầu</label>
                            <input type="datetime-local" id="editStartTime" name="startTime" class="form-control">
                        </div>
                        <div class="event-form-group">
                            <label for="editEndTime">Thời gian kết thúc</label>
                            <input type="datetime-local" id="editEndTime" name="endTime" class="form-control">
                        </div>
                    </div>
                    <div class="event-form-group">
                        <label for="editStatus">Trạng thái sự kiện</label>
                        <select id="editStatus" name="status" class="form-select">
                            <option value="ACTIVE">Hoạt động (ACTIVE)</option>
                            <option value="CANCELLED">Hủy (CANCELLED)</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" data-action="close-modal">Hủy</button>
                    <button type="submit" class="btn btn-primary">
                        <i class="fa-solid fa-check"></i> Cập nhật
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- ========================================================================== -->
    <!-- MODAL 4: XÓA SỰ KIỆN                                                       -->
    <!-- ========================================================================== -->
    <div id="deleteEventModal" class="modal-backdrop">
        <div class="modal-dialog" style="max-width: 450px;">
            <form action="<%= ctx %><%= currentPath %>" method="post">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" id="deleteId" name="id" value="">
                <div class="modal-header">
                    <h3 style="color: #ef4444;"><i class="fa-solid fa-triangle-exclamation"></i> Xác nhận xóa</h3>
                    <button type="button" class="modal-close-btn" data-action="close-modal">&times;</button>
                </div>
                <div class="modal-body">
                    <p style="margin: 0; color: #334155; font-size: 0.95rem;">
                        Bạn có chắc chắn muốn xóa sự kiện <strong id="deleteEventTitle"></strong> không?
                    </p>
                    <p style="margin-top: 8px; color: #64748b; font-size: 0.85rem;">
                        (Hệ thống sẽ thực hiện xóa mềm sự kiện này).
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" data-action="close-modal">Hủy</button>
                    <button type="submit" class="btn btn-danger" style="background:#ef4444; color:#fff; border:none;">
                        <i class="fa-solid fa-trash-can"></i> Đồng ý xóa
                    </button>
                </div>
            </form>
        </div>
    </div>
<% } %>

<script src="${pageContext.request.contextPath}/assets/js/event.js" defer></script>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
