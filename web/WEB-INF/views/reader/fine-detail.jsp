<%-- Trang chi tiết khoản phạt thuộc user, do MyFineServlet render từ GET /fine/detail. Mong đợi request attribute fine,
    activePage và session loggedUser. --%>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
            <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
                <c:set var="pageTitle" value="Chi tiết khoản phạt — FPT Library" scope="request" />
                <c:set var="pageStylesheet" value="/assets/css/my-fines.css" scope="request" />
                <c:url var="backUrl" value="/fine/my" />
                <%@ include file="/WEB-INF/views/fragments/header.jsp" %>
                    <main class="fine-page">
                        <div class="container fine-content fine-detail-page">
                            <a class="fine-back" href="${backUrl}"><i class="fa-solid fa-arrow-left">

                                </i> Quay lại khoản phạt của tôi
                            </a>
                            <header class="fine-heading">
                                <span><i class="fa-solid fa-receipt"></i> Khoản phạt #F
                                    <c:out value="${fine.id}" />
                                </span>
                                <h1>Chi tiết khoản phạt</h1>
                                <p>Thông tin đầy đủ về khoản phạt thư viện này</p>
                            </header>
                            <section class="fine-detail-card">
                                <div class="fine-detail-title">
                                    <h2>
                                        <c:out value="${fine.borrowRecord.book.title}" />
                                    </h2>
                                    <span
                                        class="fine-badge ${fine.status == 'UNPAID' ? 'unpaid' : (fine.status == 'PAID' ? 'paid' : 'waived')}">
                                        <c:choose>
                                            <c:when test="${fine.status eq 'UNPAID'}">Chưa thanh toán</c:when>
                                            <c:when test="${fine.status eq 'PAID'}">Đã thanh toán</c:when>
                                            <c:otherwise>Đã miễn</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <dl>
                                    <div>
                                        <dt>Mã khoản phạt</dt>
                                        <dd>#F
                                            <c:out value="${fine.id}" />
                                        </dd>
                                    </div>
                                    <div>
                                        <dt>Lý do</dt>
                                        <dd>
                                            <c:out value="${fine.reason}" />
                                        </dd>
                                    </div>
                                    <div>
                                        <dt>Số tiền</dt>
                                        <dd class="fine-detail-amount">
                                            <fmt:formatNumber value="${fine.amount}" pattern="#,##0" /> VNĐ
                                        </dd>
                                    </div>
                                    <c:choose>
                                        <c:when test="${fine.fineType eq 'OVERDUE'}">
                                            <div>
                                                <dt>Số ngày quá hạn</dt>
                                                <dd>
                                                    <c:out value="${fine.overdueDays}" /> ngày × 5.000 VNĐ
                                                </dd>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div>
                                                <dt>Tình trạng cuốn sách</dt>
                                                <dd>
                                                    <c:out value="${fine.bookConditionLabel}" />
                                                </dd>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                    <div>
                                        <dt>Ngày mượn</dt>
                                        <dd>
                                            <c:out value="${fine.borrowRecord.borrowDate}" default="—" />
                                        </dd>
                                    </div>
                                    <div>
                                        <dt>Hạn trả</dt>
                                        <dd>
                                            <c:out value="${fine.borrowRecord.dueDate}" default="—" />
                                        </dd>
                                    </div>
                                    <div>
                                        <dt>Ngày trả</dt>
                                        <dd>
                                            <c:out value="${fine.borrowRecord.returnDate}" default="—" />
                                        </dd>
                                    </div>
                                    <div>
                                        <dt>Ngày tạo</dt>
                                        <dd>
                                            <c:out value="${fine.createdDate}" />
                                        </dd>
                                    </div>
                                    <div>
                                        <dt>Ngày thanh toán</dt>
                                        <dd>
                                            <c:out value="${fine.paidDate}" default="—" />
                                        </dd>
                                    </div>
                                    <div>
                                        <dt>Phương thức thanh toán</dt>
                                        <dd>
                                            <c:out value="${fine.paymentMethod}" default="—" />
                                        </dd>
                                    </div>
                                </dl>
                                <c:if test="${not empty fine.paymentNote}">
                                    <div class="fine-note">
                                        <strong>Ghi chú thanh toán</strong>
                                        <p>
                                            <c:out value="${fine.paymentNote}" />
                                        </p>
                                    </div>
                                </c:if>
                                <c:if test="${fine.status eq 'UNPAID'}">
                                    <c:choose>
                                        <c:when test="${not empty fine.borrowRecord.returnDate}">
                                            <div class="fine-detail-pay-box">
                                                <c:url var="payUrl" value="/vnpay-pay">
                                                    <c:param name="fineId" value="${fine.id}" />
                                                </c:url>
                                                <a class="fine-vnpay-btn fine-vnpay-btn-lg" href="${payUrl}">
                                                    <i class="fa-solid fa-credit-card"></i> Thanh toán khoản phạt qua VNPay
                                                </a>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="fine-alert fine-alert-warning" style="margin-top: 20px;">
                                                <i class="fa-solid fa-triangle-exclamation"></i>
                                                <span>Bạn cần trả sách cho thư viện trước khi có thể thực hiện thanh toán khoản phạt này.</span>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </section>
                        </div>
                    </main>
                    <%@ include file="/WEB-INF/views/fragments/footer.jsp" %>