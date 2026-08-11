package controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;
import dao.BookDAO;
import dao.BookDAOImpl;
import model.Book;

/**
 * HomeServlet — hiển thị trang chủ của hệ thống thư viện.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        
        try {
            // Lấy 4 cuốn sách mới nhất
            List<Book> latestBooks = bookDAO.searchBooks("", "", "created_at", "DESC", 1, 4);
            request.setAttribute("latestBooks", latestBooks);
        } catch (Exception e) {
            System.err.println("Lỗi khi tải sách mới nhất trên trang chủ: " + e.getMessage());
        }

        request.setAttribute("activePage", "home");
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
