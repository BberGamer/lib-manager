<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map, java.math.BigDecimal, java.text.NumberFormat, java.util.Locale" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="dashboard-admin" scope="request" />
<c:set var="pageTitle" value="Thống kê Admin – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/dashboard.css" scope="request" />
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

<main class="page-wrapper dashboard-admin-page" style="margin: 0; padding: 0;">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-chart-line"></i> Báo cáo &amp; Thống kê
                    </div>
                    <h1 class="books-page-title">Thống kê hệ thống Admin</h1>
                    <p class="books-page-subtitle">
                        Chỉ số tài chính phạt, lượng tài khoản người dùng và phân bổ nhật ký kiểm toán
                    </p>
                </div>
                <div class="books-page-stats" aria-label="Tổng số tài khoản">
                    <div class="bps-item">
                        <span class="bps-num"><%= totalUsers %></span>
                        <span class="bps-lbl">Tài khoản</span>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="container db-container" style="padding-top: 28px; padding-bottom: 48px;">

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

<%
    List<Map<String, Object>> actionCounts = (List<Map<String, Object>>) request.getAttribute("auditActionCounts");
    int totalAuditLogs = 0;
    if (actionCounts != null) {
        for (Map<String, Object> ac : actionCounts) {
            Number cnt = (Number) ac.get("count");
            if (cnt != null) totalAuditLogs += cnt.intValue();
        }
    }
%>

            <!-- Audit Logs Donut Chart -->
            <div class="db-panel" style="display: flex; flex-direction: column;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                    <h3 class="db-panel-title" style="margin-bottom: 0;">
                        <i class="fa-solid fa-chart-pie"></i> Phân bổ hoạt động Audit Log
                    </h3>
                    <a href="${pageContext.request.contextPath}/admin/audit-logs" class="btn btn-sm btn-outline" style="font-size: 0.8rem; padding: 5px 12px; border-radius: 6px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                        Xem chi tiết <i class="fa-solid fa-arrow-right fa-xs"></i>
                    </a>
                </div>

                <% if (actionCounts != null && !actionCounts.isEmpty() && totalAuditLogs > 0) { %>
                    <div style="display: flex; align-items: center; gap: 24px; flex-wrap: wrap; justify-content: space-around; padding: 12px 0; flex: 1;">
                        <!-- Donut Canvas Container -->
                        <div style="position: relative; width: 190px; height: 190px; flex-shrink: 0;">
                            <canvas id="auditDonutChart"></canvas>
                            <div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); text-align: center; pointer-events: none;">
                                <div style="font-size: 1.4rem; font-weight: 700; color: #1e293b; line-height: 1;"><%= totalAuditLogs %></div>
                                <div style="font-size: 0.72rem; color: #64748b; margin-top: 4px; font-weight: 500;">nhật ký</div>
                            </div>
                        </div>

                        <!-- Legend & Percentage Breakdown -->
                        <div style="flex: 1; min-width: 230px; max-height: 240px; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; padding-right: 6px;">
                            <%
                                for (Map<String, Object> ac : actionCounts) {
                                    String act = (String) ac.get("action");
                                    int count = ((Number) ac.get("count")).intValue();
                                    int pct = totalAuditLogs > 0 ? Math.round((float) count * 100 / totalAuditLogs) : 0;

                                    String label = act;
                                    String color = "#94a3b8";

                                    if ("LOCK_ACCOUNT".equals(act)) {
                                        label = "Khóa tài khoản";
                                        color = "#ef4444";
                                    } else if ("UNLOCK_ACCOUNT".equals(act)) {
                                        label = "Mở khóa tài khoản";
                                        color = "#22c55e";
                                    } else if ("DELETE_USER".equals(act)) {
                                        label = "Xóa tài khoản";
                                        color = "#64748b";
                                    } else if ("WAIVE_FINE".equals(act)) {
                                        label = "Miễn giảm phạt";
                                        color = "#0284c7";
                                    } else if ("OVERRIDE_BORROW_LIMIT".equals(act)) {
                                        label = "Vượt hạn mức";
                                        color = "#f59e0b";
                                    } else if ("APPLY_DAMAGE_FINE".equals(act)) {
                                        label = "Phạt hỏng sách";
                                        color = "#ea580c";
                                    } else if ("APPLY_LOST_FINE".equals(act)) {
                                        label = "Phạt mất sách";
                                        color = "#991b1b";
                                    } else if ("CONFIRM_RESERVATION".equals(act)) {
                                        label = "Duyệt đặt trước";
                                        color = "#a855f7";
                                    } else if ("AUTO_BATCH_REMINDER".equals(act)) {
                                        label = "Quét tự động hàng loạt";
                                        color = "#06b6d4";
                                    } else if ("UPDATE_AUTOMATION_SETTING".equals(act)) {
                                        label = "Cập nhật cấu hình tự động";
                                        color = "#eab308";
                                    }
                            %>
                                <div style="display: flex; align-items: center; justify-content: space-between; font-size: 0.83rem;">
                                    <div style="display: flex; align-items: center; gap: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                        <span style="width: 10px; height: 10px; border-radius: 50%; background: <%= color %>; flex-shrink: 0;"></span>
                                        <span style="color: #334155; font-weight: 500;"><%= label %></span>
                                    </div>
                                    <div style="display: flex; align-items: center; gap: 8px; flex-shrink: 0;">
                                        <span style="font-weight: 600; color: #0f172a;"><%= count %></span>
                                        <span style="color: #94a3b8; font-size: 0.75rem; width: 34px; text-align: right;"><%= pct %>%</span>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    </div>
                <% } else { %>
                    <div style="text-align: center; padding: 48px 16px; color: var(--text-muted);">
                        <i class="fa-solid fa-chart-pie" style="font-size: 2.2rem; margin-bottom: 10px; display: block; opacity: 0.35;"></i>
                        <span style="font-size: 0.95rem;">Chưa có dữ liệu nhật ký kiểm toán trong hệ thống</span>
                    </div>
                <% } %>
            </div>
        </div>
    </div>
</main>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
document.addEventListener("DOMContentLoaded", function () {
    const canvas = document.getElementById("auditDonutChart");
    if (!canvas) return;

    <%
        StringBuilder labelsJson = new StringBuilder("[");
        StringBuilder dataJson = new StringBuilder("[");
        StringBuilder colorsJson = new StringBuilder("[");
        if (actionCounts != null) {
            boolean first = true;
            for (Map<String, Object> ac : actionCounts) {
                if (!first) {
                    labelsJson.append(",");
                    dataJson.append(",");
                    colorsJson.append(",");
                }
                String act = (String) ac.get("action");
                int count = ((Number) ac.get("count")).intValue();
                String label = act;
                String color = "#94a3b8";

                if ("LOCK_ACCOUNT".equals(act)) {
                    label = "Khóa tài khoản";
                    color = "#ef4444";
                } else if ("UNLOCK_ACCOUNT".equals(act)) {
                    label = "Mở khóa tài khoản";
                    color = "#22c55e";
                } else if ("DELETE_USER".equals(act)) {
                    label = "Xóa tài khoản";
                    color = "#64748b";
                } else if ("WAIVE_FINE".equals(act)) {
                    label = "Miễn giảm phạt";
                    color = "#0284c7";
                } else if ("OVERRIDE_BORROW_LIMIT".equals(act)) {
                    label = "Vượt hạn mức";
                    color = "#f59e0b";
                } else if ("APPLY_DAMAGE_FINE".equals(act)) {
                    label = "Phạt hỏng sách";
                    color = "#ea580c";
                } else if ("APPLY_LOST_FINE".equals(act)) {
                    label = "Phạt mất sách";
                    color = "#991b1b";
                } else if ("CONFIRM_RESERVATION".equals(act)) {
                    label = "Duyệt đặt trước";
                    color = "#a855f7";
                } else if ("AUTO_BATCH_REMINDER".equals(act)) {
                    label = "Quét tự động hàng loạt";
                    color = "#06b6d4";
                } else if ("UPDATE_AUTOMATION_SETTING".equals(act)) {
                    label = "Cập nhật cấu hình tự động";
                    color = "#eab308";
                }

                labelsJson.append("\"").append(label).append("\"");
                dataJson.append(count);
                colorsJson.append("\"").append(color).append("\"");
                first = false;
            }
        }
        labelsJson.append("]");
        dataJson.append("]");
        colorsJson.append("]");
    %>

    const labels = <%= labelsJson.toString() %>;
    const dataValues = <%= dataJson.toString() %>;
    const backgroundColors = <%= colorsJson.toString() %>;

    new Chart(canvas, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: dataValues,
                backgroundColor: backgroundColors,
                borderWidth: 2,
                borderColor: '#ffffff',
                hoverOffset: 5
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '70%',
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            const label = context.label || '';
                            const val = context.raw || 0;
                            const total = context.chart._metasets[0].total || 1;
                            const pct = Math.round((val / total) * 100);
                            return ' ' + label + ': ' + val + ' (' + pct + '%)';
                        }
                    }
                }
            }
        }
    });
});
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
