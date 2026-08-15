package controller;

import dao.FineDAO;
import service.FineService;
import model.Fine;
import model.User;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(name = "FineManagementServlet", urlPatterns = {
    "/admin/fine/list", "/admin/fine/create", "/admin/fine/update-status",
    "/librarian/fine/list", "/librarian/fine/create", "/librarian/fine/update-status"
})
public class FineManagementServlet extends HttpServlet {

    private final FineDAO fineDAO = new FineDAO();
    private final FineService fineService = new FineService();

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

        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");
        
        int page = 1;
        try {
            String p = request.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p.trim()));
        } catch (NumberFormatException ignored) {}

        int pageSize = 15;
        try {
            fineService.synchronizeAllOverdueFines();
            List<Fine> list = fineDAO.searchFines(status, keyword, page, pageSize);
            int totalRecords = fineDAO.countFines(status, keyword);
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            if (totalPages < 1) totalPages = 1;

            request.setAttribute("fineList", list);
            request.setAttribute("totalRecords", totalRecords);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("currentPageNum", page);
            request.setAttribute("selectedStatus", status != null ? status : "");
            request.setAttribute("keyword", keyword != null ? keyword : "");
            request.setAttribute("isManagePageAttr", true);
            request.setAttribute("activePage", "fine");
            request.setAttribute("pageTitle", "Quản lý khoản phạt – FPT Library");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải danh sách khoản phạt: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/fine-list.jsp").forward(request, response);
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
            if (path.endsWith("/fine/create")) {
                createFine(request, response, prefix);
            } else if (path.endsWith("/fine/update-status")) {
                updateStatus(request, response, prefix);
            } else {
                response.sendRedirect(request.getContextPath() + prefix + "/fine/list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Đã xảy ra lỗi: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + prefix + "/fine/list");
        }
    }

    private void createFine(HttpServletRequest request, HttpServletResponse response, String prefix) throws Exception {
        HttpSession session = request.getSession();
        int borrowRecordId = Integer.parseInt(request.getParameter("borrowRecordId"));
        int userId = Integer.parseInt(request.getParameter("userId"));
        String bookCondition = request.getParameter("bookCondition");
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));
        String reason = request.getParameter("reason");

        boolean success = fineService.createBookConditionFine(
                borrowRecordId, userId, bookCondition, amount, reason);
        
        if (success) {
            session.setAttribute("successMsg", "Đã ghi nhận khoản phạt phạt tiền thành công!");
        } else {
            session.setAttribute("errorMsg", "Ghi nhận khoản phạt thất bại!");
        }
        response.sendRedirect(request.getContextPath() + prefix + "/borrow/list");
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response, String prefix) throws Exception {
        HttpSession session = request.getSession();
        int id = Integer.parseInt(request.getParameter("id"));
        String status = request.getParameter("status"); // PAID, WAIVED
        String paymentMethod = request.getParameter("paymentMethod");
        String paymentNote = request.getParameter("paymentNote");

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "CASH";
        }

        User loggedUser = (User) session.getAttribute("loggedUser");
        String operator = loggedUser != null ? loggedUser.getUsername() : "System";

        boolean success = fineDAO.updateStatus(id, status, paymentMethod, paymentNote, operator);
        if (success) {
            session.setAttribute("successMsg", "Cập nhật trạng thái khoản phạt thành công!");
        } else {
            session.setAttribute("errorMsg", "Cập nhật trạng thái khoản phạt thất bại!");
        }
        response.sendRedirect(request.getContextPath() + prefix + "/fine/list");
    }
}
