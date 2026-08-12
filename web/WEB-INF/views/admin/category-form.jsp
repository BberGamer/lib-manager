<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="categories" scope="request" />
<c:set var="pageTitle" value="${formMode == 'create' ? 'Thêm thể loại' : 'Cập nhật thể loại'} – FPT Library" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="page-wrapper">
    <div class="container" style="padding-top: 30px; padding-bottom: 50px; max-width: 680px;">
        <!-- Breadcrumb / Back button -->
        <div style="margin-bottom: 20px;">
            <a href="${pageContext.request.contextPath}/admin/categories"
               style="font-size: 0.88rem; color: var(--text-secondary); text-decoration: none; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách thể loại
            </a>
        </div>

        <!-- Page Header -->
        <div style="margin-bottom: 28px;">
            <h1 class="section-title">
                <i class="fa-solid fa-${formMode == 'create' ? 'plus-circle' : 'pen-to-square'}"></i>
                ${formMode == 'create' ? 'Thêm thể loại mới' : 'Cập nhật thể loại'}
            </h1>
            <p class="section-subtitle">
                ${formMode == 'create'
                    ? 'Điền thông tin để tạo thể loại sách mới trong hệ thống'
                    : 'Chỉnh sửa thông tin thể loại sách hiện có'}
            </p>
        </div>

        <!-- Form Card -->
        <div style="background: white; border-radius: 14px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); border: 1px solid #eef2f6; overflow: hidden;">
            <div style="padding: 16px 24px; border-bottom: 1px solid #f1f4f8; display: flex; align-items: center; gap: 10px; background: #fafbff;">
                <i class="fa-solid fa-tag" style="color: var(--text-brand);"></i>
                <span style="font-weight: 700; color: var(--text-primary); font-size: 0.95rem;">
                    ${formMode == 'create' ? 'Thông tin thể loại mới' : 'Chỉnh sửa: '.concat(category.name)}
                </span>
            </div>

            <c:set var="formAction" value="${formMode == 'create' ? '/admin/categories/create' : '/admin/categories/update'}" />
            <form action="${pageContext.request.contextPath}${formAction}" method="post"
                  style="padding: 28px 24px;">
                <c:if test="${formMode == 'update'}">
                    <input type="hidden" name="id" value="${category.id}">
                </c:if>

                <!-- Name field -->
                <div style="margin-bottom: 22px;">
                    <label for="category-name"
                           style="display: block; font-size: 0.875rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">
                        Tên thể loại <span style="color: #e74c3c;">*</span>
                    </label>
                    <input id="category-name" name="name" type="text" required maxlength="100"
                           value="${fn:escapeXml(category.name)}"
                           placeholder="Ví dụ: Văn học, Khoa học, Lịch sử..."
                           style="width: 100%; padding: 11px 14px; border: 1px solid ${not empty validationErrors.name ? '#e74c3c' : '#ddd'}; border-radius: 8px; font-size: 0.9rem; box-sizing: border-box; transition: border-color 0.2s; outline: none;"
                           onfocus="this.style.borderColor='var(--text-brand)'"
                           onblur="this.style.borderColor='${not empty validationErrors.name ? '#e74c3c' : '#ddd'}'">
                    <c:if test="${not empty validationErrors.name}">
                        <p style="color: #e74c3c; font-size: 0.82rem; margin: 6px 0 0 0; display: flex; align-items: center; gap: 5px;">
                            <i class="fa-solid fa-circle-exclamation"></i>
                            <c:out value="${validationErrors.name}" />
                        </p>
                    </c:if>
                </div>

                <!-- Description field -->
                <div style="margin-bottom: 28px;">
                    <label for="category-description"
                           style="display: block; font-size: 0.875rem; font-weight: 600; margin-bottom: 8px; color: var(--text-secondary);">
                        Mô tả <span style="color: var(--text-muted); font-weight: 400;">(tuỳ chọn)</span>
                    </label>
                    <textarea id="category-description" name="description" rows="5" maxlength="500"
                              placeholder="Mô tả ngắn gọn về thể loại sách này..."
                              style="width: 100%; padding: 11px 14px; border: 1px solid ${not empty validationErrors.description ? '#e74c3c' : '#ddd'}; border-radius: 8px; font-size: 0.9rem; resize: vertical; box-sizing: border-box; font-family: inherit; line-height: 1.5; transition: border-color 0.2s; outline: none;"
                              onfocus="this.style.borderColor='var(--text-brand)'"
                              onblur="this.style.borderColor='${not empty validationErrors.description ? '#e74c3c' : '#ddd'}'"><c:out value="${category.description}" /></textarea>
                    <div style="display: flex; justify-content: space-between; margin-top: 6px;">
                        <c:if test="${not empty validationErrors.description}">
                            <p style="color: #e74c3c; font-size: 0.82rem; margin: 0; display: flex; align-items: center; gap: 5px;">
                                <i class="fa-solid fa-circle-exclamation"></i>
                                <c:out value="${validationErrors.description}" />
                            </p>
                        </c:if>
                        <span style="font-size: 0.78rem; color: var(--text-muted); margin-left: auto;" id="desc-counter">Tối đa 500 ký tự</span>
                    </div>
                </div>

                <!-- Actions -->
                <div style="display: flex; gap: 12px; justify-content: flex-end; padding-top: 20px; border-top: 1px solid #f1f4f8;">
                    <a href="${pageContext.request.contextPath}/admin/categories"
                       style="padding: 10px 20px; border-radius: 8px; background: #f1f4f8; color: var(--text-secondary); font-weight: 600; text-decoration: none; font-size: 0.9rem; border: 1px solid #e2e8f0;">
                        Hủy bỏ
                    </a>
                    <button type="submit"
                            class="btn btn-primary"
                            style="padding: 10px 24px; border-radius: 8px; font-weight: 600; font-size: 0.9rem; display: flex; align-items: center; gap: 8px;">
                        <i class="fa-solid fa-${formMode == 'create' ? 'plus' : 'floppy-disk'}"></i>
                        ${formMode == 'create' ? 'Thêm thể loại' : 'Lưu thay đổi'}
                    </button>
                </div>
            </form>
        </div>
    </div>
</main>

<script>
    // Character counter for description textarea
    const textarea = document.getElementById('category-description');
    const counter = document.getElementById('desc-counter');
    function updateCounter() {
        const remaining = 500 - textarea.value.length;
        counter.textContent = remaining + ' ký tự còn lại';
        counter.style.color = remaining < 50 ? '#e74c3c' : '';
    }
    textarea.addEventListener('input', updateCounter);
    updateCounter();
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
