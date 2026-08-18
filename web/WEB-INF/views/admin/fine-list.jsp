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
<c:set var="pageStylesheet" value="/assets/css/fine-list.css?v=4" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="fineListUrl" value="${rolePath}/fine/list" />
<c:url var="fineUpdateUrl" value="${rolePath}/fine/update-status" />

<main class="page-wrapper fine-page">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-circle-dollar-to-slot"></i> Lưu thông
                    </div>
                    <h1 class="books-page-title">Quản lý các khoản phạt độc giả</h1>
                    <p class="books-page-subtitle">
                        Thu tiền phạt trả muộn, hư hại sách và quản lý các phương thức thanh toán
                    </p>
                </div>
                <div class="books-page-stats" aria-label="Tổng số khoản phạt">
                    <div class="bps-item">
                        <span class="bps-num"><c:out value="${totalRecords}" /></span>
                        <span class="bps-lbl">Khoản phạt</span>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <div class="container fine-management-container">
        <!-- Alert notifications -->
        <c:if test="${not empty sessionScope.successMsg}">
            <div class="alert alert-success">
                <i class="fa-solid fa-circle-check"></i>
                <c:out value="${sessionScope.successMsg}" />
            </div>
            <c:remove var="successMsg" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.errorMsg}">
            <div class="alert alert-danger">
                <i class="fa-solid fa-circle-exclamation"></i>
                <c:out value="${sessionScope.errorMsg}" />
            </div>
            <c:remove var="errorMsg" scope="session" />
        </c:if>

        <!-- Filter bar -->
        <form action="${fineListUrl}" method="get" class="fine-filter-form">
            <div class="search-bar-wrapper">
                <div class="search-bar-inner">
                    <div class="search-field fine-keyword-field">
                        <label for="fine-keyword">Tìm kiếm</label>
                        <div class="search-input-wrap">
                            <i class="fa-solid fa-magnifying-glass search-icon"></i>
                            <input id="fine-keyword" class="form-control" type="search" name="keyword"
                                   value="${fn:escapeXml(keyword)}" maxlength="100" autocomplete="off"
                                   placeholder="Tên độc giả, tên sách, lý do phạt...">
                        </div>
                    </div>
                    <div class="search-field select-field">
                        <label for="fine-status">Trạng thái thanh toán</label>
                        <select id="fine-status" class="form-select" name="status">
                            <option value="">Tất cả trạng thái</option>
                            <option value="UNPAID" ${selectedStatus eq 'UNPAID' ? 'selected' : ''}>
                                Chưa thanh toán (UNPAID)
                            </option>
                            <option value="PAID" ${selectedStatus eq 'PAID' ? 'selected' : ''}>
                                Đã thanh toán (PAID)
                            </option>
                            <option value="WAIVED" ${selectedStatus eq 'WAIVED' ? 'selected' : ''}>
                                Được miễn giảm (WAIVED)
                            </option>
                        </select>
                    </div>
                    <div class="fine-filter-actions">
                        <button type="submit" class="btn btn-primary">
                            <i class="fa-solid fa-magnifying-glass"></i> Tìm
                        </button>
                        <a class="btn btn-outline fine-reset-button" href="${fineListUrl}"
                           title="Xóa bộ lọc" aria-label="Xóa bộ lọc">
                            <i class="fa-solid fa-rotate-right"></i>
                        </a>
                    </div>
                </div>
            </div>
        </form>

        <section class="books-topbar">
            <div class="fine-results-info">
                <i class="fa-solid fa-circle-dollar-to-slot"></i>
                Tổng cộng <strong><c:out value="${totalRecords}" /></strong> khoản phạt
            </div>
        </section>

        <!-- Table list -->
        <section class="data-table-wrap fine-table-card">
            <div class="fine-table-scroll">
                <table class="data-table fine-table">
                    <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Độc giả</th>
                            <th>Thông tin sách</th>
                            <th>Số tiền phạt</th>
                            <th>Lý do / Ngày tạo</th>
                            <th>Trạng thái</th>
                            <th class="fine-action-heading">Thao tác</th>
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
                                        <td><span class="fine-code">#<c:out value="${f.id}" /></span></td>
                                        <td>
                                            <div class="fine-primary-text">
                                                <c:out value="${not empty f.user ? f.user.fullName : '—'}" />
                                            </div>
                                            <div class="fine-secondary-text">
                                                @<c:out value="${not empty f.user ? f.user.username : ''}" />
                                                <c:if test="${not empty f.user.phone}">
                                                    | <c:out value="${f.user.phone}" />
                                                </c:if>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="fine-primary-text">
                                                <c:out value="${not empty f.borrowRecord and not empty f.borrowRecord.book ? f.borrowRecord.book.title : '—'}" />
                                            </div>
                                            <div class="fine-secondary-text">
                                                Lượt mượn: #<c:out value="${f.borrowRecordId}" />
                                            </div>
                                        </td>
                                        <td class="fine-amount">
                                            <fmt:formatNumber value="${f.amount}" type="currency" currencySymbol="₫" maxFractionDigits="0" />
                                        </td>
                                        <td>
                                            <div class="fine-reason"><c:out value="${f.reason}" /></div>
                                            <div class="fine-secondary-text">
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
                                                    <div class="fine-payment-meta">
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
                                        <td class="fine-action-cell">
                                            <c:choose>
                                                <c:when test="${f.status eq 'UNPAID'}">
                                                    <div class="fine-row-actions">
                                                        <button type="button" class="btn btn-sm fine-pay-button"
                                                                data-open-payment data-fine-id="${f.id}"
                                                                data-reader-name="${fn:escapeXml(not empty f.user ? f.user.fullName : '')}"
                                                                data-fine-amount="${f.amount}">
                                                            <i class="fa-solid fa-cash-register"></i> Đóng phạt
                                                        </button>
                                                        <form action="${fineUpdateUrl}" method="post" data-waive-fine>
                                                            <input type="hidden" name="id" value="${f.id}">
                                                            <input type="hidden" name="status" value="WAIVED">
                                                            <button type="submit" class="btn btn-sm fine-waive-button">
                                                                <i class="fa-solid fa-hand-holding-heart"></i> Miễn giảm
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="fine-completed">Hoàn tất</span>
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
            <c:set var="cp" value="${currentPageNum}" />
            <c:set var="tp" value="${totalPages}" />
            <c:set var="winStart" value="${cp - 2 > 2 ? cp - 2 : 2}" />
            <c:set var="winEnd" value="${cp + 2 < tp - 1 ? cp + 2 : tp - 1}" />
            <nav class="fine-pagination" aria-label="Phân trang phạt">
                <ul class="pagination" style="flex-wrap: wrap;">

                    <li class="page-item ${cp <= 1 ? 'disabled' : ''}">
                        <c:choose>
                            <c:when test="${cp > 1}">
                                <c:url var="finePrev" value="${rolePath}/fine/list">
                                    <c:param name="status" value="${selectedStatus}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="page" value="${cp - 1}" />
                                </c:url>
                                <a class="page-link" href="${finePrev}"><i class="fa-solid fa-chevron-left fa-xs"></i></a>
                            </c:when>
                            <c:otherwise>
                                <span class="page-link"><i class="fa-solid fa-chevron-left fa-xs"></i></span>
                            </c:otherwise>
                        </c:choose>
                    </li>

                    <li class="page-item ${cp == 1 ? 'active' : ''}">
                        <c:url var="fineP1" value="${rolePath}/fine/list">
                            <c:param name="status" value="${selectedStatus}" />
                            <c:param name="keyword" value="${keyword}" />
                            <c:param name="page" value="1" />
                        </c:url>
                        <a class="page-link" href="${fineP1}">1</a>
                    </li>

                    <c:if test="${winStart > 2}">
                        <li class="page-item disabled"><span class="page-link">…</span></li>
                    </c:if>

                    <c:if test="${winStart <= winEnd}">
                        <c:forEach begin="${winStart}" end="${winEnd}" var="pg">
                            <li class="page-item ${pg == cp ? 'active' : ''}">
                                <c:url var="finePUrl" value="${rolePath}/fine/list">
                                    <c:param name="status" value="${selectedStatus}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="page" value="${pg}" />
                                </c:url>
                                <a class="page-link" href="${finePUrl}"><c:out value="${pg}" /></a>
                            </li>
                        </c:forEach>
                    </c:if>

                    <c:if test="${winEnd < tp - 1}">
                        <li class="page-item disabled"><span class="page-link">…</span></li>
                    </c:if>

                    <c:if test="${tp > 1}">
                        <li class="page-item ${cp == tp ? 'active' : ''}">
                            <c:url var="finePLast" value="${rolePath}/fine/list">
                                <c:param name="status" value="${selectedStatus}" />
                                <c:param name="keyword" value="${keyword}" />
                                <c:param name="page" value="${tp}" />
                            </c:url>
                            <a class="page-link" href="${finePLast}"><c:out value="${tp}" /></a>
                        </li>
                    </c:if>

                    <li class="page-item ${cp >= tp ? 'disabled' : ''}">
                        <c:choose>
                            <c:when test="${cp < tp}">
                                <c:url var="fineNext" value="${rolePath}/fine/list">
                                    <c:param name="status" value="${selectedStatus}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="page" value="${cp + 1}" />
                                </c:url>
                                <a class="page-link" href="${fineNext}"><i class="fa-solid fa-chevron-right fa-xs"></i></a>
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
</main>

<!-- Modal Thanh toán Khoản Phạt (Pay Fine) -->
<div id="paymentModal" class="fine-modal" hidden>
    <div class="fine-modal-dialog">
        <h3 class="fine-modal-title">
            <i class="fa-solid fa-cash-register"></i> Xác nhận Đóng phạt
        </h3>
        <p class="fine-modal-description">
            Ghi nhận thanh toán khoản phạt độc giả trực tiếp bằng tiền mặt hoặc qua ví điện tử.
        </p>
        
        <form action="${fineUpdateUrl}" method="post">
            <input type="hidden" name="id" id="payFineId">
            <input type="hidden" name="status" value="PAID">
            
            <div class="fine-form-field">
                <label for="payReaderName">Độc giả nộp phạt</label>
                <input type="text" id="payReaderName" readonly>
            </div>

            <div class="fine-form-field">
                <label for="payAmountText">Số tiền thanh toán</label>
                <input class="fine-payment-amount" type="text" id="payAmountText" readonly>
            </div>

            <div class="fine-form-field">
                <label for="payment-method">Phương thức thanh toán</label>
                <select id="payment-method" name="paymentMethod">
                    <option value="CASH">Tiền mặt trực tiếp (CASH)</option>
                    <option value="ONLINE">Ví điện tử / Chuyển khoản (ONLINE)</option>
                </select>
            </div>

            <div class="fine-form-field fine-form-field-last">
                <label for="payment-note">Ghi chú thanh toán</label>
                <input type="text" name="paymentNote" placeholder="Mã giao dịch, biên lai số..."
                       id="payment-note">
            </div>

            <div class="fine-modal-actions">
                <button type="button" data-close-payment class="btn btn-outline">Hủy bỏ</button>
                <button type="submit" class="btn btn-success">Xác nhận nộp</button>
            </div>
        </form>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/fine-list.js?v=2" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
