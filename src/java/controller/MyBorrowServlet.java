/*
 * Controller HTTP hiển thị và xử lý thao tác tại trang mượn sách cá nhân của độc giả.
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
import model.BorrowRenewalResult;
import model.User;
import service.BorrowService;
import service.BorrowService.BorrowPageData;

/**
 * Điều phối trang mượn cá nhân, gồm gia hạn, hủy giữ và báo mất sách của Reader.
 */
@WebServlet(name = "MyBorrowServlet", urlPatterns = {
    "/borrow/my", "/borrow/my/renew", "/borrow/my/report-lost",
    "/borrow/create", "/borrow/cancel"
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
            
            request.setAttribute("reviewedBorrowIds",
                    borrowService.getReviewedBorrowIds(reader.getId()));

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
                int userId = reader.getId();
                if (borrowService.hasUnpaidFines(userId)) {
                    success = false;
                    successMessage = "";
                    errorMessage = "Bạn phải thanh toán hết các khoản phạt trước đó mới được mượn sách mới.";
                } else if (borrowService.getActiveBorrowCount(userId)
                        >= BorrowService.MAXIMUM_ACTIVE_BORROWS) {
                    success = false;
                    successMessage = "";
                    errorMessage = "Không thể mượn thêm sách: Bạn đã đạt giới hạn tối đa 3 lượt mượn hoạt động.";
                } else {
                    success = borrowService.createBorrowRequest(userId, parsePositiveId(request, "bookId"));
                    successMessage = "Đã tạo yêu cầu mượn sách. Vui lòng đến thư viện nhận sách trong 24 giờ.";
                    errorMessage = "Không thể tạo yêu cầu. Sách có thể đã hết hoặc bạn đã có lượt mượn đang hoạt động.";
                }
            } else if (path.endsWith("/cancel")) {
                success = borrowService.cancelBorrowRequest(parsePositiveId(request, "borrowId"), reader.getId());
                successMessage = "Đã hủy yêu cầu mượn sách và giải phóng bản sao.";
                errorMessage = "Không thể hủy yêu cầu. Chỉ yêu cầu đang chờ nhận mới được hủy.";
            } else if (path.endsWith("/report-lost")) {
                success = borrowService.reportLostBorrow(
                        parsePositiveId(request, "borrowRecordId"),
                        reader.getId(), reader.getUsername());
                successMessage = "Đã ghi nhận báo mất, cập nhật tồn kho và tạo vé phạt bằng 100% giá sách.";
                errorMessage = "Không thể báo mất. Lượt mượn không thuộc tài khoản của bạn "
                        + "hoặc không còn ở trạng thái đang mượn.";
            } else {
                BorrowRenewalResult renewalResult = borrowService.renewBorrow(
                        parsePositiveId(request, "borrowRecordId"), reader.getId());
                success = renewalResult == BorrowRenewalResult.SUCCESS;
                successMessage = "Gia hạn sách thành công. Hạn trả đã được cộng thêm 7 ngày.";
                errorMessage = renewalResult == BorrowRenewalResult.BLOCKED_BY_RESERVATION
                        ? "Không thể gia hạn vì khoảng 7 ngày mới trùng slot đặt trước và không còn bản sao khác."
                        : "Không thể gia hạn. Sách có thể đã quá hạn hoặc đã đạt giới hạn gia hạn.";
            }
            session.setAttribute(success ? "borrowSuccessMessage" : "borrowErrorMessage",
                    success ? successMessage : errorMessage);
        } catch (NumberFormatException exception) {
            session.setAttribute("borrowErrorMessage", "Mã lượt mượn không hợp lệ.");
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE,
                    "Không thể xử lý thao tác mượn sách cho userId=" + reader.getId(), exception);
            session.setAttribute("borrowErrorMessage",
                    "Không thể xử lý thao tác mượn sách lúc này.");
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
