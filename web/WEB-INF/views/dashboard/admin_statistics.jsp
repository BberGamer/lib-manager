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
    <div class="container" style="padding-top: 30px; padding-bottom: 50px;">
        <div class="section-header" style="margin-bottom: 30px;">
            <div>
                <h1 class="section-title"><i class="fa-solid fa-chart-line"></i> Thống kê hệ thống Admin</h1>
                <p class="section-subtitle">Chỉ số tài chính phạt, lượng tài khoản người dùng và nhật ký audit log ngoại lệ</p>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-error" style="background: #fde8e7; border-left: 5px solid #e74c3c; color: #e74c3c; padding: 15px; border-radius: 8px; margin-bottom: 24px;">
                <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${error}" />
            </div>
        </c:if>

        <!-- Metric Cards Grid -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 24px; margin-bottom: 40px;">
            <!-- Card 1 -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); border-left: 5px solid #9b59b6; display: flex; align-items: center; gap: 20px;">
                <div style="background: #f4ecf7; color: #9b59b6; width: 50px; height: 50px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem;">
                    <i class="fa-solid fa-users"></i>
                </div>
                <div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); font-weight: 500;">Tổng số tài khoản</div>
                    <div style="font-size: 1.8rem; font-weight: 700; color: var(--text-primary);"><%= totalUsers %></div>
                </div>
            </div>
            <!-- Card 2 -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); border-left: 5px solid #2ecc71; display: flex; align-items: center; gap: 20px;">
                <div style="background: #eafaf1; color: #2ecc71; width: 50px; height: 50px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem;">
                    <i class="fa-solid fa-money-bill-wave"></i>
                </div>
                <div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); font-weight: 500;">Phạt đã thu</div>
                    <div style="font-size: 1.4rem; font-weight: 700; color: #2ecc71;"><%= vnFormat.format(paid) %></div>
                </div>
            </div>
            <!-- Card 3 -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); border-left: 5px solid #e74c3c; display: flex; align-items: center; gap: 20px;">
                <div style="background: #fde8e7; color: #e74c3c; width: 50px; height: 50px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem;">
                    <i class="fa-solid fa-receipt"></i>
                </div>
                <div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); font-weight: 500;">Phạt chưa đóng</div>
                    <div style="font-size: 1.4rem; font-weight: 700; color: #e74c3c;"><%= vnFormat.format(unpaid) %></div>
                </div>
            </div>
            <!-- Card 4 -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); border-left: 5px solid #95a5a6; display: flex; align-items: center; gap: 20px;">
                <div style="background: #f2f4f4; color: #95a5a6; width: 50px; height: 50px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem;">
                    <i class="fa-solid fa-hand-holding-heart"></i>
                </div>
                <div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); font-weight: 500;">Đã miễn giảm (Waived)</div>
                    <div style="font-size: 1.4rem; font-weight: 700; color: #7f8c8d;"><%= vnFormat.format(waived) %></div>
                </div>
            </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 2fr; gap: 30px; margin-bottom: 40px;">
            <!-- User Distribution -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02);">
                <h3 style="font-size: 1.1rem; margin-bottom: 20px; border-bottom: 1px solid #f1f3f5; padding-bottom: 10px; color: var(--text-primary);">
                    <i class="fa-solid fa-users-gear"></i> Phân bổ tài khoản hệ thống
                </h3>
                <div style="display: flex; flex-direction: column; gap: 20px;">
                    <div>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 0.9rem;">
                            <span>Độc giả / Bạn đọc</span>
                            <span style="font-weight: 600;"><%= readers %> tài khoản</span>
                        </div>
                        <div style="background: #f1f3f5; height: 8px; border-radius: 4px; overflow: hidden;">
                            <div style="background: #3498db; width: <%= totalUsers > 0 ? (readers * 100 / totalUsers) : 0 %>%; height: 100%;"></div>
                        </div>
                    </div>
                    <div>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 0.9rem;">
                            <span>Thủ thư (LIBRARIAN)</span>
                            <span style="font-weight: 600;"><%= librarians %> tài khoản</span>
                        </div>
                        <div style="background: #f1f3f5; height: 8px; border-radius: 4px; overflow: hidden;">
                            <div style="background: #f47920; width: <%= totalUsers > 0 ? (librarians * 100 / totalUsers) : 0 %>%; height: 100%;"></div>
                        </div>
                    </div>
                    <div>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 0.9rem;">
                            <span>Quản trị viên (ADMIN)</span>
                            <span style="font-weight: 600;"><%= admins %> tài khoản</span>
                        </div>
                        <div style="background: #f1f3f5; height: 8px; border-radius: 4px; overflow: hidden;">
                            <div style="background: #9b59b6; width: <%= totalUsers > 0 ? (admins * 100 / totalUsers) : 0 %>%; height: 100%;"></div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Audit Logs -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02);">
                <h3 style="font-size: 1.1rem; margin-bottom: 20px; border-bottom: 1px solid #f1f3f5; padding-bottom: 10px; color: var(--text-primary);">
                    <i class="fa-solid fa-list-check"></i> Nhật ký Audit Log (Hành vi override ngoại lệ)
                </h3>
                <table class="table" style="width: 100%; border-collapse: collapse; font-size: 0.85rem;">
                    <thead>
                        <tr style="border-bottom: 2px solid #f1f3f5; text-align: left;">
                            <th style="padding: 10px; color: var(--text-muted);">Thời gian</th>
                            <th style="padding: 10px; color: var(--text-muted);">Hành động</th>
                            <th style="padding: 10px; color: var(--text-muted);">Người duyệt</th>
                            <th style="padding: 10px; color: var(--text-muted);">Chi tiết log</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (recentLogs != null && !recentLogs.isEmpty()) { %>
                            <% for (Map<String, Object> log : recentLogs) { %>
                                <tr style="border-bottom: 1px solid #f1f3f5;">
                                    <td style="padding: 12px 10px; color: var(--text-muted); font-size: 0.8rem;"><%= log.get("created_at") %></td>
                                    <td style="padding: 12px 10px; font-weight: 600;"><span class="badge" style="background: #fff4ec; color: #f47920; padding: 3px 6px; border-radius: 4px; font-size: 0.75rem;"><%= log.get("action") %></span></td>
                                    <td style="padding: 12px 10px; font-weight: 500;">@<%= log.get("performed_by") %></td>
                                    <td style="padding: 12px 10px; color: var(--text-secondary);"><%= log.get("detail") %></td>
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
