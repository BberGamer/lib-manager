package controller;

import dao.AuditLogDao;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller quản lý và tra cứu Audit Logs hệ thống dành riêng cho Admin.
 */
@WebServlet(name = "AuditLogServlet", urlPatterns = {
    "/admin/audit-logs", "/admin/logs"
})
public class AuditLogServlet extends HttpServlet {

    private final AuditLogDao auditLogDao = new AuditLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User loggedUser = (User) session.getAttribute("loggedUser");
        if (!loggedUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ tài khoản Admin mới có quyền truy cập Nhật ký hệ thống.");
            return;
        }

        String action = request.getParameter("action");
        String performedBy = request.getParameter("performedBy");
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");

        int page = 1;
        try {
            String p = request.getParameter("page");
            if (p != null) {
                page = Math.max(1, Integer.parseInt(p.trim()));
            }
        } catch (NumberFormatException ignored) {}

        int pageSize = 15;

        try {
            List<Map<String, Object>> logs = auditLogDao.searchAuditLogs(action, performedBy, fromDate, toDate, page, pageSize);
            int totalLogs = auditLogDao.countAuditLogs(action, performedBy, fromDate, toDate);
            int totalPages = (int) Math.ceil((double) totalLogs / pageSize);
            if (totalPages < 1) totalPages = 1;

            List<String> distinctActions = auditLogDao.getDistinctActions();

            request.setAttribute("logs", logs);
            request.setAttribute("distinctActions", distinctActions);
            request.setAttribute("totalLogs", totalLogs);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("currentPageNum", page);

            // Giữ lại trạng thái bộ lọc trên giao diện
            request.setAttribute("selectedAction", action != null ? action.trim() : "");
            request.setAttribute("keywordPerformedBy", performedBy != null ? performedBy.trim() : "");
            request.setAttribute("selectedFromDate", fromDate != null ? fromDate.trim() : "");
            request.setAttribute("selectedToDate", toDate != null ? toDate.trim() : "");

            request.setAttribute("isManagePageAttr", true);
            request.setAttribute("activePage", "audit-logs");
            request.setAttribute("pageTitle", "Nhật ký kiểm toán hệ thống – FPT Library");
            request.setAttribute("pageStylesheet", "/assets/css/dashboard.css");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải nhật ký Audit Logs: " + e.getMessage());
            request.setAttribute("isManagePageAttr", true);
            request.setAttribute("activePage", "audit-logs");
            request.setAttribute("pageTitle", "Nhật ký kiểm toán hệ thống – FPT Library");
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/audit_logs.jsp").forward(request, response);
    }
}
