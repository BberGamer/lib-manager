<%--
    Trang xem chi tiết danh mục do CategoryServlet hiển thị.
    Nhận request attribute category; session loggedUser được layout quản trị sử dụng.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="categories" scope="request" />
<c:set var="pageTitle" value="Chi tiết danh mục – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/category.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="categoryListUrl" value="/admin/categories" />
<c:url var="editUrl" value="/admin/categories/edit">
    <c:param name="id" value="${category.id}" />
</c:url>

<main class="category-management category-editor-page">
    <section class="category-editor-hero">
        <div class="category-editor-content">
            <a class="category-back-link" href="${categoryListUrl}">
                <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách danh mục
            </a>
            <span class="category-eyebrow"><i class="fa-solid fa-eye"></i> Chi tiết</span>
            <h1>Chi tiết danh mục</h1>
            <p>Xem thông tin và lịch sử cập nhật của danh mục trong hệ thống.</p>
        </div>
    </section>

    <div class="category-editor-content category-editor-body">
        <section class="category-editor-card category-detail-card">
            <header class="category-detail-heading">
                <span class="category-detail-icon"><i class="fa-solid fa-tag"></i></span>
                <div>
                    <h2><c:out value="${category.name}" /></h2>
                    <span>ID #<c:out value="${category.id}" /></span>
                </div>
            </header>

            <dl class="category-detail-list">
                <div><dt>Mã danh mục</dt><dd>#<c:out value="${category.id}" /></dd></div>
                <div><dt>Tên danh mục</dt><dd class="category-detail-name"><c:out value="${category.name}" /></dd></div>
                <div><dt>Mô tả</dt><dd>
                    <c:choose>
                        <c:when test="${not empty category.description}"><c:out value="${category.description}" /></c:when>
                        <c:otherwise><em>Chưa có mô tả</em></c:otherwise>
                    </c:choose>
                </dd></div>
                <div><dt>Người tạo</dt><dd><c:out value="${empty category.createdBy ? '—' : category.createdBy}" /></dd></div>
                <div><dt>Ngày tạo</dt><dd><c:out value="${category.createdAt}" /></dd></div>
                <div><dt>Cập nhật bởi</dt><dd><c:out value="${empty category.updatedBy ? '—' : category.updatedBy}" /></dd></div>
                <div><dt>Cập nhật lần cuối</dt><dd><c:out value="${category.updatedAt}" /></dd></div>
            </dl>
        </section>

        <div class="category-detail-actions">
            <a class="category-button category-button-secondary" href="${categoryListUrl}">
                <i class="fa-solid fa-list"></i> Tất cả danh mục
            </a>
            <a class="category-button category-button-primary" href="${editUrl}">
                <i class="fa-solid fa-pen"></i> Chỉnh sửa danh mục
            </a>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
