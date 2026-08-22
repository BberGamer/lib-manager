<%--
    Trang danh sách đồ để quên do FoundItemManagementServlet render.
    Nhận foundItemList, totalFoundItems, keyword, selectedStatus, statusValues, currentPage, totalPages và flashSuccess.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="foundItemListUrl" value="${rolePath}/found-items" />
<c:url var="newFoundItemUrl" value="${rolePath}/found-items/new" />

<main class="page-wrapper found-item-page">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-box-open"></i> Nghiệp vụ thư viện
                    </div>
                    <h1 class="books-page-title">Quản lý đồ để quên</h1>
                    <p class="books-page-subtitle">
                        Tiếp nhận và theo dõi các đồ vật được để quên tại thư viện
                    </p>
                </div>
                <div class="books-page-stats" aria-label="Tổng số đồ vật">
                    <div class="bps-item">
                        <span class="bps-num"><c:out value="${totalFoundItems}" /></span>
                        <span class="bps-lbl">Đồ vật</span>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="container" style="padding-top: 24px; padding-bottom: 48px;">
        <c:if test="${not empty flashSuccess}">
            <div class="found-item-alert found-item-alert-success">
                <i class="fa-solid fa-circle-check"></i><c:out value="${flashSuccess}" />
            </div>
        </c:if>

        <section class="found-item-filter-card">
            <form action="${foundItemListUrl}" method="get" class="found-item-filter-form">
                <label>
                    <span>Tìm kiếm</span>
                    <input type="search" name="keyword" maxlength="150" value="${fn:escapeXml(keyword)}"
                           placeholder="Tên hoặc mô tả đồ vật">
                </label>
                <label>
                    <span>Trạng thái</span>
                    <select name="status">
                        <option value="">Tất cả</option>
                        <c:forEach var="status" items="${statusValues}">
                            <option value="${status.code}" ${selectedStatus eq status.code ? 'selected' : ''}>
                                <c:out value="${status.displayName}" />
                            </option>
                        </c:forEach>
                    </select>
                </label>
                <button class="found-item-button found-item-button-primary" type="submit">
                    <i class="fa-solid fa-magnifying-glass"></i>Tìm kiếm
                </button>
                <a class="found-item-button found-item-button-primary" href="${newFoundItemUrl}">
                    <i class="fa-solid fa-plus"></i>Tiếp nhận đồ
                </a>
            </form>
        </section>

        <section class="found-item-table-card">
            <div class="found-item-table-wrap">
                <table class="found-item-table">
                    <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Tên đồ vật</th>
                            <th>Ngày tìm thấy</th>
                            <th>Trạng thái</th>
                            <th>Cập nhật</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty foundItemList}">
                                <tr>
                                    <td colspan="6" class="found-item-empty">Chưa có đồ để quên phù hợp.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="foundItem" items="${foundItemList}">
                                    <tr>
                                        <td><strong>LF-<c:out value="${foundItem.id}" /></strong></td>
                                        <td>
                                            <strong><c:out value="${foundItem.itemName}" /></strong>
                                            <c:if test="${not empty foundItem.description}">
                                                <span class="found-item-description"><c:out value="${foundItem.description}" /></span>
                                            </c:if>
                                        </td>
                                        <td><c:out value="${foundItem.foundDate}" /></td>
                                        <td><span class="found-item-status found-item-status-${foundItem.status.cssClass}"><c:out value="${foundItem.status.displayName}" /></span></td>
                                        <td><c:out value="${foundItem.updatedAt}" /></td>
                                        <td>
                                            <c:url var="detailUrl" value="${rolePath}/found-items/view">
                                                <c:param name="id" value="${foundItem.id}" />
                                            </c:url>
                                            <a class="found-item-link" href="${detailUrl}">
                                                <c:out value="${foundItem.status.code eq 'CLAIM_PENDING' ? 'Xác minh' : 'Xem'}" />
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <c:if test="${totalPages gt 1}">
            <nav class="found-item-pagination" aria-label="Phân trang đồ để quên">
                <c:forEach var="pageNumber" begin="1" end="${totalPages}">
                    <c:url var="pageUrl" value="${rolePath}/found-items">
                        <c:param name="keyword" value="${keyword}" />
                        <c:param name="status" value="${selectedStatus}" />
                        <c:param name="page" value="${pageNumber}" />
                    </c:url>
                    <a class="found-item-page-link ${pageNumber eq currentPage ? 'is-current' : ''}" href="${pageUrl}">
                        <c:out value="${pageNumber}" />
                    </a>
                </c:forEach>
            </nav>
        </c:if>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
