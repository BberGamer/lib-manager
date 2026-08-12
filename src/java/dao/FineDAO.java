package dao;

import model.Fine;
import model.User;
import model.BorrowRecord;
import model.Book;
import utils.DBContext;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    private Fine mapRow(ResultSet rs) throws SQLException {
        Fine fine = new Fine();
        fine.setId(rs.getInt("id"));
        fine.setBorrowRecordId(rs.getInt("borrow_record_id"));
        fine.setUserId(rs.getInt("user_id"));
        fine.setAmount(rs.getBigDecimal("amount"));
        fine.setOverdueDays(rs.getInt("overdue_days"));
        fine.setReason(rs.getString("reason"));
        fine.setStatus(rs.getString("status"));
        fine.setPaymentMethod(rs.getString("payment_method"));
        fine.setPaymentNote(rs.getString("payment_note"));
        
        Date pDate = rs.getDate("paid_date");
        if (pDate != null) fine.setPaidDate(pDate.toLocalDate());
        
        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) fine.setCreatedAt(cAt.toLocalDateTime());
        
        Timestamp uAt = rs.getTimestamp("updated_at");
        if (uAt != null) fine.setUpdatedAt(uAt.toLocalDateTime());

        try {
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            fine.setUser(user);
        } catch (SQLException ignored) {}

        try {
            BorrowRecord record = new BorrowRecord();
            record.setId(rs.getInt("borrow_record_id"));
            
            Book book = new Book();
            book.setTitle(rs.getString("title"));
            record.setBook(book);
            
            fine.setBorrowRecord(record);
        } catch (SQLException ignored) {}

        return fine;
    }

    public List<Fine> searchFines(String status, String keyword, int pageNum, int pageSize) throws Exception {
        List<Fine> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT f.id, f.borrow_record_id, f.user_id, f.amount, f.overdue_days, f.reason, f.status, f.payment_method, f.payment_note, f.paid_date, f.created_at, f.updated_at, ")
          .append("u.username, u.full_name, u.email, u.phone, ")
          .append("b.title ")
          .append("FROM fines f ")
          .append("INNER JOIN users u ON f.user_id = u.id ")
          .append("INNER JOIN borrow_records br ON f.borrow_record_id = br.id ")
          .append("INNER JOIN books b ON br.book_id = b.id ")
          .append("WHERE 1=1 ");

        if (status != null && !status.trim().isEmpty()) {
            sb.append("AND f.status = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (u.full_name LIKE ? OR u.username LIKE ? OR b.title LIKE ? OR f.reason LIKE ?) ");
        }

        sb.append("ORDER BY f.created_at DESC ")
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

    public int countFines(String status, String keyword) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) ")
          .append("FROM fines f ")
          .append("INNER JOIN users u ON f.user_id = u.id ")
          .append("INNER JOIN borrow_records br ON f.borrow_record_id = br.id ")
          .append("INNER JOIN books b ON br.book_id = b.id ")
          .append("WHERE 1=1 ");

        if (status != null && !status.trim().isEmpty()) {
            sb.append("AND f.status = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (u.full_name LIKE ? OR u.username LIKE ? OR b.title LIKE ? OR f.reason LIKE ?) ");
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
                ps.setString(idx++, kw);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean createFine(Fine fine) throws Exception {
        String sql = "INSERT INTO fines (borrow_record_id, user_id, amount, overdue_days, reason, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, 'UNPAID', NOW(), NOW())";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fine.getBorrowRecordId());
            ps.setInt(2, fine.getUserId());
            ps.setBigDecimal(3, fine.getAmount());
            ps.setInt(4, fine.getOverdueDays());
            ps.setString(5, fine.getReason());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status, String method, String note) throws Exception {
        String sql = "UPDATE fines SET status = ?, payment_method = ?, payment_note = ?, paid_date = CURDATE(), updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, method);
            ps.setString(3, note);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Fine findById(int id) throws Exception {
        String sql = "SELECT f.id, f.borrow_record_id, f.user_id, f.amount, f.overdue_days, f.reason, f.status, f.payment_method, f.payment_note, f.paid_date, f.created_at, f.updated_at, " +
                     "u.username, u.full_name, u.email, u.phone, " +
                     "b.title " +
                     "FROM fines f " +
                     "INNER JOIN users u ON f.user_id = u.id " +
                     "INNER JOIN borrow_records br ON f.borrow_record_id = br.id " +
                     "INNER JOIN books b ON br.book_id = b.id " +
                     "WHERE f.id = ?";
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
