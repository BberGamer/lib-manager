<%--
    Trang danh sách khoản phạt của user, do MyFineServlet render từ GET /fine/my.
    Mong đợi request attributes finePage, selectedStatus, keyword, activePage và session loggedUser.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Khoản phạt của tôi — FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/my-fines.css" scope="request" />
<c:url var="myFinesUrl" value="/fine/my" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<main class="fine-page">
    <div class="container fine-content">
        <header class="fine-heading">
            <span>
                <i class="fa-solid fa-coins"> </i> 
                Khoản phạt thư viện
            </span>
            <h1>Khoản phạt của tôi</h1>
            <p>Xem và theo dõi các khoản phạt thư viện của bạn</p>
        </header>
        <section class="fine-summary" aria-label="Tổng quan khoản phạt">
            <article><span>Tổng số khoản phạt</span>
                <strong><c:out value="${finePage.totalFines}" /></strong>
            </article>
            <article class="fine-summary-unpaid">
                <span>Số tiền chưa thanh toán</span>
                <strong><fmt:formatNumber value="${finePage.unpaidAmount}" pattern="#,##0" /> VNĐ</strong>
            </article>
            <article class="fine-summary-paid">
                <span>Số tiền đã thanh toán</span>
                <strong><fmt:formatNumber value="${finePage.paidAmount}" pattern="#,##0" /> VNĐ</strong>
            </article>
            <article>
                <span>Khoản phạt chưa thanh toán</span> 
                <strong><c:out value="${finePage.unpaidCount}" /></strong>
            </article>
        </section>
        <form class="fine-filters" action="${myFinesUrl}" method="get">
            <label>
                <span>Trạng thái</span>
                <select name="status">
                    <option value="ALL">Tất cả</option>
                    <option value="UNPAID" ${selectedStatus eq 'UNPAID' ? 'selected' : ''}>Chưa thanh toán</option>
                    <option value="PAID" ${selectedStatus eq 'PAID' ? 'selected' : ''}>Đã thanh toán</option>
                    <option value="WAIVED" ${selectedStatus eq 'WAIVED' ? 'selected' : ''}>Đã miễn</option>
                </select>
            </label>
            <label class="fine-search">
                <span>Tìm kiếm</span>
                <input name="keyword" value="${fn:escapeXml(keyword)}" placeholder="Tìm theo tên sách hoặc mã khoản phạt..."></label>
            <button type="submit">
                <i class="fa-solid fa-magnifying-glass"></i> 
                Tìm kiếm
            </button>
            <a href="${myFinesUrl}">Đặt lại</a>
        </form>
        <c:choose><c:when test="${not empty finePage.fines}">
                <div class="fine-table-wrap">
                    <table class="fine-table">
                        <thead>
                            <tr>
                                <th>Mã phạt</th>
                                <th>Sách</th>
                                <th>Lý do</th>
                                <th>Ngày mượn</th>
                                <th>Hạn trả</th>
                                <th>Ngày trả</th>
                                <th>Chi tiết phạt</th>
                                <th>Số tiền</th>
                                <th>Trạng thái</th>
                                <th>Ngày tạo</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="fine" items="${finePage.fines}">
                                <c:url var="detailUrl" value="/fine/detail">
                                    <c:param name="id" value="${fine.id}" />
                                </c:url>
                                <tr>
                                    <td data-label="Mã phạt">
                                        <strong>#F<c:out value="${fine.id}" /></strong>
                                    </td>
                                    <td data-label="Sách">
                                        <c:out value="${fine.borrowRecord.book.title}" />
                                    </td>
                                    <td data-label="Lý do">
                                        <c:out value="${fine.reason}" />
                                    </td>
                                    <td data-label="Ngày mượn">
                                        <c:out value="${fine.borrowRecord.borrowDate}" default="—" />
                                    </td>
                                    <td data-label="Hạn trả">
                                        <c:out value="${fine.borrowRecord.dueDate}" default="—" />
                                    </td>
                                    <td data-label="Ngày trả">
                                        <c:out value="${fine.borrowRecord.returnDate}" default="—" />
                                    </td>
                                    <td data-label="Chi tiết phạt">
                                        <c:choose>
                                            <c:when test="${fine.fineType eq 'OVERDUE'}">
                                                Quá hạn <c:out value="${fine.overdueDays}" /> ngày
                                                (5.000 VNĐ/ngày)
                                            </c:when>
                                            <c:otherwise>
                                                <c:out value="${fine.bookConditionLabel}" />
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td data-label="Số tiền">
                                        <strong>
                                            <fmt:formatNumber value="${fine.amount}" pattern="#,##0" />
                                            VNĐ
                                        </strong>
                                    </td>
                                    <td data-label="Trạng thái">
                                        <span class="fine-badge ${fine.status == 'UNPAID' ? 'unpaid' : (fine.status == 'PAID' ? 'paid' : 'waived')}">
                                            <c:choose>
                                                <c:when test="${fine.status eq 'UNPAID'}">Chưa thanh toán</c:when>
                                                <c:when test="${fine.status eq 'PAID'}">Đã thanh toán</c:when>
                                                <c:otherwise>Đã miễn</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>
                                    <td data-label="Ngày tạo">
                                        <c:out value="${fine.createdDate}" />
                                    </td>
                                    <td data-label="Thao tác">
                                        <a class="fine-detail-link" href="${detailUrl}">Xem chi tiết</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <section class="fine-empty">
                    <i class="fa-solid fa-circle-check"></i>
                    <h2>${empty keyword and selectedStatus eq 'ALL' ? 'Không có khoản phạt' : 'Không tìm thấy khoản phạt phù hợp.'}
                    </h2>
                    <c:if test="${empty keyword and selectedStatus eq 'ALL'}">
                        <p>Hiện tại bạn không có khoản phạt thư viện nào.
                            <br>Hãy tiếp tục trả sách đúng hạn.
                        </p>
                    </c:if>
                </section>
            </c:otherwise>
        </c:choose>
    </div></main><%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
