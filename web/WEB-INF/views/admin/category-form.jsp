<%--
    Biểu mẫu thêm hoặc sửa danh mục do CategoryServlet hiển thị.
    Nhận category, formMode và validationErrors; session loggedUser được layout quản trị sử dụng.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="categories" scope="request" />
<c:set var="pageTitle" value="${formMode == 'create' ? 'Thêm danh mục' : 'Cập nhật danh mục'} – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/category.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<c:url var="categoryListUrl" value="/admin/categories" />
<c:url var="categoryFormScriptUrl" value="/assets/js/category-form.js" />
<c:set var="formAction" value="${formMode == 'create' ? '/admin/categories/create' : '/admin/categories/update'}" />

<main class="category-management category-editor-page">
    <section class="category-editor-hero">
        <div class="category-editor-content">
            <a class="category-back-link" href="${categoryListUrl}">
                <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách danh mục
            </a>
            <span class="category-eyebrow">
                <i class="fa-solid fa-${formMode == 'create' ? 'plus' : 'pen'}"></i>
                ${formMode == 'create' ? 'Thêm mới' : 'Chỉnh sửa'}
            </span>
            <h1>${formMode == 'create' ? 'Thêm danh mục mới' : 'Cập nhật danh mục'}</h1>
            <p>${formMode == 'create'
                ? 'Tạo một chủ đề mới để tổ chức kho sách rõ ràng và dễ tìm kiếm hơn.'
                : 'Điều chỉnh tên và mô tả của danh mục trong hệ thống.'}</p>
        </div>
    </section>

    <div class="category-editor-content category-editor-body">
        <section class="category-editor-card">
            <header class="category-card-heading">
                <span class="category-card-icon"><i class="fa-solid fa-tag"></i></span>
                <div>
                    <h2>${formMode == 'create' ? 'Thông tin danh mục mới' : 'Thông tin cần cập nhật'}</h2>
                    <p>${formMode == 'create' ? 'Nhập tên và mô tả cho danh mục.' : 'Đang chỉnh sửa: '.concat(category.name)}</p>
                </div>
            </header>

            <form class="category-form" action="${pageContext.request.contextPath}${formAction}" method="post">
                <c:if test="${formMode == 'update'}">
                    <input type="hidden" name="id" value="${category.id}">
                </c:if>

                <div class="category-form-field ${not empty validationErrors.name ? 'has-error' : ''}">
                    <label for="category-name">Tên danh mục <span class="category-required">*</span></label>
                    <input id="category-name" name="name" type="text" required maxlength="100"
                           value="${fn:escapeXml(category.name)}"
                           placeholder="Ví dụ: Văn học, Khoa học, Lịch sử..."
                           aria-describedby="${not empty validationErrors.name ? 'category-name-error' : ''}">
                    <c:if test="${not empty validationErrors.name}">
                        <p class="category-field-error" id="category-name-error">
                            <i class="fa-solid fa-circle-exclamation"></i>
                            <c:out value="${validationErrors.name}" />
                        </p>
                    </c:if>
                </div>

                <div class="category-form-field ${not empty validationErrors.description ? 'has-error' : ''}">
                    <label for="category-description">Mô tả <span class="category-optional">(tùy chọn)</span></label>
                    <textarea id="category-description" name="description" rows="5" maxlength="500"
                              placeholder="Mô tả ngắn gọn về danh mục sách này..."
                              aria-describedby="category-description-meta"><c:out value="${category.description}" /></textarea>
                    <div class="category-field-meta" id="category-description-meta">
                        <c:if test="${not empty validationErrors.description}">
                            <p class="category-field-error">
                                <i class="fa-solid fa-circle-exclamation"></i>
                                <c:out value="${validationErrors.description}" />
                            </p>
                        </c:if>
                        <span class="category-character-counter" data-description-counter>Tối đa 500 ký tự</span>
                    </div>
                </div>

                <div class="category-form-actions">
                    <a class="category-button category-button-secondary" href="${categoryListUrl}">Hủy bỏ</a>
                    <button class="category-button category-button-primary" type="submit">
                        <i class="fa-solid fa-${formMode == 'create' ? 'plus' : 'floppy-disk'}"></i>
                        ${formMode == 'create' ? 'Thêm danh mục' : 'Lưu thay đổi'}
                    </button>
                </div>
            </form>
        </section>
    </div>
</main>

<script src="${categoryFormScriptUrl}" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
