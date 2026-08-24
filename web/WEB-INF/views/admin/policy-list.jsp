<%--
    Trang danh sách điều lệ do AdminPolicyServlet hiển thị;
    Nhận policyList, bộ lọc, phân trang và flash message.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="policies" scope="request" />
<c:set var="pageTitle" value="Quản lý điều lệ – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/policy.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="listUrl" value="/admin/policies" />
<c:url var="newUrl" value="/admin/policies/new" />

<main class="page-wrapper policy-page" style="margin: 0; padding: 0;">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-scale-balanced"></i> Điều lệ
                    </div>
                    <h1 class="books-page-title">Quản lý Điều lệ</h1>
                    <p class="books-page-subtitle">
                        Soạn thảo, xuất bản và theo dõi các quy định của Thư viện FPT University
                    </p>
                </div>
                <div class="books-page-stats" aria-label="Tổng số điều lệ">
                    <div class="bps-item">
                        <span class="bps-num"><c:out value="${totalPolicies}" /></span>
                        <span class="bps-lbl">Điều lệ</span>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="container policy-body" style="padding-top: 28px;">
        <c:if test="${not empty flashSuccess}">
            <div class="policy-alert success"><c:out value="${flashSuccess}" /></div>
        </c:if>
        <c:if test="${not empty flashError}">
            <div class="policy-alert error"><c:out value="${flashError}" /></div>
        </c:if>

        <section class="policy-filter-card">
            <form action="${listUrl}" method="get" class="policy-filter-form">
                <label>
                    <span>Từ khóa</span>
                    <input name="keyword" maxlength="200" value="${fn:escapeXml(keyword)}" placeholder="Tiêu đề hoặc mã điều lệ">
                </label>
                <label>
                    <span>Danh mục</span>
                    <select name="category">
                        <option value="">Tất cả</option>
                        <c:forEach var="item" items="${policyCategories}">
                            <option value="${item}" ${selectedCategory == item ? 'selected' : ''}>
                                <c:out value="${item.label}" />
                            </option>
                        </c:forEach>
                    </select>
                </label>
                <label>
                    <span>Trạng thái</span>
                    <select name="status">
                        <option value="">Tất cả</option>
                        <c:forEach var="item" items="${policyStatuses}">
                            <option value="${item}" ${selectedStatus == item ? 'selected' : ''}>
                                <c:out value="${item.label}" />
                            </option>
                        </c:forEach>
                    </select>
                </label>
                <button class="policy-button primary" type="submit">
                    <i class="fa-solid fa-magnifying-glass"></i> Lọc
                </button>
            </form>
        </section>

        <div class="policy-toolbar">
            <strong><c:out value="${totalPolicies}" /> điều lệ</strong>
            <a class="policy-button primary" href="${newUrl}">
                <i class="fa-solid fa-plus"></i> Thêm điều lệ
            </a>
        </div>

        <section class="policy-table-card">
            <div class="policy-table-wrap">
                <table class="policy-table">
                    <thead>
                        <tr>
                            <th>Mã / phiên bản</th>
                            <th>Tiêu đề</th>
                            <th>Danh mục</th>
                            <th>Hiệu lực</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty policyList}">
                                <tr>
                                    <td colspan="6" class="policy-empty">Không có điều lệ phù hợp.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="policy" items="${policyList}">
                                    <tr>
                                        <td>
                                            <strong><c:out value="${policy.policyCode}" /></strong>
                                            <small>v<c:out value="${policy.version}" /></small>
                                        </td>
                                        <td>
                                            <a class="policy-title-link" href="${pageContext.request.contextPath}/admin/policies/view?id=${policy.id}">
                                                <c:out value="${policy.title}" />
                                            </a>
                                        </td>
                                        <td><c:out value="${policy.category.label}" /></td>
                                        <td>
                                            <c:out value="${empty policy.effectiveFrom ? 'Chưa đặt' : policy.effectiveFrom}" /> – 
                                            <c:out value="${empty policy.effectiveTo ? 'Không giới hạn' : policy.effectiveTo}" />
                                        </td>
                                        <td>
                                            <span class="policy-badge ${fn:toLowerCase(policy.publicationStatus)}">
                                                <c:out value="${policy.effectiveStatus}" />
                                            </span>
                                        </td>
                                        <td>
                                            <div class="policy-actions">
                                                <c:if test="${policy.publicationStatus == 'DRAFT'}">
                                                    <a class="policy-action policy-action-edit"
                                                       href="${pageContext.request.contextPath}/admin/policies/edit?id=${policy.id}"
                                                       title="Chỉnh sửa">
                                                        <i class="fa-solid fa-pen"></i> Sửa
                                                    </a>
                                                    <form method="post"
                                                          action="${pageContext.request.contextPath}/admin/policies/publish"
                                                          data-confirm-action
                                                          data-confirm-message="Xuất bản điều lệ này?">
                                                        <input type="hidden" name="id" value="${policy.id}">
                                                        <button class="policy-action policy-action-publish"
                                                                type="submit"
                                                                title="Xuất bản">
                                                            <i class="fa-solid fa-circle-check"></i> Xuất bản
                                                        </button>
                                                    </form>
                                                    <form method="post"
                                                          action="${pageContext.request.contextPath}/admin/policies/delete"
                                                          data-confirm-action
                                                          data-confirm-message="Xóa bản nháp này?">
                                                        <input type="hidden" name="id" value="${policy.id}">
                                                        <button class="policy-action policy-action-delete"
                                                                type="submit"
                                                                title="Xóa bản nháp">
                                                            <i class="fa-solid fa-trash"></i> Xóa
                                                        </button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${policy.publicationStatus == 'PUBLISHED'}">
                                                    <a class="policy-action policy-action-revise"
                                                       href="${pageContext.request.contextPath}/admin/policies/revise?id=${policy.id}"
                                                       title="Tạo phiên bản mới">
                                                        <i class="fa-solid fa-code-branch"></i> Bản mới
                                                    </a>
                                                    <form method="post"
                                                          action="${pageContext.request.contextPath}/admin/policies/archive"
                                                          data-confirm-action
                                                          data-confirm-message="Lưu trữ điều lệ này?">
                                                        <input type="hidden" name="id" value="${policy.id}">
                                                        <button class="policy-action policy-action-archive"
                                                                type="submit"
                                                                title="Lưu trữ">
                                                            <i class="fa-solid fa-box-archive"></i> Lưu trữ
                                                        </button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${policy.reusable}">
                                                    <form method="post"
                                                          action="${pageContext.request.contextPath}/admin/policies/reuse"
                                                          data-confirm-action
                                                          data-confirm-message="Sử dụng lại điều lệ này theo khoảng hiệu lực đã thiết lập?">
                                                        <input type="hidden" name="id" value="${policy.id}">
                                                        <button class="policy-action policy-action-reuse"
                                                                type="submit"
                                                                title="Sử dụng lại">
                                                            <i class="fa-solid fa-rotate-left"></i> Sử dụng lại
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <c:if test="${totalPages > 1}">
            <nav class="policy-pagination" aria-label="Phân trang điều lệ">
                <c:forEach begin="1" end="${totalPages}" var="pageNumber">
                    <c:url var="pageUrl" value="/admin/policies">
                        <c:param name="keyword" value="${keyword}" />
                        <c:param name="category" value="${selectedCategory}" />
                        <c:param name="status" value="${selectedStatus}" />
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

<script src="${pageContext.request.contextPath}/assets/js/policy-actions.js" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
