<%-- Phân trang dùng chung cho danh sách Policy quản trị và Reader.
     Nhận paginationPath, currentPage, totalPages, keyword, selectedCategory và selectedStatus. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${totalPages > 1}">
    <c:set var="windowStart" value="${currentPage - 2 > 2 ? currentPage - 2 : 2}" />
    <c:set var="windowEnd" value="${currentPage + 2 < totalPages - 1 ? currentPage + 2 : totalPages - 1}" />
    <nav class="policy-pagination" aria-label="Phân trang điều lệ">
        <c:choose>
            <c:when test="${currentPage > 1}">
                <c:url var="previousPageUrl" value="${paginationPath}">
                    <c:param name="keyword" value="${keyword}" />
                    <c:param name="category" value="${selectedCategory}" />
                    <c:if test="${not empty selectedStatus}"><c:param name="status" value="${selectedStatus}" /></c:if>
                    <c:param name="page" value="${currentPage - 1}" />
                </c:url>
                <a class="policy-page-arrow" href="${previousPageUrl}" aria-label="Trang trước">
                    <i class="fa-solid fa-chevron-left"></i>
                </a>
            </c:when>
            <c:otherwise><span class="policy-page-arrow disabled" aria-hidden="true"><i class="fa-solid fa-chevron-left"></i></span></c:otherwise>
        </c:choose>

        <c:url var="firstPageUrl" value="${paginationPath}">
            <c:param name="keyword" value="${keyword}" />
            <c:param name="category" value="${selectedCategory}" />
            <c:if test="${not empty selectedStatus}"><c:param name="status" value="${selectedStatus}" /></c:if>
            <c:param name="page" value="1" />
        </c:url>
        <a class="${currentPage == 1 ? 'current' : ''}" href="${firstPageUrl}">1</a>

        <c:if test="${windowStart > 2}"><span class="policy-page-ellipsis">…</span></c:if>
        <c:if test="${windowStart <= windowEnd}">
            <c:forEach begin="${windowStart}" end="${windowEnd}" var="pageNumber">
                <c:url var="pageUrl" value="${paginationPath}">
                    <c:param name="keyword" value="${keyword}" />
                    <c:param name="category" value="${selectedCategory}" />
                    <c:if test="${not empty selectedStatus}"><c:param name="status" value="${selectedStatus}" /></c:if>
                    <c:param name="page" value="${pageNumber}" />
                </c:url>
                <a class="${pageNumber == currentPage ? 'current' : ''}" href="${pageUrl}"><c:out value="${pageNumber}" /></a>
            </c:forEach>
        </c:if>
        <c:if test="${windowEnd < totalPages - 1}"><span class="policy-page-ellipsis">…</span></c:if>

        <c:url var="lastPageUrl" value="${paginationPath}">
            <c:param name="keyword" value="${keyword}" />
            <c:param name="category" value="${selectedCategory}" />
            <c:if test="${not empty selectedStatus}"><c:param name="status" value="${selectedStatus}" /></c:if>
            <c:param name="page" value="${totalPages}" />
        </c:url>
        <a class="${currentPage == totalPages ? 'current' : ''}" href="${lastPageUrl}"><c:out value="${totalPages}" /></a>

        <c:choose>
            <c:when test="${currentPage < totalPages}">
                <c:url var="nextPageUrl" value="${paginationPath}">
                    <c:param name="keyword" value="${keyword}" />
                    <c:param name="category" value="${selectedCategory}" />
                    <c:if test="${not empty selectedStatus}"><c:param name="status" value="${selectedStatus}" /></c:if>
                    <c:param name="page" value="${currentPage + 1}" />
                </c:url>
                <a class="policy-page-arrow" href="${nextPageUrl}" aria-label="Trang sau">
                    <i class="fa-solid fa-chevron-right"></i>
                </a>
            </c:when>
            <c:otherwise><span class="policy-page-arrow disabled" aria-hidden="true"><i class="fa-solid fa-chevron-right"></i></span></c:otherwise>
        </c:choose>
    </nav>
</c:if>
