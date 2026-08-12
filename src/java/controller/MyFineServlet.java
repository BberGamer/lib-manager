/*
 * Controller HTTP hiển thị danh sách và chi tiết khoản phạt của độc giả đăng nhập.
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Fine;
import model.User;
import service.FineService;

/**
 * Điều phối GET `/fine/my` và `/fine/detail`, luôn lấy user từ session và kiểm tra ownership.
 */
@WebServlet(name = "MyFineServlet", urlPatterns = {"/fine/my", "/fine/detail"})
public class MyFineServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MyFineServlet.class.getName());
    private final FineService fineService = new FineService();

    /**
     * Xác thực user rồi render danh sách hoặc chi tiết theo servlet path.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @throws ServletException khi forward thất bại
     * @throws IOException khi gửi phản hồi thất bại
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        User user = requireUser(request, response);
        if (user == null) return;
        try {
            if (request.getServletPath().endsWith("/detail")) {
                showDetail(request, response, user);
            } else {
                showList(request, response, user);
            }
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Không thể tải khoản phạt cho userId=" + user.getId(), exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tải thông tin khoản phạt lúc này.");
        }
    }

    /**
     * Chuẩn hóa filter/search và chuyển dữ liệu danh sách sang JSP.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @param user người dùng đăng nhập
     * @throws Exception khi tải hoặc forward dữ liệu thất bại
     */
    private void showList(HttpServletRequest request, HttpServletResponse response, User user)
            throws Exception {
        String status = fineService.normalizeStatus(request.getParameter("status"));
        String keyword = request.getParameter("keyword");
        request.setAttribute("finePage", fineService.getMyFines(user.getId(), status, keyword));
        request.setAttribute("selectedStatus", status == null ? "ALL" : status);
        request.setAttribute("keyword", keyword == null ? "" : keyword.trim());
        request.setAttribute("activePage", "fines");
        request.getRequestDispatcher("/WEB-INF/views/reader/my-fines.jsp").forward(request, response);
    }

    /**
     * Parse mã fine và chỉ render bản ghi thuộc chính người dùng.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @param user người dùng đăng nhập
     * @throws Exception khi tải hoặc forward dữ liệu thất bại
     */
    private void showDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws Exception {
        int fineId;
        try {
            fineId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã khoản phạt không hợp lệ.");
            return;
        }
        Fine fine = fineService.getOwnedFine(fineId, user.getId());
        if (fine == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("fine", fine);
        request.setAttribute("activePage", "fines");
        request.getRequestDispatcher("/WEB-INF/views/reader/fine-detail.jsp").forward(request, response);
    }

    /**
     * Yêu cầu session đăng nhập; tài khoản quản trị vẫn chỉ truy cập dữ liệu gắn với chính ID đó.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @return user đăng nhập hoặc {@code null} nếu đã redirect
     * @throws IOException khi redirect thất bại
     */
    private User requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("loggedUser") instanceof User)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        return (User) session.getAttribute("loggedUser");
    }
}
