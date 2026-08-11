<%-- Trang chi tiết thể loại do CategoryServlet render; nhận category và chỉ hiển thị dữ liệu đã escape. --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Chi tiết thể loại" scope="request" />
<c:set var="activePage" value="categories" scope="request" />
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/category.css" scope="request" />
<jsp:include page="/WEB-INF/views/fragments/header.jsp" />
    <main class="category-page category-page-narrow">
        <header class="page-header">
            <div>
                <p class="eyebrow">Chi tiết thể loại</p>
                <h1><c:out value="${category.name}" /></h1>
            </div>
            <c:url var="listUrl" value="/admin/categories" />
            <a class="button button-neutral" href="${listUrl}">Quay lại</a>
        </header>

        <dl class="detail-list">
            <div><dt>Mã thể loại</dt><dd><c:out value="${category.id}" /></dd></div>
            <div>
                <dt>Mô tả</dt>
                <dd><c:out value="${empty category.description ? 'Chưa có mô tả' : category.description}" /></dd>
            </div>
            <div><dt>Người tạo</dt><dd><c:out value="${empty category.createdBy ? '-' : category.createdBy}" /></dd></div>
            <div><dt>Ngày tạo</dt><dd><c:out value="${category.createdAt}" /></dd></div>
            <div><dt>Cập nhật bởi</dt><dd><c:out value="${empty category.updatedBy ? '-' : category.updatedBy}" /></dd></div>
            <div><dt>Cập nhật lúc</dt><dd><c:out value="${category.updatedAt}" /></dd></div>
        </dl>

        <div class="form-actions">
            <c:url var="editUrl" value="/admin/categories/edit">
                <c:param name="id" value="${category.id}" />
            </c:url>
            <a class="button button-primary" href="${editUrl}">Sửa thể loại</a>
        </div>
    </main>
<jsp:include page="/WEB-INF/views/fragments/footer.jsp" />
