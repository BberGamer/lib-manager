<%--
    Trang danh sách danh mục do CategoryServlet hiển thị.
    Nhận categoryList, totalCategories, keyword, sortField, sortOrder, currentPage, totalPages và flash message.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="categories" scope="request" />
<c:set var="pageTitle" value="Quản lý danh mục – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/category.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="categoryListUrl" value="/admin/categories" />
<c:url var="newCategoryUrl" value="/admin/categories/new" />
<c:url var="categoryScriptUrl" value="/assets/js/category-list.js" />

<main class="category-management">
    <section class="category-hero">
        <div class="category-content category-hero-inner">
            <div>
                <span class="category-eyebrow"><i class="fa-solid fa-tags"></i> Danh mục</span>
                <h1>Danh mục sách</h1>
                <p>Phân loại và tổ chức các chủ đề sách trong hệ thống</p>
            </div>
            <div class="category-stat" aria-label="Tổng số danh mục">
                <strong><c:out value="${totalCategories}" /></strong>
                <span>Danh mục</span>
            </div>
        </div>
    </section>

    <div class="category-content category-body">
        <c:if test="${not empty flashSuccess}">
            <div class="category-alert category-alert-success">
                <i class="fa-solid fa-circle-check"></i> <c:out value="${flashSuccess}" />
            </div>
        </c:if>
        <c:if test="${not empty flashError}">
            <div class="category-alert category-alert-error">
                <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${flashError}" />
            </div>
        </c:if>

        <section class="category-search-card" aria-labelledby="category-search-title">
            <h2 id="category-search-title">Tìm kiếm danh mục</h2>
            <form action="${categoryListUrl}" method="get" class="category-search-form">
                <input type="hidden" name="sort" value="${sortField}">
                <input type="hidden" name="order" value="${sortOrder}">
                <label class="category-search-input">
                    <span class="visually-hidden">Tên danh mục</span>
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input type="search" name="keyword" value="${fn:escapeXml(keyword)}"
                           maxlength="100" placeholder="Tìm theo tên danh mục...">
                </label>
                <button class="category-button category-button-primary" type="submit">
                    <i class="fa-solid fa-magnifying-glass"></i> Tìm
                </button>
                <a class="category-button category-button-reset" href="${categoryListUrl}"
                   title="Đặt lại bộ lọc" aria-label="Đặt lại bộ lọc">
                    <i class="fa-solid fa-rotate-right"></i>
                </a>
            </form>
        </section>

        <section class="category-toolbar">
            <p><i class="fa-solid fa-tag"></i> Tổng cộng <strong><c:out value="${totalCategories}" /></strong> danh mục</p>
            <div class="category-toolbar-actions">
                <span><i class="fa-solid fa-arrow-up-wide-short"></i> Sắp xếp:</span>
                <c:url var="nameSortUrl" value="/admin/categories">
                    <c:param name="keyword" value="${keyword}" />
                    <c:param name="sort" value="name" />
                    <c:param name="order" value="${sortField == 'name' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}" />
                </c:url>
                <c:url var="dateSortUrl" value="/admin/categories">
                    <c:param name="keyword" value="${keyword}" />
                    <c:param name="sort" value="created_at" />
                    <c:param name="order" value="${sortField == 'created_at' && sortOrder == 'DESC' ? 'ASC' : 'DESC'}" />
                </c:url>
                <a class="category-sort ${sortField == 'name' ? 'is-active' : ''}" href="${nameSortUrl}">
                    Tên danh mục
                    <c:if test="${sortField == 'name'}">${sortOrder == 'ASC' ? '▲' : '▼'}</c:if>
                </a>
                <a class="category-sort ${sortField == 'created_at' ? 'is-active' : ''}" href="${dateSortUrl}">
                    Ngày tạo
                    <c:if test="${sortField == 'created_at'}">${sortOrder == 'ASC' ? '▲' : '▼'}</c:if>
                </a>
                <a class="category-button category-button-primary" href="${newCategoryUrl}">
                    <i class="fa-solid fa-plus"></i> Thêm danh mục
                </a>
            </div>
        </section>

        <section class="category-table-card">
            <div class="category-table-wrap">
                <table class="category-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tên danh mục</th>
                            <th>Mô tả</th>
                            <th>Cập nhật lần cuối</th>
                            <th class="category-actions-heading">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty categoryList}">
                                <tr><td colspan="5" class="category-empty">
                                    <i class="fa-regular fa-folder-open"></i>
                                    <strong>Không tìm thấy danh mục</strong>
                                    <span>Thử từ khóa khác hoặc đặt lại bộ lọc.</span>
                                </td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="category" items="${categoryList}">
                                    <tr>
                                        <td class="category-id">#<c:out value="${category.id}" /></td>
                                        <td><div class="category-name-cell">
                                            <span class="category-icon"><i class="fa-solid fa-tag"></i></span>
                                            <strong><c:out value="${category.name}" /></strong>
                                        </div></td>
                                        <td class="category-description">
                                            <c:choose>
                                                <c:when test="${not empty category.description}">
                                                    <c:out value="${fn:length(category.description) > 80 ? fn:substring(category.description, 0, 77) : category.description}" />
                                                    <c:if test="${fn:length(category.description) > 80}">...</c:if>
                                                </c:when>
                                                <c:otherwise><em>Chưa có mô tả</em></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="category-date"><c:out value="${category.updatedAt}" /></td>
                                        <td><div class="category-row-actions">
                                            <c:url var="viewUrl" value="/admin/categories/view"><c:param name="id" value="${category.id}" /></c:url>
                                            <c:url var="editUrl" value="/admin/categories/edit"><c:param name="id" value="${category.id}" /></c:url>
                                            <a class="category-action category-action-view" href="${viewUrl}" title="Xem chi tiết"><i class="fa-solid fa-eye"></i> Xem</a>
                                            <a class="category-action category-action-edit" href="${editUrl}" title="Chỉnh sửa"><i class="fa-solid fa-pen"></i> Sửa</a>
                                            <form action="${pageContext.request.contextPath}/admin/categories/delete" method="post"
                                                  data-delete-category data-category-name="${fn:escapeXml(category.name)}">
                                                <input type="hidden" name="id" value="${category.id}">
                                                <button class="category-action category-action-delete" type="submit" title="Xóa danh mục"><i class="fa-solid fa-trash"></i> Xóa</button>
                                            </form>
                                        </div></td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <%
            Integer totalPgC = (Integer) request.getAttribute("totalPages");
            Integer curPgC   = (Integer) request.getAttribute("currentPage");
            String ctx4      = request.getContextPath();
            if (totalPgC == null) totalPgC = 1;
            if (curPgC   == null) curPgC   = 1;
            String baseUrlC  = ctx4 + "/admin/categories?page=";
            if (totalPgC > 1) {
        %>
        <nav aria-label="Phân trang danh mục" style="margin-top: 24px;">
            <ul class="pagination">
                <!-- Prev -->
                <li class="page-item <%= curPgC <= 1 ? "disabled" : "" %>">
                    <a class="page-link" href="<%= baseUrlC %><%= curPgC - 1 %>">
                        <i class="fa-solid fa-chevron-left fa-xs"></i>
                    </a>
                </li>

                <%
                   if (totalPgC <= 7) {
                       for (int pg = 1; pg <= totalPgC; pg++) { %>
                           <li class="page-item <%= pg == curPgC ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlC %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                   } else {
                       for (int pg = 1; pg <= 2; pg++) { %>
                           <li class="page-item <%= pg == curPgC ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlC %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                       if (curPgC <= 4) {
                           for (int pg = 3; pg <= 5; pg++) { %>
                               <li class="page-item <%= pg == curPgC ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlC %><%= pg %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% } else if (curPgC >= totalPgC - 3) { %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = totalPgC - 4; pg <= totalPgC - 2; pg++) { %>
                               <li class="page-item <%= pg == curPgC ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlC %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                       } else { %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% for (int pg = curPgC - 1; pg <= curPgC + 1; pg++) { %>
                               <li class="page-item <%= pg == curPgC ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlC %><%= pg %>"><%= pg %></a>
                               </li>
                           <% } %>
                           <li class="page-item disabled"><span class="page-link">…</span></li>
                       <% }
                       for (int pg = totalPgC - 1; pg <= totalPgC; pg++) { %>
                           <li class="page-item <%= pg == curPgC ? "active" : "" %>">
                               <a class="page-link" href="<%= baseUrlC %><%= pg %>"><%= pg %></a>
                           </li>
                       <% }
                   }
                %>

                <!-- Next -->
                <li class="page-item <%= curPgC >= totalPgC ? "disabled" : "" %>">
                    <a class="page-link" href="<%= baseUrlC %><%= curPgC + 1 %>">
                        <i class="fa-solid fa-chevron-right fa-xs"></i>
                    </a>
                </li>
            </ul>
        </nav>
        <% } %>

    </div>
</main>
<script src="${categoryScriptUrl}" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
