package controller;

import dao.NotificationDAO;
import model.Notification;
import model.User;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "NotificationInboxServlet", urlPatterns = {
    "/notification/my", "/notification/read", "/notification/read-all"
})
public class NotificationInboxServlet extends HttpServlet {

    private final NotificationDAO notificationDAO = new NotificationDAO();

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
        
        int page = 1;
        try {
            String p = request.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p.trim()));
        } catch (NumberFormatException ignored) {}

        int pageSize = 10;
        try {
            List<Notification> list = notificationDAO.getNotificationsByUserId(loggedUser.getId(), page, pageSize);
            int totalRecords = notificationDAO.countNotificationsByUserId(loggedUser.getId());
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            if (totalPages < 1) totalPages = 1;

            request.setAttribute("notificationList", list);
            request.setAttribute("totalRecords", totalRecords);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("currentPageNum", page);
            request.setAttribute("activePage", "notifications");
            request.setAttribute("pageTitle", "Hộp thư thông báo của bạn – FPT Library");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải thông tin hộp thư: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/notification-inbox.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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
            if ("/notification/read".equals(path)) {
                int id = Integer.parseInt(request.getParameter("id"));
                notificationDAO.markAsRead(id);
                response.getWriter().write("success");
                return;
            } else if ("/notification/read-all".equals(path)) {
                notificationDAO.markAllAsRead(loggedUser.getId());
                session.setAttribute("successMsg", "Đã đánh dấu tất cả thông báo là đã đọc!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/notification/my");
    }
}
