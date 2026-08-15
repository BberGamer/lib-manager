<%--
    Hộp thư thông báo cá nhân dành cho Độc giả.
    Nhận các thuộc tính request: notificationList, totalRecords, totalPages, currentPageNum.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="activePage" value="notifications" scope="request" />
<c:set var="pageTitle" value="Hộp thư thông báo của bạn – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/notification.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="page-wrapper">
    <div class="container inbox-container">
        <div class="inbox-header">
            <div>
                <h1 class="section-title">
                    <i class="fa-solid fa-bell"></i> Hộp thư thông báo
                </h1>
                <p class="section-subtitle">
                    Theo dõi các cập nhật về hạn trả sách, phí phạt và thông báo hệ thống
                </p>
            </div>
            <c:if test="${not empty notificationList}">
                <form action="${pageContext.request.contextPath}/notification/read-all" method="post">
                    <button type="submit" class="btn btn-outline">
                        <i class="fa-solid fa-check-double"></i> Đọc tất cả
                    </button>
                </form>
            </c:if>
        </div>

        <!-- Flash Message Alerts -->
        <c:if test="${not empty sessionScope.successMsg}">
            <div class="alert alert-success">
                <i class="fa-solid fa-circle-check"></i>
                <c:out value="${sessionScope.successMsg}" />
            </div>
            <c:remove var="successMsg" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.errorMsg}">
            <div class="alert alert-error">
                <i class="fa-solid fa-circle-exclamation"></i>
                <c:out value="${sessionScope.errorMsg}" />
            </div>
            <c:remove var="errorMsg" scope="session" />
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-error">
                <i class="fa-solid fa-circle-exclamation"></i>
                <c:out value="${error}" />
            </div>
        </c:if>

        <!-- Notification List -->
        <div class="inbox-list">
            <c:choose>
                <c:when test="${empty notificationList}">
                    <div class="inbox-empty-card">
                        <i class="fa-regular fa-bell-slash"></i>
                        <h4>Hộp thư trống</h4>
                        <p>Bạn không có bất kỳ thông báo nào tại thời điểm này.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="notif" items="${notificationList}">
                        <c:set var="iconClass" value="fa-bell" />
                        <c:set var="iconTypeClass" value="type-default" />
                        
                        <c:choose>
                            <c:when test="${notif.type eq 'DUE_REMINDER'}">
                                <c:set var="iconClass" value="fa-clock" />
                                <c:set var="iconTypeClass" value="type-due" />
                            </c:when>
                            <c:when test="${notif.type eq 'OVERDUE'}">
                                <c:set var="iconClass" value="fa-circle-exclamation" />
                                <c:set var="iconTypeClass" value="type-overdue" />
                            </c:when>
                            <c:when test="${notif.type eq 'FINE'}">
                                <c:set var="iconClass" value="fa-circle-dollar-to-slot" />
                                <c:set var="iconTypeClass" value="type-fine" />
                            </c:when>
                            <c:when test="${notif.type eq 'RESERVATION'}">
                                <c:set var="iconClass" value="fa-bookmark" />
                                <c:set var="iconTypeClass" value="type-reservation" />
                            </c:when>
                        </c:choose>

                        <div class="inbox-item ${not notif.isRead ? 'unread' : ''}"
                             id="notif-item-${notif.id}"
                             onclick="markNotificationAsRead(${notif.id}, ${notif.isRead}, '${pageContext.request.contextPath}')">
                            
                            <!-- Icon Area -->
                            <div class="inbox-item-icon ${iconTypeClass}">
                                <i class="fa-solid ${iconClass}"></i>
                            </div>

                            <!-- Content Area -->
                            <div class="inbox-item-content">
                                <div class="inbox-item-header">
                                    <h4 class="inbox-item-title">
                                        <c:out value="${notif.title}" />
                                    </h4>
                                    <span class="inbox-item-date">
                                        <c:out value="${notif.createdAt}" />
                                    </span>
                                </div>
                                <p class="inbox-item-message">
                                    <c:out value="${notif.message}" />
                                </p>

                                <c:if test="${not notif.isRead}">
                                    <span class="inbox-unread-pill" id="badge-${notif.id}">Mới</span>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Pagination -->
        <c:if test="${totalPages > 1}">
            <div class="inbox-pagination">
                <c:forEach begin="1" end="${totalPages}" var="i">
                    <a href="${pageContext.request.contextPath}/notification/my?page=${i}"
                       class="btn page-btn ${currentPageNum == i ? 'btn-primary' : 'btn-secondary'}">
                        ${i}
                    </a>
                </c:forEach>
            </div>
        </c:if>
    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/notification.js" defer></script>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
