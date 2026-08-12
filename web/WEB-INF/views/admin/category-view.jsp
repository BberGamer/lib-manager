<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="categories" scope="request" />
<c:set var="pageTitle" value="Chi tiết thể loại – FPT Library" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="page-wrapper">
    <div class="container" style="padding-top: 30px; padding-bottom: 50px; max-width: 680px;">
        <!-- Back button -->
        <div style="margin-bottom: 20px;">
            <a href="${pageContext.request.contextPath}/admin/categories"
               style="font-size: 0.88rem; color: var(--text-secondary); text-decoration: none; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">
                <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách thể loại
            </a>
        </div>

        <!-- Page Header -->
        <div style="margin-bottom: 28px;">
            <h1 class="section-title"><i class="fa-solid fa-tag"></i> Chi tiết thể loại</h1>
            <p class="section-subtitle">Thông tin đầy đủ về thể loại sách này trong hệ thống</p>
        </div>

        <!-- Detail Card -->
        <div style="background: white; border-radius: 14px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); border: 1px solid #eef2f6; overflow: hidden; margin-bottom: 20px;">
            <!-- Card Header with color icon -->
            <div style="padding: 20px 24px; background: linear-gradient(135deg, #667eea15, #764ba210); border-bottom: 1px solid #eef2f6; display: flex; align-items: center; gap: 14px;">
                <div style="width: 48px; height: 48px; border-radius: 12px; background: linear-gradient(135deg, #667eea, #764ba2); display: flex; align-items: center; justify-content: center; flex-shrink: 0;">
                    <i class="fa-solid fa-tag" style="color: white; font-size: 1.2rem;"></i>
                </div>
                <div>
                    <h2 style="margin: 0; font-size: 1.2rem; font-weight: 700; color: var(--text-primary);">
                        <c:out value="${category.name}" />
                    </h2>
                    <span style="font-size: 0.8rem; color: var(--text-muted);">ID #<c:out value="${category.id}" /></span>
                </div>
            </div>

            <!-- Detail rows -->
            <div style="padding: 8px 0;">
                <div style="display: flex; padding: 14px 24px; border-bottom: 1px solid #f8f9fa; align-items: flex-start; gap: 20px;">
                    <div style="width: 140px; flex-shrink: 0; font-size: 0.85rem; font-weight: 600; color: var(--text-secondary);">Mã thể loại</div>
                    <div style="font-size: 0.9rem; color: var(--text-primary); font-weight: 600;">#<c:out value="${category.id}" /></div>
                </div>
                <div style="display: flex; padding: 14px 24px; border-bottom: 1px solid #f8f9fa; align-items: flex-start; gap: 20px;">
                    <div style="width: 140px; flex-shrink: 0; font-size: 0.85rem; font-weight: 600; color: var(--text-secondary);">Tên thể loại</div>
                    <div style="font-size: 0.9rem; color: var(--text-primary); font-weight: 700;"><c:out value="${category.name}" /></div>
                </div>
                <div style="display: flex; padding: 14px 24px; border-bottom: 1px solid #f8f9fa; align-items: flex-start; gap: 20px;">
                    <div style="width: 140px; flex-shrink: 0; font-size: 0.85rem; font-weight: 600; color: var(--text-secondary);">Mô tả</div>
                    <div style="font-size: 0.9rem; color: var(--text-secondary); line-height: 1.6;">
                        <c:choose>
                            <c:when test="${not empty category.description}">
                                <c:out value="${category.description}" />
                            </c:when>
                            <c:otherwise>
                                <span style="color: #ccc; font-style: italic;">Chưa có mô tả</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div style="display: flex; padding: 14px 24px; border-bottom: 1px solid #f8f9fa; align-items: center; gap: 20px;">
                    <div style="width: 140px; flex-shrink: 0; font-size: 0.85rem; font-weight: 600; color: var(--text-secondary);">Người tạo</div>
                    <div style="font-size: 0.9rem; color: var(--text-primary);">
                        <c:out value="${empty category.createdBy ? '—' : category.createdBy}" />
                    </div>
                </div>
                <div style="display: flex; padding: 14px 24px; border-bottom: 1px solid #f8f9fa; align-items: center; gap: 20px;">
                    <div style="width: 140px; flex-shrink: 0; font-size: 0.85rem; font-weight: 600; color: var(--text-secondary);">Ngày tạo</div>
                    <div style="font-size: 0.9rem; color: var(--text-primary);"><c:out value="${category.createdAt}" /></div>
                </div>
                <div style="display: flex; padding: 14px 24px; border-bottom: 1px solid #f8f9fa; align-items: center; gap: 20px;">
                    <div style="width: 140px; flex-shrink: 0; font-size: 0.85rem; font-weight: 600; color: var(--text-secondary);">Cập nhật bởi</div>
                    <div style="font-size: 0.9rem; color: var(--text-primary);">
                        <c:out value="${empty category.updatedBy ? '—' : category.updatedBy}" />
                    </div>
                </div>
                <div style="display: flex; padding: 14px 24px; align-items: center; gap: 20px;">
                    <div style="width: 140px; flex-shrink: 0; font-size: 0.85rem; font-weight: 600; color: var(--text-secondary);">Cập nhật lần cuối</div>
                    <div style="font-size: 0.9rem; color: var(--text-primary);"><c:out value="${category.updatedAt}" /></div>
                </div>
            </div>
        </div>

        <!-- Action buttons -->
        <div style="display: flex; gap: 12px;">
            <a href="${pageContext.request.contextPath}/admin/categories"
               style="padding: 10px 20px; border-radius: 8px; background: #f1f4f8; color: var(--text-secondary); font-weight: 600; text-decoration: none; font-size: 0.9rem; border: 1px solid #e2e8f0; display: inline-flex; align-items: center; gap: 7px;">
                <i class="fa-solid fa-list"></i> Tất cả thể loại
            </a>
            <c:url var="editUrl" value="/admin/categories/edit">
                <c:param name="id" value="${category.id}" />
            </c:url>
            <a href="${editUrl}"
               class="btn btn-primary"
               style="padding: 10px 22px; border-radius: 8px; font-weight: 600; text-decoration: none; font-size: 0.9rem; display: inline-flex; align-items: center; gap: 7px;">
                <i class="fa-solid fa-pen"></i> Chỉnh sửa thể loại này
            </a>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
