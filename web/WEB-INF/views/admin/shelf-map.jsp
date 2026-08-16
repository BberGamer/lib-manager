<%--
    Bản đồ kệ do ShelfManagementServlet hiển thị.
    Nhận shelfList lấy từ database và rolePath cho các liên kết theo vai trò.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="shelf" scope="request" />
<c:set var="pageTitle" value="Bản đồ kệ – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/shelf-map.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<c:url var="shelfListUrl" value="${rolePath}/shelf" />

<main class="shelf-page">
    <div class="shelf-content">
        <header class="map-heading">
            <a class="back" href="${shelfListUrl}">
                <i class="fa-solid fa-arrow-left"></i>&nbsp; Danh sách kệ
            </a>
            <h1>Bản đồ kệ</h1>
            <p>Nhóm theo tầng và khu vực từ dữ liệu thực tế.</p>
        </header>

        <c:choose>
            <c:when test="${empty shelfList}">
                <p class="map-empty">Chưa có dữ liệu kệ để hiển thị.</p>
            </c:when>
            <c:otherwise>
                <c:forEach var="floor" begin="1" end="100">
                    <c:set var="hasFloor" value="false" />
                    <c:forEach var="candidateShelf" items="${shelfList}">
                        <c:if test="${candidateShelf.floorNumber eq floor}">
                            <c:set var="hasFloor" value="true" />
                        </c:if>
                    </c:forEach>

                    <c:if test="${hasFloor}">
                        <section class="floor">
                            <h2>Tầng <c:out value="${floor}" /></h2>
                            <div class="shelf-grid">
                                <c:forEach var="shelf" items="${shelfList}">
                                    <c:if test="${shelf.floorNumber eq floor}">
                                        <c:url var="shelfDetailUrl" value="${rolePath}/shelf/detail">
                                            <c:param name="id" value="${shelf.id}" />
                                        </c:url>
                                        <c:set var="occupancyClass"
                                               value="${shelf.bookCount ge shelf.capacity
                                                        ? 'full'
                                                        : (shelf.bookCount * 100 ge shelf.capacity * 80
                                                           ? 'near-full' : 'available')}" />
                                        <a id="shelf-${shelf.id}"
                                           class="shelf-block ${shelf.status eq 'INACTIVE'
                                                                 ? 'inactive' : occupancyClass}"
                                           href="${shelfDetailUrl}">
                                            <small><c:out value="${shelf.area}" /></small>
                                            <strong><c:out value="${shelf.code}" /></strong>
                                            <span><c:out value="${shelf.name}" /></span>
                                            <b>
                                                <c:out value="${shelf.bookCount}" />/<c:out
                                                        value="${shelf.capacity}" />
                                            </b>
                                            <em>
                                                ${shelf.status eq 'INACTIVE'
                                                  ? 'Ngừng sử dụng'
                                                  : (shelf.bookCount ge shelf.capacity ? 'Đã đầy' : 'Còn chỗ')}
                                            </em>
                                        </a>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </section>
                    </c:if>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</main>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
