<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Fine, java.time.format.DateTimeFormatter, java.text.NumberFormat, java.util.Locale" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="fine" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    List<Fine> fineList = (List<Fine>) request.getAttribute("fineList");
    String successMsg = (String) session.getAttribute("successMsg");
    String errorMsg = (String) session.getAttribute("errorMsg");
    if (successMsg != null) session.removeAttribute("successMsg");
    if (errorMsg != null) session.removeAttribute("errorMsg");
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
%>

<main class="page-wrapper">
    <div class="container" style="padding-top: 30px; padding-bottom: 50px;">
        <div class="section-header" style="margin-bottom: 30px; display: flex; justify-content: space-between; align-items: center;">
            <div>
                <h1 class="section-title"><i class="fa-solid fa-circle-dollar-to-slot"></i> Quản lý các Khoản phạt độc giả</h1>
                <p class="section-subtitle">Thu tiền phạt trả muộn, hư hại sách và quản lý các phương thức thanh toán</p>
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
            <form action="${pageContext.request.contextPath}/admin/fine/list" method="get" style="display: flex; gap: 15px; flex-wrap: wrap; align-items: flex-end;">
                <div style="flex: 1; min-width: 250px;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Tìm kiếm</label>
                    <input type="text" name="keyword" value="${keyword}" placeholder="Tên độc giả, tên sách, lý do phạt..." 
                           style="width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
                </div>
                <div style="width: 200px;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Trạng thái thanh toán</label>
                    <select name="status" style="width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem; background: white;">
                        <option value="">Tất cả trạng thái</option>
                        <option value="UNPAID" ${selectedStatus == 'UNPAID' ? 'selected' : ''}>Chưa thanh toán (UNPAID)</option>
                        <option value="PENDING_VERIFY" ${selectedStatus == 'PENDING_VERIFY' ? 'selected' : ''}>Chờ xác minh</option>
                        <option value="PAID" ${selectedStatus == 'PAID' ? 'selected' : ''}>Đã thanh toán (PAID)</option>
                        <option value="WAIVED" ${selectedStatus == 'WAIVED' ? 'selected' : ''}>Được miễn giảm (WAIVED)</option>
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
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Số tiền phạt</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Lý do / Ngày tạo</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Trạng thái</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem; text-align: right;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (fineList == null || fineList.isEmpty()) { %>
                        <tr>
                            <td colspan="7" style="padding: 40px; text-align: center; color: var(--text-muted);">
                                <i class="fa-solid fa-credit-card" style="font-size: 2.5rem; margin-bottom: 15px; display: block;"></i>
                                Không tìm thấy khoản phạt tiền nào
                            </td>
                        </tr>
                    <% } else {
                        for (Fine f : fineList) {
                            String badgeColor = "";
                            String badgeText = "";
                            if ("UNPAID".equals(f.getStatus())) {
                                badgeColor = "background: #fde8e7; color: #e74c3c;";
                                badgeText = "Chưa thanh toán";
                            } else if ("PAID".equals(f.getStatus())) {
                                badgeColor = "background: #e8f8f5; color: #27ae60;";
                                badgeText = "Đã thanh toán";
                            } else if ("WAIVED".equals(f.getStatus())) {
                                badgeColor = "background: #f2f4f4; color: #7f8c8d;";
                                badgeText = "Được miễn giảm";
                            } else if ("PENDING_VERIFY".equals(f.getStatus())) {
                                badgeColor = "background: #fff9e6; color: #f39c12;";
                                badgeText = "Chờ xác minh";
                            }
                    %>
                        <tr style="border-bottom: 1px solid #eee; transition: background 0.2s;" onmouseover="this.style.background='#fafafa'" onmouseout="this.style.background='none'">
                            <td style="padding: 16px 20px; font-weight: 600;"><%= f.getId() %></td>
                            <td style="padding: 16px 20px;">
                                <div style="font-weight: 600; color: var(--text-primary);"><%= f.getUser().getFullName() %></div>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">@<%= f.getUser().getUsername() %> | <%= f.getUser().getPhone() %></div>
                            </td>
                            <td style="padding: 16px 20px;">
                                <div style="font-weight: 600; color: var(--text-primary);"><%= f.getBorrowRecord().getBook().getTitle() %></div>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">Lượt mượn: #<%= f.getBorrowRecordId() %></div>
                            </td>
                            <td style="padding: 16px 20px; font-weight: 700; color: #e74c3c;">
                                <%= currencyFormat.format(f.getAmount()) %>
                            </td>
                            <td style="padding: 16px 20px;">
                                <div style="font-weight: 500;"><%= f.getReason() %></div>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">
                                    Ngày tạo: <%= f.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) %>
                                </div>
                            </td>
                            <td style="padding: 16px 20px;">
                                <span style="display: inline-block; white-space: nowrap; font-size: 0.75rem; font-weight: 700; padding: 4px 10px; border-radius: 20px; <%= badgeColor %>"><%= badgeText %></span>
                                <% if ("PAID".equals(f.getStatus())) { %>
                                    <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 4px; white-space: nowrap;">
                                        Cách: <%= f.getPaymentMethod() %> | Ngày: <%= f.getPaidDate() %>
                                    </div>
                                <% } %>
                            </td>
                            <td style="padding: 16px 20px; text-align: right;">
                                <% if ("UNPAID".equals(f.getStatus()) || "PENDING_VERIFY".equals(f.getStatus())) { %>
                                    <div style="display: flex; justify-content: flex-end; gap: 8px;">
                                        <button class="btn btn-sm btn-success" onclick="openPaymentModal(<%= f.getId() %>, '<%= f.getUser().getFullName() %>', <%= f.getAmount().doubleValue() %>)" style="font-size: 0.8rem; border-radius: 6px;">
                                            <i class="fa-solid fa-cash-register"></i> Đóng phạt
                                        </button>
                                        <form action="${pageContext.request.contextPath}/admin/fine/update-status" method="post" style="display: inline;" onsubmit="return confirm('Bạn có chắc chắn muốn miễn giảm khoản tiền phạt này không?')">
                                            <input type="hidden" name="id" value="<%= f.getId() %>">
                                            <input type="hidden" name="status" value="WAIVED">
                                            <button type="submit" class="btn btn-sm btn-outline-secondary" style="font-size: 0.8rem; border-radius: 6px; border: 1px solid #7f8c8d; color: #7f8c8d; background: transparent;">
                                                <i class="fa-solid fa-hand-holding-heart"></i> Miễn giảm
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
            Integer totalPgF = (Integer) request.getAttribute("totalPages");
            Integer curPgF   = (Integer) request.getAttribute("currentPageNum");
            String kwF       = request.getAttribute("keyword") != null ? (String) request.getAttribute("keyword") : "";
            String stF       = request.getAttribute("selectedStatus") != null ? (String) request.getAttribute("selectedStatus") : "";
            if (totalPgF == null) totalPgF = 1;
            if (curPgF   == null) curPgF   = 1;
            String baseUrlF  = request.getContextPath() + "/admin/fine/list?status="
                             + java.net.URLEncoder.encode(stF, "UTF-8")
                             + "&keyword=" + java.net.URLEncoder.encode(kwF, "UTF-8")
                             + "&page=";
            if (totalPgF > 1) {
        %>
        <nav aria-label="Phân trang phạt" style="margin-top: 30px;">
            <ul class="pagination">
                <li class="page-item <%= curPgF <= 1 ? "disabled" : "" %>">
                    <a class="page-link" href="<%= baseUrlF %><%= curPgF - 1 %>"><i class="fa-solid fa-chevron-left fa-xs"></i></a>
                </li>
                <%
                   if (totalPgF <= 7) {
                       for (int pg = 1; pg <= totalPgF; pg++) { %>
                           <li class="page-item <%= pg == curPgF ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlF %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                   } else {
                       for (int pg = 1; pg <= 2; pg++) { %>
                           <li class="page-item <%= pg == curPgF ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlF %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                       if (curPgF <= 4) {
                           for (int pg = 3; pg <= 5; pg++) { %>
                               <li class="page-item <%= pg == curPgF ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlF %><%= pg %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% } else if (curPgF >= totalPgF - 3) { %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = totalPgF - 4; pg <= totalPgF - 2; pg++) { %>
                               <li class="page-item <%= pg == curPgF ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlF %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                       } else { %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = curPgF - 1; pg <= curPgF + 1; pg++) { %>
                               <li class="page-item <%= pg == curPgF ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlF %><%= pg %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% }
                       for (int pg = totalPgF - 1; pg <= totalPgF; pg++) { %>
                           <li class="page-item <%= pg == curPgF ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlF %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                   }
                %>
                <li class="page-item <%= curPgF >= totalPgF ? "disabled" : "" %>">
                    <a class="page-link" href="<%= baseUrlF %><%= curPgF + 1 %>"><i class="fa-solid fa-chevron-right fa-xs"></i></a>
                </li>
            </ul>
        </nav>
        <% } %>

    </div>
</main>

<!-- Modal Thanh toán Khoản Phạt (Pay Fine) -->
<div id="paymentModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center;">
    <div style="background: white; width: 500px; border-radius: 12px; padding: 30px; box-shadow: 0 10px 25px rgba(0,0,0,0.15);">
        <h3 style="margin-top: 0; margin-bottom: 10px; display: flex; align-items: center; gap: 10px;"><i class="fa-solid fa-cash-register" style="color: #27ae60;"></i> Xác nhận Đóng phạt</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 24px;">Ghi nhận thanh toán khoản phạt độc giả trực tiếp bằng tiền mặt hoặc qua ví điện tử.</p>
        
        <form action="${pageContext.request.contextPath}/admin/fine/update-status" method="post">
            <input type="hidden" name="id" id="payFineId">
            <input type="hidden" name="status" value="PAID">
            
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Độc giả nộp phạt</label>
                <input type="text" id="payReaderName" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa;">
            </div>

            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Số tiền thanh toán</label>
                <input type="text" id="payAmountText" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa; font-weight: bold; color: #e74c3c;">
            </div>

            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Phương thức thanh toán</label>
                <select name="paymentMethod" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem; background: white;">
                    <option value="CASH">Tiền mặt trực tiếp (CASH)</option>
                    <option value="ONLINE">Ví điện tử / Chuyển khoản (ONLINE)</option>
                </select>
            </div>

            <div style="margin-bottom: 24px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Ghi chú thanh toán</label>
                <input type="text" name="paymentNote" placeholder="Mã giao dịch, biên lai số..."
                       style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 12px;">
                <button type="button" onclick="closePaymentModal()" class="btn btn-secondary" style="padding: 10px 20px; border-radius: 8px;">Hủy bỏ</button>
                <button type="submit" class="btn btn-success" style="padding: 10px 24px; border-radius: 8px;">Xác nhận nộp</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openPaymentModal(id, name, amount) {
        document.getElementById('payFineId').value = id;
        document.getElementById('payReaderName').value = name;
        
        let formatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
        document.getElementById('payAmountText').value = formatter.format(amount);
        
        document.getElementById('paymentModal').style.display = 'flex';
    }
    
    function closePaymentModal() {
        document.getElementById('paymentModal').style.display = 'none';
    }

    window.onclick = function(event) {
        let pm = document.getElementById('paymentModal');
        if (event.target == pm) pm.style.display = 'none';
    }
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
