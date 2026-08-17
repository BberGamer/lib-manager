<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="audit-logs" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="page-wrapper">
    <div class="container db-container">
        <!-- Section Header -->
        <div class="section-header db-section-header" style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 16px; margin-bottom: 24px;">
            <div>
                <h1 class="section-title">
                    <i class="fa-solid fa-shield-halved" style="color: var(--primary, #e67e22);"></i> Nhật ký kiểm toán hệ thống (Audit Logs)
                </h1>
                <p class="section-subtitle">
                    Theo dõi toàn bộ lịch sử can thiệp đặc biệt, miễn giảm phạt, override giới hạn và quản trị tài khoản
                </p>
            </div>
            <div style="background: white; padding: 10px 20px; border-radius: 10px; border: 1px solid var(--border-color, #e2e8f0); text-align: right; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
                <div style="font-size: 0.8rem; color: var(--text-muted, #718096); font-weight: 500;">Tổng số bản ghi</div>
                <div style="font-size: 1.5rem; font-weight: 700; color: var(--text-primary, #2d3748);">
                    <c:out value="${totalLogs}" />
                </div>
            </div>
        </div>

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
            <nav aria-label="Phân trang nhật ký" style="margin-top: 24px; display: flex; justify-content: center;">
                <ul class="pagination" style="display: flex; gap: 4px; list-style: none; padding: 0;">
                    <li class="page-item ${currentPageNum <= 1 ? 'disabled' : ''}">
                        <c:url var="prevUrl" value="/admin/audit-logs">
                            <c:param name="action" value="${selectedAction}" />
                            <c:param name="performedBy" value="${keywordPerformedBy}" />
                            <c:param name="fromDate" value="${selectedFromDate}" />
                            <c:param name="toDate" value="${selectedToDate}" />
                            <c:param name="page" value="${currentPageNum - 1}" />
                        </c:url>
                        <a class="page-link btn btn-secondary" href="${prevUrl}" style="padding: 8px 12px; border-radius: 6px; font-size: 0.85rem; ${currentPageNum <= 1 ? 'pointer-events: none; opacity: 0.5;' : ''}">
                            <i class="fa-solid fa-chevron-left"></i>
                        </a>
                    </li>

                    <c:forEach begin="1" end="${totalPages}" var="p">
                        <c:url var="pUrl" value="/admin/audit-logs">
                            <c:param name="action" value="${selectedAction}" />
                            <c:param name="performedBy" value="${keywordPerformedBy}" />
                            <c:param name="fromDate" value="${selectedFromDate}" />
                            <c:param name="toDate" value="${selectedToDate}" />
                            <c:param name="page" value="${p}" />
                        </c:url>
                        <li class="page-item ${p == currentPageNum ? 'active' : ''}">
                            <a class="page-link btn ${p == currentPageNum ? 'btn-primary' : 'btn-secondary'}" href="${pUrl}" style="padding: 8px 14px; border-radius: 6px; font-size: 0.85rem; font-weight: 600;">
                                <c:out value="${p}" />
                            </a>
                        </li>
                    </c:forEach>

                    <li class="page-item ${currentPageNum >= totalPages ? 'disabled' : ''}">
                        <c:url var="nextUrl" value="/admin/audit-logs">
                            <c:param name="action" value="${selectedAction}" />
                            <c:param name="performedBy" value="${keywordPerformedBy}" />
                            <c:param name="fromDate" value="${selectedFromDate}" />
                            <c:param name="toDate" value="${selectedToDate}" />
                            <c:param name="page" value="${currentPageNum + 1}" />
                        </c:url>
                        <a class="page-link btn btn-secondary" href="${nextUrl}" style="padding: 8px 12px; border-radius: 6px; font-size: 0.85rem; ${currentPageNum >= totalPages ? 'pointer-events: none; opacity: 0.5;' : ''}">
                            <i class="fa-solid fa-chevron-right"></i>
                        </a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
