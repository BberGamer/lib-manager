/* Controller reader cho tạo, xem và hủy reservation. */
package controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.logging.*;
import model.User;
import service.ReservationService;

/**
 * Điều phối các endpoint reservation của chính reader đăng nhập.
 */
@WebServlet(name = "MyReservationServlet", urlPatterns = {"/reservation/create", "/reservation/my", "/reservation/cancel"})
public class MyReservationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MyReservationServlet.class.getName());
    private final ReservationService service = new ReservationService();

    /**
     * Hiển thị trang xác nhận hoặc danh sách cá nhân.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        try {
            if (request.getServletPath().endsWith("/create")) {
                int bookId = parseId(request, "bookId");
                
                // Check if user has unpaid fines
                if (new dao.FineDAO().searchByUser(user.getId(), "UNPAID", null).size() > 0) {
                    request.getSession().setAttribute("reservationErrorMessage", "Bạn phải thanh toán hết các khoản phạt trước đó mới được đặt trước sách.");
                    response.sendRedirect(request.getContextPath() + "/books");
                    return;
                }
                
                var info = service.getCreationInfo(user.getId(), bookId);
                if (info == null) {
                    request.getSession().setAttribute("reservationErrorMessage", "Không thể đặt trước: sách có thể đang khả dụng hoặc bạn đã có yêu cầu đang hoạt động.");
                    response.sendRedirect(request.getContextPath() + "/books");
                    return;
                }
                request.setAttribute("reservationInfo", info);
                request.setAttribute("activePage", "reservations");
                request.getRequestDispatcher("/WEB-INF/views/reader/reservation-confirm.jsp").forward(request, response);
            } else {
                request.setAttribute("reservationList", service.getMyReservations(user.getId()));
                request.setAttribute("activePage", "reservations");
                request.getRequestDispatcher("/WEB-INF/views/reader/my-reservations.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            response.sendError(400, "Mã dữ liệu không hợp lệ.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Không thể tải đặt trước cho userId=" + user.getId(), e);
            response.sendError(500, "Không thể tải thông tin đặt trước lúc này.");
        }
    }

    /**
     * Xử lý tạo/hủy rồi redirect theo PRG.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        HttpSession session = request.getSession();
        try {
            boolean success;
            if (request.getServletPath().endsWith("/cancel")) {
                success = service.cancelReservation(parseId(request, "reservationId"), user.getId());
                session.setAttribute(success ? "reservationSuccessMessage" : "reservationErrorMessage", success ? "Hủy đặt trước thành công." : "Không thể hủy yêu cầu đặt trước này.");
            } else {
                int bookId = parseId(request, "bookId");
                if (new dao.FineDAO().searchByUser(user.getId(), "UNPAID", null).size() > 0) {
                    session.setAttribute("reservationErrorMessage", "Bạn phải thanh toán hết các khoản phạt trước đó mới được đặt trước sách.");
                    success = false;
                } else {
                    success = service.createReservation(user.getId(), bookId);
                    session.setAttribute(success ? "reservationSuccessMessage" : "reservationErrorMessage", success ? "Đặt trước sách thành công." : "Không thể đặt trước. Sách có thể đã khả dụng hoặc bạn đã có yêu cầu đang hoạt động.");
                }
            }
        } catch (NumberFormatException e) {
            session.setAttribute("reservationErrorMessage", "Mã dữ liệu không hợp lệ.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi thao tác đặt trước của userId=" + user.getId(), e);
            session.setAttribute("reservationErrorMessage", "Không thể xử lý yêu cầu đặt trước lúc này.");
        }
        response.sendRedirect(request.getContextPath() + "/reservation/my");
    }

    /**
     * Lấy user session hoặc redirect login.
     */
    private User requireUser(HttpServletRequest r, HttpServletResponse p) throws IOException {
        HttpSession s = r.getSession(false);
        if (s == null || !(s.getAttribute("loggedUser") instanceof User)) {
            p.sendRedirect(r.getContextPath() + "/login");
            return null;
        }
        User u = (User) s.getAttribute("loggedUser");
        if (!u.isReader()) {
            p.sendError(403);
            return null;
        }
        return u;
    }

    /**
     * Parse ID dương.
     */
    private int parseId(HttpServletRequest r, String n) {
        int v = Integer.parseInt(r.getParameter(n));
        if (v <= 0) {
            throw new NumberFormatException();
        }
        return v;
    }
}
