<%-- Trang xác nhận đặt trước do MyReservationServlet render; nhận reservationInfo và loggedUser. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Xác nhận đặt trước — FPT Library" scope="request"/>
<c:set var="pageStylesheet" value="/assets/css/my-reservations.css" scope="request"/>
<c:url var="createUrl" value="/reservation/create"/><c:url var="backUrl" value="/books"/>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="reservation-page">
    <div class="container reservation-content">
        <section class="reservation-confirm-card">
            <div class="reservation-confirm-icon">
                <i class="fa-solid fa-bookmark"></i>
            </div><h1>Đặt trước sách</h1>
            <p>Bạn sẽ được thông báo khi đến lượt và có sách sẵn sàng để mượn.</p>
            <dl>
                <div>
                    <dt>Tên sách</dt>
                    <dd><c:out value="${reservationInfo.book.title}"/></dd>
                </div>
                <div>
                    <dt>ISBN</dt>
                    <dd><c:out value="${reservationInfo.book.isbn}"/></dd>
                </div>
                <div>
                    <dt>Số bản sao khả dụng</dt>
                    <dd>0</dd>
                </div>
                <div>
                    <dt>Số người đang chờ</dt>
                    <dd><c:out value="${reservationInfo.waitingCount}"/></dd>
                </div>
                <div>
                    <dt>Vị trí dự kiến của bạn</dt>
                    <dd class="queue-number"><c:out value="${reservationInfo.expectedPosition}"/></dd>
                </div>
            </dl>
            <div class="reservation-actions">
                <a href="${backUrl}">Quay lại</a>
                <form action="${createUrl}" method="post">
                    <input type="hidden" name="bookId" value="${reservationInfo.book.id}">
                    <button type="submit">Xác nhận đặt trước</button>
                </form>
            </div>
        </section>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>

