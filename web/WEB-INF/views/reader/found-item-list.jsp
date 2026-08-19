<%-- Trang Reader xem đồ để quên do ReaderFoundItemServlet render; nhận foundItems, imageUrls, keyword và flash session. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:url var="readerFoundItemsUrl" value="/found-items" />
<c:url var="readerClaimUrl" value="/found-items/claim" />
<c:url var="readerMyClaimsUrl" value="/found-items/my-claims" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<main class="reader-found-items-page">
    <div class="container">
        <header class="reader-found-items-heading">
            <h1>Đồ để quên</h1>
            <p>Xem đồ đang được thư viện tiếp nhận và gửi thông tin xác minh để nhận lại.</p>
            <a class="btn btn-outline" href="${readerMyClaimsUrl}">Yêu cầu nhận đồ của tôi</a>
        </header>

        <c:if test="${not empty sessionScope.foundItemClaimSuccess}">
            <div class="reader-found-items-alert success"><c:out value="${sessionScope.foundItemClaimSuccess}" /></div>
            <c:remove var="foundItemClaimSuccess" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.foundItemClaimError}">
            <div class="reader-found-items-alert error"><c:out value="${sessionScope.foundItemClaimError}" /></div>
            <c:remove var="foundItemClaimError" scope="session" />
        </c:if>

        <form action="${readerFoundItemsUrl}" method="get" class="reader-found-items-search">
            <label for="found-item-keyword">Tìm đồ để quên</label>
            <div>
                <input id="found-item-keyword" name="keyword" value="${keyword}" placeholder="Nhập tên hoặc mô tả đồ vật">
                <button type="submit" class="btn btn-primary">Tìm kiếm</button>
            </div>
        </form>


        <c:choose>
            <c:when test="${not empty foundItems}">
                <div class="reader-found-items-grid">
                    <c:forEach var="item" items="${foundItems}">
                        <article class="reader-found-item-card">
                            <c:if test="${not empty imageUrls[item.id]}">
                                <img src="${imageUrls[item.id]}" alt="Ảnh đồ vật đã tiếp nhận" class="reader-found-item-image">
                            </c:if>
                            <h2><c:out value="${item.itemName}" /></h2>
                            <p class="reader-found-item-date">Ngày tìm thấy: <c:out value="${item.foundDate}" /></p>
                            <p><c:out value="${not empty item.description ? item.description : 'Chưa có mô tả thêm.'}" /></p>
                            <form action="${readerClaimUrl}" method="post" class="reader-found-item-claim-form">
                                <input type="hidden" name="itemId" value="${item.id}">
                                <label for="claim-note-${item.id}">Đặc điểm để xác minh *</label>
                                <textarea id="claim-note-${item.id}" name="claimNote" maxlength="1000" required
                                          placeholder="Ví dụ: màu sắc, nhãn dán hoặc đặc điểm riêng của đồ vật"></textarea>
                                <button type="submit" class="btn btn-primary">Yêu cầu nhận lại</button>
                            </form>
                        </article>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p class="reader-found-items-empty">Hiện chưa có đồ để quên phù hợp.</p>
            </c:otherwise>
        </c:choose>
    </div>
</main>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
