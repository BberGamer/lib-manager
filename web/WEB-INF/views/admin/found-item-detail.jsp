<%--
    Trang chi tiết đồ để quên do FoundItemManagementServlet render.
    Nhận foundItem và rolePath; chỉ hiển thị dữ liệu cơ bản của bản ghi tiếp nhận.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="foundItemListUrl" value="${rolePath}/found-items" />

<main class="found-item-page">
    <div class="found-item-editor-container found-item-editor-body">
        <a class="found-item-back-link" href="${foundItemListUrl}"><i class="fa-solid fa-arrow-left"></i>Quay lại danh sách</a>
        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="found-item-alert success"><c:out value="${sessionScope.flashSuccess}" /></div>
            <c:remove var="flashSuccess" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="found-item-alert error"><c:out value="${sessionScope.flashError}" /></div>
            <c:remove var="flashError" scope="session" />
        </c:if>
        <section class="found-item-detail-card">
            <header>
                <span class="found-item-detail-icon"><i class="fa-solid fa-box-open"></i></span>
                <div>
                    <p>Mã đồ để quên: LF-<c:out value="${foundItem.id}" /></p>
                    <h1><c:out value="${foundItem.itemName}" /></h1>
                </div>
                <span class="found-item-status found-item-status-${foundItem.status.cssClass}"><c:out value="${foundItem.status.displayName}" /></span>
            </header>
            <dl>
                <div><dt>Ngày tìm thấy</dt><dd><c:out value="${foundItem.foundDate}" /></dd></div>
                <div><dt>Mô tả</dt><dd><c:out value="${not empty foundItem.description ? foundItem.description : 'Chưa có mô tả'}" /></dd></div>
                <c:if test="${not empty imageUrl}">
                    <div><dt>Ảnh đồ vật</dt><dd><img class="found-item-image" src="${imageUrl}" alt="Ảnh đồ vật đã tiếp nhận"></dd></div>
                </c:if>
                <div><dt>Thời điểm tiếp nhận</dt><dd><c:out value="${foundItem.createdAt}" /></dd></div>
                <div><dt>Cập nhật gần nhất</dt><dd><c:out value="${foundItem.updatedAt}" /></dd></div>
            </dl>
            <c:if test="${not empty latestClaim}">
                <section class="found-item-claim-review">
                    <h2>Yêu cầu nhận đồ</h2>
                    <p><strong>Reader:</strong> <c:out value="${latestClaim.readerName}" />
                        (<c:out value="${latestClaim.readerUsername}" />)</p>
                    <p><strong>Ghi chú xác minh:</strong> <c:out value="${latestClaim.claimNote}" /></p>
                    <c:if test="${latestClaim.status.code eq 'PENDING'}">
                        <form action="${rolePath}/found-items/verify" method="post" class="found-item-review-actions">
                            <input type="hidden" name="itemId" value="${foundItem.id}">
                            <input type="hidden" name="claimId" value="${latestClaim.id}">
                            <button type="submit" name="decision" value="APPROVE" class="btn btn-primary">Chấp nhận yêu cầu</button>
                            <button type="submit" name="decision" value="REJECT" class="btn btn-outline">Từ chối yêu cầu</button>
                        </form>
                    </c:if>
                    <c:if test="${latestClaim.status.code eq 'APPROVED'}"><p>Đã chấp nhận, đang chờ Reader xác nhận đã nhận đồ.</p></c:if>
                    <c:if test="${latestClaim.status.code eq 'READER_CONFIRMED'}">
                        <p>Reader đã xác nhận nhận đồ. Hãy xác nhận hoàn tất bàn giao tại quầy.</p>
                        <form action="${rolePath}/found-items/complete-handover" method="post" class="found-item-review-actions">
                            <input type="hidden" name="itemId" value="${foundItem.id}">
                            <input type="hidden" name="claimId" value="${latestClaim.id}">
                            <button type="submit" class="btn btn-primary">Xác nhận đã giao đồ</button>
                        </form>
                    </c:if>
                    <c:if test="${latestClaim.status.code eq 'COMPLETED'}"><p>Đã hoàn tất giao đồ.</p></c:if>
                </section>
            </c:if>
        </section>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
