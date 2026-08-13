<%-- Trang quản lý mượn trả do BorrowManagementServlet hiển thị. Mong đợi request attributes borrowList,
    borrowActionPrefix, totalPages, currentPageNum, selectedStatus và keyword; session attributes successMsg, errorMsg
    và loggedUser. --%>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
            <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

                <c:set var="isManagePageAttr" value="true" scope="request" />
                <c:set var="activePage" value="borrow" scope="request" />
                <c:set var="pageStylesheet" value="/assets/css/borrow-list.css" scope="request" />
                <c:url var="borrowListUrl" value="${borrowActionPrefix}/borrow/list" />
                <c:url var="confirmPickupUrl" value="${borrowActionPrefix}/borrow/confirm-pickup" />
                <c:url var="confirmReturnUrl" value="${borrowActionPrefix}/borrow/confirm-return" />
                <c:url var="createFineUrl" value="${borrowActionPrefix}/fine/create" />
                <c:url var="borrowListScriptUrl" value="/assets/js/borrow-list.js" />
                <%@ include file="/WEB-INF/views/fragments/header.jsp" %>

                    <main class="page-wrapper borrow-management-page">
                        <div class="container borrow-management-container">
                            <header class="borrow-page-header">
                                <h1 class="section-title">
                                    <i class="fa-solid fa-handshake"></i>
                                    Quản lý mượn trả sách
                                </h1>
                                <p class="section-subtitle">
                                    Phê duyệt yêu cầu mượn, ghi nhận trả sách và xử lý sự cố quá hạn hoặc mất sách.
                                </p>
                            </header>

                            <c:if test="${not empty sessionScope.successMsg}">
                                <div class="borrow-alert borrow-alert-success" role="status">
                                    <i class="fa-solid fa-circle-check"></i>
                                    <c:out value="${sessionScope.successMsg}" />
                                </div>
                                <c:remove var="successMsg" scope="session" />
                            </c:if>
                            <c:if test="${not empty sessionScope.errorMsg}">
                                <div class="borrow-alert borrow-alert-error" role="alert">
                                    <i class="fa-solid fa-circle-exclamation"></i>
                                    <c:out value="${sessionScope.errorMsg}" />
                                </div>
                                <c:remove var="errorMsg" scope="session" />
                            </c:if>

                            <section class="borrow-filter-card" aria-labelledby="borrow-filter-title">
                                <h2 id="borrow-filter-title" class="visually-hidden">Bộ lọc lượt mượn</h2>
                                <form class="borrow-filter-form" action="${borrowListUrl}" method="get">
                                    <label class="borrow-filter-keyword">
                                        <span>Tìm kiếm</span>
                                        <input type="search" name="keyword" maxlength="200"
                                            value="${fn:escapeXml(keyword)}"
                                            placeholder="Tên độc giả, tên sách, ISBN, mã vạch...">
                                    </label>
                                    <label>
                                        <span>Trạng thái</span>
                                        <select name="status">
                                            <option value="">Tất cả trạng thái</option>
                                            <option value="PENDING_PICKUP" ${selectedStatus eq 'PENDING_PICKUP'
                                                ? 'selected' : '' }>Chờ nhận sách</option>
                                            <option value="BORROWED" ${selectedStatus eq 'BORROWED' ? 'selected' : '' }>
                                                Đang mượn</option>
                                            <option value="RETURNED" ${selectedStatus eq 'RETURNED' ? 'selected' : '' }>
                                                Đã trả</option>
                                            <option value="OVERDUE" ${selectedStatus eq 'OVERDUE' ? 'selected' : '' }>
                                                Quá hạn</option>
                                            <option value="EXPIRED" ${selectedStatus eq 'EXPIRED' ? 'selected' : '' }>
                                                Hết hạn nhận</option>
                                            <option value="CANCELLED" ${selectedStatus eq 'CANCELLED' ? 'selected' : ''
                                                }>Đã hủy</option>
                                        </select>
                                    </label>
                                    <button type="submit" class="btn btn-primary borrow-filter-button">
                                        <i class="fa-solid fa-magnifying-glass"></i>
                                        Lọc kết quả
                                    </button>
                                </form>
                            </section>

                            <section class="borrow-table-card" aria-label="Danh sách lượt mượn trả">
                                <div class="borrow-table-scroll">
                                    <table class="borrow-table">
                                        <thead>
                                            <tr>
                                                <th>Mã</th>
                                                <th>Độc giả</th>
                                                <th>Thông tin sách</th>
                                                <th>Bản sao</th>
                                                <th>Ngày mượn / Hạn trả</th>
                                                <th>Trạng thái</th>
                                                <th class="borrow-table-actions-heading">Thao tác</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${empty borrowList}">
                                                    <tr>
                                                        <td class="borrow-empty-state" colspan="7">
                                                            <i class="fa-solid fa-folder-open"></i>
                                                            <span>Không tìm thấy lượt mượn trả sách nào.</span>
                                                        </td>
                                                    </tr>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach var="borrowRecord" items="${borrowList}">
                                                        <tr>
                                                            <td class="borrow-record-id">
                                                                <c:out value="${borrowRecord.id}" />
                                                            </td>
                                                            <td>
                                                                <strong>
                                                                    <c:out value="${borrowRecord.user.fullName}" />
                                                                </strong>
                                                                <small>
                                                                    @
                                                                    <c:out value="${borrowRecord.user.username}" />
                                                                    <c:if test="${not empty borrowRecord.user.phone}">
                                                                        ·
                                                                        <c:out value="${borrowRecord.user.phone}" />
                                                                    </c:if>
                                                                </small>
                                                            </td>
                                                            <td>
                                                                <strong>
                                                                    <c:out value="${borrowRecord.book.title}" />
                                                                </strong>
                                                                <small>ISBN:
                                                                    <c:out value="${borrowRecord.book.isbn}" />
                                                                </small>
                                                            </td>
                                                            <td>
                                                                <c:choose>
                                                                    <c:when test="${not empty borrowRecord.bookCopy}">
                                                                        <span class="borrow-barcode">
                                                                            <c:out
                                                                                value="${borrowRecord.bookCopy.barcode}" />
                                                                        </span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="borrow-muted">Chưa gán bản
                                                                            sao</span>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td>
                                                                <span>
                                                                    <c:out
                                                                        value="${empty borrowRecord.borrowDate ? '-' : borrowRecord.borrowDate}" />
                                                                </span>
                                                                <small>Hạn:
                                                                    <c:out
                                                                        value="${empty borrowRecord.dueDate ? '-' : borrowRecord.dueDate}" />
                                                                </small>
                                                            </td>
                                                            <td>
                                                                <span
                                                                    class="borrow-status borrow-status-${fn:toLowerCase(borrowRecord.status)}">
                                                                    <c:choose>
                                                                        <c:when
                                                                            test="${borrowRecord.status eq 'PENDING_PICKUP'}">
                                                                            Chờ nhận sách</c:when>
                                                                        <c:when
                                                                            test="${borrowRecord.status eq 'BORROWED'}">
                                                                            Đang mượn</c:when>
                                                                        <c:when
                                                                            test="${borrowRecord.status eq 'RETURNED'}">
                                                                            Đã trả</c:when>
                                                                        <c:when
                                                                            test="${borrowRecord.status eq 'OVERDUE'}">
                                                                            Quá hạn</c:when>
                                                                        <c:when
                                                                            test="${borrowRecord.status eq 'EXPIRED'}">
                                                                            Hết hạn nhận</c:when>
                                                                        <c:when
                                                                            test="${borrowRecord.status eq 'CANCELLED'}">
                                                                            Đã hủy</c:when>
                                                                        <c:otherwise>
                                                                            <c:out value="${borrowRecord.status}" />
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </span>
                                                            </td>
                                                            <td class="borrow-table-actions">
                                                                <c:choose>
                                                                    <c:when
                                                                        test="${borrowRecord.status eq 'PENDING_PICKUP'}">
                                                                        <button type="button"
                                                                            class="btn btn-sm btn-primary"
                                                                            data-open-loan-modal
                                                                            data-record-id="${borrowRecord.id}"
                                                                            data-book-title="${fn:escapeXml(borrowRecord.book.title)}"
                                                                            data-reader-name="${fn:escapeXml(borrowRecord.user.fullName)}"
                                                                            data-barcode="${empty borrowRecord.bookCopy ? '-' : fn:escapeXml(borrowRecord.bookCopy.barcode)}"
                                                                            data-request-date="${borrowRecord.requestDate}"
                                                                            data-pickup-deadline="${borrowRecord.pickupDeadline}">
                                                                            <i class="fa-solid fa-check"></i> Xác nhận
                                                                            giao sách
                                                                        </button>
                                                                    </c:when>
                                                                    <c:when
                                                                        test="${borrowRecord.status eq 'BORROWED' || borrowRecord.status eq 'OVERDUE'}">
                                                                        <div class="borrow-action-group">
                                                                            <button type="button"
                                                                                class="btn btn-sm btn-success"
                                                                                data-open-return-modal
                                                                                data-record-id="${borrowRecord.id}"
                                                                                data-book-title="${fn:escapeXml(borrowRecord.book.title)}"
                                                                                data-barcode="${fn:escapeXml(borrowRecord.bookCopy.barcode)}">
                                                                                <i class="fa-solid fa-rotate-left"></i>
                                                                                Nhận trả
                                                                            </button>
                                                                            <c:if test="${not borrowRecord.hasFine}">
                                                                                <button type="button"
                                                                                    class="btn btn-sm borrow-fine-button"
                                                                                    data-open-fine-modal
                                                                                    data-record-id="${borrowRecord.id}"
                                                                                    data-user-id="${borrowRecord.userId}"
                                                                                    data-reader-name="${fn:escapeXml(borrowRecord.user.fullName)}"
                                                                                    data-book-title="${fn:escapeXml(borrowRecord.book.title)}"
                                                                                    data-book-price="${empty borrowRecord.book.price ? 0 : borrowRecord.book.price}">
                                                                                    <i
                                                                                        class="fa-solid fa-circle-dollar-to-slot"></i>
                                                                                    Phạt
                                                                                </button>
                                                                            </c:if>
                                                                        </div>
                                                                    </c:when>
                                                                    <c:otherwise><span class="borrow-muted">Hoàn
                                                                            tất</span></c:otherwise>
                                                                </c:choose>
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
                                <nav class="borrow-pagination" aria-label="Phân trang mượn sách">
                                    <c:if test="${currentPageNum gt 1}">
                                        <c:url var="previousPageUrl" value="${borrowActionPrefix}/borrow/list">
                                            <c:param name="status" value="${selectedStatus}" />
                                            <c:param name="keyword" value="${keyword}" />
                                            <c:param name="page" value="${currentPageNum - 1}" />
                                        </c:url>
                                        <a href="${previousPageUrl}" aria-label="Trang trước"><i
                                                class="fa-solid fa-chevron-left"></i></a>
                                    </c:if>
                                    <c:forEach begin="1" end="${totalPages}" var="pageNumber">
                                        <c:url var="pageUrl" value="${borrowActionPrefix}/borrow/list">
                                            <c:param name="status" value="${selectedStatus}" />
                                            <c:param name="keyword" value="${keyword}" />
                                            <c:param name="page" value="${pageNumber}" />
                                        </c:url>
                                        <a class="${pageNumber eq currentPageNum ? 'current' : ''}" href="${pageUrl}">
                                            <c:out value="${pageNumber}" />
                                        </a>
                                    </c:forEach>
                                    <c:if test="${currentPageNum lt totalPages}">
                                        <c:url var="nextPageUrl" value="${borrowActionPrefix}/borrow/list">
                                            <c:param name="status" value="${selectedStatus}" />
                                            <c:param name="keyword" value="${keyword}" />
                                            <c:param name="page" value="${currentPageNum + 1}" />
                                        </c:url>
                                        <a href="${nextPageUrl}" aria-label="Trang sau"><i
                                                class="fa-solid fa-chevron-right"></i></a>
                                    </c:if>
                                </nav>
                            </c:if>
                        </div>
                    </main>

                    <div class="borrow-modal" data-borrow-modal="loan" hidden>
                        <section class="borrow-modal-dialog" role="dialog" aria-modal="true"
                            aria-labelledby="loan-modal-title">
                            <h2 id="loan-modal-title"><i class="fa-solid fa-book-open"></i> Xác nhận giao sách</h2>
                            <p>Xác nhận độc giả đã đến nhận đúng bản sao đang được thư viện giữ.</p>
                            <form action="${confirmPickupUrl}" method="post">
                                <input type="hidden" name="id" data-loan-field="recordId">
                                <label><span>Độc giả</span><input type="text" readonly
                                        data-loan-field="readerName"></label>
                                <label><span>Đầu sách mượn</span><input type="text" readonly
                                        data-loan-field="bookTitle"></label>
                                <div class="borrow-modal-grid">
                                    <label><span>Mã yêu cầu</span><input type="text" readonly
                                            data-loan-field="requestId"></label>
                                    <label><span>Mã vạch bản sao</span><input class="borrow-monospace" type="text"
                                            readonly data-loan-field="barcode"></label>
                                    <label><span>Thời điểm yêu cầu</span><input type="text" readonly
                                            data-loan-field="requestDate"></label>
                                    <label><span>Hạn cuối nhận sách</span><input class="borrow-deadline" type="text"
                                            readonly data-loan-field="pickupDeadline"></label>
                                </div>
                                <div class="borrow-modal-actions">
                                    <button type="button" class="btn btn-secondary" data-close-borrow-modal>Hủy
                                        bỏ</button>
                                    <button type="submit" class="btn btn-primary">Xác nhận duyệt</button>
                                </div>
                            </form>
                        </section>
                    </div>

                    <div class="borrow-modal" data-borrow-modal="return" hidden>
                        <section class="borrow-modal-dialog" role="dialog" aria-modal="true"
                            aria-labelledby="return-modal-title">
                            <h2 id="return-modal-title"><i class="fa-solid fa-rotate-left borrow-success-icon"></i> Nhận
                                trả sách</h2>
                            <p>Ghi nhận cuốn sách đã được hoàn trả về thư viện.</p>
                            <form action="${confirmReturnUrl}" method="post">
                                <input type="hidden" name="id" data-return-field="recordId">
                                <label><span>Tên sách</span><input type="text" readonly
                                        data-return-field="bookTitle"></label>
                                <label><span>Mã vạch</span><input class="borrow-monospace" type="text" readonly
                                        data-return-field="barcode"></label>
                                <label>
                                    <span>Tình trạng cuốn sách</span>
                                    <select name="condition">
                                        <option value="GOOD">Bình thường (GOOD)</option>
                                        <option value="DAMAGED">Hỏng nhẹ (DAMAGED)</option>
                                        <option value="LOST">Mất hoàn toàn (LOST)</option>
                                    </select>
                                </label>
                                <label><span>Ghi chú bổ sung</span><textarea name="note" rows="3"
                                        placeholder="Tình trạng trang sách, ghi chú hao mòn..."></textarea></label>
                                <div class="borrow-modal-actions">
                                    <button type="button" class="btn btn-secondary" data-close-borrow-modal>Hủy
                                        bỏ</button>
                                    <button type="submit" class="btn btn-success">Xác nhận trả</button>
                                </div>
                            </form>
                        </section>
                    </div>

                    <div class="borrow-modal" data-borrow-modal="fine" hidden>
                        <section class="borrow-modal-dialog" role="dialog" aria-modal="true"
                            aria-labelledby="fine-modal-title">
                            <h2 id="fine-modal-title"><i
                                    class="fa-solid fa-circle-dollar-to-slot borrow-danger-icon"></i> Lập phiếu phạt độc
                                giả</h2>
                            <p>Tạo khoản phạt do trả quá hạn hoặc làm hỏng, mất sách.</p>
                            <form action="${createFineUrl}" method="post">
                                <input type="hidden" name="borrowRecordId" data-fine-field="recordId">
                                <input type="hidden" name="userId" data-fine-field="userId">
                                <label><span>Độc giả bị phạt</span><input type="text" readonly
                                        data-fine-field="readerName"></label>
                                <label><span>Tên đầu sách</span><input type="text" readonly
                                        data-fine-field="bookTitle"></label>
                                <label>
                                    <span>Tình trạng cuốn sách</span>
                                    <select name="bookCondition" required data-fine-field="condition">
                                        <option value="DAMAGED">Hỏng nhẹ</option>
                                        <option value="LOST">Mất sách</option>
                                    </select>
                                </label>
                                <label>
                                    <span>Số tiền phạt (đ)</span>
                                    <input type="number" name="amount" required min="1" step="1"
                                        data-fine-field="amount">
                                    <small>Hệ thống tự điền theo tình trạng sách; thủ thư có thể điều chỉnh khi
                                        cần.</small>
                                </label>
                                <label><span>Lý do phạt</span><input type="text" name="reason" required maxlength="255"
                                        placeholder="Mô tả tình trạng hỏng hoặc mất sách..."></label>
                                <div class="borrow-modal-actions">
                                    <button type="button" class="btn btn-secondary" data-close-borrow-modal>Hủy
                                        bỏ</button>
                                    <button type="submit" class="btn btn-danger">Lập phiếu</button>
                                </div>
                            </form>
                        </section>
                    </div>

                    <script src="${borrowListScriptUrl}" defer></script>
                    <%@ include file="/WEB-INF/views/fragments/footer.jsp" %>