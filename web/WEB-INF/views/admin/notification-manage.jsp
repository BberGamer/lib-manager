<%--
    Trang Quản lý Thông báo dành cho Thủ thư / Quản trị viên.
    Nhận các thuộc tính: nearDueLoans, overdueLoans, unpaidFines, sentHistory, totalSent, selectedFilterType.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="notifications" scope="request" />
<c:set var="pageTitle" value="Quản lý Thông báo – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/notification.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:set var="rolePath" value="${navUser.admin ? '/admin' : '/librarian'}" />
<c:set var="manageUrl" value="${pageContext.request.contextPath}${rolePath}/notification/manage" />
<c:set var="sendUrl" value="${pageContext.request.contextPath}${rolePath}/notification/send" />

<main class="page-wrapper">
    <div class="container notification-manage-container">
        <div class="notification-manage-header">
            <div>
                <h1 class="section-title">
                    <i class="fa-solid fa-bullhorn"></i> Quản lý Thông báo
                </h1>
                <p class="section-subtitle">
                    Soạn và gửi thông báo đến người dùng trong hệ thống
                </p>
            </div>
            <div class="notification-manage-stat">
                <div class="stat-number"><c:out value="${totalSent}" /></div>
                <div class="stat-label">Đã gửi</div>
            </div>
        </div>

        <!-- Alert messages -->
        <c:if test="${not empty sessionScope.successMsg}">
            <div class="alert alert-success">
                <i class="fa-solid fa-circle-check"></i>
                <c:out value="${sessionScope.successMsg}" />
            </div>
            <c:remove var="successMsg" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.errorMsg}">
            <div class="alert alert-error">
                <i class="fa-solid fa-circle-exclamation"></i>
                <c:out value="${sessionScope.errorMsg}" />
            </div>
            <c:remove var="errorMsg" scope="session" />
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-error">
                <i class="fa-solid fa-circle-exclamation"></i>
                <c:out value="${error}" />
            </div>
        </c:if>

        <!-- Main Tab Navigation -->
        <div class="notification-tabs">
            <button class="notification-tab-btn ${activeTab eq 'reminders' ? '' : 'active'}"
                    onclick="switchNotificationTab('compose-tab')"
                    id="tab-compose-tab-btn"
                    type="button">
                <i class="fa-solid fa-pen-to-square"></i> Soạn &amp; Lịch sử gửi
            </button>
            <button class="notification-tab-btn ${activeTab eq 'reminders' ? 'active' : ''}"
                    onclick="switchNotificationTab('reminders-tab')"
                    id="tab-reminders-tab-btn"
                    type="button">
                <i class="fa-solid fa-clock"></i> Nhắc nhở mượn trả &amp; Phạt
            </button>
        </div>

        <!-- TAB 1: COMPOSE & HISTORY -->
        <div class="tab-content" id="compose-tab" style="${activeTab eq 'reminders' ? 'display: none;' : 'display: block;'}">
            
            <!-- Compose Form Card -->
            <div class="notification-compose-card">
                <h3 class="notification-compose-title">
                    <i class="fa-solid fa-paper-plane" style="color: var(--text-brand, #e67e22);"></i> Soạn thông báo mới
                </h3>
                
                <form action="${sendUrl}" method="post" onsubmit="showNotificationLoading(this)">
                    <input type="hidden" name="action" value="create-notification">
                    
                    <div class="notification-form-grid">
                        <div>
                            <label class="notification-field-label">Tiêu đề *</label>
                            <input type="text" name="title" required
                                   placeholder="Nhập tiêu đề thông báo..."
                                   class="notification-input">
                        </div>
                        <div>
                            <label class="notification-field-label">Loại thông báo</label>
                            <select name="type" class="notification-select">
                                <option value="SYSTEM">📢 Hệ thống</option>
                                <option value="DUE_REMINDER">⏰ Hạn trả</option>
                                <option value="FINE">💸 Phí phạt</option>
                            </select>
                        </div>
                    </div>
                    
                    <div class="notification-field">
                        <label class="notification-field-label">Nội dung *</label>
                        <textarea name="message" required rows="4"
                                  placeholder="Nhập nội dung thông báo..."
                                  class="notification-textarea"></textarea>
                    </div>
                    
                    <div class="notification-form-bottom">
                        <div class="recipient-group">
                            <label class="notification-field-label">
                                ID người nhận (phân cách bằng dấu phẩy) — <i>để trống = gửi tất cả độc giả</i>
                            </label>
                            <input type="text" name="userIds"
                                   placeholder="Ví dụ: 1, 2, 5 hoặc username..."
                                   class="notification-input">
                        </div>
                        <button type="submit" class="notification-submit-btn">
                            <i class="fa-solid fa-paper-plane"></i> Gửi ngay
                        </button>
                    </div>
                </form>
            </div>

            <!-- Sent History List Card -->
            <div class="notification-table-card">
                <div class="notification-table-header">
                    <h3 class="notification-table-title">Lịch sử thông báo đã gửi</h3>
                    
                    <form action="${manageUrl}" method="get" style="display: flex; gap: 10px; margin: 0;">
                        <input type="hidden" name="tab" value="compose">
                        <select name="filterType" class="notification-select" style="width: auto; padding: 6px 12px; font-size: 0.85rem;">
                            <option value="">Tất cả loại</option>
                            <option value="SYSTEM" ${selectedFilterType eq 'SYSTEM' ? 'selected' : ''}>Hệ thống</option>
                            <option value="DUE_REMINDER" ${selectedFilterType eq 'DUE_REMINDER' ? 'selected' : ''}>Hạn trả</option>
                            <option value="OVERDUE" ${selectedFilterType eq 'OVERDUE' ? 'selected' : ''}>Quá hạn</option>
                            <option value="FINE" ${selectedFilterType eq 'FINE' ? 'selected' : ''}>Phí phạt</option>
                        </select>
                        <button type="submit" class="btn btn-sm btn-secondary" style="padding: 6px 12px; border-radius: 6px;">
                            <i class="fa-solid fa-filter"></i> Lọc
                        </button>
                    </form>
                </div>
                
                <table class="notification-table">
                    <thead>
                        <tr>
                            <th>Người nhận</th>
                            <th>Tiêu đề</th>
                            <th>Loại</th>
                            <th>Đã đọc</th>
                            <th>Ngày gửi</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty sentHistory}">
                                <tr>
                                    <td colspan="5" class="notification-empty-cell">
                                        <i class="fa-solid fa-folder-open"></i>
                                        <span>Chưa có thông báo nào được gửi</span>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="n" items="${sentHistory}">
                                    <tr>
                                        <td>
                                            <div style="font-weight: 600;">
                                                <c:out value="${not empty n.user ? n.user.fullName : '—'}" />
                                            </div>
                                            <div style="font-size: 0.78rem; color: var(--text-muted, #95a5a6);">
                                                @<c:out value="${not empty n.user ? n.user.username : ''}" />
                                            </div>
                                        </td>
                                        <td>
                                            <div style="font-weight: 600; color: var(--text-primary);">
                                                <c:out value="${n.title}" />
                                            </div>
                                            <div style="font-size: 0.78rem; color: var(--text-muted, #95a5a6);">
                                                <c:choose>
                                                    <c:when test="${fn:length(n.message) > 50}">
                                                        <c:out value="${fn:substring(n.message, 0, 47)}..." />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:out value="${n.message}" />
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${n.type eq 'SYSTEM'}">
                                                    <span class="notif-badge notif-badge-system">
                                                        <i class="fa-solid fa-bullhorn"></i> Hệ thống
                                                    </span>
                                                </c:when>
                                                <c:when test="${n.type eq 'DUE_REMINDER'}">
                                                    <span class="notif-badge notif-badge-due">
                                                        <i class="fa-solid fa-clock"></i> Hạn trả
                                                    </span>
                                                </c:when>
                                                <c:when test="${n.type eq 'OVERDUE'}">
                                                    <span class="notif-badge notif-badge-overdue">
                                                        <i class="fa-solid fa-circle-exclamation"></i> Quá hạn
                                                    </span>
                                                </c:when>
                                                <c:when test="${n.type eq 'FINE'}">
                                                    <span class="notif-badge notif-badge-fine">
                                                        <i class="fa-solid fa-coins"></i> Phí phạt
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="notif-badge notif-badge-system">
                                                        <c:out value="${n.type}" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${n.isRead}">
                                                    <span style="font-size: 0.8rem; color: #27ae60;">
                                                        <i class="fa-solid fa-circle-check"></i> Đã đọc
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="font-size: 0.8rem; color: #f39c12;">
                                                        <i class="fa-solid fa-clock"></i> Chưa đọc
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-size: 0.8rem; color: var(--text-muted, #95a5a6);">
                                            <c:out value="${n.createdAt}" />
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <!-- Pagination for Sent History -->
            <c:if test="${totalPages > 1}">
                <nav aria-label="Phân trang thông báo" style="margin-top: 24px;">
                    <ul class="pagination">
                        <li class="page-item ${currentPageNum <= 1 ? 'disabled' : ''}">
                            <c:url var="prevUrl" value="${rolePath}/notification/manage">
                                <c:param name="tab" value="compose" />
                                <c:param name="filterType" value="${selectedFilterType}" />
                                <c:param name="page" value="${currentPageNum - 1}" />
                            </c:url>
                            <a class="page-link" href="${prevUrl}">
                                <i class="fa-solid fa-chevron-left fa-xs"></i>
                            </a>
                        </li>
                        <c:forEach begin="1" end="${totalPages}" var="p">
                            <c:url var="pUrl" value="${rolePath}/notification/manage">
                                <c:param name="tab" value="compose" />
                                <c:param name="filterType" value="${selectedFilterType}" />
                                <c:param name="page" value="${p}" />
                            </c:url>
                            <li class="page-item ${p == currentPageNum ? 'active' : ''}">
                                <a class="page-link" href="${pUrl}">
                                    <c:out value="${p}" />
                                </a>
                            </li>
                        </c:forEach>
                        <li class="page-item ${currentPageNum >= totalPages ? 'disabled' : ''}">
                            <c:url var="nextUrl" value="${rolePath}/notification/manage">
                                <c:param name="tab" value="compose" />
                                <c:param name="filterType" value="${selectedFilterType}" />
                                <c:param name="page" value="${currentPageNum + 1}" />
                            </c:url>
                            <a class="page-link" href="${nextUrl}">
                                <i class="fa-solid fa-chevron-right fa-xs"></i>
                            </a>
                        </li>
                    </ul>
                </nav>
            </c:if>
        </div>

        <!-- TAB 2: AUTOMATIC REMINDERS -->
        <div class="tab-content" id="reminders-tab" style="${activeTab eq 'reminders' ? 'display: block;' : 'display: none;'}">
            
            <!-- Automated Scheduler Control Banner -->
            <div style="background: white; border: 1px solid #cbd5e1; border-radius: 12px; padding: 20px 24px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.04);">
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; margin-bottom: 16px;">
                    <div>
                        <div style="font-weight: 700; font-size: 1.08rem; color: #1e293b; display: flex; align-items: center; gap: 8px;">
                            <i class="fa-solid fa-robot" style="color: #3b82f6;"></i> Hệ thống quét tự động &amp; Lập lịch gửi thông báo (Batch Job)
                        </div>
                        <div style="font-size: 0.85rem; color: #64748b; margin-top: 4px;">
                            Tự động đồng bộ tiền phạt và quét gửi thông báo hạn mượn sách lúc <b>07:00 sáng</b> mỗi ngày.
                        </div>
                    </div>
                    <div>
                        <form action="${sendUrl}" method="post" onsubmit="showNotificationLoading(this)" style="margin: 0;">
                            <input type="hidden" name="action" value="run-auto-job">
                            <input type="hidden" name="sub" value="${empty activeSubTab ? 'due' : activeSubTab}">
                            <button type="submit" class="btn btn-primary" style="padding: 9px 18px; border-radius: 8px; font-weight: 600; display: inline-flex; align-items: center; gap: 8px; box-shadow: 0 2px 6px rgba(230,126,34,0.3);">
                                <i class="fa-solid fa-bolt"></i> Chạy quét &amp; Gửi hàng loạt ngay
                            </button>
                        </form>
                    </div>
                </div>

                <!-- Toggle Controls Form -->
                <form action="${sendUrl}" method="post" style="display: flex; align-items: center; gap: 20px; flex-wrap: wrap; padding-top: 14px; border-top: 1px dashed #e2e8f0; margin: 0;">
                    <input type="hidden" name="action" value="toggle-automation">
                    <input type="hidden" name="sub" value="${empty activeSubTab ? 'due' : activeSubTab}">
                    
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span style="font-size: 0.88rem; font-weight: 600; color: #334155;">
                            <i class="fa-regular fa-clock"></i> Lập lịch quét (Cron Job 07:00):
                        </span>
                        <input type="hidden" name="enableJob" id="enableJobInput" value="${autoJobEnabled}">
                        <button type="button" onclick="document.getElementById('enableJobInput').value = '${not autoJobEnabled}'; this.form.submit();"
                                class="btn btn-sm ${autoJobEnabled ? 'btn-success' : 'btn-secondary'}"
                                style="padding: 4px 12px; border-radius: 20px; font-size: 0.78rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px; ${autoJobEnabled ? 'background: #16a34a; border-color: #16a34a; color: white;' : 'background: #94a3b8; border-color: #94a3b8; color: white;'}">
                            <i class="fa-solid ${autoJobEnabled ? 'fa-toggle-on' : 'fa-toggle-off'}"></i>
                            ${autoJobEnabled ? 'ĐANG BẬT' : 'ĐÃ TẮT'}
                        </button>
                    </div>

                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span style="font-size: 0.88rem; font-weight: 600; color: #334155;">
                            <i class="fa-regular fa-envelope"></i> Tự động gửi Email (SMTP):
                        </span>
                        <input type="hidden" name="enableEmail" id="enableEmailInput" value="${autoEmailEnabled}">
                        <button type="button" onclick="document.getElementById('enableEmailInput').value = '${not autoEmailEnabled}'; this.form.submit();"
                                class="btn btn-sm ${autoEmailEnabled ? 'btn-success' : 'btn-secondary'}"
                                style="padding: 4px 12px; border-radius: 20px; font-size: 0.78rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px; ${autoEmailEnabled ? 'background: #16a34a; border-color: #16a34a; color: white;' : 'background: #94a3b8; border-color: #94a3b8; color: white;'}">
                            <i class="fa-solid ${autoEmailEnabled ? 'fa-toggle-on' : 'fa-toggle-off'}"></i>
                            ${autoEmailEnabled ? 'ĐANG BẬT' : 'ĐÃ TẮT'}
                        </button>
                    </div>
                </form>
            </div>

            <!-- Sub-tab Navigation -->
            <div class="reminder-sub-nav">
                <button type="button" onclick="switchReminderSubSection('due-reminders-sub')"
                        class="btn btn-sm ${empty activeSubTab || activeSubTab eq 'due' ? 'btn-primary' : 'btn-outline'} reminder-sub-btn" id="due-sub-btn">
                    Sách sắp đến hạn (${not empty nearDueLoans ? fn:length(nearDueLoans) : 0})
                </button>
                <button type="button" onclick="switchReminderSubSection('overdue-reminders-sub')"
                        class="btn btn-sm ${activeSubTab eq 'overdue' ? 'btn-primary' : 'btn-outline'} reminder-sub-btn" id="overdue-sub-btn">
                    Quá hạn (${not empty overdueLoans ? fn:length(overdueLoans) : 0})
                </button>
                <button type="button" onclick="switchReminderSubSection('fines-reminders-sub')"
                        class="btn btn-sm ${activeSubTab eq 'fines' ? 'btn-primary' : 'btn-outline'} reminder-sub-btn" id="fines-sub-btn">
                    Phạt chưa nộp (${not empty unpaidFines ? fn:length(unpaidFines) : 0})
                </button>
            </div>

            <!-- Due Reminders Section -->
            <div class="reminder-sub-section" id="due-reminders-sub" style="${empty activeSubTab || activeSubTab eq 'due' ? 'display: block;' : 'display: none;'}">
                <div class="notification-table-card">
                    <table class="notification-table">
                        <thead>
                            <tr>
                                <th>Mã mượn</th>
                                <th>Độc giả</th>
                                <th>Sách mượn</th>
                                <th>Hạn trả</th>
                                <th style="text-align: right;">Gửi email</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty nearDueLoans}">
                                    <tr>
                                        <td colspan="5" class="notification-empty-cell">
                                            <i class="fa-solid fa-circle-check" style="color: #2ecc71;"></i>
                                            <span>Không có độc giả nào sắp hết hạn sách mượn (trong 3 ngày tới)</span>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="br" items="${nearDueLoans}">
                                        <tr>
                                            <td style="font-weight: 600;">#<c:out value="${br.id}" /></td>
                                            <td>
                                                <div style="font-weight: 600;"><c:out value="${br.user.fullName}" /></div>
                                                <div style="font-size: 0.8rem; color: var(--text-muted, #95a5a6);"><c:out value="${br.user.email}" /></div>
                                            </td>
                                            <td>
                                                <div style="font-weight: 600;"><c:out value="${br.book.title}" /></div>
                                                <div style="font-size: 0.8rem; color: var(--text-muted, #95a5a6);">
                                                    Barcode: <c:out value="${not empty br.bookCopy ? br.bookCopy.barcode : ''}" />
                                                </div>
                                            </td>
                                            <td style="font-weight: 600; color: #f39c12;">
                                                <c:out value="${br.dueDate}" />
                                            </td>
                                            <td style="text-align: right;">
                                                <form action="${sendUrl}" method="post" style="display: inline;" onsubmit="showNotificationLoading(this)">
                                                    <input type="hidden" name="action" value="send-due">
                                                    <input type="hidden" name="sub" value="due">
                                                    <input type="hidden" name="id" value="${br.id}">
                                                    <button type="submit" class="btn btn-sm btn-primary" style="font-size: 0.8rem; border-radius: 6px;">
                                                        <i class="fa-solid fa-paper-plane"></i> Gửi nhắc nhở
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Overdue Section -->
            <div class="reminder-sub-section" id="overdue-reminders-sub" style="${activeSubTab eq 'overdue' ? 'display: block;' : 'display: none;'}">
                <div class="notification-table-card">
                    <table class="notification-table">
                        <thead>
                            <tr>
                                <th>Mã mượn</th>
                                <th>Độc giả</th>
                                <th>Sách mượn</th>
                                <th>Hạn trả</th>
                                <th style="text-align: right;">Gửi email</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty overdueLoans}">
                                    <tr>
                                        <td colspan="5" class="notification-empty-cell">
                                            <i class="fa-solid fa-circle-check" style="color: #2ecc71;"></i>
                                            <span>Không có độc giả nào bị quá hạn</span>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="br" items="${overdueLoans}">
                                        <tr>
                                            <td style="font-weight: 600;">#<c:out value="${br.id}" /></td>
                                            <td>
                                                <div style="font-weight: 600;"><c:out value="${br.user.fullName}" /></div>
                                                <div style="font-size: 0.8rem; color: var(--text-muted, #95a5a6);"><c:out value="${br.user.email}" /></div>
                                            </td>
                                            <td>
                                                <div style="font-weight: 600;"><c:out value="${br.book.title}" /></div>
                                                <div style="font-size: 0.8rem; color: var(--text-muted, #95a5a6);">
                                                    Barcode: <c:out value="${not empty br.bookCopy ? br.bookCopy.barcode : ''}" />
                                                </div>
                                            </td>
                                            <td style="font-weight: 700; color: #e74c3c;">
                                                <c:out value="${br.dueDate}" /> (Quá hạn)
                                            </td>
                                            <td style="text-align: right;">
                                                <form action="${sendUrl}" method="post" style="display: inline;" onsubmit="showNotificationLoading(this)">
                                                    <input type="hidden" name="action" value="send-overdue">
                                                    <input type="hidden" name="sub" value="overdue">
                                                    <input type="hidden" name="id" value="${br.id}">
                                                    <button type="submit" class="btn btn-sm btn-danger" style="font-size: 0.8rem; border-radius: 6px; background:#e74c3c; border-color:#e74c3c;">
                                                        <i class="fa-solid fa-paper-plane"></i> Gửi cảnh báo
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Unpaid Fines Section -->
            <div class="reminder-sub-section" id="fines-reminders-sub" style="${activeSubTab eq 'fines' ? 'display: block;' : 'display: none;'}">
                <div class="notification-table-card">
                    <table class="notification-table">
                        <thead>
                            <tr>
                                <th>Mã phạt</th>
                                <th>Độc giả</th>
                                <th>Số tiền</th>
                                <th>Lý do</th>
                                <th style="text-align: right;">Gửi email</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty unpaidFines}">
                                    <tr>
                                        <td colspan="5" class="notification-empty-cell">
                                            <i class="fa-solid fa-circle-check" style="color: #2ecc71;"></i>
                                            <span>Không phát hiện khoản tiền phạt chưa thanh toán nào</span>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="f" items="${unpaidFines}">
                                        <tr>
                                            <td style="font-weight: 600;">#<c:out value="${f.id}" /></td>
                                            <td>
                                                <div style="font-weight: 600;"><c:out value="${f.user.fullName}" /></div>
                                                <div style="font-size: 0.8rem; color: var(--text-muted, #95a5a6);"><c:out value="${f.user.email}" /></div>
                                            </td>
                                            <td style="font-weight: 700; color: #e74c3c;">
                                                <fmt:formatNumber value="${f.amount}" type="currency" currencySymbol="₫" maxFractionDigits="0" />
                                            </td>
                                            <td><c:out value="${f.reason}" /></td>
                                            <td style="text-align: right;">
                                                <form action="${sendUrl}" method="post" style="display: inline;" onsubmit="showNotificationLoading(this)">
                                                    <input type="hidden" name="action" value="send-fine">
                                                    <input type="hidden" name="sub" value="fines">
                                                    <input type="hidden" name="id" value="${f.id}">
                                                    <button type="submit" class="btn btn-sm btn-warning" style="font-size: 0.8rem; border-radius: 6px; background:#f39c12; border-color:#f39c12; color:white;">
                                                        <i class="fa-solid fa-paper-plane"></i> Yêu cầu nộp phạt
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </div>
</main>

<!-- Loading Overlay -->
<div id="loadingOverlay" class="notification-loading-overlay" style="display: none;">
    <div class="notification-spinner"></div>
    <span style="font-weight: 600; color: var(--text-secondary, #526177);">Đang xử lý gửi thông báo đến người nhận...</span>
</div>

<script src="${pageContext.request.contextPath}/assets/js/notification.js" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
