package controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import model.User;
import service.UserProfileService;

@WebServlet(name = "UserProfileController", urlPatterns = {"/user/profile"})
public class UserProfileServlet extends HttpServlet {

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
        if (idParam != null && logged.isAdmin()) {
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
        request.getRequestDispatcher("/WEB-INF/views/user_profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Kiểm tra xác thực: Chưa đăng nhập -> Chuyển hướng về trang login
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User logged = (User) session.getAttribute("loggedUser");
        String idParam = request.getParameter("id");
        int id = logged.getId();
        boolean isAdminEditingOther = false;

        // 2. Phân quyền đặc biệt: Bắt buộc quyền Admin mới được truyền tham số ?id=X để sửa hồ sơ người khác
        if (idParam != null && logged.isAdmin()) {
            try {
                id = Integer.parseInt(idParam);
                isAdminEditingOther = (id != logged.getId()); // Flag đánh dấu Admin đang sửa tài khoản của người khác
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
                // Hành động 1: Cập nhật thông tin cá nhân
                String fullName = request.getParameter("fullName");
                String email = request.getParameter("email");
                String phone = request.getParameter("phone");
                String studentId = request.getParameter("studentId");
                String role = request.getParameter("role");
                String activeParam = request.getParameter("active");
                Integer active = null;
                if (activeParam != null) {
                    try { active = Integer.parseInt(activeParam); } catch (NumberFormatException e) { }
                }

                // Gọi Service thực hiện Validate dữ liệu và cập nhật DB (truyền logged.isAdmin() để cho phép sửa Role/Active)
                error = userProfileService.updateProfile(targetUser, fullName, email, phone,
                        studentId, role, active, logged.isAdmin());
                request.setAttribute(error == null ? "success" : "error",
                        error == null ? "Cập nhật thông tin cá nhân thành công." : error);

            } else if ("changePassword".equals(action)) {
                // Hành động 2: Đổi mật khẩu
                String oldPassword = request.getParameter("oldPassword");
                String newPassword = request.getParameter("newPassword");
                String confirmPassword = request.getParameter("confirmPassword");
                // Truyền isAdminEditingOther: Nếu Admin sửa giùm người khác -> Không cần nhập oldPassword
                error = userProfileService.changePassword(targetUser, oldPassword, newPassword,
                        confirmPassword, isAdminEditingOther);
                request.setAttribute(error == null ? "success" : "error",
                        error == null ? "Đổi mật khẩu thành công." : error);
            }

            // 3. Nếu người dùng tự sửa hồ sơ của chính mình -> Cập nhật lại đối tượng loggedUser trong Session
            if (!isAdminEditingOther) {
                session.setAttribute("loggedUser", targetUser);
            }
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi xử lý: " + e.getMessage());
        }

        doGet(request, response);
    }
}