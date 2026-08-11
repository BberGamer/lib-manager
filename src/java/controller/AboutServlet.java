package controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * AboutServlet – xử lý trang giới thiệu thư viện.
 * URL: /about
 */
@WebServlet(name = "AboutServlet", urlPatterns = {"/about"})
public class AboutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        request.setAttribute("activePage", "about");
        request.setAttribute("pageTitle", "Giới thiệu – FPT Library");
        request.setAttribute("pageDesc",
            "Tìm hiểu về thư viện FPT University: lịch sử, sứ mệnh, giá trị cốt lõi và thông tin liên hệ.");
        request.getRequestDispatcher("/WEB-INF/views/about.jsp").forward(request, response);
    }
}
