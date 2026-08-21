<%--
    Fragment hiển thị phần đầu trang và điều hướng dùng chung cho các trang do controller chuyển tiếp đến JSP.
    Mong đợi request attributes `pageTitle`, `pageDesc`, `activePage` hoặc `currentPage`,
    `isManagePageAttr`, `headerUnreadCount`; session attribute `loggedUser` chứa người dùng đăng nhập.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="navCurrentPage" value="${not empty activePage ? activePage : currentPage}" />
<c:set var="resolvedPageTitle"
       value="${not empty pageTitle ? pageTitle : 'Thư Viện FPT University'}" />
<c:set var="resolvedPageDescription"
       value="${not empty pageDesc
           ? pageDesc
           : 'Hệ thống thư viện điện tử FPT University – Khám phá kho tàng tri thức'}" />
<c:set var="navUser" value="${sessionScope.loggedUser}" />

<c:url var="styleUrl" value="/assets/css/style.css" />
<c:url var="customStyleUrl" value="/assets/css/custom.css" />
<c:url var="headerScriptUrl" value="/assets/js/header.js" />
<c:url var="logoUrl" value="/assets/images/logo.png" />
<c:url var="homeUrl" value="/home" />
<c:url var="booksUrl" value="/books" />
<c:url var="aboutUrl" value="/about" />
<c:url var="policiesPublicUrl" value="/policies" />
<c:url var="eventsUrl" value="/events" />
<c:url var="myBorrowsUrl" value="/borrow/my" />
<c:url var="myReservationsUrl" value="/reservation/my" />
<c:url var="myFinesUrl" value="/fine/my" />
<c:url var="myNotificationsUrl" value="/notification/my" />
<c:url var="readerFoundItemsUrl" value="/found-items" />
<c:set var="rolePath" value="${navUser.admin ? '/admin' : '/librarian'}" />
<c:url var="borrowManagementUrl" value="${rolePath}/borrow/list" />
<c:url var="reservationManagementUrl" value="${rolePath}/reservation/list" />
<c:url var="fineManagementUrl" value="${rolePath}/fine/list" />
<c:url var="notificationManagementUrl" value="${rolePath}/notification/manage" />
<c:url var="foundItemsUrl" value="/librarian/found-items" />
<c:url var="shelfManagementUrl" value="${rolePath}/shelf" />
<c:url var="authorsUrl" value="${rolePath}/authors" />
<c:url var="categoriesUrl" value="${rolePath}/categories" />
<c:url var="policiesAdminUrl" value="/admin/policies" />
<c:url var="usersUrl" value="${rolePath}/users" />
<c:url var="auditLogsUrl" value="/admin/audit-logs" />
<c:url var="profileUrl" value="${navUser.admin ? '/admin/user/profile' : (navUser.librarian ? '/librarian/user/profile' : '/user/profile')}" />
<c:url var="loginUrl" value="/login" />
<c:url var="logoutUrl" value="/logout" />
<c:url var="libraryStatsUrl" value="${rolePath}/dashboard/library" />
<c:url var="adminStatsUrl" value="/admin" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${fn:escapeXml(resolvedPageTitle)}</title>
    <meta name="description" content="${fn:escapeXml(resolvedPageDescription)}">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&amp;family=Be+Vietnam+Pro:wght@600;700;800&amp;display=swap"
          rel="stylesheet">
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${styleUrl}">
    <link rel="stylesheet" href="${customStyleUrl}">
    <c:if test="${not empty pageStylesheet}">
        <c:url var="pageStylesheetUrl" value="${pageStylesheet}" />
        <link rel="stylesheet" href="${pageStylesheetUrl}">
    </c:if>
    <script src="${headerScriptUrl}" defer></script>
</head>
<body>

<c:choose>
    <c:when test="${isManagePageAttr}">
        <header class="admin-mobile-header">
            <button class="admin-mobile-toggle" type="button" data-sidebar-toggle
                    aria-label="Mở menu quản trị" aria-expanded="false">
                <i class="fa-solid fa-bars"></i>
            </button>
            <a href="${navUser.admin ? adminStatsUrl : (navUser.librarian ? libraryStatsUrl : homeUrl)}" class="admin-mobile-brand">
                <img src="${logoUrl}" alt="FPT Logo">
                <span>FPT Library</span>
            </a>
            <div class="admin-mobile-spacer" aria-hidden="true"></div>
        </header>

        <div class="admin-sidebar-backdrop" data-sidebar-backdrop></div>
        <div class="admin-shell">
            <aside class="admin-sidebar" data-admin-sidebar>
                <script>
                    if (localStorage.getItem('sidebar-collapsed') === 'true') {
                        document.currentScript.parentElement.classList.add('collapsed');
                    }
                </script>
                <jsp:include page="/WEB-INF/views/fragments/sidebar.jsp" />
            </aside>
            <div class="admin-main">
    </c:when>
    <c:otherwise>
        <nav class="navbar" data-main-navbar>
            <div class="container">
                <div class="navbar-inner">
                    <a href="${homeUrl}" class="navbar-brand">
                        <img class="navbar-logo" src="${logoUrl}" alt="FPT University">
                        <span class="brand-accent">FPT Library</span>
                    </a>

                    <ul class="navbar-nav">
                        <li class="nav-item">
                            <a href="${homeUrl}"
                               class="nav-link ${navCurrentPage eq 'home' ? 'active' : ''}">
                                <i class="fa-solid fa-house"></i> Trang chủ
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="${booksUrl}"
                               class="nav-link ${navCurrentPage eq 'books' ? 'active' : ''}">
                                <i class="fa-solid fa-book"></i> Danh sách sách
                            </a>
                        </li>

                        <c:if test="${not empty navUser and not navUser.adminOrLibrarian}">
                            <li class="nav-item dropdown">
                                <a href="#" class="nav-link ${navCurrentPage eq 'borrows' or navCurrentPage eq 'reservations' ? 'active' : ''}">
                                    <i class="fa-solid fa-book-bookmark"></i> Tủ sách của tôi
                                    <i class="fa-solid fa-chevron-down fa-xs nav-chevron"></i>
                                </a>
                                <ul class="dropdown-menu">
                                    <li><a href="${myBorrowsUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'borrows' ? 'active' : ''}">
                                            <i class="fa-solid fa-book-open"></i> Đang mượn & Lịch sử
                                        </a></li>
                                    <li><a href="${myReservationsUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'reservations' ? 'active' : ''}">
                                            <i class="fa-solid fa-bookmark"></i> Hàng chờ đặt trước
                                        </a></li>
                                </ul>
                            </li>
                        </c:if>

                        <c:if test="${empty navUser or not navUser.adminOrLibrarian}">
                            <li class="nav-item dropdown">
                                <a href="#" class="nav-link ${navCurrentPage eq 'events'
                                        or navCurrentPage eq 'policies'
                                        or navCurrentPage eq 'about'
                                        or navCurrentPage eq 'found-items'
                                        or navCurrentPage eq 'fines' ? 'active' : ''}">
                                    <i class="fa-solid fa-circle-info"></i> Thông tin & Tiện ích
                                    <i class="fa-solid fa-chevron-down fa-xs nav-chevron"></i>
                                </a>
                                <ul class="dropdown-menu">
                                    <li><a href="${eventsUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'events' ? 'active' : ''}">
                                            <i class="fa-solid fa-calendar-days"></i> Sự kiện
                                        </a></li>
                                    <li><a href="${policiesPublicUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'policies' ? 'active' : ''}">
                                            <i class="fa-solid fa-scale-balanced"></i> Điều lệ & Quy định
                                        </a></li>
                                    <li><a href="${aboutUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'about' ? 'active' : ''}">
                                            <i class="fa-solid fa-circle-info"></i> Giới thiệu
                                        </a></li>
                                    <li><a href="${readerFoundItemsUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'found-items' ? 'active' : ''}">
                                            <i class="fa-solid fa-box-open"></i> Đồ để quên
                                        </a></li>
                                    <c:if test="${not empty navUser and not navUser.adminOrLibrarian}">
                                        <li class="dropdown-divider"></li>
                                        <li><a href="${myFinesUrl}"
                                               class="dropdown-item ${navCurrentPage eq 'fines' ? 'active' : ''}">
                                                <i class="fa-solid fa-coins"></i> Khoản phạt của tôi
                                            </a></li>
                                    </c:if>
                                </ul>
                            </li>
                        </c:if>

                        <c:if test="${not empty navUser and navUser.adminOrLibrarian}">
                            <c:set var="isManageActive"
                                   value="${navCurrentPage eq 'borrows'
                                       or navCurrentPage eq 'reservations'
                                       or navCurrentPage eq 'fines'
                                       or navCurrentPage eq 'notifications'
                                       or navCurrentPage eq 'found-items'
                                       or navCurrentPage eq 'shelf'
                                       or navCurrentPage eq 'authors'
                                       or navCurrentPage eq 'categories'
                                       or navCurrentPage eq 'policies'
                                       or navCurrentPage eq 'users'
                                       or navCurrentPage eq 'audit-logs'
                                       or navCurrentPage eq 'dashboard-library'
                                       or navCurrentPage eq 'dashboard-admin'}" />
                            <li class="nav-item dropdown">
                                <a href="#" class="nav-link ${isManageActive ? 'active' : ''}">
                                    <i class="fa-solid fa-briefcase"></i> Quản lý
                                    <i class="fa-solid fa-chevron-down fa-xs nav-chevron"></i>
                                </a>
                                <ul class="dropdown-menu">
                                    <li><a href="${borrowManagementUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'borrows' ? 'active' : ''}">
                                            <i class="fa-solid fa-hand-holding-hand"></i> Mượn sách
                                        </a></li>
                                    <li><a href="${reservationManagementUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'reservations' ? 'active' : ''}">
                                            <i class="fa-solid fa-list-check"></i> Đặt trước
                                        </a></li>
                                    <li><a href="${fineManagementUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'fines' ? 'active' : ''}">
                                            <i class="fa-solid fa-file-invoice-dollar"></i> Phạt
                                        </a></li>
                                    <li><a href="${notificationManagementUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'notifications' ? 'active' : ''}">
                                            <i class="fa-solid fa-bullhorn"></i> Thông báo
                                        </a></li>
                                    <c:if test="${navUser.librarian}">
                                        <li><a href="${foundItemsUrl}"
                                               class="dropdown-item ${navCurrentPage eq 'found-items' ? 'active' : ''}">
                                                <i class="fa-solid fa-box-open"></i> Đồ để quên
                                            </a></li>
                                    </c:if>
                                    <li class="dropdown-divider"></li>
                                    <li><a href="${shelfManagementUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'shelf' ? 'active' : ''}">
                                            <i class="fa-solid fa-layer-group"></i> Vị trí kệ
                                        </a></li>
                                    <c:if test="${navUser.admin}">
                                        <li><a href="${authorsUrl}"
                                               class="dropdown-item ${navCurrentPage eq 'authors' ? 'active' : ''}">
                                                <i class="fa-solid fa-user-pen"></i> Tác giả
                                            </a></li>
                                        <li><a href="${categoriesUrl}"
                                               class="dropdown-item ${navCurrentPage eq 'categories' ? 'active' : ''}">
                                                <i class="fa-solid fa-tags"></i> Danh mục
                                            </a></li>
                                        <li><a href="${policiesAdminUrl}"
                                               class="dropdown-item ${navCurrentPage eq 'policies' ? 'active' : ''}">
                                                <i class="fa-solid fa-scale-balanced"></i> Điều lệ
                                            </a></li>
                                        <li class="dropdown-divider"></li>
                                        <li><a href="${usersUrl}"
                                               class="dropdown-item ${navCurrentPage eq 'users' ? 'active' : ''}">
                                                <i class="fa-solid fa-user-gear"></i> Tài khoản
                                            </a></li>
                                        <li><a href="${auditLogsUrl}"
                                               class="dropdown-item ${navCurrentPage eq 'audit-logs' ? 'active' : ''}">
                                                <i class="fa-solid fa-shield-halved"></i> Nhật ký Audit Log
                                            </a></li>
                                    </c:if>
                                    <li class="dropdown-divider"></li>
                                    <li><a href="${libraryStatsUrl}"
                                           class="dropdown-item ${navCurrentPage eq 'dashboard-library' ? 'active' : ''}">
                                            <i class="fa-solid fa-chart-pie"></i> Thống kê Thư viện
                                        </a></li>
                                    <c:if test="${navUser.admin}">
                                        <li><a href="${adminStatsUrl}"
                                               class="dropdown-item ${navCurrentPage eq 'dashboard-admin' ? 'active' : ''}">
                                                <i class="fa-solid fa-chart-line"></i> Thống kê Admin
                                            </a></li>
                                    </c:if>
                                </ul>
                            </li>
                        </c:if>
                    </ul>

                    <div class="navbar-actions">
                        <c:choose>
                            <c:when test="${not empty navUser}">
                                <a href="${myNotificationsUrl}" class="header-notif-btn ${navCurrentPage eq 'notifications' ? 'active' : ''}" title="Thông báo của tôi">
                                    <i class="fa-solid fa-bell"></i>
                                    <c:if test="${headerUnreadCount gt 0}">
                                        <span class="header-notif-badge"><c:out value="${headerUnreadCount}" /></span>
                                    </c:if>
                                </a>

                                <a href="${profileUrl}" class="user-info" title="Hồ sơ cá nhân">
                                    <i class="fa-solid fa-circle-user fa-xl"></i>
                                    <span class="user-info-text">
                                        <span class="user-info-name">
                                            <c:out value="${not empty navUser.fullName
                                                ? navUser.fullName : navUser.username}" />
                                        </span>
                                        <span class="user-role-badge ${fn:toLowerCase(navUser.role)}">
                                            <c:out value="${navUser.role}" />
                                        </span>
                                    </span>
                                </a>
                                <form class="logout-form" action="${logoutUrl}" method="post">
                                    <button class="btn btn-outline btn-sm btn-logout" type="submit"
                                            title="Đăng xuất" aria-label="Đăng xuất">
                                        <i class="fa-solid fa-right-from-bracket"></i>
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <a href="${loginUrl}" class="btn-login">
                                    <i class="fa-solid fa-right-to-bracket"></i> Đăng nhập
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </nav>
    </c:otherwise>
</c:choose>
