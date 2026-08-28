/* Controller reader tiếp nhận ngày mong muốn và điều phối thao tác đặt trước sách. */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import service.ReservationService;
import service.ReservationValidationException;

/**
 * Điều phối form tạo, API tính ngày dự kiến, danh sách và thao tác hủy reservation của reader.
 */
@WebServlet(name = "MyReservationServlet", urlPatterns = {
    "/reservation/create", "/reservation/estimate", "/reservation/my", "/reservation/cancel"
})
public class MyReservationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MyReservationServlet.class.getName());
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ReservationService reservationService = new ReservationService();

    /**
     * Hiển thị form, danh sách cá nhân hoặc trả ngày dự kiến cho thay đổi trên form.
     *
     * @param request yêu cầu HTTP của reader
     * @param response phản hồi HTML hoặc JSON
     * @throws ServletException khi forward JSP thất bại
     * @throws IOException khi không thể ghi phản hồi
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        try {
            String path = request.getServletPath();
            if (path.endsWith("/estimate")) {
                writeEstimate(request, response, user);
            } else if (path.endsWith("/create")) {
                showCreationForm(request, response, user);
            } else {
                request.setAttribute("reservationList",
                        reservationService.getMyReservations(user.getId()));
                request.setAttribute("activePage", "reservations");
                request.getRequestDispatcher("/WEB-INF/views/reader/my-reservations.jsp")
                        .forward(request, response);
            }
        } catch (ReservationValidationException exception) {
            request.getSession().setAttribute("reservationErrorMessage", exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/books");
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã dữ liệu không hợp lệ.");
        } catch (DateTimeParseException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ngày nhận sách không hợp lệ.");
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE,
                    "Không thể tải đặt trước cho userId=" + user.getId(), exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tải thông tin đặt trước lúc này.");
        }
    }

    /**
     * Xử lý tạo hoặc hủy rồi redirect theo Post/Redirect/Get.
     *
     * @param request yêu cầu POST của reader
     * @param response phản hồi redirect
     * @throws IOException khi không thể redirect
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        HttpSession session = request.getSession();
        try {
            if (request.getServletPath().endsWith("/cancel")) {
                boolean success = reservationService.cancelReservation(
                        parseId(request, "reservationId"), user.getId());
                session.setAttribute(success
                        ? "reservationSuccessMessage" : "reservationErrorMessage",
                        success ? "Hủy đặt trước thành công."
                                : "Không thể hủy yêu cầu đặt trước này.");
            } else {
                int bookId = parseId(request, "bookId");
                LocalDate requestedPickupDate = parsePickupDate(request);
                ReservationService.CreationResult result = reservationService.createReservation(
                        user.getId(), bookId, requestedPickupDate);
                session.setAttribute("reservationSuccessMessage",
                        "Đặt trước thành công từ "
                                + DISPLAY_DATE.format(result.getExpectedPickupDate())
                                + " đến " + DISPLAY_DATE.format(result.getExpectedEndDate())
                                + " (không bao gồm ngày kết thúc).");
            }
        } catch (ReservationValidationException exception) {
            session.setAttribute("reservationErrorMessage", exception.getMessage());
        } catch (NumberFormatException exception) {
            session.setAttribute("reservationErrorMessage", "Mã dữ liệu không hợp lệ.");
        } catch (DateTimeParseException exception) {
            session.setAttribute("reservationErrorMessage", "Ngày nhận sách không hợp lệ.");
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE,
                    "Lỗi thao tác đặt trước của userId=" + user.getId(), exception);
            session.setAttribute("reservationErrorMessage",
                    "Không thể xử lý yêu cầu đặt trước lúc này.");
        }
        response.sendRedirect(request.getContextPath() + "/reservation/my");
    }

    /**
     * Chuẩn bị form xác nhận cùng khoảng ngày được phép chọn.
     *
     * @param request yêu cầu hiện tại
     * @param response phản hồi HTML
     * @param user reader đã xác thực
     * @throws Exception khi không thể tải hoặc forward dữ liệu
     */
    private void showCreationForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws Exception {
        int bookId = parseId(request, "bookId");
        ReservationService.CreationInfo info = reservationService.getCreationInfo(
                user.getId(), bookId);
        request.setAttribute("reservationInfo", info);
        request.setAttribute("activePage", "reservations");
        request.getRequestDispatcher("/WEB-INF/views/reader/reservation-confirm.jsp")
                .forward(request, response);
    }

    /**
     * Trả JSON nhỏ chứa ngày dự kiến mới để JavaScript cập nhật ngay trên form.
     *
     * @param request yêu cầu có bookId và requestedPickupDate
     * @param response phản hồi JSON
     * @param user reader đã xác thực
     * @throws IOException khi không thể ghi JSON
     */
    private void writeEstimate(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            int bookId = parseId(request, "bookId");
            LocalDate requestedPickupDate = parsePickupDate(request);
            ReservationService.PickupEstimate estimate = reservationService.estimatePickupDate(
                    user.getId(), bookId, requestedPickupDate);
            response.getWriter().write("{\"earliestAvailableDate\":\""
                    + estimate.getEarliestAvailableDate()
                    + "\",\"expectedPickupDate\":\""
                    + estimate.getExpectedPickupDate()
                    + "\",\"expectedEndDate\":\""
                    + estimate.getExpectedEndDate()
                    + "\",\"availableCapacity\":"
                    + estimate.getAvailableCapacity() + "}");
        } catch (ReservationValidationException | NumberFormatException
                | DateTimeParseException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\""
                    + escapeJson(exception.getMessage()) + "\"}");
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE,
                    "Không thể tính ngày dự kiến cho userId=" + user.getId(), exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                    "{\"message\":\"Không thể tính ngày dự kiến lúc này.\"}");
        }
    }

    /**
     * Lấy reader từ session hoặc kết thúc yêu cầu bằng redirect/HTTP 403.
     *
     * @param request yêu cầu hiện tại
     * @param response phản hồi dùng khi chưa xác thực hoặc sai vai trò
     * @return reader hợp lệ, hoặc {@code null} khi đã kết thúc phản hồi
     * @throws IOException khi không thể redirect hoặc gửi lỗi
     */
    private User requireUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("loggedUser") instanceof User)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        User user = (User) session.getAttribute("loggedUser");
        if (!user.isReader()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }

    /**
     * Parse một tham số ID dương.
     *
     * @param request yêu cầu chứa tham số
     * @param parameterName tên tham số
     * @return ID dương
     * @throws NumberFormatException khi thiếu, sai định dạng hoặc không dương
     */
    private int parseId(HttpServletRequest request, String parameterName) {
        int value = Integer.parseInt(request.getParameter(parameterName));
        if (value <= 0) {
            throw new NumberFormatException();
        }
        return value;
    }

    /**
     * Parse ngày ISO từ trường chọn ngày nhận.
     *
     * @param request yêu cầu chứa requestedPickupDate
     * @return ngày đã parse
     * @throws DateTimeParseException khi giá trị trống hoặc sai định dạng
     */
    private LocalDate parsePickupDate(HttpServletRequest request) {
        String value = request.getParameter("requestedPickupDate");
        if (value == null || value.trim().isEmpty()) {
            throw new DateTimeParseException("Vui lòng chọn ngày nhận sách.", "", 0);
        }
        return LocalDate.parse(value.trim());
    }

    /**
     * Escape các ký tự có ý nghĩa trong chuỗi JSON trả về cho trình duyệt.
     *
     * @param value chuỗi thông báo cần mã hóa
     * @return chuỗi an toàn trong JSON string
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "Yêu cầu không hợp lệ.";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
