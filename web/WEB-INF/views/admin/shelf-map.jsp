<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.BookCopy, java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="shelf" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    List<BookCopy> copyList = (List<BookCopy>) request.getAttribute("copyList");
    List<String> distinctAreas = (List<String>) request.getAttribute("distinctAreas");
    String successMsg = (String) session.getAttribute("successMsg");
    String errorMsg = (String) session.getAttribute("errorMsg");
    if (successMsg != null) session.removeAttribute("successMsg");
    if (errorMsg != null) session.removeAttribute("errorMsg");
%>

<main class="page-wrapper">
    <div class="container" style="padding-top: 30px; padding-bottom: 50px;">
        <div class="section-header" style="margin-bottom: 30px; display: flex; justify-content: space-between; align-items: center;">
            <div>
                <h1 class="section-title"><i class="fa-solid fa-map-location-dot"></i> Sơ đồ bố trí kho sách (Shelf Map)</h1>
                <p class="section-subtitle">Định vị và cập nhật khu lưu trữ (Area), dãy kệ sách (Shelf), và ô chứa (Slot) của từng bản sao</p>
            </div>
        </div>

        <!-- Alerts -->
        <% if (successMsg != null) { %>
            <div class="alert alert-success" style="background: #e8f8f5; border-left: 5px solid #2ecc71; color: #27ae60; padding: 15px; border-radius: 8px; margin-bottom: 24px;">
                <i class="fa-solid fa-circle-check"></i> <%= successMsg %>
            </div>
        <% } %>
        <% if (errorMsg != null) { %>
            <div class="alert alert-error" style="background: #fde8e7; border-left: 5px solid #e74c3c; color: #c0392b; padding: 15px; border-radius: 8px; margin-bottom: 24px;">
                <i class="fa-solid fa-circle-exclamation"></i> <%= errorMsg %>
            </div>
        <% } %>

        <!-- Filter bar -->
        <div style="background: white; border-radius: 12px; padding: 20px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); margin-bottom: 30px;">
            <form action="${pageContext.request.contextPath}/admin/shelf" method="get" style="display: flex; gap: 15px; flex-wrap: wrap; align-items: flex-end;">
                <div style="flex: 1; min-width: 250px;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Tìm kiếm bản sao</label>
                    <input type="text" name="keyword" value="${keyword}" placeholder="Nhập mã Barcode hoặc tiêu đề sách..." 
                           style="width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
                </div>
                <div style="width: 200px;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Khu vực lưu trữ (Area)</label>
                    <select name="area" style="width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem; background: white;">
                        <option value="">Tất cả khu vực</option>
                        <% if (distinctAreas != null) {
                            for (String a : distinctAreas) {
                        %>
                            <option value="<%= a %>" ${selectedArea == a ? 'selected' : ''}><%= a %></option>
                        <%  }
                        } %>
                    </select>
                </div>
                <div>
                    <button type="submit" class="btn btn-primary" style="padding: 10px 24px; border-radius: 8px;">
                        <i class="fa-solid fa-magnifying-glass"></i> Định vị
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/shelf" class="btn btn-secondary" style="padding: 10px 20px; border-radius: 8px; text-decoration: none; margin-left: 8px; display: inline-block;">
                        Đặt lại
                    </a>
                </div>
            </form>
        </div>

        <!-- Table list -->
        <div style="background: white; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); overflow: hidden;">
            <table style="width: 100%; border-collapse: collapse; text-align: left;">
                <thead>
                    <tr style="background: #f8f9fa; border-bottom: 1px solid #eee;">
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Barcode</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Tên đầu sách / ISBN</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Khu vực (Area)</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Kệ sách (Shelf)</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Ô chứa (Slot)</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Trạng thái</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem; text-align: right;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (copyList == null || copyList.isEmpty()) { %>
                        <tr>
                            <td colspan="7" style="padding: 40px; text-align: center; color: var(--text-muted);">
                                <i class="fa-solid fa-map" style="font-size: 2.5rem; margin-bottom: 15px; display: block;"></i>
                                Không tìm thấy bản sao sách nào trong kho
                            </td>
                        </tr>
                    <% } else {
                        for (BookCopy bc : copyList) {
                            String badgeColor = "";
                            if ("AVAILABLE".equals(bc.getStatus())) {
                                badgeColor = "background: #e8f8f5; color: #27ae60;";
                            } else if ("BORROWED".equals(bc.getStatus())) {
                                badgeColor = "background: #ebf5fb; color: #2980b9;";
                            } else {
                                badgeColor = "background: #fde8e7; color: #e74c3c;";
                            }
                    %>
                        <tr style="border-bottom: 1px solid #eee; transition: background 0.2s;" onmouseover="this.style.background='#fafafa'" onmouseout="this.style.background='none'">
                            <td style="padding: 16px 20px; font-family: monospace; font-weight: bold;"><%= bc.getBarcode() %></td>
                            <td style="padding: 16px 20px;">
                                <div style="font-weight: 600; color: var(--text-primary);"><%= bc.getBook().getTitle() %></div>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">ISBN: <%= bc.getBook().getIsbn() %></div>
                            </td>
                            <td style="padding: 16px 20px;">
                                <span style="font-weight: 600; color: #f47920;"><%= bc.getArea() != null && !bc.getArea().isEmpty() ? bc.getArea() : "-" %></span>
                            </td>
                            <td style="padding: 16px 20px; font-weight: 500;">
                                <%= bc.getShelf() != null && !bc.getShelf().isEmpty() ? bc.getShelf() : "-" %>
                            </td>
                            <td style="padding: 16px 20px; font-weight: 500;">
                                <%= bc.getSlot() != null && !bc.getSlot().isEmpty() ? bc.getSlot() : "-" %>
                            </td>
                            <td style="padding: 16px 20px;">
                                <span style="font-size: 0.75rem; font-weight: 700; padding: 4px 10px; border-radius: 20px; <%= badgeColor %>"><%= bc.getStatus() %></span>
                            </td>
                            <td style="padding: 16px 20px; text-align: right;">
                                <button class="btn btn-sm btn-primary" onclick="openLocationModal(<%= bc.getId() %>, '<%= bc.getBarcode() %>', '<%= bc.getBook().getTitle() %>', '<%= bc.getArea() %>', '<%= bc.getShelf() %>', '<%= bc.getSlot() %>')" style="font-size: 0.8rem; border-radius: 6px;">
                                    <i class="fa-solid fa-pen-to-square"></i> Đổi vị trí
                                </button>
                            </td>
                        </tr>
                    <% }
                    } %>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
        <% if (((Integer)request.getAttribute("totalPages")) != null && (Integer)request.getAttribute("totalPages") > 1) {
            int totalPg = (Integer) request.getAttribute("totalPages");
            int curPg   = (Integer) request.getAttribute("currentPageNum");
            String kw   = request.getAttribute("keyword") != null ? (String) request.getAttribute("keyword") : "";
            String ar   = request.getAttribute("selectedArea") != null ? (String) request.getAttribute("selectedArea") : "";
            String ctx2 = request.getContextPath();
            String baseUrl = ctx2 + "/admin/shelf?area=" + java.net.URLEncoder.encode(ar,"UTF-8")
                           + "&keyword=" + java.net.URLEncoder.encode(kw,"UTF-8") + "&page=";
        %>
        <nav aria-label="Phân trang" style="margin-top: 30px;">
            <ul class="pagination">
                <!-- Prev -->
                <li class="page-item <%= curPg <= 1 ? "disabled" : "" %>">
                    <a class="page-link" href="<%= baseUrl %><%= curPg - 1 %>">
                        <i class="fa-solid fa-chevron-left fa-xs"></i>
                    </a>
                </li>

                <%
                   if (totalPg <= 7) {
                       for (int pg = 1; pg <= totalPg; pg++) { %>
                           <li class="page-item <%= pg == curPg ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                   } else {
                       // Show first 2 pages
                       for (int pg = 1; pg <= 2; pg++) { %>
                           <li class="page-item <%= pg == curPg ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }

                       if (curPg <= 4) {
                           // Near start
                           for (int pg = 3; pg <= 5; pg++) { %>
                               <li class="page-item <%= pg == curPg ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% } else if (curPg >= totalPg - 3) { %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = totalPg - 4; pg <= totalPg - 2; pg++) { %>
                               <li class="page-item <%= pg == curPg ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                       } else { %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = curPg - 1; pg <= curPg + 1; pg++) { %>
                               <li class="page-item <%= pg == curPg ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% }

                       // Show last 2 pages
                       for (int pg = totalPg - 1; pg <= totalPg; pg++) { %>
                           <li class="page-item <%= pg == curPg ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrl %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                   }
                %>

                <!-- Next -->
                <li class="page-item <%= curPg >= totalPg ? "disabled" : "" %>">
                    <a class="page-link" href="<%= baseUrl %><%= curPg + 1 %>">
                        <i class="fa-solid fa-chevron-right fa-xs"></i>
                    </a>
                </li>
            </ul>
        </nav>
        <% } %>


    </div>
</main>

<!-- Modal Cập nhật vị trí (Update Location) -->
<div id="locationModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center;">
    <div style="background: white; width: 500px; border-radius: 12px; padding: 30px; box-shadow: 0 10px 25px rgba(0,0,0,0.15);">
        <h3 style="margin-top: 0; margin-bottom: 10px; display: flex; align-items: center; gap: 10px;"><i class="fa-solid fa-map-location-dot" style="color: var(--text-brand);"></i> Định vị bản sao sách</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 24px;">Điều chỉnh vị trí lưu trữ vật lý của cuốn sách để thuận tiện tìm kiếm.</p>
        
        <form action="${pageContext.request.contextPath}/admin/shelf/update" method="post">
            <input type="hidden" name="id" id="locCopyId">
            
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Mã Barcode</label>
                <input type="text" id="locBarcode" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa; font-family: monospace; font-weight: bold;">
            </div>

            <div style="margin-bottom: 20px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Tên sách</label>
                <input type="text" id="locBookTitle" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa;">
            </div>

            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Khu vực lưu trữ (Area)</label>
                <input type="text" name="area" id="locArea" required placeholder="Ví dụ: Khu A, Khu B, Phòng học..."
                       style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 24px;">
                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Dãy kệ (Shelf)</label>
                    <input type="text" name="shelf" id="locShelf" placeholder="Ví dụ: Kệ 01, Kệ Ngoại ngữ..."
                           style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
                </div>
                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Ngăn ô (Slot)</label>
                    <input type="text" name="slot" id="locSlot" placeholder="Ví dụ: Ngăn 1, Ô A2..."
                           style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
                </div>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 12px;">
                <button type="button" onclick="closeLocationModal()" class="btn btn-secondary" style="padding: 10px 20px; border-radius: 8px;">Hủy bỏ</button>
                <button type="submit" class="btn btn-primary" style="padding: 10px 24px; border-radius: 8px;">Cập nhật</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openLocationModal(id, barcode, title, area, shelf, slot) {
        document.getElementById('locCopyId').value = id;
        document.getElementById('locBarcode').value = barcode;
        document.getElementById('locBookTitle').value = title;
        document.getElementById('locArea').value = area === 'null' ? '' : area;
        document.getElementById('locShelf').value = shelf === 'null' ? '' : shelf;
        document.getElementById('locSlot').value = slot === 'null' ? '' : slot;
        document.getElementById('locationModal').style.display = 'flex';
    }
    
    function closeLocationModal() {
        document.getElementById('locationModal').style.display = 'none';
    }

    window.onclick = function(event) {
        let lm = document.getElementById('locationModal');
        if (event.target == lm) lm.style.display = 'none';
    }
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
