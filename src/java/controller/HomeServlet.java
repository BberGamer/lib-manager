package controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * HomeServlet — hiển thị trang chủ của hệ thống thư viện.
 *
 * Tầng: Controller.
 * Trách nhiệm: forward đến home.jsp. Trang chủ truy cập tự do,
 * không yêu cầu đăng nhập. Nếu đã đăng nhập thì JSP hiển thị
 * quick-actions theo role; nếu chưa thì hiển thị giao diện chào mừng
 * với nút đăng nhập.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    /**
     * Xử lý GET /home — forward đến trang chủ, không yêu cầu đăng nhập.
     *
     * @param request  HTTP request từ trình duyệt
     * @param response HTTP response trả về client
     * @throws ServletException khi xảy ra lỗi xử lý servlet
     * @throws IOException      khi xảy ra lỗi I/O
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        request.setAttribute("activePage", "home");
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
