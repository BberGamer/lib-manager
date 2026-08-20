<%--
    Trang xác nhận đặt trước do MyReservationServlet render; nhận reservationInfo và loggedUser.
    reservationInfo chứa đầu sách, giới hạn ngày chọn, ngày có sách sớm nhất và ngày nhận dự kiến.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Xác nhận đặt trước — FPT Library" scope="request"/>
<c:set var="pageStylesheet" value="/assets/css/my-reservations.css" scope="request"/>
<c:url var="createUrl" value="/reservation/create"/>
<c:url var="estimateUrl" value="/reservation/estimate"/>
<c:url var="backUrl" value="/books"/>
<c:url var="reservationScriptUrl" value="/assets/js/reservation-form.js"/>
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
            <form class="reservation-schedule-form" action="${createUrl}" method="post"
                  data-reservation-form data-estimate-url="${estimateUrl}">
                <input type="hidden" name="bookId" value="${reservationInfo.book.id}">
                <label for="requestedPickupDate">Ngày bạn muốn nhận sách</label>
                <input id="requestedPickupDate" name="requestedPickupDate" type="date"
                       min="${reservationInfo.minimumPickupDate}"
                       max="${reservationInfo.maximumPickupDate}"
                       value="${reservationInfo.requestedPickupDate}"
                       data-pickup-date required>
                <small>
                    Chọn ngày nhận mong muốn trong vòng 1 năm kể từ hôm nay.
                </small>
                <div class="reservation-estimate" aria-live="polite">
                    <div class="reservation-estimate-row">
                        <span>Ngày sớm nhất dự kiến có sách</span>
                        <strong>
                            <time datetime="${reservationInfo.earliestAvailableDate}"
                                  data-earliest-date>
                                <c:out value="${reservationInfo.earliestAvailableDate}"/>
                            </time>
                        </strong>
                    </div>
                    <div class="reservation-estimate-row">
                        <span>Ngày nhận dự kiến theo lựa chọn của bạn</span>
                        <strong>
                            <time datetime="${reservationInfo.expectedPickupDate}"
                                  data-expected-date>
                                <c:out value="${reservationInfo.expectedPickupDate}"/>
                            </time>
                        </strong>
                    </div>
                    <p data-estimate-message>
                        Ngày sớm nhất được tính từ hạn trả của các bản sách và hàng chờ hiện tại.
                    </p>
                </div>
                <div class="reservation-actions">
                    <a href="${backUrl}">Quay lại</a>
                    <button type="submit" data-reservation-submit>Xác nhận đặt trước</button>
                </div>
            </form>
        </section>
    </div>
</main>

<script src="${reservationScriptUrl}" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
