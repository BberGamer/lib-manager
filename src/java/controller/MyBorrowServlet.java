/*
 * Controller HTTP hiển thị và xử lý gia hạn tại trang mượn sách cá nhân của độc giả.
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
import model.User;
import service.BorrowService;
import service.BorrowService.BorrowPageData;

/**
 * Điều phối GET `/borrow/my` và POST `/borrow/my/renew` cho người dùng vai trò READER.
 */
@WebServlet(name = "MyBorrowServlet", urlPatterns = {
    "/borrow/my", "/borrow/my/renew", "/borrow/create", "/borrow/cancel"
})
public class MyBorrowServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MyBorrowServlet.class.getName());
    private final BorrowService borrowService = new BorrowService();

    /**
     * Xác thực độc giả, tải dữ liệu mượn và chuyển tiếp sang JSP.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @throws ServletException khi không thể render trang
     * @throws IOException khi chuyển hướng hoặc chuyển tiếp thất bại
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        User reader = requireReader(request, response);
        if (reader == null) {
            return;
        }

        try {
            BorrowPageData pageData = borrowService.getBorrowPage(reader.getId());
            request.setAttribute("borrowPage", pageData);
            request.setAttribute("maximumActiveBorrows", BorrowService.MAXIMUM_ACTIVE_BORROWS);
            request.setAttribute("maximumRenewals", BorrowService.MAXIMUM_RENEWALS);
            request.setAttribute("activePage", "borrows");
            request.getRequestDispatcher("/WEB-INF/views/reader/my-borrows.jsp").forward(request, response);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Không thể tải trang mượn sách của userId=" + reader.getId(), exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tải thông tin mượn sách lúc này.");
        }
    }

    /**
     * Kiểm tra dữ liệu đầu vào trước khi yêu cầu service gia hạn, sau đó áp dụng PRG.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @throws IOException khi chuyển hướng hoặc gửi lỗi thất bại
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        User reader = requireReader(request, response);
        if (reader == null) {
            return;
        }
        HttpSession session = request.getSession(false);

        try {
            String path = request.getServletPath();
            boolean success;
            String successMessage;
            String errorMessage;
            if (path.endsWith("/create")) {
                success = borrowService.createBorrowRequest(reader.getId(), parsePositiveId(request, "bookId"));
                successMessage = "Đã tạo yêu cầu mượn sách. Vui lòng đến thư viện nhận sách trong 24 giờ.";
                errorMessage = "Không thể tạo yêu cầu. Sách có thể đã hết hoặc bạn đã có lượt mượn đang hoạt động.";
            } else if (path.endsWith("/cancel")) {
                success = borrowService.cancelBorrowRequest(parsePositiveId(request, "borrowId"), reader.getId());
                successMessage = "Đã hủy yêu cầu mượn sách và giải phóng bản sao.";
                errorMessage = "Không thể hủy yêu cầu. Chỉ yêu cầu đang chờ nhận mới được hủy.";
            } else {
                success = borrowService.renewBorrow(parsePositiveId(request, "borrowRecordId"), reader.getId());
                successMessage = "Gia hạn sách thành công. Hạn trả đã được cộng thêm 7 ngày.";
                errorMessage = "Không thể gia hạn. Sách có thể đã quá hạn hoặc đã đạt giới hạn gia hạn.";
            }
            session.setAttribute(success ? "borrowSuccessMessage" : "borrowErrorMessage",
                    success ? successMessage : errorMessage);
        } catch (NumberFormatException exception) {
            session.setAttribute("borrowErrorMessage", "Mã lượt mượn không hợp lệ.");
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Không thể gia hạn sách cho userId=" + reader.getId(), exception);
            session.setAttribute("borrowErrorMessage", "Không thể gia hạn sách lúc này.");
        }
        response.sendRedirect(request.getContextPath() + "/borrow/my");
    }

    /** Parse mã dương từ tham số bắt buộc. */
    private int parsePositiveId(HttpServletRequest request, String parameterName) {
        int value = Integer.parseInt(request.getParameter(parameterName));
        if (value <= 0) throw new NumberFormatException("Mã phải lớn hơn 0");
        return value;
    }

    /**
     * Yêu cầu phiên đăng nhập của độc giả và trả phản hồi phù hợp khi không đủ quyền.
     *
     * @param request yêu cầu HTTP hiện tại
     * @param response phản hồi HTTP hiện tại
     * @return độc giả đăng nhập hoặc {@code null} nếu đã gửi chuyển hướng/lỗi
     * @throws IOException khi không thể gửi phản hồi
     */
    private User requireReader(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
}
