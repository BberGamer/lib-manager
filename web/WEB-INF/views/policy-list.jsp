<%--
    Trang danh sách điều lệ hiệu lực do PolicyServlet hiển thị;
    Nhận policyList, bộ lọc và phân trang.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="activePage" value="policies" scope="request" />
<c:set var="pageTitle" value="Điều lệ thư viện – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/policy.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="listUrl" value="/policies" />

<main class="policy-public">
    <section class="policy-public-hero">
        <div class="policy-public-container">
            <span class="policy-eyebrow">
                <i class="fa-solid fa-scale-balanced"></i> Thư viện FPT
            </span>
            <h1>Điều lệ thư viện</h1>
            <p>Các quy định hiện đang được áp dụng tại thư viện.</p>
        </div>
    </section>

    <div class="policy-public-container policy-public-body">
        <form class="policy-public-filter" action="${listUrl}" method="get">
            <input name="keyword" maxlength="200" value="${fn:escapeXml(keyword)}" placeholder="Tìm điều lệ...">
            <select name="category">
                <option value="">Tất cả danh mục</option>
                <c:forEach var="item" items="${policyCategories}">
                    <option value="${item}" ${selectedCategory == item ? 'selected' : ''}>
                        <c:out value="${item.label}" />
                    </option>
                </c:forEach>
            </select>
            <button class="policy-button primary" type="submit">Tìm kiếm</button>
        </form>

        <p class="policy-result-count">
            <c:out value="${totalPolicies}" /> điều lệ đang áp dụng
        </p>

        <div class="policy-card-grid">
            <c:choose>
                <c:when test="${empty policyList}">
                    <div class="policy-empty-card">Chưa có điều lệ phù hợp.</div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="policy" items="${policyList}">
                        <article class="policy-public-card">
                            <span><c:out value="${policy.category.label}" /></span>
                            <h2>
                                <a href="${pageContext.request.contextPath}/policies/view?id=${policy.id}">
                                    <c:out value="${policy.title}" />
                                </a>
                            </h2>
                            <small>Hiệu lực từ <c:out value="${policy.effectiveFrom}" /></small>
                        </article>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <c:if test="${totalPages > 1}">
            <nav class="policy-pagination">
                <c:forEach begin="1" end="${totalPages}" var="pageNumber">
                    <c:url var="pageUrl" value="/policies">
                        <c:param name="keyword" value="${keyword}" />
                        <c:param name="category" value="${selectedCategory}" />
                        <c:param name="page" value="${pageNumber}" />
                    </c:url>
                    <a class="${pageNumber == currentPage ? 'current' : ''}" href="${pageUrl}">
                        <c:out value="${pageNumber}" />
                    </a>
                </c:forEach>
            </nav>
        </c:if>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
