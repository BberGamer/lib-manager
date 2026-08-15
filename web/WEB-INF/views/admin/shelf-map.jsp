<%--
    Trang sơ đồ kho sách do ShelfManagementServlet hiển thị.
    Mong đợi request attributes copyList, distinctAreas, rolePath, totalPages, currentPageNum,
    selectedArea và keyword; session attributes successMsg, errorMsg và loggedUser.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="shelf" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/shelf-map.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<c:url var="shelfListUrl" value="${rolePath}/shelf" />
<c:url var="shelfUpdateUrl" value="${rolePath}/shelf/update" />
<c:url var="shelfMapScriptUrl" value="/assets/js/shelf-map.js" />

<main class="page-wrapper shelf-management-page">
    <div class="container shelf-management-container">
        <header class="shelf-page-header">
            <h1 class="section-title">
                <i class="fa-solid fa-map-location-dot"></i>
                Sơ đồ bố trí kho sách
            </h1>
            <p class="section-subtitle">
                Định vị và cập nhật khu lưu trữ, kệ sách và ô chứa của từng bản sao.
            </p>
        </header>

        <c:if test="${not empty sessionScope.successMsg}">
            <div class="shelf-alert shelf-alert-success" role="status">
                <i class="fa-solid fa-circle-check"></i>
                <c:out value="${sessionScope.successMsg}" />
            </div>
            <c:remove var="successMsg" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.errorMsg}">
            <div class="shelf-alert shelf-alert-error" role="alert">
                <i class="fa-solid fa-circle-exclamation"></i>
                <c:out value="${sessionScope.errorMsg}" />
            </div>
            <c:remove var="errorMsg" scope="session" />
        </c:if>

        <section class="shelf-filter-card" aria-labelledby="shelf-filter-title">
            <h2 id="shelf-filter-title" class="visually-hidden">Bộ lọc vị trí bản sao</h2>
            <form class="shelf-filter-form" action="${shelfListUrl}" method="get">
                <label class="shelf-filter-keyword">
                    <span>Tìm kiếm bản sao</span>
                    <input type="search" name="keyword" maxlength="200"
                           value="${fn:escapeXml(keyword)}"
                           placeholder="Nhập mã vạch hoặc tiêu đề sách...">
                </label>
                <label>
                    <span>Khu vực lưu trữ</span>
                    <select name="area">
                        <option value="">Tất cả khu vực</option>
                        <c:forEach var="area" items="${distinctAreas}">
                            <option value="${fn:escapeXml(area)}"
                                    ${selectedArea eq area ? 'selected' : ''}>
                                <c:out value="${area}" />
                            </option>
                        </c:forEach>
                    </select>
                </label>
                <button type="submit" class="btn btn-primary shelf-filter-button">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    Định vị
                </button>
            </form>
        </section>

        <section class="shelf-table-card" aria-label="Danh sách vị trí bản sao sách">
            <div class="shelf-table-scroll">
                <table class="shelf-table">
                    <thead>
                        <tr>
                            <th>Mã vạch</th>
                            <th>Tên đầu sách / ISBN</th>
                            <th>Khu vực</th>
                            <th>Kệ sách</th>
                            <th>Ô chứa</th>
                            <th>Trạng thái</th>
                            <th class="shelf-actions-heading">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty copyList}">
                                <tr>
                                    <td class="shelf-empty-state" colspan="7">
                                        <i class="fa-solid fa-map"></i>
                                        <span>Không tìm thấy bản sao sách nào trong kho.</span>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="bookCopy" items="${copyList}">
                                    <tr>
                                        <td class="shelf-barcode">
                                            <c:out value="${bookCopy.barcode}" />
                                        </td>
                                        <td>
                                            <strong><c:out value="${bookCopy.book.title}" /></strong>
                                            <small>ISBN: <c:out value="${bookCopy.book.isbn}" /></small>
                                        </td>
                                        <td>
                                            <span class="shelf-area">
                                                <c:out value="${empty bookCopy.area ? '-' : bookCopy.area}" />
                                            </span>
                                        </td>
                                        <td><c:out value="${empty bookCopy.shelf ? '-' : bookCopy.shelf}" /></td>
                                        <td><c:out value="${empty bookCopy.slot ? '-' : bookCopy.slot}" /></td>
                                        <td>
                                            <span class="shelf-status
                                                  shelf-status-${fn:toLowerCase(bookCopy.status)}">
                                                <c:choose>
                                                    <c:when test="${bookCopy.status eq 'AVAILABLE'}">Sẵn sàng</c:when>
                                                    <c:when test="${bookCopy.status eq 'BORROWED'}">Đang mượn</c:when>
                                                    <c:when test="${bookCopy.status eq 'RESERVED'}">
                                                        Đã đặt trước
                                                    </c:when>
                                                    <c:when test="${bookCopy.status eq 'MAINTENANCE'}">Bảo trì</c:when>
                                                    <c:when test="${bookCopy.status eq 'LOST'}">Thất lạc</c:when>
                                                    <c:otherwise><c:out value="${bookCopy.status}" /></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <td class="shelf-actions">
                                            <button type="button" class="btn btn-sm btn-primary"
                                                    data-open-location-modal
                                                    data-copy-id="${bookCopy.id}"
                                                    data-barcode="${fn:escapeXml(bookCopy.barcode)}"
                                                    data-book-title="${fn:escapeXml(bookCopy.book.title)}"
                                                    data-area="${fn:escapeXml(bookCopy.area)}"
                                                    data-shelf="${fn:escapeXml(bookCopy.shelf)}"
                                                    data-slot="${fn:escapeXml(bookCopy.slot)}">
                                                <i class="fa-solid fa-pen-to-square"></i>
                                                Đổi vị trí
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <c:if test="${totalPages gt 1}">
            <nav class="shelf-pagination" aria-label="Phân trang vị trí kho sách">
                <ul class="pagination">
                    <li class="page-item ${currentPageNum le 1 ? 'disabled' : ''}">
                        <c:choose>
                            <c:when test="${currentPageNum gt 1}">
                                <c:url var="previousPageUrl" value="${rolePath}/shelf">
                                    <c:param name="area" value="${selectedArea}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="page" value="${currentPageNum - 1}" />
                                </c:url>
                                <a class="page-link" href="${previousPageUrl}" aria-label="Trang trước">
                                    <i class="fa-solid fa-chevron-left fa-xs"></i>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <span class="page-link" aria-hidden="true">
                                    <i class="fa-solid fa-chevron-left fa-xs"></i>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </li>

                    <c:choose>
                        <c:when test="${totalPages le 7}">
                            <c:forEach begin="1" end="${totalPages}" var="pageNumber">
                                <c:url var="pageUrl" value="${rolePath}/shelf">
                                    <c:param name="area" value="${selectedArea}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="page" value="${pageNumber}" />
                                </c:url>
                                <li class="page-item ${pageNumber eq currentPageNum ? 'active' : ''}">
                                    <a class="page-link" href="${pageUrl}">
                                        <c:out value="${pageNumber}" />
                                    </a>
                                </li>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <c:forEach begin="1" end="2" var="pageNumber">
                                <c:url var="pageUrl" value="${rolePath}/shelf">
                                    <c:param name="area" value="${selectedArea}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="page" value="${pageNumber}" />
                                </c:url>
                                <li class="page-item ${pageNumber eq currentPageNum ? 'active' : ''}">
                                    <a class="page-link" href="${pageUrl}">
                                        <c:out value="${pageNumber}" />
                                    </a>
                                </li>
                            </c:forEach>

                            <c:choose>
                                <c:when test="${currentPageNum le 4}">
                                    <c:forEach begin="3" end="5" var="pageNumber">
                                        <c:url var="pageUrl" value="${rolePath}/shelf">
                                            <c:param name="area" value="${selectedArea}" />
                                            <c:param name="keyword" value="${keyword}" />
                                            <c:param name="page" value="${pageNumber}" />
                                        </c:url>
                                        <li class="page-item
                                                   ${pageNumber eq currentPageNum ? 'active' : ''}">
                                            <a class="page-link" href="${pageUrl}">
                                                <c:out value="${pageNumber}" />
                                            </a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item disabled">
                                        <span class="page-link">…</span>
                                    </li>
                                </c:when>
                                <c:when test="${currentPageNum ge totalPages - 3}">
                                    <li class="page-item disabled">
                                        <span class="page-link">…</span>
                                    </li>
                                    <c:forEach begin="${totalPages - 4}" end="${totalPages - 2}"
                                               var="pageNumber">
                                        <c:url var="pageUrl" value="${rolePath}/shelf">
                                            <c:param name="area" value="${selectedArea}" />
                                            <c:param name="keyword" value="${keyword}" />
                                            <c:param name="page" value="${pageNumber}" />
                                        </c:url>
                                        <li class="page-item
                                                   ${pageNumber eq currentPageNum ? 'active' : ''}">
                                            <a class="page-link" href="${pageUrl}">
                                                <c:out value="${pageNumber}" />
                                            </a>
                                        </li>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <li class="page-item disabled">
                                        <span class="page-link">…</span>
                                    </li>
                                    <c:forEach begin="${currentPageNum - 1}" end="${currentPageNum + 1}"
                                               var="pageNumber">
                                        <c:url var="pageUrl" value="${rolePath}/shelf">
                                            <c:param name="area" value="${selectedArea}" />
                                            <c:param name="keyword" value="${keyword}" />
                                            <c:param name="page" value="${pageNumber}" />
                                        </c:url>
                                        <li class="page-item
                                                   ${pageNumber eq currentPageNum ? 'active' : ''}">
                                            <a class="page-link" href="${pageUrl}">
                                                <c:out value="${pageNumber}" />
                                            </a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item disabled">
                                        <span class="page-link">…</span>
                                    </li>
                                </c:otherwise>
                            </c:choose>

                            <c:forEach begin="${totalPages - 1}" end="${totalPages}" var="pageNumber">
                                <c:url var="pageUrl" value="${rolePath}/shelf">
                                    <c:param name="area" value="${selectedArea}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="page" value="${pageNumber}" />
                                </c:url>
                                <li class="page-item ${pageNumber eq currentPageNum ? 'active' : ''}">
                                    <a class="page-link" href="${pageUrl}">
                                        <c:out value="${pageNumber}" />
                                    </a>
                                </li>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>

                    <li class="page-item ${currentPageNum ge totalPages ? 'disabled' : ''}">
                        <c:choose>
                            <c:when test="${currentPageNum lt totalPages}">
                                <c:url var="nextPageUrl" value="${rolePath}/shelf">
                                    <c:param name="area" value="${selectedArea}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="page" value="${currentPageNum + 1}" />
                                </c:url>
                                <a class="page-link" href="${nextPageUrl}" aria-label="Trang sau">
                                    <i class="fa-solid fa-chevron-right fa-xs"></i>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <span class="page-link" aria-hidden="true">
                                    <i class="fa-solid fa-chevron-right fa-xs"></i>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </li>
                </ul>
            </nav>
        </c:if>
    </div>
</main>

<div class="shelf-modal" data-location-modal hidden>
    <section class="shelf-modal-dialog" role="dialog" aria-modal="true"
             aria-labelledby="location-modal-title">
        <h2 id="location-modal-title">
            <i class="fa-solid fa-map-location-dot"></i>
            Định vị bản sao sách
        </h2>
        <p>Điều chỉnh vị trí lưu trữ vật lý để thuận tiện tìm kiếm bản sao.</p>
        <form action="${shelfUpdateUrl}" method="post">
            <input type="hidden" name="id" data-location-field="copyId">
            <label>
                <span>Mã vạch</span>
                <input class="shelf-monospace" type="text" readonly data-location-field="barcode">
            </label>
            <label>
                <span>Tên sách</span>
                <input type="text" readonly data-location-field="bookTitle">
            </label>
            <label>
                <span>Khu vực lưu trữ</span>
                <select name="area" required data-location-field="area">
                    <option value="">-- Chọn khu vực --</option>
                    <option value="Khu A">Khu A</option>
                    <option value="Khu B">Khu B</option>
                    <option value="Khu C">Khu C</option>
                    <option value="Tầng 1">Tầng 1</option>
                    <option value="Tầng 2">Tầng 2</option>
                    <option value="Tầng 3">Tầng 3</option>
                    <option value="Phòng đọc">Phòng đọc</option>
                    <option value="Kho lưu trữ">Kho lưu trữ</option>
                </select>
            </label>
            <div class="shelf-modal-grid">
                <label>
                    <span>Dãy kệ</span>
                    <input type="text" name="shelf" maxlength="100"
                           placeholder="Ví dụ: Kệ 01, Kệ Ngoại ngữ..."
                           data-location-field="shelf">
                </label>
                <label>
                    <span>Ngăn ô</span>
                    <input type="text" name="slot" maxlength="100"
                           placeholder="Ví dụ: Ngăn 1, Ô A2..."
                           data-location-field="slot">
                </label>
            </div>
            <div class="shelf-modal-actions">
                <button type="button" class="btn btn-secondary" data-close-location-modal>
                    Hủy bỏ
                </button>
                <button type="submit" class="btn btn-primary">Cập nhật</button>
            </div>
        </form>
    </section>
</div>

<script src="${shelfMapScriptUrl}" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
