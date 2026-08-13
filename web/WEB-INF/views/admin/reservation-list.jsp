<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.ReservationRecord, java.time.format.DateTimeFormatter" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="reservation" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    List<ReservationRecord> reservationList = (List<ReservationRecord>) request.getAttribute("reservationList");
    String successMsg = (String) session.getAttribute("successMsg");
    String errorMsg = (String) session.getAttribute("errorMsg");
    if (successMsg != null) session.removeAttribute("successMsg");
    if (errorMsg != null) session.removeAttribute("errorMsg");
%>

<main class="page-wrapper">
    <div class="container" style="padding-top: 30px; padding-bottom: 50px;">
        <div class="section-header" style="margin-bottom: 30px; display: flex; justify-content: space-between; align-items: center;">
            <div>
                <h1 class="section-title"><i class="fa-solid fa-clock"></i> Quản lý Đặt trước sách (Reservations)</h1>
                <p class="section-subtitle">Duyệt các yêu cầu đặt giữ chỗ trước của độc giả khi sách đang bận</p>
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
            <form action="${pageContext.request.contextPath}/admin/reservation/list" method="get" style="display: flex; gap: 15px; flex-wrap: wrap; align-items: flex-end;">
                <div style="flex: 1; min-width: 250px;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Tìm kiếm</label>
                    <input type="text" name="keyword" value="${keyword}" placeholder="Tên độc giả, tên sách, ISBN..." 
                           style="width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
                </div>
                <div style="width: 200px;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Trạng thái</label>
                    <select name="status" style="width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem; background: white;">
                        <option value="">Tất cả trạng thái</option>
                        <option value="WAITING" ${selectedStatus == 'WAITING' ? 'selected' : ''}>Chờ mượn (WAITING)</option>
                        <option value="READY_FOR_PICKUP" ${selectedStatus == 'READY_FOR_PICKUP' ? 'selected' : ''}>Sách sẵn sàng (READY_FOR_PICKUP)</option>
                        <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>Đã mượn sách (COMPLETED)</option>
                        <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy (CANCELLED)</option>
                        <option value="EXPIRED" ${selectedStatus == 'EXPIRED' ? 'selected' : ''}>Đã hết hạn (EXPIRED)</option>
                    </select>
                </div>
                <div>
                    <button type="submit" class="btn btn-primary" style="padding: 10px 24px; border-radius: 8px;">
                        <i class="fa-solid fa-magnifying-glass"></i> Lọc kết quả
                    </button>
                </div>
            </form>
        </div>

        <!-- Table list -->
        <div style="background: white; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); overflow: hidden;">
            <table style="width: 100%; border-collapse: collapse; text-align: left;">
                <thead>
                    <tr style="background: #f8f9fa; border-bottom: 1px solid #eee;">
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Mã</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Độc giả</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Thông tin sách</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Ngày yêu cầu</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Ngày hết hạn</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Trạng thái</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem; text-align: right;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (reservationList == null || reservationList.isEmpty()) { %>
                        <tr>
                            <td colspan="7" style="padding: 40px; text-align: center; color: var(--text-muted);">
                                <i class="fa-solid fa-hourglass-empty" style="font-size: 2.5rem; margin-bottom: 15px; display: block;"></i>
                                Không tìm thấy yêu cầu đặt chỗ nào
                            </td>
                        </tr>
                    <% } else {
                        for (ReservationRecord r : reservationList) {
                            String badgeColor = "";
                            String badgeText = "";
                            if ("WAITING".equals(r.getStatus())) {
                                badgeColor = "background: #fff9e6; color: #f39c12;";
                                badgeText = "Chờ mượn";
                            } else if ("READY_FOR_PICKUP".equals(r.getStatus())) {
                                badgeColor = "background: #ebf5fb; color: #2980b9;";
                                badgeText = "Sách sẵn sàng";
                            } else if ("COMPLETED".equals(r.getStatus())) {
                                badgeColor = "background: #e8f8f5; color: #27ae60;";
                                badgeText = "Đã hoàn thành";
                            } else if ("CANCELLED".equals(r.getStatus())) {
                                badgeColor = "background: #f2f4f4; color: #7f8c8d;";
                                badgeText = "Đã hủy bỏ";
                            } else if ("EXPIRED".equals(r.getStatus())) {
                                badgeColor = "background: #fde8e7; color: #e74c3c;";
                                badgeText = "Đã quá hạn";
                            }
                    %>
                        <tr style="border-bottom: 1px solid #eee; transition: background 0.2s;" onmouseover="this.style.background='#fafafa'" onmouseout="this.style.background='none'">
                            <td style="padding: 16px 20px; font-weight: 600;"><%= r.getId() %></td>
                            <td style="padding: 16px 20px;">
                                <div style="font-weight: 600; color: var(--text-primary);"><%= r.getUser().getFullName() %></div>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">@<%= r.getUser().getUsername() %> | <%= r.getUser().getPhone() %></div>
                            </td>
                            <td style="padding: 16px 20px;">
                                <div style="font-weight: 600; color: var(--text-primary);"><%= r.getBook().getTitle() %></div>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">ISBN: <%= r.getBook().getIsbn() %></div>
                            </td>
                            <td style="padding: 16px 20px; font-size: 0.9rem;">
                                <%= r.getReserveDate() != null ? r.getReserveDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "-" %>
                            </td>
                            <td style="padding: 16px 20px; font-size: 0.9rem;">
                                <%= r.getExpiryDate() != null ? r.getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "-" %>
                            </td>
                            <td style="padding: 16px 20px;">
                                <span style="font-size: 0.75rem; font-weight: 700; padding: 4px 10px; border-radius: 20px; <%= badgeColor %>"><%= badgeText %></span>
                            </td>
                            <td style="padding: 16px 20px; text-align: right;">
                                <% if ("WAITING".equals(r.getStatus())) { %>
                                    <div style="display: flex; justify-content: flex-end; gap: 8px;">
                                        <form action="${pageContext.request.contextPath}${rolePath}/reservation/update" method="post" style="display: inline;">
                                            <input type="hidden" name="id" value="<%= r.getId() %>">
                                            <input type="hidden" name="action" value="ready">
                                            <button type="submit" class="btn btn-sm btn-primary" style="font-size: 0.8rem; border-radius: 6px;">
                                                <i class="fa-solid fa-circle-check"></i> Sách về (Ready)
                                            </button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}${rolePath}/reservation/update" method="post" style="display: inline;">
                                            <input type="hidden" name="id" value="<%= r.getId() %>">
                                            <input type="hidden" name="action" value="cancel">
                                            <button type="submit" class="btn btn-sm btn-outline-danger" style="font-size: 0.8rem; border-radius: 6px; border: 1px solid #7f8c8d; color: #7f8c8d; background: transparent;">
                                                <i class="fa-solid fa-ban"></i> Hủy
                                            </button>
                                        </form>
                                    </div>
                                <% } else if ("READY_FOR_PICKUP".equals(r.getStatus())) { %>
                                    <div style="display: flex; justify-content: flex-end; gap: 8px;">
                                        <form action="${pageContext.request.contextPath}${rolePath}/reservation/update" method="post" style="display: inline;">
                                            <input type="hidden" name="id" value="<%= r.getId() %>">
                                            <input type="hidden" name="action" value="complete">
                                            <button type="submit" class="btn btn-sm btn-success" style="font-size: 0.8rem; border-radius: 6px;">
                                                <i class="fa-solid fa-check-double"></i> Đã lấy (Done)
                                            </button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}${rolePath}/reservation/update" method="post" style="display: inline;">
                                            <input type="hidden" name="id" value="<%= r.getId() %>">
                                            <input type="hidden" name="action" value="cancel">
                                            <button type="submit" class="btn btn-sm btn-outline-danger" style="font-size: 0.8rem; border-radius: 6px; border: 1px solid #7f8c8d; color: #7f8c8d; background: transparent;">
                                                <i class="fa-solid fa-ban"></i> Hủy
                                            </button>
                                        </form>
                                    </div>
                                <% } else { %>
                                    <span style="color: var(--text-muted); font-size: 0.85rem;">Hoàn tất</span>
                                <% } %>
                            </td>
                        </tr>
                    <% }
                    } %>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
        <%
            Integer totalPgR = (Integer) request.getAttribute("totalPages");
            Integer curPgR   = (Integer) request.getAttribute("currentPageNum");
            String kwR       = request.getAttribute("keyword") != null ? (String) request.getAttribute("keyword") : "";
            String stR       = request.getAttribute("selectedStatus") != null ? (String) request.getAttribute("selectedStatus") : "";
            if (totalPgR == null) totalPgR = 1;
            if (curPgR   == null) curPgR   = 1;
            String baseUrlR  = request.getContextPath() + "/librarian/reservation/list?status="
                             + java.net.URLEncoder.encode(stR, "UTF-8")
                             + "&keyword=" + java.net.URLEncoder.encode(kwR, "UTF-8")
                             + "&page=";
            if (totalPgR > 1) {
        %>
        <nav aria-label="Phân trang đặt sách" style="margin-top: 30px;">
            <ul class="pagination">
                <li class="page-item <%= curPgR <= 1 ? "disabled" : "" %>">
                    <a class="page-link" href="<%= baseUrlR %><%= curPgR - 1 %>"><i class="fa-solid fa-chevron-left fa-xs"></i></a>
                </li>
                <%
                   if (totalPgR <= 7) {
                       for (int pg = 1; pg <= totalPgR; pg++) { %>
                           <li class="page-item <%= pg == curPgR ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlR %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                   } else {
                       for (int pg = 1; pg <= 2; pg++) { %>
                           <li class="page-item <%= pg == curPgR ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlR %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                       if (curPgR <= 4) {
                           for (int pg = 3; pg <= 5; pg++) { %>
                               <li class="page-item <%= pg == curPgR ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlR %><%= pg %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% } else if (curPgR >= totalPgR - 3) { %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = totalPgR - 4; pg <= totalPgR - 2; pg++) { %>
                               <li class="page-item <%= pg == curPgR ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlR %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                       } else { %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = curPgR - 1; pg <= curPgR + 1; pg++) { %>
                               <li class="page-item <%= pg == curPgR ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlR %><%= pg %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% }
                       for (int pg = totalPgR - 1; pg <= totalPgR; pg++) { %>
                           <li class="page-item <%= pg == curPgR ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlR %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                   }
                %>
                <li class="page-item <%= curPgR >= totalPgR ? "disabled" : "" %>">
                    <a class="page-link" href="<%= baseUrlR %><%= curPgR + 1 %>"><i class="fa-solid fa-chevron-right fa-xs"></i></a>
                </li>
            </ul>
        </nav>
        <% } %>

    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
