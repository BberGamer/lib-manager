package controller;

import dao.BorrowRecordDAO;
import model.BorrowRecord;
import model.User;
import service.BorrowService;
import service.ReservationService;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "BorrowManagementServlet", urlPatterns = {
    "/admin/borrow/list", "/admin/borrow/confirm-pickup", "/admin/borrow/confirm-return",
    "/librarian/borrow/list", "/librarian/borrow/confirm-pickup", "/librarian/borrow/confirm-return"
})
public class BorrowManagementServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BorrowManagementServlet.class.getName());
    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
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
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách mượn sách", exception);
            request.setAttribute("error", "Không thể tải danh sách mượn sách lúc này.");
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
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Không thể xử lý thao tác mượn trả", exception);
            session.setAttribute("errorMsg", "Không thể xử lý thao tác mượn trả lúc này.");
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list");
        }
    }

    private void confirmPickup(HttpServletRequest request, HttpServletResponse response,
            String operator, String prefix) throws Exception {
        int borrowId = Integer.parseInt(request.getParameter("id"));
        String barcode = request.getParameter("barcode");

        if (barcode == null || barcode.trim().isEmpty()) {
            request.getSession().setAttribute("errorMsg", "Vui lòng quét hoặc nhập mã vạch bản sao sách!");
            response.sendRedirect(request.getContextPath() + prefix + "/borrow/list");
            return;
        }

        boolean success = borrowService.confirmPickup(borrowId, barcode.trim(), operator);
        request.getSession().setAttribute(success ? "successMsg" : "errorMsg",
                success ? "Đã xác nhận giao sách thành công."
                        : "Không thể xác nhận giao sách. Mã vạch bản sao có thể không hợp lệ, không đúng đầu sách hoặc đã có người mượn/giữ.");
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
