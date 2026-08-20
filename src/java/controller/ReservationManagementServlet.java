package controller;

import dao.ReservationDAO;
import model.ReservationRecord;
import model.User;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "ReservationManagementServlet", urlPatterns = {
    "/admin/reservation/list", "/admin/reservation/update",
    "/librarian/reservation/list", "/librarian/reservation/update"
})
public class ReservationManagementServlet extends HttpServlet {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final service.ReservationService reservationService = new service.ReservationService();

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
            reservationService.expireExpiredReadyReservations();
            List<ReservationRecord> list = reservationDAO.searchReservations(status, keyword, page, pageSize);
            int totalRecords = reservationDAO.countReservations(status, keyword);
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            if (totalPages < 1) totalPages = 1;

            request.setAttribute("reservationList", list);
            request.setAttribute("totalRecords", totalRecords);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("currentPageNum", page);
            request.setAttribute("selectedStatus", status != null ? status : "");
            request.setAttribute("keyword", keyword != null ? keyword : "");
            request.setAttribute("isManagePageAttr", true);
            request.setAttribute("activePage", "reservation");
            request.setAttribute("pageTitle", "Quản lý đặt giữ chỗ – FPT Library");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải danh sách đặt chỗ: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/reservation-list.jsp").forward(request, response);
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
            if (path.endsWith("/reservation/update")) {
                int id = Integer.parseInt(request.getParameter("id"));
                String action = request.getParameter("action");
                if ("ready".equals(action)) {
                    ReservationRecord tempRes = reservationDAO.findById(id);
                    if (tempRes != null && new dao.FineDAO().searchByUser(tempRes.getUserId(), "UNPAID", null).size() > 0) {
                        session.setAttribute("errorMsg", "Không thể chuyển sang trạng thái Sẵn sàng: Độc giả này hiện đang có khoản phạt chưa thanh toán!");
                    } else if (tempRes != null && new dao.BorrowRecordDAO().countActiveByUserId(tempRes.getUserId()) >= 3) {
                        session.setAttribute("errorMsg", "Không thể chuyển sang trạng thái Sẵn sàng: Độc giả này đã đạt giới hạn tối đa 3 lượt mượn hoạt động!");
                    } else {
                        ReservationRecord record = reservationDAO.manuallyReadyReservation(id, loggedUser.getUsername());
                        if (record != null) {
                            reservationService.notifyReservationReady(record, true);
                            session.setAttribute("successMsg", "Đã cập nhật trạng thái thành công và gửi thông báo cho độc giả!");
                        } else {
                            session.setAttribute("errorMsg", "Không thể chuyển sang trạng thái Sẵn sàng. Không có bản sao nào khả dụng hoặc yêu cầu không ở trạng thái Chờ mượn!");
                        }
                    }
                } else {
                    String status = "WAITING";
                    if ("cancel".equals(action)) {
                        status = "CANCELLED";
                    } else if ("complete".equals(action)) {
                        status = "COMPLETED";
                    }
                    boolean success = reservationDAO.updateStatus(id, status);
                    if (success) {
                        session.setAttribute("successMsg", "Đã cập nhật trạng thái đặt chỗ thành công!");
                    } else {
                        session.setAttribute("errorMsg", "Cập nhật trạng thái đặt chỗ thất bại!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + prefix + "/reservation/list");
    }
}
