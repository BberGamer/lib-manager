<%--
    Trang danh sách kệ do ShelfManagementServlet hiển thị.
    Nhận shelfList, areas, bộ lọc, phân trang, rolePath và flash message.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="shelf" scope="request" />
<c:set var="pageTitle" value="Quản lý kệ sách – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/shelf-map.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="shelfListUrl" value="${rolePath}/shelf" />
<c:url var="shelfMapUrl" value="${rolePath}/shelf/map" />
<c:url var="shelfNewUrl" value="${rolePath}/shelf/new" />

<main class="page-wrapper shelf-page">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-layer-group"></i> Kho sách
                    </div>
                    <h1 class="books-page-title">Quản lý kệ sách</h1>
                    <p class="books-page-subtitle">
                        Tra cứu sức chứa, khu vực và vị trí các kệ trong Thư viện FPT University
                    </p>
                </div>
                <div class="books-page-stats" aria-label="Tổng số kệ">
                    <div class="bps-item">
                        <span class="bps-num"><c:out value="${totalShelves}" /></span>
                        <span class="bps-lbl">Kệ sách</span>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="container shelf-main-content">
        <c:if test="${not empty flashSuccess}">
            <div class="alert alert-success">
                <i class="fa-solid fa-circle-check"></i>
                <c:out value="${flashSuccess}" />
            </div>
        </c:if>
        <c:if test="${not empty flashError}">
            <div class="alert alert-danger">
                <i class="fa-solid fa-circle-xmark"></i>
                <c:out value="${flashError}" />
            </div>
        </c:if>

        <form action="${shelfListUrl}" method="get" novalidate>
            <div class="search-bar-wrapper">
                <div class="search-bar-inner">
                    <div class="search-field shelf-keyword-field">
                        <label for="shelf-keyword">Tìm kiếm</label>
                        <div class="search-input-wrap">
                            <i class="fa-solid fa-magnifying-glass search-icon"></i>
                            <input id="shelf-keyword" class="form-control" type="search" name="keyword"
                                   maxlength="100" value="${fn:escapeXml(keyword)}"
                                   placeholder="Nhập mã hoặc tên kệ..." autocomplete="off">
                        </div>
                    </div>
                    <div class="search-field select-field">
                        <label for="shelf-area">Khu vực</label>
                        <select id="shelf-area" class="form-select" name="area">
                            <option value="">-- Tất cả khu vực --</option>
                            <c:forEach var="area" items="${areas}">
                                <option value="${fn:escapeXml(area)}"
                                        ${selectedArea eq area ? 'selected' : ''}>
                                    <c:out value="${area}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="search-field select-field">
                        <label for="shelf-status">Trạng thái</label>
                        <select id="shelf-status" class="form-select" name="status">
                            <option value="">-- Tất cả trạng thái --</option>
                            <option value="ACTIVE" ${selectedStatus eq 'ACTIVE' ? 'selected' : ''}>
                                Đang sử dụng
                            </option>
                            <option value="INACTIVE" ${selectedStatus eq 'INACTIVE' ? 'selected' : ''}>
                                Ngừng sử dụng
                            </option>
                        </select>
                    </div>
                    <div class="shelf-filter-actions">
                        <button class="btn btn-primary" type="submit">
                            <i class="fa-solid fa-search"></i> Tìm
                        </button>
                        <a class="btn btn-outline" href="${shelfListUrl}" title="Xóa bộ lọc">
                            <i class="fa-solid fa-rotate-right"></i>
                        </a>
                    </div>
                </div>
            </div>
        </form>

        <section class="books-topbar">
            <div class="results-info shelf-results-info">
                <i class="fa-solid fa-layer-group"></i>
                Tổng cộng <strong><c:out value="${totalShelves}" /></strong> kệ sách
            </div>
            <div class="shelf-toolbar-actions">
                <a class="btn btn-outline btn-sm" href="${shelfMapUrl}">
                    <i class="fa-solid fa-map"></i> Bản đồ kệ
                </a>
                <a class="btn btn-primary btn-sm" href="${shelfNewUrl}">
                    <i class="fa-solid fa-plus"></i> Thêm kệ sách
                </a>
            </div>
        </section>

        <c:choose>
            <c:when test="${empty shelfList}">
                <section class="empty-state shelf-empty-state">
                    <div class="empty-icon"><i class="fa-solid fa-magnifying-glass"></i></div>
                    <h3>Không tìm thấy kệ sách</h3>
                    <p>Thử thay đổi từ khóa hoặc đặt lại bộ lọc.</p>
                    <a class="btn btn-outline" href="${shelfListUrl}">
                        <i class="fa-solid fa-rotate-right"></i> Xóa bộ lọc
                    </a>
                </section>
            </c:when>
            <c:otherwise>
                <div class="data-table-wrap">
                    <table class="data-table shelf-data-table">
                        <thead>
                            <tr>
                                <th>Mã kệ</th>
                                <th>Tên kệ</th>
                                <th>Khu vực</th>
                                <th>Tầng</th>
                                <th>Số sách</th>
                                <th>Sức chứa</th>
                                <th>Trạng thái</th>
                                <th class="shelf-action-heading">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="shelf" items="${shelfList}">
                                <c:url var="detailUrl" value="${rolePath}/shelf/detail">
                                    <c:param name="id" value="${shelf.id}" />
                                </c:url>
                                <c:url var="editUrl" value="${rolePath}/shelf/edit">
                                    <c:param name="id" value="${shelf.id}" />
                                </c:url>
                                <tr>
                                    <td><span class="shelf-code"><c:out value="${shelf.code}" /></span></td>
                                    <td><strong><c:out value="${shelf.name}" /></strong></td>
                                    <td><c:out value="${shelf.area}" /></td>
                                    <td>Tầng <c:out value="${shelf.floorNumber}" /></td>
                                    <td><c:out value="${shelf.bookCount}" /></td>
                                    <td><c:out value="${shelf.capacity}" /></td>
                                    <td>
                                        <span class="shelf-status
                                              ${shelf.status eq 'ACTIVE' ? 'is-active' : 'is-inactive'}">
                                            ${shelf.status eq 'ACTIVE' ? 'Đang sử dụng' : 'Ngừng sử dụng'}
                                        </span>
                                    </td>
                                    <td>
                                        <div class="shelf-row-actions">
                                            <a class="btn btn-outline btn-sm" href="${detailUrl}" title="Xem chi tiết">
                                                <i class="fa-solid fa-eye"></i>
                                            </a>
                                            <a class="btn btn-outline btn-sm" href="${editUrl}" title="Chỉnh sửa">
                                                <i class="fa-solid fa-pen"></i>
                                            </a>
                                            <form action="${pageContext.request.contextPath}${rolePath}/shelf/delete"
                                                  method="post" data-delete-shelf>
                                                <input type="hidden" name="id" value="${shelf.id}">
                                                <button class="btn btn-danger btn-sm" type="submit" title="Xóa kệ">
                                                    <i class="fa-solid fa-trash"></i>
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

        <c:if test="${totalPages gt 1}">
            <nav class="shelf-pagination" aria-label="Phân trang kệ sách">
                <ul class="pagination">
                    <c:forEach begin="1" end="${totalPages}" var="page">
                        <c:url var="pageUrl" value="${rolePath}/shelf">
                            <c:param name="page" value="${page}" />
                            <c:param name="keyword" value="${keyword}" />
                            <c:param name="area" value="${selectedArea}" />
                            <c:param name="status" value="${selectedStatus}" />
                        </c:url>
                        <li class="page-item ${page eq currentPage ? 'active' : ''}">
                            <a class="page-link" href="${pageUrl}"><c:out value="${page}" /></a>
                        </li>
                    </c:forEach>
                </ul>
            </nav>
        </c:if>
    </div>
</main>
<script src="${pageContext.request.contextPath}/assets/js/shelf-map.js" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
