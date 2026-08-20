package controller;

import dao.BorrowRecordDAO;
import dao.BookCopyDAO;
import model.BorrowRecord;
import model.BookCopy;
import model.User;
import service.BorrowService;
import service.ReservationService;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "BorrowManagementServlet", urlPatterns = {
    "/admin/borrow/list", "/admin/borrow/confirm-pickup", "/admin/borrow/confirm-return",
    "/librarian/borrow/list", "/librarian/borrow/confirm-pickup", "/librarian/borrow/confirm-return"
})
public class BorrowManagementServlet extends HttpServlet {

    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
    private final BookCopyDAO bookCopyDAO = new BookCopyDAO();
    private final BorrowService borrowService = new BorrowService();
    private final ReservationService reservationService = new ReservationService();

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
            borrowService.expirePendingBorrowRequests();
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
            User loggedUser = (User) request.getSession(false).getAttribute("loggedUser");
            request.setAttribute("borrowActionPrefix", loggedUser.isAdmin() ? "/admin" : "/librarian");
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
            if (path.endsWith("/borrow/confirm-pickup")) {
                confirmPickup(request, response, loggedUser.getUsername(), prefix);
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

    /** Xác nhận độc giả đã nhận bản sao được giữ, mọi thời hạn do service quyết định. */
    private void confirmPickup(HttpServletRequest request, HttpServletResponse response,
            String operator, String prefix) throws Exception {
        int borrowId = Integer.parseInt(request.getParameter("id"));
        boolean success = borrowService.confirmPickup(borrowId, operator);
        request.getSession().setAttribute(success ? "successMsg" : "errorMsg",
                success ? "Đã xác nhận giao sách thành công."
                        : "Không thể xác nhận giao sách. Yêu cầu có thể đã hết hạn hoặc không hợp lệ.");
        response.sendRedirect(request.getContextPath() + prefix + "/borrow/list");
    }

    private void confirmLoan(HttpServletRequest request, HttpServletResponse response, String operator, String prefix) throws Exception {
        int id = Integer.parseInt(request.getParameter("id"));
        String barcode = request.getParameter("barcode");
        HttpSession session = request.getSession();

        if (barcode == null || barcode.trim().isEmpty()) {
            session.setAttribute("errorMsg", "Vui lòng nhập hoặc quét mã vạch của bản sao sách!");
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list?status=PENDING_PICKUP");
            return;
        }

        BorrowRecord record = borrowRecordDAO.findById(id);
        if (record == null) {
            session.setAttribute("errorMsg", "Không tìm thấy yêu cầu mượn sách này!");
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list?status=PENDING_PICKUP");
            return;
        }

        List<BookCopy> copies = bookCopyDAO.searchCopies(record.getBookId(), barcode.trim(), null, 1, 10);
        BookCopy targetCopy = null;
        for (BookCopy c : copies) {
            if (barcode.trim().equalsIgnoreCase(c.getBarcode())) {
                targetCopy = c;
                break;
            }
        }

        if (targetCopy == null) {
            session.setAttribute("errorMsg", "Không tìm thấy bản sao sách có mã vạch '" + barcode + "' của đầu sách này!");
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list?status=PENDING_PICKUP");
            return;
        }

        boolean isBorrowedOrReserved = false;
        try {
            isBorrowedOrReserved = bookCopyDAO.isCopyBorrowedOrReserved(targetCopy.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean hasBorrowableCondition = "GOOD".equals(targetCopy.getBookCondition())
                || "WORN".equals(targetCopy.getBookCondition());
        if (!hasBorrowableCondition || isBorrowedOrReserved) {
            session.setAttribute("errorMsg", "Bản sao sách này hiện không khả dụng cho mượn (đang mượn/giữ hoặc tình trạng không tốt)!");
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list?status=PENDING_PICKUP");
            return;
        }

        boolean success = borrowRecordDAO.confirmLoan(
                id, targetCopy.getId(), operator, BorrowService.LOAN_PERIOD_DAYS);
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

        dao.BorrowRecordDAO.ReturnResult result = borrowService.confirmReturn(id, operator, condition, note);
        if (result.success) {
            session.setAttribute("successMsg", "Đã xác nhận hoàn trả sách thành công!");
            
            if (result.activatedReservationId != -1) {
                reservationService.notifyReservationReady(
                        result.activatedReservationId, result.activatedUserId,
                        result.userFullName, result.userEmail, result.bookTitle, true);
            }
        } else {
            session.setAttribute("errorMsg", "Xác nhận trả sách thất bại!");
        }
        response.sendRedirect(request.getContextPath() + prefix + "/borrow/list");
    }
}
