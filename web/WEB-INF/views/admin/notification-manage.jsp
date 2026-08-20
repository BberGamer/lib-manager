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

<style>
/* Modern Automation Control Styles */
.automation-banner-card {
    background: #ffffff !important;
    border: 1px solid #cbd5e1 !important;
    border-radius: 14px !important;
    padding: 22px 26px !important;
    margin-bottom: 24px !important;
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.04) !important;
}
.automation-top-row {
    display: flex !important;
    justify-content: space-between !important;
    align-items: center !important;
    flex-wrap: wrap !important;
    gap: 16px !important;
    padding-bottom: 18px !important;
    border-bottom: 1px solid #f1f5f9 !important;
}
.automation-title-group h4 {
    margin: 0 !important;
    font-size: 1.1rem !important;
    font-weight: 700 !important;
    color: #1e293b !important;
    display: flex !important;
    align-items: center !important;
    gap: 10px !important;
}
.automation-title-group p {
    margin: 4px 0 0 0 !important;
    font-size: 0.85rem !important;
    color: #64748b !important;
}
.automation-grid {
    display: grid !important;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)) !important;
    gap: 16px !important;
    margin-top: 18px !important;
}
.toggle-control-card {
    background: #f8fafc !important;
    border: 1px solid #e2e8f0 !important;
    border-radius: 12px !important;
    padding: 16px 20px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: space-between !important;
    gap: 14px !important;
    box-sizing: border-box !important;
    transition: all 0.2s ease !important;
}
.toggle-control-card:hover {
    background: #ffffff !important;
    border-color: #cbd5e1 !important;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04) !important;
}
.toggle-info {
    display: flex !important;
    align-items: center !important;
    gap: 14px !important;
}
.toggle-icon-wrap {
    width: 42px !important;
    height: 42px !important;
    border-radius: 10px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    font-size: 1.2rem !important;
    flex-shrink: 0 !important;
}
.toggle-icon-wrap.icon-blue {
    background: #eff6ff !important;
    color: #2563eb !important;
}
.toggle-icon-wrap.icon-emerald {
    background: #ecfdf5 !important;
    color: #059669 !important;
}
.toggle-title {
    font-size: 0.95rem !important;
    font-weight: 700 !important;
    color: #1e293b !important;
    line-height: 1.2 !important;
}
.toggle-desc {
    font-size: 0.8rem !important;
    color: #64748b !important;
    margin-top: 3px !important;
}
.toggle-right-group {
    display: flex !important;
    align-items: center !important;
    gap: 12px !important;
}
.toggle-state-text {
    font-size: 0.82rem !important;
    font-weight: 700 !important;
    color: #94a3b8 !important;
    min-width: 32px !important;
    text-align: right !important;
    transition: color 0.2s ease !important;
}
.toggle-state-text.active {
    color: #10b981 !important;
}
/* iOS Toggle Switch */
.ios-switch {
    position: relative !important;
    display: inline-block !important;
    width: 48px !important;
    height: 26px !important;
    flex-shrink: 0 !important;
    cursor: pointer !important;
    margin: 0 !important;
}
.ios-switch input {
    opacity: 0 !important;
    width: 0 !important;
    height: 0 !important;
    margin: 0 !important;
    position: absolute !important;
}
.ios-slider {
    position: absolute !important;
    top: 0 !important;
    left: 0 !important;
    right: 0 !important;
    bottom: 0 !important;
    background-color: #cbd5e1 !important;
    border-radius: 34px !important;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important;
}
.ios-slider:before {
    position: absolute !important;
    content: "" !important;
    height: 20px !important;
    width: 20px !important;
    left: 3px !important;
    bottom: 3px !important;
    background-color: #ffffff !important;
    border-radius: 50% !important;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2) !important;
}
.ios-switch input:checked + .ios-slider {
    background-color: #10b981 !important;
}
.ios-switch input:focus + .ios-slider {
    box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2) !important;
}
.ios-switch input:checked + .ios-slider:before {
    transform: translateX(22px) !important;
}
</style>

<main class="page-wrapper notification-manage-page" style="margin: 0; padding: 0;">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-bullhorn"></i> Thông báo
                    </div>
                    <h1 class="books-page-title">Quản lý &amp; Gửi thông báo</h1>
                    <p class="books-page-subtitle">
                        Soạn thông báo tùy chỉnh đến độc giả hoặc thiết lập tự động gửi nhắc nhở
                    </p>
                </div>
                <div class="books-page-stats" aria-label="Tổng số thông báo đã gửi">
                    <div class="bps-item">
                        <span class="bps-num"><c:out value="${totalSent != null ? totalSent : totalRecords}" /></span>
                        <span class="bps-lbl">Đã gửi</span>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="container notification-manage-container" style="padding-top: 28px;">

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
                                                <c:when test="${n.type eq 'RESERVATION'}">
                                                    <span class="notif-badge notif-badge-system">
                                                        <i class="fa-solid fa-bookmark"></i> Đặt trước
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
                <c:set var="cp" value="${currentPageNum}" />
                <c:set var="tp" value="${totalPages}" />
                <c:set var="winStart" value="${cp - 2 > 2 ? cp - 2 : 2}" />
                <c:set var="winEnd" value="${cp + 2 < tp - 1 ? cp + 2 : tp - 1}" />
                <nav aria-label="Phân trang thông báo" style="margin-top: 24px;">
                    <ul class="pagination" style="flex-wrap: wrap;">
                        <li class="page-item ${cp <= 1 ? 'disabled' : ''}">
                            <c:choose>
                                <c:when test="${cp > 1}">
                                    <c:url var="notifPrev" value="${rolePath}/notification/manage">
                                        <c:param name="tab" value="compose" />
                                        <c:param name="filterType" value="${selectedFilterType}" />
                                        <c:param name="page" value="${cp - 1}" />
                                    </c:url>
                                    <a class="page-link" href="${notifPrev}"><i class="fa-solid fa-chevron-left fa-xs"></i></a>
                                </c:when>
                                <c:otherwise>
                                    <span class="page-link"><i class="fa-solid fa-chevron-left fa-xs"></i></span>
                                </c:otherwise>
                            </c:choose>
                        </li>

                        <li class="page-item ${cp == 1 ? 'active' : ''}">
                            <c:url var="notifP1" value="${rolePath}/notification/manage">
                                <c:param name="tab" value="compose" />
                                <c:param name="filterType" value="${selectedFilterType}" />
                                <c:param name="page" value="1" />
                            </c:url>
                            <a class="page-link" href="${notifP1}">1</a>
                        </li>

                        <c:if test="${winStart > 2}">
                            <li class="page-item disabled"><span class="page-link">…</span></li>
                        </c:if>

                        <c:if test="${winStart <= winEnd}">
                            <c:forEach begin="${winStart}" end="${winEnd}" var="p">
                                <li class="page-item ${p == cp ? 'active' : ''}">
                                    <c:url var="notifPUrl" value="${rolePath}/notification/manage">
                                        <c:param name="tab" value="compose" />
                                        <c:param name="filterType" value="${selectedFilterType}" />
                                        <c:param name="page" value="${p}" />
                                    </c:url>
                                    <a class="page-link" href="${notifPUrl}"><c:out value="${p}" /></a>
                                </li>
                            </c:forEach>
                        </c:if>

                        <c:if test="${winEnd < tp - 1}">
                            <li class="page-item disabled"><span class="page-link">…</span></li>
                        </c:if>

                        <c:if test="${tp > 1}">
                            <li class="page-item ${cp == tp ? 'active' : ''}">
                                <c:url var="notifPLast" value="${rolePath}/notification/manage">
                                    <c:param name="tab" value="compose" />
                                    <c:param name="filterType" value="${selectedFilterType}" />
                                    <c:param name="page" value="${tp}" />
                                </c:url>
                                <a class="page-link" href="${notifPLast}"><c:out value="${tp}" /></a>
                            </li>
                        </c:if>

                        <li class="page-item ${cp >= tp ? 'disabled' : ''}">
                            <c:choose>
                                <c:when test="${cp < tp}">
                                    <c:url var="notifNext" value="${rolePath}/notification/manage">
                                        <c:param name="tab" value="compose" />
                                        <c:param name="filterType" value="${selectedFilterType}" />
                                        <c:param name="page" value="${cp + 1}" />
                                    </c:url>
                                    <a class="page-link" href="${notifNext}"><i class="fa-solid fa-chevron-right fa-xs"></i></a>
                                </c:when>
                                <c:otherwise>
                                    <span class="page-link"><i class="fa-solid fa-chevron-right fa-xs"></i></span>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </ul>
                </nav>
            </c:if>
        </div>

        <!-- TAB 2: AUTOMATIC REMINDERS -->
        <div class="tab-content" id="reminders-tab" style="${activeTab eq 'reminders' ? 'display: block;' : 'display: none;'}">
            
            <!-- Automated Scheduler Control Banner -->
            <div class="automation-banner-card">
                <div class="automation-top-row">
                    <div class="automation-title-group">
                        <h4>
                            <i class="fa-solid fa-robot" style="color: #3b82f6;"></i> Hệ thống quét tự động &amp; Lập lịch gửi thông báo (Batch Job)
                        </h4>
                        <p>
                            Tự động đồng bộ tiền phạt và quét gửi thông báo nhắc hạn / quá hạn mượn sách lúc <b>07:00 sáng</b> mỗi ngày.
                        </p>
                    </div>
                    <div>
                        <form action="${sendUrl}" method="post" onsubmit="showNotificationLoading(this)" style="margin: 0;">
                            <input type="hidden" name="action" value="run-auto-job">
                            <input type="hidden" name="sub" value="${empty activeSubTab ? 'due' : activeSubTab}">
                            <button type="submit" class="btn btn-primary" style="padding: 10px 20px; border-radius: 8px; font-weight: 700; display: inline-flex; align-items: center; gap: 8px; box-shadow: 0 4px 10px rgba(230,126,34,0.25);">
                                <i class="fa-solid fa-bolt"></i> Chạy quét &amp; Gửi hàng loạt ngay
                            </button>
                        </form>
                    </div>
                </div>

                <!-- Toggle Controls Form -->
                <form id="toggleAutomationForm" action="${sendUrl}" method="post" style="margin: 0;">
                    <input type="hidden" name="action" value="toggle-automation">
                    <input type="hidden" name="sub" value="${empty activeSubTab ? 'due' : activeSubTab}">
                    <input type="hidden" name="enableJob" id="enableJobInput" value="${autoJobEnabled}">
                    <input type="hidden" name="enableEmail" id="enableEmailInput" value="${autoEmailEnabled}">

                    <div class="automation-grid">
                        <!-- Setting 1: Cron Job -->
                        <div class="toggle-control-card">
                            <div class="toggle-info">
                                <div class="toggle-icon-wrap icon-blue">
                                    <i class="fa-solid fa-clock-rotate-left"></i>
                                </div>
                                <div>
                                    <div class="toggle-title">Lập lịch quét định kỳ</div>
                                    <div class="toggle-desc">Tự động kích hoạt lúc 07:00 sáng</div>
                                </div>
                            </div>
                            <div class="toggle-right-group">
                                <span class="toggle-state-text ${autoJobEnabled ? 'active' : ''}">
                                    ${autoJobEnabled ? 'BẬT' : 'TẮT'}
                                </span>
                                <label class="ios-switch">
                                    <input type="checkbox" ${autoJobEnabled ? 'checked' : ''}
                                           onchange="document.getElementById('enableJobInput').value = this.checked; document.getElementById('toggleAutomationForm').submit();">
                                    <span class="ios-slider"></span>
                                </label>
                            </div>
                        </div>

                        <!-- Setting 2: Email SMTP -->
                        <div class="toggle-control-card">
                            <div class="toggle-info">
                                <div class="toggle-icon-wrap icon-emerald">
                                    <i class="fa-solid fa-envelope-circle-check"></i>
                                </div>
                                <div>
                                    <div class="toggle-title">Tự động gửi Email (SMTP)</div>
                                    <div class="toggle-desc">Bắn email thông báo tới hòm thư độc giả</div>
                                </div>
                            </div>
                            <div class="toggle-right-group">
                                <span class="toggle-state-text ${autoEmailEnabled ? 'active' : ''}">
                                    ${autoEmailEnabled ? 'BẬT' : 'TẮT'}
                                </span>
                                <label class="ios-switch">
                                    <input type="checkbox" ${autoEmailEnabled ? 'checked' : ''}
                                           onchange="document.getElementById('enableEmailInput').value = this.checked; document.getElementById('toggleAutomationForm').submit();">
                                    <span class="ios-slider"></span>
                                </label>
                            </div>
                        </div>
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
