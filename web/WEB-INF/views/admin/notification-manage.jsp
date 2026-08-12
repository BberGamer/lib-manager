<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.BorrowRecord, model.Fine, model.Notification, java.text.NumberFormat, java.util.Locale, java.time.format.DateTimeFormatter" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="notifications" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    List<BorrowRecord> nearDueLoans = (List<BorrowRecord>) request.getAttribute("nearDueLoans");
    List<BorrowRecord> overdueLoans = (List<BorrowRecord>) request.getAttribute("overdueLoans");
    List<Fine> unpaidFines = (List<Fine>) request.getAttribute("unpaidFines");
    List<Notification> sentHistory = (List<Notification>) request.getAttribute("sentHistory");
    int totalSent = (Integer) request.getAttribute("totalSent");
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
                <h1 class="section-title"><i class="fa-solid fa-bullhorn"></i> Quản lý Thông báo</h1>
                <p class="section-subtitle">Soạn và gửi thông báo đến người dùng trong hệ thống</p>
            </div>
            <div style="background: #eef3ff; padding: 10px 20px; border-radius: 12px; text-align: center; border: 1px solid #d0deff;">
                <div style="font-size: 1.8rem; font-weight: 800; color: var(--text-brand); line-height: 1;"><%= totalSent %></div>
                <div style="font-size: 0.75rem; font-weight: 600; color: var(--text-secondary); margin-top: 4px; text-transform: uppercase;">Đã gửi</div>
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
        <% 
            String reqError = (String) request.getAttribute("error");
            if (reqError != null) { 
        %>
            <div class="alert alert-error" style="background: #fde8e7; border-left: 5px solid #e74c3c; color: #c0392b; padding: 15px; border-radius: 8px; margin-bottom: 24px;">
                <i class="fa-solid fa-circle-exclamation"></i> <%= reqError %>
            </div>
        <% } %>

        <!-- Tab Headers -->
        <div style="display: flex; gap: 10px; border-bottom: 2px solid #eee; margin-bottom: 25px;">
            <button class="tab-btn active" onclick="switchTab('compose-tab')" id="tab-compose-tab-btn" style="padding: 12px 20px; font-weight: 700; border: none; background: none; cursor: pointer; color: var(--text-secondary); border-bottom: 3px solid transparent; transition: all 0.2s; font-size: 0.95rem;">
                <i class="fa-solid fa-pen-to-square"></i> Soạn &amp; Lịch sử gửi
            </button>
            <button class="tab-btn" onclick="switchTab('reminders-tab')" id="tab-reminders-tab-btn" style="padding: 12px 20px; font-weight: 700; border: none; background: none; cursor: pointer; color: var(--text-secondary); border-bottom: 3px solid transparent; transition: all 0.2s; font-size: 0.95rem;">
                <i class="fa-solid fa-clock"></i> Nhắc nhở mượn trả &amp; Phạt
            </button>
        </div>

        <c:set var="rolePath" value="${navUser.admin ? '/admin' : '/librarian'}" />

        <!-- TAB 1: COMPOSE & HISTORY -->
        <div class="tab-content" id="compose-tab" style="display: block;">
            
            <!-- Compose Form (Screenshot Style) -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 15px rgba(0,0,0,0.03); border: 1px solid #eef2f6; margin-bottom: 35px;">
                <h3 style="margin-top: 0; margin-bottom: 20px; font-size: 1.1rem; font-weight: 700; color: var(--text-primary); border-bottom: 1px solid #f1f4f8; padding-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                    <i class="fa-solid fa-paper-plane" style="color: var(--text-brand);"></i> Soạn thông báo mới
                </h3>
                
                <form action="${pageContext.request.contextPath}${rolePath}/notification/send" method="post" onsubmit="showLoadingSpinner(this)">
                    <input type="hidden" name="action" value="create-notification">
                    
                    <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 20px; margin-bottom: 16px;">
                        <div>
                            <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Tiêu đề *</label>
                            <input type="text" name="title" required placeholder="Nhập tiêu đề thông báo..." 
                                   style="width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
                        </div>
                        <div>
                            <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Loại thông báo</label>
                            <select name="type" style="width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem; background: white;">
                                <option value="SYSTEM">📢 Hệ thống</option>
                                <option value="DUE_REMINDER">⏰ Hạn trả</option>
                                <option value="FINE">💸 Phí phạt</option>
                            </select>
                        </div>
                    </div>
                    
                    <div style="margin-bottom: 16px;">
                        <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Nội dung *</label>
                        <textarea name="message" required rows="4" placeholder="Nhập nội dung thông báo..." 
                                  style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem; resize: vertical;"></textarea>
                    </div>
                    
                    <div style="display: flex; gap: 15px; align-items: flex-end;">
                        <div style="flex: 1;">
                            <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">ID người nhận (phân cách bằng dấu phẩy) — <i>để trống = gửi tất cả độc giả</i></label>
                            <input type="text" name="userIds" placeholder="Ví dụ: 1, 2, 5 hoặc username..." 
                                   style="width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
                        </div>
                        <button type="submit" class="btn btn-primary" style="padding: 10px 24px; border-radius: 8px; font-weight: 600; background: #e67e22; border-color: #e67e22;">
                            <i class="fa-solid fa-paper-plane"></i> Gửi ngay
                        </button>
                    </div>
                </form>
            </div>

            <!-- Sent History List -->
            <div style="background: white; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); overflow: hidden; border: 1px solid #eee;">
                <div style="padding: 18px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
                    <h3 style="margin: 0; font-size: 1rem; font-weight: 700; color: var(--text-primary);">Lịch sử thông báo đã gửi</h3>
                    
                    <form action="${pageContext.request.contextPath}${rolePath}/notification/manage" method="get" style="display: flex; gap: 10px; margin: 0;">
                        <select name="filterType" style="padding: 6px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 0.85rem; background: white;">
                            <option value="">Tất cả loại</option>
                            <option value="SYSTEM" ${selectedFilterType == 'SYSTEM' ? 'selected' : ''}>Hệ thống</option>
                            <option value="DUE_REMINDER" ${selectedFilterType == 'DUE_REMINDER' ? 'selected' : ''}>Hạn trả</option>
                            <option value="OVERDUE" ${selectedFilterType == 'OVERDUE' ? 'selected' : ''}>Quá hạn</option>
                            <option value="FINE" ${selectedFilterType == 'FINE' ? 'selected' : ''}>Phí phạt</option>
                        </select>
                        <button type="submit" class="btn btn-sm btn-secondary" style="padding: 6px 12px; border-radius: 6px;"><i class="fa-solid fa-filter"></i> Lọc</button>
                    </form>
                </div>
                
                <table style="width: 100%; border-collapse: collapse; text-align: left;">
                    <thead>
                        <tr style="background: #f8f9fa; border-bottom: 1px solid #eee;">
                            <th style="padding: 14px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Người nhận</th>
                            <th style="padding: 14px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Tiêu đề</th>
                            <th style="padding: 14px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Loại</th>
                            <th style="padding: 14px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Đã đọc</th>
                            <th style="padding: 14px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Ngày gửi</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (sentHistory == null || sentHistory.isEmpty()) { %>
                            <tr>
                                <td colspan="5" style="padding: 40px; text-align: center; color: var(--text-muted);">
                                    <i class="fa-solid fa-folder-open" style="font-size: 2.2rem; margin-bottom: 12px; display: block;"></i>
                                    Chưa có thông báo nào được gửi
                                </td>
                            </tr>
                        <% } else {
                            for (Notification n : sentHistory) {
                                String typeBadge = "";
                                if ("SYSTEM".equals(n.getType())) {
                                    typeBadge = "<span style='font-size:0.75rem; font-weight:700; padding:2px 8px; border-radius:12px; background:#eef3ff; color:var(--text-brand);'><i class='fa-solid fa-bullhorn'></i> Hệ thống</span>";
                                } else if ("DUE_REMINDER".equals(n.getType())) {
                                    typeBadge = "<span style='font-size:0.75rem; font-weight:700; padding:2px 8px; border-radius:12px; background:#fff9e6; color:#f39c12;'><i class='fa-solid fa-clock'></i> Hạn trả</span>";
                                } else if ("OVERDUE".equals(n.getType())) {
                                    typeBadge = "<span style='font-size:0.75rem; font-weight:700; padding:2px 8px; border-radius:12px; background:#fde8e7; color:#e74c3c;'><i class='fa-solid fa-exclamation-circle'></i> Quá hạn</span>";
                                } else if ("FINE".equals(n.getType())) {
                                    typeBadge = "<span style='font-size:0.75rem; font-weight:700; padding:2px 8px; border-radius:12px; background:#ebf5fb; color:#2980b9;'><i class='fa-solid fa-coins'></i> Phí phạt</span>";
                                }
                        %>
                            <tr style="border-bottom: 1px solid #eee; font-size: 0.9rem;">
                                <td style="padding: 14px 20px;">
                                    <div style="font-weight: 600;"><%= n.getUser() != null ? n.getUser().getFullName() : "—" %></div>
                                    <div style="font-size: 0.78rem; color: var(--text-muted);">@<%= n.getUser() != null ? n.getUser().getUsername() : "" %></div>
                                </td>
                                <td style="padding: 14px 20px;">
                                    <div style="font-weight: 600; color: var(--text-primary);"><%= n.getTitle() %></div>
                                    <div style="font-size: 0.78rem; color: var(--text-muted);"><%= n.getMessage().length() > 50 ? n.getMessage().substring(0, 47) + "..." : n.getMessage() %></div>
                                </td>
                                <td style="padding: 14px 20px;"><%= typeBadge %></td>
                                <td style="padding: 14px 20px;">
                                    <% if (n.isIsRead()) { %>
                                        <span style="font-size: 0.8rem; color: #27ae60;"><i class="fa-solid fa-circle-check"></i> Đã đọc</span>
                                    <% } else { %>
                                        <span style="font-size: 0.8rem; color: #f39c12;"><i class="fa-solid fa-clock"></i> Chưa đọc</span>
                                    <% } %>
                                </td>
                                <td style="padding: 14px 20px; font-size: 0.8rem; color: var(--text-muted);">
                                    <%= n.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) %>
                                </td>
                            </tr>
                        <% }
                        } %>
                    </tbody>
                </table>
            </div>
            
            <!-- Pagination for History -->
            <%
                Integer totalPgN = (Integer) request.getAttribute("totalPages");
                Integer curPgN   = (Integer) request.getAttribute("currentPageNum");
                String ftN       = request.getAttribute("selectedFilterType") != null ? (String) request.getAttribute("selectedFilterType") : "";
                String rolePathN = request.getAttribute("rolePath") != null ? (String) request.getAttribute("rolePath") : "/librarian";
                if (totalPgN == null) totalPgN = 1;
                if (curPgN   == null) curPgN   = 1;
                String baseUrlN  = request.getContextPath() + rolePathN + "/notification/manage?filterType="
                                 + java.net.URLEncoder.encode(ftN, "UTF-8") + "&page=";
                if (totalPgN > 1) {
            %>
            <nav aria-label="Phân trang thông báo" style="margin-top: 24px;">
                <ul class="pagination">
                    <li class="page-item <%= curPgN <= 1 ? "disabled" : "" %>">
                        <a class="page-link" href="<%= baseUrlN %><%= curPgN - 1 %>"><i class="fa-solid fa-chevron-left fa-xs"></i></a>
                    </li>
                    <%
                       if (totalPgN <= 7) {
                           for (int pg = 1; pg <= totalPgN; pg++) { %>
                               <li class="page-item <%= pg == curPgN ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlN %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                       } else {
                           for (int pg = 1; pg <= 2; pg++) { %>
                               <li class="page-item <%= pg == curPgN ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlN %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                           if (curPgN <= 4) {
                               for (int pg = 3; pg <= 5; pg++) { %>
                                   <li class="page-item <%= pg == curPgN ? "active" : "" %>">
                                       <a class="page-link" href="<%= baseUrlN %><%= pg %>"><%= pg %></a>
                                   </li>
                               <% } %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% } else if (curPgN >= totalPgN - 3) { %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                               <% for (int pg = totalPgN - 4; pg <= totalPgN - 2; pg++) { %>
                                   <li class="page-item <%= pg == curPgN ? "active" : "" %>">
                                       <a class="page-link" href="<%= baseUrlN %><%= pg %>"><%= pg %></a>
                                   </li>
                               <% }
                           } else { %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                               <% for (int pg = curPgN - 1; pg <= curPgN + 1; pg++) { %>
                                   <li class="page-item <%= pg == curPgN ? "active" : "" %>">
                                       <a class="page-link" href="<%= baseUrlN %><%= pg %>"><%= pg %></a>
                                   </li>
                               <% } %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% }
                           for (int pg = totalPgN - 1; pg <= totalPgN; pg++) { %>
                               <li class="page-item <%= pg == curPgN ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlN %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                       }
                    %>
                    <li class="page-item <%= curPgN >= totalPgN ? "disabled" : "" %>">
                        <a class="page-link" href="<%= baseUrlN %><%= curPgN + 1 %>"><i class="fa-solid fa-chevron-right fa-xs"></i></a>
                    </li>
                </ul>
            </nav>
            <% } %>

        </div>

        <!-- TAB 2: AUTOMATIC REMINDERS -->
        <div class="tab-content" id="reminders-tab" style="display: none;">
            
            <div style="display: flex; gap: 15px; margin-bottom: 25px; border-bottom: 1px dashed #ddd; padding-bottom: 15px;">
                <button onclick="switchReminderSubSection('due-reminders-sub')" class="btn btn-sm btn-outline active-sub-btn" id="due-sub-btn" style="border-radius: 20px; padding: 6px 16px;">Sách sắp đến hạn (<%= nearDueLoans != null ? nearDueLoans.size() : 0 %>)</button>
                <button onclick="switchReminderSubSection('overdue-reminders-sub')" class="btn btn-sm btn-outline" id="overdue-sub-btn" style="border-radius: 20px; padding: 6px 16px;">Quá hạn (<%= overdueLoans != null ? overdueLoans.size() : 0 %>)</button>
                <button onclick="switchReminderSubSection('fines-reminders-sub')" class="btn btn-sm btn-outline" id="fines-sub-btn" style="border-radius: 20px; padding: 6px 16px;">Phạt chưa nộp (<%= unpaidFines != null ? unpaidFines.size() : 0 %>)</button>
            </div>

            <!-- Due Reminders Section -->
            <div class="reminder-sub-section" id="due-reminders-sub" style="display: block;">
                <div style="background: white; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); overflow: hidden; border: 1px solid #eee;">
                    <table style="width: 100%; border-collapse: collapse; text-align: left;">
                        <thead>
                            <tr style="background: #f8f9fa; border-bottom: 1px solid #eee;">
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Mã mượn</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Độc giả</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Sách mượn</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Hạn trả</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); text-align: right;">Gửi email</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (nearDueLoans == null || nearDueLoans.isEmpty()) { %>
                                <tr>
                                    <td colspan="5" style="padding: 40px; text-align: center; color: var(--text-muted);">
                                        <i class="fa-solid fa-check-circle" style="font-size: 2.2rem; margin-bottom: 12px; display: block; color: #2ecc71;"></i>
                                        Không có độc giả nào sắp hết hạn sách mượn (trong 3 ngày tới)
                                    </td>
                                </tr>
                            <% } else {
                                for (BorrowRecord br : nearDueLoans) {
                            %>
                                <tr style="border-bottom: 1px solid #eee;">
                                    <td style="padding: 16px 20px; font-weight: 600;">#<%= br.getId() %></td>
                                    <td style="padding: 16px 20px;">
                                        <div style="font-weight: 600;"><%= br.getUser().getFullName() %></div>
                                        <div style="font-size: 0.8rem; color: var(--text-muted);"><%= br.getUser().getEmail() %></div>
                                    </td>
                                    <td style="padding: 16px 20px;">
                                        <div style="font-weight: 600;"><%= br.getBook().getTitle() %></div>
                                        <div style="font-size: 0.8rem; color: var(--text-muted);">Barcode: <%= br.getBookCopy() != null ? br.getBookCopy().getBarcode() : "" %></div>
                                    </td>
                                    <td style="padding: 16px 20px; font-weight: 600; color: #f39c12;"><%= br.getDueDate() %></td>
                                    <td style="padding: 16px 20px; text-align: right;">
                                        <form action="${pageContext.request.contextPath}${rolePath}/notification/send" method="post" style="display:inline;" onsubmit="showLoadingSpinner(this)">
                                            <input type="hidden" name="action" value="send-due">
                                            <input type="hidden" name="id" value="<%= br.getId() %>">
                                            <button type="submit" class="btn btn-sm btn-primary" style="font-size: 0.8rem; border-radius: 6px;"><i class="fa-solid fa-paper-plane"></i> Gửi nhắc nhở</button>
                                        </form>
                                    </td>
                                </tr>
                            <% }
                            } %>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Overdue Section -->
            <div class="reminder-sub-section" id="overdue-reminders-sub" style="display: none;">
                <div style="background: white; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); overflow: hidden; border: 1px solid #eee;">
                    <table style="width: 100%; border-collapse: collapse; text-align: left;">
                        <thead>
                            <tr style="background: #f8f9fa; border-bottom: 1px solid #eee;">
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Mã mượn</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Độc giả</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Sách mượn</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Hạn trả</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); text-align: right;">Gửi email</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (overdueLoans == null || overdueLoans.isEmpty()) { %>
                                <tr>
                                    <td colspan="5" style="padding: 40px; text-align: center; color: var(--text-muted);">
                                        <i class="fa-solid fa-check-circle" style="font-size: 2.2rem; margin-bottom: 12px; display: block; color: #2ecc71;"></i>
                                        Không có độc giả nào bị quá hạn
                                    </td>
                                </tr>
                            <% } else {
                                for (BorrowRecord br : overdueLoans) {
                            %>
                                <tr style="border-bottom: 1px solid #eee;">
                                    <td style="padding: 16px 20px; font-weight: 600;">#<%= br.getId() %></td>
                                    <td style="padding: 16px 20px;">
                                        <div style="font-weight: 600;"><%= br.getUser().getFullName() %></div>
                                        <div style="font-size: 0.8rem; color: var(--text-muted);"><%= br.getUser().getEmail() %></div>
                                    </td>
                                    <td style="padding: 16px 20px;">
                                        <div style="font-weight: 600;"><%= br.getBook().getTitle() %></div>
                                        <div style="font-size: 0.8rem; color: var(--text-muted);">Barcode: <%= br.getBookCopy() != null ? br.getBookCopy().getBarcode() : "" %></div>
                                    </td>
                                    <td style="padding: 16px 20px; font-weight: 700; color: #e74c3c;"><%= br.getDueDate() %> (Quá hạn)</td>
                                    <td style="padding: 16px 20px; text-align: right;">
                                        <form action="${pageContext.request.contextPath}${rolePath}/notification/send" method="post" style="display:inline;" onsubmit="showLoadingSpinner(this)">
                                            <input type="hidden" name="action" value="send-overdue">
                                            <input type="hidden" name="id" value="<%= br.getId() %>">
                                            <button type="submit" class="btn btn-sm btn-danger" style="font-size: 0.8rem; border-radius: 6px; background:#e74c3c; border-color:#e74c3c;"><i class="fa-solid fa-paper-plane"></i> Gửi cảnh báo</button>
                                        </form>
                                    </td>
                                </tr>
                            <% }
                            } %>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Unpaid Fines Section -->
            <div class="reminder-sub-section" id="fines-reminders-sub" style="display: none;">
                <div style="background: white; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); overflow: hidden; border: 1px solid #eee;">
                    <table style="width: 100%; border-collapse: collapse; text-align: left;">
                        <thead>
                            <tr style="background: #f8f9fa; border-bottom: 1px solid #eee;">
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Mã phạt</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Độc giả</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Số tiền</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary);">Lý do</th>
                                <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); text-align: right;">Gửi email</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (unpaidFines == null || unpaidFines.isEmpty()) { %>
                                <tr>
                                    <td colspan="5" style="padding: 40px; text-align: center; color: var(--text-muted);">
                                        <i class="fa-solid fa-check-circle" style="font-size: 2.2rem; margin-bottom: 12px; display: block; color: #2ecc71;"></i>
                                        Không phát hiện khoản tiền phạt chưa thanh toán nào
                                    </td>
                                </tr>
                            <% } else {
                                for (Fine f : unpaidFines) {
                            %>
                                <tr style="border-bottom: 1px solid #eee;">
                                    <td style="padding: 16px 20px; font-weight: 600;">#<%= f.getId() %></td>
                                    <td style="padding: 16px 20px;">
                                        <div style="font-weight: 600;"><%= f.getUser().getFullName() %></div>
                                        <div style="font-size: 0.8rem; color: var(--text-muted);"><%= f.getUser().getEmail() %></div>
                                    </td>
                                    <td style="padding: 16px 20px; font-weight: 700; color: #e74c3c;"><%= currencyFormat.format(f.getAmount()) %></td>
                                    <td style="padding: 16px 20px;"><%= f.getReason() %></td>
                                    <td style="padding: 16px 20px; text-align: right;">
                                        <form action="${pageContext.request.contextPath}${rolePath}/notification/send" method="post" style="display:inline;" onsubmit="showLoadingSpinner(this)">
                                            <input type="hidden" name="action" value="send-fine">
                                            <input type="hidden" name="id" value="<%= f.getId() %>">
                                            <button type="submit" class="btn btn-sm btn-warning" style="font-size: 0.8rem; border-radius: 6px; background:#f39c12; border-color:#f39c12; color:white;"><i class="fa-solid fa-paper-plane"></i> Yêu cầu nộp phạt</button>
                                        </form>
                                    </td>
                                </tr>
                            <% }
                            } %>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </div>
</main>

<div id="loadingOverlay" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255,255,255,0.7); z-index: 9999; align-items: center; justify-content: center; flex-direction: column;">
    <div style="border: 4px solid #f3f3f3; border-top: 4px solid var(--text-brand); border-radius: 50%; width: 50px; height: 50px; animation: spin 1s linear infinite; margin-bottom: 15px;"></div>
    <span style="font-weight: 600; color: var(--text-secondary);">Đang xử lý gửi thông báo đến người nhận...</span>
</div>

<style>
    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }
</style>

<script>
    function switchTab(tabId) {
        document.querySelectorAll('.tab-content').forEach(el => el.style.display = 'none');
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('active');
            btn.style.borderBottom = '3px solid transparent';
            btn.style.color = 'var(--text-secondary)';
        });

        document.getElementById(tabId).style.display = 'block';
        
        let activeBtn = document.getElementById('tab-' + tabId + '-btn');
        activeBtn.classList.add('active');
        activeBtn.style.borderBottom = '3px solid var(--text-brand)';
        activeBtn.style.color = 'var(--text-brand)';
    }

    function switchReminderSubSection(subSectionId) {
        document.querySelectorAll('.reminder-sub-section').forEach(el => el.style.display = 'none');
        document.getElementById(subSectionId).style.display = 'block';

        // Styling for buttons
        document.getElementById('due-sub-btn').className = "btn btn-sm btn-outline";
        document.getElementById('overdue-sub-btn').className = "btn btn-sm btn-outline";
        document.getElementById('fines-sub-btn').className = "btn btn-sm btn-outline";

        let activeSubBtnId = '';
        if (subSectionId === 'due-reminders-sub') activeSubBtnId = 'due-sub-btn';
        if (subSectionId === 'overdue-reminders-sub') activeSubBtnId = 'overdue-sub-btn';
        if (subSectionId === 'fines-reminders-sub') activeSubBtnId = 'fines-sub-btn';

        document.getElementById(activeSubBtnId).className = "btn btn-sm btn-primary";
    }

    // Set initial tab styling
    document.addEventListener("DOMContentLoaded", function() {
        // Keep the state of filters or tabs active
        let paramPage = '<%= request.getParameter("page") %>';
        let paramFilterType = '<%= request.getParameter("filterType") %>';
        if ((paramPage && paramPage !== 'null') || (paramFilterType && paramFilterType !== 'null')) {
            switchTab('compose-tab');
        } else {
            switchTab('compose-tab');
        }
    });

    function showLoadingSpinner(form) {
        document.getElementById('loadingOverlay').style.display = 'flex';
    }
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
