package controller;

import dao.BookDAO;
import dao.BookDAOImpl;
import model.Book;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import service.BorrowService;
import service.ReservationService;
import java.util.List;

/**
 * BookListServlet – xử lý danh sách sách với search, filter, sort, phân trang.
 * URL: /books
 *
 * Query params:
 *   keyword  – từ khóa tìm kiếm
 *   category – lọc danh mục
 *   sort     – cột sắp xếp (title | publish_year | available | price)
 *   order    – ASC | DESC
 *   page     – trang hiện tại (mặc định 1)
 *   view     – "grid" | "table" (mặc định "grid")
 */
@WebServlet(name = "BookListServlet", urlPatterns = {"/books", "/admin/books", "/librarian/books"})
public class BookListServlet extends HttpServlet {

    private final BorrowService borrowService = new BorrowService();
    private final ReservationService reservationService = new ReservationService();

    private static final int PAGE_SIZE = 12;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            borrowService.expirePendingBorrowRequests();
        } catch (Exception exception) {
            getServletContext().log("Không thể xử lý yêu cầu mượn đã hết hạn.", exception);
        }
        response.setContentType("text/html;charset=UTF-8");
        
        String path = request.getServletPath();
        if ("/admin/books".equals(path) || "/librarian/books".equals(path)) {
            request.setAttribute("isManagePageAttr", true);
        }
        if ("/librarian/books".equals(path)) {
            request.setAttribute("rolePath", "/librarian");
        } else if ("/admin/books".equals(path)) {
            request.setAttribute("rolePath", "/admin");
        }
        
        request.setAttribute("activePage", "books");
        request.setAttribute("pageTitle", "Danh sách sách – FPT Library");
        request.setAttribute("pageDesc",
            "Tìm kiếm và khám phá hàng trăm đầu sách học thuật trong thư viện FPT University.");

        // ---- Đọc tham số ----
        String keyword  = trim(request.getParameter("keyword"));
        String category = trim(request.getParameter("category"));
        String sort     = trim(request.getParameter("sort"));
        String order    = trim(request.getParameter("order"));
        String view     = trim(request.getParameter("view"));

        if (sort  == null || sort.isEmpty())  sort  = "title";
        if (order == null || order.isEmpty()) order = "ASC";
        if (view  == null || view.isEmpty())  view  = "grid";

        int page = 1;
        try {
            String p = request.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p.trim()));
        } catch (NumberFormatException ignored) {}

        // ---- Gọi DAO ----
        try {
            BookDAO dao = new BookDAOImpl();

            List<Book> books = dao.searchBooks(keyword, category, sort, order, page, PAGE_SIZE);
            reservationService.applyImmediateAvailability(books);
            int totalRecords = dao.countBooks(keyword, category);
            int totalPages   = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
            if (totalPages < 1) totalPages = 1;
            if (page > totalPages) page = totalPages;

            List<String> categories = dao.getAllCategories();

            // ---- Set attributes ----
            request.setAttribute("books",            books);
            request.setAttribute("totalRecords",     totalRecords);
            request.setAttribute("totalPages",       totalPages);
            request.setAttribute("currentPageNum",   page);
            request.setAttribute("categories",       categories);
            request.setAttribute("keyword",          keyword != null ? keyword : "");
            request.setAttribute("selectedCategory", category != null ? category : "");
            request.setAttribute("sortField",        sort);
            request.setAttribute("sortOrder",        order);
            request.setAttribute("viewMode",         view);
            request.setAttribute("pageSize",         PAGE_SIZE);

        } catch (Exception e) {
            request.setAttribute("dbError", "Lỗi tải dữ liệu: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/books.jsp").forward(request, response);
    }

    /** Hỗ trợ form POST (trường hợp submit từ form search). */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private String trim(String s) {
        return (s != null) ? s.trim() : null;
    }
}
