<%--
    Trang chi tiết đồ để quên do FoundItemManagementServlet render.
    Nhận foundItem và rolePath; chỉ hiển thị dữ liệu cơ bản của bản ghi tiếp nhận.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="foundItemListUrl" value="${rolePath}/found-items" />

<main class="page-wrapper found-item-page">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-box-open"></i> Đồ để quên
                    </div>
                    <h1 class="books-page-title">Chi tiết đồ để quên</h1>
                    <p class="books-page-subtitle">
                        Thông tin tiếp nhận và xử lý quy trình bàn giao đồ cho độc giả
                    </p>
                </div>
                <div>
                    <a class="btn btn-outline btn-sm" href="${foundItemListUrl}">
                        <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách
                    </a>
                </div>
            </div>
        </div>
    </section>

    <div class="container" style="padding-top: 24px; padding-bottom: 48px; max-width: 960px;">
        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="found-item-alert found-item-alert-success" role="status">
                <i class="fa-solid fa-circle-check"></i>
                <c:out value="${sessionScope.flashSuccess}" />
            </div>
            <c:remove var="flashSuccess" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="found-item-alert found-item-alert-error" role="alert">
                <i class="fa-solid fa-circle-exclamation"></i>
                <c:out value="${sessionScope.flashError}" />
            </div>
            <c:remove var="flashError" scope="session" />
        </c:if>

        <section class="found-item-detail-card">
            <header>
                <div class="found-item-detail-header-left">
                    <span class="found-item-detail-icon"><i class="fa-solid fa-box-open"></i></span>
                    <div>
                        <p>Mã đồ để quên: LF-<c:out value="${foundItem.id}" /></p>
                        <h1><c:out value="${foundItem.itemName}" /></h1>
                    </div>
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
                    <h2><i class="fa-solid fa-user-check"></i> Yêu cầu nhận đồ từ độc giả</h2>
                    <p><strong>Độc giả gửi yêu cầu:</strong> <c:out value="${latestClaim.readerName}" /> (<c:out value="${latestClaim.readerUsername}" />)</p>
                    <p><strong>Ghi chú nhận dạng / xác minh:</strong> <c:out value="${latestClaim.claimNote}" /></p>
                    <c:if test="${latestClaim.status.code eq 'PENDING'}">
                        <form action="${rolePath}/found-items/verify" method="post" class="found-item-review-actions">
                            <input type="hidden" name="itemId" value="${foundItem.id}">
                            <input type="hidden" name="claimId" value="${latestClaim.id}">
                            <button type="submit" name="decision" value="APPROVE" class="btn btn-primary">
                                <i class="fa-solid fa-check"></i> Chấp nhận yêu cầu
                            </button>
                            <button type="submit" name="decision" value="REJECT" class="btn btn-outline">
                                <i class="fa-solid fa-xmark"></i> Từ chối yêu cầu
                            </button>
                        </form>
                    </c:if>
                    <c:if test="${latestClaim.status.code eq 'APPROVED'}">
                        <p style="color: #b45309; font-weight: 600;"><i class="fa-solid fa-clock"></i> Đã chấp nhận yêu cầu, đang chờ Reader xác nhận đã đến nhận đồ tại quầy.</p>
                    </c:if>
                    <c:if test="${latestClaim.status.code eq 'READER_CONFIRMED'}">
                        <p style="color: #15803d; font-weight: 600;"><i class="fa-solid fa-circle-info"></i> Reader đã xác nhận nhận đồ. Thủ thư hãy xác nhận hoàn tất bàn giao tại quầy:</p>
                        <form action="${rolePath}/found-items/complete-handover" method="post" class="found-item-review-actions">
                            <input type="hidden" name="itemId" value="${foundItem.id}">
                            <input type="hidden" name="claimId" value="${latestClaim.id}">
                            <button type="submit" class="btn btn-primary">
                                <i class="fa-solid fa-handshake"></i> Xác nhận đã giao đồ hoàn tất
                            </button>
                        </form>
                    </c:if>
                    <c:if test="${latestClaim.status.code eq 'COMPLETED'}">
                        <p style="color: #15803d; font-weight: 600;"><i class="fa-solid fa-circle-check"></i> Đã hoàn tất trao trả đồ cho độc giả.</p>
                    </c:if>
                </section>
            </c:if>
        </section>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
