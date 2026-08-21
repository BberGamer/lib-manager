<%--
    Form tiếp nhận đồ để quên do FoundItemManagementServlet render.
    Nhận foundItem, validationErrors và rolePath; ảnh sẽ được bổ sung ở bước upload sau.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="foundItemListUrl" value="${rolePath}/found-items" />
<c:url var="createFoundItemUrl" value="${rolePath}/found-items/create" />

<main class="page-wrapper found-item-page">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-plus"></i> Tiếp nhận
                    </div>
                    <h1 class="books-page-title">Thêm đồ để quên mới</h1>
                    <p class="books-page-subtitle">
                        Ghi nhận thông tin cơ bản để nhân viên thư viện theo dõi và hoàn trả đúng người
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

    <div class="container" style="padding-top: 24px; padding-bottom: 48px; max-width: 820px;">
        <section class="found-item-form-card">
            <form action="${createFoundItemUrl}" method="post" enctype="multipart/form-data" class="found-item-form">
                <div class="found-item-form-field">
                    <label for="item-status">Trạng thái</label>
                    <input id="item-status" type="text" readonly value="${initialFoundItemStatus}">
                    <p class="found-item-field-help">Đồ mới tiếp nhận luôn ở trạng thái “Có thể nhận”.</p>
                </div>

                <div class="found-item-form-field ${not empty validationErrors.itemName ? 'has-error' : ''}">
                    <label for="item-name">Tên đồ vật <span>*</span></label>
                    <input id="item-name" name="itemName" type="text" required maxlength="150"
                           value="${fn:escapeXml(foundItem.itemName)}" placeholder="Ví dụ: Ví tiền màu đen">
                    <c:if test="${not empty validationErrors.itemName}">
                        <p class="found-item-field-error"><c:out value="${validationErrors.itemName}" /></p>
                    </c:if>
                </div>

                <div class="found-item-form-field ${not empty validationErrors.foundDate ? 'has-error' : ''}">
                    <label for="found-date">Ngày tìm thấy <span>*</span></label>
                    <input id="found-date" name="foundDate" type="date" required value="${foundItem.foundDate}">
                    <c:if test="${not empty validationErrors.foundDate}">
                        <p class="found-item-field-error"><c:out value="${validationErrors.foundDate}" /></p>
                    </c:if>
                </div>

                <div class="found-item-form-field ${not empty validationErrors.description ? 'has-error' : ''}">
                    <label for="item-description">Mô tả</label>
                    <textarea id="item-description" name="description" rows="5" maxlength="2000"
                              placeholder="Mô tả bên ngoài, màu sắc hoặc đặc điểm cơ bản..."><c:out value="${foundItem.description}" /></textarea>
                    <c:if test="${not empty validationErrors.description}">
                        <p class="found-item-field-error"><c:out value="${validationErrors.description}" /></p>
                    </c:if>
                </div>

                <div class="found-item-form-field ${not empty validationErrors.imageFile ? 'has-error' : ''}">
                    <label for="item-image">Ảnh đồ vật <span class="found-item-optional">(tùy chọn)</span></label>
                    <input id="item-image" name="imageFile" type="file" accept="image/jpeg,image/png">
                    <p class="found-item-field-help">Chỉ chấp nhận ảnh JPEG/PNG, dung lượng tối đa 5 MB.</p>
                    <c:if test="${not empty validationErrors.imageFile}">
                        <p class="found-item-field-error"><c:out value="${validationErrors.imageFile}" /></p>
                    </c:if>
                </div>

                <c:if test="${not empty validationErrors.general}">
                    <p class="found-item-field-error"><c:out value="${validationErrors.general}" /></p>
                </c:if>

                <div class="found-item-form-actions">
                    <a class="found-item-button found-item-button-secondary" href="${foundItemListUrl}">Hủy</a>
                    <button class="found-item-button found-item-button-primary" type="submit">
                        <i class="fa-solid fa-floppy-disk"></i>Lưu tiếp nhận
                    </button>
                </div>
            </form>
        </section>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
