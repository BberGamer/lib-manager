<%--
    Trang nhật ký kiểm toán do AuditLogServlet hiển thị;
    nhận logs, totalLogs, distinctActions và các giá trị bộ lọc từ request.
--%>
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
                                    <c:when test="${act eq 'CREATE_USER'}">Tạo tài khoản mới (CREATE_USER)</c:when>
                                    <c:when test="${act eq 'UPDATE_USER'}">Cập nhật tài khoản (UPDATE_USER)</c:when>
                                    <c:when test="${act eq 'WAIVE_FINE'}">Miễn giảm phạt (WAIVE_FINE)</c:when>
                                    <c:when test="${act eq 'FINE_PAID_CASH'}">Thu phạt tiền mặt (FINE_PAID_CASH)</c:when>
                                    <c:when test="${act eq 'FINE_PAID_ONLINE'}">Thanh toán phạt VNPay (FINE_PAID_ONLINE)</c:when>
                                    <c:when test="${act eq 'OVERRIDE_BORROW_LIMIT'}">Vượt hạn mức mượn (OVERRIDE)</c:when>
                                    <c:when test="${act eq 'APPLY_DAMAGE_FINE'}">Phạt hỏng sách (DAMAGE_FINE)</c:when>
                                    <c:when test="${act eq 'APPLY_LOST_FINE'}">Phạt mất sách (LOST_FINE)</c:when>
                                    <c:when test="${act eq 'BORROW_CONFIRM_PICKUP'}">Giao sách cho độc giả (PICKUP)</c:when>
                                    <c:when test="${act eq 'BORROW_CONFIRM_RETURN'}">Nhận trả sách (RETURN)</c:when>
                                    <c:when test="${act eq 'CONFIRM_RESERVATION'}">Xác nhận đặt trước (RESERVATION)</c:when>
                                    <c:when test="${act eq 'CANCEL_RESERVATION_BY_STAFF'}">Hủy đặt trước (CANCEL_RES)</c:when>
                                    <c:when test="${act eq 'FOUND_ITEM_CREATE'}">Tiếp nhận đồ để quên (FOUND_ITEM)</c:when>
                                    <c:when test="${act eq 'FOUND_ITEM_VERIFY_APPROVE'}">Duyệt nhận lại đồ (CLAIM_APPROVE)</c:when>
                                    <c:when test="${act eq 'FOUND_ITEM_VERIFY_REJECT'}">Từ chối nhận lại đồ (CLAIM_REJECT)</c:when>
                                    <c:when test="${act eq 'FOUND_ITEM_HANDOVER_COMPLETE'}">Bàn giao đồ hoàn tất (HANDOVER)</c:when>
                                    <c:when test="${act eq 'BOOK_CREATE'}">Thêm đầu sách mới (BOOK_CREATE)</c:when>
                                    <c:when test="${act eq 'BOOK_UPDATE'}">Cập nhật đầu sách (BOOK_UPDATE)</c:when>
                                    <c:when test="${act eq 'BOOK_DELETE'}">Xóa đầu sách (BOOK_DELETE)</c:when>
                                    <c:when test="${act eq 'BOOK_COPY_ADD'}">Thêm bản sao sách (COPY_ADD)</c:when>
                                    <c:when test="${act eq 'BOOK_COPY_UPDATE'}">Cập nhật bản sao sách (COPY_UPDATE)</c:when>
                                    <c:when test="${act eq 'BOOK_COPY_DELETE'}">Xóa bản sao sách (COPY_DELETE)</c:when>
                                    <c:when test="${act eq 'BOOK_BULK_IMPORT' or act eq 'IMPORT_BOOKS'}">Nhập sách từ file CSV (IMPORT)</c:when>
                                    <c:when test="${act eq 'BOOK_EXPORT'}">Xuất sách ra CSV (EXPORT)</c:when>
                                    <c:when test="${act eq 'SHELF_CREATE'}">Thêm kệ sách (SHELF_CREATE)</c:when>
                                    <c:when test="${act eq 'SHELF_UPDATE'}">Cập nhật kệ sách (SHELF_UPDATE)</c:when>
                                    <c:when test="${act eq 'SHELF_DELETE'}">Xóa kệ sách (SHELF_DELETE)</c:when>
                                    <c:when test="${act eq 'AUTHOR_CREATE'}">Thêm tác giả (AUTHOR_CREATE)</c:when>
                                    <c:when test="${act eq 'AUTHOR_UPDATE'}">Cập nhật tác giả (AUTHOR_UPDATE)</c:when>
                                    <c:when test="${act eq 'AUTHOR_DELETE'}">Xóa tác giả (AUTHOR_DELETE)</c:when>
                                    <c:when test="${act eq 'CATEGORY_CREATE'}">Thêm danh mục (CATEGORY_CREATE)</c:when>
                                    <c:when test="${act eq 'CATEGORY_UPDATE'}">Cập nhật danh mục (CATEGORY_UPDATE)</c:when>
                                    <c:when test="${act eq 'CATEGORY_DELETE'}">Xóa danh mục (CATEGORY_DELETE)</c:when>
                                    <c:when test="${act eq 'EVENT_CREATE'}">Thêm sự kiện (EVENT_CREATE)</c:when>
                                    <c:when test="${act eq 'EVENT_UPDATE'}">Cập nhật sự kiện (EVENT_UPDATE)</c:when>
                                    <c:when test="${act eq 'EVENT_DELETE'}">Xóa sự kiện (EVENT_DELETE)</c:when>
                                    <c:when test="${act eq 'POLICY_CREATE'}">Tạo nháp điều lệ (POLICY_CREATE)</c:when>
                                    <c:when test="${act eq 'POLICY_PUBLISH'}">Xuất bản điều lệ (POLICY_PUBLISH)</c:when>
                                    <c:when test="${act eq 'POLICY_ARCHIVE'}">Lưu trữ điều lệ (POLICY_ARCHIVE)</c:when>
                                    <c:when test="${act eq 'POLICY_REUSE'}">Sử dụng lại điều lệ (POLICY_REUSE)</c:when>
                                    <c:when test="${act eq 'POLICY_DELETE'}">Xóa nháp điều lệ (POLICY_DELETE)</c:when>
                                    <c:when test="${act eq 'POLICY_REVISE'}">Sửa đổi điều lệ (POLICY_REVISE)</c:when>
                                    <c:when test="${act eq 'NOTIFICATION_BROADCAST'}">Phát thông báo hệ thống (BROADCAST)</c:when>
                                    <c:when test="${act eq 'NOTIFICATION_SEND'}">Gửi thông báo độc giả (NOTIFY_SEND)</c:when>
                                    <c:when test="${act eq 'SEND_DUE_REMINDER'}">Nhắc nhở sắp đến hạn (DUE_REMINDER)</c:when>
                                    <c:when test="${act eq 'SEND_OVERDUE_WARNING'}">Cảnh báo sách quá hạn (OVERDUE)</c:when>
                                    <c:when test="${act eq 'SEND_FINE_REMINDER'}">Nhắc nộp phí phạt (FINE_REMINDER)</c:when>
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
                                                <c:when test="${log.action eq 'CREATE_USER'}">
                                                    <span style="background: #d1fae5; color: #047857; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-user-plus"></i> Tạo tài khoản
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'UPDATE_USER'}">
                                                    <span style="background: #ede9fe; color: #6d28d9; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-user-pen"></i> Cập nhật tài khoản
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'FINE_PAID_CASH'}">
                                                    <span style="background: #dcfce7; color: #166534; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-money-bill-wave"></i> Thu tiền mặt
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'FINE_PAID_ONLINE'}">
                                                    <span style="background: #cffafe; color: #0e7490; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-credit-card"></i> VNPay Online
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'BORROW_CONFIRM_PICKUP'}">
                                                    <span style="background: #dcfce7; color: #15803d; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-hand-holding"></i> Giao sách
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'BORROW_CONFIRM_RETURN'}">
                                                    <span style="background: #ecfccb; color: #4d7c0f; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-rotate-left"></i> Nhận trả sách
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'CANCEL_RESERVATION_BY_STAFF'}">
                                                    <span style="background: #f3e8ff; color: #6b21a8; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-ban"></i> Hủy đặt trước
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'FOUND_ITEM_CREATE'}">
                                                    <span style="background: #ffedd5; color: #c2410c; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-box-open"></i> Đồ để quên
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'FOUND_ITEM_VERIFY_APPROVE'}">
                                                    <span style="background: #ecfccb; color: #3f6212; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-circle-check"></i> Duyệt nhận đồ
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'FOUND_ITEM_VERIFY_REJECT'}">
                                                    <span style="background: #fee2e2; color: #b91c1c; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-circle-xmark"></i> Từ chối nhận đồ
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'FOUND_ITEM_HANDOVER_COMPLETE'}">
                                                    <span style="background: #dcfce7; color: #166534; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-clipboard-check"></i> Bàn giao đồ
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'BOOK_CREATE'}">
                                                    <span style="background: #dbeafe; color: #1d4ed8; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-book"></i> Thêm đầu sách
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'BOOK_UPDATE'}">
                                                    <span style="background: #e0e7ff; color: #3730a3; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-book-open-reader"></i> Sửa đầu sách
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'BOOK_DELETE'}">
                                                    <span style="background: #ede9fe; color: #5b21b6; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-trash-can"></i> Xóa đầu sách
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'BOOK_COPY_ADD'}">
                                                    <span style="background: #d1fae5; color: #065f46; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-barcode"></i> Thêm bản sao
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'BOOK_COPY_UPDATE'}">
                                                    <span style="background: #ecfdf5; color: #047857; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-pen-to-square"></i> Sửa bản sao
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'BOOK_COPY_DELETE'}">
                                                    <span style="background: #ffe4e6; color: #9f1239; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-square-minus"></i> Xóa bản sao
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'BOOK_BULK_IMPORT' or log.action eq 'IMPORT_BOOKS'}">
                                                    <span style="background: #dbeafe; color: #1e40af; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-file-import"></i> Nhập sách CSV
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'BOOK_EXPORT'}">
                                                    <span style="background: #e0f2fe; color: #0369a1; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-file-export"></i> Xuất sách CSV
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'SHELF_CREATE'}">
                                                    <span style="background: #fef3c7; color: #92400e; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-layer-group"></i> Thêm kệ sách
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'SHELF_UPDATE'}">
                                                    <span style="background: #fef9c3; color: #854d0e; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-cubes-stacked"></i> Sửa kệ sách
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'SHELF_DELETE'}">
                                                    <span style="background: #fef2f2; color: #78350f; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-box-archive"></i> Xóa kệ sách
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'AUTHOR_CREATE'}">
                                                    <span style="background: #e0e7ff; color: #3730a3; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-user-pen"></i> Thêm tác giả
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'AUTHOR_UPDATE'}">
                                                    <span style="background: #ede9fe; color: #4338ca; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-pen-nib"></i> Sửa tác giả
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'AUTHOR_DELETE'}">
                                                    <span style="background: #f1f5f9; color: #312e81; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-user-minus"></i> Xóa tác giả
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'CATEGORY_CREATE'}">
                                                    <span style="background: #ccfbf1; color: #115e59; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-tag"></i> Thêm danh mục
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'CATEGORY_UPDATE'}">
                                                    <span style="background: #e6fffa; color: #0f766e; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-tags"></i> Sửa danh mục
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'CATEGORY_DELETE'}">
                                                    <span style="background: #f0fdfa; color: #134e4a; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-tag"></i> Xóa danh mục
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'EVENT_CREATE'}">
                                                    <span style="background: #ffe4e6; color: #be123c; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-calendar-plus"></i> Thêm sự kiện
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'EVENT_UPDATE'}">
                                                    <span style="background: #fff1f2; color: #9f1239; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-calendar-days"></i> Sửa sự kiện
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'EVENT_DELETE'}">
                                                    <span style="background: #fee2e2; color: #881337; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-calendar-xmark"></i> Xóa sự kiện
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'POLICY_CREATE'}">
                                                    <span style="background: #cffafe; color: #155e75; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-file-circle-plus"></i> Nháp điều lệ
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'POLICY_PUBLISH'}">
                                                    <span style="background: #d1fae5; color: #065f46; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-file-circle-check"></i> Xuất bản điều lệ
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'POLICY_ARCHIVE'}">
                                                    <span style="background: #f5f5f4; color: #44403c; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-box-archive"></i> Lưu trữ điều lệ
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'POLICY_REUSE'}">
                                                    <span class="audit-action-badge audit-action-policy-reuse">
                                                        <i class="fa-solid fa-rotate-left"></i> Sử dụng lại điều lệ
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'POLICY_DELETE'}">
                                                    <span style="background: #fee2e2; color: #991b1b; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-file-circle-xmark"></i> Xóa nháp điều lệ
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'POLICY_REVISE'}">
                                                    <span style="background: #e0f2fe; color: #075985; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-file-pen"></i> Sửa đổi điều lệ
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'NOTIFICATION_BROADCAST'}">
                                                    <span style="background: #fce7f3; color: #9d174d; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-bullhorn"></i> Phát thông báo
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'NOTIFICATION_SEND'}">
                                                    <span style="background: #ffe4e6; color: #be123c; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-paper-plane"></i> Gửi thông báo
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'SEND_DUE_REMINDER'}">
                                                    <span style="background: #fef3c7; color: #b45309; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-clock"></i> Nhắc sắp đến hạn
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'SEND_OVERDUE_WARNING'}">
                                                    <span style="background: #fee2e2; color: #b91c1c; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-triangle-exclamation"></i> Cảnh báo quá hạn
                                                    </span>
                                                </c:when>
                                                <c:when test="${log.action eq 'SEND_FINE_REMINDER'}">
                                                    <span style="background: #ffe4e6; color: #881337; padding: 5px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                                                        <i class="fa-solid fa-money-bill-wave"></i> Nhắc nộp phạt
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
