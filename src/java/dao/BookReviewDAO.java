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
        String sql = "SELECT br.id, br.book_id, br.user_id, br.rating, br.comment, br.created_at, br.updated_at, " +
                     "u.full_name, u.student_id " +
                     "FROM book_reviews br " +
                     "INNER JOIN users u ON br.user_id = u.id " +
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

                    list.add(br);
                }
            }
        }
        return list;
    }

    public boolean insert(BookReview review) throws Exception {
        String sql = "INSERT INTO book_reviews (book_id, user_id, rating, comment, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, review.getBookId());
            ps.setInt(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            return ps.executeUpdate() > 0;
        }
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
