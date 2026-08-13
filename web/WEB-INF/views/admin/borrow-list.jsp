<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.BorrowRecord, java.time.format.DateTimeFormatter" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="borrow" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    List<BorrowRecord> borrowList = (List<BorrowRecord>) request.getAttribute("borrowList");
    String successMsg = (String) session.getAttribute("successMsg");
    String errorMsg = (String) session.getAttribute("errorMsg");
    if (successMsg != null) session.removeAttribute("successMsg");
    if (errorMsg != null) session.removeAttribute("errorMsg");
%>

<main class="page-wrapper">
    <div class="container" style="padding-top: 30px; padding-bottom: 50px;">
        <div class="section-header" style="margin-bottom: 30px; display: flex; justify-content: space-between; align-items: center;">
            <div>
                <h1 class="section-title"><i class="fa-solid fa-handshake"></i> Quản lý Mượn trả sách</h1>
                <p class="section-subtitle">Phê duyệt yêu cầu mượn, ghi nhận trả sách và xử lý sự cố quá hạn/mất mát</p>
            </div>
        </div>

        <!-- Alert messages -->
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
            <form action="${pageContext.request.contextPath}/admin/borrow/list" method="get" style="display: flex; gap: 15px; flex-wrap: wrap; align-items: flex-end;">
                <div style="flex: 1; min-width: 250px;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Tìm kiếm</label>
                    <input type="text" name="keyword" value="${keyword}" placeholder="Tên độc giả, tên sách, ISBN, mã vạch..."
                           style="width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
                </div>
                <div style="width: 200px;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Trạng thái</label>
                    <select name="status" style="width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem; background: white;">
                        <option value="">Tất cả trạng thái</option>
                        <option value="PENDING_PICKUP" ${selectedStatus == 'PENDING_PICKUP' ? 'selected' : ''}>Chờ nhận sách</option>
                        <option value="BORROWED" ${selectedStatus == 'BORROWED' ? 'selected' : ''}>Đang mượn</option>
                        <option value="RETURNED" ${selectedStatus == 'RETURNED' ? 'selected' : ''}>Đã trả</option>
                        <option value="OVERDUE" ${selectedStatus == 'OVERDUE' ? 'selected' : ''}>Quá hạn</option>
                        <option value="EXPIRED" ${selectedStatus == 'EXPIRED' ? 'selected' : ''}>Hết hạn nhận</option>
                        <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
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
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Bản sao / Vị trí</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Ngày mượn / Hạn trả</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Trạng thái</th>
                        <th style="padding: 16px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.85rem; text-align: right;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (borrowList == null || borrowList.isEmpty()) { %>
                        <tr>
                            <td colspan="7" style="padding: 40px; text-align: center; color: var(--text-muted);">
                                <i class="fa-solid fa-folder-open" style="font-size: 2.5rem; margin-bottom: 15px; display: block;"></i>
                                Không tìm thấy lượt mượn trả sách nào
                            </td>
                        </tr>
                    <% } else {
                        for (BorrowRecord br : borrowList) {
                            String badgeColor = "";
                            String badgeText = "";
                            if ("PENDING_PICKUP".equals(br.getStatus())) {
                                badgeColor = "background: #fff9e6; color: #f39c12;";
                                badgeText = "Chờ duyệt mượn";
                            } else if ("BORROWED".equals(br.getStatus())) {
                                badgeColor = "background: #ebf5fb; color: #2980b9;";
                                badgeText = "Đang mượn";
                            } else if ("RETURNED".equals(br.getStatus())) {
                                badgeColor = "background: #e8f8f5; color: #27ae60;";
                                badgeText = "Đã trả";
                            } else if ("OVERDUE".equals(br.getStatus())) {
                                badgeColor = "background: #fde8e7; color: #e74c3c;";
                                badgeText = "Quá hạn";
                            } else if ("EXPIRED".equals(br.getStatus())) {
                                badgeColor = "background: #f2f4f4; color: #7f8c8d;";
                                badgeText = "Hết hạn nhận";
                            } else if ("CANCELLED".equals(br.getStatus())) {
                                badgeColor = "background: #f2f4f4; color: #7f8c8d;";
                                badgeText = "Đã hủy";
                            }
                    %>
                        <tr style="border-bottom: 1px solid #eee; transition: background 0.2s;" onmouseover="this.style.background='#fafafa'" onmouseout="this.style.background='none'">
                            <td style="padding: 16px 20px; font-weight: 600;"><%= br.getId() %></td>
                            <td style="padding: 16px 20px;">
                                <div style="font-weight: 600; color: var(--text-primary);"><%= br.getUser().getFullName() %></div>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">@<%= br.getUser().getUsername() %> | <%= br.getUser().getPhone() %></div>
                            </td>
                            <td style="padding: 16px 20px;">
                                <div style="font-weight: 600; color: var(--text-primary);"><%= br.getBook().getTitle() %></div>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">ISBN: <%= br.getBook().getIsbn() %></div>
                            </td>
                            <td style="padding: 16px 20px;">
                                <% if (br.getBookCopy() != null) { %>
                                    <span style="font-family: monospace; font-size: 0.9rem; font-weight: 600; background: #f0f0f0; padding: 3px 8px; border-radius: 4px;"><%= br.getBookCopy().getBarcode() %></span>
                                <% } else { %>
                                    <span style="color: var(--text-muted); font-style: italic;">Chưa gán bản sao</span>
                                <% } %>
                            </td>
                            <td style="padding: 16px 20px;">
                                <div style="font-size: 0.9rem;"><%= br.getBorrowDate() != null ? br.getBorrowDate() : "-" %></div>
                                <div style="font-size: 0.8rem; color: var(--text-muted); font-weight: 500;">Hạn: <%= br.getDueDate() != null ? br.getDueDate() : "-" %></div>
                            </td>
                            <td style="padding: 16px 20px;">
                                <span style="display: inline-block; white-space: nowrap; font-size: 0.75rem; font-weight: 700; padding: 4px 10px; border-radius: 20px; <%= badgeColor %>"><%= badgeText %></span>
                            </td>
                            <td style="padding: 16px 20px; text-align: right;">
                                <% if ("PENDING_PICKUP".equals(br.getStatus())) { %>
                                    <button class="btn btn-sm btn-primary" onclick="openLoanModal(<%= br.getId() %>, '<%= br.getBook().getTitle() %>', '<%= br.getUser().getFullName() %>', '<%= br.getBookCopy() != null ? br.getBookCopy().getBarcode() : "-" %>', '<%= br.getRequestDate() != null ? br.getRequestDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-" %>', '<%= br.getPickupDeadline() != null ? br.getPickupDeadline().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-" %>')" style="font-size: 0.8rem; border-radius: 6px;">
                                        <i class="fa-solid fa-check"></i> Xác nhận giao sách
                                    </button>
                                <% } else if ("BORROWED".equals(br.getStatus()) || "OVERDUE".equals(br.getStatus())) { %>
                                    <div style="display: flex; justify-content: flex-end; gap: 8px;">
                                        <button class="btn btn-sm btn-success" onclick="openReturnModal(<%= br.getId() %>, '<%= br.getBook().getTitle() %>', '<%= br.getBookCopy().getBarcode() %>')" style="font-size: 0.8rem; border-radius: 6px;">
                                            <i class="fa-solid fa-rotate-left"></i> Nhận trả
                                        </button>
                                        <button class="btn btn-sm btn-outline-danger" onclick="openFineModal(<%= br.getId() %>, <%= br.getUserId() %>, '<%= br.getUser().getFullName() %>', '<%= br.getBook().getTitle() %>')" style="font-size: 0.8rem; border-radius: 6px; border: 1px solid #e74c3c; color: #e74c3c; background: transparent;">
                                            <i class="fa-solid fa-circle-dollar-to-slot"></i> Phạt
                                        </button>
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
            Integer totalPgB = (Integer) request.getAttribute("totalPages");
            Integer curPgB   = (Integer) request.getAttribute("currentPageNum");
            String kwB       = request.getAttribute("keyword") != null ? (String) request.getAttribute("keyword") : "";
            String stB       = request.getAttribute("selectedStatus") != null ? (String) request.getAttribute("selectedStatus") : "";
            if (totalPgB == null) totalPgB = 1;
            if (curPgB   == null) curPgB   = 1;
            String baseUrlB  = request.getContextPath() + "/librarian/borrow/list?status="
                             + java.net.URLEncoder.encode(stB, "UTF-8")
                             + "&keyword=" + java.net.URLEncoder.encode(kwB, "UTF-8")
                             + "&page=";
            if (totalPgB > 1) {
        %>
        <nav aria-label="Phân trang mượn sách" style="margin-top: 30px;">
            <ul class="pagination">
                <li class="page-item <%= curPgB <= 1 ? "disabled" : "" %>">
                    <a class="page-link" href="<%= baseUrlB %><%= curPgB - 1 %>"><i class="fa-solid fa-chevron-left fa-xs"></i></a>
                </li>
                <%
                   if (totalPgB <= 7) {
                       for (int pg = 1; pg <= totalPgB; pg++) { %>
                           <li class="page-item <%= pg == curPgB ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlB %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                   } else {
                       for (int pg = 1; pg <= 2; pg++) { %>
                           <li class="page-item <%= pg == curPgB ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlB %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                       if (curPgB <= 4) {
                           for (int pg = 3; pg <= 5; pg++) { %>
                               <li class="page-item <%= pg == curPgB ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlB %><%= pg %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% } else if (curPgB >= totalPgB - 3) { %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = totalPgB - 4; pg <= totalPgB - 2; pg++) { %>
                               <li class="page-item <%= pg == curPgB ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlB %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                       } else { %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = curPgB - 1; pg <= curPgB + 1; pg++) { %>
                               <li class="page-item <%= pg == curPgB ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlB %><%= pg %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% }
                       for (int pg = totalPgB - 1; pg <= totalPgB; pg++) { %>
                           <li class="page-item <%= pg == curPgB ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlB %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                   }
                %>
                <li class="page-item <%= curPgB >= totalPgB ? "disabled" : "" %>">
                    <a class="page-link" href="<%= baseUrlB %><%= curPgB + 1 %>"><i class="fa-solid fa-chevron-right fa-xs"></i></a>
                </li>
            </ul>
        </nav>
        <% } %>

    </div>
</main>

<!-- Modal Cho Mượn (Assign Barcode) -->
<div id="loanModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center;">
    <div style="background: white; width: 500px; border-radius: 12px; padding: 30px; box-shadow: 0 10px 25px rgba(0,0,0,0.15); position: relative;">
        <h3 style="margin-top: 0; margin-bottom: 10px; display: flex; align-items: center; gap: 10px;"><i class="fa-solid fa-book-open" style="color: var(--text-brand);"></i> Xác nhận giao sách</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 24px;">Xác nhận độc giả đã đến nhận đúng bản sao đang được thư viện giữ.</p>
        
        <form action="${pageContext.request.contextPath}${borrowActionPrefix}/borrow/confirm-pickup" method="post">
            <input type="hidden" name="id" id="loanRecordId">
            
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Độc giả</label>
                <input type="text" id="loanReaderName" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa;">
            </div>
            
            <div style="margin-bottom: 20px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Đầu sách mượn</label>
                <input type="text" id="loanBookTitle" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa;">
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px;">
                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Mã yêu cầu</label>
                    <input type="text" id="loanRequestId" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa;">
                </div>
                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Mã vạch bản sao</label>
                    <input type="text" id="loanCopyBarcode" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa; font-family: monospace;">
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 24px;">
                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Thời điểm yêu cầu</label>
                    <input type="text" id="loanRequestDate" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa;">
                </div>
                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Hạn cuối nhận sách</label>
                    <input type="text" id="loanPickupDeadline" readonly style="width: 100%; padding: 10px; border: 1px solid #f2b8a0; border-radius: 8px; background: #fff8f4; color: #c65319; font-weight: 600;">
                </div>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 12px;">
                <button type="button" onclick="closeLoanModal()" class="btn btn-secondary" style="padding: 10px 20px; border-radius: 8px;">Hủy bỏ</button>
                <button type="submit" class="btn btn-primary" style="padding: 10px 24px; border-radius: 8px;">Xác nhận duyệt</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal Trả Sách (Confirm Return) -->
<div id="returnModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center;">
    <div style="background: white; width: 500px; border-radius: 12px; padding: 30px; box-shadow: 0 10px 25px rgba(0,0,0,0.15);">
        <h3 style="margin-top: 0; margin-bottom: 10px; display: flex; align-items: center; gap: 10px;"><i class="fa-solid fa-rotate-left" style="color: #27ae60;"></i> Nhận trả sách</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 24px;">Ghi nhận cuốn sách đã được hoàn trả về thư viện thành công.</p>
        
        <form action="${pageContext.request.contextPath}${borrowActionPrefix}/borrow/confirm-return" method="post">
            <input type="hidden" name="id" id="returnRecordId">
            
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Tên sách</label>
                <input type="text" id="returnBookTitle" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa;">
            </div>

            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Mã vạch</label>
                <input type="text" id="returnBarcode" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa; font-family: monospace;">
            </div>

            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Tình trạng cuốn sách</label>
                <select name="condition" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem; background: white;">
                    <option value="GOOD">Bình thường (GOOD)</option>
                    <option value="DAMAGED">Hỏng nhẹ (DAMAGED)</option>
                    <option value="LOST">Mất mát hoàn toàn (LOST)</option>
                </select>
            </div>

            <div style="margin-bottom: 24px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Ghi chú bổ sung</label>
                <textarea name="note" placeholder="Tình trạng trang sách, ghi chú hao mòn..." rows="3" 
                          style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem; resize: vertical;"></textarea>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 12px;">
                <button type="button" onclick="closeReturnModal()" class="btn btn-secondary" style="padding: 10px 20px; border-radius: 8px;">Hủy bỏ</button>
                <button type="submit" class="btn btn-success" style="padding: 10px 24px; border-radius: 8px;">Xác nhận trả</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal Phạt Tiền (Record Fine) -->
<div id="fineModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center;">
    <div style="background: white; width: 500px; border-radius: 12px; padding: 30px; box-shadow: 0 10px 25px rgba(0,0,0,0.15);">
        <h3 style="margin-top: 0; margin-bottom: 10px; display: flex; align-items: center; gap: 10px;"><i class="fa-solid fa-circle-dollar-to-slot" style="color: #e74c3c;"></i> Lập phiếu phạt độc giả</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 24px;">Tạo khoản tiền phạt độc giả do trả quá hạn hoặc làm hỏng/mất sách.</p>
        
        <form action="${pageContext.request.contextPath}/admin/fine/create" method="post">
            <input type="hidden" name="borrowRecordId" id="fineBorrowRecordId">
            <input type="hidden" name="userId" id="fineUserId">
            
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Độc giả bị phạt</label>
                <input type="text" id="fineReaderName" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa;">
            </div>

            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Tên đầu sách</label>
                <input type="text" id="fineBookTitle" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa;">
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 16px;">
                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Số tiền phạt (đ)</label>
                    <input type="number" name="amount" required min="1000" step="500" value="10000"
                           style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
                </div>
                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary);">Số ngày quá hạn</label>
                    <input type="number" name="overdueDays" required min="0" value="0"
                           style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
                </div>
            </div>

            <div style="margin-bottom: 24px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">Lý do phạt</label>
                <input type="text" name="reason" required placeholder="Trả muộn X ngày, hư hại bìa sách..."
                       style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 12px;">
                <button type="button" onclick="closeFineModal()" class="btn btn-secondary" style="padding: 10px 20px; border-radius: 8px;">Hủy bỏ</button>
                <button type="submit" class="btn btn-danger" style="padding: 10px 24px; border-radius: 8px; background: #e74c3c; border-color: #e74c3c;">Lập phiếu</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openLoanModal(id, title, name, barcode, requestDate, pickupDeadline) {
        document.getElementById('loanRecordId').value = id;
        document.getElementById('loanBookTitle').value = title;
        document.getElementById('loanReaderName').value = name;
        document.getElementById('loanRequestId').value = '#' + id;
        document.getElementById('loanCopyBarcode').value = barcode;
        document.getElementById('loanRequestDate').value = requestDate;
        document.getElementById('loanPickupDeadline').value = pickupDeadline;
        document.getElementById('loanModal').style.display = 'flex';
    }
    
    function closeLoanModal() {
        document.getElementById('loanModal').style.display = 'none';
    }

    function openReturnModal(id, title, barcode) {
        document.getElementById('returnRecordId').value = id;
        document.getElementById('returnBookTitle').value = title;
        document.getElementById('returnBarcode').value = barcode;
        document.getElementById('returnModal').style.display = 'flex';
    }
    
    function closeReturnModal() {
        document.getElementById('returnModal').style.display = 'none';
    }

    function openFineModal(brId, userId, name, bookTitle) {
        document.getElementById('fineBorrowRecordId').value = brId;
        document.getElementById('fineUserId').value = userId;
        document.getElementById('fineReaderName').value = name;
        document.getElementById('fineBookTitle').value = bookTitle;
        document.getElementById('fineModal').style.display = 'flex';
    }
    
    function closeFineModal() {
        document.getElementById('fineModal').style.display = 'none';
    }

    // Đóng modal khi nhấn ra ngoài
    window.onclick = function(event) {
        let lm = document.getElementById('loanModal');
        let rm = document.getElementById('returnModal');
        let fm = document.getElementById('fineModal');
        if (event.target == lm) lm.style.display = 'none';
        if (event.target == rm) rm.style.display = 'none';
        if (event.target == fm) fm.style.display = 'none';
    }
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
