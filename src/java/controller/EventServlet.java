/**
 * Servlet điều hướng và xử lý các yêu cầu HTTP liên quan đến Sự kiện.
 * Thuộc tầng Controller.
 *
 * Mẫu thiết kế MVC:
 * - Tiếp nhận tham số từ HTTP Request.
 * - Gọi tầng Service (EventService) để thực hiện xử lý nghiệp vụ và kiểm tra hợp lệ.
 * - Đặt dữ liệu vào request attributes và chuyển tiếp (forward) đến JSP hoặc chuyển hướng (redirect).
 * - Cho phép người dùng chưa đăng nhập (khách) và độc giả (READER) vào xem sự kiện.
 */
package controller;

import model.Event;
import model.User;
import service.EventService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller quản lý luồng điều hướng sự kiện cho cả Khách, Reader, Librarian và Admin.
 */
@WebServlet(name = "EventServlet", urlPatterns = {"/events", "/admin/events", "/librarian/events"})
public class EventServlet extends HttpServlet {

    private final EventService eventService = new EventService();

    /**
     * Xử lý yêu cầu GET: hiển thị danh sách sự kiện, tìm kiếm, lọc, sắp xếp và phân trang.
     * Cho phép cả người dùng chưa đăng nhập (guest) xem danh sách và chi tiết sự kiện.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        User loggedUser = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        // 1. Đọc các tham số tìm kiếm, lọc, sắp xếp, phân trang từ URL
        String q = request.getParameter("q");
        String statusFilter = request.getParameter("status");
        String sortField = request.getParameter("sort");
        String sortOrder = request.getParameter("order");

        if (sortField == null || sortField.trim().isEmpty()) {
            sortField = "start_time";
            
            
            
            // sortOrder = "DESC";
        }
        if (!"DESC".equalsIgnoreCase(sortOrder)) {
            sortOrder = "ASC";
        }

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageStr.trim());
            } catch (NumberFormatException ignored) {
                page = 1;
            }
        }

        // 2. Thực hiện truy vấn qua EventService
        try {
            EventService.SearchResult result = eventService.search(q, statusFilter, sortField, sortOrder, page);

            request.setAttribute("events", result.events);
            request.setAttribute("totalRecords", result.totalRecords);
            request.setAttribute("totalPages", result.totalPages);
            request.setAttribute("currentPageNum", result.currentPage);

            request.setAttribute("q", q != null ? q : "");
            request.setAttribute("statusFilter", statusFilter != null ? statusFilter : "");
            request.setAttribute("sortField", sortField);
            request.setAttribute("sortBy", sortField);
            request.setAttribute("sortOrder", sortOrder);

        } catch (Exception e) {
            request.setAttribute("errorMsg", "Không thể tải danh sách sự kiện: " + e.getMessage());
        }

        // 3. Lấy thông báo Flash từ session (nếu có)
        if (session != null) {
            String successMsg = (String) session.getAttribute("successMsg");
            if (successMsg != null) {
                request.setAttribute("successMsg", successMsg);
                session.removeAttribute("successMsg");
            }
            String errorMsg = (String) session.getAttribute("errorMsg");
            if (errorMsg != null) {
                request.setAttribute("errorMsg", errorMsg);
                session.removeAttribute("errorMsg");
            }
        }

        // 4. Cấu hình layout và phân quyền giao diện:
        // Nếu là Admin hoặc Librarian -> hiển thị giao diện Quản trị có Sidebar (isManagePageAttr = true)
        boolean isManagePage = (loggedUser != null && loggedUser.isAdminOrLibrarian());
        String path = request.getServletPath();

        request.setAttribute("isManagePageAttr", isManagePage);
        request.setAttribute("pageTitle", "Quản Lý Sự Kiện - FPT Library");
        request.setAttribute("activePage", "events");
        request.setAttribute("currentPath", path != null ? path : "/events");

        // Forward đến trang JSP duy nhất hiển thị danh sách và modal
        request.getRequestDispatcher("/WEB-INF/views/events.jsp").forward(request, response);
    }

    /**
     * Xử lý yêu cầu POST: Thêm mới, Chỉnh sửa, Xóa sự kiện (Chỉ dành cho LIBRARIAN và ADMIN).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Kiểm tra xác thực đăng nhập
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User loggedUser = (User) session.getAttribute("loggedUser");

        // 2. Kiểm tra phân quyền (CHỈ LIBRARIAN và ADMIN mới được phép thực hiện thay đổi dữ liệu)
        if (!loggedUser.isAdminOrLibrarian()) {
            session.setAttribute("errorMsg", "Bạn không có quyền thực hiện thao tác này!");
            response.sendRedirect(request.getContextPath() + "/events");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            if ("create".equalsIgnoreCase(action)) {
                Event event = new Event();
                event.setTitle(request.getParameter("title"));
                event.setDescription(request.getParameter("description"));
                event.setStartTime(parseDateTime(request.getParameter("startTime")));
                event.setEndTime(parseDateTime(request.getParameter("endTime")));
                event.setStatus(request.getParameter("status"));
                event.setCreatedBy(loggedUser.getUsername());

                eventService.createEvent(event);
                session.setAttribute("successMsg", "Thêm sự kiện mới thành công!");

            } else if ("update".equalsIgnoreCase(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Event event = eventService.getEventById(id);
                event.setTitle(request.getParameter("title"));
                event.setDescription(request.getParameter("description"));
                event.setStartTime(parseDateTime(request.getParameter("startTime")));
                event.setEndTime(parseDateTime(request.getParameter("endTime")));
                event.setStatus(request.getParameter("status"));
                event.setUpdatedBy(loggedUser.getUsername());

                eventService.updateEvent(event);
                session.setAttribute("successMsg", "Cập nhật sự kiện thành công!");

            } else if ("delete".equalsIgnoreCase(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                eventService.deleteEvent(id, loggedUser.getUsername());
                session.setAttribute("successMsg", "Xóa sự kiện thành công!");

            } else {
                session.setAttribute("errorMsg", "Hành động không hợp lệ.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            session.setAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Đã xảy ra lỗi: " + e.getMessage());
        }

        // Chuyển hướng lại trang hiện tại (Post/Redirect/Get)
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/events")) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(request.getContextPath() + "/events");
        }
    }

    /**
     * Chuyển đổi chuỗi ngày giờ từ input datetime-local sang LocalDateTime.
     *
     * @param dateTimeStr Chuỗi định dạng "yyyy-MM-ddTHH:mm" hoặc "yyyy-MM-ddTHH:mm:ss"
     * @return Đối tượng LocalDateTime hoặc null nếu chuỗi rỗng
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        String str = dateTimeStr.trim();
        try {
            if (str.length() == 16) {
                return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            } else if (str.length() == 19) {
                return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } else {
                return LocalDateTime.parse(str);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng ngày giờ không hợp lệ.");
        }
    }
}
