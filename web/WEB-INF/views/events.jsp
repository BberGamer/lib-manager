<%--
    Trang Quản lý / Xem danh sách Sự kiện (Event Management).
    Đồng bộ giao diện, Sidebar và cấu trúc layout hoàn toàn giống trang Quản lý người dùng (user_list.jsp).
    Hỗ trợ cho cả người dùng chưa đăng nhập (khách), Độc giả (READER), Thủ thư (LIBRARIAN) và Admin.

    Các request attributes mong đợi:
    - `events`: Danh sách sự kiện đã phân trang (List<Event>)
    - `totalRecords`: Tổng số sự kiện (int)
    - `totalPages`: Tổng số trang (int)
    - `currentPageNum`: Trang hiện tại (int)
    - `q`: Từ khóa tìm kiếm (String)
    - `statusFilter`: Trạng thái lọc (String)
    - `sortBy`: Trường sắp xếp (String)
    - `sortOrder`: Thứ tự sắp xếp (String)
    - `currentPath`: Đường dẫn servlet hiện tại (String)
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="loggedUser" value="${sessionScope.loggedUser}" />
<c:set var="canManage" value="${not empty loggedUser and loggedUser.adminOrLibrarian}" />
<c:set var="isManagePageAttr" value="${canManage}" scope="request" />
<c:set var="activePage" value="events" scope="request" />
<c:set var="pageTitle" value="Quản lý sự kiện – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/user-list.css" scope="request" />

<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/event.css">

<c:set var="eventsUrl" value="${pageContext.request.contextPath}${not empty currentPath ? currentPath : '/events'}" />

<main class="page-wrapper">

    <!-- ===== PAGE HEADER BANNER ===== -->
    <div class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow user-hero-eyebrow">
                        <i class="fa-solid fa-calendar-days"></i> Sự kiện
                    </div>
                    <h1 class="books-page-title">Quản lý Sự kiện</h1>
                    <p class="books-page-subtitle">Xem, tìm kiếm và quản lý các sự kiện, hội thảo tại Thư viện FPT</p>
                </div>
                <div class="books-page-stats">
                    <div class="bps-item">
                        <span class="bps-num"><c:out value="${totalRecords}" /></span>
                        <span class="bps-lbl">Sự kiện</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="container user-list-container">

        <!-- ===== SEARCH & FILTER BAR ===== -->
        <form id="searchForm" action="${eventsUrl}" method="get">
            <input type="hidden" name="sort" value="${fn:escapeXml(sortBy)}">
            <input type="hidden" name="order" value="${fn:escapeXml(sortOrder)}">
            <input type="hidden" name="page" value="1">

            <div class="search-bar-wrapper">
                <div class="search-bar-inner">

                    <div class="search-field user-search-keyword">
                        <label for="keywordInput">Tìm kiếm sự kiện</label>
                        <div class="search-input-wrap">
                            <i class="fa-solid fa-magnifying-glass search-icon"></i>
                            <input type="text" id="keywordInput" name="q" class="form-control"
                                   placeholder="Nhập tiêu đề sự kiện..."
                                   value="${fn:escapeXml(q)}" maxlength="200" autocomplete="off">
                        </div>
                    </div>

                    <div class="search-field select-field">
                        <label for="statusSelect">Trạng thái</label>
                        <select id="statusSelect" name="status" class="form-select">
                            <option value="">-- Tất cả trạng thái --</option>
                            <option value="UPCOMING" ${statusFilter eq 'UPCOMING' ? 'selected' : ''}>Sắp diễn ra</option>
                            <option value="ONGOING" ${statusFilter eq 'ONGOING' ? 'selected' : ''}>Đang diễn ra</option>
                            <option value="ENDED" ${statusFilter eq 'ENDED' ? 'selected' : ''}>Đã kết thúc</option>
                            <option value="CANCELLED" ${statusFilter eq 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                        </select>
                    </div>

                    <div class="user-search-buttons">
                        <button type="submit" class="btn btn-primary" id="searchBtn">
                            <i class="fa-solid fa-magnifying-glass"></i> Lọc
                        </button>
                        <a href="${eventsUrl}" class="btn btn-outline" title="Xóa bộ lọc">
                            <i class="fa-solid fa-rotate-right"></i>
                        </a>
                    </div>

                </div>
            </div>
        </form>

        <!-- ===== THÔNG BÁO FLASH ===== -->
        <c:if test="${not empty successMsg}">
            <div class="alert alert-success" style="margin-top: 15px;">
                <i class="fa-solid fa-circle-check"></i>
                <c:out value="${successMsg}" />
            </div>
        </c:if>
        <c:if test="${not empty errorMsg}">
            <div class="alert alert-danger" style="margin-top: 15px;">
                <i class="fa-solid fa-circle-exclamation"></i>
                <c:out value="${errorMsg}" />
            </div>
        </c:if>

        <!-- ===== TOPBAR RESULTS & ACTIONS ===== -->
        <div class="books-topbar user-topbar">
            <div class="results-info user-results-info">
                <c:choose>
                    <c:when test="${not empty q or not empty statusFilter}">
                        <i class="fa-solid fa-filter fa-xs user-icon-primary"></i>
                        Kết quả lọc: <strong><c:out value="${totalRecords}" /></strong> sự kiện
                    </c:when>
                    <c:otherwise>
                        <i class="fa-solid fa-calendar-days fa-xs user-icon-primary-space"></i>
                        Tổng cộng <strong><c:out value="${totalRecords}" /></strong> sự kiện
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="user-topbar-actions">
                <!-- Sort Group giống user_list.jsp -->
                <div class="sort-group">
                    <span class="sort-label"><i class="fa-solid fa-arrow-up-wide-short"></i> Sắp xếp:</span>

                    <c:set var="isStartActive" value="${sortBy eq 'start_time' or empty sortBy}" />
                    <c:set var="startNextOrder" value="${isStartActive and sortOrder eq 'ASC' ? 'DESC' : 'ASC'}" />
                    <c:url var="sortStartUrl" value="${eventsUrl}">
                        <c:param name="q" value="${q}" />
                        <c:param name="status" value="${statusFilter}" />
                        <c:param name="sort" value="start_time" />
                        <c:param name="order" value="${startNextOrder}" />
                        <c:param name="page" value="1" />
                    </c:url>
                    <a href="${sortStartUrl}" class="sort-btn ${isStartActive ? 'sort-btn-active' : ''}">
                        Thời gian bắt đầu<c:if test="${isStartActive}"><c:out value="${sortOrder eq 'ASC' ? ' ▲' : ' ▼'}" /></c:if>
                    </a>

                    <c:set var="isTitleActive" value="${sortBy eq 'title'}" />
                    <c:set var="titleNextOrder" value="${isTitleActive and sortOrder eq 'ASC' ? 'DESC' : 'ASC'}" />
                    <c:url var="sortTitleUrl" value="${eventsUrl}">
                        <c:param name="q" value="${q}" />
                        <c:param name="status" value="${statusFilter}" />
                        <c:param name="sort" value="title" />
                        <c:param name="order" value="${titleNextOrder}" />
                        <c:param name="page" value="1" />
                    </c:url>
                    <a href="${sortTitleUrl}" class="sort-btn ${isTitleActive ? 'sort-btn-active' : ''}">
                        Tiêu đề sự kiện<c:if test="${isTitleActive}"><c:out value="${sortOrder eq 'ASC' ? ' ▲' : ' ▼'}" /></c:if>
                    </a>

                    <c:set var="isStatusActive" value="${sortBy eq 'status'}" />
                    <c:set var="statusNextOrder" value="${isStatusActive and sortOrder eq 'ASC' ? 'DESC' : 'ASC'}" />
                    <c:url var="sortStatusUrl" value="${eventsUrl}">
                        <c:param name="q" value="${q}" />
                        <c:param name="status" value="${statusFilter}" />
                        <c:param name="sort" value="status" />
                        <c:param name="order" value="${statusNextOrder}" />
                        <c:param name="page" value="1" />
                    </c:url>
                    <a href="${sortStatusUrl}" class="sort-btn ${isStatusActive ? 'sort-btn-active' : ''}">
                        Trạng thái<c:if test="${isStatusActive}"><c:out value="${sortOrder eq 'ASC' ? ' ▲' : ' ▼'}" /></c:if>
                    </a>
                </div>

                <c:if test="${canManage}">
                    <button type="button" class="btn btn-primary" data-action="open-add-modal">
                        <i class="fa-solid fa-plus"></i> Thêm sự kiện mới
                    </button>
                </c:if>
            </div>
        </div>

        <!-- ===== DATA TABLE CARD ===== -->
        <div class="user-card">
            <div class="user-section-head">
                <h2 class="user-section-title">Danh sách sự kiện</h2>
                <span class="user-section-count">Trang <c:out value="${currentPageNum}" /> / <c:out value="${totalPages}" /></span>
            </div>

            <div class="user-table-wrap">
                <table class="user-table">
                    <thead>
                        <tr class="user-table-head-row">
                            <th class="user-th-id">STT</th>
                            <th class="user-th">Tiêu đề sự kiện</th>
                            <th class="user-th">Thời gian bắt đầu</th>
                            <th class="user-th">Thời gian kết thúc</th>
                            <th class="user-th">Trạng thái</th>
                            <th class="user-th">Người tạo</th>
                            <th class="user-th-action">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty events}">
                                <tr>
                                    <td colspan="7" class="user-empty-td">
                                        <div class="user-empty-icon">
                                            <i class="fa-solid fa-calendar-xmark" style="color: #cbd5e1;"></i>
                                        </div>
                                        <h3 class="user-empty-title">Không tìm thấy sự kiện nào</h3>
                                        <p class="user-empty-desc">Thử thay đổi từ khóa hoặc bộ lọc tìm kiếm của bạn.</p>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="item" items="${events}" varStatus="loop">
                                    <c:set var="displayStatus" value="${item.displayStatus}" />
                                    <c:set var="startTimeFormatted" value="${fn:replace(item.startTime, 'T', ' ')}" />
                                    <c:set var="endTimeFormatted" value="${fn:replace(item.endTime, 'T', ' ')}" />
                                    <tr class="user-tr">
                                        <td class="user-td-id"><c:out value="${(currentPageNum - 1) * 10 + loop.index + 1}" /></td>
                                        <td class="user-td-username">
                                            <span class="user-username-link" style="color: #0f172a;"><c:out value="${item.title}" /></span>
                                        </td>
                                        <td class="user-td-fullname"><c:out value="${startTimeFormatted}" /></td>
                                        <td class="user-td-fullname"><c:out value="${endTimeFormatted}" /></td>
                                        <td class="user-td-status">
                                            <c:choose>
                                                <c:when test="${displayStatus eq 'UPCOMING'}">
                                                    <span class="user-status-badge-active" style="background:#fff7ed; color:#c2410c;">Sắp diễn ra</span>
                                                </c:when>
                                                <c:when test="${displayStatus eq 'ONGOING'}">
                                                    <span class="user-status-badge-active" style="background:#d1fae5; color:#065f46;">Đang diễn ra</span>
                                                </c:when>
                                                <c:when test="${displayStatus eq 'ENDED'}">
                                                    <span class="user-status-badge-locked" style="background:#f3f4f6; color:#4b5563;">Đã kết thúc</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="user-status-badge-locked" style="background:#fee2e2; color:#991b1b;">Đã hủy</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="user-td-email"><c:out value="${item.createdBy}" /></td>
                                        <td class="user-td-actions">
                                            <div class="user-row-actions">
                                                <!-- Nút Xem chi tiết -->
                                                <button type="button" class="user-action-view"
                                                        data-action="open-view-modal"
                                                        data-id="${item.id}"
                                                        data-title="${fn:escapeXml(item.title)}"
                                                        data-description="${fn:escapeXml(item.description)}"
                                                        data-start-time="${startTimeFormatted}"
                                                        data-end-time="${endTimeFormatted}"
                                                        data-display-status="${displayStatus}"
                                                        data-created-by="${fn:escapeXml(item.createdBy)}"
                                                        data-updated-by="${fn:escapeXml(item.updatedBy)}"
                                                        title="Xem chi tiết">
                                                    <i class="fa-solid fa-eye"></i> Xem
                                                </button>

                                                <c:if test="${canManage}">
                                                    <!-- Nút Sửa -->
                                                    <button type="button" class="user-action-view" style="color:#c2410c; border-color:#fcd34d; background:#fff7ed;"
                                                            data-action="open-edit-modal"
                                                            data-id="${item.id}"
                                                            data-title="${fn:escapeXml(item.title)}"
                                                            data-description="${fn:escapeXml(item.description)}"
                                                            data-start-time-raw="${item.startTime}"
                                                            data-end-time-raw="${item.endTime}"
                                                            data-status="${item.status}"
                                                            title="Chỉnh sửa sự kiện">
                                                        <i class="fa-solid fa-pen-to-square"></i> Sửa
                                                    </button>

                                                    <!-- Nút Xóa -->
                                                    <button type="button" class="user-action-delete"
                                                            data-action="open-delete-modal"
                                                            data-id="${item.id}"
                                                            data-title="${fn:escapeXml(item.title)}"
                                                            title="Xóa sự kiện">
                                                        <i class="fa-solid fa-trash-can"></i> Xóa
                                                    </button>
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

            <!-- ===== PAGINATION ===== -->
            <c:if test="${totalPages gt 1}">
                <div class="user-pagination-wrap">
                    <nav aria-label="Phân trang">
                        <ul class="pagination user-pagination-ul">
                            <!-- Prev -->
                            <c:url var="prevUrl" value="${eventsUrl}">
                                <c:param name="q" value="${q}" />
                                <c:param name="status" value="${statusFilter}" />
                                <c:param name="sort" value="${sortBy}" />
                                <c:param name="order" value="${sortOrder}" />
                                <c:param name="page" value="${currentPageNum - 1}" />
                            </c:url>
                            <li class="page-item ${currentPageNum le 1 ? 'disabled' : ''}">
                                <a class="page-link" href="${prevUrl}">
                                    <i class="fa-solid fa-chevron-left fa-xs"></i>
                                </a>
                            </li>

                            <c:choose>
                                <%-- Trường hợp tổng số trang <= 7: hiển thị tất cả các trang --%>
                                <c:when test="${totalPages le 7}">
                                    <c:forEach var="p" begin="1" end="${totalPages}">
                                        <c:url var="pUrl" value="${eventsUrl}">
                                            <c:param name="q" value="${q}" />
                                            <c:param name="status" value="${statusFilter}" />
                                            <c:param name="sort" value="${sortBy}" />
                                            <c:param name="order" value="${sortOrder}" />
                                            <c:param name="page" value="${p}" />
                                        </c:url>
                                        <li class="page-item ${currentPageNum eq p ? 'active' : ''}">
                                            <a class="page-link" href="${pUrl}"><c:out value="${p}" /></a>
                                        </li>
                                    </c:forEach>
                                </c:when>

                                <%-- Trường hợp tổng số trang > 7: có rút gọn bằng dấu ba chấm (…) --%>
                                <c:otherwise>
                                    <%-- 2 trang đầu --%>
                                    <c:forEach var="p" begin="1" end="2">
                                        <c:url var="pUrl" value="${eventsUrl}">
                                            <c:param name="q" value="${q}" />
                                            <c:param name="status" value="${statusFilter}" />
                                            <c:param name="sort" value="${sortBy}" />
                                            <c:param name="order" value="${sortOrder}" />
                                            <c:param name="page" value="${p}" />
                                        </c:url>
                                        <li class="page-item ${currentPageNum eq p ? 'active' : ''}">
                                            <a class="page-link" href="${pUrl}"><c:out value="${p}" /></a>
                                        </li>
                                    </c:forEach>

                                    <c:choose>
                                        <%-- Trang hiện tại <= 4 --%>
                                        <c:when test="${currentPageNum le 4}">
                                            <c:forEach var="p" begin="3" end="5">
                                                <c:url var="pUrl" value="${eventsUrl}">
                                                    <c:param name="q" value="${q}" />
                                                    <c:param name="status" value="${statusFilter}" />
                                                    <c:param name="sort" value="${sortBy}" />
                                                    <c:param name="order" value="${sortOrder}" />
                                                    <c:param name="page" value="${p}" />
                                                </c:url>
                                                <li class="page-item ${currentPageNum eq p ? 'active' : ''}">
                                                    <a class="page-link" href="${pUrl}"><c:out value="${p}" /></a>
                                                </li>
                                            </c:forEach>
                                            <li class="page-item disabled"><span class="page-link">…</span></li>
                                        </c:when>

                                        <%-- Trang hiện tại >= totalPages - 3 --%>
                                        <c:when test="${currentPageNum ge (totalPages - 3)}">
                                            <li class="page-item disabled"><span class="page-link">…</span></li>
                                            <c:forEach var="p" begin="${totalPages - 4}" end="${totalPages - 2}">
                                                <c:url var="pUrl" value="${eventsUrl}">
                                                    <c:param name="q" value="${q}" />
                                                    <c:param name="status" value="${statusFilter}" />
                                                    <c:param name="sort" value="${sortBy}" />
                                                    <c:param name="order" value="${sortOrder}" />
                                                    <c:param name="page" value="${p}" />
                                                </c:url>
                                                <li class="page-item ${currentPageNum eq p ? 'active' : ''}">
                                                    <a class="page-link" href="${pUrl}"><c:out value="${p}" /></a>
                                                </li>
                                            </c:forEach>
                                        </c:when>

                                        <%-- Trang hiện tại ở giữa --%>
                                        <c:otherwise>
                                            <li class="page-item disabled"><span class="page-link">…</span></li>
                                            <c:forEach var="p" begin="${currentPageNum - 1}" end="${currentPageNum + 1}">
                                                <c:url var="pUrl" value="${eventsUrl}">
                                                    <c:param name="q" value="${q}" />
                                                    <c:param name="status" value="${statusFilter}" />
                                                    <c:param name="sort" value="${sortBy}" />
                                                    <c:param name="order" value="${sortOrder}" />
                                                    <c:param name="page" value="${p}" />
                                                </c:url>
                                                <li class="page-item ${currentPageNum eq p ? 'active' : ''}">
                                                    <a class="page-link" href="${pUrl}"><c:out value="${p}" /></a>
                                                </li>
                                            </c:forEach>
                                            <li class="page-item disabled"><span class="page-link">…</span></li>
                                        </c:otherwise>
                                    </c:choose>

                                    <%-- 2 trang cuối --%>
                                    <c:forEach var="p" begin="${totalPages - 1}" end="${totalPages}">
                                        <c:url var="pUrl" value="${eventsUrl}">
                                            <c:param name="q" value="${q}" />
                                            <c:param name="status" value="${statusFilter}" />
                                            <c:param name="sort" value="${sortBy}" />
                                            <c:param name="order" value="${sortOrder}" />
                                            <c:param name="page" value="${p}" />
                                        </c:url>
                                        <li class="page-item ${currentPageNum eq p ? 'active' : ''}">
                                            <a class="page-link" href="${pUrl}"><c:out value="${p}" /></a>
                                        </li>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>

                            <!-- Next -->
                            <c:url var="nextUrl" value="${eventsUrl}">
                                <c:param name="q" value="${q}" />
                                <c:param name="status" value="${statusFilter}" />
                                <c:param name="sort" value="${sortBy}" />
                                <c:param name="order" value="${sortOrder}" />
                                <c:param name="page" value="${currentPageNum + 1}" />
                            </c:url>
                            <li class="page-item ${currentPageNum ge totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${nextUrl}">
                                    <i class="fa-solid fa-chevron-right fa-xs"></i>
                                </a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>

    </div>
</main>

<!-- ========================================================================== -->
<!-- MODAL 1: XEM CHI TIẾT SỰ KIỆN                                               -->
<!-- ========================================================================== -->
<div id="viewEventModal" class="modal-backdrop">
    <div class="modal-dialog">
        <div class="modal-header">
            <h3><i class="fa-solid fa-circle-info"></i> Chi tiết sự kiện</h3>
            <button type="button" class="modal-close-btn" data-action="close-modal">&times;</button>
        </div>
        <div class="modal-body">
            <div class="detail-group">
                <div class="detail-label">Tiêu đề sự kiện</div>
                <div id="viewTitle" class="detail-value" style="font-weight: 700; font-size: 1.1rem;"></div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Trạng thái</div>
                <div class="detail-value">
                    <span id="viewDisplayStatus"></span>
                </div>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                <div class="detail-group">
                    <div class="detail-label">Bắt đầu</div>
                    <div id="viewStartTime" class="detail-value"></div>
                </div>
                <div class="detail-group">
                    <div class="detail-label">Kết thúc</div>
                    <div id="viewEndTime" class="detail-value"></div>
                </div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Mô tả sự kiện</div>
                <div id="viewDescription" class="detail-value" style="white-space: pre-wrap;"></div>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 16px; border-top: 1px dashed #e2e8f0; padding-top: 12px;">
                <div class="detail-group" style="margin:0;">
                    <div class="detail-label">Tài khoản tạo</div>
                    <div id="viewCreatedBy" class="detail-value"></div>
                </div>
                <div class="detail-group" style="margin:0;">
                    <div class="detail-label">Cập nhật gần nhất</div>
                    <div id="viewUpdatedBy" class="detail-value"></div>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-outline" data-action="close-modal">Đóng</button>
        </div>
    </div>
</div>

<c:if test="${canManage}">
    <!-- ========================================================================== -->
    <!-- MODAL 2: THÊM SỰ KIỆN MỚI                                                  -->
    <!-- Ghi chú: Validate hoàn toàn ở Service, KHÔNG có required/pattern ở HTML    -->
    <!-- ========================================================================== -->
    <div id="addEventModal" class="modal-backdrop">
        <div class="modal-dialog">
            <form action="${eventsUrl}" method="post">
                <input type="hidden" name="action" value="create">
                <div class="modal-header">
                    <h3><i class="fa-solid fa-calendar-plus"></i> Thêm sự kiện mới</h3>
                    <button type="button" class="modal-close-btn" data-action="close-modal">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="event-form-group" style="margin-bottom: 16px;">
                        <label for="addTitle">Tiêu đề sự kiện</label>
                        <input type="text" id="addTitle" name="title" class="form-control"
                               placeholder="Nhập tiêu đề sự kiện (3 - 200 ký tự)">
                    </div>
                    <div class="event-form-group" style="margin-bottom: 16px;">
                        <label for="addDescription">Mô tả sự kiện</label>
                        <textarea id="addDescription" name="description" class="form-control" rows="4"
                                  placeholder="Nhập nội dung mô tả sự kiện (tối đa 1000 ký tự)"></textarea>
                    </div>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
                        <div class="event-form-group">
                            <label for="addStartTime">Thời gian bắt đầu</label>
                            <input type="datetime-local" id="addStartTime" name="startTime" class="form-control">
                        </div>
                        <div class="event-form-group">
                            <label for="addEndTime">Thời gian kết thúc</label>
                            <input type="datetime-local" id="addEndTime" name="endTime" class="form-control">
                        </div>
                    </div>
                    <div class="event-form-group">
                        <label for="addStatus">Trạng thái sự kiện</label>
                        <select id="addStatus" name="status" class="form-select">
                            <option value="ACTIVE">Hoạt động (ACTIVE)</option>
                            <option value="CANCELLED">Hủy (CANCELLED)</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" data-action="close-modal">Hủy</button>
                    <button type="submit" class="btn btn-primary">
                        <i class="fa-solid fa-floppy-disk"></i> Lưu sự kiện
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- ========================================================================== -->
    <!-- MODAL 3: CHỈNH SỬA SỰ KIỆN                                                 -->
    <!-- Ghi chú: Validate hoàn toàn ở Service, KHÔNG có required/pattern ở HTML    -->
    <!-- ========================================================================== -->
    <div id="editEventModal" class="modal-backdrop">
        <div class="modal-dialog">
            <form action="${eventsUrl}" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" id="editId" name="id" value="">
                <div class="modal-header">
                    <h3><i class="fa-solid fa-pen-to-square"></i> Cập nhật sự kiện</h3>
                    <button type="button" class="modal-close-btn" data-action="close-modal">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="event-form-group" style="margin-bottom: 16px;">
                        <label for="editTitle">Tiêu đề sự kiện</label>
                        <input type="text" id="editTitle" name="title" class="form-control"
                               placeholder="Nhập tiêu đề sự kiện (3 - 200 ký tự)">
                    </div>
                    <div class="event-form-group" style="margin-bottom: 16px;">
                        <label for="editDescription">Mô tả sự kiện</label>
                        <textarea id="editDescription" name="description" class="form-control" rows="4"
                                  placeholder="Nhập nội dung mô tả sự kiện (tối đa 1000 ký tự)"></textarea>
                    </div>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
                        <div class="event-form-group">
                            <label for="editStartTime">Thời gian bắt đầu</label>
                            <input type="datetime-local" id="editStartTime" name="startTime" class="form-control">
                        </div>
                        <div class="event-form-group">
                            <label for="editEndTime">Thời gian kết thúc</label>
                            <input type="datetime-local" id="editEndTime" name="endTime" class="form-control">
                        </div>
                    </div>
                    <div class="event-form-group">
                        <label for="editStatus">Trạng thái sự kiện</label>
                        <select id="editStatus" name="status" class="form-select">
                            <option value="ACTIVE">Hoạt động (ACTIVE)</option>
                            <option value="CANCELLED">Hủy (CANCELLED)</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" data-action="close-modal">Hủy</button>
                    <button type="submit" class="btn btn-primary">
                        <i class="fa-solid fa-check"></i> Cập nhật
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- ========================================================================== -->
    <!-- MODAL 4: XÓA SỰ KIỆN (SOFT DELETE)                                         -->
    <!-- ========================================================================== -->
    <div id="deleteEventModal" class="modal-backdrop">
        <div class="modal-dialog" style="max-width: 450px;">
            <form action="${eventsUrl}" method="post">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" id="deleteId" name="id" value="">
                <div class="modal-header">
                    <h3 style="color: #ef4444;"><i class="fa-solid fa-triangle-exclamation"></i> Xác nhận xóa</h3>
                    <button type="button" class="modal-close-btn" data-action="close-modal">&times;</button>
                </div>
                <div class="modal-body">
                    <p style="margin: 0; color: #334155; font-size: 0.95rem;">
                        Bạn có chắc chắn muốn xóa sự kiện <strong id="deleteEventTitle"></strong> không?
                    </p>
                    <p style="margin-top: 8px; color: #64748b; font-size: 0.85rem;">
                        (Hệ thống sẽ thực hiện xóa mềm sự kiện này).
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" data-action="close-modal">Hủy</button>
                    <button type="submit" class="btn btn-danger" style="background:#ef4444; color:#fff; border:none;">
                        <i class="fa-solid fa-trash-can"></i> Đồng ý xóa
                    </button>
                </div>
            </form>
        </div>
    </div>
</c:if>

<script src="${pageContext.request.contextPath}/assets/js/event.js" defer></script>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
