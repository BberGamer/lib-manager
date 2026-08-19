<%-- Trang Reader theo dõi yêu cầu nhận đồ do ReaderFoundItemServlet render; nhận myClaims và flash session. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:url var="readerFoundItemsUrl" value="/found-items" />
<c:url var="readerConfirmPickupUrl" value="/found-items/confirm-pickup" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<main class="reader-found-items-page"><div class="container">
    <header class="reader-found-items-heading"><h1>Yêu cầu nhận đồ của tôi</h1>
        <p>Theo dõi yêu cầu đang xử lý và lịch sử đồ đã nhận.</p>
        <a class="btn btn-outline" href="${readerFoundItemsUrl}">Quay lại tìm đồ để quên</a></header>
    <c:if test="${not empty sessionScope.foundItemClaimSuccess}"><div class="reader-found-items-alert success"><c:out value="${sessionScope.foundItemClaimSuccess}" /></div><c:remove var="foundItemClaimSuccess" scope="session" /></c:if>
    <c:if test="${not empty sessionScope.foundItemClaimError}"><div class="reader-found-items-alert error"><c:out value="${sessionScope.foundItemClaimError}" /></div><c:remove var="foundItemClaimError" scope="session" /></c:if>
    <c:choose><c:when test="${not empty myClaims}"><section class="reader-my-claims">
        <c:forEach var="claim" items="${myClaims}"><article class="reader-my-claim-card">
            <strong><c:out value="${claim.itemName}" /></strong><p>Gửi yêu cầu: <c:out value="${claim.createdAt}" /></p>
            <c:choose>
                <c:when test="${claim.status.code eq 'PENDING'}"><p>Đang chờ Thủ thư xác minh.</p></c:when>
                <c:when test="${claim.status.code eq 'APPROVED'}"><p>Đã được chấp nhận. Sau khi nhận tại quầy, hãy xác nhận.</p><form action="${readerConfirmPickupUrl}" method="post"><input type="hidden" name="claimId" value="${claim.id}"><button type="submit" class="btn btn-primary">Tôi đã nhận đồ</button></form></c:when>
                <c:when test="${claim.status.code eq 'READER_CONFIRMED'}"><p>Bạn đã xác nhận nhận đồ. Đang chờ Thủ thư hoàn tất bàn giao.</p></c:when>
                <c:when test="${claim.status.code eq 'COMPLETED'}"><p>Đã hoàn tất nhận đồ.</p></c:when>
                <c:otherwise><p>Yêu cầu đã bị từ chối.</p></c:otherwise>
            </c:choose>
        </article></c:forEach>
    </section></c:when><c:otherwise><p class="reader-found-items-empty">Bạn chưa có yêu cầu nhận đồ nào.</p></c:otherwise></c:choose>
</div></main>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
