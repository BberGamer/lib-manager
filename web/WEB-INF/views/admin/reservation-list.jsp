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
    <div class="container reservation-management-container">
        <header class="reservation-page-header">
            <h1 class="section-title">
                <i class="fa-solid fa-clock"></i>
                Quản lý đặt trước sách
            </h1>
            <p class="section-subtitle">
                Duyệt các yêu cầu giữ chỗ trước của độc giả khi sách đang được mượn.
            </p>
        </header>

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
                            <th>Mã</th>
                            <th>Độc giả</th>
                            <th>Thông tin sách</th>
                            <th>Ngày yêu cầu</th>
                            <th>Ngày hết hạn</th>
                            <th>Trạng thái</th>
                            <th class="reservation-actions-heading">Thao tác</th>
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
                                            <c:out value="${reservation.id}" />
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
                                            <c:out value="${empty reservation.reserveDate
                                                ? '-' : reservation.reserveDate}" />
                                        </td>
                                        <td>
                                            <c:out value="${empty reservation.expiryDate
                                                ? '-' : reservation.expiryDate}" />
                                        </td>
                                        <td>
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
                                                        <form action="${reservationUpdateUrl}" method="post">
                                                            <input type="hidden" name="id" value="${reservation.id}">
                                                            <input type="hidden" name="action" value="ready">
                                                            <button type="submit" class="btn btn-sm btn-primary">
                                                                <i class="fa-solid fa-circle-check"></i>
                                                                Sách đã về
                                                            </button>
                                                        </form>
                                                        <form action="${reservationUpdateUrl}" method="post">
                                                            <input type="hidden" name="id" value="${reservation.id}">
                                                            <input type="hidden" name="action" value="cancel">
                                                            <button type="submit"
                                                                    class="btn btn-sm reservation-cancel-button">
                                                                <i class="fa-solid fa-ban"></i>
                                                                Hủy
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:when test="${reservation.status eq 'READY_FOR_PICKUP'}">
                                                    <div class="reservation-action-group">
                                                        <form action="${reservationUpdateUrl}" method="post">
                                                            <input type="hidden" name="id" value="${reservation.id}">
                                                            <input type="hidden" name="action" value="complete">
                                                            <button type="submit" class="btn btn-sm btn-success">
                                                                <i class="fa-solid fa-check-double"></i>
                                                                Đã lấy sách
                                                            </button>
                                                        </form>
                                                        <form action="${reservationUpdateUrl}" method="post">
                                                            <input type="hidden" name="id" value="${reservation.id}">
                                                            <input type="hidden" name="action" value="cancel">
                                                            <button type="submit"
                                                                    class="btn btn-sm reservation-cancel-button">
                                                                <i class="fa-solid fa-ban"></i>
                                                                Hủy
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="reservation-muted">Hoàn tất</span>
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
            <nav class="reservation-pagination" aria-label="Phân trang đặt trước">
                <c:if test="${currentPageNum gt 1}">
                    <c:url var="previousPageUrl" value="${rolePath}/reservation/list">
                        <c:param name="status" value="${selectedStatus}" />
                        <c:param name="keyword" value="${keyword}" />
                        <c:param name="page" value="${currentPageNum - 1}" />
                    </c:url>
                    <a href="${previousPageUrl}" aria-label="Trang trước">
                        <i class="fa-solid fa-chevron-left"></i>
                    </a>
                </c:if>
                <c:forEach begin="1" end="${totalPages}" var="pageNumber">
                    <c:url var="pageUrl" value="${rolePath}/reservation/list">
                        <c:param name="status" value="${selectedStatus}" />
                        <c:param name="keyword" value="${keyword}" />
                        <c:param name="page" value="${pageNumber}" />
                    </c:url>
                    <a class="${pageNumber eq currentPageNum ? 'current' : ''}" href="${pageUrl}">
                        <c:out value="${pageNumber}" />
                    </a>
                </c:forEach>
                <c:if test="${currentPageNum lt totalPages}">
                    <c:url var="nextPageUrl" value="${rolePath}/reservation/list">
                        <c:param name="status" value="${selectedStatus}" />
                        <c:param name="keyword" value="${keyword}" />
                        <c:param name="page" value="${currentPageNum + 1}" />
                    </c:url>
                    <a href="${nextPageUrl}" aria-label="Trang sau">
                        <i class="fa-solid fa-chevron-right"></i>
                    </a>
                </c:if>
            </nav>
        </c:if>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
