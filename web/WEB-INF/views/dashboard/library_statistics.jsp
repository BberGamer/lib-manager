<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isManagePageAttr" value="true" scope="request" />
<c:set var="activePage" value="dashboard-library" scope="request" />
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
    int damaged = conditionCount != null && conditionCount.containsKey("DAMAGED") ? conditionCount.get("DAMAGED") : 0;
    int lost = conditionCount != null && conditionCount.containsKey("LOST") ? conditionCount.get("LOST") : 0;
%>

<main class="page-wrapper">
    <div class="container" style="padding-top: 30px; padding-bottom: 50px;">
        <div class="section-header" style="margin-bottom: 30px;">
            <div>
                <h1 class="section-title"><i class="fa-solid fa-chart-pie"></i> Thống kê hoạt động Thư viện</h1>
                <p class="section-subtitle">Chỉ số vận hành kho sách, lượt mượn trả và kiểm kê tài sản</p>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-error" style="background: #fde8e7; border-left: 5px solid #e74c3c; color: #e74c3c; padding: 15px; border-radius: 8px; margin-bottom: 24px;">
                <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${error}" />
            </div>
        </c:if>

        <!-- Metric Cards -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 24px; margin-bottom: 40px;">
            <!-- Card 1 -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); border-left: 5px solid #f47920; display: flex; align-items: center; gap: 20px;">
                <div style="background: #fff4ec; color: #f47920; width: 50px; height: 50px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem;">
                    <i class="fa-solid fa-book"></i>
                </div>
                <div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); font-weight: 500;">Tổng đầu sách</div>
                    <div style="font-size: 1.8rem; font-weight: 700; color: var(--text-primary);"><%= booksVal %></div>
                </div>
            </div>
            <!-- Card 2 -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); border-left: 5px solid #3498db; display: flex; align-items: center; gap: 20px;">
                <div style="background: #ebf5fb; color: #3498db; width: 50px; height: 50px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem;">
                    <i class="fa-solid fa-copy"></i>
                </div>
                <div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); font-weight: 500;">Tổng bản sao</div>
                    <div style="font-size: 1.8rem; font-weight: 700; color: var(--text-primary);"><%= copiesVal %></div>
                </div>
            </div>
            <!-- Card 3 -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); border-left: 5px solid #2ecc71; display: flex; align-items: center; gap: 20px;">
                <div style="background: #eafaf1; color: #2ecc71; width: 50px; height: 50px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem;">
                    <i class="fa-solid fa-circle-check"></i>
                </div>
                <div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); font-weight: 500;">Đang có sẵn</div>
                    <div style="font-size: 1.8rem; font-weight: 700; color: var(--text-primary);"><%= available %></div>
                </div>
            </div>
            <!-- Card 4 -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); border-left: 5px solid #e67e22; display: flex; align-items: center; gap: 20px;">
                <div style="background: #fdf2e9; color: #e67e22; width: 50px; height: 50px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem;">
                    <i class="fa-solid fa-hand-holding"></i>
                </div>
                <div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); font-weight: 500;">Đang cho mượn</div>
                    <div style="font-size: 1.8rem; font-weight: 700; color: var(--text-primary);"><%= borrowed %></div>
                </div>
            </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 30px; margin-bottom: 40px;">
            <!-- Condition Stats -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02);">
                <h3 style="font-size: 1.1rem; margin-bottom: 20px; border-bottom: 1px solid #f1f3f5; padding-bottom: 10px; color: var(--text-primary);">
                    <i class="fa-solid fa-shield-halved"></i> Tình trạng chất lượng sách
                </h3>
                <div style="display: flex; flex-direction: column; gap: 15px;">
                    <div>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 0.9rem;">
                            <span>Chất lượng tốt (GOOD)</span>
                            <span style="font-weight: 600;"><%= good %> bản</span>
                        </div>
                        <div style="background: #f1f3f5; height: 8px; border-radius: 4px; overflow: hidden;">
                            <div style="background: #2ecc71; width: <%= totalCopies > 0 ? (good * 100 / totalCopies) : 0 %>%; height: 100%;"></div>
                        </div>
                    </div>
                    <div>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 0.9rem;">
                            <span>Hư hỏng nhẹ (DAMAGED)</span>
                            <span style="font-weight: 600;"><%= damaged %> bản</span>
                        </div>
                        <div style="background: #f1f3f5; height: 8px; border-radius: 4px; overflow: hidden;">
                            <div style="background: #f1c40f; width: <%= totalCopies > 0 ? (damaged * 100 / totalCopies) : 0 %>%; height: 100%;"></div>
                        </div>
                    </div>
                    <div>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 0.9rem;">
                            <span>Mất sách (LOST)</span>
                            <span style="font-weight: 600;"><%= lost %> bản</span>
                        </div>
                        <div style="background: #f1f3f5; height: 8px; border-radius: 4px; overflow: hidden;">
                            <div style="background: #e74c3c; width: <%= totalCopies > 0 ? (lost * 100 / totalCopies) : 0 %>%; height: 100%;"></div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Status Stats -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02);">
                <h3 style="font-size: 1.1rem; margin-bottom: 20px; border-bottom: 1px solid #f1f3f5; padding-bottom: 10px; color: var(--text-primary);">
                    <i class="fa-solid fa-circle-dot"></i> Tình trạng lưu thông
                </h3>
                <div style="display: flex; flex-direction: column; gap: 15px;">
                    <div>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 0.9rem;">
                            <span>Khả dụng trong kho (AVAILABLE)</span>
                            <span style="font-weight: 600;"><%= available %> bản</span>
                        </div>
                        <div style="background: #f1f3f5; height: 8px; border-radius: 4px; overflow: hidden;">
                            <div style="background: #2ecc71; width: <%= totalCopies > 0 ? (available * 100 / totalCopies) : 0 %>%; height: 100%;"></div>
                        </div>
                    </div>
                    <div>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 0.9rem;">
                            <span>Độc giả đang mượn (BORROWED)</span>
                            <span style="font-weight: 600;"><%= borrowed %> bản</span>
                        </div>
                        <div style="background: #f1f3f5; height: 8px; border-radius: 4px; overflow: hidden;">
                            <div style="background: #3498db; width: <%= totalCopies > 0 ? (borrowed * 100 / totalCopies) : 0 %>%; height: 100%;"></div>
                        </div>
                    </div>
                    <div>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 0.9rem;">
                            <span>Được đặt giữ trước (RESERVED)</span>
                            <span style="font-weight: 600;"><%= reserved %> bản</span>
                        </div>
                        <div style="background: #f1f3f5; height: 8px; border-radius: 4px; overflow: hidden;">
                            <div style="background: #9b59b6; width: <%= totalCopies > 0 ? (reserved * 100 / totalCopies) : 0 %>%; height: 100%;"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div style="display: grid; grid-template-columns: 1.2fr 0.8fr; gap: 30px;">
            <!-- Top Books -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02);">
                <h3 style="font-size: 1.1rem; margin-bottom: 20px; border-bottom: 1px solid #f1f3f5; padding-bottom: 10px; color: var(--text-primary);">
                    <i class="fa-solid fa-fire" style="color: #e74c3c;"></i> Top 5 sách được mượn nhiều nhất
                </h3>
                <table class="table" style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="border-bottom: 2px solid #f1f3f5; text-align: left;">
                            <th style="padding: 10px; font-size: 0.85rem; color: var(--text-muted);">Sách</th>
                            <th style="padding: 10px; font-size: 0.85rem; color: var(--text-muted);">ISBN</th>
                            <th style="padding: 10px; text-align: center; font-size: 0.85rem; color: var(--text-muted);">Số lượt mượn</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (topBooks != null && !topBooks.isEmpty()) { %>
                            <% for (Map<String, Object> item : topBooks) { %>
                                <tr style="border-bottom: 1px solid #f1f3f5;">
                                    <td style="padding: 12px 10px; font-weight: 500; font-size: 0.9rem; color: var(--text-primary);"><%= item.get("title") %></td>
                                    <td style="padding: 12px 10px; font-size: 0.85rem; color: var(--text-secondary);"><%= item.get("isbn") %></td>
                                    <td style="padding: 12px 10px; text-align: center; font-weight: 700; color: #f47920; font-size: 0.95rem;"><%= item.get("borrow_count") %></td>
                                </tr>
                            <% } %>
                        <% } else { %>
                            <tr><td colspan="3" style="text-align: center; padding: 20px; color: var(--text-muted);">Không có dữ liệu lượt mượn</td></tr>
                        <% } %>
                    </tbody>
                </table>
            </div>

            <!-- Top Overdue Users -->
            <div style="background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.02);">
                <h3 style="font-size: 1.1rem; margin-bottom: 20px; border-bottom: 1px solid #f1f3f5; padding-bottom: 10px; color: var(--text-primary);">
                    <i class="fa-solid fa-triangle-exclamation" style="color: #e74c3c;"></i> Top độc giả nợ sách quá hạn
                </h3>
                <table class="table" style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="border-bottom: 2px solid #f1f3f5; text-align: left;">
                            <th style="padding: 10px; font-size: 0.85rem; color: var(--text-muted);">Họ tên</th>
                            <th style="padding: 10px; text-align: center; font-size: 0.85rem; color: var(--text-muted);">Số phiếu trễ</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (topOverdue != null && !topOverdue.isEmpty()) { %>
                            <% for (Map<String, Object> item : topOverdue) { %>
                                <tr style="border-bottom: 1px solid #f1f3f5;">
                                    <td style="padding: 12px 10px; font-size: 0.9rem; color: var(--text-primary);">
                                        <div style="font-weight: 500;"><%= item.get("full_name") %></div>
                                        <div style="font-size: 0.75rem; color: var(--text-muted);">@<%= item.get("username") %></div>
                                    </td>
                                    <td style="padding: 12px 10px; text-align: center; font-weight: 700; color: #e74c3c; font-size: 0.95rem;"><%= item.get("overdue_count") %></td>
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
