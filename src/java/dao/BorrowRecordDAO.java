/*
 * DAO quản lý dữ liệu lượt mượn, nhận sách, trả sách và các truy vấn nhắc hạn.
 * Lớp chịu trách nhiệm ánh xạ dữ liệu JDBC và bảo đảm tính nhất quán của các giao dịch mượn trả.
 */
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

/**
 * Cung cấp các thao tác lưu trữ cho lượt mượn sách và phối hợp cập nhật bản sao, đầu sách,
 * đặt trước trong cùng giao dịch khi trạng thái mượn thay đổi.
 */
public class BorrowRecordDAO {

    /**
     * Chứa kết quả nhận trả và thông tin đặt trước vừa được kích hoạt để controller gửi thông báo.
     */
    public static class ReturnResult {
        public boolean success = false;
        public int activatedReservationId = -1;
        public int activatedUserId = -1;
        public String bookTitle = null;
        public String userEmail = null;
        public String userFullName = null;
    }

    private static final String BORROW_RECORD_SELECT
            = "SELECT br.id, br.user_id, br.book_id, br.copy_id, br.request_date, br.pickup_deadline, "
            + "br.pickup_date, br.borrow_date, br.due_date, "
            + "br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, "
            + "u.username, u.full_name, u.email, u.phone, b.title, b.isbn, b.price, "
            + "bc.barcode, bc.book_condition, bc.status AS copy_status "
            + "FROM borrow_records br "
            + "INNER JOIN users u ON br.user_id = u.id "
            + "INNER JOIN books b ON br.book_id = b.id "
            + "LEFT JOIN book_copies bc ON br.copy_id = bc.id ";

    /**
     * Ánh xạ hàng kết quả truy vấn thành lượt mượn cùng các đối tượng liên kết nếu truy vấn có trả về.
     *
     * @param rs hàng dữ liệu đang được trỏ tới
     * @return lượt mượn đã được ánh xạ
     * @throws SQLException khi không thể đọc cột bắt buộc
     */
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

        Timestamp requestDate = rs.getTimestamp("request_date");
        Timestamp pickupDeadline = rs.getTimestamp("pickup_deadline");
        Timestamp pickupDate = rs.getTimestamp("pickup_date");
        if (requestDate != null) {
            record.setRequestDate(requestDate.toLocalDateTime());
        }
        if (pickupDeadline != null) {
            record.setPickupDeadline(pickupDeadline.toLocalDateTime());
        }
        if (pickupDate != null) {
            record.setPickupDate(pickupDate.toLocalDateTime());
        }

        Date bDate = rs.getDate("borrow_date");
        if (bDate != null) {
            record.setBorrowDate(bDate.toLocalDate());
        }

        Date dDate = rs.getDate("due_date");
        if (dDate != null) {
            record.setDueDate(dDate.toLocalDate());
        }

        Date rDate = rs.getDate("return_date");
        if (rDate != null) {
            record.setReturnDate(rDate.toLocalDate());
        }

        record.setRenewalCount(rs.getInt("renewal_count"));
        record.setStatus(rs.getString("status"));
        record.setNote(rs.getString("note"));

        try {
            record.setHasFine(rs.getBoolean("has_fine"));
        } catch (SQLException ignored) {
            record.setHasFine(false);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            record.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            record.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        // Các truy vấn rút gọn có thể không chứa toàn bộ cột của đối tượng liên kết.
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
            book.setPrice(rs.getInt("price"));
            record.setBook(book);
        } catch (SQLException ignored) {
        }

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
        } catch (SQLException ignored) {
        }

        return record;
    }

    /**
     * Tìm các lượt mượn cho trang quản lý theo trạng thái, từ khóa và phân trang.
     *
     * @param status trạng thái cần lọc hoặc rỗng để lấy tất cả
     * @param keyword từ khóa theo độc giả, sách hoặc mã bản sao
     * @param pageNum số trang bắt đầu từ 1
     * @param pageSize số bản ghi tối đa trên một trang
     * @return danh sách lượt mượn mới nhất trước
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
    public List<BorrowRecord> searchBorrowRecords(String status, String keyword, int pageNum, int pageSize) throws Exception {
        List<BorrowRecord> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT br.id, br.user_id, br.book_id, br.copy_id, br.request_date, br.pickup_deadline, br.pickup_date, br.borrow_date, br.due_date, br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, ")
                .append("u.username, u.full_name, u.email, u.phone, ")
                .append("b.title, b.isbn, b.price, ")
                .append("bc.barcode, bc.book_condition, bc.status AS copy_status, ")
                .append("EXISTS (SELECT 1 FROM fines f WHERE f.borrow_record_id = br.id ")
                .append("AND f.fine_type = 'BOOK_CONDITION') AS has_fine ")
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

    /**
     * Đếm tổng số lượt mượn khớp bộ lọc để tính phân trang quản lý.
     *
     * @param status trạng thái cần lọc hoặc rỗng để lấy tất cả
     * @param keyword từ khóa theo độc giả, sách hoặc mã bản sao
     * @return tổng số bản ghi phù hợp
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
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

    /**
     * Tìm một lượt mượn cùng thông tin độc giả, sách và bản sao theo mã định danh.
     *
     * @param id mã lượt mượn
     * @return lượt mượn tương ứng hoặc {@code null} nếu không tồn tại
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
    public BorrowRecord findById(int id) throws Exception {
        String sql = "SELECT br.id, br.user_id, br.book_id, br.copy_id, br.request_date, br.pickup_deadline, br.pickup_date, br.borrow_date, br.due_date, br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, "
                + "u.username, u.full_name, u.email, u.phone, "
                + "b.title, b.isbn, b.price, "
                + "bc.barcode, bc.book_condition, bc.status AS copy_status "
                + "FROM borrow_records br "
                + "INNER JOIN users u ON br.user_id = u.id "
                + "INNER JOIN books b ON br.book_id = b.id "
                + "LEFT JOIN book_copies bc ON br.copy_id = bc.id "
                + "WHERE br.id = ?";
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
     * Lấy toàn bộ lượt mượn của một độc giả để hiển thị trang cá nhân.
     *
     * @param userId mã người dùng đã được controller xác thực
     * @return danh sách lượt mượn mới nhất trước, không bao giờ trả về
     * {@code null}
     * @throws Exception khi không thể truy vấn cơ sở dữ liệu
     */
    public List<BorrowRecord> findByUserId(int userId) throws Exception {
        String sql = BORROW_RECORD_SELECT
                + "WHERE br.user_id = ? "
                + "ORDER BY CASE WHEN br.status IN ('PENDING_PICKUP', 'BORROWED', 'OVERDUE') THEN 0 ELSE 1 END, "
                + "br.created_at DESC";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection connection = DBContext.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapRow(resultSet));
                }
            }
        }
        return records;
    }

    /**
     * Gia hạn một lượt mượn thuộc đúng độc giả và vẫn còn ở trạng thái đang
     * mượn. Điều kiện số lần gia hạn được kiểm tra nguyên tử trong câu lệnh cập
     * nhật.
     *
     * @param borrowRecordId mã lượt mượn
     * @param userId mã độc giả sở hữu lượt mượn
     * @param maximumRenewals số lần gia hạn tối đa
     * @param extensionDays số ngày cộng thêm vào hạn trả
     * @return {@code true} nếu có đúng một lượt mượn được gia hạn
     * @throws Exception khi không thể cập nhật cơ sở dữ liệu
     */
    public boolean renewForUser(int borrowRecordId, int userId, int maximumRenewals,
            int extensionDays) throws Exception {
        String sql = "UPDATE borrow_records "
                + "SET due_date = DATE_ADD(due_date, INTERVAL ? DAY), "
                + "renewal_count = renewal_count + 1, updated_at = NOW() "
                + "WHERE id = ? AND user_id = ? AND status = 'BORROWED' "
                + "AND due_date >= CURDATE() AND renewal_count < ?";
        try (Connection connection = DBContext.getInstance().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, extensionDays);
            statement.setInt(2, borrowRecordId);
            statement.setInt(3, userId);
            statement.setInt(4, maximumRenewals);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Xác nhận giao một bản sao cho lượt mượn và đồng bộ trạng thái kho sách trong một giao dịch.
     *
     * @param id mã lượt mượn
     * @param copyId mã bản sao được giao
     * @param operator tài khoản nhân viên thực hiện
     * @return {@code true} nếu giao dịch hoàn tất, ngược lại {@code false}
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    public boolean confirmLoan(int id, int copyId, String operator) throws Exception {
        String selectRecord = "SELECT book_id FROM borrow_records WHERE id = ?";
        String updateRecord = "UPDATE borrow_records SET copy_id = ?, status = 'BORROWING', "
                + "borrow_date = CURDATE(), due_date = DATE_ADD(CURDATE(), INTERVAL 14 DAY), "
                + "updated_at = NOW() WHERE id = ?";
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

                // Ghi nhận ngày mượn và hạn trả trước khi cập nhật trạng thái kho.
                try (PreparedStatement ps = conn.prepareStatement(updateRecord)) {
                    ps.setInt(1, copyId);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }

                // Đánh dấu bản sao đang được độc giả giữ.
                try (PreparedStatement ps = conn.prepareStatement(updateCopy)) {
                    ps.setString(1, operator);
                    ps.setInt(2, copyId);
                    ps.executeUpdate();
                }

                // Đồng bộ số lượng khả dụng tổng hợp của đầu sách.
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

    /**
     * Xác nhận trả sách và chuyển bản sao cho người đặt kế tiếp hoặc trả về kho trong một giao dịch.
     * Khoản phạt liên quan vẫn giữ nguyên trạng thái; chỉ luồng đóng phạt mới được cập nhật thanh toán.
     *
     * @param id mã lượt mượn cần nhận trả
     * @param operator tài khoản nhân viên nhận sách
     * @param condition tình trạng bản sao khi nhận lại
     * @param note ghi chú kiểm tra bản sao
     * @return kết quả nhận trả và dữ liệu người đặt kế tiếp nếu có
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    public ReturnResult confirmReturn(int id, String operator, String condition, String note) throws Exception {
        ReturnResult resObj = new ReturnResult();
        resObj.success = false;

        String selectRecord = "SELECT book_id, copy_id FROM borrow_records WHERE id = ? "
                + "AND status IN ('BORROWED', 'OVERDUE')";
        String updateRecord = "UPDATE borrow_records SET return_date = CURDATE(), status = 'RETURNED', updated_at = NOW() WHERE id = ?";
        
        String selectReservation = "SELECT r.id, r.user_id, b.title, u.email, u.full_name "
                + "FROM book_reservations r "
                + "INNER JOIN books b ON r.book_id = b.id "
                + "INNER JOIN users u ON r.user_id = u.id "
                + "WHERE r.book_id = ? AND r.status = 'WAITING' "
                + "AND NOT EXISTS (SELECT 1 FROM fines f WHERE f.user_id = r.user_id AND f.status = 'UNPAID') "
                + "AND (SELECT COUNT(*) FROM borrow_records br WHERE br.user_id = r.user_id AND br.status IN ('PENDING_PICKUP', 'BORROWED', 'OVERDUE')) < 3 "
                + "ORDER BY r.created_at, r.id LIMIT 1 FOR UPDATE";
        
        String updateRes = "UPDATE book_reservations SET status = 'READY_FOR_PICKUP', notified_at = NOW(), expiry_date = DATE_ADD(NOW(), INTERVAL 24 HOUR), updated_at = NOW() WHERE id = ?";
        
        String updateCopyReserved = "UPDATE book_copies SET status = 'RESERVED', book_condition = ?, note = ?, updated_by = ?, updated_at = NOW() WHERE id = ?";
        String updateCopyAvailable = "UPDATE book_copies SET status = 'AVAILABLE', book_condition = ?, note = ?, updated_by = ?, updated_at = NOW() WHERE id = ?";
        
        String insertBorrow = "INSERT INTO borrow_records (user_id, book_id, copy_id, request_date, pickup_deadline, borrow_date, due_date, renewal_count, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 24 HOUR), NULL, NULL, 0, 'PENDING_PICKUP', NOW(), NOW())";
        
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
                    return resObj;
                }

                // Việc nhận trả không đồng nghĩa khoản phạt đã được thanh toán.
                try (PreparedStatement ps = conn.prepareStatement(updateRecord)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }

                // Khóa người đặt hợp lệ đầu tiên để tránh hai giao dịch cùng cấp một bản sao.
                int waitingResId = -1;
                int waitingUserId = -1;
                String bookTitle = null;
                String userEmail = null;
                String userFullName = null;
                try (PreparedStatement ps = conn.prepareStatement(selectReservation)) {
                    ps.setInt(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            waitingResId = rs.getInt("id");
                            waitingUserId = rs.getInt("user_id");
                            bookTitle = rs.getString("title");
                            userEmail = rs.getString("email");
                            userFullName = rs.getString("full_name");
                        }
                    }
                }

                if (waitingResId != -1) {
                    // Chuyển yêu cầu đặt trước sang trạng thái sẵn sàng nhận.
                    try (PreparedStatement ps = conn.prepareStatement(updateRes)) {
                        ps.setInt(1, waitingResId);
                        ps.executeUpdate();
                    }
                    // Giữ nguyên bản sao vừa trả cho người đặt kế tiếp.
                    try (PreparedStatement ps = conn.prepareStatement(updateCopyReserved)) {
                        ps.setString(1, condition);
                        ps.setString(2, note);
                        ps.setString(3, operator);
                        ps.setInt(4, copyId);
                        ps.executeUpdate();
                    }
                    // Tạo lượt chờ nhận tương ứng với thời gian giữ sách 24 giờ.
                    try (PreparedStatement ps = conn.prepareStatement(insertBorrow)) {
                        ps.setInt(1, waitingUserId);
                        ps.setInt(2, bookId);
                        ps.setInt(3, copyId);
                        ps.executeUpdate();
                    }

                    resObj.activatedReservationId = waitingResId;
                    resObj.activatedUserId = waitingUserId;
                    resObj.bookTitle = bookTitle;
                    resObj.userEmail = userEmail;
                    resObj.userFullName = userFullName;
                } else {
                    // Không có người chờ nên đưa bản sao trở lại kho khả dụng.
                    try (PreparedStatement ps = conn.prepareStatement(updateCopyAvailable)) {
                        ps.setString(1, condition);
                        ps.setString(2, note);
                        ps.setString(3, operator);
                        ps.setInt(4, copyId);
                        ps.executeUpdate();
                    }
                    // Đồng bộ số lượng khả dụng tổng hợp của đầu sách.
                    try (PreparedStatement ps = conn.prepareStatement(updateBook)) {
                        ps.setInt(1, bookId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                resObj.success = true;
                return resObj;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Lấy các lượt đang mượn sẽ đến hạn trong khoảng số ngày chỉ định để gửi nhắc nhở.
     *
     * @param days số ngày cảnh báo tính từ ngày hiện tại
     * @return danh sách lượt mượn theo hạn trả gần nhất
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
    public List<BorrowRecord> getNearDueLoans(int days) throws Exception {
        List<BorrowRecord> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT br.id, br.user_id, br.book_id, br.copy_id, br.request_date, br.pickup_deadline, br.pickup_date, br.borrow_date, br.due_date, br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, ")
                .append("u.username, u.full_name, u.email, u.phone, ")
                .append("b.title, b.isbn, b.price, ")
                .append("bc.barcode, bc.book_condition, bc.status AS copy_status ")
                .append("FROM borrow_records br ")
                .append("INNER JOIN users u ON br.user_id = u.id ")
                .append("INNER JOIN books b ON br.book_id = b.id ")
                .append("LEFT JOIN book_copies bc ON br.copy_id = bc.id ")
                .append("WHERE br.status = 'BORROWED' AND br.due_date >= CURDATE() AND br.due_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) ")
                .append("ORDER BY br.due_date ASC");

        try (Connection conn = utils.DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy các lượt đã mang trạng thái quá hạn hoặc có hạn trả trước ngày hiện tại.
     *
     * @return danh sách lượt quá hạn theo hạn trả cũ nhất
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
    public List<BorrowRecord> getOverdueLoans() throws Exception {
        List<BorrowRecord> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT br.id, br.user_id, br.book_id, br.copy_id, br.request_date, br.pickup_deadline, br.pickup_date, br.borrow_date, br.due_date, br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, ")
                .append("u.username, u.full_name, u.email, u.phone, ")
                .append("b.title, b.isbn, b.price, ")
                .append("bc.barcode, bc.book_condition, bc.status AS copy_status ")
                .append("FROM borrow_records br ")
                .append("INNER JOIN users u ON br.user_id = u.id ")
                .append("INNER JOIN books b ON br.book_id = b.id ")
                .append("LEFT JOIN book_copies bc ON br.copy_id = bc.id ")
                .append("WHERE br.status = 'OVERDUE' OR (br.status = 'BORROWED' AND br.due_date < CURDATE()) ")
                .append("ORDER BY br.due_date ASC");

        try (Connection conn = utils.DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Tạo yêu cầu giữ một bản sao khả dụng trong giao dịch có khóa hàng.
     *
     * @param userId mã độc giả
     * @param bookId mã đầu sách
     * @param holdHours số giờ giữ sách
     * @return {@code true} nếu tạo yêu cầu thành công
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    public boolean createPickupRequest(int userId, int bookId, int holdHours) throws Exception {
        String duplicateSql = "SELECT id FROM borrow_records WHERE user_id = ? AND book_id = ? "
                + "AND status IN ('PENDING_PICKUP', 'BORROWED', 'OVERDUE') FOR UPDATE";
        String copySql = "SELECT id FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE' "
                + "ORDER BY id LIMIT 1 FOR UPDATE";
        String insertSql = "INSERT INTO borrow_records (user_id, book_id, copy_id, request_date, "
                + "pickup_deadline, borrow_date, due_date, renewal_count, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? HOUR), NULL, NULL, 0, "
                + "'PENDING_PICKUP', NOW(), NOW())";
        String holdSql = "UPDATE book_copies SET status = 'RESERVED', updated_at = NOW() "
                + "WHERE id = ? AND status = 'AVAILABLE'";
        String decreaseSql = "UPDATE books SET available=GREATEST(0, available-1) WHERE id=?";
        String queueSql = "SELECT id,user_id,status FROM book_reservations WHERE book_id=? "
                + "AND status IN ('WAITING','READY_FOR_PICKUP') ORDER BY CASE "
                + "WHEN status='READY_FOR_PICKUP' THEN 0 ELSE 1 END,created_at,id LIMIT 1 FOR UPDATE";
        String completeReservationSql = "UPDATE book_reservations SET status='COMPLETED',"
                + "updated_at=NOW() WHERE id=?";
        try (Connection connection = DBContext.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(duplicateSql)) {
                    statement.setInt(1, userId);
                    statement.setInt(2, bookId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                    }
                }
                Integer readyReservationId = null;
                try (PreparedStatement statement = connection.prepareStatement(queueSql)) {
                    statement.setInt(1, bookId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            if (!"READY_FOR_PICKUP".equals(resultSet.getString("status"))
                                    || resultSet.getInt("user_id") != userId) {
                                connection.rollback();
                                return false;
                            }
                            readyReservationId = resultSet.getInt("id");
                        }
                    }
                }
                int copyId;
                try (PreparedStatement statement = connection.prepareStatement(copySql)) {
                    statement.setInt(1, bookId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                        copyId = resultSet.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(holdSql)) {
                    statement.setInt(1, copyId);
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setInt(1, userId);
                    statement.setInt(2, bookId);
                    statement.setInt(3, copyId);
                    statement.setInt(4, holdHours);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(decreaseSql)) {
                    statement.setInt(1, bookId);
                    statement.executeUpdate();
                }
                if (readyReservationId != null) {
                    try (PreparedStatement statement = connection.prepareStatement(completeReservationSql)) {
                        statement.setInt(1, readyReservationId);
                        statement.executeUpdate();
                    }
                }
                connection.commit();
                return true;
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Hủy yêu cầu đang chờ nhận thuộc user và giải phóng bản sao trong cùng
     * giao dịch.
     *
     * @param borrowId mã yêu cầu
     * @param userId mã chủ sở hữu
     * @return {@code true} nếu hủy thành công
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    public boolean cancelPickupRequest(int borrowId, int userId) throws Exception {
        return closePendingRequest(borrowId, userId, "CANCELLED");
    }

    /**
     * Xác nhận giao sách đang được giữ và bắt đầu thời hạn mượn 14 ngày.
     *
     * @param borrowId mã yêu cầu
     * @param operator tài khoản thủ thư thao tác
     * @return {@code true} nếu xác nhận thành công
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    public boolean confirmPickup(int borrowId, String operator) throws Exception {
        String selectSql = "SELECT copy_id FROM borrow_records WHERE id = ? AND status = 'PENDING_PICKUP' "
                + "AND pickup_deadline >= NOW() FOR UPDATE";
        String borrowSql = "UPDATE borrow_records SET status='BORROWED', pickup_date=NOW(), "
                + "borrow_date=CURDATE(), due_date=DATE_ADD(CURDATE(), INTERVAL 14 DAY), updated_at=NOW() WHERE id=?";
        String copySql = "UPDATE book_copies SET status='BORROWED', updated_by=?, updated_at=NOW() "
                + "WHERE id=? AND status='RESERVED'";
        try (Connection connection = DBContext.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int copyId;
                try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                    statement.setInt(1, borrowId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                        copyId = resultSet.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(copySql)) {
                    statement.setString(1, operator);
                    statement.setInt(2, copyId);
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(borrowSql)) {
                    statement.setInt(1, borrowId);
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Đóng các yêu cầu quá hạn nhận và trả các bản sao về trạng thái khả dụng.
     *
     * @return số yêu cầu đã hết hạn
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    public int expirePendingRequests() throws Exception {
        String copiesSql = "UPDATE book_copies bc INNER JOIN borrow_records br ON br.copy_id=bc.id "
                + "SET bc.status='AVAILABLE', bc.updated_at=NOW() WHERE br.status='PENDING_PICKUP' "
                + "AND br.pickup_deadline < NOW()";
        String recordsSql = "UPDATE borrow_records SET status='EXPIRED', updated_at=NOW() "
                + "WHERE status='PENDING_PICKUP' AND pickup_deadline < NOW()";
        String booksSql = "UPDATE books b INNER JOIN (SELECT book_id, COUNT(*) quantity "
                + "FROM borrow_records WHERE status='PENDING_PICKUP' AND pickup_deadline < NOW() "
                + "GROUP BY book_id) expired ON expired.book_id=b.id "
                + "SET b.available=b.available+expired.quantity";
        try (Connection connection = DBContext.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement books = connection.prepareStatement(booksSql); PreparedStatement copies = connection.prepareStatement(copiesSql); PreparedStatement records = connection.prepareStatement(recordsSql)) {
                books.executeUpdate();
                copies.executeUpdate();
                int count = records.executeUpdate();
                connection.commit();
                return count;
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Đóng một yêu cầu chờ nhận và giải phóng bản sao trong cùng giao dịch.
     *
     * @param borrowId mã yêu cầu chờ nhận
     * @param userId mã độc giả sở hữu yêu cầu
     * @param targetStatus trạng thái kết thúc cần ghi
     * @return {@code true} nếu yêu cầu hợp lệ và được đóng
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    private boolean closePendingRequest(int borrowId, int userId, String targetStatus) throws Exception {
        String selectSql = "SELECT copy_id, book_id FROM borrow_records WHERE id=? AND user_id=? "
                + "AND status='PENDING_PICKUP' FOR UPDATE";
        String recordSql = "UPDATE borrow_records SET status=?, updated_at=NOW() WHERE id=?";
        String copySql = "UPDATE book_copies SET status='AVAILABLE', updated_at=NOW() "
                + "WHERE id=? AND status='RESERVED'";
        String increaseSql = "UPDATE books SET available=available+1 WHERE id=?";
        try (Connection connection = DBContext.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int copyId;
                int bookId;
                try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                    statement.setInt(1, borrowId);
                    statement.setInt(2, userId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                        copyId = resultSet.getInt(1);
                        bookId = resultSet.getInt(2);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(copySql)) {
                    statement.setInt(1, copyId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(recordSql)) {
                    statement.setString(1, targetStatus);
                    statement.setInt(2, borrowId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(increaseSql)) {
                    statement.setInt(1, bookId);
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Đếm các lượt đang hoạt động của một độc giả để kiểm tra giới hạn mượn.
     *
     * @param userId mã độc giả
     * @return số lượt chờ nhận, đang mượn hoặc quá hạn
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
    public int countActiveByUserId(int userId) throws Exception {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND status IN ('PENDING_PICKUP', 'BORROWED', 'OVERDUE')";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
