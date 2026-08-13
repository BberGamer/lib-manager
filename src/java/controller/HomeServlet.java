package controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import dao.BookDAO;
import dao.BookDAOImpl;
import model.Book;

/**
 * HomeServlet — hiển thị trang chủ của hệ thống thư viện.
 * Tầng: Controller (HttpServlet request/response orchestration)
 * Phụ trách: Khởi tạo dữ liệu trang chủ bao gồm các chỉ số thống kê và danh sách sách mới nhất.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(HomeServlet.class.getName());
    private final BookDAO bookDAO = new BookDAOImpl();

    /**
     * Xử lý yêu cầu GET hiển thị trang chủ.
     * 
     * @param request  đối tượng HttpServletRequest từ client
     * @param response đối tượng HttpServletResponse trả về client
     * @throws ServletException nếu có lỗi điều hướng Servlet
     * @throws IOException      nếu có lỗi I/O
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. Sách mượn nhiều nhất
        try {
            List<Book> popularBooks = bookDAO.getTopBorrowedBooks(8);
            request.setAttribute("popularBooks", popularBooks);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy sách mượn nhiều nhất trong HomeServlet", e);
        }

        // 2. Sách mới nhất
        try {
            List<Book> latestBooks = bookDAO.getLatestBooks(30, 8);
            request.setAttribute("latestBooks", latestBooks);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy sách mới nhất trong HomeServlet", e);
        }

        // 3. Thống kê tổng số sách
        try {
            int totalBooks = bookDAO.countBooks(null, null);
            request.setAttribute("totalBooks", totalBooks);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đếm tổng số sách trong HomeServlet", e);
        }

        // 4. Thống kê số lượng danh mục
        try {
            List<String> categories = bookDAO.getAllCategories();
            int totalCategories = (categories != null) ? categories.size() : 0;
            request.setAttribute("totalCategories", totalCategories);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đếm danh mục trong HomeServlet", e);
        }

        request.setAttribute("activePage", "home");
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
