/**
 * Controller xử lý đăng nhập người dùng.
 * Thuộc tầng controller, điều phối yêu cầu đăng nhập và điều hướng người dùng dựa trên vai trò.
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;
import service.LoginService;

/**
 * Servlet xử lý yêu cầu đăng nhập (/login).
 * 
 * Đảm nhận hiển thị trang đăng nhập (GET) và xử lý xác thực thông tin đăng nhập (POST).
 * Sau khi đăng nhập thành công, điều hướng người dùng tới trang tương ứng dựa vào vai trò (ADMIN, LIBRARIAN, READER).
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    /**
     * Dịch vụ xử lý logic xác thực người dùng.
     */
    private final LoginService loginService = new LoginService();

    /**
     * Hiển thị giao diện trang đăng nhập.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @throws ServletException khi có lỗi Servlet
     * @throws IOException khi có lỗi vào/ra dữ liệu
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    /**
     * Xử lý xác thực thông tin đăng nhập và chuyển hướng người dùng.
     *
     * @param request yêu cầu HTTP chứa thông tin username và password
     * @param response phản hồi HTTP
     * @throws ServletException khi có lỗi Servlet
     * @throws IOException khi có lỗi vào/ra dữ liệu
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Thiết lập bảng mã UTF-8 để nhận tham số chứa tiếng Việt chuẩn xác
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 2. Đọc tên đăng nhập và mật khẩu từ form gửi lên
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            // 3. Gọi tầng Service xử lý xác thực (kiểm tra rỗng, kiểm tra mật khẩu MD5, kiểm tra tài khoản bị khóa)
            User user = loginService.login(username, password);

            // 4. Đăng nhập thành công -> Tạo/Lấy phiên làm việc Session và lưu thông tin người dùng
            HttpSession session = request.getSession();
            session.setAttribute("loggedUser", user);

            // 5. Điều hướng người dùng dựa vào vai trò (Role-based Redirect)
            if (user.isAdmin()) {
                // Admin -> Chuyển hướng tới trang quản trị Admin
                response.sendRedirect(request.getContextPath() + "/admin");
            } else if (user.isLibrarian()) {
                // Thủ thư (Librarian) -> Chuyển hướng tới Dashboard thủ thư
                response.sendRedirect(request.getContextPath() + "/librarian/dashboard/library");
            } else {
                // Độc giả (Reader) -> Chuyển hướng tới trang chủ độc giả
                response.sendRedirect(request.getContextPath() + "/home");
            }
            return;
        } catch (LoginService.EmptyInputException | LoginService.InvalidCredentialsException | LoginService.AccountLockedException e) {
            // Bắt lỗi nghiệp vụ phân tầng từ Service để hiển thị thông báo lỗi thân thiện ra giao diện
            request.setAttribute("error", e.getMessage());
        } catch (Exception e) {
            // Bắt lỗi hệ thống không mong muốn
            request.setAttribute("error", "Lỗi máy chủ: " + e.getMessage());
        }

        // Nếu có lỗi -> Chuyển tiếp (forward) lại trang login.jsp kèm thông báo lỗi
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }
}