package controller;

import dao.BorrowRecordDAO;
import dao.BookCopyDAO;
import model.BorrowRecord;
import model.BookCopy;
import model.User;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "BorrowManagementServlet", urlPatterns = {
    "/admin/borrow/list", "/admin/borrow/confirm-loan", "/admin/borrow/confirm-return",
    "/librarian/borrow/list", "/librarian/borrow/confirm-loan", "/librarian/borrow/confirm-return"
})
public class BorrowManagementServlet extends HttpServlet {

    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
    private final BookCopyDAO bookCopyDAO = new BookCopyDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User loggedUser = (User) session.getAttribute("loggedUser");
        if (!loggedUser.isAdminOrLibrarian()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này.");
            return;
        }

        String path = request.getServletPath();
        String prefix = loggedUser.isAdmin() ? "/admin" : "/librarian";
        
        if (path.endsWith("/borrow/list")) {
            showList(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list");
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");
        
        int page = 1;
        try {
            String p = request.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p.trim()));
        } catch (NumberFormatException ignored) {}

        int pageSize = 15;
        try {
            List<BorrowRecord> list = borrowRecordDAO.searchBorrowRecords(status, keyword, page, pageSize);
            int totalRecords = borrowRecordDAO.countBorrowRecords(status, keyword);
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            if (totalPages < 1) totalPages = 1;

            request.setAttribute("borrowList", list);
            request.setAttribute("totalRecords", totalRecords);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("currentPageNum", page);
            request.setAttribute("selectedStatus", status != null ? status : "");
            request.setAttribute("keyword", keyword != null ? keyword : "");
            request.setAttribute("isManagePageAttr", true);
            request.setAttribute("activePage", "borrow");
            request.setAttribute("pageTitle", "Quản lý mượn trả – FPT Library");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải danh sách mượn sách: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/borrow-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User loggedUser = (User) session.getAttribute("loggedUser");
        if (!loggedUser.isAdminOrLibrarian()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện hành động này.");
            return;
        }

        String path = request.getServletPath();
        String prefix = loggedUser.isAdmin() ? "/admin" : "/librarian";
        try {
            if (path.endsWith("/borrow/confirm-loan")) {
                confirmLoan(request, response, loggedUser.getUsername(), prefix);
            } else if (path.endsWith("/borrow/confirm-return")) {
                confirmReturn(request, response, loggedUser.getUsername(), prefix);
            } else {
                response.sendRedirect(request.getContextPath() + prefix + "/borrow/list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Đã xảy ra lỗi: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list");
        }
    }

    private void confirmLoan(HttpServletRequest request, HttpServletResponse response, String operator, String prefix) throws Exception {
        int id = Integer.parseInt(request.getParameter("id"));
        String barcode = request.getParameter("barcode");
        HttpSession session = request.getSession();

        if (barcode == null || barcode.trim().isEmpty()) {
            session.setAttribute("errorMsg", "Vui lòng nhập hoặc quét mã Barcode của bản sao sách!");
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list?status=PENDING");
            return;
        }

        BorrowRecord record = borrowRecordDAO.findById(id);
        if (record == null) {
            session.setAttribute("errorMsg", "Không tìm thấy yêu cầu mượn sách này!");
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list?status=PENDING");
            return;
        }

        List<BookCopy> copies = bookCopyDAO.searchCopies(record.getBookId(), barcode.trim(), null, null, 1, 10);
        BookCopy targetCopy = null;
        for (BookCopy c : copies) {
            if (barcode.trim().equalsIgnoreCase(c.getBarcode())) {
                targetCopy = c;
                break;
            }
        }

        if (targetCopy == null) {
            session.setAttribute("errorMsg", "Không tìm thấy bản sao sách có mã Barcode '" + barcode + "' của đầu sách này!");
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list?status=PENDING");
            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(targetCopy.getStatus())) {
            session.setAttribute("errorMsg", "Bản sao sách này hiện đang có trạng thái '" + targetCopy.getStatus() + "' và không khả dụng cho mượn!");
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list?status=PENDING");
            return;
        }

        boolean success = borrowRecordDAO.confirmLoan(id, targetCopy.getId(), operator);
        if (success) {
            session.setAttribute("successMsg", "Đã xác nhận cho mượn sách thành công!");
        } else {
            session.setAttribute("errorMsg", "Xác nhận cho mượn thất bại!");
        }
        response.sendRedirect(request.getContextPath() + prefix + "/borrow/list");
    }

    private void confirmReturn(HttpServletRequest request, HttpServletResponse response, String operator, String prefix) throws Exception {
        int id = Integer.parseInt(request.getParameter("id"));
        String condition = request.getParameter("condition");
        String note = request.getParameter("note");
        HttpSession session = request.getSession();

        if (condition == null || condition.trim().isEmpty()) {
            condition = "GOOD";
        }

        boolean success = borrowRecordDAO.confirmReturn(id, operator, condition, note);
        if (success) {
            session.setAttribute("successMsg", "Đã xác nhận hoàn trả sách thành công!");
        } else {
            session.setAttribute("errorMsg", "Xác nhận trả sách thất bại!");
        }
        response.sendRedirect(request.getContextPath() + prefix + "/borrow/list");
    }
}
