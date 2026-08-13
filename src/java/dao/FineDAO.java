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

    private static final String FINE_DETAIL_SELECT =
            "SELECT f.id, f.borrow_record_id, f.user_id, f.amount, f.overdue_days, f.reason, "
            + "f.status, f.payment_method, f.payment_note, f.paid_date, f.created_at, f.updated_at, "
            + "u.username, u.full_name, u.email, u.phone, b.title, "
            + "br.borrow_date, br.due_date, br.return_date "
            + "FROM fines f INNER JOIN users u ON f.user_id = u.id "
            + "INNER JOIN borrow_records br ON f.borrow_record_id = br.id "
            + "INNER JOIN books b ON br.book_id = b.id ";

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
            Date borrowDate = rs.getDate("borrow_date");
            Date dueDate = rs.getDate("due_date");
            Date returnDate = rs.getDate("return_date");
            if (borrowDate != null) record.setBorrowDate(borrowDate.toLocalDate());
            if (dueDate != null) record.setDueDate(dueDate.toLocalDate());
            if (returnDate != null) record.setReturnDate(returnDate.toLocalDate());
            
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
          .append("b.title, br.borrow_date, br.due_date, br.return_date ")
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
        String sql = "INSERT INTO fines (borrow_record_id, user_id, amount, overdue_days, reason, "
                + "status, created_at, updated_at) "
                + "SELECT ?, ?, ?, ?, ?, 'UNPAID', NOW(), NOW() "
                + "WHERE NOT EXISTS (SELECT 1 FROM fines WHERE borrow_record_id = ?)";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fine.getBorrowRecordId());
            ps.setInt(2, fine.getUserId());
            ps.setBigDecimal(3, fine.getAmount());
            ps.setInt(4, fine.getOverdueDays());
            ps.setString(5, fine.getReason());
            ps.setInt(6, fine.getBorrowRecordId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status, String method, String note, String operator) throws Exception {
        String sql = "UPDATE fines SET status = ?, payment_method = ?, payment_note = ?, paid_date = CURDATE(), updated_at = NOW() WHERE id = ?";
        int borrowRecordId = -1;
        String borrowStatus = "";

        try (Connection conn = DBContext.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Update the fine status
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, status);
                    ps.setString(2, method);
                    ps.setString(3, note);
                    ps.setInt(4, id);
                    if (ps.executeUpdate() <= 0) {
                        conn.rollback();
                        return false;
                    }
                }

                // 2. Retrieve borrow record ID and check status
                String selectBorrowRecord = "SELECT f.borrow_record_id, br.status "
                        + "FROM fines f INNER JOIN borrow_records br ON f.borrow_record_id = br.id "
                        + "WHERE f.id = ?";
                try (PreparedStatement ps = conn.prepareStatement(selectBorrowRecord)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            borrowRecordId = rs.getInt("borrow_record_id");
                            borrowStatus = rs.getString("status");
                        }
                    }
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        // 3. If borrow record status is OVERDUE and fine is paid/waived, trigger return
        if (borrowRecordId != -1 && "OVERDUE".equalsIgnoreCase(borrowStatus)
                && ("PAID".equalsIgnoreCase(status) || "WAIVED".equalsIgnoreCase(status))) {
            new BorrowRecordDAO().confirmReturn(borrowRecordId, operator, "GOOD", "Trả tự động khi thanh toán phạt");
        }

        return true;
    }

    public Fine findById(int id) throws Exception {
        String sql = "SELECT f.id, f.borrow_record_id, f.user_id, f.amount, f.overdue_days, f.reason, f.status, f.payment_method, f.payment_note, f.paid_date, f.created_at, f.updated_at, " +
                     "u.username, u.full_name, u.email, u.phone, " +
                     "b.title, br.borrow_date, br.due_date, br.return_date " +
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

    /**
     * Tìm các khoản phạt thuộc một người dùng theo trạng thái và từ khóa.
     *
     * @param userId mã người dùng sở hữu khoản phạt
     * @param status trạng thái hợp lệ hoặc {@code null} để lấy tất cả
     * @param keyword từ khóa tên sách hoặc mã khoản phạt
     * @return danh sách khoản phạt mới nhất trước
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
    public List<Fine> searchByUser(int userId, String status, String keyword) throws Exception {
        StringBuilder sql = new StringBuilder(FINE_DETAIL_SELECT).append("WHERE f.user_id = ? ");
        if (status != null) {
            sql.append("AND f.status = ? ");
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (b.title LIKE ? OR CAST(f.id AS CHAR) LIKE ?) ");
        }
        sql.append("ORDER BY f.created_at DESC");
        List<Fine> fines = new ArrayList<>();
        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setInt(index++, userId);
            if (status != null) {
                statement.setString(index++, status);
            }
            if (keyword != null && !keyword.isEmpty()) {
                String searchPattern = "%" + keyword + "%";
                statement.setString(index++, searchPattern);
                statement.setString(index, searchPattern.replace("#F", "").replace("#f", ""));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    fines.add(mapRow(resultSet));
                }
            }
        }
        return fines;
    }

    /**
     * Tìm chi tiết khoản phạt theo cả mã khoản phạt và chủ sở hữu.
     *
     * @param fineId mã khoản phạt
     * @param userId mã người dùng đang đăng nhập
     * @return khoản phạt thuộc người dùng hoặc {@code null}
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
    public Fine findByIdAndUserId(int fineId, int userId) throws Exception {
        String sql = FINE_DETAIL_SELECT + "WHERE f.id = ? AND f.user_id = ?";
        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, fineId);
            statement.setInt(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }
}
