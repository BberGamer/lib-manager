<%-- Fragment hiển thị sidebar quản trị được header.jsp nhúng vào giao diện quản trị. Mong đợi request attribute
    `activePage` hoặc `currentPage` để đánh dấu mục hiện tại; session attribute `loggedUser` chứa người dùng đã được
    controller xác thực và phân quyền. --%>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
            <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

                <c:set var="sidebarCurrentPage" value="${not empty activePage ? activePage : currentPage}" />
                <c:set var="sidebarUser" value="${sessionScope.loggedUser}" />

                <c:set var="rolePath" value="${sidebarUser.admin ? '/admin' : '/librarian'}" />
                <c:url var="sidebarHomeUrl" value="/home" />
                <c:url var="sidebarProfileUrl" value="${rolePath}/user/profile" />
                <c:url var="sidebarBorrowUrl" value="${rolePath}/borrow/list" />
                <c:url var="sidebarReservationUrl" value="${rolePath}/reservation/list" />
                <c:url var="sidebarFineUrl" value="${rolePath}/fine/list" />
                <c:url var="sidebarNotificationUrl" value="${rolePath}/notification/manage" />
                <c:url var="sidebarFoundItemsUrl" value="/librarian/found-items" />
                <c:url var="sidebarShelfUrl" value="${rolePath}/shelf" />
                <c:url var="sidebarBooksUrl" value="${rolePath}/books" />
                <c:url var="sidebarAuthorsUrl" value="${rolePath}/authors" />
                <c:url var="sidebarCategoriesUrl" value="${rolePath}/categories" />
                <c:url var="sidebarPoliciesUrl" value="/admin/policies" />
                <c:url var="sidebarUsersUrl" value="${rolePath}/users" />
                <c:url var="sidebarLogoutUrl" value="/logout" />
                <c:url var="sidebarLibraryStatsUrl" value="${rolePath}/dashboard/library" />
                <c:url var="sidebarAdminStatsUrl" value="/admin" />
                <c:url var="sidebarAuditLogsUrl" value="/admin/audit-logs" />
                <c:url var="sidebarLogoUrl" value="/assets/images/logo.png" />

                <div class="admin-sidebar-header-row"
                    style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; min-height: 40px;">
                    <a href="${sidebarUser.admin ? sidebarAdminStatsUrl : (sidebarUser.librarian ? sidebarLibraryStatsUrl : sidebarHomeUrl)}"
                        class="admin-sidebar-brand" style="margin-bottom: 0;">
                        <span class="brand-icon">
                            <img src="${sidebarLogoUrl}" alt="FPT University">
                        </span>
                        <span class="brand-text-wrapper">
                            <span class="brand-title">FPT Library</span>
                            <span class="brand-subtitle">Hệ thống quản trị</span>
                        </span>
                    </a>
                    <button type="button" class="sidebar-desktop-toggle-btn" id="sidebarDesktopToggle"
                        title="Thu gọn / Mở rộng Sidebar">
                        <i class="fa-solid fa-angle-left"></i>
                    </button>
                </div>

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
                    <c:choose>
                        <c:when test="${sidebarUser.librarian}">
                            <!-- LIBRARIAN SIDEBAR -->
                            <div class="admin-nav-group">
                                <div class="admin-nav-header">Báo cáo &amp; Thống kê</div>
                                <nav class="admin-nav" aria-label="Báo cáo thống kê">
                                    <a href="${sidebarLibraryStatsUrl}"
                                        class="admin-nav-item ${sidebarCurrentPage eq 'dashboard-library' ? 'active' : ''}">
                                        <i class="fa-solid fa-chart-pie"></i>
                                        <span>Thống kê Thư viện</span>
                                    </a>
                                </nav>
                            </div>

                            <div class="admin-nav-group">
                                <div class="admin-nav-header">Nghiệp vụ</div>
                                <nav class="admin-nav" aria-label="Điều hướng nghiệp vụ">
                                    <a href="${sidebarBorrowUrl}"
                                        class="admin-nav-item ${sidebarCurrentPage eq 'borrows' || sidebarCurrentPage eq 'borrow' ? 'active' : ''}">
                                        <i class="fa-solid fa-hand-holding-hand"></i>
                                        <span>Mượn trả sách</span>
                                    </a>
                                    <a href="${sidebarReservationUrl}"
                                        class="admin-nav-item ${sidebarCurrentPage eq 'reservations' || sidebarCurrentPage eq 'reservation' ? 'active' : ''}">
                                        <i class="fa-solid fa-list-check"></i>
                                        <span>Đặt trước sách</span>
                                    </a>
                                    <a href="${sidebarFineUrl}"
                                        class="admin-nav-item ${sidebarCurrentPage eq 'fines' || sidebarCurrentPage eq 'fine' ? 'active' : ''}">
                                        <i class="fa-solid fa-file-invoice-dollar"></i>
                                        <span>Quản lý phạt</span>
                                    </a>
                                    <a href="${sidebarNotificationUrl}"
                                        class="admin-nav-item ${sidebarCurrentPage eq 'notifications' ? 'active' : ''}">
                                        <i class="fa-solid fa-bullhorn"></i>
                                        <span>Gửi thông báo</span>
                                    </a>
                                    <a href="${sidebarFoundItemsUrl}"
                                        class="admin-nav-item ${sidebarCurrentPage eq 'found-items' ? 'active' : ''}">
                                        <i class="fa-solid fa-box-open"></i>
                                        <span>Đồ để quên</span>
                                    </a>
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
                                        <i class="fa-solid fa-boxes-stacked"></i>
                                        <span>Quản lý bản sao</span>
                                    </a>
                                </nav>
                            </div>
                        </c:when>

                        <c:when test="${sidebarUser.admin}">
                            <!-- ADMIN SIDEBAR -->
                            <div class="admin-nav-group">
                                <div class="admin-nav-header">Báo cáo &amp; Thống kê</div>
                                <nav class="admin-nav" aria-label="Báo cáo thống kê">
                                    <a href="${sidebarAdminStatsUrl}"
                                        class="admin-nav-item ${sidebarCurrentPage eq 'dashboard-admin' ? 'active' : ''}">
                                        <i class="fa-solid fa-chart-line"></i>
                                        <span>Thống kê Admin</span>
                                    </a>
                                    <a href="${sidebarLibraryStatsUrl}"
                                        class="admin-nav-item ${sidebarCurrentPage eq 'dashboard-library' ? 'active' : ''}">
                                        <i class="fa-solid fa-chart-pie"></i>
                                        <span>Thống kê Thư viện</span>
                                    </a>
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
                                    <a href="${sidebarPoliciesUrl}"
                                        class="admin-nav-item ${sidebarCurrentPage eq 'policies' ? 'active' : ''}">
                                        <i class="fa-solid fa-scale-balanced"></i>
                                        <span>Quản lý điều lệ</span>
                                    </a>
                                </nav>
                            </div>

                            <div class="admin-nav-group">
                                <div class="admin-nav-header">Hệ thống</div>
                                <nav class="admin-nav" aria-label="Điều hướng hệ thống">
                                    <a href="${sidebarUsersUrl}"
                                        class="admin-nav-item ${sidebarCurrentPage eq 'users' ? 'active' : ''}">
                                        <i class="fa-solid fa-users-gear"></i>
                                        <span>Quản lý tài khoản</span>
                                    </a>
                                    <a href="${sidebarAuditLogsUrl}"
                                        class="admin-nav-item ${sidebarCurrentPage eq 'audit-logs' ? 'active' : ''}">
                                        <i class="fa-solid fa-shield-halved"></i>
                                        <span>Nhật ký Audit Log</span>
                                    </a>
                                </nav>
                            </div>
                        </c:when>
                    </c:choose>
                </div>

                <div class="admin-sidebar-footer">
                    <form class="admin-sidebar-logout-form" action="${sidebarLogoutUrl}" method="post">
                        <button class="admin-sidebar-footer-btn logout-btn" type="submit">
                            <i class="fa-solid fa-right-from-bracket"></i>
                            <span>Đăng xuất</span>
                        </button>
                    </form>
                </div>
