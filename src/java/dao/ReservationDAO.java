package dao;

import model.ReservationRecord;
import model.Book;
import model.User;
import utils.DBContext;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private ReservationRecord mapRow(ResultSet rs) throws SQLException {
        ReservationRecord record = new ReservationRecord();
        record.setId(rs.getInt("id"));
        record.setBookId(rs.getInt("book_id"));
        record.setUserId(rs.getInt("user_id"));
        
        Timestamp rDate = rs.getTimestamp("reserve_date");
        if (rDate != null) record.setReserveDate(rDate.toLocalDateTime());
        
        Timestamp eDate = rs.getTimestamp("expiry_date");
        if (eDate != null) record.setExpiryDate(eDate.toLocalDateTime());
        
        record.setStatus(rs.getString("status"));
        
        Timestamp nAt = rs.getTimestamp("notified_at");
        if (nAt != null) record.setNotifiedAt(nAt.toLocalDateTime());
        
        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) record.setCreatedAt(cAt.toLocalDateTime());
        
        Timestamp uAt = rs.getTimestamp("updated_at");
        if (uAt != null) record.setUpdatedAt(uAt.toLocalDateTime());

        try {
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            record.setUser(user);
        } catch (SQLException ignored) {}

        try {
            Book book = new Book();
            book.setId(rs.getInt("book_id"));
            book.setTitle(rs.getString("title"));
            book.setIsbn(rs.getString("isbn"));
            record.setBook(book);
        } catch (SQLException ignored) {}

        return record;
    }

    public List<ReservationRecord> searchReservations(String status, String keyword, int pageNum, int pageSize) throws Exception {
        List<ReservationRecord> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT r.id, r.book_id, r.user_id, r.reserve_date, r.expiry_date, r.status, r.notified_at, r.created_at, r.updated_at, ")
          .append("u.username, u.full_name, u.email, u.phone, ")
          .append("b.title, b.isbn ")
          .append("FROM book_reservations r ")
          .append("INNER JOIN users u ON r.user_id = u.id ")
          .append("INNER JOIN books b ON r.book_id = b.id ")
          .append("WHERE 1=1 ");

        if (status != null && !status.trim().isEmpty()) {
            sb.append("AND r.status = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (u.full_name LIKE ? OR u.username LIKE ? OR b.title LIKE ?) ");
        }

        sb.append("ORDER BY r.created_at DESC ")
          .append("LIMIT ?, ?");

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(idx++, status.trim());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            int offset = (pageNum - 1) * pageSize;
            ps.setInt(idx++, offset);
            ps.setInt(idx++, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int countReservations(String status, String keyword) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) ")
          .append("FROM book_reservations r ")
          .append("INNER JOIN users u ON r.user_id = u.id ")
          .append("INNER JOIN books b ON r.book_id = b.id ")
          .append("WHERE 1=1 ");

        if (status != null && !status.trim().isEmpty()) {
            sb.append("AND r.status = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (u.full_name LIKE ? OR u.username LIKE ? OR b.title LIKE ?) ");
        }

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(idx++, status.trim());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean updateStatus(int id, String status) throws Exception {
        String sql = "UPDATE book_reservations SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public ReservationRecord findById(int id) throws Exception {
        String sql = "SELECT r.id, r.book_id, r.user_id, r.reserve_date, r.expiry_date, r.status, r.notified_at, r.created_at, r.updated_at, " +
                     "u.username, u.full_name, u.email, u.phone, " +
                     "b.title, b.isbn " +
                     "FROM book_reservations r " +
                     "INNER JOIN users u ON r.user_id = u.id " +
                     "INNER JOIN books b ON r.book_id = b.id " +
                     "WHERE r.id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }
}
