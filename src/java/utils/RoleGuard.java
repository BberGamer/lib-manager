package utils;

import model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * RBAC utility: kiểm tra quyền theo ma trận §4.2 của spec.
 * Dùng để guard các endpoint trong Servlet trước khi xử lý business logic.
 *
 * Role hierarchy: ADMIN > LIBRARIAN > READER (USER)
 * Spec: library-rules-spec-v2.md §4
 */
public class RoleGuard {

    private RoleGuard() {}

    /**
     * Lấy logged user từ session. Trả về null nếu chưa đăng nhập.
     */
    public static User getLoggedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (User) session.getAttribute("loggedUser") : null;
    }

    /**
     * Yêu cầu đăng nhập. Nếu chưa đăng nhập → redirect /login.
     * @return user nếu đã đăng nhập, null nếu không (đã redirect)
     */
    public static User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getLoggedUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        return user;
    }

    /**
     * Yêu cầu role Librarian hoặc Admin.
     * Nếu không thỏa → 403 Forbidden.
     * @return true nếu có quyền, false nếu không (đã gửi error response)
     */
    public static boolean requireLibrarianOrAdmin(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (user == null || !user.isAdminOrLibrarian()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền thực hiện thao tác này. Yêu cầu quyền Thủ thư hoặc Admin.");
            return false;
        }
        return true;
    }

    /**
     * Yêu cầu role Admin.
     * Nếu không phải Admin → 403 Forbidden.
     * @return true nếu là Admin, false nếu không (đã gửi error response)
     */
    public static boolean requireAdmin(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (user == null || !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền thực hiện thao tác này. Yêu cầu quyền Admin.");
            return false;
        }
        return true;
    }

    /**
     * §4.2 Kiểm tra user đang thao tác trên chính tài khoản của mình.
     * Librarian và Admin được bypass kiểm tra này.
     */
    public static boolean isSelfOrStaff(User loggedUser, int targetUserId) {
        if (loggedUser == null) return false;
        if (loggedUser.isAdminOrLibrarian()) return true;
        return loggedUser.getId() == targetUserId;
    }

    /**
     * §4.2 Kiểm tra quyền override cảnh báo vượt ngưỡng mượn (§1.2).
     * Chỉ Librarian hoặc Admin mới có quyền override.
     */
    public static boolean canOverrideBorrowLimit(User user) {
        return user != null && user.isAdminOrLibrarian();
    }

    /**
     * §4.2 Kiểm tra quyền tạo/waive phạt.
     * - Tạo phạt: Librarian hoặc Admin
     * - Waive phạt: chỉ Admin
     */
    public static boolean canCreateFine(User user) {
        return user != null && user.isAdminOrLibrarian();
    }

    public static boolean canWaiveFine(User user) {
        return user != null && user.isAdmin();
    }

    /**
     * §4.2, §4.3 Kiểm tra quyền khóa tài khoản.
     * - Librarian: chỉ khóa tạm (không mở khóa)
     * - Admin: toàn quyền khóa/mở
     */
    public static boolean canLockAccount(User user) {
        return user != null && user.isAdminOrLibrarian();
    }

    public static boolean canUnlockAccount(User user) {
        return user != null && user.isAdmin();
    }
}
