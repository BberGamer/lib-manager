<%--
    Trang chi tiết kệ do ShelfManagementServlet hiển thị.
    Nhận shelf, shelf.bookCopies và rolePath.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="shelf" scope="request" />
<c:set var="pageTitle" value="Chi tiết kệ – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/shelf-map.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="shelfListUrl" value="${rolePath}/shelf" />
<c:url var="shelfEditUrl" value="${rolePath}/shelf/edit">
    <c:param name="id" value="${shelf.id}" />
</c:url>

<main class="shelf-page">
    <div class="shelf-content">
        <a class="back" href="${shelfListUrl}">
            <i class="fa-solid fa-arrow-left"></i>&nbsp; Quay lại danh sách
        </a>

        <section class="detail-head">
            <div>
                <span class="shelf-code"><c:out value="${shelf.code}" /></span>
                <h1><c:out value="${shelf.name}" /></h1>
                <p>
                    <c:out value="${shelf.area}" />
                    · Tầng <c:out value="${shelf.floorNumber}" />
                </p>
            </div>
            <a class="btn btn-primary" href="${shelfEditUrl}">
                <i class="fa-solid fa-pen"></i> Sửa kệ
            </a>
        </section>

        <div class="metrics">
            <article>
                <strong><c:out value="${shelf.capacity}" /></strong>
                <span>Sức chứa</span>
            </article>
            <article>
                <strong><c:out value="${shelf.bookCount}" /></strong>
                <span>Số sách hiện tại</span>
            </article>
            <article>
                <strong><c:out value="${shelf.availableSlots}" /></strong>
                <span>Vị trí còn trống</span>
            </article>
        </div>

        <section class="data-table-wrap">
            <header class="shelf-section-heading">
                <h2>Sách đang nằm trên kệ</h2>
            </header>
            <table class="data-table shelf-data-table">
                <thead>
                    <tr>
                        <th>Barcode</th>
                        <th>Đầu sách</th>
                        <th>ISBN</th>
                        <th>Ngăn</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty shelf.bookCopies}">
                            <tr>
                                <td colspan="5" class="shelf-table-empty">Kệ chưa có sách.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="copy" items="${shelf.bookCopies}">
                                <tr>
                                    <td>
                                        <span class="shelf-copy-barcode">
                                            <c:out value="${copy.barcode}" />
                                        </span>
                                    </td>
                                    <td>
                                        <span class="shelf-book-title">
                                            <c:out value="${copy.book.title}" />
                                        </span>
                                    </td>
                                    <td><c:out value="${copy.book.isbn}" /></td>
                                    <td><c:out value="${copy.slot}" /></td>
                                    <td>
                                        <span class="copy-status is-${fn:toLowerCase(copy.status)}">
                                            <c:choose>
                                                <c:when test="${copy.status eq 'AVAILABLE'}">Có sẵn</c:when>
                                                <c:when test="${copy.status eq 'BORROWED'}">Đang mượn</c:when>
                                                <c:when test="${copy.status eq 'RESERVED'}">Đã đặt trước</c:when>
                                                <c:when test="${copy.status eq 'MAINTENANCE'}">Bảo trì</c:when>
                                                <c:when test="${copy.status eq 'LOST'}">Thất lạc</c:when>
                                                <c:otherwise><c:out value="${copy.status}" /></c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </section>

        <c:if test="${totalPages gt 1}">
            <nav class="shelf-pagination" aria-label="Phân trang sách trên kệ">
                <ul class="pagination">
                    <li class="page-item ${currentPage le 1 ? 'disabled' : ''}">
                        <c:url var="previousPageUrl" value="${rolePath}/shelf/detail">
                            <c:param name="id" value="${shelf.id}" />
                            <c:param name="page" value="${currentPage - 1}" />
                        </c:url>
                        <a class="page-link" href="${previousPageUrl}" aria-label="Trang trước">
                            <i class="fa-solid fa-chevron-left"></i>
                        </a>
                    </li>

                    <c:forEach begin="1" end="${totalPages}" var="pageNumber">
                        <c:url var="detailPageUrl" value="${rolePath}/shelf/detail">
                            <c:param name="id" value="${shelf.id}" />
                            <c:param name="page" value="${pageNumber}" />
                        </c:url>
                        <li class="page-item ${pageNumber eq currentPage ? 'active' : ''}">
                            <a class="page-link" href="${detailPageUrl}">
                                <c:out value="${pageNumber}" />
                            </a>
                        </li>
                    </c:forEach>

                    <li class="page-item ${currentPage ge totalPages ? 'disabled' : ''}">
                        <c:url var="nextPageUrl" value="${rolePath}/shelf/detail">
                            <c:param name="id" value="${shelf.id}" />
                            <c:param name="page" value="${currentPage + 1}" />
                        </c:url>
                        <a class="page-link" href="${nextPageUrl}" aria-label="Trang sau">
                            <i class="fa-solid fa-chevron-right"></i>
                        </a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </div>
</main>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
