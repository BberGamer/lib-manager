<%-- Trang danh sách thể loại do CategoryServlet render; nhận categoryList, currentPage,
     totalPages, flashSuccess và flashError trong request. --%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Quản lý thể loại" scope="request" />
<c:set var="activePage" value="categories" scope="request" />
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/category.css" scope="request" />
<jsp:include page="/WEB-INF/views/fragments/header.jsp" />
    <main class="category-page">
        <header class="page-header">
            <div>
                <p class="eyebrow">Quản trị thư viện</p>
                <h1>Thể loại sách</h1>
            </div>
            <c:url var="newCategoryUrl" value="/admin/categories/new" />
            <a class="button button-primary" href="${newCategoryUrl}">Thêm thể loại</a>
        </header>

        <c:if test="${not empty flashSuccess}">
            <div class="alert alert-success" role="status"><c:out value="${flashSuccess}" /></div>
        </c:if>
        <c:if test="${not empty flashError}">
            <div class="alert alert-error" role="alert"><c:out value="${flashError}" /></div>
        </c:if>

        <section class="table-section" aria-label="Danh sách thể loại">
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th scope="col">ID</th>
                            <th scope="col">Tên thể loại</th>
                            <th scope="col">Mô tả</th>
                            <th scope="col">Cập nhật gần nhất</th>
                            <th scope="col" class="actions-column">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="category" items="${categoryList}">
                            <tr>
                                <td><c:out value="${category.id}" /></td>
                                <td class="category-name"><c:out value="${category.name}" /></td>
                                <td class="description-cell">
                                    <c:choose>
                                        <c:when test="${not empty category.description}">
                                            <c:out value="${category.description}" />
                                        </c:when>
                                        <c:otherwise><span class="muted">Chưa có mô tả</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td><c:out value="${category.updatedAt}" /></td>
                                <td>
                                    <div class="row-actions">
                                        <c:url var="viewUrl" value="/admin/categories/view">
                                            <c:param name="id" value="${category.id}" />
                                        </c:url>
                                        <c:url var="editUrl" value="/admin/categories/edit">
                                            <c:param name="id" value="${category.id}" />
                                        </c:url>
                                        <a class="button button-neutral" href="${viewUrl}">Xem</a>
                                        <a class="button button-neutral" href="${editUrl}">Sửa</a>
                                        <form action="${pageContext.request.contextPath}/admin/categories/delete"
                                              method="post">
                                            <input type="hidden" name="id" value="${category.id}">
                                            <button class="button button-danger" type="submit">Xóa</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty categoryList}">
                            <tr><td class="empty-state" colspan="5">Chưa có thể loại nào.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>

            <c:if test="${totalPages > 1}">
                <nav class="pagination" aria-label="Phân trang thể loại">
                    <c:forEach var="pageNumber" begin="1" end="${totalPages}">
                        <c:url var="pageUrl" value="/admin/categories">
                            <c:param name="page" value="${pageNumber}" />
                        </c:url>
                        <a class="page-link ${pageNumber == currentPage ? 'is-current' : ''}"
                           href="${pageUrl}"><c:out value="${pageNumber}" /></a>
                    </c:forEach>
                </nav>
            </c:if>
        </section>
    </main>
<jsp:include page="/WEB-INF/views/fragments/footer.jsp" />
