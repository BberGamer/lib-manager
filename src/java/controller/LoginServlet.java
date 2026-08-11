package controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import model.User;
import service.LoginService;


@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final LoginService loginService = new LoginService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            User user = loginService.login(username, password);
            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("loggedUser", user);
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            } else {
                request.setAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng.");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi máy chủ: " + e.getMessage());
        }
        request.getRequestDispatcher("/view/login.jsp").forward(request, response);
    }
}