<%--
    Trang chi tiết quản trị do AdminPolicyServlet hiển thị;
    Nhận attribute policy.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="policies" scope="request" />
<c:set var="pageTitle" value="Chi tiết điều lệ – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/policy.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="policy-page">
    <div class="policy-detail-container">
        <a class="policy-back" href="${pageContext.request.contextPath}/admin/policies">
            <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách
        </a>
        <article class="policy-detail">
            <header>
                <span class="policy-badge"><c:out value="${policy.effectiveStatus}" /></span>
                <h1><c:out value="${policy.title}" /></h1>
                <p>
                    <c:out value="${policy.policyCode}" /> · phiên bản <c:out value="${policy.version}" /> · <c:out value="${policy.category.label}" />
                </p>
            </header>
            <dl>
                <div>
                    <dt>Khoảng hiệu lực</dt>
                    <dd>
                        <c:out value="${empty policy.effectiveFrom ? 'Chưa đặt' : policy.effectiveFrom}" /> – <c:out value="${empty policy.effectiveTo ? 'Không giới hạn' : policy.effectiveTo}" />
                    </dd>
                </div>
                <div>
                    <dt>Người cập nhật</dt>
                    <dd><c:out value="${policy.updatedBy}" /></dd>
                </div>
            </dl>
            <section class="policy-content">
                <c:out value="${policy.content}" />
            </section>
        </article>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
