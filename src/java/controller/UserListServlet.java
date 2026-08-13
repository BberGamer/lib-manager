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
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        User logged = (User) session.getAttribute("loggedUser");
        if (!logged.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        try {
            switch (action == null ? "" : action) {
                case "create":
                    handleCreate(request);
                    break;
                case "delete":
                    handleDelete(request);
                    break;
                case "lock":
                case "unlock":
                    handleToggleActive(request, action);
                    break;
                case "update":
                    handleUpdate(request);
                    break;
                default:
                    session.setAttribute("errorMsg", "Hành động không hợp lệ.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Lỗi nghiệp vụ đã có message thân thiện từ UserService -> hiển thị thẳng
            session.setAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Lỗi hệ thống, vui lòng thử lại.");
        }

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

        userService.createUser(u, request.getParameter("password"));
        request.getSession().setAttribute("successMsg", "Tạo người dùng thành công.");
    }

    private void handleDelete(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        userService.deleteUser(id);
        request.getSession().setAttribute("successMsg", "Xóa người dùng thành công.");
    }

    private void handleToggleActive(HttpServletRequest request, String action) {
        int id = Integer.parseInt(request.getParameter("id"));
        userService.setActive(id, "unlock".equals(action) ? 1 : 0);
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