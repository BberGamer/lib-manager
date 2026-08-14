<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map, java.math.BigDecimal, java.text.NumberFormat, java.util.Locale" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="dashboard-admin" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    Map<String, Integer> roleCount = (Map<String, Integer>) request.getAttribute("roleCount");
    Map<String, BigDecimal> finesStats = (Map<String, BigDecimal>) request.getAttribute("finesStats");
    List<Map<String, Object>> recentLogs = (List<Map<String, Object>>) request.getAttribute("recentLogs");

    int admins = roleCount != null && roleCount.containsKey("ADMIN") ? roleCount.get("ADMIN") : 0;
    int librarians = roleCount != null && roleCount.containsKey("LIBRARIAN") ? roleCount.get("LIBRARIAN") : 0;
    int readers = roleCount != null && roleCount.containsKey("READER") ? roleCount.get("READER") : 0;
    int totalUsers = admins + librarians + readers;

    BigDecimal paid = finesStats != null && finesStats.containsKey("PAID") ? finesStats.get("PAID") : BigDecimal.ZERO;
    BigDecimal unpaid = finesStats != null && finesStats.containsKey("UNPAID") ? finesStats.get("UNPAID") : BigDecimal.ZERO;
    BigDecimal waived = finesStats != null && finesStats.containsKey("WAIVED") ? finesStats.get("WAIVED") : BigDecimal.ZERO;

    NumberFormat vnFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
%>

<main class="page-wrapper">
    <div class="container db-container">
        <div class="section-header db-section-header">
            <div>
                <h1 class="section-title"><i class="fa-solid fa-chart-line"></i> Thống kê hệ thống Admin</h1>
                <p class="section-subtitle">Chỉ số tài chính phạt, lượng tài khoản người dùng và nhật ký audit log ngoại lệ</p>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-error db-error-alert">
                <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${error}" />
            </div>
        </c:if>

        <!-- Metric Cards Grid -->
        <div class="db-metrics-grid" style="grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));">
            <!-- Card 1 -->
            <div class="db-card db-card-purple">
                <div class="db-card-icon db-icon-purple">
                    <i class="fa-solid fa-users"></i>
                </div>
                <div>
                    <div class="db-card-label">Tổng số tài khoản</div>
                    <div class="db-card-value"><%= totalUsers %></div>
                </div>
            </div>
            <!-- Card 2 -->
            <div class="db-card db-card-green">
                <div class="db-card-icon db-icon-green">
                    <i class="fa-solid fa-money-bill-wave"></i>
                </div>
                <div>
                    <div class="db-card-label">Phạt đã thu</div>
                    <div class="db-card-value-green"><%= vnFormat.format(paid) %></div>
                </div>
            </div>
            <!-- Card 3 -->
            <div class="db-card db-card-red">
                <div class="db-card-icon db-icon-red">
                    <i class="fa-solid fa-receipt"></i>
                </div>
                <div>
                    <div class="db-card-label">Phạt chưa đóng</div>
                    <div class="db-card-value-red"><%= vnFormat.format(unpaid) %></div>
                </div>
            </div>
            <!-- Card 4 -->
            <div class="db-card db-card-gray">
                <div class="db-card-icon db-icon-gray">
                    <i class="fa-solid fa-hand-holding-heart"></i>
                </div>
                <div>
                    <div class="db-card-label">Đã miễn giảm (Waived)</div>
                    <div class="db-card-value-gray"><%= vnFormat.format(waived) %></div>
                </div>
            </div>
        </div>

        <div class="db-columns-admin-grid">
            <!-- User Distribution -->
            <div class="db-panel">
                <h3 class="db-panel-title">
                    <i class="fa-solid fa-users-gear"></i> Phân bổ tài khoản hệ thống
                </h3>
                <div class="db-progress-list-wide">
                    <div>
                        <div class="db-progress-header">
                            <span>Độc giả / Bạn đọc</span>
                            <span style="font-weight: 600;"><%= readers %> tài khoản</span>
                        </div>
                        <div class="db-progress-bar-wrap">
                            <div class="db-progress-bar db-progress-blue" style="width: <%= totalUsers > 0 ? (readers * 100 / totalUsers) : 0 %>%;"></div>
                        </div>
                    </div>
                    <div>
                        <div class="db-progress-header">
                            <span>Thủ thư (LIBRARIAN)</span>
                            <span style="font-weight: 600;"><%= librarians %> tài khoản</span>
                        </div>
                        <div class="db-progress-bar-wrap">
                            <div class="db-progress-bar db-progress-orange" style="width: <%= totalUsers > 0 ? (librarians * 100 / totalUsers) : 0 %>%;"></div>
                        </div>
                    </div>
                    <div>
                        <div class="db-progress-header">
                            <span>Quản trị viên (ADMIN)</span>
                            <span style="font-weight: 600;"><%= admins %> tài khoản</span>
                        </div>
                        <div class="db-progress-bar-wrap">
                            <div class="db-progress-bar db-progress-purple" style="width: <%= totalUsers > 0 ? (admins * 100 / totalUsers) : 0 %>%;"></div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Audit Logs -->
            <div class="db-panel">
                <h3 class="db-panel-title">
                    <i class="fa-solid fa-list-check"></i> Nhật ký Audit Log (Hành vi override ngoại lệ)
                </h3>
                <table class="db-table-sm">
                    <thead>
                        <tr class="db-table-header">
                            <th>Thời gian</th>
                            <th>Hành động</th>
                            <th>Người duyệt</th>
                            <th>Chi tiết log</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (recentLogs != null && !recentLogs.isEmpty()) { %>
                            <% for (Map<String, Object> log : recentLogs) { %>
                                <tr class="db-table-row">
                                    <td class="db-table-cell-muted"><%= log.get("created_at") %></td>
                                    <td><span class="badge db-badge-action"><%= log.get("action") %></span></td>
                                    <td class="db-table-cell-title">@<%= log.get("performed_by") %></td>
                                    <td><%= log.get("detail") %></td>
                                </tr>
                            <% } %>
                        <% } else { %>
                            <tr><td colspan="4" style="text-align: center; padding: 20px; color: var(--text-muted);">Chưa ghi nhận bất kỳ audit log nào trong hệ thống</td></tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
