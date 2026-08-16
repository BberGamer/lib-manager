<%--
    Biểu mẫu kệ do ShelfManagementServlet hiển thị.
    Nhận shelf, formMode, rolePath và validationErrors.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="shelf" scope="request" />
<c:set var="pageTitle"
       value="${formMode eq 'create' ? 'Thêm' : 'Cập nhật'} kệ sách – FPT Library"
       scope="request" />
<c:set var="pageStylesheet" value="/assets/css/shelf-map.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="shelfListUrl" value="${rolePath}/shelf" />
<c:set var="formAction"
       value="${pageContext.request.contextPath}${rolePath}/shelf/${formMode eq 'create' ? 'create' : 'update'}" />

<main class="shelf-page">
    <div class="shelf-content narrow">
        <a class="back" href="${shelfListUrl}">
            <i class="fa-solid fa-arrow-left"></i>&nbsp; Quay lại danh sách
        </a>

        <section class="shelf-form-card">
            <h1>${formMode eq 'create' ? 'Thêm kệ sách' : 'Cập nhật kệ sách'}</h1>

            <form method="post" action="${formAction}">
                <c:if test="${formMode eq 'update'}">
                    <input type="hidden" name="id" value="${shelf.id}">
                </c:if>

                <div class="form-grid">
                    <label>
                        Mã kệ *
                        <input name="code" required maxlength="20"
                               value="${fn:escapeXml(shelf.code)}">
                        <small><c:out value="${validationErrors.code}" /></small>
                    </label>

                    <label>
                        Tên kệ *
                        <input name="name" required maxlength="100"
                               value="${fn:escapeXml(shelf.name)}">
                        <small><c:out value="${validationErrors.name}" /></small>
                    </label>

                    <label>
                        Khu vực *
                        <input name="area" required maxlength="50"
                               value="${fn:escapeXml(shelf.area)}">
                        <small><c:out value="${validationErrors.area}" /></small>
                    </label>

                    <label>
                        Tầng *
                        <input type="number" name="floorNumber" required min="1" max="100"
                               value="${shelf.floorNumber gt 0 ? shelf.floorNumber : 1}">
                        <small><c:out value="${validationErrors.floorNumber}" /></small>
                    </label>

                    <label>
                        Sức chứa *
                        <input type="number" name="capacity" required min="1"
                               value="${shelf.capacity gt 0 ? shelf.capacity : 1}">
                        <small><c:out value="${validationErrors.capacity}" /></small>
                    </label>

                    <label>
                        Trạng thái *
                        <select name="status">
                            <option value="ACTIVE" ${shelf.status ne 'INACTIVE' ? 'selected' : ''}>
                                Đang sử dụng
                            </option>
                            <option value="INACTIVE" ${shelf.status eq 'INACTIVE' ? 'selected' : ''}>
                                Ngừng sử dụng
                            </option>
                        </select>
                        <small><c:out value="${validationErrors.status}" /></small>
                    </label>
                </div>

                <label>
                    Mô tả
                    <textarea name="description" maxlength="500" rows="4"><c:out
                            value="${shelf.description}" /></textarea>
                </label>

                <div class="form-actions">
                    <a class="btn btn-outline" href="${shelfListUrl}">Hủy</a>
                    <button class="btn btn-primary" type="submit">
                        ${formMode eq 'create' ? 'Thêm kệ sách' : 'Lưu thay đổi'}
                    </button>
                </div>
            </form>
        </section>
    </div>
</main>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
