<%--
    Trang Quản lý các Khoản phạt độc giả dành cho Quản trị viên / Thủ thư.
    Nhận các thuộc tính: fineList, totalPages, currentPageNum, keyword, selectedStatus.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="fine" scope="request" />
<c:set var="pageTitle" value="Quản lý Phạt độc giả – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/fine-list.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="page-wrapper">
    <div class="container fine-management-container">
        <div class="fine-page-header">
            <h1 class="section-title">
                <i class="fa-solid fa-circle-dollar-to-slot"></i> Quản lý các Khoản phạt độc giả
            </h1>
            <p class="section-subtitle">
                Thu tiền phạt trả muộn, hư hại sách và quản lý các phương thức thanh toán
            </p>
        </div>

        <!-- Alert notifications -->
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

        <!-- Filter bar -->
        <section class="fine-filter-card">
            <form action="${pageContext.request.contextPath}/admin/fine/list" method="get" class="fine-filter-form">
                <div style="flex: 1; min-width: 250px;">
                    <label>Tìm kiếm</label>
                    <input type="text" name="keyword" value="${fn:escapeXml(keyword)}"
                           placeholder="Tên độc giả, tên sách, lý do phạt...">
                </div>
                <div style="width: 220px;">
                    <label>Trạng thái thanh toán</label>
                    <select name="status">
                        <option value="">Tất cả trạng thái</option>
                        <option value="UNPAID" ${selectedStatus eq 'UNPAID' ? 'selected' : ''}>Chưa thanh toán (UNPAID)</option>
                        <option value="PAID" ${selectedStatus eq 'PAID' ? 'selected' : ''}>Đã thanh toán (PAID)</option>
                        <option value="WAIVED" ${selectedStatus eq 'WAIVED' ? 'selected' : ''}>Được miễn giảm (WAIVED)</option>
                    </select>
                </div>
                <div>
                    <button type="submit" class="btn btn-primary" style="padding: 10px 24px; border-radius: 8px;">
                        <i class="fa-solid fa-magnifying-glass"></i> Lọc kết quả
                    </button>
                </div>
            </form>
        </section>

        <!-- Table list -->
        <section class="fine-table-card">
            <div class="fine-table-scroll">
                <table class="fine-table">
                    <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Độc giả</th>
                            <th>Thông tin sách</th>
                            <th>Số tiền phạt</th>
                            <th>Lý do / Ngày tạo</th>
                            <th>Trạng thái</th>
                            <th style="text-align: right;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty fineList}">
                                <tr>
                                    <td colspan="7" class="fine-empty-state">
                                        <i class="fa-solid fa-credit-card"></i>
                                        <span>Không tìm thấy khoản phạt tiền nào</span>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="f" items="${fineList}">
                                    <tr>
                                        <td style="font-weight: 600;">#<c:out value="${f.id}" /></td>
                                        <td>
                                            <div style="font-weight: 600; color: var(--text-primary);">
                                                <c:out value="${not empty f.user ? f.user.fullName : '—'}" />
                                            </div>
                                            <div style="font-size: 0.8rem; color: var(--text-muted, #95a5a6);">
                                                @<c:out value="${not empty f.user ? f.user.username : ''}" />
                                                <c:if test="${not empty f.user.phone}">
                                                    | <c:out value="${f.user.phone}" />
                                                </c:if>
                                            </div>
                                        </td>
                                        <td>
                                            <div style="font-weight: 600; color: var(--text-primary);">
                                                <c:out value="${not empty f.borrowRecord and not empty f.borrowRecord.book ? f.borrowRecord.book.title : '—'}" />
                                            </div>
                                            <div style="font-size: 0.8rem; color: var(--text-muted, #95a5a6);">
                                                Lượt mượn: #<c:out value="${f.borrowRecordId}" />
                                            </div>
                                        </td>
                                        <td class="fine-amount">
                                            <fmt:formatNumber value="${f.amount}" type="currency" currencySymbol="₫" maxFractionDigits="0" />
                                        </td>
                                        <td>
                                            <div style="font-weight: 500;"><c:out value="${f.reason}" /></div>
                                            <div style="font-size: 0.8rem; color: var(--text-muted, #95a5a6);">
                                                Ngày tạo: <c:out value="${f.createdAt}" />
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${f.status eq 'UNPAID'}">
                                                    <span class="fine-status fine-status-unpaid">Chưa thanh toán</span>
                                                </c:when>
                                                <c:when test="${f.status eq 'PAID'}">
                                                    <span class="fine-status fine-status-paid">Đã thanh toán</span>
                                                    <div style="font-size: 0.75rem; color: var(--text-muted, #95a5a6); margin-top: 4px; white-space: nowrap;">
                                                        Cách: <c:out value="${f.paymentMethod}" /> | Ngày: <c:out value="${f.paidDate}" />
                                                    </div>
                                                </c:when>
                                                <c:when test="${f.status eq 'WAIVED'}">
                                                    <span class="fine-status fine-status-waived">Được miễn giảm</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="fine-status"><c:out value="${f.status}" /></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: right;">
                                            <c:choose>
                                                <c:when test="${f.status eq 'UNPAID'}">
                                                    <div style="display: flex; justify-content: flex-end; gap: 8px;">
                                                        <button type="button" class="btn btn-sm btn-success"
                                                                onclick="openPaymentModal(${f.id}, '${fn:escapeXml(not empty f.user ? f.user.fullName : '')}', ${f.amount})"
                                                                style="font-size: 0.8rem; border-radius: 6px;">
                                                            <i class="fa-solid fa-cash-register"></i> Đóng phạt
                                                        </button>
                                                        <form action="${pageContext.request.contextPath}/admin/fine/update-status"
                                                              method="post" style="display: inline;"
                                                              onsubmit="return confirm('Bạn có chắc chắn muốn miễn giảm khoản tiền phạt này không?')">
                                                            <input type="hidden" name="id" value="${f.id}">
                                                            <input type="hidden" name="status" value="WAIVED">
                                                            <button type="submit" class="btn btn-sm btn-outline-secondary"
                                                                    style="font-size: 0.8rem; border-radius: 6px; border: 1px solid #7f8c8d; color: #7f8c8d; background: transparent;">
                                                                <i class="fa-solid fa-hand-holding-heart"></i> Miễn giảm
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: var(--text-muted, #95a5a6); font-size: 0.85rem;">Hoàn tất</span>
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
        </section>

        <!-- Pagination -->
        <c:if test="${totalPages > 1}">
            <nav aria-label="Phân trang phạt" style="margin-top: 30px;">
                <ul class="pagination">
                    <li class="page-item ${currentPageNum <= 1 ? 'disabled' : ''}">
                        <c:url var="prevUrl" value="/admin/fine/list">
                            <c:param name="status" value="${selectedStatus}" />
                            <c:param name="keyword" value="${keyword}" />
                            <c:param name="page" value="${currentPageNum - 1}" />
                        </c:url>
                        <a class="page-link" href="${prevUrl}">
                            <i class="fa-solid fa-chevron-left fa-xs"></i>
                        </a>
                    </li>
                    <c:forEach begin="1" end="${totalPages}" var="pg">
                        <c:url var="pageUrl" value="/admin/fine/list">
                            <c:param name="status" value="${selectedStatus}" />
                            <c:param name="keyword" value="${keyword}" />
                            <c:param name="page" value="${pg}" />
                        </c:url>
                        <li class="page-item ${pg == currentPageNum ? 'active' : ''}">
                            <a class="page-link" href="${pageUrl}">
                                <c:out value="${pg}" />
                            </a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${currentPageNum >= totalPages ? 'disabled' : ''}">
                        <c:url var="nextUrl" value="/admin/fine/list">
                            <c:param name="status" value="${selectedStatus}" />
                            <c:param name="keyword" value="${keyword}" />
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
</main>

<!-- Modal Thanh toán Khoản Phạt (Pay Fine) -->
<div id="paymentModal" class="fine-modal" style="display: none;" hidden>
    <div class="fine-modal-dialog">
        <h3 style="margin-top: 0; margin-bottom: 10px; display: flex; align-items: center; gap: 10px;">
            <i class="fa-solid fa-cash-register" style="color: #27ae60;"></i> Xác nhận Đóng phạt
        </h3>
        <p style="color: var(--text-muted, #95a5a6); font-size: 0.9rem; margin-bottom: 24px;">
            Ghi nhận thanh toán khoản phạt độc giả trực tiếp bằng tiền mặt hoặc qua ví điện tử.
        </p>
        
        <form action="${pageContext.request.contextPath}/admin/fine/update-status" method="post">
            <input type="hidden" name="id" id="payFineId">
            <input type="hidden" name="status" value="PAID">
            
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary, #526177);">Độc giả nộp phạt</label>
                <input type="text" id="payReaderName" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa;">
            </div>

            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: var(--text-secondary, #526177);">Số tiền thanh toán</label>
                <input type="text" id="payAmountText" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #f8f9fa; font-weight: bold; color: #e74c3c;">
            </div>

            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary, #526177);">Phương thức thanh toán</label>
                <select name="paymentMethod" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem; background: white;">
                    <option value="CASH">Tiền mặt trực tiếp (CASH)</option>
                    <option value="ONLINE">Ví điện tử / Chuyển khoản (ONLINE)</option>
                </select>
            </div>

            <div style="margin-bottom: 24px;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary, #526177);">Ghi chú thanh toán</label>
                <input type="text" name="paymentNote" placeholder="Mã giao dịch, biên lai số..."
                       style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 0.9rem;">
            </div>

            <div class="fine-modal-actions">
                <button type="button" onclick="closePaymentModal()" class="btn btn-secondary" style="padding: 10px 20px; border-radius: 8px;">Hủy bỏ</button>
                <button type="submit" class="btn btn-success" style="padding: 10px 24px; border-radius: 8px;">Xác nhận nộp</button>
            </div>
        </form>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/fine-list.js" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
