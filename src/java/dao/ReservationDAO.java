package dao;

import model.ReservationRecord;
import model.Book;
import model.User;
import utils.DBContext;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy cập dữ liệu yêu cầu đặt trước, bao gồm hàng chờ, lịch nhận dự kiến
 * và các thao tác thay đổi trạng thái reservation.
 */
public class ReservationDAO {

    /** Điều kiện loại bản sao đang được giữ hoặc chưa được hoàn trả. */
    private static final String ACTIVE_BORROW_CONFLICT_SQL = "SELECT 1 FROM borrow_records br "
            + "WHERE br.copy_id = bc.id AND ((br.status = 'PENDING_PICKUP' "
            + "AND br.pickup_deadline >= NOW()) OR (br.status IN ('BORROWED', 'OVERDUE') "
            + "AND br.return_date IS NULL))";

    /** Biểu thức tính vị trí động của reservation đang chờ trong từng đầu sách. */
    private static final String QUEUE_POSITION_SQL = "CASE WHEN r.status='WAITING' THEN "
            + "(SELECT COUNT(*) FROM book_reservations earlier "
            + "WHERE earlier.book_id=r.book_id "
            + "AND earlier.status IN ('WAITING','READY_FOR_PICKUP') "
            + "AND (earlier.created_at<r.created_at OR (earlier.created_at=r.created_at "
            + "AND earlier.id<=r.id))) ELSE 0 END";

    private ReservationRecord mapRow(ResultSet rs) throws SQLException {
        ReservationRecord record = new ReservationRecord();
        record.setId(rs.getInt("id"));
        record.setBookId(rs.getInt("book_id"));
        record.setUserId(rs.getInt("user_id"));

        Timestamp rDate = rs.getTimestamp("reserve_date");
        if (rDate != null) {
            record.setReserveDate(rDate.toLocalDateTime());
        }

        Date requestedDate = rs.getDate("requested_pickup_date");
        if (requestedDate != null) {
            record.setRequestedPickupDate(requestedDate.toLocalDate());
        }

        Date expectedDate = rs.getDate("expected_pickup_date");
        if (expectedDate != null) {
            record.setExpectedPickupDate(expectedDate.toLocalDate());
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

        Timestamp delayAt = rs.getTimestamp("delay_notified_at");
        if (delayAt != null) {
            record.setDelayNotifiedAt(delayAt.toLocalDateTime());
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

    /**
     * Tìm một trang reservation cho màn hình quản lý và tính vị trí hàng chờ động.
     *
     * @param status trạng thái cần lọc, để trống để lấy mọi trạng thái
     * @param keyword từ khóa tên độc giả, tài khoản hoặc tên sách
     * @param sortOrder thứ tự ưu tiên {@code ASC}, {@code DESC} hoặc {@code NEWEST}
     * @param pageNum số trang bắt đầu từ 1
     * @param pageSize số bản ghi tối đa trên một trang
     * @return danh sách reservation phù hợp với bộ lọc
     * @throws Exception khi không thể truy vấn dữ liệu
     */
    public List<ReservationRecord> searchReservations(String status, String keyword,
            String sortOrder, int pageNum, int pageSize) throws Exception {
        List<ReservationRecord> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT r.id, r.book_id, r.user_id, r.reserve_date, r.requested_pickup_date, "
                + "r.expected_pickup_date, r.expiry_date, r.status, r.notified_at, "
                + "r.delay_notified_at, r.created_at, r.updated_at, ")
                .append("u.username, u.full_name, u.email, u.phone, ")
                .append("b.title, b.isbn, ")
                .append(QUEUE_POSITION_SQL).append(" AS queue_position ")
                .append("FROM book_reservations r ")
                .append("INNER JOIN users u ON r.user_id = u.id ")
                .append("INNER JOIN books b ON r.book_id = b.id ")
                .append("WHERE 1=1 ");

        appendManagementFilters(sb, status, keyword);
        appendManagementOrder(sb, sortOrder);
        sb.append("LIMIT ?, ?");

        try (Connection conn = DBContext.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = bindManagementFilters(ps, status, keyword);
            int offset = (pageNum - 1) * pageSize;
            ps.setInt(idx++, offset);
            ps.setInt(idx, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReservationRecord record = mapRow(rs);
                    record.setQueuePosition(rs.getInt("queue_position"));
                    list.add(record);
                }
            }
        }
        return list;
    }

    /**
     * Đếm reservation khớp cùng bộ lọc của màn hình quản lý.
     *
     * @param status trạng thái cần lọc, để trống để lấy mọi trạng thái
     * @param keyword từ khóa tên độc giả, tài khoản hoặc tên sách
     * @return tổng số reservation phù hợp
     * @throws Exception khi không thể truy vấn dữ liệu
     */
    public int countReservations(String status, String keyword) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) ")
                .append("FROM book_reservations r ")
                .append("INNER JOIN users u ON r.user_id = u.id ")
                .append("INNER JOIN books b ON r.book_id = b.id ")
                .append("WHERE 1=1 ");

        appendManagementFilters(sb, status, keyword);

        try (Connection conn = DBContext.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            bindManagementFilters(ps, status, keyword);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Ghép các điều kiện dùng chung cho truy vấn danh sách và truy vấn đếm.
     *
     * @param sql câu SQL đang được xây dựng
     * @param status trạng thái cần lọc
     * @param keyword từ khóa cần tìm
     */
    private void appendManagementFilters(StringBuilder sql, String status, String keyword) {
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND r.status = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (u.full_name LIKE ? OR u.username LIKE ? OR b.title LIKE ?) ");
        }
    }

    /**
     * Ghép mệnh đề sắp xếp cố định; reservation không còn trong hàng chờ luôn nằm sau
     * các reservation có vị trí ưu tiên khi người dùng chọn sắp xếp theo ưu tiên.
     *
     * @param sql câu SQL đang được xây dựng
     * @param sortOrder thứ tự ưu tiên đã được kiểm tra
     */
    private void appendManagementOrder(StringBuilder sql, String sortOrder) {
        if ("ASC".equals(sortOrder)) {
            sql.append("ORDER BY CASE WHEN queue_position > 0 THEN 0 ELSE 1 END, ")
                    .append("queue_position ASC, r.created_at ASC, r.id ASC ");
        } else if ("DESC".equals(sortOrder)) {
            sql.append("ORDER BY CASE WHEN queue_position > 0 THEN 0 ELSE 1 END, ")
                    .append("queue_position DESC, r.created_at DESC, r.id DESC ");
        } else {
            sql.append("ORDER BY r.created_at DESC, r.id DESC ");
        }
    }

    /**
     * Gán tham số theo đúng thứ tự các điều kiện quản lý đã được ghép vào SQL.
     *
     * @param statement câu lệnh cần gán tham số
     * @param status trạng thái cần lọc
     * @param keyword từ khóa cần tìm
     * @return chỉ số tham số kế tiếp chưa được sử dụng
     * @throws SQLException khi không thể gán tham số
     */
    private int bindManagementFilters(PreparedStatement statement, String status, String keyword)
            throws SQLException {
        int parameterIndex = 1;
        if (status != null && !status.trim().isEmpty()) {
            statement.setString(parameterIndex++, status.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchPattern = "%" + keyword.trim() + "%";
            statement.setString(parameterIndex++, searchPattern);
            statement.setString(parameterIndex++, searchPattern);
            statement.setString(parameterIndex++, searchPattern);
        }
        return parameterIndex;
    }

    /**
     * Hủy reservation đang hoạt động theo thao tác của nhân viên.
     *
     * @param reservationId mã reservation cần hủy
     * @return {@code true} khi reservation và lượt chờ nhận liên quan đã được hủy
     * @throws Exception khi giao dịch cập nhật thất bại
     */
    public boolean cancelByStaff(int reservationId) throws Exception {
        return cancelActiveReservation(reservationId, null);
    }

    public ReservationRecord findById(int id) throws Exception {
        String sql = "SELECT r.id, r.book_id, r.user_id, r.reserve_date, r.requested_pickup_date, "
                + "r.expected_pickup_date, r.expiry_date, r.status, r.notified_at, "
                + "r.delay_notified_at, r.created_at, r.updated_at, "
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
        String sql = "SELECT r.id, r.book_id, r.user_id, r.reserve_date, r.requested_pickup_date, "
                + "r.expected_pickup_date, r.expiry_date, r.status, r.notified_at, "
                + "r.delay_notified_at, r.created_at, r.updated_at, u.username, u.full_name, u.email, "
                + "u.phone, b.title, b.isbn, " + QUEUE_POSITION_SQL + " AS queue_position "
                + "FROM book_reservations r INNER JOIN users u ON r.user_id=u.id "
                + "INNER JOIN books b ON r.book_id=b.id WHERE r.user_id=? ORDER BY r.created_at DESC";
        List<ReservationRecord> records = new ArrayList<>();
        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
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
        String sql = "SELECT r.id, r.book_id, r.user_id, r.reserve_date, r.requested_pickup_date, "
                + "r.expected_pickup_date, r.expiry_date, r.status, r.notified_at, "
                + "r.delay_notified_at, r.created_at, r.updated_at, u.username,u.full_name,u.email,u.phone, "
                + "b.title,b.isbn FROM book_reservations r INNER JOIN users u ON r.user_id=u.id "
                + "INNER JOIN books b ON r.book_id=b.id WHERE r.user_id=? AND r.book_id=? "
                + "AND r.status IN ('WAITING','READY_FOR_PICKUP') LIMIT 1";
        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
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
        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    /**
     * Mở kết nối để service điều phối một giao dịch thay đổi lịch đặt trước.
     *
     * @return kết nối JDBC đang mở
     * @throws SQLException khi không thể kết nối cơ sở dữ liệu
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public Connection openTransactionConnection() throws SQLException, ClassNotFoundException {
        return DBContext.getInstance().getConnection();
    }

    /**
     * Khóa đầu sách để tuần tự hóa các thao tác cùng thay đổi sức chứa lịch của đầu sách đó.
     *
     * @param connection kết nối đang tham gia giao dịch
     * @param bookId mã đầu sách cần khóa
     * @return {@code true} khi đầu sách còn tồn tại và chưa bị xóa
     * @throws SQLException khi không thể khóa dữ liệu
     */
    public boolean lockBook(Connection connection, int bookId) throws SQLException {
        String sql = "SELECT id FROM books WHERE id=? AND is_deleted=0 FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /**
     * Đếm các bản sao vật lý đủ điều kiện lưu thông để service dùng làm sức chứa lịch.
     *
     * @param connection kết nối dùng để đọc cùng một ảnh chụp giao dịch
     * @param bookId mã đầu sách
     * @return số bản sao chưa xóa và có tình trạng GOOD hoặc WORN
     * @throws SQLException khi không thể đọc dữ liệu bản sao
     */
    public int countEligibleCopies(Connection connection, int bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM book_copies WHERE book_id=? AND is_deleted=0 "
                + "AND book_condition IN ('GOOD','WORN')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    /**
     * Đọc các reservation của đầu sách để service tự quyết định trạng thái nào chiếm lịch.
     *
     * @param connection kết nối dùng để đọc cùng một ảnh chụp giao dịch
     * @param bookId mã đầu sách
     * @return danh sách reservation có ngày slot, không bao giờ trả về {@code null}
     * @throws SQLException khi không thể đọc lịch reservation
     */
    public List<ReservationRecord> findSchedulingReservations(Connection connection, int bookId)
            throws SQLException {
        String sql = "SELECT id,user_id,book_id,expected_pickup_date,status "
                + "FROM book_reservations WHERE book_id=? ORDER BY expected_pickup_date,id";
        List<ReservationRecord> records = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ReservationRecord record = new ReservationRecord();
                    record.setId(result.getInt("id"));
                    record.setUserId(result.getInt("user_id"));
                    record.setBookId(result.getInt("book_id"));
                    Date expectedDate = result.getDate("expected_pickup_date");
                    record.setExpectedPickupDate(
                            expectedDate == null ? null : expectedDate.toLocalDate());
                    record.setStatus(result.getString("status"));
                    records.add(record);
                }
            }
        }
        return records;
    }

    /**
     * Kiểm tra trùng reservation của cùng độc giả trong giao dịch tạo mới.
     *
     * @param connection kết nối đang giữ khóa đầu sách
     * @param userId mã độc giả
     * @param bookId mã đầu sách
     * @return {@code true} khi đã có reservation WAITING hoặc READY_FOR_PICKUP
     * @throws SQLException khi không thể đọc dữ liệu
     */
    public boolean hasActiveForUser(Connection connection, int userId, int bookId)
            throws SQLException {
        String sql = "SELECT 1 FROM book_reservations WHERE user_id=? AND book_id=? "
                + "AND status IN ('WAITING','READY_FOR_PICKUP') LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, bookId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /**
     * Ghi reservation WAITING sau khi service đã khóa và kiểm tra toàn bộ lịch.
     *
     * @param connection kết nối đang tham gia giao dịch
     * @param userId mã độc giả
     * @param bookId mã đầu sách
     * @param requestedPickupDate ngày bắt đầu độc giả chọn
     * @param expectedPickupDate ngày bắt đầu slot đã phân bổ
     * @return {@code true} khi chèn đúng một reservation
     * @throws SQLException khi không thể ghi dữ liệu
     */
    public boolean insertWaiting(Connection connection, int userId, int bookId,
            LocalDate requestedPickupDate, LocalDate expectedPickupDate) throws SQLException {
        String sql = "INSERT INTO book_reservations(user_id,book_id,reserve_date,"
                + "requested_pickup_date,expected_pickup_date,status,created_at,updated_at) "
                + "VALUES(?,?,NOW(),?,?,'WAITING',NOW(),NOW())";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, bookId);
            statement.setDate(3, Date.valueOf(requestedPickupDate));
            statement.setDate(4, Date.valueOf(expectedPickupDate));
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Lấy các yêu cầu đã lỡ ngày dự kiến vì bản sách liên quan vẫn đang quá hạn.
     *
     * @return yêu cầu cần gửi thông báo trễ đúng một lần
     * @throws Exception khi không thể đọc dữ liệu đặt trước
     */
    public List<ReservationRecord> findUnnotifiedDelayedReservations() throws Exception {
        String sql = "SELECT r.id,r.book_id,r.user_id,r.reserve_date,r.requested_pickup_date,"
                + "r.expected_pickup_date,r.expiry_date,r.status,r.notified_at,r.delay_notified_at,"
                + "r.created_at,r.updated_at,u.username,u.full_name,u.email,u.phone,b.title,b.isbn "
                + "FROM book_reservations r INNER JOIN users u ON u.id=r.user_id "
                + "INNER JOIN books b ON b.id=r.book_id WHERE r.status='WAITING' "
                + "AND r.delay_notified_at IS NULL AND r.expected_pickup_date<CURDATE() "
                + "AND EXISTS (SELECT 1 FROM borrow_records overdue_br "
                + "WHERE overdue_br.book_id=r.book_id AND overdue_br.return_date IS NULL "
                + "AND overdue_br.status IN ('BORROWED','OVERDUE') "
                + "AND overdue_br.due_date<CURDATE()) ORDER BY r.expected_pickup_date,r.id";
        List<ReservationRecord> records = new ArrayList<>();
        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                records.add(mapRow(result));
            }
        }
        return records;
    }

    /**
     * Ghi nhận yêu cầu đã nhận thông báo trễ để batch sau không gửi lặp.
     *
     * @param reservationId mã yêu cầu đặt trước
     * @return {@code true} khi dấu mốc được ghi lần đầu
     * @throws Exception khi không thể cập nhật dữ liệu
     */
    public boolean markDelayNotified(int reservationId) throws Exception {
        String sql = "UPDATE book_reservations SET delay_notified_at=NOW(),updated_at=NOW() "
                + "WHERE id=? AND delay_notified_at IS NULL";
        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Lấy các yêu cầu đã đến ngày dự kiến để thử tạo slot chờ nhận khi còn sách.
     *
     * @return danh sách mã yêu cầu theo ngày dự kiến và thứ tự tạo
     * @throws Exception khi không thể đọc hàng chờ
     */
    public List<Integer> findDueWaitingIds() throws Exception {
        String sql = "SELECT id FROM book_reservations WHERE status='WAITING' "
                + "AND expected_pickup_date<=CURDATE() ORDER BY expected_pickup_date,created_at,id";
        List<Integer> ids = new ArrayList<>();
        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                ids.add(result.getInt("id"));
            }
        }
        return ids;
    }

    /**
     * Hủy reservation đang hoạt động thuộc đúng độc giả.
     *
     * @param reservationId mã reservation cần hủy
     * @param userId mã độc giả sở hữu reservation
     * @return {@code true} khi reservation và lượt chờ nhận liên quan đã được hủy
     * @throws Exception khi giao dịch cập nhật thất bại
     */
    public boolean cancelOwned(int reservationId, int userId) throws Exception {
        return cancelActiveReservation(reservationId, userId);
    }

    /**
     * Dùng chung giao dịch hủy reservation và lượt chờ nhận đã được tạo từ reservation đó.
     *
     * @param reservationId mã reservation cần hủy
     * @param ownerUserId mã chủ sở hữu cần kiểm tra, hoặc {@code null} với thao tác nhân viên
     * @return {@code true} khi dữ liệu đang hoạt động được hủy
     * @throws Exception khi giao dịch cập nhật thất bại
     */
    private boolean cancelActiveReservation(int reservationId, Integer ownerUserId)
            throws Exception {
        String selectSql = "SELECT user_id,book_id FROM book_reservations WHERE id=? "
                + (ownerUserId == null ? "" : "AND user_id=? ")
                + "AND status IN ('WAITING','READY_FOR_PICKUP') FOR UPDATE";
        String reservationSql = "UPDATE book_reservations SET status='CANCELLED',updated_at=NOW() WHERE id=?";
        String borrowSql = "UPDATE borrow_records SET status='CANCELLED',updated_at=NOW() "
                + "WHERE user_id=? AND book_id=? AND status='PENDING_PICKUP'";
        try (Connection connection = DBContext.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int userId;
                int bookId;
                try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                    statement.setInt(1, reservationId);
                    if (ownerUserId != null) {
                        statement.setInt(2, ownerUserId);
                    }
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                        userId = result.getInt("user_id");
                        bookId = result.getInt("book_id");
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(reservationSql)) {
                    statement.setInt(1, reservationId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(borrowSql)) {
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
     * Giữ một slot chờ nhận cho reservation WAITING cũ nhất trong thời lượng chỉ định.
     *
     * @param bookId mã đầu sách
     * @param readyHours số giờ giữ slot chờ nhận
     * @return {@code true} khi reservation được tạo lượt chờ nhận thành công
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    public boolean activateNext(int bookId, int readyHours) throws Exception {
        String sql = "SELECT id FROM book_reservations WHERE book_id=? AND status='WAITING' "
                + "AND (expected_pickup_date IS NULL OR expected_pickup_date<=CURDATE()) "
                + "ORDER BY expected_pickup_date,created_at,id LIMIT 1";
        int reservationId = -1;
        try (Connection connection = DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    reservationId = result.getInt("id");
                }
            }
        }
        return reservationId > 0
                && manuallyReadyReservation(reservationId, "SYSTEM", readyHours) != null;
    }

    /**
     * Kích hoạt thủ công một reservation với thời gian giữ mặc định 24 giờ.
     *
     * @param reservationId mã reservation đang chờ
     * @param operator tài khoản thực hiện
     * @return reservation đã chuyển sang chờ nhận, hoặc {@code null} nếu không còn hợp lệ
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    public ReservationRecord manuallyReadyReservation(int reservationId, String operator) throws Exception {
        return manuallyReadyReservation(reservationId, operator, 24);
    }

    /**
     * Khóa reservation và một slot có thể mượn, sau đó tạo lượt chờ nhận chưa gán bản sao.
     *
     * @param reservationId mã reservation đang chờ
     * @param operator tài khoản thực hiện
     * @param readyHours số giờ giữ slot chờ nhận
     * @return reservation đã chuyển sang chờ nhận, hoặc {@code null} nếu không còn hợp lệ
     * @throws Exception khi thao tác dữ liệu thất bại
     */
    private ReservationRecord manuallyReadyReservation(int reservationId, String operator,
            int readyHours) throws Exception {
        String selectRes = "SELECT r.book_id, r.user_id, r.status, b.title, u.email, u.full_name "
                + "FROM book_reservations r "
                + "INNER JOIN books b ON r.book_id = b.id "
                + "INNER JOIN users u ON r.user_id = u.id "
                + "WHERE r.id=? AND NOT EXISTS (SELECT 1 FROM fines f WHERE f.user_id=r.user_id "
                + "AND f.status='UNPAID') AND (SELECT COUNT(*) FROM borrow_records br "
                + "WHERE br.user_id=r.user_id AND ((br.status='PENDING_PICKUP' "
                + "AND br.pickup_deadline>=NOW()) OR (br.status IN ('BORROWED','OVERDUE') "
                + "AND br.return_date IS NULL)))<3 FOR UPDATE";
        String availableSlotSql = "SELECT bc.id FROM book_copies bc WHERE bc.book_id = ? AND bc.is_deleted = 0 "
                + "AND bc.book_condition IN ('GOOD', 'WORN') AND NOT EXISTS ("
                + ACTIVE_BORROW_CONFLICT_SQL + ") AND (SELECT COUNT(*) FROM borrow_records active_borrow "
                + "WHERE active_borrow.book_id=bc.book_id AND ((active_borrow.status='PENDING_PICKUP' "
                + "AND active_borrow.pickup_deadline>=NOW()) OR "
                + "(active_borrow.status IN ('BORROWED','OVERDUE') "
                + "AND active_borrow.return_date IS NULL))) < (SELECT COUNT(*) FROM book_copies eligible_copy "
                + "WHERE eligible_copy.book_id=bc.book_id AND eligible_copy.is_deleted=0 "
                + "AND eligible_copy.book_condition IN ('GOOD','WORN')) "
                + "ORDER BY bc.id LIMIT 1 FOR UPDATE";
        String updateRes = "UPDATE book_reservations SET status='READY_FOR_PICKUP',"
                + "expected_pickup_date=GREATEST(COALESCE(expected_pickup_date,CURDATE()),"
                + "CURDATE()),notified_at=NOW(),"
                + "expiry_date=DATE_ADD(NOW(),INTERVAL ? HOUR),updated_at=NOW() WHERE id=?";
        String insertBorrow = "INSERT INTO borrow_records (user_id, book_id, copy_id, "
                + "request_date, pickup_deadline, borrow_date, due_date, renewal_count, "
                + "status, created_at, updated_at) "
                + "VALUES (?, ?, NULL, NOW(), DATE_ADD(NOW(), INTERVAL ? HOUR), NULL, NULL, 0, "
                + "'PENDING_PICKUP', NOW(), NOW())";

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

                boolean hasAvailableSlot = false;
                try (PreparedStatement ps = conn.prepareStatement(availableSlotSql)) {
                    ps.setInt(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            hasAvailableSlot = true;
                        }
                    }
                }

                if (!hasAvailableSlot) {
                    conn.rollback();
                    return null;
                }

                // 1. Update reservation
                try (PreparedStatement ps = conn.prepareStatement(updateRes)) {
                    ps.setInt(1, readyHours);
                    ps.setInt(2, reservationId);
                    ps.executeUpdate();
                }

                // 3. Insert borrow record
                try (PreparedStatement ps = conn.prepareStatement(insertBorrow)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, bookId);
                    ps.setInt(3, readyHours);
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
