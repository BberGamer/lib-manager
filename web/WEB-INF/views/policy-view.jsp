<%--
    Trang chi tiết điều lệ hiệu lực do PolicyServlet hiển thị;
    Nhận attribute policy.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="activePage" value="policies" scope="request" />
<c:set var="pageTitle" value="${policy.title} – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/policy.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="policy-public">
    <div class="policy-detail-container public">
        <a class="policy-back" href="${pageContext.request.contextPath}/policies">
            <i class="fa-solid fa-arrow-left"></i> Danh sách điều lệ
        </a>
        <article class="policy-detail">
            <header>
                <span class="policy-eyebrow"><c:out value="${policy.category.label}" /></span>
                <h1><c:out value="${policy.title}" /></h1>
            </header>
            <div class="policy-period">
                <i class="fa-regular fa-calendar"></i> Hiệu lực từ <c:out value="${policy.effectiveFrom}" />
                <c:if test="${not empty policy.effectiveTo}">
                    đến <c:out value="${policy.effectiveTo}" />
                </c:if>
            </div>
            <section class="policy-content">
                <c:out value="${policy.content}" />
            </section>
        </article>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
