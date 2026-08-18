<%-- Trang danh sách tác giả do AuthorServlet hiển thị; nhận authorList, filter, sort, paging và flash message. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="authors" scope="request" />
<c:set var="pageTitle" value="Quản lý tác giả – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/author.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<c:url var="authorListUrl" value="/admin/authors" />
<c:url var="newAuthorUrl" value="/admin/authors/new" />
<c:url var="authorScriptUrl" value="/assets/js/author-list.js" />

<main class="page-wrapper author-management" style="margin: 0; padding: 0;">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-user-pen"></i> Tác giả
                    </div>
                    <h1 class="books-page-title">Quản lý Tác giả</h1>
                    <p class="books-page-subtitle">
                        Quản lý danh sách và thông tin chi tiết của các tác giả trong Thư viện FPT
                    </p>
                </div>
                <div class="books-page-stats" aria-label="Tổng số tác giả">
                    <div class="bps-item">
                        <span class="bps-num"><c:out value="${totalAuthors}" /></span>
                        <span class="bps-lbl">Tác giả</span>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="container author-body" style="padding-top: 28px;">
        <c:if test="${not empty flashSuccess}"><div class="author-alert success"><i class="fa-solid fa-circle-check"></i> <c:out value="${flashSuccess}" /></div></c:if>
        <c:if test="${not empty flashError}"><div class="author-alert error"><i class="fa-solid fa-circle-exclamation"></i> <c:out value="${flashError}" /></div></c:if>

        <section class="author-search-card"><h2>Tìm kiếm tác giả</h2>
            <form class="author-search-form" action="${authorListUrl}" method="get">
                <input type="hidden" name="sort" value="${sortField}"><input type="hidden" name="order" value="${sortOrder}">
                <label class="author-search-input"><span class="visually-hidden">Từ khóa tìm kiếm</span>
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input type="search" name="keyword" maxlength="150" value="${fn:escapeXml(keyword)}" placeholder="Tìm theo tên tác giả...">
                </label>
                <button class="author-button primary" type="submit"><i class="fa-solid fa-magnifying-glass"></i> Tìm</button>
            </form>
        </section>

        <section class="author-toolbar"><p><i class="fa-solid fa-user-pen"></i> Tổng cộng <strong><c:out value="${totalAuthors}" /></strong> tác giả</p>
            <div class="author-toolbar-actions"><span><i class="fa-solid fa-arrow-up-wide-short"></i> Sắp xếp:</span>
                <c:url var="nameSortUrl" value="/admin/authors"><c:param name="keyword" value="${keyword}" /><c:param name="sort" value="name" /><c:param name="order" value="${sortField == 'name' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}" /></c:url>
                <c:url var="nationalitySortUrl" value="/admin/authors"><c:param name="keyword" value="${keyword}" /><c:param name="sort" value="nationality" /><c:param name="order" value="${sortField == 'nationality' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}" /></c:url>
                <c:url var="birthDateSortUrl" value="/admin/authors"><c:param name="keyword" value="${keyword}" /><c:param name="sort" value="birth_date" /><c:param name="order" value="${sortField == 'birth_date' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}" /></c:url>
                <c:url var="createdAtSortUrl" value="/admin/authors"><c:param name="keyword" value="${keyword}" /><c:param name="sort" value="created_at" /><c:param name="order" value="${sortField == 'created_at' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}" /></c:url>
                <a class="author-sort ${sortField == 'name' ? 'active' : ''}" href="${nameSortUrl}">Tên tác giả<c:if test="${sortField == 'name'}"> ${sortOrder == 'ASC' ? '▲' : '▼'}</c:if></a>
                <a class="author-sort ${sortField == 'nationality' ? 'active' : ''}" href="${nationalitySortUrl}">Quốc tịch<c:if test="${sortField == 'nationality'}"> ${sortOrder == 'ASC' ? '▲' : '▼'}</c:if></a>
                <a class="author-sort ${sortField == 'birth_date' ? 'active' : ''}" href="${birthDateSortUrl}">Ngày sinh<c:if test="${sortField == 'birth_date'}"> ${sortOrder == 'ASC' ? '▲' : '▼'}</c:if></a>
                <a class="author-sort ${sortField == 'created_at' ? 'active' : ''}" href="${createdAtSortUrl}">Ngày tạo<c:if test="${sortField == 'created_at'}"> ${sortOrder == 'ASC' ? '▲' : '▼'}</c:if></a>
                <a class="author-button primary" href="${newAuthorUrl}"><i class="fa-solid fa-plus"></i> Thêm tác giả</a>
            </div>
        </section>

        <section class="author-table-card"><div class="author-table-heading"><h2>Danh sách dữ liệu</h2><span><c:out value="${totalAuthors}" /> bản ghi</span></div>
            <div class="author-table-wrap"><table class="author-table"><thead><tr><th>Ảnh đại diện</th><th>Tên tác giả</th><th>Quốc tịch</th><th>Ngày sinh</th><th>Tiểu sử</th><th>Người tạo</th><th>Thao tác</th></tr></thead><tbody>
                <c:choose><c:when test="${empty authorList}"><tr><td colspan="7" class="author-empty"><i class="fa-regular fa-folder-open"></i><strong>Không tìm thấy tác giả</strong><span>Thử từ khóa khác hoặc đặt lại bộ lọc.</span></td></tr></c:when>
                <c:otherwise><c:forEach var="author" items="${authorList}"><tr>
                    <td><span class="author-avatar"><c:choose><c:when test="${not empty author.avatarUrl}"><img src="${fn:escapeXml(author.avatarUrl)}" alt="Ảnh ${fn:escapeXml(author.name)}"></c:when><c:otherwise><c:out value="${fn:substring(author.name, 0, 1)}" /></c:otherwise></c:choose></span></td>
                    <td><a class="author-name" href="${pageContext.request.contextPath}/admin/authors/view?id=${author.id}"><c:out value="${author.name}" /></a></td>
                    <td><c:out value="${empty author.nationality ? '—' : author.nationality}" /></td><td><c:out value="${empty author.birthDate ? '—' : author.birthDate}" /></td>
                    <td class="author-bio"><c:out value="${empty author.bio ? 'Chưa có tiểu sử' : (fn:length(author.bio) > 70 ? fn:substring(author.bio, 0, 67).concat('...') : author.bio)}" /></td>
                    <td><c:out value="${empty author.createdBy ? '—' : author.createdBy}" /></td>
                    <td><div class="author-row-actions"><a class="author-action edit" href="${pageContext.request.contextPath}/admin/authors/edit?id=${author.id}"><i class="fa-solid fa-pen-to-square"></i> Sửa</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/authors/delete" data-delete-author data-author-name="${fn:escapeXml(author.name)}"><input type="hidden" name="id" value="${author.id}"><button class="author-action delete" type="submit"><i class="fa-solid fa-trash"></i> Xóa</button></form></div></td>
                </tr></c:forEach></c:otherwise></c:choose>
            </tbody></table></div>
        </section>
        <%
                Integer totalPgA = (Integer) request.getAttribute("totalPages");
                Integer curPgA   = (Integer) request.getAttribute("currentPage");
                String kwA       = request.getAttribute("keyword")  != null ? (String) request.getAttribute("keyword")  : "";
                String sfA       = request.getAttribute("sortField") != null ? (String) request.getAttribute("sortField") : "name";
                String soA       = request.getAttribute("sortOrder") != null ? (String) request.getAttribute("sortOrder") : "ASC";
                String ctx3      = request.getContextPath();
                if (totalPgA == null) totalPgA = 1;
                if (curPgA   == null) curPgA   = 1;
                String baseUrlA = ctx3 + "/admin/authors?keyword=" + java.net.URLEncoder.encode(kwA,"UTF-8")
                                + "&sort=" + java.net.URLEncoder.encode(sfA,"UTF-8")
                                + "&order=" + java.net.URLEncoder.encode(soA,"UTF-8")
                                + "&page=";
                if (totalPgA > 1) {
            %>
            <nav aria-label="Phân trang tác giả" style="margin-top: 24px;">
                <ul class="pagination">
                    <!-- Prev -->
                    <li class="page-item <%= curPgA <= 1 ? "disabled" : "" %>">
                        <a class="page-link" href="<%= baseUrlA %><%= curPgA - 1 %>">
                            <i class="fa-solid fa-chevron-left fa-xs"></i>
                        </a>
                    </li>

                    <%
                       if (totalPgA <= 7) {
                           for (int pg = 1; pg <= totalPgA; pg++) { %>
                               <li class="page-item <%= pg == curPgA ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlA %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                       } else {
                           for (int pg = 1; pg <= 2; pg++) { %>
                               <li class="page-item <%= pg == curPgA ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlA %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                           if (curPgA <= 4) {
                               for (int pg = 3; pg <= 5; pg++) { %>
                                   <li class="page-item <%= pg == curPgA ? "active" : "" %>">
                                       <a class="page-link" href="<%= baseUrlA %><%= pg %>"><%= pg %></a>
                                   </li>
                               <% } %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% } else if (curPgA >= totalPgA - 3) { %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                               <% for (int pg = totalPgA - 4; pg <= totalPgA - 2; pg++) { %>
                                   <li class="page-item <%= pg == curPgA ? "active" : "" %>">
                                       <a class="page-link" href="<%= baseUrlA %><%= pg %>"><%= pg %></a>
                                   </li>
                               <% }
                           } else { %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                               <% for (int pg = curPgA - 1; pg <= curPgA + 1; pg++) { %>
                                   <li class="page-item <%= pg == curPgA ? "active" : "" %>">
                                       <a class="page-link" href="<%= baseUrlA %><%= pg %>"><%= pg %></a>
                                   </li>
                               <% } %>
                               <li class="page-item disabled"><span class="page-link">…</span></li>
                           <% }
                           for (int pg = totalPgA - 1; pg <= totalPgA; pg++) { %>
                               <li class="page-item <%= pg == curPgA ? "active" : "" %>">
                                   <a class="page-link" href="<%= baseUrlA %><%= pg %>"><%= pg %></a>
                               </li>
                           <% }
                       }
                    %>

                    <!-- Next -->
                    <li class="page-item <%= curPgA >= totalPgA ? "disabled" : "" %>">
                        <a class="page-link" href="<%= baseUrlA %><%= curPgA + 1 %>">
                            <i class="fa-solid fa-chevron-right fa-xs"></i>
                        </a>
                    </li>
                </ul>
            </nav>
            <% } %>
    </div>
</main>
<script src="${authorScriptUrl}" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
