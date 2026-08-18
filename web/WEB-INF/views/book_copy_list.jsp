<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Book, model.BookCopy, model.User, java.util.List" %>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    Book book = (Book) request.getAttribute("book");
    List<BookCopy> copies = (List<BookCopy>) request.getAttribute("copies");
    Integer totalRecords = (Integer) request.getAttribute("totalRecords");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    Integer currentPageNum = (Integer) request.getAttribute("currentPageNum");
    String keyword = (String) request.getAttribute("keyword");
    String selectedArea = (String) request.getAttribute("selectedArea");
    List<String> distinctAreas = (List<String>) request.getAttribute("distinctAreas");

    String ctx = request.getContextPath();
    if (totalRecords == null) totalRecords = 0;
    if (totalPages == null) totalPages = 1;
    if (currentPageNum == null) currentPageNum = 1;
    if (keyword == null) keyword = "";
    if (selectedArea == null) selectedArea = "";

    // Retrieve success and error alerts
    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String rolePath = (String) request.getAttribute("rolePath");
    if (rolePath == null) rolePath = "";
%>

<main class="page-wrapper">

<!-- ===== PAGE HEADER ===== -->
<div class="books-page-header">
    <div class="container">
        <div class="books-page-header-inner">
            <div>
                <div class="hero-eyebrow" style="margin-bottom:10px;">
                    <i class="fa-solid fa-boxes-stacked"></i> Quản lý bản sao
                </div>
                <h1 class="books-page-title"><%= book != null ? book.getTitle() : "Đầu sách không tồn tại" %></h1>
                <p class="books-page-subtitle">
                    <a href="<%= ctx %><%= rolePath %>/book/detail?id=<%= book != null ? book.getId() : "" %>" style="color:var(--primary);">
                        <i class="fa-solid fa-arrow-left"></i> Quay lại chi tiết sách
                    </a>
                </p>
            </div>
            <% if (book != null) { %>
            <div class="books-page-stats" style="display:flex; align-items:center; gap:16px;">
                <div class="bps-item">
                    <span class="bps-num"><%= totalRecords %></span>
                    <span class="bps-lbl">Tổng bản sao</span>
                </div>
                <a href="<%= ctx %><%= rolePath %>/book/copy/add?bookId=<%= book.getId() %>" class="btn btn-primary">
                    <i class="fa-solid fa-plus"></i> Thêm bản sao
                </a>
            </div>
            <% } %>
        </div>
    </div>
</div>

<div class="container" style="padding-top:28px;">

    <!-- Alerts Notification -->
    <% if ("added".equals(success)) { %>
        <div class="alert alert-success"><i class="fa-solid fa-circle-check"></i> Thêm bản sao mới thành công!</div>
    <% } else if ("updated".equals(success)) { %>
        <div class="alert alert-success"><i class="fa-solid fa-circle-check"></i> Cập nhật bản sao thành công!</div>
    <% } else if ("deleted".equals(success)) { %>
        <div class="alert alert-success"><i class="fa-solid fa-circle-check"></i> Xóa bản sao thành công!</div>
    <% } %>

    <% if ("cannot_delete".equals(error)) { %>
        <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark"></i> Không thể xóa bản sao đang được mượn hoặc đặt trước.</div>
    <% } else if ("delete_failed".equals(error)) { %>
        <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark"></i> Xóa bản sao thất bại do lỗi hệ thống.</div>
    <% } else if ("barcode_exists".equals(error)) { %>
        <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark"></i> Mã barcode đã tồn tại trong hệ thống.</div>
    <% } %>

    <!-- ==================== SEARCH & FILTER BAR ==================== -->
    <form id="searchForm" action="<%= ctx %><%= rolePath %>/book/copies" method="get">
        <input type="hidden" name="bookId" value="<%= book != null ? book.getId() : "" %>">
        <input type="hidden" name="page" value="1">

        <div class="search-bar-wrapper">
            <div class="search-bar-inner">
                <!-- Barcode Keyword -->
                <div class="search-field" style="flex:2;">
                    <label for="keywordInput">Mã bản sao (Barcode)</label>
                    <div class="search-input-wrap">
                        <i class="fa-solid fa-barcode search-icon"></i>
                        <input type="text" id="keywordInput" name="keyword"
                               class="form-control"
                               placeholder="Nhập mã barcode bản sao..."
                               value="<%= keyword %>"
                               maxlength="50"
                               autocomplete="off">
                    </div>
                </div>



                <!-- Location Area Filter -->
                <div class="search-field select-field">
                    <label for="areaSelect">Khu vực / Tầng</label>
                    <select id="areaSelect" name="area" class="form-select">
                        <option value="">-- Tất cả vị trí --</option>
                        <% if (distinctAreas != null) {
                            for (String a : distinctAreas) {
                                if (a != null) {
                                    String sel = a.equals(selectedArea) ? "selected" : "";
                        %>
                            <option value="<%= a %>" <%= sel %>><%= a %></option>
                        <%      }
                            }
                        } %>
                    </select>
                </div>

                <!-- Buttons -->
                <div style="display:flex; gap:8px; align-items:flex-end;">
                    <button type="submit" class="btn btn-primary">
                        <i class="fa-solid fa-search"></i> Tìm
                    </button>
                </div>
            </div>
        </div>
    </form>

    <!-- ==================== COPIES TABLE ==================== -->
    <% if (copies == null || copies.isEmpty()) { %>
        <div class="empty-state" style="padding:80px 24px;">
            <div class="empty-icon"><i class="fa-solid fa-magnifying-glass"></i></div>
            <h3>Không tìm thấy bản sao nào</h3>
            <p>Thử thay đổi từ khóa barcode hoặc bộ lọc trạng thái/vị trí.</p>
        </div>
    <% } else { %>
        <div class="data-table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 5%; text-align: center;">#</th>
                        <th style="width: 20%; text-align: center;">Mã bản sao (Barcode)</th>
                        <th style="width: 15%; text-align: center;">Tình trạng (Condition)</th>
                        <th style="width: 20%; text-align: center;">Khu vực / Tầng</th>
                        <th style="width: 15%; text-align: center;">Kệ / Ngăn</th>
                        <th style="width: 15%; text-align: center;">Ghi chú</th>
                        <th style="width: 10%; text-align: center;">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <% int rowNum = (currentPageNum - 1) * 20 + 1;
                       for (BookCopy bc : copies) { 
                           // Mapping Condition display
                           String conditionLabel = "Tốt";
                           String conditionClass = "badge-success";
                           if ("WORN".equals(bc.getBookCondition())) {
                               conditionLabel = "Hao mòn";
                               conditionClass = "badge-warning";
                           } else if ("DAMAGED".equals(bc.getBookCondition())) {
                               conditionLabel = "Hỏng";
                               conditionClass = "badge-danger";
                           } else if ("LOST".equals(bc.getBookCondition())) {
                               conditionLabel = "Mất";
                               conditionClass = "badge-danger";
                           }

                    %>
                        <tr>
                            <td style="color:var(--text-muted); font-size:0.82rem; text-align: center;"><%= rowNum++ %></td>
                            <td style="font-weight: 600; font-family: monospace; letter-spacing: 0.5px; text-align: center;"><%= bc.getBarcode() %></td>
                            <td style="text-align: center;">
                                <span class="badge <%= conditionClass %>">
                                    <%= conditionLabel %>
                                </span>
                            </td>
                            <td style="text-align: center;"><%= bc.getArea() != null ? bc.getArea() : "—" %></td>
                            <td style="text-align: center;">
                                <% if (bc.getShelf() != null || bc.getSlot() != null) { %>
                                    <%= bc.getShelf() != null ? bc.getShelf() : "—" %> / <%= bc.getSlot() != null ? bc.getSlot() : "—" %>
                                <% } else { %>
                                    —
                                <% } %>
                            </td>
                            <td style="font-size:0.85rem; color:var(--text-secondary); max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; text-align: center;" title="<%= bc.getNote() != null ? bc.getNote() : "" %>">
                                <%= bc.getNote() != null ? bc.getNote() : "—" %>
                            </td>
                            <td style="text-align:center;">
                                <div style="display:flex; gap:6px; justify-content:center;">
                                    <a href="<%= ctx %><%= rolePath %>/book/copy/edit?id=<%= bc.getId() %>" class="btn btn-outline btn-sm" style="padding: 4px 8px;" title="Chỉnh sửa bản sao">
                                        <i class="fa-solid fa-pen-to-square"></i>
                                    </a>
                                    <% 
                                        boolean isDeletable = !bc.isBorrowedOrReserved();
                                        HttpSession s = request.getSession(false);
                                        User u = (s != null) ? (User) s.getAttribute("loggedUser") : null;
                                        if (u != null && u.isAdmin
                                        ()) {
                                    %>
                                        <button type="button" class="btn btn-danger btn-sm" style="padding: 4px 8px;" 
                                                onclick="confirmDeleteCopy('<%= bc.getId() %>', '<%= bc.getBarcode() %>')"
                                                <%= isDeletable ? "" : "disabled title=\"Không thể xóa bản sao đang được mượn hoặc đặt trước\" style=\"opacity:0.5; cursor:not-allowed;\"" %>>
                                            <i class="fa-solid fa-trash"></i>
                                        </button>
                                    <% } %>
                                </div>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

        <!-- ==================== PAGINATION ==================== -->
        <% if (totalPages > 1) {
            String pgBaseUrl = ctx + rolePath + "/book/copies?bookId=" + book.getId()
                + "&keyword=" + java.net.URLEncoder.encode(keyword,"UTF-8")
                + "&area=" + java.net.URLEncoder.encode(selectedArea,"UTF-8")
                + "&page=";
        %>
            <nav aria-label="Phân trang">
                <ul class="pagination">
                    <!-- Prev -->
                    <li class="page-item <%= currentPageNum <= 1 ? "disabled" : "" %>">
                        <a class="page-link" href="<%= pgBaseUrl %><%= currentPageNum - 1 %>">
                            <i class="fa-solid fa-chevron-left fa-xs"></i>
                        </a>
                    </li>

                    <% 
                       if (totalPages <= 7) {
                           for (int pg = 1; pg <= totalPages; pg++) { %>
                               <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                   <a class="page-link" href="<%= pgBaseUrl %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                       } else {
                           // Show first 2 pages
                           for (int pg = 1; pg <= 2; pg++) { %>
                               <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                   <a class="page-link" href="<%= pgBaseUrl %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }

                           if (currentPageNum <= 4) {
                               // Current page is near the start
                               for (int pg = 3; pg <= 5; pg++) { %>
                                   <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                       <a class="page-link" href="<%= pgBaseUrl %><%= pg %>"><%= pg %></a>
                                   </li>
                               <% } %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% } else if (currentPageNum >= totalPages - 3) {
                               // Current page is near the end %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                               <% for (int pg = totalPages - 4; pg <= totalPages - 2; pg++) { %>
                                   <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                       <a class="page-link" href="<%= pgBaseUrl %><%= pg %>"><%= pg %></a>
                                   </li>
                               <% }
                           } else {
                               // Current page is in the middle %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                               <% for (int pg = currentPageNum - 1; pg <= currentPageNum + 1; pg++) { %>
                                   <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                       <a class="page-link" href="<%= pgBaseUrl %><%= pg %>"><%= pg %></a>
                                   </li>
                               <% } %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% }

                           // Show last 2 pages
                           for (int pg = totalPages - 1; pg <= totalPages; pg++) { %>
                               <li class="page-item <%= pg == currentPageNum ? "active" : "" %>">
                                   <a class="page-link" href="<%= pgBaseUrl %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                       }
                    %>

                    <!-- Next -->
                    <li class="page-item <%= currentPageNum >= totalPages ? "disabled" : "" %>">
                        <a class="page-link" href="<%= pgBaseUrl %><%= currentPageNum + 1 %>">
                            <i class="fa-solid fa-chevron-right fa-xs"></i>
                        </a>
                    </li>
                </ul>
            </nav>
        <% } %>
    <% } %>

</div>
</main>

<!-- ===== DELETE CONFIRMATION MODAL ===== -->
<div id="deleteCopyModal" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.7); z-index:9999; align-items:center; justify-content:center; backdrop-filter:blur(4px);">
    <div style="background:var(--bg-card); border:1px solid var(--border-light); border-radius:var(--radius-lg); padding:36px; max-width:440px; width:90%; box-shadow:var(--shadow-lg); position:relative;">
        <div style="font-size:2.5rem; margin-bottom:14px; text-align:center;">🗑️</div>
        <h3 style="font-size:1.15rem; font-weight:700; color:var(--text-primary); margin-bottom:10px; text-align:center;">Xác nhận xóa bản sao</h3>
        <p style="color:var(--text-secondary); font-size:0.9rem; margin-bottom:28px; text-align:center; line-height:1.6;">
            Bạn có chắc muốn xóa bản sao: <strong id="deleteBarcodeLabel"></strong>?<br>
            <span style="color:var(--danger); font-size:0.82rem;">Hành động này không thể hoàn tác.</span>
        </p>
        <div style="display:flex; gap:12px; justify-content:flex-end;">
            <button onclick="document.getElementById('deleteCopyModal').style.display='none'" class="btn btn-outline">Hủy</button>
            <a id="confirmDeleteLink" href="#" class="btn btn-danger">
                <i class="fa-solid fa-trash"></i> Xóa
            </a>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>

<script>
function confirmDeleteCopy(id, barcode) {
    document.getElementById('deleteBarcodeLabel').textContent = barcode;
    document.getElementById('confirmDeleteLink').href = '<%= ctx %><%= rolePath %>/book/copy/delete?id=' + id;
    document.getElementById('deleteCopyModal').style.display = 'flex';
}
document.getElementById('deleteCopyModal').addEventListener('click', function(e) {
    if (e.target === this) this.style.display = 'none';
});
</script>
