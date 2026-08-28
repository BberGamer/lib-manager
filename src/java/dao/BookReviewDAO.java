/*
 * Lớp DAO quản lý các thao tác lưu trữ dữ liệu đánh giá sách (BookReview).
 * Thuộc tầng DAO (Data Access Object).
 * Chịu trách nhiệm tương tác với cơ sở dữ liệu để thêm, sửa, xóa và truy vấn các đánh giá sách.
 */
package dao;

import model.BookReview;
import model.User;
import utils.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu đối tượng đánh giá sách {@link BookReview}.
 * Cung cấp các phương thức làm việc với CSDL để lấy danh sách đánh giá,
 * tạo mới, cập nhật, xóa và kiểm tra điều kiện mượn sách trước khi cho phép độc giả đánh giá.
 */
public class BookReviewDAO {

    /**
     * Lấy danh sách tất cả các đánh giá của một đầu sách dựa vào ID sách,
     * kèm theo thông tin người đánh giá và ngày mượn tương ứng.
     *
     * @param bookId mã số của đầu sách cần lấy đánh giá
     * @return danh sách các đối tượng {@link BookReview}
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public List<BookReview> getReviewsByBookId(int bookId) throws Exception {
        return getReviewsByBookId(bookId, 1, Integer.MAX_VALUE);
    }

    /**
     * Lấy danh sách các đánh giá sách theo trang (LIMIT - OFFSET).
     *
     * @param bookId mã số của đầu sách
     * @param pageNum trang hiện tại (từ 1 trở lên)
     * @param pageSize số bản ghi hiển thị tối đa trên một trang
     * @return danh sách các đối tượng {@link BookReview} của trang hiện tại
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public List<BookReview> getReviewsByBookId(int bookId, int pageNum, int pageSize) throws Exception {
        List<BookReview> list = new ArrayList<>();
        int offset = Math.max(0, (pageNum - 1) * pageSize);
        String sql = "SELECT br.id, br.book_id, br.user_id, br.rating, br.comment, br.created_at, br.updated_at, br.borrow_id, " +
                     "u.full_name, u.student_id, " +
                     "bor.borrow_date " +
                     "FROM book_reviews br " +
                     "INNER JOIN users u ON br.user_id = u.id " +
                     "LEFT JOIN borrow_records bor ON br.borrow_id = bor.id " +
                     "WHERE br.book_id = ? " +
                     "ORDER BY br.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, pageSize);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookReview br = new BookReview();
                    br.setId(rs.getInt("id"));
                    br.setBookId(rs.getInt("book_id"));
                    br.setUserId(rs.getInt("user_id"));
                    br.setRating(rs.getInt("rating"));
                    br.setComment(rs.getString("comment"));
                    br.setCreatedAt(rs.getTimestamp("created_at"));
                    br.setUpdatedAt(rs.getTimestamp("updated_at"));

                    br.setUserFullName(rs.getString("full_name"));
                    br.setUserStudentId(rs.getString("student_id"));

                    int borrowId = rs.getInt("borrow_id");
                    if (!rs.wasNull()) {
                        br.setBorrowId(borrowId);
                    }
                    Date bDate = rs.getDate("borrow_date");
                    if (bDate != null) {
                        br.setBorrowDate(bDate.toLocalDate());
                    }

                    list.add(br);
                }
            }
        }
        return list;
    }

    /**
     * Đếm tổng số lượng đánh giá của một đầu sách dựa vào ID sách.
     *
     * @param bookId mã số của đầu sách
     * @return tổng số bản ghi đánh giá
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public int countReviewsByBookId(int bookId) throws Exception {
        String sql = "SELECT COUNT(*) FROM book_reviews WHERE book_id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Thêm một đánh giá sách mới vào cơ sở dữ liệu.
     *
     * @param review đối tượng {@link BookReview} chứa thông tin đánh giá cần tạo
     * @return true nếu thêm thành công, false nếu thất bại
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public boolean insert(BookReview review) throws Exception {
        String sql = "INSERT INTO book_reviews (book_id, user_id, rating, comment, borrow_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, review.getBookId());
            ps.setInt(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            if (review.getBorrowId() != null && review.getBorrowId() > 0) {
                ps.setInt(5, review.getBorrowId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Tìm kiếm một đánh giá sách theo ID đánh giá.
     *
     * @param id mã số ID của bản ghi đánh giá
     * @return đối tượng {@link BookReview} nếu tìm thấy, hoặc null nếu không tìm thấy
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public BookReview findById(int id) throws Exception {
        String sql = "SELECT id, book_id, user_id, rating, comment, created_at, updated_at, borrow_id FROM book_reviews WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BookReview br = new BookReview();
                    br.setId(rs.getInt("id"));
                    br.setBookId(rs.getInt("book_id"));
                    br.setUserId(rs.getInt("user_id"));
                    br.setRating(rs.getInt("rating"));
                    br.setComment(rs.getString("comment"));
                    br.setCreatedAt(rs.getTimestamp("created_at"));
                    br.setUpdatedAt(rs.getTimestamp("updated_at"));
                    int borrowId = rs.getInt("borrow_id");
                    if (!rs.wasNull()) {
                        br.setBorrowId(borrowId);
                    }
                    return br;
                }
            }
        }
        return null;
    }

    /**
     * Cập nhật nội dung bình luận và số sao đánh giá của một bản ghi đánh giá.
     *
     * @param review đối tượng {@link BookReview} chứa thông tin cần cập nhật
     * @return true nếu cập nhật thành công, false nếu thất bại
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public boolean update(BookReview review) throws Exception {
        String sql = "UPDATE book_reviews SET rating = ?, comment = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, review.getRating());
            ps.setString(2, review.getComment());
            ps.setInt(3, review.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa một bản ghi đánh giá sách khỏi cơ sở dữ liệu theo ID.
     *
     * @param id mã số ID của đánh giá cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM book_reviews WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy tập hợp danh sách các ID lượt mượn (borrow_id) đã được độc giả đánh giá.
     *
     * @param userId mã số độc giả
     * @return Tập hợp (Set) các borrow_id đã đánh giá
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public java.util.Set<Integer> getReviewedBorrowIds(int userId) throws Exception {
        java.util.Set<Integer> set = new java.util.HashSet<>();
        String sql = "SELECT borrow_id FROM book_reviews WHERE user_id = ? AND borrow_id IS NOT NULL";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getInt("borrow_id"));
                }
            }
        }
        return set;
    }

    /**
     * Lấy ID lượt mượn gần nhất của độc giả cho đầu sách cụ thể mà chưa thực hiện đánh giá.
     *
     * @param bookId mã số đầu sách
     * @param userId mã số độc giả
     * @return ID lượt mượn (borrow_id) chưa đánh giá, hoặc null nếu không còn lượt mượn nào chưa đánh giá
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public Integer getUnreviewedBorrowId(int bookId, int userId) throws Exception {
        String sql = "SELECT id FROM borrow_records " +
                     "WHERE book_id = ? AND user_id = ? " +
                     "AND status IN ('BORROWED', 'RETURNED', 'OVERDUE') " +
                     "AND id NOT IN (SELECT borrow_id FROM book_reviews WHERE borrow_id IS NOT NULL AND user_id = ?) " +
                     "ORDER BY borrow_date DESC, id DESC LIMIT 1";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }

    /**
     * Kiểm tra xem một lượt mượn cụ thể có hợp lệ để thực hiện đánh giá hay không.
     *
     * @param borrowId mã số lượt mượn cần kiểm tra
     * @param bookId   mã số đầu sách
     * @param userId   mã số độc giả
     * @return true nếu lượt mượn hợp lệ và chưa đánh giá, false nếu không hợp lệ
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public boolean isBorrowEligibleForReview(int borrowId, int bookId, int userId) throws Exception {
        String sql = "SELECT COUNT(*) FROM borrow_records " +
                     "WHERE id = ? AND book_id = ? AND user_id = ? " +
                     "AND status IN ('BORROWED', 'RETURNED', 'OVERDUE') " +
                     "AND id NOT IN (SELECT borrow_id FROM book_reviews WHERE borrow_id IS NOT NULL AND user_id = ?)";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowId);
            ps.setInt(2, bookId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Tính điểm số đánh giá sao trung bình của một đầu sách.
     *
     * @param bookId mã số đầu sách
     * @return điểm số trung bình (từ 0.0 đến 5.0)
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public double getAverageRating(int bookId) throws Exception {
        String sql = "SELECT AVG(rating) FROM book_reviews WHERE book_id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double val = rs.getDouble(1);
                    if (rs.wasNull()) return 0.0;
                    return val;
                }
            }
        }
        return 0.0;
    }

    /**
     * Kiểm tra xem độc giả đã từng mượn đầu sách này ít nhất một lần hay chưa.
     *
     * @param bookId mã số đầu sách
     * @param userId mã số độc giả
     * @return true nếu đã từng mượn, false nếu chưa từng mượn
     * @throws Exception nếu có lỗi truy vấn cơ sở dữ liệu
     */
    public boolean hasBorrowedBook(int bookId, int userId) throws Exception {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE book_id = ? AND user_id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}

