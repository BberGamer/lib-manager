<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="audit-logs" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="page-wrapper audit-logs-page" style="margin: 0; padding: 0;">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-clipboard-list"></i> Kiểm toán
                    </div>
                    <h1 class="books-page-title">Nhật ký kiểm toán (Audit Log)</h1>
                    <p class="books-page-subtitle">
                        Theo dõi toàn bộ lịch sử can thiệp đặc biệt, cấu hình và quản trị tài khoản
                    </p>
                </div>
                <div class="books-page-stats" aria-label="Tổng số bản ghi kiểm toán">
                    <div class="bps-item">
                        <span class="bps-num"><c:out value="${totalLogs}" /></span>
                        <span class="bps-lbl">Nhật ký</span>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="container db-container" style="padding-top: 28px; padding-bottom: 48px;">

        <!-- Alert messages -->
        <c:if test="${not empty error}">
            <div class="alert alert-error db-error-alert" style="margin-bottom: 20px;">
                <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${error}" />
            </div>
        </c:if>

        <!-- Filter Card -->
        <div class="db-panel" style="margin-bottom: 24px; padding: 20px; background: white; border-radius: 12px; border: 1px solid var(--border-color, #e2e8f0); box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
            <form action="${pageContext.request.contextPath}/admin/audit-logs" method="get" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; align-items: end;">
                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-primary, #2d3748);">
                        <i class="fa-solid fa-tag"></i> Loại hành động (Action)
                    </label>
                    <select name="action" class="form-control" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.9rem;">
                        <option value="">-- Tất cả hành động --</option>
                        <c:forEach var="act" items="${distinctActions}">
                            <option value="${act}" ${selectedAction eq act ? 'selected' : ''}>
                                <c:choose>
                                    <c:when test="${act eq 'LOCK_ACCOUNT'}">Khóa tài khoản (LOCK_ACCOUNT)</c:when>
                                    <c:when test="${act eq 'UNLOCK_ACCOUNT'}">Mở khóa tài khoản (UNLOCK_ACCOUNT)</c:when>
                                    <c:when test="${act eq 'DELETE_USER'}">Xóa tài khoản (DELETE_USER)</c:when>
                                    <c:when test="${act eq 'WAIVE_FINE'}">Miễn giảm phạt (WAIVE_FINE)</c:when>
                                    <c:when test="${act eq 'OVERRIDE_BORROW_LIMIT'}">Vượt hạn mức mượn (OVERRIDE)</c:when>
                                    <c:when test="${act eq 'APPLY_DAMAGE_FINE'}">Phạt hỏng sách (DAMAGE_FINE)</c:when>
                                    <c:when test="${act eq 'APPLY_LOST_FINE'}">Phạt mất sách (LOST_FINE)</c:when>
                                    <c:when test="${act eq 'CONFIRM_RESERVATION'}">Xác nhận đặt trước (RESERVATION)</c:when>
                                    <c:when test="${act eq 'AUTO_BATCH_REMINDER'}">Quét tự động hàng loạt (BATCH_REMINDER)</c:when>
                                    <c:when test="${act eq 'UPDATE_AUTOMATION_SETTING'}">Cập nhật cấu hình tự động (SETTING)</c:when>
                                    <c:otherwise><c:out value="${act}" /></c:otherwise>
                                </c:choose>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-primary, #2d3748);">
                        <i class="fa-solid fa-user"></i> Người thực hiện
                    </label>
                    <input type="text" name="performedBy" value="${keywordPerformedBy}" placeholder="Nhập username..."
                           class="form-control" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.9rem;">
                </div>

                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-primary, #2d3748);">
                        <i class="fa-regular fa-calendar"></i> Từ ngày
                    </label>
                    <input type="date" name="fromDate" value="${selectedFromDate}"
                           class="form-control" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.9rem;">
                </div>

                <div>
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-primary, #2d3748);">
                        <i class="fa-regular fa-calendar-check"></i> Đến ngày
                    </label>
                    <input type="date" name="toDate" value="${selectedToDate}"
                           class="form-control" style="width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.9rem;">
                </div>

                <div style="display: flex; gap: 8px;">
                    <button type="submit" class="btn btn-primary" style="flex: 1; padding: 9px 16px; border-radius: 8px; font-weight: 600; display: flex; align-items: center; justify-content: center; gap: 6px;">
                        <i class="fa-solid fa-filter"></i> Lọc
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/audit-logs" class="btn btn-secondary" style="padding: 9px 16px; border-radius: 8px; font-weight: 600; display: flex; align-items: center; justify-content: center;" title="Xóa bộ lọc">
                        <i class="fa-solid fa-rotate-left"></i>
                    </a>
                </div>
            </form>
        </div>

        <!-- Table Card -->
        <div class="db-panel" style="background: white; border-radius: 12px; border: 1px solid var(--border-color, #e2e8f0); box-shadow: 0 1px 3px rgba(0,0,0,0.05); overflow: hidden; padding: 0;">
            <div style="overflow-x: auto;">
                <table class="db-table" style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr class="db-table-header" style="background: #f8fafc; border-bottom: 1px solid #e2e8f0;">
                            <th style="padding: 14px 16px; text-align: left; font-size: 0.85rem; font-weight: 700; color: #475569; width: 60px;">ID</th>
                            <th style="padding: 14px 16px; text-align: left; font-size: 0.85rem; font-weight: 700; color: #475569; width: 170px;">Thời gian</th>
                            <th style="padding: 14px 16px; text-align: left; font-size: 0.85rem; font-weight: 700; color: #475569; width: 180px;">Hành động</th>
                            <th style="padding: 14px 16px; text-align: left; font-size: 0.85rem; font-weight: 700; color: #475569; width: 150px;">Người thực hiện</th>
                            <th style="padding: 14px 16px; text-align: left; font-size: 0.85rem; font-weight: 700; color: #475569; width: 180px;">Đối tượng bị ảnh hưởng</th>
                            <th style="padding: 14px 16px; text-align: left; font-size: 0.85rem; font-weight: 700; color: #475569;">Nội dung chi tiết</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty logs}">
                                <tr>
                                    <td colspan="6" style="text-align: center; padding: 48px 16px; color: var(--text-muted, #94a3b8);">
                                        <i class="fa-solid fa-folder-open" style="font-size: 2.5rem; margin-bottom: 12px; display: block; opacity: 0.5;"></i>
                                        <span style="font-size: 1rem; font-weight: 500;">Không tìm thấy bản ghi nhật ký kiểm toán nào phù hợp</span>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="log" items="${logs}">
                                    <tr class="db-table-row" style="border-bottom: 1px solid #f1f5f9; transition: background 0.15s ease;">
                                        <td style="padding: 12px 16px; font-weight: 600; color: #64748b;">
                                            #<c:out value="${log.id}" />
                                        </td>
                                        <td style="padding: 12px 16px; font-size: 0.85rem; color: #64748b; white-space: nowrap;">
                                            <c:out value="${log.created_at}" />
                                        </td>
                                        <td style="padding: 12px 16px;">
                                            <c:choose>
                                                <c:when test="${log.action eq 'UNLOCK_ACCOUNT'}">
                                                    <span style="background: #dcfce7; color: #15803d; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-lock-open"></i> Mở khóa tài khoản
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'LOCK_ACCOUNT'}">
                                                    <span style="background: #fee2e2; color: #dc2626; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-user-lock"></i> Khóa tài khoản
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'DELETE_USER'}">
                                                    <span style="background: #f1f5f9; color: #475569; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-user-xmark"></i> Xóa tài khoản
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'WAIVE_FINE'}">
                                                    <span style="background: #e0f2fe; color: #0284c7; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-hand-holding-dollar"></i> Miễn giảm phạt
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'OVERRIDE_BORROW_LIMIT'}">
                                                    <span style="background: #fef3c7; color: #d97706; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-triangle-exclamation"></i> Vượt hạn mức mượn
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'APPLY_DAMAGE_FINE'}">
                                                    <span style="background: #ffedd5; color: #c2410c; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-book-medical"></i> Phạt hỏng sách
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'APPLY_LOST_FINE'}">
                                                    <span style="background: #fee2e2; color: #991b1b; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-circle-xmark"></i> Phạt mất sách
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'CONFIRM_RESERVATION'}">
                                                    <span style="background: #f3e8ff; color: #7e22ce; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-bookmark"></i> Xác nhận đặt trước
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'AUTO_BATCH_REMINDER'}">
                                                    <span style="background: #e0f2fe; color: #0369a1; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-robot"></i> Quét tự động hàng loạt
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'UPDATE_AUTOMATION_SETTING'}">
                                                    <span style="background: #fef9c3; color: #854d0e; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-sliders"></i> Cập nhật cấu hình tự động
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="background: #f1f5f9; color: #475569; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-shield"></i> <c:out value="${log.action}" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="padding: 12px 16px;">
                                            <span style="font-weight: 600; color: var(--text-primary, #1e293b);">
                                                @<c:out value="${log.performed_by}" />
                                            </span>
                                        </td>
                                        <td style="padding: 12px 16px;">
                                            <c:choose>
                                                <c:when test="${not empty log.target_fullname or not empty log.target_username}">
                                                    <div style="font-weight: 600; color: #334155; font-size: 0.88rem;">
                                                        <c:out value="${not empty log.target_fullname ? log.target_fullname : log.target_username}" />
                                                    </div>
                                                    <div style="font-size: 0.78rem; color: #94a3b8;">
                                                        ID: #<c:out value="${log.target_user_id}" /> (@<c:out value="${log.target_username}" />)
                                                    </div>
                                                </c:when>
                                                <c:when test="${log.target_user_id > 0}">
                                                    <span style="font-size: 0.85rem; color: #64748b;">User ID: #<c:out value="${log.target_user_id}" /></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: #cbd5e1; font-size: 0.85rem;">—</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="padding: 12px 16px; font-size: 0.88rem; color: #334155; line-height: 1.4;">
                                            <c:choose>
                                                <c:when test="${log.detail eq 'unlocked by admin'}">
                                                    Đã mở khóa tài khoản hoạt động trở lại
                                                </c:when>
                                                <c:when test="${fn:startsWith(log.detail, 'reason=')}">
                                                    Đã khóa tài khoản | Lý do: <c:out value="${fn:substringAfter(log.detail, 'reason=')}" />
                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${log.detail}" />
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Pagination -->
        <c:if test="${totalPages > 1}">
            <c:set var="cp" value="${currentPageNum}" />
            <c:set var="tp" value="${totalPages}" />
            <c:set var="winStart" value="${cp - 2 > 2 ? cp - 2 : 2}" />
            <c:set var="winEnd" value="${cp + 2 < tp - 1 ? cp + 2 : tp - 1}" />
            <nav aria-label="Phân trang nhật ký" style="margin-top: 24px; display: flex; justify-content: center;">
                <ul class="pagination" style="display: flex; gap: 4px; list-style: none; padding: 0; flex-wrap: wrap;">

                    <%-- Prev --%>
                    <li class="page-item ${cp <= 1 ? 'disabled' : ''}">
                        <c:choose>
                            <c:when test="${cp > 1}">
                                <c:url var="auditPrevUrl" value="/admin/audit-logs">
                                    <c:param name="action" value="${selectedAction}" />
                                    <c:param name="performedBy" value="${keywordPerformedBy}" />
                                    <c:param name="fromDate" value="${selectedFromDate}" />
                                    <c:param name="toDate" value="${selectedToDate}" />
                                    <c:param name="page" value="${cp - 1}" />
                                </c:url>
                                <a class="page-link btn btn-secondary" href="${auditPrevUrl}" style="padding: 8px 12px; border-radius: 6px; font-size: 0.85rem;">
                                    <i class="fa-solid fa-chevron-left fa-xs"></i>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <span class="page-link btn btn-secondary" style="padding: 8px 12px; border-radius: 6px; font-size: 0.85rem; pointer-events: none; opacity: 0.45; cursor: not-allowed;">
                                    <i class="fa-solid fa-chevron-left fa-xs"></i>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </li>

                    <%-- Page 1 --%>
                    <li class="page-item ${cp == 1 ? 'active' : ''}">
                        <c:url var="auditP1" value="/admin/audit-logs">
                            <c:param name="action" value="${selectedAction}" />
                            <c:param name="performedBy" value="${keywordPerformedBy}" />
                            <c:param name="fromDate" value="${selectedFromDate}" />
                            <c:param name="toDate" value="${selectedToDate}" />
                            <c:param name="page" value="1" />
                        </c:url>
                        <a class="page-link btn ${cp == 1 ? 'btn-primary' : 'btn-secondary'}" href="${auditP1}" style="padding: 8px 14px; border-radius: 6px; font-size: 0.85rem; font-weight: 600;">1</a>
                    </li>

                    <%-- Left ellipsis --%>
                    <c:if test="${winStart > 2}">
                        <li class="page-item disabled"><span class="page-link btn btn-secondary" style="padding: 8px 12px; border-radius: 6px; font-size: 0.85rem;">…</span></li>
                    </c:if>

                    <%-- Window pages --%>
                    <c:if test="${winStart <= winEnd}">
                        <c:forEach begin="${winStart}" end="${winEnd}" var="p">
                            <li class="page-item ${p == cp ? 'active' : ''}">
                                <c:url var="auditPUrl" value="/admin/audit-logs">
                                    <c:param name="action" value="${selectedAction}" />
                                    <c:param name="performedBy" value="${keywordPerformedBy}" />
                                    <c:param name="fromDate" value="${selectedFromDate}" />
                                    <c:param name="toDate" value="${selectedToDate}" />
                                    <c:param name="page" value="${p}" />
                                </c:url>
                                <a class="page-link btn ${p == cp ? 'btn-primary' : 'btn-secondary'}" href="${auditPUrl}" style="padding: 8px 14px; border-radius: 6px; font-size: 0.85rem; font-weight: 600;"><c:out value="${p}" /></a>
                            </li>
                        </c:forEach>
                    </c:if>

                    <%-- Right ellipsis --%>
                    <c:if test="${winEnd < tp - 1}">
                        <li class="page-item disabled"><span class="page-link btn btn-secondary" style="padding: 8px 12px; border-radius: 6px; font-size: 0.85rem;">…</span></li>
                    </c:if>

                    <%-- Last page --%>
                    <c:if test="${tp > 1}">
                        <li class="page-item ${cp == tp ? 'active' : ''}">
                            <c:url var="auditPLast" value="/admin/audit-logs">
                                <c:param name="action" value="${selectedAction}" />
                                <c:param name="performedBy" value="${keywordPerformedBy}" />
                                <c:param name="fromDate" value="${selectedFromDate}" />
                                <c:param name="toDate" value="${selectedToDate}" />
                                <c:param name="page" value="${tp}" />
                            </c:url>
                            <a class="page-link btn ${cp == tp ? 'btn-primary' : 'btn-secondary'}" href="${auditPLast}" style="padding: 8px 14px; border-radius: 6px; font-size: 0.85rem; font-weight: 600;"><c:out value="${tp}" /></a>
                        </li>
                    </c:if>

                    <%-- Next --%>
                    <li class="page-item ${cp >= tp ? 'disabled' : ''}">
                        <c:choose>
                            <c:when test="${cp < tp}">
                                <c:url var="auditNextUrl" value="/admin/audit-logs">
                                    <c:param name="action" value="${selectedAction}" />
                                    <c:param name="performedBy" value="${keywordPerformedBy}" />
                                    <c:param name="fromDate" value="${selectedFromDate}" />
                                    <c:param name="toDate" value="${selectedToDate}" />
                                    <c:param name="page" value="${cp + 1}" />
                                </c:url>
                                <a class="page-link btn btn-secondary" href="${auditNextUrl}" style="padding: 8px 12px; border-radius: 6px; font-size: 0.85rem;">
                                    <i class="fa-solid fa-chevron-right fa-xs"></i>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <span class="page-link btn btn-secondary" style="padding: 8px 12px; border-radius: 6px; font-size: 0.85rem; pointer-events: none; opacity: 0.45; cursor: not-allowed;">
                                    <i class="fa-solid fa-chevron-right fa-xs"></i>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </li>

                </ul>
            </nav>
        </c:if>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
