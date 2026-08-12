<%--
    Fragment hiển thị sidebar quản trị được header.jsp nhúng vào giao diện quản trị.
    Mong đợi request attribute `activePage` hoặc `currentPage` để đánh dấu mục hiện tại;
    session attribute `loggedUser` chứa người dùng đã được controller xác thực và phân quyền.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="sidebarCurrentPage" value="${not empty activePage ? activePage : currentPage}" />
<c:set var="sidebarUser" value="${sessionScope.loggedUser}" />

<c:url var="sidebarHomeUrl" value="/home" />
<c:url var="sidebarProfileUrl" value="/user/profile" />
<c:url var="sidebarBorrowUrl" value="/borrow/list" />
<c:url var="sidebarReservationUrl" value="/reservation/list" />
<c:url var="sidebarFineUrl" value="/fine/list" />
<c:url var="sidebarNotificationUrl" value="/notification/manage" />
<c:url var="sidebarShelfUrl" value="/shelf" />
<c:url var="sidebarBooksUrl" value="/books" />
<c:url var="sidebarAuthorsUrl" value="/authors" />
<c:url var="sidebarCategoriesUrl" value="/admin/categories" />
<c:url var="sidebarUsersUrl" value="/users" />
<c:url var="sidebarLogoutUrl" value="/logout" />
<c:url var="sidebarLibraryStatsUrl" value="/dashboard/library" />
<c:url var="sidebarAdminStatsUrl" value="/dashboard/admin" />

<a href="${sidebarHomeUrl}" class="admin-sidebar-brand">
    <span class="brand-icon">
        <i class="fa-solid fa-graduation-cap"></i>
    </span>
    <span>
        <span class="brand-title">FPT Library</span>
        <span class="brand-subtitle">Hệ thống quản trị</span>
    </span>
</a>

<c:if test="${not empty sidebarUser}">
    <a href="${sidebarProfileUrl}" class="admin-sidebar-usercard" title="Xem hồ sơ cá nhân">
        <span class="admin-sidebar-usercard-avatar">
            <i class="fa-solid fa-user-gear"></i>
        </span>
        <span class="admin-sidebar-usercard-info">
            <span class="admin-sidebar-usercard-name">
                <c:out value="${not empty sidebarUser.fullName
                    ? sidebarUser.fullName : sidebarUser.username}" />
            </span>
            <span class="admin-sidebar-usercard-role ${fn:toLowerCase(sidebarUser.role)}">
                <c:out value="${sidebarUser.role}" />
            </span>
        </span>
    </a>
</c:if>

<div class="admin-sidebar-nav-container">
    <div class="admin-nav-group">
        <div class="admin-nav-header">Nghiệp vụ</div>
        <nav class="admin-nav" aria-label="Điều hướng nghiệp vụ">
            <a href="${sidebarBorrowUrl}"
               class="admin-nav-item ${sidebarCurrentPage eq 'borrows' ? 'active' : ''}">
                <i class="fa-solid fa-hand-holding-hand"></i>
                <span>Mượn trả sách</span>
            </a>
            <a href="${sidebarReservationUrl}"
               class="admin-nav-item ${sidebarCurrentPage eq 'reservations' ? 'active' : ''}">
                <i class="fa-solid fa-list-check"></i>
                <span>Đặt trước sách</span>
            </a>
            <a href="${sidebarFineUrl}"
               class="admin-nav-item ${sidebarCurrentPage eq 'fines' ? 'active' : ''}">
                <i class="fa-solid fa-file-invoice-dollar"></i>
                <span>Quản lý phạt</span>
            </a>
            <a href="${sidebarNotificationUrl}"
               class="admin-nav-item ${sidebarCurrentPage eq 'notifications' ? 'active' : ''}">
                <i class="fa-solid fa-bullhorn"></i>
                <span>Gửi thông báo</span>
            </a>
        </nav>
    </div>

    <div class="admin-nav-group">
        <div class="admin-nav-header">Báo cáo &amp; Thống kê</div>
        <nav class="admin-nav" aria-label="Báo cáo thống kê">
            <a href="${sidebarLibraryStatsUrl}"
               class="admin-nav-item ${sidebarCurrentPage eq 'dashboard-library' ? 'active' : ''}">
                <i class="fa-solid fa-chart-pie"></i>
                <span>Thống kê Thư viện</span>
            </a>
            <c:if test="${sidebarUser.admin}">
                <a href="${sidebarAdminStatsUrl}"
                   class="admin-nav-item ${sidebarCurrentPage eq 'dashboard-admin' ? 'active' : ''}">
                    <i class="fa-solid fa-chart-line"></i>
                    <span>Thống kê Admin</span>
                </a>
            </c:if>
        </nav>
    </div>

    <div class="admin-nav-group">
        <div class="admin-nav-header">Dữ liệu &amp; Kho</div>
        <nav class="admin-nav" aria-label="Điều hướng dữ liệu và kho">
            <a href="${sidebarShelfUrl}"
               class="admin-nav-item ${sidebarCurrentPage eq 'shelf' ? 'active' : ''}">
                <i class="fa-solid fa-layer-group"></i>
                <span>Vị trí kệ sách</span>
            </a>
            <a href="${sidebarBooksUrl}"
               class="admin-nav-item ${sidebarCurrentPage eq 'books' ? 'active' : ''}">
                <i class="fa-solid fa-book"></i>
                <span>Kho sách</span>
            </a>

            <c:if test="${sidebarUser.admin}">
                <a href="${sidebarAuthorsUrl}"
                   class="admin-nav-item ${sidebarCurrentPage eq 'authors' ? 'active' : ''}">
                    <i class="fa-solid fa-user-pen"></i>
                    <span>Quản lý tác giả</span>
                </a>
                <a href="${sidebarCategoriesUrl}"
                   class="admin-nav-item ${sidebarCurrentPage eq 'categories' ? 'active' : ''}">
                    <i class="fa-solid fa-tags"></i>
                    <span>Quản lý danh mục</span>
                </a>
            </c:if>
        </nav>
    </div>

    <c:if test="${sidebarUser.admin}">
        <div class="admin-nav-group">
            <div class="admin-nav-header">Hệ thống</div>
            <nav class="admin-nav" aria-label="Điều hướng hệ thống">
                <a href="${sidebarUsersUrl}"
                   class="admin-nav-item ${sidebarCurrentPage eq 'users' ? 'active' : ''}">
                    <i class="fa-solid fa-users-gear"></i>
                    <span>Quản lý tài khoản</span>
                </a>
            </nav>
        </div>
    </c:if>
</div>

<div class="admin-sidebar-footer">
    <a href="${sidebarHomeUrl}" class="admin-sidebar-footer-btn">
        <i class="fa-solid fa-globe"></i>
        <span>Vào Trang chủ</span>
    </a>
    <form class="admin-sidebar-logout-form" action="${sidebarLogoutUrl}" method="post">
        <button class="admin-sidebar-footer-btn logout-btn" type="submit">
            <i class="fa-solid fa-right-from-bracket"></i>
            <span>Đăng xuất</span>
        </button>
    </form>
</div>
