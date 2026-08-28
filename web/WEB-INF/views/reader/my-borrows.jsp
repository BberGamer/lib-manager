<%--
    Trang theo dõi sách đang mượn và lịch sử mượn của độc giả.
    Controller render: MyBorrowServlet (GET /borrow/my).
    Mong đợi request attributes: borrowPage, maximumActiveBorrows, maximumRenewals,
    activePage; session attribute: loggedUser và thông báo flash tùy chọn. Trang gửi
    thao tác báo mất tới POST /borrow/my/report-lost.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Sách đang mượn — FPT Library" scope="request" />
<c:set var="pageDesc" value="Theo dõi tình trạng mượn, hạn trả và lịch sử mượn sách." scope="request" />
<c:set var="pageStylesheet" value="/assets/css/my-borrows.css" scope="request" />
<c:url var="renewUrl" value="/borrow/my/renew" />
<c:url var="cancelUrl" value="/borrow/cancel" />
<c:url var="reportLostUrl" value="/borrow/my/report-lost" />
<c:url var="myBorrowScriptUrl" value="/assets/js/my-borrows.js" />

<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="borrow-page">
    <section class="borrow-hero">
        <div class="container borrow-hero-inner">
            <div>
                <div class="borrow-eyebrow"><i class="fa-solid fa-book-open"></i> Mượn sách</div>
                <h1>Sách đang mượn</h1>
                <p>Theo dõi tình trạng mượn và gia hạn sách của bạn</p>
            </div>
            <div class="borrow-hero-counts" aria-label="Tổng quan mượn sách">
                <div>
                    <strong>
                        <c:out value="${borrowPage.activeRecords.size()}" />
                    </strong>
                    <span>Đang hoạt động</span>
                </div>
                <div>
                    <strong>
                        <c:out value="${maximumActiveBorrows}" />
                    </strong>
                    <span>Giới hạn</span>
                </div>
            </div>
        </div>
    </section>

    <div class="container borrow-content">
        <c:if test="${not empty sessionScope.borrowSuccessMessage}">
            <div class="borrow-alert borrow-alert-success" role="status">
                <i class="fa-solid fa-circle-check"></i>
                <c:out value="${sessionScope.borrowSuccessMessage}" />
            </div>
            <c:remove var="borrowSuccessMessage" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.borrowErrorMessage}">
            <div class="borrow-alert borrow-alert-error" role="alert">
                <i class="fa-solid fa-circle-exclamation"></i>
                <c:out value="${sessionScope.borrowErrorMessage}" />
            </div>
            <c:remove var="borrowErrorMessage" scope="session" />
        </c:if>

        <section class="borrow-summary" aria-label="Chỉ số mượn sách">
            <article class="borrow-summary-card borrow-summary-limit ${borrowPage.activeRecords.size() ge maximumActiveBorrows ? 'is-limit-reached' : 'is-under-limit'}">
                <span>Đang hoạt động / Giới hạn</span>
                <strong><c:out value="${borrowPage.activeRecords.size()}" /> /
                    <c:out value="${maximumActiveBorrows}" /></strong>
                    <c:choose>
                        <c:when test="${borrowPage.activeRecords.size() ge maximumActiveBorrows}">
                        <small class="limit-reached"><i class="fa-solid fa-triangle-exclamation"></i>
                            Đã đạt giới hạn</small>
                        </c:when>
                        <c:otherwise><small>Bạn vẫn có thể mượn thêm sách</small></c:otherwise>
                </c:choose>
            </article>
            <article class="borrow-summary-card borrow-summary-due">
                <span>Sắp đến hạn (7 ngày)</span>
                <strong><c:out value="${borrowPage.upcomingDueCount}" /></strong>
                <small>cuốn cần trả sớm</small>
            </article>
        </section>

        <section class="borrow-section" aria-labelledby="active-borrows-title">
            <h2 id="active-borrows-title">
                <i class="fa-solid fa-book-open"></i>
                Đang mượn
            </h2>
            <div class="borrow-table-wrap">
                <table class="borrow-table">
                    <thead>
                        <tr>
                            <th>Sách</th>
                            <th>Ngày mượn</th>
                            <th>Hạn trả</th>
                            <th>Gia hạn</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="record" items="${borrowPage.activeRecords}">
                            <tr>
                                <td data-label="Sách">
                                    <strong><c:out value="${record.book.title}" /></strong>
                                    <small>Mã vạch: <c:out value="${record.bookCopy.barcode}" default="—" /></small>
                                    <c:if test="${record.status eq 'PENDING_PICKUP'}">
                                        <small>Nhận trước: <c:out value="${record.pickupDeadline}" /></small>
                                    </c:if>
                                </td>
                                <td data-label="Ngày mượn">
                                    <c:out value="${record.borrowDate}" default="—" />
                                </td>
                                <td data-label="Hạn trả" class="${record.status eq 'OVERDUE' ? 'borrow-date-overdue' : ''}">
                                    <strong><c:out value="${record.dueDate}" default="—" /></strong>
                                    <c:if test="${record.status eq 'OVERDUE'}">
                                        <small><i class="fa-solid fa-triangle-exclamation"></i> Quá hạn!</small>
                                    </c:if>
                                </td>
                                <td data-label="Gia hạn">
                                    <c:out value="${record.renewalCount}" /> /
                                    <c:out value="${maximumRenewals}" /> lần
                                </td>
                                <td data-label="Trạng thái">
                                    <c:choose>
                                        <c:when test="${record.status eq 'OVERDUE'}">
                                            <span class="borrow-status overdue">Quá hạn</span>
                                        </c:when>
                                        <c:when test="${record.status eq 'PENDING_PICKUP'}">
                                            <span class="borrow-status pending">Chờ nhận sách</span>
                                        </c:when>
                                        <c:otherwise><span class="borrow-status borrowing">Đang mượn</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td data-label="Thao tác">
                                    <c:choose>
                                        <c:when test="${record.status eq 'PENDING_PICKUP'}">
                                            <form action="${cancelUrl}" method="post" class="renew-form">
                                                <input type="hidden" name="borrowId" value="${record.id}">
                                                <button type="submit" class="renew-button">Hủy yêu cầu</button>
                                            </form>
                                        </c:when>
                                        <c:when test="${record.status eq 'BORROWED'
                                                and borrowPage.renewalBlockedBorrowIds.contains(record.id)}">
                                            <span class="renew-unavailable">Không thể gia hạn</span>
                                            <small class="renew-reservation-note">
                                                Khoảng gia hạn trùng slot đặt trước và không còn bản sao khác.
                                            </small>
                                        </c:when>
                                        <c:when test="${borrowPage.renewalEligibleBorrowIds.contains(record.id)}">
                                            <form action="${renewUrl}" method="post" class="renew-form">
                                                <input type="hidden" name="borrowRecordId" value="${record.id}">
                                                <button type="submit" class="renew-button">
                                                    <i class="fa-solid fa-rotate"></i> Gia hạn
                                                </button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="renew-unavailable">Không thể gia hạn</span>
                                        </c:otherwise>
                                    </c:choose>
                                    <c:if test="${record.status eq 'BORROWED' or record.status eq 'OVERDUE'}">
                                        <div class="borrow-lost-action">
                                            <button type="button" class="borrow-lost-button"
                                                    data-open-lost-dialog
                                                    data-borrow-record-id="${record.id}">
                                                <i class="fa-solid fa-triangle-exclamation"></i>
                                                Báo mất sách
                                            </button>
                                        </div>
                                    </c:if>
                                    <c:if test="${record.status eq 'BORROWED' or record.status eq 'OVERDUE'}">
                                        <div class="borrow-review-action">
                                            <c:choose>
                                                <c:when test="${reviewedBorrowIds.contains(record.id)}">
                                                    <a class="borrow-review-link is-reviewed"
                                                       href="${pageContext.request.contextPath}/book/detail?id=${record.bookId}">
                                                        <i class="fa-solid fa-star"></i> Đã đánh giá
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <a class="borrow-review-link is-pending"
                                                       href="${pageContext.request.contextPath}/book/detail?id=${record.bookId}&borrowId=${record.id}">
                                                        <i class="fa-solid fa-pen-to-square"></i> Viết đánh giá
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty borrowPage.activeRecords}">
                            <tr>
                                <td colspan="6" class="borrow-empty">
                                    <i class="fa-solid fa-book"></i>
                                    <span>Bạn chưa có sách đang mượn.</span>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="borrow-section borrow-history" aria-labelledby="borrow-history-title">
            <h2 id="borrow-history-title">
                <i class="fa-solid fa-clock-rotate-left"></i> Lịch sử mượn
            </h2>
            <div class="borrow-table-wrap">
                <table class="borrow-table">
                    <thead>
                        <tr>
                            <th>Sách</th>
                            <th>Ngày mượn</th>
                            <th>Ngày trả</th>
                            <th>Trạng thái</th>
                            <th>Đánh giá</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="record" items="${borrowPage.historyRecords}">
                            <tr>
                                <td data-label="Sách">
                                    <strong><c:out value="${record.book.title}" /></strong>
                                    <small>Mã vạch: <c:out value="${record.bookCopy.barcode}" default="—" /></small>
                                </td>
                                <td data-label="Ngày mượn">
                                    <c:out value="${record.borrowDate}" default="—" />
                                </td>
                                <td data-label="Ngày trả">
                                    <c:out value="${record.returnDate}" default="—" />
                                </td>
                                <td data-label="Trạng thái">
                                    <span class="borrow-status ${record.status eq 'LOST' ? 'lost' : 'returned'}">
                                        <c:choose>
                                            <c:when test="${record.status eq 'RETURNED'}">Đã trả</c:when>
                                            <c:when test="${record.status eq 'EXPIRED'}">Hết hạn nhận</c:when>
                                            <c:when test="${record.status eq 'LOST'}">Đã báo mất</c:when>
                                            <c:otherwise>Đã hủy</c:otherwise>
                                        </c:choose>
                                    </span>
                                </td>
                                <td data-label="Đánh giá">
                                    <c:choose>
                                        <c:when test="${record.status eq 'RETURNED'}">
                                            <c:choose>
                                                <c:when test="${reviewedBorrowIds.contains(record.id)}">
                                                    <a class="borrow-review-link is-reviewed"
                                                       href="${pageContext.request.contextPath}/book/detail?id=${record.bookId}">
                                                        <i class="fa-solid fa-star"></i> Đã đánh giá
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <a class="borrow-review-link is-history-pending"
                                                       href="${pageContext.request.contextPath}/book/detail?id=${record.bookId}&borrowId=${record.id}">
                                                        Viết đánh giá
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="borrow-review-empty">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty borrowPage.historyRecords}">
                            <tr>
                                <td colspan="5" class="borrow-empty">
                                    <i class="fa-solid fa-clock-rotate-left"></i>
                                    <span>Chưa có lịch sử mượn sách.</span>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</main>

<dialog class="borrow-lost-dialog" data-lost-dialog aria-labelledby="borrow-lost-dialog-title">
    <section class="borrow-lost-dialog-content">
        <button type="button" class="borrow-lost-dialog-close" data-close-lost-dialog
                aria-label="Đóng cửa sổ xác nhận">
            <i class="fa-solid fa-xmark"></i>
        </button>
        <div class="borrow-lost-dialog-icon" aria-hidden="true">
            <i class="fa-solid fa-book-skull"></i>
        </div>
        <h2 id="borrow-lost-dialog-title">Xác nhận báo mất sách</h2>
        <p>
            Bạn đang báo mất cuốn <strong data-lost-book-title></strong>.
            phạt 100% giá trị cuốn sách.
        </p>
        <form action="${reportLostUrl}" method="post">
            <input type="hidden" name="borrowRecordId" data-lost-borrow-id>
            <div class="borrow-lost-dialog-actions">
                <button type="button" class="borrow-lost-dialog-cancel" data-close-lost-dialog>
                    Quay lại
                </button>
                <button type="submit" class="borrow-lost-dialog-confirm">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                    Xác nhận báo mất
                </button>
            </div>
        </form>
    </section>
</dialog>

<script src="${myBorrowScriptUrl}" defer></script>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
