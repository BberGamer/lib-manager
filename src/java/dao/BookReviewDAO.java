package dao;

import model.BookReview;
import model.User;
import utils.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookReviewDAO {
    public List<BookReview> getReviewsByBookId(int bookId) throws Exception {
        List<BookReview> list = new ArrayList<>();
        String sql = "SELECT br.id, br.book_id, br.user_id, br.rating, br.comment, br.created_at, br.updated_at, br.borrow_id, " +
                     "u.full_name, u.student_id, " +
                     "bor.borrow_date " +
                     "FROM book_reviews br " +
                     "INNER JOIN users u ON br.user_id = u.id " +
                     "LEFT JOIN borrow_records bor ON br.borrow_id = bor.id " +
                     "WHERE br.book_id = ? " +
                     "ORDER BY br.created_at DESC";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
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

    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM book_reviews WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

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
