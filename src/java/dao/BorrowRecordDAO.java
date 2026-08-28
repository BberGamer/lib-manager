/*
 * DAO quản lý dữ liệu lượt mượn, nhận sách, trả sách và các truy vấn nhắc hạn.
 * Lớp chịu trách nhiệm ánh xạ dữ liệu JDBC và bảo đảm tính nhất quán của các giao dịch mượn trả.
 */
package dao;

import model.BorrowRecord;
import model.Book;
import model.BookCopy;
import model.User;
import java.math.BigDecimal;
import utils.DBContext;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cung cấp các thao tác lưu trữ cho lượt mượn sách và phối hợp cập nhật bản
 * sao, đầu sách,
 * đặt trước trong cùng giao dịch khi trạng thái mượn thay đổi.
 */
public class BorrowRecordDAO {

    /**
     * Chứa kết quả nhận trả và thông tin đặt trước vừa được kích hoạt để controller
     * gửi thông báo.
     */
    public static class ReturnResult {
        public boolean success = false;
        public int activatedReservationId = -1;
        public int activatedUserId = -1;
        public String bookTitle = null;
        public String userEmail = null;
        public String userFullName = null;
    }

    // Câu SELECT nền lấy đầy đủ lượt mượn cùng độc giả, đầu sách và bản sao
    // để tái sử dụng khi đọc dữ liệu.
    private static final String BORROW_RECORD_SELECT = "SELECT br.id, br.user_id, br.book_id, br.copy_id, br.request_date, br.pickup_deadline, "
            + "br.pickup_date, br.borrow_date, br.due_date, "
            + "br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, "
            + "u.username, u.full_name, u.email, u.phone, b.title, b.isbn, b.price, "
            + "bc.barcode, bc.book_condition "
            + "FROM borrow_records br "
            + "INNER JOIN users u ON br.user_id = u.id "
            + "INNER JOIN books b ON br.book_id = b.id "
            + "LEFT JOIN book_copies bc ON br.copy_id = bc.id ";

    /**
     * Ánh xạ hàng kết quả truy vấn thành lượt mượn cùng các đối tượng liên kết nếu
     * truy vấn có trả về.
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
                record.setBookCopy(copy);
            }
        } catch (SQLException ignored) {
        }

        return record;
    }

    /**
     * Tìm các lượt mượn cho trang quản lý theo trạng thái, từ khóa và phân trang.
     *
     * @param status   trạng thái cần lọc hoặc rỗng để lấy tất cả
     * @param keyword  từ khóa theo độc giả, sách hoặc mã bản sao
     * @param pageNum  số trang bắt đầu từ 1
     * @param pageSize số bản ghi tối đa trên một trang
     * @return danh sách lượt mượn mới nhất trước
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
    public List<BorrowRecord> searchBorrowRecords(String status, String keyword, int pageNum, int pageSize)
            throws Exception {
        List<BorrowRecord> list = new ArrayList<>();
        // Tạo câu SQL động để lọc lượt mượn theo trạng thái/từ khóa
        // và chỉ lấy dữ liệu của trang hiện tại.
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT br.id, br.user_id, br.book_id, br.copy_id, br.request_date, br.pickup_deadline, br.pickup_date, br.borrow_date, br.due_date, br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, ")
                .append("u.username, u.full_name, u.email, u.phone, ")
                .append("b.title, b.isbn, b.price, ")
                .append("bc.barcode, bc.book_condition, ")
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

    /**
     * Đếm tổng số lượt mượn khớp bộ lọc để tính phân trang quản lý.
     *
     * @param status  trạng thái cần lọc hoặc rỗng để lấy tất cả
     * @param keyword từ khóa theo độc giả, sách hoặc mã bản sao
     * @return tổng số bản ghi phù hợp
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
    public int countBorrowRecords(String status, String keyword) throws Exception {
        // Tạo câu SQL đếm theo cùng bộ lọc của truy vấn danh sách
        // để tính đúng tổng số trang quản lý.
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
        // Lấy một lượt mượn theo ID cùng thông tin người mượn, sách và bản sao
        // để hiển thị hoặc xử lý nghiệp vụ.
        String sql = "SELECT br.id, br.user_id, br.book_id, br.copy_id, br.request_date, br.pickup_deadline, br.pickup_date, br.borrow_date, br.due_date, br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, "
                + "u.username, u.full_name, u.email, u.phone, "
                + "b.title, b.isbn, b.price, "
                + "bc.barcode, bc.book_condition "
                + "FROM borrow_records br "
                + "INNER JOIN users u ON br.user_id = u.id "
                + "INNER JOIN books b ON br.book_id = b.id "
                + "LEFT JOIN book_copies bc ON br.copy_id = bc.id "
                + "WHERE br.id = ?";
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
     * Lấy toàn bộ lượt mượn của một độc giả để hiển thị trang cá nhân.(lấy danh
     * sách sách mượn của người dùng)
     *
     * @param userId mã người dùng đã được controller xác thực
     * @return danh sách lượt mượn mới nhất trước, không bao giờ trả về
     *         {@code null}
     * @throws Exception khi không thể truy vấn cơ sở dữ liệu
     */
    public List<BorrowRecord> findByUserId(int userId) throws Exception {
        // Lấy toàn bộ lượt mượn của một độc giả, ưu tiên các lượt còn hoạt động
        // trước lịch sử đã kết thúc.
        String sql = BORROW_RECORD_SELECT
                + "WHERE br.user_id = ? "
                + "ORDER BY CASE WHEN br.status IN ('PENDING_PICKUP', 'BORROWED', 'OVERDUE') THEN 0 ELSE 1 END, "
                + "br.created_at DESC";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
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
     * Đọc các lượt đang chiếm bản sao để service dựng lịch theo khoảng thời gian.
     *
     * @param connection kết nối dùng để đọc cùng một ảnh chụp giao dịch
     * @param bookId mã đầu sách
     * @return các lượt chờ nhận, đang mượn hoặc quá hạn còn hiệu lực
     * @throws SQLException khi không thể đọc dữ liệu mượn
     */
    public List<BorrowRecord> findSchedulingBorrows(Connection connection, int bookId)
            throws SQLException {
        String sql = "SELECT id,user_id,book_id,copy_id,request_date,pickup_deadline,"
                + "borrow_date,due_date,return_date,renewal_count,status "
                + "FROM borrow_records WHERE book_id=? AND "
                + "((status='PENDING_PICKUP' AND pickup_deadline>=NOW()) OR "
                + "(status IN ('BORROWED','OVERDUE') AND return_date IS NULL)) "
                + "ORDER BY due_date,id";
        List<BorrowRecord> records = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    records.add(mapSchedulingBorrow(result));
                }
            }
        }
        return records;
    }

    /**
     * Kiểm tra độc giả đang có lượt mượn hoặc chờ nhận của cùng đầu sách hay không.
     *
     * @param userId mã độc giả
     * @param bookId mã đầu sách
     * @return {@code true} khi tồn tại lượt còn hiệu lực
     * @throws Exception khi không thể truy vấn dữ liệu
     */
    public boolean hasActiveForUserAndBook(int userId, int bookId) throws Exception {
        try (Connection connection = DBContext.getInstance().getConnection()) {
            return hasActiveForUserAndBook(connection, userId, bookId);
        }
    }

    /**
     * Kiểm tra lượt mượn trùng trong kết nối do service đang điều phối.
     *
     * @param connection kết nối đang tham gia giao dịch
     * @param userId mã độc giả
     * @param bookId mã đầu sách
     * @return {@code true} khi tồn tại lượt còn hiệu lực
     * @throws SQLException khi không thể truy vấn dữ liệu
     */
    public boolean hasActiveForUserAndBook(Connection connection, int userId, int bookId)
            throws SQLException {
        String sql = "SELECT 1 FROM borrow_records WHERE user_id=? AND book_id=? AND "
                + "((status='PENDING_PICKUP' AND pickup_deadline>=NOW()) OR "
                + "(status IN ('BORROWED','OVERDUE') AND return_date IS NULL)) LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, bookId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /**
     * Đọc lượt mượn dùng cho quyết định gia hạn, có thể khóa hàng khi caller chuẩn bị cập nhật.
     *
     * @param connection kết nối đang tham gia giao dịch
     * @param borrowRecordId mã lượt mượn
     * @param userId mã độc giả sở hữu lượt mượn
     * @param lockForUpdate có thêm khóa ghi vào lượt mượn hay không
     * @return lượt mượn phù hợp hoặc {@code null} khi không tồn tại
     * @throws SQLException khi không thể đọc dữ liệu
     */
    public BorrowRecord findRenewalCandidate(Connection connection, int borrowRecordId,
            int userId, boolean lockForUpdate) throws SQLException {
        String sql = "SELECT id,user_id,book_id,copy_id,request_date,pickup_deadline,"
                + "borrow_date,due_date,return_date,renewal_count,status "
                + "FROM borrow_records WHERE id=? AND user_id=?"
                + (lockForUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, borrowRecordId);
            statement.setInt(2, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapSchedulingBorrow(result) : null;
            }
        }
    }

    /**
     * Ánh xạ tập cột rút gọn dùng cho lịch slot và giao dịch gia hạn.
     *
     * @param result hàng kết quả hiện tại
     * @return lượt mượn chứa các mốc thời gian cần thiết
     * @throws SQLException khi không thể đọc cột bắt buộc
     */
    private BorrowRecord mapSchedulingBorrow(ResultSet result) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(result.getInt("id"));
        record.setUserId(result.getInt("user_id"));
        record.setBookId(result.getInt("book_id"));
        int copyId = result.getInt("copy_id");
        record.setCopyId(result.wasNull() ? null : copyId);
        Timestamp requestDate = result.getTimestamp("request_date");
        Timestamp pickupDeadline = result.getTimestamp("pickup_deadline");
        Date borrowDate = result.getDate("borrow_date");
        Date dueDate = result.getDate("due_date");
        Date returnDate = result.getDate("return_date");
        record.setRequestDate(requestDate == null ? null : requestDate.toLocalDateTime());
        record.setPickupDeadline(
                pickupDeadline == null ? null : pickupDeadline.toLocalDateTime());
        record.setBorrowDate(borrowDate == null ? null : borrowDate.toLocalDate());
        record.setDueDate(dueDate == null ? null : dueDate.toLocalDate());
        record.setReturnDate(returnDate == null ? null : returnDate.toLocalDate());
        record.setRenewalCount(result.getInt("renewal_count"));
        record.setStatus(result.getString("status"));
        return record;
    }

    /**
     * Đồng bộ các lượt chưa trả đã qua hạn từ trạng thái đang mượn sang quá
     * hạn.(cập nhật sách quá hạn.)
     *
     * @return số lượt mượn được chuyển sang trạng thái {@code OVERDUE}
     * @throws Exception khi không thể cập nhật cơ sở dữ liệu
     */
    public int markOverdueBorrows() throws Exception {
        // Chuyển mọi lượt BORROWED đã qua ngày phải trả sang OVERDUE
        // để trạng thái lưu trữ phản ánh đúng hạn mượn.
        String sql = "UPDATE borrow_records "
                + "SET status = 'OVERDUE', updated_at = NOW() "
                + "WHERE status = 'BORROWED' AND due_date < CURDATE()";
        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        }
    }

    /**
     * Mở kết nối để service điều phối giao dịch báo mất qua nhiều DAO.
     *
     * @return kết nối JDBC đang mở
     * @throws SQLException           khi không thể kết nối cơ sở dữ liệu
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public Connection openTransactionConnection() throws SQLException, ClassNotFoundException {
        return DBContext.getInstance().getConnection();
    }

    /**
     * Ghi nhận một bản sao bị mất theo yêu cầu của chính độc giả đang mượn.
     * Lượt mượn, bản sao và đầu sách được khóa rồi cập nhật nhưng phương thức không
     * tự
     * commit hoặc rollback để service có thể tạo vé phạt trong cùng giao dịch.
     *
     * @param connection     kết nối đang tham gia giao dịch nghiệp vụ
     * @param borrowRecordId mã lượt mượn cần báo mất
     * @param userId         mã độc giả sở hữu lượt mượn
     * @param operator       tài khoản ghi nhận thay đổi bản sao
     * @return dữ liệu để tạo vé phạt, hoặc {@code null} khi lượt mượn không hợp lệ
     * @throws SQLException khi không thể cập nhật dữ liệu
     */
    public LostReportDetails reportLostForUser(Connection connection, int borrowRecordId,
            int userId, String operator) throws SQLException {
        // Khóa lượt mượn và bản sao hợp lệ của đúng độc giả,
        // đồng thời đọc giá sách để lập khoản phạt mất sách.
        String selectSql = "SELECT br.book_id,br.copy_id,b.price FROM borrow_records br "
                + "INNER JOIN book_copies bc ON bc.id=br.copy_id "
                + "INNER JOIN books b ON b.id=br.book_id AND b.is_deleted=0 "
                + "WHERE br.id=? AND br.user_id=? AND br.status IN ('BORROWED','OVERDUE') "
                + "AND br.return_date IS NULL AND bc.is_deleted=0 "
                + "AND bc.book_condition<>'LOST' FOR UPDATE";
        // Đóng lượt mượn bằng ngày hiện tại và trạng thái LOST
        // để lượt này không còn được xem là đang mượn.
        String borrowSql = "UPDATE borrow_records SET return_date=CURDATE(),status='LOST',updated_at=NOW() "
                + "WHERE id=? AND user_id=? AND status IN ('BORROWED','OVERDUE') "
                + "AND return_date IS NULL";
        // Đánh dấu bản sao vật lý bị mất và xóa mềm
        // để bản sao không thể tiếp tục được lưu thông.
        String copySql = "UPDATE book_copies SET book_condition='LOST',is_deleted=1,"
                + "updated_by=?,updated_at=NOW() "
                + "WHERE id=? AND is_deleted=0 AND book_condition<>'LOST'";
        // Giảm tổng số lượng và số lượng khả dụng của đầu sách sau khi mất bản sao,
        // đồng thời không cho giá trị âm.
        String bookSql = "UPDATE books SET available=GREATEST(0,available-1),"
                + "quantity=GREATEST(0,quantity-1),updated_by=?,updated_at=NOW() "
                + "WHERE id=? AND is_deleted=0";

        int bookId;
        int copyId;
        Integer bookPrice;
        try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setInt(1, borrowRecordId);
            statement.setInt(2, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                bookId = result.getInt("book_id");
                copyId = result.getInt("copy_id");
                int storedPrice = result.getInt("price");
                bookPrice = result.wasNull() ? null : storedPrice;
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(borrowSql)) {
            statement.setInt(1, borrowRecordId);
            statement.setInt(2, userId);
            if (statement.executeUpdate() != 1) {
                return null;
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(copySql)) {
            statement.setString(1, operator);
            statement.setInt(2, copyId);
            if (statement.executeUpdate() != 1) {
                return null;
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(bookSql)) {
            statement.setString(1, operator);
            statement.setInt(2, bookId);
            if (statement.executeUpdate() != 1) {
                return null;
            }
        }
        return new LostReportDetails(borrowRecordId, userId, bookPrice);
    }

    /**
     * Dữ liệu bất biến của lượt báo mất dùng để service tạo vé phạt đúng người và
     * đúng giá sách.
     */
    public static final class LostReportDetails {

        private final int borrowRecordId;
        private final int userId;
        private final Integer bookPrice;

        /**
         * Khởi tạo dữ liệu của lượt báo mất đã được khóa và cập nhật.
         *
         * @param borrowRecordId mã lượt mượn
         * @param userId         mã độc giả
         * @param bookPrice      giá sách lưu tại thời điểm báo mất
         */
        public LostReportDetails(int borrowRecordId, int userId, Integer bookPrice) {
            this.borrowRecordId = borrowRecordId;
            this.userId = userId;
            this.bookPrice = bookPrice;
        }

        /** @return mã lượt mượn dùng để liên kết vé phạt */
        public int getBorrowRecordId() {
            return borrowRecordId;
        }

        /** @return mã độc giả chịu khoản phạt */
        public int getUserId() {
            return userId;
        }

        /** @return giá sách dùng làm 100% số tiền phạt, có thể chưa được khai báo */
        public Integer getBookPrice() {
            return bookPrice;
        }
    }

    /**
     * Cập nhật hạn trả của lượt đã được khóa và được service xác nhận còn đủ điều kiện.
     *
     * @param connection kết nối đang giữ khóa lượt mượn và đầu sách
     * @param record lượt mượn chứa hạn trả cùng số lần gia hạn hiện tại
     * @param proposedDueDate hạn trả mới đã được service tính
     * @return {@code true} khi cập nhật đúng một lượt và dữ liệu chưa thay đổi
     * @throws SQLException khi không thể cập nhật dữ liệu
     */
    public boolean renewLocked(Connection connection, BorrowRecord record,
            LocalDate proposedDueDate) throws SQLException {
        String sql = "UPDATE borrow_records SET due_date=?,renewal_count=renewal_count+1,"
                + "updated_at=NOW() WHERE id=? AND user_id=? AND status='BORROWED' "
                + "AND return_date IS NULL AND due_date=? AND renewal_count=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(proposedDueDate));
            statement.setInt(2, record.getId());
            statement.setInt(3, record.getUserId());
            statement.setDate(4, Date.valueOf(record.getDueDate()));
            statement.setInt(5, record.getRenewalCount());
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Xác nhận trả sách và chuyển bản sao cho người đặt kế tiếp hoặc trả về kho(xác
     * nhận trả sách.)
     * trong một giao dịch.
     * Khoản phạt liên quan vẫn giữ nguyên trạng thái; chỉ luồng đóng phạt mới được
     * cập nhật thanh toán.
     *
     * @param id        mã lượt mượn cần nhận trả
     * @param operator  tài khoản nhân viên nhận sách
     * @param condition tình trạng bản sao khi nhận lại
     * @param note      ghi chú kiểm tra bản sao
     * @return kết quả nhận trả và dữ liệu người đặt kế tiếp nếu có
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    public ReturnResult confirmReturn(int id, String operator, String condition, String note) throws Exception {
        ReturnResult resObj = new ReturnResult();
        resObj.success = false;

        // Khóa lượt đang mượn hoặc quá hạn để lấy đúng đầu sách, bản sao, độc giả và giá sách
        // trước khi thực hiện quy trình trả.
        String selectRecord = "SELECT br.book_id, br.copy_id, br.user_id, b.price, b.title "
                + "FROM borrow_records br "
                + "INNER JOIN books b ON br.book_id = b.id "
                + "WHERE br.id = ? AND br.status IN ('BORROWED', 'OVERDUE') FOR UPDATE";
        // Hoàn tất lượt mượn bằng cách ghi ngày trả và chuyển trạng thái tương ứng.
        String updateRecord = "UPDATE borrow_records SET return_date = CURDATE(), status = ?, updated_at = NOW() WHERE id = ?";

        // Khóa reservation WAITING hợp lệ đầu tiên của đầu sách
        // để cấp quyền nhận sách cho đúng người kế tiếp.
        String selectReservation = "SELECT r.id, r.user_id, b.title, u.email, u.full_name "
                + "FROM book_reservations r "
                + "INNER JOIN books b ON r.book_id = b.id "
                + "INNER JOIN users u ON r.user_id = u.id "
                + "WHERE r.book_id = ? AND r.status = 'WAITING' "
                + "AND (r.expected_pickup_date IS NULL OR r.expected_pickup_date<=CURDATE()) "
                + "AND NOT EXISTS (SELECT 1 FROM fines f WHERE f.user_id = r.user_id AND f.status = 'UNPAID') "
                + "AND (SELECT COUNT(*) FROM borrow_records br WHERE br.user_id = r.user_id AND "
                + "((br.status = 'PENDING_PICKUP' AND br.pickup_deadline >= NOW()) OR "
                + "(br.status IN ('BORROWED', 'OVERDUE') AND br.return_date IS NULL))) < 3 "
                + "ORDER BY r.expected_pickup_date, r.created_at, r.id LIMIT 1 FOR UPDATE";

        // Chuyển reservation kế tiếp sang READY_FOR_PICKUP
        // và thiết lập thời hạn nhận sách trong 24 giờ.
        String updateRes = "UPDATE book_reservations SET status='READY_FOR_PICKUP',"
                + "expected_pickup_date=GREATEST(COALESCE(expected_pickup_date,CURDATE()),"
                + "CURDATE()),notified_at=NOW(),"
                + "expiry_date=DATE_ADD(NOW(),INTERVAL 24 HOUR),updated_at=NOW() WHERE id=?";

        // Lưu tình trạng thực tế và ghi chú của bản sao vừa được thủ thư nhận lại.
        String updateCopyCondition = "UPDATE book_copies SET book_condition = ?, note = ?, updated_by = ?, updated_at = NOW() WHERE id = ?";

        // Đặt bản sao bị mất/hỏng nặng thành đã bị loại bỏ (is_deleted = 1).
        String updateCopyLostOrDamaged = "UPDATE book_copies SET book_condition = ?, is_deleted = 1, note = ?, updated_by = ?, updated_at = NOW() WHERE id = ?";

        // Giảm tổng số lượng và số lượng khả dụng của đầu sách sau khi mất/hỏng nặng bản sao.
        String updateBookLostOrDamaged = "UPDATE books SET available = GREATEST(0, available - 1), "
                + "quantity = GREATEST(0, quantity - 1), updated_by = ?, updated_at = NOW() "
                + "WHERE id = ? AND is_deleted = 0";

        // Tạo lượt PENDING_PICKUP cho người đặt kế tiếp
        // nhưng chưa gán bản sao cho đến lúc xác nhận giao sách.
        String insertBorrow = "INSERT INTO borrow_records (user_id, book_id, copy_id, request_date, "
                + "pickup_deadline, borrow_date, due_date, renewal_count, status, created_at, updated_at) "
                + "VALUES (?, ?, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 24 HOUR), NULL, NULL, 0, "
                + "'PENDING_PICKUP', NOW(), NOW())";

        try (Connection conn = DBContext.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bookId = -1;
                int copyId = -1;
                int userId = -1;
                int price = 0;
                String bookTitle = null;
                try (PreparedStatement ps = conn.prepareStatement(selectRecord)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            bookId = rs.getInt("book_id");
                            copyId = rs.getInt("copy_id");
                            userId = rs.getInt("user_id");
                            price = rs.getInt("price");
                            bookTitle = rs.getString("title");
                        }
                    }
                }
                if (bookId == -1 || copyId == -1) {
                    conn.rollback();
                    return resObj;
                }
                boolean isBorrowableCondition = "GOOD".equals(condition) || "WORN".equals(condition);
                String recordStatus = "LOST".equals(condition) ? "LOST" : "RETURNED";

                // Cập nhật lượt mượn
                try (PreparedStatement ps = conn.prepareStatement(updateRecord)) {
                    ps.setString(1, recordStatus);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }

                // Cập nhật bản sao và đầu sách tương ứng
                if ("LOST".equals(condition) || "DAMAGED".equals(condition)) {
                    try (PreparedStatement ps = conn.prepareStatement(updateCopyLostOrDamaged)) {
                        ps.setString(1, condition);
                        ps.setString(2, note);
                        ps.setString(3, operator);
                        ps.setInt(4, copyId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(updateBookLostOrDamaged)) {
                        ps.setString(1, operator);
                        ps.setInt(2, bookId);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(updateCopyCondition)) {
                        ps.setString(1, condition);
                        ps.setString(2, note);
                        ps.setString(3, operator);
                        ps.setInt(4, copyId);
                        ps.executeUpdate();
                    }
                }

                // Tự động lập phiếu phạt nếu có hư hỏng hoặc mất mát
                if ("LOST".equals(condition) || "DAMAGED".equals(condition) || "WORN".equals(condition)) {
                    BigDecimal fineAmount;
                    String reason;
                    if ("LOST".equals(condition)) {
                        fineAmount = BigDecimal.valueOf(price);
                        reason = "Bồi thường 100% giá sách do làm mất cuốn sách: " + bookTitle;
                    } else if ("DAMAGED".equals(condition)) {
                        fineAmount = BigDecimal.valueOf(price);
                        reason = "Bồi thường 100% giá sách do làm hỏng nặng cuốn sách: " + bookTitle;
                    } else {
                        fineAmount = BigDecimal.valueOf(Math.round(price * 0.3));
                        reason = "Bồi thường 30% giá sách do làm hỏng nhẹ cuốn sách: " + bookTitle;
                    }

                    String insertFineSql = "INSERT INTO fines (borrow_record_id, user_id, amount, overdue_days, "
                            + "book_condition, fine_type, reason, status, created_at, updated_at) "
                            + "SELECT ?, ?, ?, 0, ?, 'BOOK_CONDITION', ?, 'UNPAID', NOW(), NOW() "
                            + "WHERE NOT EXISTS (SELECT 1 FROM fines WHERE borrow_record_id = ? AND fine_type = 'BOOK_CONDITION')";
                    try (PreparedStatement ps = conn.prepareStatement(insertFineSql)) {
                        ps.setInt(1, id);
                        ps.setInt(2, userId);
                        ps.setBigDecimal(3, fineAmount);
                        ps.setString(4, condition);
                        ps.setString(5, reason);
                        ps.setInt(6, id);
                        ps.executeUpdate();
                    }
                }

                // Khóa người đặt hợp lệ đầu tiên để tránh hai giao dịch cùng cấp một slot chờ
                // nhận.
                int waitingResId = -1;
                int waitingUserId = -1;
                String waitingBookTitle = null;
                String userEmail = null;
                String userFullName = null;
                if (isBorrowableCondition) {
                    try (PreparedStatement ps = conn.prepareStatement(selectReservation)) {
                        ps.setInt(1, bookId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                waitingResId = rs.getInt("id");
                                waitingUserId = rs.getInt("user_id");
                                waitingBookTitle = rs.getString("title");
                                userEmail = rs.getString("email");
                                userFullName = rs.getString("full_name");
                            }
                        }
                    }
                }

                if (waitingResId != -1) {
                    // Chuyển yêu cầu đặt trước sang trạng thái sẵn sàng nhận.
                    try (PreparedStatement ps = conn.prepareStatement(updateRes)) {
                        ps.setInt(1, waitingResId);
                        ps.executeUpdate();
                    }
                    // Tạo lượt chờ nhận tương ứng với thời gian giữ sách 24 giờ.
                    try (PreparedStatement ps = conn.prepareStatement(insertBorrow)) {
                        ps.setInt(1, waitingUserId);
                        ps.setInt(2, bookId);
                        ps.executeUpdate();
                    }

                    resObj.activatedReservationId = waitingResId;
                    resObj.activatedUserId = waitingUserId;
                    resObj.bookTitle = waitingBookTitle;
                    resObj.userEmail = userEmail;
                    resObj.userFullName = userFullName;
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
     * Lấy các lượt đang mượn sẽ đến hạn trong khoảng số ngày chỉ định để gửi nhắc
     * nhở.
     *
     * @param days số ngày cảnh báo tính từ ngày hiện tại
     * @return danh sách lượt mượn theo hạn trả gần nhất
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
    public List<BorrowRecord> getNearDueLoans(int days) throws Exception {
        List<BorrowRecord> list = new ArrayList<>();
        // Lấy các lượt BORROWED có hạn trả từ hôm nay đến số ngày cảnh báo,
        // ưu tiên hạn gần nhất.
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT br.id, br.user_id, br.book_id, br.copy_id, br.request_date, br.pickup_deadline, br.pickup_date, br.borrow_date, br.due_date, br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, ")
                .append("u.username, u.full_name, u.email, u.phone, ")
                .append("b.title, b.isbn, b.price, ")
                .append("bc.barcode, bc.book_condition ")
                .append("FROM borrow_records br ")
                .append("INNER JOIN users u ON br.user_id = u.id ")
                .append("INNER JOIN books b ON br.book_id = b.id ")
                .append("LEFT JOIN book_copies bc ON br.copy_id = bc.id ")
                .append("WHERE br.status = 'BORROWED' AND br.due_date >= CURDATE() AND br.due_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) ")
                .append("ORDER BY br.due_date ASC");

        try (Connection conn = utils.DBContext.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sb.toString())) {
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
        // Lấy các lượt đã mang trạng thái OVERDUE hoặc vẫn BORROWED
        // nhưng ngày trả đã qua để xử lý đồng bộ trễ.
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT br.id, br.user_id, br.book_id, br.copy_id, br.request_date, br.pickup_deadline, br.pickup_date, br.borrow_date, br.due_date, br.return_date, br.renewal_count, br.status, br.note, br.created_at, br.updated_at, ")
                .append("u.username, u.full_name, u.email, u.phone, ")
                .append("b.title, b.isbn, b.price, ")
                .append("bc.barcode, bc.book_condition ")
                .append("FROM borrow_records br ")
                .append("INNER JOIN users u ON br.user_id = u.id ")
                .append("INNER JOIN books b ON br.book_id = b.id ")
                .append("LEFT JOIN book_copies bc ON br.copy_id = bc.id ")
                .append("WHERE br.status = 'OVERDUE' OR (br.status = 'BORROWED' AND br.due_date < CURDATE()) ")
                .append("ORDER BY br.due_date ASC");

        try (Connection conn = utils.DBContext.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Thêm yêu cầu giữ sách bằng kết nối giao dịch do service quản lý.
     * Service phải khóa đầu sách và kiểm tra lịch sức chứa trước khi gọi phương thức này.
     *
     * @param connection kết nối đang tham gia giao dịch
     * @param userId mã độc giả
     * @param bookId mã đầu sách
     * @param holdHours số giờ giữ sách
     * @return {@code true} khi thêm đúng một yêu cầu
     * @throws SQLException khi không thể ghi dữ liệu
     */
    public boolean insertPickupRequest(Connection connection, int userId, int bookId,
            int holdHours) throws SQLException {
        String sql = "INSERT INTO borrow_records (user_id, book_id, copy_id, request_date, "
                + "pickup_deadline, borrow_date, due_date, renewal_count, status, created_at, updated_at) "
                + "VALUES (?, ?, NULL, NOW(), DATE_ADD(NOW(), INTERVAL ? HOUR), NULL, NULL, 0, "
                + "'PENDING_PICKUP', NOW(), NOW())";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, bookId);
            statement.setInt(3, holdHours);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Hủy yêu cầu đang chờ nhận thuộc user và giải phóng bản sao trong cùng (xác
     * nhận giao sách và chuyển trạng thái thành BORROWED)
     * giao dịch.
     *
     * @param borrowId mã yêu cầu
     * @param userId   mã chủ sở hữu
     * @return {@code true} nếu hủy thành công
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    public boolean cancelPickupRequest(int borrowId, int userId) throws Exception {
        return closePendingRequest(borrowId, userId, "CANCELLED");
    }

    /**
     * Xác nhận giao sách từ slot chờ nhận, gán bản sao thực tế và bắt đầu thời hạn
     * mượn do service truyền vào.
     *
     * @param borrowId       mã yêu cầu mượn trả
     * @param copyId         mã bản sao sách thực tế được giao
     * @param operator       tài khoản thủ thư thao tác
     * @param loanPeriodDays số ngày mượn được áp dụng
     * @return {@code true} nếu xác nhận thành công
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    public boolean confirmPickup(int borrowId, int copyId, String operator,
            int loanPeriodDays) throws Exception {
        if (borrowId <= 0 || copyId <= 0 || loanPeriodDays <= 0) {
            return false;
        }
        // Khóa yêu cầu chờ nhận và xác minh bản sao cùng đầu sách đang đủ điều kiện,
        // chưa bị lượt khác chiếm dụng.
        String selectSql = "SELECT br.user_id, br.book_id FROM borrow_records br "
                + "INNER JOIN book_copies bc ON bc.id=? AND bc.book_id=br.book_id "
                + "WHERE br.id=? AND br.status='PENDING_PICKUP' AND br.pickup_deadline>=NOW() "
                + "AND bc.is_deleted=0 AND bc.book_condition IN ('GOOD','WORN') "
                + "AND br.copy_id IS NULL "
                + "AND NOT EXISTS (SELECT 1 FROM borrow_records occupied_borrow "
                + "WHERE occupied_borrow.copy_id=bc.id AND occupied_borrow.id<>br.id AND "
                + "((occupied_borrow.status='PENDING_PICKUP' "
                + "AND occupied_borrow.pickup_deadline>=NOW()) OR "
                + "(occupied_borrow.status IN ('BORROWED','OVERDUE') "
                + "AND occupied_borrow.return_date IS NULL))) FOR UPDATE";
        // Gán bản sao thực tế, chuyển yêu cầu sang BORROWED
        // và thiết lập ngày mượn cùng hạn trả theo chính sách.
        String borrowSql = "UPDATE borrow_records SET copy_id=?,status='BORROWED',pickup_date=NOW(),"
                + "borrow_date=CURDATE(),due_date=DATE_ADD(CURDATE(),INTERVAL ? DAY),"
                + "updated_at=NOW() WHERE id=? AND status='PENDING_PICKUP'";
        // Hoàn tất reservation READY_FOR_PICKUP tương ứng sau khi sách đã được giao thành công.
        String completeReservationSql = "UPDATE book_reservations SET status='COMPLETED', "
                + "updated_at=NOW() WHERE user_id=? AND book_id=? AND status='READY_FOR_PICKUP'";
        try (Connection connection = DBContext.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int userId;
                int bookId;
                try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                    statement.setInt(1, copyId);
                    statement.setInt(2, borrowId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                        userId = resultSet.getInt("user_id");
                        bookId = resultSet.getInt("book_id");
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(borrowSql)) {
                    statement.setInt(1, copyId);
                    statement.setInt(2, loanPeriodDays);
                    statement.setInt(3, borrowId);
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(completeReservationSql)) {
                    statement.setInt(1, userId);
                    statement.setInt(2, bookId);
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
        // Đóng các yêu cầu mượn chờ nhận đã quá thời hạn
        // để giải phóng slot mượn của đầu sách.
        String recordsSql = "UPDATE borrow_records SET status='EXPIRED', updated_at=NOW() "
                + "WHERE status='PENDING_PICKUP' AND pickup_deadline < NOW()";
        // Đồng bộ reservation sẵn sàng nhận đã quá hạn sang EXPIRED trong cùng giao dịch.
        String reservationsSql = "UPDATE book_reservations SET status='EXPIRED', updated_at=NOW() "
                + "WHERE status='READY_FOR_PICKUP' AND expiry_date < NOW()";
        try (Connection connection = DBContext.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement records = connection.prepareStatement(recordsSql);
                    PreparedStatement reservations = connection.prepareStatement(reservationsSql)) {
                int expiredRecords = records.executeUpdate();
                reservations.executeUpdate();
                connection.commit();
                return expiredRecords;
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
     * @param borrowId     mã yêu cầu chờ nhận
     * @param userId       mã độc giả sở hữu yêu cầu
     * @param targetStatus trạng thái kết thúc cần ghi
     * @return {@code true} nếu yêu cầu hợp lệ và được đóng
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    private boolean closePendingRequest(int borrowId, int userId, String targetStatus) throws Exception {
        // Khóa yêu cầu PENDING_PICKUP thuộc đúng người dùng
        // và lấy book_id để đồng bộ reservation liên quan.
        String selectSql = "SELECT book_id FROM borrow_records WHERE id=? AND user_id=? "
                + "AND status='PENDING_PICKUP' FOR UPDATE";
        // Chuyển yêu cầu mượn sang trạng thái kết thúc do caller chỉ định, chẳng hạn CANCELLED.
        String recordSql = "UPDATE borrow_records SET status=?, updated_at=NOW() WHERE id=?";
        // Chuyển reservation READY_FOR_PICKUP của cùng người và đầu sách
        // sang cùng trạng thái kết thúc.
        String reservationSql = "UPDATE book_reservations SET status=?, updated_at=NOW() "
                + "WHERE user_id=? AND book_id=? AND status='READY_FOR_PICKUP'";
        try (Connection connection = DBContext.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int bookId;
                try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                    statement.setInt(1, borrowId);
                    statement.setInt(2, userId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                        bookId = resultSet.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(recordSql)) {
                    statement.setString(1, targetStatus);
                    statement.setInt(2, borrowId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(reservationSql)) {
                    statement.setString(1, targetStatus);
                    statement.setInt(2, userId);
                    statement.setInt(3, bookId);
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
        try (Connection conn = DBContext.getInstance().getConnection()) {
            return countActiveByUserId(conn, userId);
        }
    }

    /**
     * Đếm lượt mượn còn hoạt động bằng kết nối giao dịch hiện tại.
     *
     * @param connection kết nối đang tham gia giao dịch
     * @param userId mã độc giả
     * @return số lượt chờ nhận, đang mượn hoặc quá hạn
     * @throws SQLException khi không thể truy vấn dữ liệu
     */
    public int countActiveByUserId(Connection connection, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id=? AND "
                + "((status='PENDING_PICKUP' AND pickup_deadline>=NOW()) OR "
                + "(status IN ('BORROWED','OVERDUE') AND return_date IS NULL))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }
}
