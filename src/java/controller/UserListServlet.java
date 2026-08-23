package controller;

import model.User;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import service.UserListService;

@WebServlet(name = "UserListServlet", urlPatterns = {"/users", "/admin/users", "/librarian/users"})
public class UserListServlet extends HttpServlet {

    private final UserListService userService = new UserListService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String q = request.getParameter("q");
        String role = request.getParameter("role");
        Integer active = parseIntOrNull(request.getParameter("active"));

        String sortField = request.getParameter("sort");
        String sortOrder = request.getParameter("order");
        if (sortField == null || sortField.isEmpty()) sortField = "username";
        if (!"DESC".equalsIgnoreCase(sortOrder)) sortOrder = "ASC";

        int page = parseIntOrDefault(request.getParameter("page"), 1);
        if (page < 1) page = 1;

        try {
            UserListService.SearchResult result = userService.search(q, role, active, sortField, sortOrder, page);
            
            request.setAttribute("users", result.users);
            request.setAttribute("totalRecords", result.totalRecords);
            request.setAttribute("totalPages", result.totalPages);
            request.setAttribute("currentPageNum", result.currentPage);
            request.setAttribute("q", q != null ? q : "");
            request.setAttribute("roleFilter", role);
            request.setAttribute("activeFilter", active);
            request.setAttribute("sortField", sortField);
            request.setAttribute("sortOrder", sortOrder);
            request.setAttribute("currentPage", "users");
            request.setAttribute("currentServletPath", request.getServletPath());
        } catch (Exception e) {
            request.setAttribute("error", "Không thể tải danh sách người dùng: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/user_list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Kiểm tra xác thực đăng nhập
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 2. Bắt buộc quyền Admin mới được thực hiện các thao tác thêm/sửa/xóa/khóa người dùng
        User logged = (User) session.getAttribute("loggedUser");
        if (!logged.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác quản trị này.");
            return;
        }

        String action = request.getParameter("action");
        try {
            // 3. Phân luồng xử lý theo hành động (action)
            switch (action == null ? "" : action) {
                case "create":
                    handleCreate(request); // Tạo người dùng mới
                    break;
                case "delete":
                    handleDelete(request); // Xóa người dùng
                    break;
                case "lock":
                case "unlock":
                    handleToggleActive(request, action); // Khóa / Mở khóa tài khoản
                    break;
                case "update":
                    handleUpdate(request); // Cập nhật người dùng
                    break;
                default:
                    session.setAttribute("errorMsg", "Hành động không hợp lệ.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Lỗi nghiệp vụ đã có message thân thiện từ UserService -> hiển thị thẳng cho Admin
            session.setAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Lỗi hệ thống, vui lòng thử lại.");
        }

        // 4. Áp dụng mẫu Post/Redirect/Get (PRG Pattern) để tránh bị lặp form khi F5
        String redirectPath = request.getServletPath() != null ? request.getServletPath() : "/users";
        response.sendRedirect(request.getContextPath() + redirectPath);
    }

    private void handleCreate(HttpServletRequest request) {
        User u = new User();
        u.setUsername(request.getParameter("username"));
        u.setFullName(request.getParameter("fullName"));
        u.setEmail(request.getParameter("email"));
        u.setPhone(request.getParameter("phone"));
        u.setStudentId(request.getParameter("studentId"));
        u.setAvatar(request.getParameter("avatar"));
        u.setRole(request.getParameter("role"));

        // createUser trả về newUserId của tài khoản vừa tạo
        int newUserId = userService.createUser(u, request.getParameter("password"));

        // Ghi audit log tạo tài khoản mới
        User logged = (User) request.getSession().getAttribute("loggedUser");
        String operator = logged != null ? logged.getUsername() : "admin";
        utils.AuditLogger.logCreateUser(operator, newUserId, u.getUsername(),
                u.getRole() != null ? u.getRole() : "READER");
        request.getSession().setAttribute("successMsg", "Tạo người dùng thành công.");
    }

    private void handleDelete(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        User logged = (User) request.getSession().getAttribute("loggedUser");
        userService.deleteUser(id, logged);
        String operator = logged != null ? logged.getUsername() : "admin";
        utils.AuditLogger.log("DELETE_USER", operator, id, "Vô hiệu hóa tài khoản (chuyển active = 0) cho User ID #" + id);
        request.getSession().setAttribute("successMsg", "Xóa người dùng thành công (tài khoản đã chuyển sang trạng thái Khóa).");
    }

    private void handleToggleActive(HttpServletRequest request, String action) {
        int id = Integer.parseInt(request.getParameter("id"));
        boolean isUnlock = "unlock".equals(action);
        User logged = (User) request.getSession().getAttribute("loggedUser");
        userService.setActive(id, isUnlock ? 1 : 0, logged);
        String operator = logged != null ? logged.getUsername() : "admin";
        if (isUnlock) {
            utils.AuditLogger.logUnlockAccount(operator, id);
        } else {
            utils.AuditLogger.logLockAccount(operator, id, "Khóa tài khoản bởi Quản trị viên");
        }
        request.getSession().setAttribute("successMsg", "Thao tác thành công.");
    }

    private void handleUpdate(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        Integer active = parseIntOrNull(request.getParameter("active"));

        userService.updateUser(id,
                request.getParameter("fullName"),
                request.getParameter("email"),
                request.getParameter("phone"),
                request.getParameter("studentId"),
                request.getParameter("avatar"),
                request.getParameter("role"),
                active,
                request.getParameter("password"));

        // Ghi audit log cập nhật tài khoản
        User logged = (User) request.getSession().getAttribute("loggedUser");
        String operator = logged != null ? logged.getUsername() : "admin";
        String newRole = request.getParameter("role");
        String detail = "Cập nhật thông tin tài khoản User ID #" + id
                + (newRole != null && !newRole.isEmpty() ? " | Vai trò mới: " + newRole : "");
        utils.AuditLogger.logUpdateUser(operator, id, detail);

        request.getSession().setAttribute("successMsg", "Cập nhật thành công.");
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    private int parseIntOrDefault(String s, int def) {
        if (s == null || s.isEmpty()) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}