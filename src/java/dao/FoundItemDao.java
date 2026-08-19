/**
 * DAO sở hữu các truy vấn JDBC cho bảng found_items.
 * Lớp thuộc tầng dao và không chứa quy tắc HTTP hay nghiệp vụ bàn giao.
 */
package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.FoundItem;
import model.FoundItemStatus;
import utils.DBContext;

/**
 * Thực hiện đọc và tạo dữ liệu đồ để quên từ cơ sở dữ liệu.
 */
public class FoundItemDao {

    private static final String COLUMNS = "id, item_name, description, found_date, image_path, status, "
            + "created_by, created_at, updated_at";

    /**
     * Tìm danh sách đồ để quên theo từ khóa và trạng thái, có phân trang.
     *
     * @param keyword từ khóa tìm trong tên hoặc mô tả
     * @param status trạng thái lọc, null nghĩa là lấy tất cả
     * @param offset vị trí bắt đầu trang
     * @param limit số bản ghi tối đa
     * @return danh sách đồ vật phù hợp
     * @throws SQLException khi truy vấn thất bại
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public List<FoundItem> findAll(String keyword, FoundItemStatus status, int offset, int limit)
            throws SQLException, ClassNotFoundException {
        StringBuilder sql = new StringBuilder("SELECT ").append(COLUMNS)
                .append(" FROM found_items WHERE (? = '' OR item_name LIKE ? OR description LIKE ?) ");
        if (status != null) {
            sql.append("AND status = ? ");
        }
        sql.append("ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?");

        List<FoundItem> items = new ArrayList<>();
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            String pattern = "%" + keyword + "%";
            statement.setString(1, keyword);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            int index = 4;
            if (status != null) {
                statement.setString(index++, status.name());
            }
            statement.setInt(index++, limit);
            statement.setInt(index, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapFoundItem(resultSet));
                }
            }
        }
        return items;
    }

    /**
     * Đếm đồ để quên phù hợp với cùng điều kiện tìm kiếm của danh sách.
     *
     * @param keyword từ khóa tìm kiếm
     * @param status trạng thái lọc, null nghĩa là lấy tất cả
     * @return tổng số bản ghi phù hợp
     * @throws SQLException khi truy vấn thất bại
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public int count(String keyword, FoundItemStatus status) throws SQLException, ClassNotFoundException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM found_items "
                + "WHERE (? = '' OR item_name LIKE ? OR description LIKE ?) ");
        if (status != null) {
            sql.append("AND status = ?");
        }
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            String pattern = "%" + keyword + "%";
            statement.setString(1, keyword);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            if (status != null) {
                statement.setString(4, status.name());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /**
     * Tìm một đồ để quên theo mã.
     *
     * @param id mã đồ vật
     * @return đồ vật nếu tồn tại
     * @throws SQLException khi truy vấn thất bại
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public Optional<FoundItem> findById(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT " + COLUMNS + " FROM found_items WHERE id = ?";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapFoundItem(resultSet)) : Optional.empty();
            }
        }
    }

    /**
     * Khóa và đọc đồ để quên trong giao dịch đang xử lý yêu cầu nhận lại.
     *
     * @param connection kết nối thuộc giao dịch hiện tại
     * @param id mã đồ để quên
     * @return đồ để quên nếu tồn tại
     * @throws SQLException khi không thể khóa hoặc đọc dữ liệu
     */
    public Optional<FoundItem> findByIdForUpdate(Connection connection, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM found_items WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapFoundItem(resultSet)) : Optional.empty();
            }
        }
    }

    /**
     * Cập nhật trạng thái đồ trong giao dịch do service quản lý.
     *
     * @param connection kết nối thuộc giao dịch hiện tại
     * @param itemId mã đồ để quên
     * @param status trạng thái mới đã được service kiểm tra
     * @throws SQLException khi cập nhật thất bại
     */
    public void updateStatus(Connection connection, int itemId, FoundItemStatus status) throws SQLException {
        String sql = "UPDATE found_items SET status = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, itemId);
            statement.executeUpdate();
        }
    }

    /**
     * Mở kết nối cho giao dịch nghiệp vụ được service điều phối.
     *
     * @return kết nối JDBC đang mở
     * @throws SQLException khi không thể kết nối cơ sở dữ liệu
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public Connection openTransactionConnection() throws SQLException, ClassNotFoundException {
        return openConnection();
    }

    /**
     * Lưu một đồ để quên mới với trạng thái AVAILABLE mặc định.
     *
     * @param item dữ liệu đã được service validation
     * @param actorUserId mã nhân viên tiếp nhận
     * @return đồ vật vừa tạo cùng mã định danh
     * @throws SQLException khi ghi hoặc đọc lại dữ liệu thất bại
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public FoundItem insert(FoundItem item, int actorUserId) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO found_items (item_name, description, found_date, image_path, status, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, item.getItemName());
            statement.setString(2, item.getDescription());
            statement.setDate(3, Date.valueOf(item.getFoundDate()));
            statement.setString(4, item.getImagePath());
            statement.setString(5, item.getStatus().name());
            statement.setInt(6, actorUserId);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Không nhận được mã đồ để quên vừa tạo.");
                }
                item.setId(generatedKeys.getInt(1));
            }
        }
        return findById(item.getId()).orElseThrow(
                () -> new SQLException("Không đọc lại được đồ để quên vừa tạo."));
    }

    /**
     * Mở kết nối mới theo cơ chế JDBC hiện có của dự án.
     *
     * @return kết nối cơ sở dữ liệu đang mở
     * @throws SQLException khi không kết nối được cơ sở dữ liệu
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    private Connection openConnection() throws SQLException, ClassNotFoundException {
        return DBContext.getInstance().getConnection();
    }

    /**
     * Ánh xạ một hàng ResultSet thành mô hình FoundItem.
     *
     * @param resultSet hàng dữ liệu đang được trỏ tới
     * @return mô hình đồ để quên tương ứng
     * @throws SQLException khi không đọc được cột dữ liệu
     */
    private FoundItem mapFoundItem(ResultSet resultSet) throws SQLException {
        FoundItem item = new FoundItem();
        item.setId(resultSet.getInt("id"));
        item.setItemName(resultSet.getString("item_name"));
        item.setDescription(resultSet.getString("description"));
        Date foundDate = resultSet.getDate("found_date");
        item.setFoundDate(foundDate == null ? null : foundDate.toLocalDate());
        item.setImagePath(resultSet.getString("image_path"));
        item.setStatus(FoundItemStatus.valueOf(resultSet.getString("status")));
        item.setCreatedBy(resultSet.getInt("created_by"));
        item.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        item.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return item;
    }

    /**
     * Chuyển Timestamp nullable sang LocalDateTime.
     *
     * @param timestamp giá trị JDBC có thể null
     * @return LocalDateTime tương ứng hoặc null
     */
    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
