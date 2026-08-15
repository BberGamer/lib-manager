<%-- Trang reservation của user do MyReservationServlet render; nhận reservationList và flash message. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đặt trước của tôi — FPT Library" scope="request"/>
<c:set var="pageStylesheet" value="/assets/css/my-reservations.css" scope="request"/>
<c:url var="cancelUrl" value="/reservation/cancel"/>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<main class="reservation-page">
    <div class="container reservation-content">
        <header class="reservation-heading">
            <span>
                <i class="fa-solid fa-bookmark"></i>
                Hàng chờ mượn sách
            </span>
            <h1>Đặt trước của tôi</h1>
            <p>Theo dõi vị trí và trạng thái các yêu cầu đặt trước.</p>
        </header>
        <c:if test="${not empty sessionScope.reservationSuccessMessage}">
            <div class="reservation-alert success">
                <c:out value="${sessionScope.reservationSuccessMessage}"/>
            </div>
            <c:remove var="reservationSuccessMessage" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.reservationErrorMessage}">
            <div class="reservation-alert error">
                <c:out value="${sessionScope.reservationErrorMessage}"/>
            </div>
            <c:remove var="reservationErrorMessage" scope="session"/>
        </c:if>
        <c:choose>
            <c:when test="${not empty reservationList}">
                <div class="reservation-table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>Tên sách</th>
                                <th>Ngày đặt trước</th>
                                <th>Vị trí hiện tại</th>
                                <th>Trạng thái</th>
                                <th>Hạn thực hiện mượn</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="record" items="${reservationList}">
                                <tr>
                                    <td data-label="Tên sách">
                                        <strong>
                                            <c:out value="${record.book.title}"/>
                                        </strong>
                                        <small>ISBN: <c:out value="${record.book.isbn}"/></small>
                                    </td>
                                    <td data-label="Ngày đặt">
                                        <c:out value="${record.reserveDate}"/>
                                    </td>
                                    <td data-label="Vị trí">
                                        <c:out value="${record.queuePosition gt 0 ? record.queuePosition : '—'}"/>
                                    </td>
                                    <td data-label="Trạng thái">
                                        <span class="reservation-badge ${record.status == 'WAITING' ? 'waiting' : (record.status == 'READY_FOR_PICKUP' ? 'ready' : 'closed')}">
                                            <c:choose>
                                                <c:when test="${record.status eq 'WAITING'}">Đang chờ</c:when>
                                                <c:when test="${record.status eq 'READY_FOR_PICKUP'}">Sẵn sàng để mượn</c:when>
                                                <c:when test="${record.status eq 'COMPLETED'}">Đã hoàn tất</c:when>
                                                <c:when test="${record.status eq 'CANCELLED'}">Đã hủy</c:when>
                                                <c:otherwise>Đã hết hạn</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>
                                    <td data-label="Hạn mượn">
                                        <c:out value="${record.expiryDate}" default="—"/>
                                    </td>
                                    <td data-label="Thao tác">
                                        <c:if test="${record.status eq 'WAITING' or record.status eq 'READY_FOR_PICKUP'}">
                                            <form action="${cancelUrl}" method="post">
                                                <input type="hidden" name="reservationId" value="${record.id}">
                                                <button type="submit">Hủy đặt trước</button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <section class="reservation-empty">
                    <i class="fa-solid fa-bookmark"></i>
                    <h2>Bạn chưa có yêu cầu đặt trước</h2>
                    <p>Khi sách hết bản sao khả dụng, bạn có thể tham gia hàng chờ tại trang danh sách sách.</p>
                </section>
            </c:otherwise>
        </c:choose>
    </div>
</main>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
