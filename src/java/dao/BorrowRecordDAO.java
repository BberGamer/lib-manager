package dao;

import model.BorrowRecord;
import model.Book;
import model.BookCopy;
import model.User;
import utils.DBContext;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAO {

    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(rs.getInt("id"));
        record.setUserId(rs.getInt("user_id"));
        record.setBookId(rs.getInt("book_id"));
        
        int copyId = rs.getInt("copy_id");
        if (!rs.wasNull()) {
            record.setCopyId(copyId);
        } else {
            record.setCopyId(null);
        }

        Date bDate = rs.getDate("borrow_date");
        if (bDate != null) record.setBorrowDate(bDate.toLocalDate());
        
        Date dDate = rs.getDate("due_date");
        if (dDate != null) record.setDueDate(dDate.toLocalDate());
        
        Date rDate = rs.getDate("return_date");
        if (rDate != null) record.setReturnDate(rDate.toLocalDate());

        record.setRenewalCount(rs.getInt("renewal_count"));
        record.setStatus(rs.getString("status"));
        record.setNote(rs.getString("note"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) record.setCreatedAt(createdAt.toLocalDateTime());
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) record.setUpdatedAt(updatedAt.toLocalDateTime());

        // Map joined objects if present
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

        try {
            String barcode = rs.getString("barcode");
            if (barcode != null) {
                BookCopy copy = new BookCopy();
                copy.setId(copyId);
                copy.setBarcode(barcode);
                copy.setBookCondition(rs.getString("book_condition"));
                copy.setStatus(rs.getString("copy_status"));
                record.setBookCopy(copy);
            }
        } catch (SQLException ignored) {}

        return record;
    }

    public List<BorrowRecord> searchBorrowRecords(String status, String keyword, int pageNum, int pageSize) throws Exception {
        List<BorrowRecord> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT br.id, br.user_id, br.book_id, br.copy_id, br.borrow_date, br.due_date, br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, ")
          .append("u.username, u.full_name, u.email, u.phone, ")
          .append("b.title, b.isbn, ")
          .append("bc.barcode, bc.book_condition, bc.status AS copy_status ")
          .append("FROM borrow_records br ")
          .append("INNER JOIN users u ON br.user_id = u.id ")
          .append("INNER JOIN books b ON br.book_id = b.id ")
          .append("LEFT JOIN book_copies bc ON br.copy_id = bc.id ")
          .append("WHERE 1=1 ");

        if (status != null && !status.trim().isEmpty()) {
            sb.append("AND br.status = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (u.full_name LIKE ? OR u.username LIKE ? OR b.title LIKE ? OR bc.barcode LIKE ?) ");
        }

        sb.append("ORDER BY br.created_at DESC ")
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

    public int countBorrowRecords(String status, String keyword) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) ")
          .append("FROM borrow_records br ")
          .append("INNER JOIN users u ON br.user_id = u.id ")
          .append("INNER JOIN books b ON br.book_id = b.id ")
          .append("LEFT JOIN book_copies bc ON br.copy_id = bc.id ")
          .append("WHERE 1=1 ");

        if (status != null && !status.trim().isEmpty()) {
            sb.append("AND br.status = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (u.full_name LIKE ? OR u.username LIKE ? OR b.title LIKE ? OR bc.barcode LIKE ?) ");
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

    public BorrowRecord findById(int id) throws Exception {
        String sql = "SELECT br.id, br.user_id, br.book_id, br.copy_id, br.borrow_date, br.due_date, br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, " +
                     "u.username, u.full_name, u.email, u.phone, " +
                     "b.title, b.isbn, " +
                     "bc.barcode, bc.book_condition, bc.status AS copy_status " +
                     "FROM borrow_records br " +
                     "INNER JOIN users u ON br.user_id = u.id " +
                     "INNER JOIN books b ON br.book_id = b.id " +
                     "LEFT JOIN book_copies bc ON br.copy_id = bc.id " +
                     "WHERE br.id = ?";
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

    public boolean confirmLoan(int id, int copyId, String operator) throws Exception {
        String selectRecord = "SELECT book_id FROM borrow_records WHERE id = ?";
        String updateRecord = "UPDATE borrow_records SET copy_id = ?, status = 'BORROWING', borrow_date = CURDATE(), due_date = DATE_ADD(CURDATE(), INTERVAL 14 DAY), updated_at = NOW() WHERE id = ?";
        String updateCopy = "UPDATE book_copies SET status = 'BORROWED', updated_by = ?, updated_at = NOW() WHERE id = ?";
        String updateBook = "UPDATE books SET available = GREATEST(0, available - 1) WHERE id = ?";

        try (Connection conn = DBContext.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bookId = -1;
                try (PreparedStatement ps = conn.prepareStatement(selectRecord)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            bookId = rs.getInt("book_id");
                        }
                    }
                }
                if (bookId == -1) {
                    conn.rollback();
                    return false;
                }

                // 1. Update borrow record
                try (PreparedStatement ps = conn.prepareStatement(updateRecord)) {
                    ps.setInt(1, copyId);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }

                // 2. Update book copy
                try (PreparedStatement ps = conn.prepareStatement(updateCopy)) {
                    ps.setString(1, operator);
                    ps.setInt(2, copyId);
                    ps.executeUpdate();
                }

                // 3. Update book availability
                try (PreparedStatement ps = conn.prepareStatement(updateBook)) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean confirmReturn(int id, String operator, String condition, String note) throws Exception {
        String selectRecord = "SELECT book_id, copy_id FROM borrow_records WHERE id = ?";
        String updateRecord = "UPDATE borrow_records SET return_date = CURDATE(), status = 'RETURNED', updated_at = NOW() WHERE id = ?";
        String updateCopy = "UPDATE book_copies SET status = 'AVAILABLE', book_condition = ?, note = ?, updated_by = ?, updated_at = NOW() WHERE id = ?";
        String updateBook = "UPDATE books SET available = available + 1 WHERE id = ?";

        try (Connection conn = DBContext.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bookId = -1;
                int copyId = -1;
                try (PreparedStatement ps = conn.prepareStatement(selectRecord)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            bookId = rs.getInt("book_id");
                            copyId = rs.getInt("copy_id");
                        }
                    }
                }
                if (bookId == -1 || copyId == -1) {
                    conn.rollback();
                    return false;
                }

                // 1. Update borrow record
                try (PreparedStatement ps = conn.prepareStatement(updateRecord)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }

                // 2. Update book copy
                try (PreparedStatement ps = conn.prepareStatement(updateCopy)) {
                    ps.setString(1, condition);
                    ps.setString(2, note);
                    ps.setString(3, operator);
                    ps.setInt(4, copyId);
                    ps.executeUpdate();
                }

                // 3. Update book availability
                try (PreparedStatement ps = conn.prepareStatement(updateBook)) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
