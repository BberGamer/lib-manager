package controller;

import dao.BookCopyDAO;
import dao.BookDAO;
import dao.BookDAOImpl;
import model.Book;
import model.BookCopy;
import model.User;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "BookCopyListServlet", urlPatterns = {"/book/copies"})
public class BookCopyListServlet extends HttpServlet {

    private static final int PAGE_SIZE = 20;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        
        // 1. Authorization check
        HttpSession session = request.getSession(false);
        User loggedUser = (session != null) ? (User) session.getAttribute("loggedUser") : null;
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!loggedUser.isAdminOrLibrarian()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem thông tin này.");
            return;
        }

        // 2. Parse required bookId parameter
        String bookIdStr = request.getParameter("bookId");
        int bookId = -1;
        try {
            if (bookIdStr != null) {
                bookId = Integer.parseInt(bookIdStr.trim());
            }
        } catch (NumberFormatException e) {
            // ignore
        }

        if (bookId <= 0) {
            response.sendRedirect(request.getContextPath() + "/books");
            return;
        }

        // 3. Retrieve Book and verify it exists
        BookDAO bookDao = new BookDAOImpl();
        Book book = null;
        try {
            book = bookDao.findById(bookId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (book == null) {
            response.sendRedirect(request.getContextPath() + "/books");
            return;
        }

        // 4. Parse search, filter, and paging parameters
        String keyword = request.getParameter("keyword");
        if (keyword != null) {
            keyword = keyword.trim();
        } else {
            keyword = "";
        }

        String status = request.getParameter("status");
        if (status != null) {
            status = status.trim();
        } else {
            status = "";
        }

        String area = request.getParameter("area");
        if (area != null) {
            area = area.trim();
        } else {
            area = "";
        }

        int pageNum = 1;
        String pageStr = request.getParameter("page");
        try {
            if (pageStr != null) {
                pageNum = Integer.parseInt(pageStr.trim());
                if (pageNum < 1) pageNum = 1;
            }
        } catch (NumberFormatException e) {
            pageNum = 1;
        }

        // 5. Query databases
        BookCopyDAO copyDao = new BookCopyDAO();
        List<BookCopy> copies = null;
        int totalRecords = 0;
        List<String> distinctAreas = null;

        try {
            copies = copyDao.searchCopies(bookId, keyword, status, area, pageNum, PAGE_SIZE);
            totalRecords = copyDao.countCopies(bookId, keyword, status, area);
            distinctAreas = copyDao.getDistinctAreas();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
        if (totalPages < 1) {
            totalPages = 1;
        }

        // 6. Set request attributes
        request.setAttribute("book", book);
        request.setAttribute("copies", copies);
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPageNum", pageNum);
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("selectedArea", area);
        request.setAttribute("distinctAreas", distinctAreas);
        
        request.setAttribute("currentPage", "books"); // for navbar active state highlight
        request.setAttribute("pageTitle", "Danh sách bản sao: " + book.getTitle() + " – FPT Library");
        
        // 7. Forward to JSP
        request.getRequestDispatcher("/WEB-INF/views/book_copy_list.jsp").forward(request, response);
    }
}
