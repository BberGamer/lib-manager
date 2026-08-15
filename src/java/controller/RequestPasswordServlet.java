/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller;

import dao.DAOTokenForget;
import dao.UserDAO;
import model.TokenForgetPassword;
import model.User;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import service.ResetSPasswordService;

/**
 *
 * @author HP
 */
@WebServlet(name="requestPassword", urlPatterns={"/requestPassword"})
public class RequestPasswordServlet extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet requestPassword</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet requestPassword at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        UserDAO userDAO = new UserDAO();
        String email = request.getParameter("email");

        try {
            // 1. Kiểm tra Email có tồn tại trong cơ sở dữ liệu không
            User user = userDAO.getUserByEmail(email);
            if(user == null) {
                request.setAttribute("mess", "email khong ton tai");
                request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
                return;
            }
            ResetSPasswordService service = new ResetSPasswordService();
            // 2. Sinh mã Token ngẫu nhiên duy nhất (UUID) và đường dẫn reset
            String token = service.generateToken();
            
            String linkReset = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + request.getContextPath() + "/resetPassword?token=" + token;
            
            // 3. Khởi tạo đối tượng Token với hạn sử dụng 10 phút (expireDateTime)
            TokenForgetPassword newTokenForget = new TokenForgetPassword(
                    user.getId(), false, token, service.expireDateTime());
            
            // 4. Lưu Token vào bảng tokenForgetPassword trong DB
            DAOTokenForget daoToken = new DAOTokenForget();
            boolean isInsert = daoToken.insertTokenForget(newTokenForget);
            if(!isInsert) {
                request.setAttribute("mess", "have error in server");
                request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
                return;
            }

            // 5. Gửi Email chứa đường dẫn khôi phục mật khẩu qua JavaMail SMTP
            boolean isSend = service.sendEmail(email, linkReset, user.getUsername());
            if(!isSend) {
                request.setAttribute("mess", "can not send request");
                request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
                return;
            }
            request.setAttribute("mess", "send request success");
            request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("mess", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/requestPassword.jsp").forward(request, response);
        }
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
