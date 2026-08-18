/*
 * Servlet xử lý các yêu cầu liên quan đến đánh giá sách của độc giả.
 * Thuộc tầng Controller.
 * Chịu trách nhiệm tiếp nhận yêu cầu thêm, sửa, xóa đánh giá sách.
 */
package controller;

import dao.BookReviewDAO;
import model.BookReview;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Điều phối các yêu cầu POST tới `/book-review` để thực hiện thêm, sửa, xóa đánh giá sách.
 */
@WebServlet(name = "BookReviewServlet", urlPatterns = {"/book-review"})
public class BookReviewServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BookReviewServlet.class.getName());
    private final BookReviewDAO reviewDAO = new BookReviewDAO();

    /**
     * Không hỗ trợ phương thức GET trực tiếp cho địa chỉ này, chuyển hướng về trang chủ.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @throws ServletException nếu có lỗi servlet
     * @throws IOException nếu có lỗi I/O
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/books");
    }

    /**
     * Xử lý các yêu cầu POST để thêm, sửa hoặc xóa đánh giá.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @throws ServletException nếu có lỗi servlet
     * @throws IOException nếu có lỗi I/O
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        User loggedUser = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        String bookIdStr = request.getParameter("bookId");
        int bookId = 0;
        try {
            if (bookIdStr != null) {
                bookId = Integer.parseInt(bookIdStr);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "bookId không hợp lệ: " + bookIdStr);
        }

        if (bookId <= 0) {
            response.sendRedirect(request.getContextPath() + "/books");
            return;
        }

        try {
            if ("add".equals(action)) {
                processAdd(request, response, loggedUser, bookId);
            } else if ("edit".equals(action)) {
                processEdit(request, response, loggedUser, bookId);
            } else if ("delete".equals(action)) {
                processDelete(request, response, loggedUser, bookId);
            } else {
                response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xử lý đánh giá sách: " + e.getMessage(), e);
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=exception");
        }
    }

    /**
     * Xử lý việc tạo mới đánh giá sách.
     */
    private void processAdd(HttpServletRequest request, HttpServletResponse response, User loggedUser, int bookId)
            throws Exception {
        if (!loggedUser.isReader()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ độc giả mới có quyền đánh giá sách.");
            return;
        }

        String ratingStr = request.getParameter("rating");
        String comment = request.getParameter("comment");
        String borrowIdStr = request.getParameter("borrowId");

        int rating = 0;
        Integer borrowId = null;
        try {
            rating = Integer.parseInt(ratingStr);
            if (borrowIdStr != null && !borrowIdStr.trim().isEmpty()) {
                borrowId = Integer.parseInt(borrowIdStr);
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=invalid_input");
            return;
        }

        if (rating < 1 || rating > 5) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=invalid_rating");
            return;
        }

        // Nếu không truyền borrowId từ form, tự động tìm lượt mượn gần nhất chưa đánh giá
        if (borrowId == null) {
            borrowId = reviewDAO.getUnreviewedBorrowId(bookId, loggedUser.getId());
        }

        if (borrowId == null) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=not_borrowed");
            return;
        }

        // Tạo đối tượng đánh giá mới
        BookReview review = new BookReview();
        review.setBookId(bookId);
        review.setUserId(loggedUser.getId());
        review.setRating(rating);
        review.setComment(comment);
        review.setBorrowId(borrowId);

        boolean success = reviewDAO.insert(review);
        if (success) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&success=review_added");
        } else {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=review_failed");
        }
    }

    /**
     * Xử lý việc cập nhật/sửa đánh giá sách.
     */
    private void processEdit(HttpServletRequest request, HttpServletResponse response, User loggedUser, int bookId)
            throws Exception {
        String reviewIdStr = request.getParameter("reviewId");
        String ratingStr = request.getParameter("rating");
        String comment = request.getParameter("comment");

        int reviewId = 0;
        int rating = 0;
        try {
            reviewId = Integer.parseInt(reviewIdStr);
            rating = Integer.parseInt(ratingStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=invalid_input");
            return;
        }

        if (rating < 1 || rating > 5) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=invalid_rating");
            return;
        }

        BookReview existing = reviewDAO.findById(reviewId);
        if (existing == null) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=not_found");
            return;
        }

        // Bảo mật: Chỉ tác giả mới được sửa
        if (existing.getUserId() != loggedUser.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền sửa đánh giá của người khác.");
            return;
        }

        // Quy tắc: Chỉ sửa trong vòng 7 ngày kể từ lúc tạo
        long diff = System.currentTimeMillis() - existing.getCreatedAt().getTime();
        if (diff > 7L * 24 * 60 * 60 * 1000) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=edit_expired");
            return;
        }

        existing.setRating(rating);
        existing.setComment(comment);

        boolean success = reviewDAO.update(existing);
        if (success) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&success=review_updated");
        } else {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=update_failed");
        }
    }

    /**
     * Xử lý việc xóa đánh giá sách.
     */
    private void processDelete(HttpServletRequest request, HttpServletResponse response, User loggedUser, int bookId)
            throws Exception {
        String reviewIdStr = request.getParameter("reviewId");
        int reviewId = 0;
        try {
            reviewId = Integer.parseInt(reviewIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=invalid_input");
            return;
        }

        BookReview existing = reviewDAO.findById(reviewId);
        if (existing == null) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=not_found");
            return;
        }

        // Bảo mật: Chỉ tác giả hoặc Admin mới được xóa
        if (existing.getUserId() != loggedUser.getId() && !loggedUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xóa đánh giá này.");
            return;
        }

        boolean success = reviewDAO.delete(reviewId);
        if (success) {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&success=review_deleted");
        } else {
            response.sendRedirect(request.getContextPath() + "/book/detail?id=" + bookId + "&error=delete_failed");
        }
    }
}
