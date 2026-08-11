<%-- Biểu mẫu thêm/sửa thể loại do CategoryServlet render; nhận category, formMode
     và validationErrors trong request. --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="${formMode == 'create' ? 'Thêm thể loại' : 'Cập nhật thể loại'}"
       scope="request" />
<c:set var="activePage" value="categories" scope="request" />
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/category.css" scope="request" />
<jsp:include page="/WEB-INF/views/fragments/header.jsp" />
    <main class="category-page category-page-narrow">
        <header class="page-header">
            <div>
                <p class="eyebrow">Quản trị thư viện</p>
                <h1>${formMode == 'create' ? 'Thêm thể loại' : 'Cập nhật thể loại'}</h1>
            </div>
            <c:url var="listUrl" value="/admin/categories" />
            <a class="button button-neutral" href="${listUrl}">Quay lại</a>
        </header>

        <c:set var="formAction" value="${formMode == 'create'
                ? '/admin/categories/create' : '/admin/categories/update'}" />
        <form class="category-form" action="${pageContext.request.contextPath}${formAction}" method="post">
            <c:if test="${formMode == 'update'}">
                <input type="hidden" name="id" value="${category.id}">
            </c:if>

            <div class="form-field">
                <label for="category-name">Tên thể loại <span aria-hidden="true">*</span></label>
                <input id="category-name" name="name" type="text" required maxlength="100"
                       value="${fn:escapeXml(category.name)}"
                       aria-describedby="name-error">
                <c:if test="${not empty validationErrors.name}">
                    <p id="name-error" class="field-error"><c:out value="${validationErrors.name}" /></p>
                </c:if>
            </div>

            <div class="form-field">
                <label for="category-description">Mô tả</label>
                <textarea id="category-description" name="description" rows="6"
                          maxlength="500" aria-describedby="description-error"><c:out value="${category.description}" /></textarea>
                <div class="field-meta">Tối đa 500 ký tự</div>
                <c:if test="${not empty validationErrors.description}">
                    <p id="description-error" class="field-error">
                        <c:out value="${validationErrors.description}" />
                    </p>
                </c:if>
            </div>

            <div class="form-actions">
                <a class="button button-neutral" href="${listUrl}">Hủy</a>
                <button class="button button-primary" type="submit">
                    ${formMode == 'create' ? 'Thêm thể loại' : 'Lưu thay đổi'}
                </button>
            </div>
        </form>
    </main>
<jsp:include page="/WEB-INF/views/fragments/footer.jsp" />
