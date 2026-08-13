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
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            User user = loginService.login(username, password);
            HttpSession session = request.getSession();
            session.setAttribute("loggedUser", user);

            if (user.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/admin");
            } else if (user.isLibrarian()) {
                response.sendRedirect(request.getContextPath() + "/librarian/dashboard/library");
            } else {
                response.sendRedirect(request.getContextPath() + "/home");
            }
            return;
        } catch (LoginService.EmptyInputException | LoginService.InvalidCredentialsException | LoginService.AccountLockedException e) {
            request.setAttribute("error", e.getMessage());
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi máy chủ: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }
}