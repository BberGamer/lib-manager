package controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import model.User;
import service.UserProfileService;

@WebServlet(name = "UserProfileController", urlPatterns = {"/user/profile"})
public class UserProfileController extends HttpServlet {

    private final UserProfileService userProfileService = new UserProfileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User logged = (User) session.getAttribute("loggedUser");
        String idParam = request.getParameter("id");
        int id = logged.getId();

        // Chỉ admin mới được xem hồ sơ người khác qua ?id=...
        if (idParam != null && isAdmin(logged)) {
            try {
                id = Integer.parseInt(idParam);
            } catch (NumberFormatException e) { /* giữ nguyên id = chính họ nếu tham số sai */ }
        }

        try {
            User profileUser = userProfileService.getProfile(id);
            request.setAttribute("profileUser", profileUser);
        } catch (Exception e) {
            request.setAttribute("error", "Không thể tải thông tin người dùng: " + e.getMessage());
        }
        request.getRequestDispatcher("/user_profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User logged = (User) session.getAttribute("loggedUser");
        String idParam = request.getParameter("id");
        int id = logged.getId();
        boolean isAdminEditingOther = false;

        if (idParam != null && isAdmin(logged)) {
            try {
                id = Integer.parseInt(idParam);
                isAdminEditingOther = (id != logged.getId());
            } catch (NumberFormatException e) { }
        }

        String action = request.getParameter("action");

        try {
            User targetUser = userProfileService.getProfile(id);
            if (targetUser == null) {
                request.setAttribute("error", "Người dùng không tồn tại.");
                doGet(request, response);
                return;
            }

            String error = null;
            if ("updateProfile".equals(action)) {
                String fullName = request.getParameter("fullName");
                String email = request.getParameter("email");
                String phone = request.getParameter("phone");
                String studentId = request.getParameter("studentId");
                String avatar = request.getParameter("avatar");
                String role = request.getParameter("role");
                String activeParam = request.getParameter("active");
                Integer active = null;
                if (activeParam != null) {
                    try { active = Integer.parseInt(activeParam); } catch (NumberFormatException e) { }
                }

                error = userProfileService.updateProfile(targetUser, fullName, email, phone,
                        studentId, avatar, role, active, isAdmin(logged));
                request.setAttribute(error == null ? "success" : "error",
                        error == null ? "Cập nhật thông tin cá nhân thành công." : error);

            } else if ("changePassword".equals(action)) {
                String oldPassword = request.getParameter("oldPassword");
                String newPassword = request.getParameter("newPassword");
                String confirmPassword = request.getParameter("confirmPassword");
                error = userProfileService.changePassword(targetUser, oldPassword, newPassword,
                        confirmPassword, isAdminEditingOther);
                request.setAttribute(error == null ? "success" : "error",
                        error == null ? "Đổi mật khẩu thành công." : error);
            }

            // Nếu đang tự sửa hồ sơ của chính mình, cập nhật lại session cho khớp dữ liệu mới
            if (!isAdminEditingOther) {
                session.setAttribute("loggedUser", targetUser);
            }
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi xử lý: " + e.getMessage());
        }

        doGet(request, response);
    }

    // Kiểm tra quyền admin dựa trên field role của User
    private boolean isAdmin(User user) {
        return "admin".equalsIgnoreCase(user.getRole());
    }
}