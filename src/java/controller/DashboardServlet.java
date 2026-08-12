package controller;

import dao.DashboardDao;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller xử lý các trang Dashboard thống kê vận hành (Library & Admin).
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {
    "/dashboard/library", "/admin/dashboard/library", "/librarian/dashboard/library", "/dashboard/admin", "/admin"
})
public class DashboardServlet extends HttpServlet {

    private final DashboardDao dashboardDao = new DashboardDao();

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
        String path = request.getServletPath();

        try {
            if ("/dashboard/library".equals(path) || "/admin/dashboard/library".equals(path) || "/librarian/dashboard/library".equals(path)) {
                // Librarian & Admin
                if (!loggedUser.isAdminOrLibrarian()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này.");
                    return;
                }
                
                request.setAttribute("totalBooks", dashboardDao.getTotalBooks());
                request.setAttribute("totalCopies", dashboardDao.getTotalCopies());
                request.setAttribute("statusCount", dashboardDao.getCopiesCountByStatus());
                request.setAttribute("conditionCount", dashboardDao.getCopiesCountByCondition());
                request.setAttribute("topBooks", dashboardDao.getTopBorrowedBooks(5));
                request.setAttribute("topOverdue", dashboardDao.getTopOverdueUsers(5));
                
                request.setAttribute("currentPage", "dashboard-library");
                request.setAttribute("pageTitle", "Thống kê Thư viện – FPT Library");
                request.setAttribute("isManagePageAttr", true);
                request.getRequestDispatcher("/WEB-INF/views/dashboard/library_statistics.jsp").forward(request, response);
                
            } else if ("/dashboard/admin".equals(path) || "/admin".equals(path)) {
                // Admin only
                if (!loggedUser.isAdmin()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ tài khoản Admin mới có quyền truy cập.");
                    return;
                }
                
                request.setAttribute("roleCount", dashboardDao.getUsersCountByRole());
                request.setAttribute("finesStats", dashboardDao.getFinesStats());
                request.setAttribute("recentLogs", dashboardDao.getRecentAuditLogs(10));
                
                request.setAttribute("currentPage", "dashboard-admin");
                request.setAttribute("pageTitle", "Thống kê hệ thống Admin – FPT Library");
                request.setAttribute("isManagePageAttr", true);
                request.getRequestDispatcher("/WEB-INF/views/dashboard/admin_statistics.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải dữ liệu thống kê: " + e.getMessage());
            request.setAttribute("isManagePageAttr", true);
            if ("/dashboard/library".equals(path) || "/admin/dashboard/library".equals(path) || "/librarian/dashboard/library".equals(path)) {
                request.setAttribute("currentPage", "dashboard-library");
                request.setAttribute("pageTitle", "Thống kê Thư viện – FPT Library");
                request.getRequestDispatcher("/WEB-INF/views/dashboard/library_statistics.jsp").forward(request, response);
            } else {
                request.setAttribute("currentPage", "dashboard-admin");
                request.setAttribute("pageTitle", "Thống kê hệ thống Admin – FPT Library");
                request.getRequestDispatcher("/WEB-INF/views/dashboard/admin_statistics.jsp").forward(request, response);
            }
        }
    }
}
