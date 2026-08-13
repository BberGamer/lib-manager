<%-- Form thêm/sửa draft do AdminPolicyServlet hiển thị; nhận policy, formMode,
     policyCategories và validationErrors. Phiên bản được backend quản lý tự động. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="isManagePageAttr" value="true" scope="request"/>
<c:set var="activePage" value="policies" scope="request"/>
<c:set var="pageTitle" value="${formMode == 'create' ? 'Thêm điều lệ' : (formMode == 'revision' ? 'Tạo phiên bản mới' : 'Sửa điều lệ')} – FPT Library" scope="request"/>
<c:set var="pageStylesheet" value="/assets/css/policy.css" scope="request"/>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<c:url var="listUrl" value="/admin/policies"/>
<c:set var="actionPath" value="${formMode == 'create' ? '/admin/policies/create' : (formMode == 'revision' ? '/admin/policies/revise' : '/admin/policies/update')}"/>
<main class="policy-page">
    <section class="policy-hero"><div class="policy-container policy-hero-inner"><div>
        <span class="policy-eyebrow"><i class="fa-solid fa-scale-balanced"></i> Điều lệ</span>
        <h1>${formMode == 'create' ? 'Thêm điều lệ mới' : (formMode == 'revision' ? 'Soạn phiên bản mới' : 'Cập nhật bản nháp')}</h1>
        <p>Nội dung được lưu dưới dạng văn bản thuần và chỉ công khai sau khi xuất bản.</p>
    </div></div></section>
    <div class="policy-form-container">
        <a class="policy-back" href="${listUrl}"><i class="fa-solid fa-arrow-left"></i> Quay lại danh sách</a>
        <section class="policy-form-card">
            <form method="post" action="${pageContext.request.contextPath}${actionPath}"
                  class="policy-form" data-policy-form novalidate>
                <c:if test="${formMode == 'update'}"><input type="hidden" name="id" value="${policy.id}"></c:if>
                <c:if test="${formMode == 'revision'}"><input type="hidden" name="sourceId" value="${sourceId}"></c:if>
                <c:if test="${not empty validationErrors}"><div class="policy-validation-summary" role="alert">Vui lòng kiểm tra lại các trường được đánh dấu bên dưới.</div></c:if>
                <div class="policy-form-grid">
                    <label class="policy-field full ${not empty validationErrors.policyCode ? 'invalid' : ''}" data-policy-field>
                        <span>Mã điều lệ *</span>
                        <input name="policyCode" required minlength="2" maxlength="50"
                               pattern="[A-Z][A-Z0-9_]{1,49}" value="${fn:escapeXml(policy.policyCode)}"
                               placeholder="BORROW_RULES" aria-describedby="policy-code-hint policy-code-error">
                        <span id="policy-code-hint" class="policy-field-hint">Từ 2–50 ký tự: chữ in hoa, số hoặc dấu gạch dưới; bắt đầu bằng chữ.</span>
                        <small id="policy-code-error" data-policy-error><c:out value="${validationErrors.policyCode}" /></small>
                    </label>
                    <label class="policy-field full ${not empty validationErrors.title ? 'invalid' : ''}" data-policy-field>
                        <span>Tiêu đề *</span><input name="title" required minlength="3" maxlength="200" value="${fn:escapeXml(policy.title)}">
                        <small data-policy-error><c:out value="${validationErrors.title}" /></small>
                    </label>
                    <label class="policy-field full ${not empty validationErrors.category ? 'invalid' : ''}" data-policy-field>
                        <span>Danh mục *</span><select name="category" required><c:forEach var="item" items="${policyCategories}"><option value="${item}" ${policy.category == item ? 'selected' : ''}><c:out value="${item.label}" /></option></c:forEach></select>
                        <small data-policy-error><c:out value="${validationErrors.category}" /></small>
                    </label>
                    <label class="policy-field ${not empty validationErrors.effectiveFrom ? 'invalid' : ''}" data-policy-field>
                        <span>Ngày bắt đầu</span><input type="date" name="effectiveFrom" value="${not empty effectiveFromValue ? fn:escapeXml(effectiveFromValue) : policy.effectiveFrom}">
                        <small data-policy-error><c:out value="${validationErrors.effectiveFrom}" /></small>
                    </label>
                    <label class="policy-field ${not empty validationErrors.effectiveTo ? 'invalid' : ''}" data-policy-field>
                        <span>Ngày kết thúc</span><input type="date" name="effectiveTo" value="${not empty effectiveToValue ? fn:escapeXml(effectiveToValue) : policy.effectiveTo}">
                        <small data-policy-error><c:out value="${validationErrors.effectiveTo}" /></small>
                    </label>
                    <label class="policy-field full ${not empty validationErrors.content ? 'invalid' : ''}" data-policy-field>
                        <span>Nội dung *</span><textarea name="content" required maxlength="10000" rows="14" data-policy-content><c:out value="${policy.content}" /></textarea>
                        <span class="policy-counter" data-policy-counter>Tối đa 10000 ký tự</span>
                        <small data-policy-error><c:out value="${validationErrors.content}" /></small>
                    </label>
                </div>
                <div class="policy-form-actions"><a class="policy-button secondary" href="${listUrl}">Hủy</a><button class="policy-button primary" type="submit"><i class="fa-solid fa-floppy-disk"></i> Lưu bản nháp</button></div>
            </form>
        </section>
    </div>
</main>
<script src="${pageContext.request.contextPath}/assets/js/policy-form.js" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
