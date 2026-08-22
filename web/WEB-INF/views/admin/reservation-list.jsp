<%--
    Trang quản lý đặt trước do ReservationManagementServlet hiển thị.
    Mong đợi request attributes reservationList, totalPages, currentPageNum, selectedStatus và keyword;
    session attributes successMsg, errorMsg và loggedUser.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="reservation" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/reservation-list.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<c:url var="reservationListUrl" value="${rolePath}/reservation/list" />
<c:url var="reservationUpdateUrl" value="${rolePath}/reservation/update" />

<main class="page-wrapper reservation-management-page">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-clock"></i> Đặt trước
                    </div>
                    <h1 class="books-page-title">Quản lý đặt trước sách</h1>
                    <p class="books-page-subtitle">
                        Duyệt và theo dõi các yêu cầu giữ sách của độc giả trong Thư viện FPT University
                    </p>
                </div>
                <div class="books-page-stats" aria-label="Tổng số yêu cầu đặt trước">
                    <div class="bps-item">
                        <span class="bps-num"><c:out value="${totalRecords}" /></span>
                        <span class="bps-lbl">Đặt trước</span>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="container reservation-management-container" style="padding-top: 28px;">

        <c:if test="${not empty sessionScope.successMsg}">
            <div class="reservation-alert reservation-alert-success" role="status">
                <i class="fa-solid fa-circle-check"></i>
                <c:out value="${sessionScope.successMsg}" />
            </div>
            <c:remove var="successMsg" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.errorMsg}">
            <div class="reservation-alert reservation-alert-error" role="alert">
                <i class="fa-solid fa-circle-exclamation"></i>
                <c:out value="${sessionScope.errorMsg}" />
            </div>
            <c:remove var="errorMsg" scope="session" />
        </c:if>

        <section class="reservation-filter-card" aria-labelledby="reservation-filter-title">
            <h2 id="reservation-filter-title" class="visually-hidden">Bộ lọc yêu cầu đặt trước</h2>
            <form class="reservation-filter-form" action="${reservationListUrl}" method="get">
                <label class="reservation-filter-keyword">
                    <span>Tìm kiếm</span>
                    <input type="search" name="keyword" maxlength="200"
                           value="${fn:escapeXml(keyword)}"
                           placeholder="Tên độc giả, tên sách, ISBN...">
                </label>
                <label>
                    <span>Trạng thái</span>
                    <select name="status">
                        <option value="">Tất cả trạng thái</option>
                        <option value="WAITING" ${selectedStatus eq 'WAITING' ? 'selected' : ''}>
                            Chờ mượn
                        </option>
                        <option value="READY_FOR_PICKUP"
                                ${selectedStatus eq 'READY_FOR_PICKUP' ? 'selected' : ''}>
                            Sách sẵn sàng
                        </option>
                        <option value="COMPLETED" ${selectedStatus eq 'COMPLETED' ? 'selected' : ''}>
                            Đã mượn sách
                        </option>
                        <option value="CANCELLED" ${selectedStatus eq 'CANCELLED' ? 'selected' : ''}>
                            Đã hủy
                        </option>
                        <option value="EXPIRED" ${selectedStatus eq 'EXPIRED' ? 'selected' : ''}>
                            Đã hết hạn
                        </option>
                    </select>
                </label>
                <button type="submit" class="btn btn-primary reservation-filter-button">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    Lọc kết quả
                </button>
            </form>
        </section>

        <section class="reservation-table-card" aria-label="Danh sách yêu cầu đặt trước">
            <div class="reservation-table-scroll">
                <table class="reservation-table">
                    <thead>
                        <tr>
                            <th style="width: 55px;">Mã</th>
                            <th>Độc giả</th>
                            <th>Thông tin sách</th>
                            <th>Ngày đặt / Muốn nhận</th>
                            <th>Dự kiến / Hạn giữ</th>
                            <th style="text-align: center;">Trạng thái</th>
                            <th class="reservation-actions-heading" style="text-align: right;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty reservationList}">
                                <tr>
                                    <td class="reservation-empty-state" colspan="7">
                                        <i class="fa-solid fa-hourglass-empty"></i>
                                        <span>Không tìm thấy yêu cầu đặt trước nào.</span>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="reservation" items="${reservationList}">
                                    <tr>
                                        <td class="reservation-record-id">
                                            #<c:out value="${reservation.id}" />
                                        </td>
                                        <td>
                                            <strong><c:out value="${reservation.user.fullName}" /></strong>
                                            <small>
                                                @<c:out value="${reservation.user.username}" />
                                                <c:if test="${not empty reservation.user.phone}">
                                                    · <c:out value="${reservation.user.phone}" />
                                                </c:if>
                                            </small>
                                        </td>
                                        <td>
                                            <strong><c:out value="${reservation.book.title}" /></strong>
                                            <small>ISBN: <c:out value="${reservation.book.isbn}" /></small>
                                        </td>
                                        <td>
                                            <span style="display: block; font-weight: 500; white-space: nowrap;">
                                                <c:choose>
                                                    <c:when test="${not empty reservation.reserveDate}">
                                                        ${fn:replace(fn:substring(reservation.reserveDate, 0, 16), 'T', ' ')}
                                                    </c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <c:if test="${not empty reservation.requestedPickupDate}">
                                                <small style="color: #64748b; white-space: nowrap;">
                                                    Nhận: <c:out value="${reservation.requestedPickupDate}" />
                                                </small>
                                            </c:if>
                                        </td>
                                        <td>
                                            <span style="display: block; font-weight: 500; white-space: nowrap;">
                                                <c:choose>
                                                    <c:when test="${not empty reservation.expectedPickupDate}">
                                                        Dự kiến: <c:out value="${reservation.expectedPickupDate}" />
                                                    </c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <c:if test="${not empty reservation.expiryDate}">
                                                <small style="color: #ef4444; font-weight: 500; white-space: nowrap;">
                                                    Hạn: <c:out value="${reservation.expiryDate}" />
                                                </small>
                                            </c:if>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="reservation-status
                                                  reservation-status-${fn:toLowerCase(reservation.status)}">
                                                <c:choose>
                                                    <c:when test="${reservation.status eq 'WAITING'}">Chờ mượn</c:when>
                                                    <c:when test="${reservation.status eq 'READY_FOR_PICKUP'}">
                                                        Sách sẵn sàng
                                                    </c:when>
                                                    <c:when test="${reservation.status eq 'COMPLETED'}">
                                                        Đã hoàn thành
                                                    </c:when>
                                                    <c:when test="${reservation.status eq 'CANCELLED'}">Đã hủy</c:when>
                                                    <c:when test="${reservation.status eq 'EXPIRED'}">
                                                        Đã quá hạn
                                                    </c:when>
                                                    <c:otherwise><c:out value="${reservation.status}" /></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <td class="reservation-actions">
                                            <c:choose>
                                                <c:when test="${reservation.status eq 'WAITING'}">
                                                    <div class="reservation-action-group">
                                                        <form action="${reservationUpdateUrl}" method="post" style="display: inline-block; margin: 0;">
                                                            <input type="hidden" name="id" value="${reservation.id}">
                                                            <input type="hidden" name="action" value="ready">
                                                            <button type="submit" class="btn btn-sm btn-primary" style="white-space: nowrap;">
                                                                <i class="fa-solid fa-circle-check"></i>
                                                                Sách đã về
                                                            </button>
                                                        </form>
                                                        <form action="${reservationUpdateUrl}" method="post" style="display: inline-block; margin: 0;">
                                                            <input type="hidden" name="id" value="${reservation.id}">
                                                            <input type="hidden" name="action" value="cancel">
                                                            <button type="submit"
                                                                    class="btn btn-sm reservation-cancel-button" style="white-space: nowrap;">
                                                                <i class="fa-solid fa-ban"></i>
                                                                Hủy
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:when test="${reservation.status eq 'READY_FOR_PICKUP'}">
                                                    <span class="reservation-muted" style="white-space: nowrap;">
                                                        Chờ giao sách
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="reservation-muted" style="white-space: nowrap;">Hoàn tất</span>
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

        <c:if test="${totalPages gt 1}">
            <c:set var="cp" value="${currentPageNum}" />
            <c:set var="tp" value="${totalPages}" />
            <c:set var="winStart" value="${cp - 2 > 2 ? cp - 2 : 2}" />
            <c:set var="winEnd" value="${cp + 2 < tp - 1 ? cp + 2 : tp - 1}" />
            <nav class="reservation-pagination" aria-label="Phân trang đặt trước">

                <c:choose>
                    <c:when test="${cp > 1}">
                        <c:url var="resvPrev" value="${rolePath}/reservation/list">
                            <c:param name="status" value="${selectedStatus}" />
                            <c:param name="keyword" value="${keyword}" />
                            <c:param name="page" value="${cp - 1}" />
                        </c:url>
                        <a href="${resvPrev}" aria-label="Trang trước">
                            <i class="fa-solid fa-chevron-left"></i>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <span class="disabled" aria-hidden="true">
                            <i class="fa-solid fa-chevron-left"></i>
                        </span>
                    </c:otherwise>
                </c:choose>

                <c:url var="resvP1" value="${rolePath}/reservation/list">
                    <c:param name="status" value="${selectedStatus}" />
                    <c:param name="keyword" value="${keyword}" />
                    <c:param name="page" value="1" />
                </c:url>
                <a class="${cp == 1 ? 'current' : ''}" href="${resvP1}">1</a>

                <c:if test="${winStart > 2}"><span class="reservation-page-ellipsis">…</span></c:if>

                <c:if test="${winStart <= winEnd}">
                    <c:forEach begin="${winStart}" end="${winEnd}" var="pageNumber">
                        <c:url var="resvPUrl" value="${rolePath}/reservation/list">
                            <c:param name="status" value="${selectedStatus}" />
                            <c:param name="keyword" value="${keyword}" />
                            <c:param name="page" value="${pageNumber}" />
                        </c:url>
                        <a class="${pageNumber eq cp ? 'current' : ''}" href="${resvPUrl}"><c:out value="${pageNumber}" /></a>
                    </c:forEach>
                </c:if>

                <c:if test="${winEnd < tp - 1}"><span class="reservation-page-ellipsis">…</span></c:if>

                <c:if test="${tp > 1}">
                    <c:url var="resvPLast" value="${rolePath}/reservation/list">
                        <c:param name="status" value="${selectedStatus}" />
                        <c:param name="keyword" value="${keyword}" />
                        <c:param name="page" value="${tp}" />
                    </c:url>
                    <a class="${cp == tp ? 'current' : ''}" href="${resvPLast}"><c:out value="${tp}" /></a>
                </c:if>

                <c:choose>
                    <c:when test="${cp < tp}">
                        <c:url var="resvNext" value="${rolePath}/reservation/list">
                            <c:param name="status" value="${selectedStatus}" />
                            <c:param name="keyword" value="${keyword}" />
                            <c:param name="page" value="${cp + 1}" />
                        </c:url>
                        <a href="${resvNext}" aria-label="Trang sau">
                            <i class="fa-solid fa-chevron-right"></i>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <span class="disabled" aria-hidden="true">
                            <i class="fa-solid fa-chevron-right"></i>
                        </span>
                    </c:otherwise>
                </c:choose>

            </nav>
        </c:if>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
