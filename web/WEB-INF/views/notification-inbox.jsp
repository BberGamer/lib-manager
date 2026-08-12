<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Notification, java.time.format.DateTimeFormatter" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="activePage" value="notifications" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    List<Notification> notificationList = (List<Notification>) request.getAttribute("notificationList");
    String successMsg = (String) session.getAttribute("successMsg");
    String errorMsg = (String) session.getAttribute("errorMsg");
    if (successMsg != null) session.removeAttribute("successMsg");
    if (errorMsg != null) session.removeAttribute("errorMsg");
%>

<main class="page-wrapper">
    <div class="container" style="padding-top: 30px; padding-bottom: 50px; max-width: 800px;">
        <div class="section-header" style="margin-bottom: 30px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px;">
            <div>
                <h1 class="section-title"><i class="fa-solid fa-bell"></i> Hộp thư thông báo</h1>
                <p class="section-subtitle">Theo dõi các cập nhật về hạn trả sách, phí phạt và thông báo hệ thống</p>
            </div>
            <% if (notificationList != null && !notificationList.isEmpty()) { %>
                <form action="${pageContext.request.contextPath}/notification/read-all" method="post" style="margin: 0;">
                    <button type="submit" class="btn btn-outline" style="border: 1px solid #ddd; border-radius: 8px; font-weight: 600; padding: 8px 16px;">
                        <i class="fa-solid fa-check-double"></i> Đọc tất cả
                    </button>
                </form>
            <% } %>
        </div>

        <!-- Alerts -->
        <% if (successMsg != null) { %>
            <div class="alert alert-success" style="background: #e8f8f5; border-left: 5px solid #2ecc71; color: #27ae60; padding: 15px; border-radius: 8px; margin-bottom: 24px;">
                <i class="fa-solid fa-circle-check"></i> <%= successMsg %>
            </div>
        <% } %>
        <% if (errorMsg != null) { %>
            <div class="alert alert-error" style="background: #fde8e7; border-left: 5px solid #e74c3c; color: #c0392b; padding: 15px; border-radius: 8px; margin-bottom: 24px;">
                <i class="fa-solid fa-circle-exclamation"></i> <%= errorMsg %>
            </div>
        <% } %>

        <!-- Notification List -->
        <div style="display: flex; flex-direction: column; gap: 16px;">
            <% if (notificationList == null || notificationList.isEmpty()) { %>
                <div style="background: white; border-radius: 12px; padding: 50px 20px; text-align: center; box-shadow: 0 4px 6px rgba(0,0,0,0.02); color: var(--text-muted);">
                    <i class="fa-regular fa-bell-slash" style="font-size: 3rem; margin-bottom: 15px; display: block; color: #ccc;"></i>
                    <h4 style="margin: 0 0 8px 0; color: var(--text-secondary);">Hộp thư trống</h4>
                    <p style="margin: 0; font-size: 0.9rem;">Bạn không có bất kỳ thông báo nào tại thời điểm này.</p>
                </div>
            <% } else {
                for (Notification n : notificationList) {
                    String iconClass = "fa-bell";
                    String iconBg = "#f1f2f6";
                    String iconColor = "#57606f";
                    
                    if ("DUE_REMINDER".equals(n.getType())) {
                        iconClass = "fa-clock";
                        iconBg = "#fff9e6";
                        iconColor = "#f39c12";
                    } else if ("OVERDUE".equals(n.getType())) {
                        iconClass = "fa-circle-exclamation";
                        iconBg = "#fde8e7";
                        iconColor = "#e74c3c";
                    } else if ("FINE".equals(n.getType())) {
                        iconClass = "fa-circle-dollar-to-slot";
                        iconBg = "#ebf5fb";
                        iconColor = "#2980b9";
                    } else if ("RESERVATION".equals(n.getType())) {
                        iconClass = "fa-bookmark";
                        iconBg = "#e8f8f5";
                        iconColor = "#27ae60";
                    }
                    
                    String itemBg = n.isIsRead() ? "white" : "#f9fbfd";
                    String borderLeft = n.isIsRead() ? "1px solid #eee" : "4px solid var(--text-brand)";
            %>
                <div class="notification-item" id="notif-item-<%= n.getId() %>" onclick="markAsRead(<%= n.getId() %>, <%= n.isIsRead() %>)"
                     style="background: <%= itemBg %>; border: 1px solid #eee; border-left: <%= borderLeft %>; border-radius: 10px; padding: 20px; display: flex; gap: 15px; cursor: pointer; transition: all 0.2s; box-shadow: 0 2px 4px rgba(0,0,0,0.01);"
                     onmouseover="this.style.transform='translateY(-2px)'; this.style.boxShadow='0 4px 8px rgba(0,0,0,0.04)'"
                     onmouseout="this.style.transform='none'; this.style.boxShadow='0 2px 4px rgba(0,0,0,0.01)'">
                    
                    <!-- Icon Area -->
                    <div style="background: <%= iconBg %>; color: <%= iconColor %>; width: 44px; height: 44px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1.15rem;">
                        <i class="fa-solid <%= iconClass %>"></i>
                    </div>
                    
                    <!-- Text Area -->
                    <div style="flex: 1;">
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 10px; margin-bottom: 6px;">
                            <h4 style="margin: 0; font-size: 0.98rem; font-weight: <%= n.isIsRead() ? "600" : "700" %>; color: var(--text-primary);"><%= n.getTitle() %></h4>
                            <span style="font-size: 0.78rem; color: var(--text-muted); white-space: nowrap;">
                                <%= n.getCreatedAt() != null ? n.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "" %>
                            </span>
                        </div>
                        <p style="margin: 0; font-size: 0.9rem; color: var(--text-secondary); line-height: 1.5;"><%= n.getMessage().replace("\n", "<br/>") %></p>
                        
                        <!-- Unread badge indicator -->
                        <% if (!n.isIsRead()) { %>
                            <span class="unread-badge" id="badge-<%= n.getId() %>" style="display: inline-block; font-size: 0.72rem; font-weight: 700; color: var(--text-brand); background: #eef3ff; padding: 2px 8px; border-radius: 12px; margin-top: 8px;">Mới</span>
                        <% } %>
                    </div>
                </div>
            <% }
            } %>
        </div>

        <!-- Pagination -->
        <c:if test="${totalPages > 1}">
            <div style="display: flex; justify-content: center; gap: 10px; margin-top: 30px;">
                <c:forEach begin="1" end="${totalPages}" var="i">
                    <a href="${pageContext.request.contextPath}/notification/my?page=${i}" 
                       class="btn ${currentPageNum == i ? 'btn-primary' : 'btn-secondary'}" 
                       style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border-radius: 50%; font-weight: 600; text-decoration: none; padding: 0;">
                        ${i}
                    </a>
                </c:forEach>
            </div>
        </c:if>
    </div>
</main>

<script>
    function markAsRead(id, isRead) {
        if (isRead) return; // Already read
        
        // Send AJAX request
        let xhr = new XMLHttpRequest();
        xhr.open("POST", "${pageContext.request.contextPath}/notification/read", true);
        xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4 && xhr.status === 200) {
                // Update UI dynamically
                let item = document.getElementById('notif-item-' + id);
                item.style.background = 'white';
                item.style.borderLeft = '1px solid #eee';
                
                let badge = document.getElementById('badge-' + id);
                if (badge) badge.remove();
                
                // Decrement header unread badge if present
                let headerBadge = document.querySelector('.notification-unread-dot');
                if (headerBadge) {
                    // Quick refresh badge or decrement count text inside header
                    // We can just reload to be safe, or leave it to the next page load.
                }
            }
        };
        xhr.send("id=" + id);
    }
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
