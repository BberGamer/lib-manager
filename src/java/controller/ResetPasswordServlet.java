package controller;

import dao.DAOTokenForget;
import dao.UserDAO;
import model.TokenForgetPassword;
import model.User;
import service.ResetSPasswordService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name="resetPassword", urlPatterns={"/resetPassword"})
public class ResetPasswordServlet extends HttpServlet {
    DAOTokenForget DAOToken = new DAOTokenForget();
    UserDAO DAOUser = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String token = request.getParameter("token");
        HttpSession session = request.getSession();
        if(token != null) {
            try {
                // 1. Tìm Token trong DB và kiểm tra 3 điều kiện: Không tồn tại, Đã dùng, Quá hạn (10 phút)
                TokenForgetPassword tokenForgetPassword = DAOToken.getTokenPassword(token);
                ResetSPasswordService service = new ResetSPasswordService();
                if(tokenForgetPassword == null) {
                    request.setAttribute("mess", "token invalid"); // Token không tồn tại
                    request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
                    return;
                }
                if(tokenForgetPassword.isIsUsed()) {
                    request.setAttribute("mess", "token is used"); // Token đã bị sử dụng rồi
                    request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
                    return;
                }
                if(service.isExpireTime(tokenForgetPassword.getExpiryTime())) {
                    request.setAttribute("mess", "token is expiry time"); // Token đã hết hạn quá 10 phút
                    request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
                    return;
                }
                User user = DAOUser.getUserById(tokenForgetPassword.getUserId());
                if (user != null) {
                    request.setAttribute("email", user.getEmail());
                }
                session.setAttribute("token", tokenForgetPassword.getToken());
                request.getRequestDispatcher("/WEB-INF/views/resetPassword.jsp").forward(request, response);
            } catch (Exception e) {
                request.setAttribute("mess", e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
            }
        } else {
            request.getRequestDispatcher("/WEB-INF/views/resetPassword.jsp").forward(request, response);
        }
    } 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirm_password");

        // 2. Validate hai mật khẩu nhập vào phải khớp nhau
        if(!password.equals(confirmPassword)) {
            request.setAttribute("mess", "confirm password must same password");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/views/resetPassword.jsp").forward(request, response);
            return;
        }
        HttpSession session = request.getSession();
        String tokenStr = (String) session.getAttribute("token");
        
        try {
            if (tokenStr != null) {
                TokenForgetPassword tokenForgetPassword = DAOToken.getTokenPassword(tokenStr);
                ResetSPasswordService service = new ResetSPasswordService();
                if (tokenForgetPassword == null) {
                    request.setAttribute("mess", "token invalid");
                    request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
                    return;
                }
                if (tokenForgetPassword.isIsUsed()) {
                    request.setAttribute("mess", "token is used");
                    request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
                    return;
                }
                if (service.isExpireTime(tokenForgetPassword.getExpiryTime())) {
                    request.setAttribute("mess", "token is expiry time");
                    request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
                    return;
                }
                // 3. Đánh dấu Token đã được sử dụng thành công (isUsed = true)
                tokenForgetPassword.setIsUsed(true);
                DAOToken.updateStatus(tokenForgetPassword);
                session.removeAttribute("token");
            }

            // 4. Cập nhật mật khẩu băm mới vào DB theo Email
            DAOUser.updatePasswordByEmail(email, password);
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("mess", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/resetPassword.jsp").forward(request, response);
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
