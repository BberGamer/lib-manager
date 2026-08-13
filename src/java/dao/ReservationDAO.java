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
        if (rDate != null) {
            record.setReserveDate(rDate.toLocalDateTime());
        }

        Timestamp eDate = rs.getTimestamp("expiry_date");
        if (eDate != null) {
            record.setExpiryDate(eDate.toLocalDateTime());
        }

        record.setStatus(rs.getString("status"));

        Timestamp nAt = rs.getTimestamp("notified_at");
        if (nAt != null) {
            record.setNotifiedAt(nAt.toLocalDateTime());
        }

        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) {
            record.setCreatedAt(cAt.toLocalDateTime());
        }

        Timestamp uAt = rs.getTimestamp("updated_at");
        if (uAt != null) {
            record.setUpdatedAt(uAt.toLocalDateTime());
        }

        try {
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            record.setUser(user);
        } catch (SQLException ignored) {
        }

        try {
            Book book = new Book();
            book.setId(rs.getInt("book_id"));
            book.setTitle(rs.getString("title"));
            book.setIsbn(rs.getString("isbn"));
            record.setBook(book);
        } catch (SQLException ignored) {
        }

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

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
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

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
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
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean updateStatus(int id, String status) throws Exception {
        String sql = "UPDATE book_reservations SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public ReservationRecord findById(int id) throws Exception {
        String sql = "SELECT r.id, r.book_id, r.user_id, r.reserve_date, r.expiry_date, r.status, r.notified_at, r.created_at, r.updated_at, "
                + "u.username, u.full_name, u.email, u.phone, "
                + "b.title, b.isbn "
                + "FROM book_reservations r "
                + "INNER JOIN users u ON r.user_id = u.id "
                + "INNER JOIN books b ON r.book_id = b.id "
                + "WHERE r.id = ?";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
     * Lấy toàn bộ reservation của user và tính vị trí hàng chờ động.
     */
    public List<ReservationRecord> findByUserId(int userId) throws Exception {
        String sql = "SELECT r.id, r.book_id, r.user_id, r.reserve_date, r.expiry_date, r.status, "
                + "r.notified_at, r.created_at, r.updated_at, u.username, u.full_name, u.email, "
                + "u.phone, b.title, b.isbn, CASE WHEN r.status='WAITING' THEN "
                + "(SELECT COUNT(*) FROM book_reservations earlier WHERE earlier.book_id=r.book_id "
                + "AND earlier.status IN ('WAITING','READY_FOR_PICKUP') "
                + "AND (earlier.created_at<r.created_at OR (earlier.created_at=r.created_at "
                + "AND earlier.id<=r.id))) ELSE 0 END queue_position "
                + "FROM book_reservations r INNER JOIN users u ON r.user_id=u.id "
                + "INNER JOIN books b ON r.book_id=b.id WHERE r.user_id=? ORDER BY r.created_at DESC";
        List<ReservationRecord> records = new ArrayList<>();
        try (Connection connection = DBContext.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ReservationRecord record = mapRow(resultSet);
                    record.setQueuePosition(resultSet.getInt("queue_position"));
                    records.add(record);
                }
            }
        }
        return records;
    }

    /**
     * Trả về reservation active của user cho sách, hoặc null.
     */
    public ReservationRecord findActive(int userId, int bookId) throws Exception {
        String sql = "SELECT r.id, r.book_id, r.user_id, r.reserve_date, r.expiry_date, r.status, "
                + "r.notified_at, r.created_at, r.updated_at, u.username,u.full_name,u.email,u.phone, "
                + "b.title,b.isbn FROM book_reservations r INNER JOIN users u ON r.user_id=u.id "
                + "INNER JOIN books b ON r.book_id=b.id WHERE r.user_id=? AND r.book_id=? "
                + "AND r.status IN ('WAITING','READY_FOR_PICKUP') LIMIT 1";
        try (Connection connection = DBContext.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, bookId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapRow(result) : null;
            }
        }
    }

    /**
     * Đếm số reservation đang hoạt động của đầu sách.
     */
    public int countActiveByBook(int bookId) throws Exception {
        String sql = "SELECT COUNT(*) FROM book_reservations WHERE book_id=? "
                + "AND status IN ('WAITING','READY_FOR_PICKUP')";
        try (Connection connection = DBContext.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    /**
     * Tạo reservation WAITING sau khi khóa đầu sách và kiểm tra lại điều kiện.
     */
    public boolean createWaiting(int userId, int bookId) throws Exception {
        String lockBook = "SELECT id FROM books WHERE id=? AND is_deleted=0 FOR UPDATE";
        String available = "SELECT id FROM book_copies WHERE book_id=? AND status='AVAILABLE' "
                + "AND is_deleted=0 LIMIT 1 FOR UPDATE";
        String duplicate = "SELECT id FROM book_reservations WHERE user_id=? AND book_id=? "
                + "AND status IN ('WAITING','READY_FOR_PICKUP') FOR UPDATE";
        String activeBorrow = "SELECT id FROM borrow_records WHERE user_id=? AND book_id=? "
                + "AND status IN ('PENDING_PICKUP','BORROWED','OVERDUE') FOR UPDATE";
        String insert = "INSERT INTO book_reservations(user_id,book_id,reserve_date,status,created_at,updated_at) "
                + "VALUES(?,?,NOW(),'WAITING',NOW(),NOW())";
        try (Connection connection = DBContext.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement s = connection.prepareStatement(lockBook)) {
                    s.setInt(1, bookId);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) {
                            connection.rollback();
                            return false;
                        }
                    }
                }
                try (PreparedStatement s = connection.prepareStatement(available)) {
                    s.setInt(1, bookId);
                    try (ResultSet r = s.executeQuery()) {
                        if (r.next()) {
                            connection.rollback();
                            return false;
                        }
                    }
                }
                for (String sql : new String[]{duplicate, activeBorrow}) {
                    try (PreparedStatement s = connection.prepareStatement(sql)) {
                        s.setInt(1, userId);
                        s.setInt(2, bookId);
                        try (ResultSet r = s.executeQuery()) {
                            if (r.next()) {
                                connection.rollback();
                                return false;
                            }
                        }
                    }
                }
                try (PreparedStatement s = connection.prepareStatement(insert)) {
                    s.setInt(1, userId);
                    s.setInt(2, bookId);
                    s.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Hủy reservation active thuộc user.
     */
    public boolean cancelOwned(int reservationId, int userId) throws Exception {
        String sql = "UPDATE book_reservations SET status='CANCELLED',updated_at=NOW() WHERE id=? "
                + "AND user_id=? AND status IN ('WAITING','READY_FOR_PICKUP')";
        try (Connection c = DBContext.getInstance().getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, reservationId);
            s.setInt(2, userId);
            return s.executeUpdate() == 1;
        }
    }

    /**
     * Đưa reservation WAITING cũ nhất sang trạng thái sẵn sàng trong 24 giờ.
     */
    public boolean activateNext(int bookId, int readyHours) throws Exception {
        String sql = "UPDATE book_reservations SET status='READY_FOR_PICKUP',notified_at=NOW(),"
                + "expiry_date=DATE_ADD(NOW(),INTERVAL ? HOUR),updated_at=NOW() WHERE id=(SELECT id FROM "
                + "(SELECT id FROM book_reservations WHERE book_id=? AND status='WAITING' "
                + "ORDER BY created_at,id LIMIT 1) next_reservation)";
        try (Connection c = DBContext.getInstance().getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, readyHours);
            s.setInt(2, bookId);
            return s.executeUpdate() == 1;
        }
    }

    public ReservationRecord manuallyReadyReservation(int reservationId, String operator) throws Exception {
        String selectRes = "SELECT r.book_id, r.user_id, r.status, b.title, u.email, u.full_name "
                + "FROM book_reservations r "
                + "INNER JOIN books b ON r.book_id = b.id "
                + "INNER JOIN users u ON r.user_id = u.id "
                + "WHERE r.id = ? FOR UPDATE";
        String copySql = "SELECT id FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE' "
                + "ORDER BY id LIMIT 1 FOR UPDATE";
        String updateRes = "UPDATE book_reservations SET status = 'READY_FOR_PICKUP', notified_at = NOW(), expiry_date = DATE_ADD(NOW(), INTERVAL 24 HOUR), updated_at = NOW() WHERE id = ?";
        String updateCopy = "UPDATE book_copies SET status = 'RESERVED', updated_by = ?, updated_at = NOW() WHERE id = ?";
        String insertBorrow = "INSERT INTO borrow_records (user_id, book_id, copy_id, request_date, pickup_deadline, borrow_date, due_date, renewal_count, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 24 HOUR), NULL, NULL, 0, 'PENDING_PICKUP', NOW(), NOW())";
        String updateBook = "UPDATE books SET available = GREATEST(0, available - 1) WHERE id = ?";

        try (Connection conn = DBContext.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bookId = -1;
                int userId = -1;
                String bookTitle = "";
                String email = "";
                String fullName = "";
                String resStatus = "";

                try (PreparedStatement ps = conn.prepareStatement(selectRes)) {
                    ps.setInt(1, reservationId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            bookId = rs.getInt("book_id");
                            userId = rs.getInt("user_id");
                            resStatus = rs.getString("status");
                            bookTitle = rs.getString("title");
                            email = rs.getString("email");
                            fullName = rs.getString("full_name");
                        }
                    }
                }

                if (bookId == -1 || !"WAITING".equals(resStatus)) {
                    conn.rollback();
                    return null;
                }

                // Check for available copy
                int copyId = -1;
                try (PreparedStatement ps = conn.prepareStatement(copySql)) {
                    ps.setInt(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            copyId = rs.getInt(1);
                        }
                    }
                }

                if (copyId == -1) {
                    conn.rollback();
                    return null;
                }

                // 1. Update reservation
                try (PreparedStatement ps = conn.prepareStatement(updateRes)) {
                    ps.setInt(1, reservationId);
                    ps.executeUpdate();
                }

                // 2. Update copy to RESERVED
                try (PreparedStatement ps = conn.prepareStatement(updateCopy)) {
                    ps.setString(1, operator);
                    ps.setInt(2, copyId);
                    ps.executeUpdate();
                }

                // 3. Insert borrow record
                try (PreparedStatement ps = conn.prepareStatement(insertBorrow)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, bookId);
                    ps.setInt(3, copyId);
                    ps.executeUpdate();
                }

                // 4. Update book available count
                try (PreparedStatement ps = conn.prepareStatement(updateBook)) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }

                conn.commit();

                // Create a record object to return data for notification
                ReservationRecord record = new ReservationRecord();
                record.setId(reservationId);
                record.setBookId(bookId);
                record.setUserId(userId);
                
                model.Book book = new model.Book();
                book.setTitle(bookTitle);
                record.setBook(book);
                
                User user = new User();
                user.setId(userId);
                user.setEmail(email);
                user.setFullName(fullName);
                record.setUser(user);

                return record;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
