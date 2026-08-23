package controller;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ReservationRecord;
import model.User;
import service.ReservationService;

/**
 * Điều phối màn hình quản lý reservation cho Admin và Librarian; đọc bộ lọc,
 * gọi {@link ReservationService} và chuyển dữ liệu sang JSP quản lý.
 */
public class ReservationManagementServlet extends HttpServlet {

    private static final Logger LOGGER
            = Logger.getLogger(ReservationManagementServlet.class.getName());
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int MAXIMUM_KEYWORD_LENGTH = 200;
    private static final Set<String> SUPPORTED_STATUSES = Set.of(
            "WAITING", "READY_FOR_PICKUP", "COMPLETED", "CANCELLED", "EXPIRED");
    private static final Set<String> SUPPORTED_SORT_ORDERS = Set.of("NEWEST", "ASC", "DESC");

    private final ReservationService reservationService = new ReservationService();

    /**
     * Hiển thị danh sách reservation theo từ khóa, trạng thái và thứ tự ưu tiên.
     *
     * @param request request chứa bộ lọc danh sách
     * @param response response dùng để forward sang JSP hoặc trả lỗi HTTP
     * @throws ServletException khi không thể forward request
     * @throws IOException khi không thể ghi response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
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

        String status = normalizeParameter(request.getParameter("status"));
        String keyword = normalizeParameter(request.getParameter("keyword"));
        String sortOrder = normalizeParameter(request.getParameter("order"))
                .toUpperCase(Locale.ROOT);
        if (sortOrder.isEmpty()) {
            sortOrder = "NEWEST";
        }
        if (!status.isEmpty() && !SUPPORTED_STATUSES.contains(status)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Trạng thái đặt trước không hợp lệ.");
            return;
        }
        if (!SUPPORTED_SORT_ORDERS.contains(sortOrder)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Cách sắp xếp đặt trước không hợp lệ.");
            return;
        }
        if (keyword.length() > MAXIMUM_KEYWORD_LENGTH) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Từ khóa tìm kiếm không được vượt quá 200 ký tự.");
            return;
        }

        int page;
        try {
            page = parsePositiveInteger(request.getParameter("page"), 1);
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Trang phải là số nguyên dương.");
            return;
        }

        try {
            reservationService.expireExpiredReadyReservations();
            int totalRecords = reservationService.countReservationsForManagement(
                    status, keyword);
            int totalPages = Math.max(1,
                    (int) Math.ceil((double) totalRecords / DEFAULT_PAGE_SIZE));
            page = Math.min(page, totalPages);
            List<ReservationRecord> list = reservationService.getReservationsForManagement(
                    status, keyword, sortOrder, page, DEFAULT_PAGE_SIZE);

            request.setAttribute("reservationList", list);
            request.setAttribute("totalRecords", totalRecords);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("currentPageNum", page);
            request.setAttribute("selectedStatus", status);
            request.setAttribute("keyword", keyword);
            request.setAttribute("sortOrder", sortOrder);
            request.setAttribute("isManagePageAttr", true);
            request.setAttribute("activePage", "reservation");
            request.setAttribute("pageTitle", "Quản lý đặt giữ chỗ – FPT Library");
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách reservation quản lý", exception);
            request.setAttribute("error", "Không thể tải danh sách đặt trước. Vui lòng thử lại.");
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/reservation-list.jsp").forward(request, response);
    }

    /**
     * Xử lý xác nhận sách đã về hoặc hủy reservation theo thao tác của nhân viên.
     *
     * @param request request chứa mã reservation và hành động
     * @param response response dùng để chuyển hướng theo Post/Redirect/Get
     * @throws ServletException khi servlet không thể xử lý request
     * @throws IOException khi không thể chuyển hướng
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
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
                    boolean success = reservationService.markReservationReady(
                            id, loggedUser.getUsername(), true);
                    if (success) {
                        session.setAttribute("successMsg",
                                "Sách đã về và được chuyển sang danh sách chờ giao sách.");
                    } else {
                        session.setAttribute("errorMsg", "Không thể xác nhận sách đã về. "
                                + "Bản sao hoặc yêu cầu đặt trước không còn hợp lệ.");
                    }
                } else if ("cancel".equals(action)) {
                    boolean success = reservationService.cancelReservationByStaff(id);
                    if (success) {
                        session.setAttribute("successMsg", "Đã hủy yêu cầu đặt trước thành công.");
                    } else {
                        session.setAttribute("errorMsg",
                                "Không thể hủy yêu cầu không còn hoạt động.");
                    }
                } else {
                    session.setAttribute("errorMsg", "Thao tác đặt trước không hợp lệ.");
                }
            }
        } catch (NumberFormatException exception) {
            session.setAttribute("errorMsg", "Mã yêu cầu đặt trước không hợp lệ.");
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật reservation", exception);
            session.setAttribute("errorMsg",
                    "Không thể cập nhật yêu cầu đặt trước. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + prefix + "/reservation/list");
    }

    /**
     * Chuẩn hóa tham số văn bản để các tầng sau nhận giá trị không null và đã bỏ khoảng trắng.
     *
     * @param value giá trị request cần chuẩn hóa
     * @return chuỗi đã trim hoặc chuỗi rỗng nếu tham số không tồn tại
     */
    private String normalizeParameter(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Đọc số nguyên dương, dùng giá trị mặc định khi tham số trống.
     *
     * @param value tham số cần đọc
     * @param defaultValue giá trị dùng khi tham số trống
     * @return số nguyên dương đã đọc hoặc giá trị mặc định
     * @throws NumberFormatException khi giá trị không phải số nguyên dương
     */
    private int parsePositiveInteger(String value, int defaultValue)
            throws NumberFormatException {
        String normalizedValue = normalizeParameter(value);
        if (normalizedValue.isEmpty()) {
            return defaultValue;
        }
        int parsedValue = Integer.parseInt(normalizedValue);
        if (parsedValue < 1) {
            throw new NumberFormatException("Giá trị phải lớn hơn 0.");
        }
        return parsedValue;
    }
}
