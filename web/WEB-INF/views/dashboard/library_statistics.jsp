<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="dashboard-library" scope="request" />
<c:set var="pageTitle" value="Thống kê Thư viện – FPT Library" scope="request" />
<c:set var="pageStylesheet" value="/assets/css/dashboard.css" scope="request" />
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    Integer totalBooks = (Integer) request.getAttribute("totalBooks");
    Integer totalCopies = (Integer) request.getAttribute("totalCopies");
    Map<String, Integer> statusCount = (Map<String, Integer>) request.getAttribute("statusCount");
    Map<String, Integer> conditionCount = (Map<String, Integer>) request.getAttribute("conditionCount");
    List<Map<String, Object>> topBooks = (List<Map<String, Object>>) request.getAttribute("topBooks");
    List<Map<String, Object>> topOverdue = (List<Map<String, Object>>) request.getAttribute("topOverdue");

    int booksVal = totalBooks != null ? totalBooks : 0;
    int copiesVal = totalCopies != null ? totalCopies : 0;

    int available = statusCount != null && statusCount.containsKey("AVAILABLE") ? statusCount.get("AVAILABLE") : 0;
    int borrowed = statusCount != null && statusCount.containsKey("BORROWED") ? statusCount.get("BORROWED") : 0;
    int reserved = statusCount != null && statusCount.containsKey("RESERVED") ? statusCount.get("RESERVED") : 0;

    int good = conditionCount != null && conditionCount.containsKey("GOOD") ? conditionCount.get("GOOD") : 0;
    int damaged = conditionCount != null ? (conditionCount.getOrDefault("DAMAGED", 0) + conditionCount.getOrDefault("WORN", 0)) : 0;
    int lost = conditionCount != null && conditionCount.containsKey("LOST") ? conditionCount.get("LOST") : 0;
%>

<main class="page-wrapper dashboard-library-page" style="margin: 0; padding: 0;">
    <section class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-chart-pie"></i> Báo cáo &amp; Thống kê
                    </div>
                    <h1 class="books-page-title">Thống kê hoạt động Thư viện</h1>
                    <p class="books-page-subtitle">
                        Chỉ số vận hành kho sách, tình trạng lưu thông, chất lượng sách và kiểm kê tài sản
                    </p>
                </div>
                <div class="books-page-stats" aria-label="Tổng số đầu sách">
                    <div class="bps-item">
                        <span class="bps-num"><%= booksVal %></span>
                        <span class="bps-lbl">Đầu sách</span>
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

        <!-- Metric Cards -->
        <div class="db-metrics-grid">
            <!-- Card 1 -->
            <div class="db-card db-card-orange">
                <div class="db-card-icon db-icon-orange">
                    <i class="fa-solid fa-book"></i>
                </div>
                <div>
                    <div class="db-card-label">Tổng đầu sách</div>
                    <div class="db-card-value"><%= booksVal %></div>
                </div>
            </div>
            <!-- Card 2 -->
            <div class="db-card db-card-blue">
                <div class="db-card-icon db-icon-blue">
                    <i class="fa-solid fa-copy"></i>
                </div>
                <div>
                    <div class="db-card-label">Tổng bản sao</div>
                    <div class="db-card-value"><%= copiesVal %></div>
                </div>
            </div>
            <!-- Card 3 -->
            <div class="db-card db-card-green">
                <div class="db-card-icon db-icon-green">
                    <i class="fa-solid fa-circle-check"></i>
                </div>
                <div>
                    <div class="db-card-label">Đang có sẵn</div>
                    <div class="db-card-value"><%= available %></div>
                </div>
            </div>
            <!-- Card 4 -->
            <div class="db-card db-card-amber">
                <div class="db-card-icon db-icon-amber">
                    <i class="fa-solid fa-hand-holding"></i>
                </div>
                <div>
                    <div class="db-card-label">Đang cho mượn</div>
                    <div class="db-card-value"><%= borrowed %></div>
                </div>
            </div>
        </div>

        <div class="db-columns-grid">
            <!-- Condition Stats -->
            <div class="db-panel">
                <h3 class="db-panel-title">
                    <i class="fa-solid fa-shield-halved"></i> Tình trạng chất lượng sách
                </h3>
                <div class="db-progress-list">
                    <div>
                        <div class="db-progress-header">
                            <span>Chất lượng tốt (GOOD)</span>
                            <span style="font-weight: 600;"><%= good %> bản</span>
                        </div>
                        <div class="db-progress-bar-wrap">
                            <div class="db-progress-bar db-progress-green" style="width: <%= copiesVal > 0 ? (good * 100 / copiesVal) : 0 %>%;"></div>
                        </div>
                    </div>
                    <div>
                        <div class="db-progress-header">
                            <span>Hư hỏng nhẹ (DAMAGED)</span>
                            <span style="font-weight: 600;"><%= damaged %> bản</span>
                        </div>
                        <div class="db-progress-bar-wrap">
                            <div class="db-progress-bar db-progress-yellow" style="width: <%= copiesVal > 0 ? (damaged * 100 / copiesVal) : 0 %>%;"></div>
                        </div>
                    </div>
                    <div>
                        <div class="db-progress-header">
                            <span>Mất sách (LOST)</span>
                            <span style="font-weight: 600;"><%= lost %> bản</span>
                        </div>
                        <div class="db-progress-bar-wrap">
                            <div class="db-progress-bar db-progress-red" style="width: <%= copiesVal > 0 ? (lost * 100 / copiesVal) : 0 %>%;"></div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Status Stats -->
            <div class="db-panel">
                <h3 class="db-panel-title">
                    <i class="fa-solid fa-circle-dot"></i> Tình trạng lưu thông
                </h3>
                <div class="db-progress-list">
                    <div>
                        <div class="db-progress-header">
                            <span>Khả dụng trong kho (AVAILABLE)</span>
                            <span style="font-weight: 600;"><%= available %> bản</span>
                        </div>
                        <div class="db-progress-bar-wrap">
                            <div class="db-progress-bar db-progress-green" style="width: <%= copiesVal > 0 ? (available * 100 / copiesVal) : 0 %>%;"></div>
                        </div>
                    </div>
                    <div>
                        <div class="db-progress-header">
                            <span>Độc giả đang mượn (BORROWED)</span>
                            <span style="font-weight: 600;"><%= borrowed %> bản</span>
                        </div>
                        <div class="db-progress-bar-wrap">
                            <div class="db-progress-bar db-progress-blue" style="width: <%= copiesVal > 0 ? (borrowed * 100 / copiesVal) : 0 %>%;"></div>
                        </div>
                    </div>
                    <div>
                        <div class="db-progress-header">
                            <span>Được đặt giữ trước (RESERVED)</span>
                            <span style="font-weight: 600;"><%= reserved %> bản</span>
                        </div>
                        <div class="db-progress-bar-wrap">
                            <div class="db-progress-bar db-progress-purple" style="width: <%= copiesVal > 0 ? (reserved * 100 / copiesVal) : 0 %>%;"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="db-columns-unequal-grid">
            <!-- Top Books -->
            <div class="db-panel">
                <h3 class="db-panel-title">
                    <i class="fa-solid fa-fire" style="color: #e74c3c;"></i> Top 5 sách được mượn nhiều nhất
                </h3>
                <table class="db-table">
                    <thead>
                        <tr class="db-table-header">
                            <th>Sách</th>
                            <th>ISBN</th>
                            <th style="text-align: center;">Số lượt mượn</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (topBooks != null && !topBooks.isEmpty()) { %>
                            <% for (Map<String, Object> item : topBooks) { %>
                                <tr class="db-table-row">
                                    <td class="db-table-cell-title"><%= item.get("title") %></td>
                                    <td><%= item.get("isbn") %></td>
                                    <td class="db-table-cell-bold db-table-cell-orange" style="text-align: center;"><%= item.get("borrow_count") %></td>
                                </tr>
                            <% } %>
                        <% } else { %>
                            <tr><td colspan="3" style="text-align: center; padding: 20px; color: var(--text-muted);">Không có dữ liệu lượt mượn</td></tr>
                        <% } %>
                    </tbody>
                </table>
            </div>

            <!-- Top Overdue Users -->
            <div class="db-panel">
                <h3 class="db-panel-title">
                    <i class="fa-solid fa-triangle-exclamation" style="color: #e74c3c;"></i> Top độc giả nợ sách quá hạn
                </h3>
                <table class="db-table">
                    <thead>
                        <tr class="db-table-header">
                            <th>Họ tên</th>
                            <th style="text-align: center;">Số phiếu trễ</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (topOverdue != null && !topOverdue.isEmpty()) { %>
                            <% for (Map<String, Object> item : topOverdue) { %>
                                <tr class="db-table-row">
                                    <td>
                                        <div class="db-table-cell-title"><%= item.get("full_name") %></div>
                                        <div class="db-table-cell-sub">@<%= item.get("username") %></div>
                                    </td>
                                    <td class="db-table-cell-bold db-table-cell-red" style="text-align: center;"><%= item.get("overdue_count") %></td>
                                </tr>
                            <% } %>
                        <% } else { %>
                            <tr><td colspan="2" style="text-align: center; padding: 20px; color: var(--text-muted);">Không có độc giả quá hạn</td></tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
