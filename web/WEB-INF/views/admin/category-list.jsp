<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="categories" scope="request" />
<c:set var="pageTitle" value="Quản lý Thể loại – FPT Library" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="page-wrapper">
    <div class="container" style="padding-top: 30px; padding-bottom: 50px;">
        <!-- Page Header -->
        <div style="margin-bottom: 28px; display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 15px;">
            <div>
                <h1 class="section-title"><i class="fa-solid fa-tags"></i> Quản lý Thể loại sách</h1>
                <p class="section-subtitle">Tổ chức và phân loại các đầu sách theo thể loại, giúp độc giả tìm kiếm dễ dàng hơn</p>
            </div>
            <a href="${pageContext.request.contextPath}/admin/categories/new"
               class="btn btn-primary"
               style="padding: 10px 22px; border-radius: 8px; font-weight: 600; display: flex; align-items: center; gap: 8px; text-decoration: none;">
                <i class="fa-solid fa-plus"></i> Thêm thể loại
            </a>
        </div>

        <!-- Alerts -->
        <c:if test="${not empty flashSuccess}">
            <div style="background: #e8f8f5; border-left: 5px solid #2ecc71; color: #27ae60; padding: 15px; border-radius: 8px; margin-bottom: 24px;">
                <i class="fa-solid fa-circle-check"></i> <c:out value="${flashSuccess}" />
            </div>
        </c:if>
        <c:if test="${not empty flashError}">
            <div style="background: #fde8e7; border-left: 5px solid #e74c3c; color: #c0392b; padding: 15px; border-radius: 8px; margin-bottom: 24px;">
                <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${flashError}" />
            </div>
        </c:if>

        <!-- Table Card -->
        <div style="background: white; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.04); border: 1px solid #eef2f6; overflow: hidden;">
            <!-- Table header row with count badge -->
            <div style="padding: 18px 24px; border-bottom: 1px solid #f1f4f8; display: flex; align-items: center; gap: 12px;">
                <i class="fa-solid fa-list" style="color: var(--text-brand); font-size: 1.1rem;"></i>
                <span style="font-weight: 700; font-size: 1rem; color: var(--text-primary);">Danh sách thể loại</span>
                <c:if test="${not empty categoryList}">
                    <span style="background: #eef3ff; color: var(--text-brand); font-size: 0.78rem; font-weight: 700; padding: 2px 10px; border-radius: 20px;">
                        <c:out value="${fn:length(categoryList)}" /> thể loại
                    </span>
                </c:if>
            </div>

            <table style="width: 100%; border-collapse: collapse; text-align: left;">
                <thead>
                    <tr style="background: #f8f9fa; border-bottom: 1px solid #eee;">
                        <th style="padding: 14px 24px; font-weight: 600; color: var(--text-secondary); font-size: 0.83rem; text-transform: uppercase; letter-spacing: 0.5px; width: 60px;">ID</th>
                        <th style="padding: 14px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.83rem; text-transform: uppercase; letter-spacing: 0.5px;">Tên thể loại</th>
                        <th style="padding: 14px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.83rem; text-transform: uppercase; letter-spacing: 0.5px;">Mô tả</th>
                        <th style="padding: 14px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.83rem; text-transform: uppercase; letter-spacing: 0.5px; width: 160px;">Cập nhật lần cuối</th>
                        <th style="padding: 14px 20px; font-weight: 600; color: var(--text-secondary); font-size: 0.83rem; text-transform: uppercase; letter-spacing: 0.5px; text-align: right; width: 180px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty categoryList}">
                            <tr>
                                <td colspan="5" style="padding: 60px 20px; text-align: center; color: var(--text-muted);">
                                    <i class="fa-regular fa-folder-open" style="font-size: 3rem; margin-bottom: 14px; display: block; color: #ddd;"></i>
                                    <p style="font-weight: 600; color: var(--text-secondary); margin: 0 0 6px 0;">Chưa có thể loại nào</p>
                                    <p style="font-size: 0.88rem; margin: 0;">Bắt đầu bằng cách thêm thể loại đầu tiên.</p>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="category" items="${categoryList}" varStatus="loop">
                                <tr style="border-bottom: 1px solid #f4f6f8; transition: background 0.15s;"
                                    onmouseover="this.style.background='#fafbff'"
                                    onmouseout="this.style.background='white'">
                                    <!-- ID -->
                                    <td style="padding: 16px 24px; font-weight: 700; color: var(--text-muted); font-size: 0.9rem;">
                                        #<c:out value="${category.id}" />
                                    </td>
                                    <!-- Name -->
                                    <td style="padding: 16px 20px;">
                                        <div style="display: flex; align-items: center; gap: 10px;">
                                            <div style="width: 36px; height: 36px; border-radius: 8px; background: linear-gradient(135deg, #667eea, #764ba2); display: flex; align-items: center; justify-content: center; flex-shrink: 0;">
                                                <i class="fa-solid fa-tag" style="color: white; font-size: 0.8rem;"></i>
                                            </div>
                                            <span style="font-weight: 600; color: var(--text-primary); font-size: 0.95rem;">
                                                <c:out value="${category.name}" />
                                            </span>
                                        </div>
                                    </td>
                                    <!-- Description -->
                                    <td style="padding: 16px 20px; font-size: 0.88rem; color: var(--text-secondary); max-width: 320px;">
                                        <c:choose>
                                            <c:when test="${not empty category.description}">
                                                <c:out value="${fn:length(category.description) > 80
                                                    ? fn:substring(category.description, 0, 77) : category.description}" />
                                                <c:if test="${fn:length(category.description) > 80}">...</c:if>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: #ccc; font-style: italic;">Chưa có mô tả</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <!-- Updated At -->
                                    <td style="padding: 16px 20px; font-size: 0.83rem; color: var(--text-muted);">
                                        <c:out value="${category.updatedAt}" />
                                    </td>
                                    <!-- Actions -->
                                    <td style="padding: 16px 20px; text-align: right;">
                                        <div style="display: flex; gap: 8px; justify-content: flex-end; align-items: center;">
                                            <c:url var="viewUrl" value="/admin/categories/view">
                                                <c:param name="id" value="${category.id}" />
                                            </c:url>
                                            <c:url var="editUrl" value="/admin/categories/edit">
                                                <c:param name="id" value="${category.id}" />
                                            </c:url>
                                            <a href="${viewUrl}"
                                               style="font-size: 0.8rem; padding: 5px 12px; border-radius: 6px; background: #f1f4f8; color: var(--text-secondary); text-decoration: none; font-weight: 600; border: 1px solid #eee; display: inline-flex; align-items: center; gap: 5px;"
                                               title="Xem chi tiết">
                                                <i class="fa-solid fa-eye"></i> Xem
                                            </a>
                                            <a href="${editUrl}"
                                               style="font-size: 0.8rem; padding: 5px 12px; border-radius: 6px; background: #eef3ff; color: var(--text-brand); text-decoration: none; font-weight: 600; border: 1px solid #d0deff; display: inline-flex; align-items: center; gap: 5px;"
                                               title="Chỉnh sửa">
                                                <i class="fa-solid fa-pen"></i> Sửa
                                            </a>
                                            <form action="${pageContext.request.contextPath}/admin/categories/delete"
                                                  method="post" style="margin: 0; display: inline;"
                                                  onsubmit="return confirm('Bạn chắc chắn muốn xóa thể loại \'${fn:escapeXml(category.name)}\'? Thao tác này không thể hoàn tác.')">
                                                <input type="hidden" name="id" value="${category.id}">
                                                <button type="submit"
                                                        style="font-size: 0.8rem; padding: 5px 12px; border-radius: 6px; background: #fde8e7; color: #e74c3c; font-weight: 600; border: 1px solid #fac9c5; cursor: pointer; display: inline-flex; align-items: center; gap: 5px;"
                                                        title="Xóa thể loại">
                                                    <i class="fa-solid fa-trash"></i> Xóa
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
        <c:if test="${totalPages > 1}">
            <div style="display: flex; justify-content: center; gap: 8px; margin-top: 28px;">
                <c:forEach var="pageNumber" begin="1" end="${totalPages}">
                    <c:url var="pageUrl" value="/admin/categories">
                        <c:param name="page" value="${pageNumber}" />
                    </c:url>
                    <a href="${pageUrl}"
                       style="width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; border-radius: 50%;
                              font-weight: 600; text-decoration: none; font-size: 0.88rem; transition: all 0.2s;
                              ${pageNumber == currentPage
                                ? 'background: var(--text-brand); color: white;'
                                : 'background: white; color: var(--text-secondary); border: 1px solid #ddd;'}">
                        <c:out value="${pageNumber}" />
                    </a>
                </c:forEach>
            </div>
        </c:if>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
