/**
 * DAO quản lý toàn bộ SQL và ánh xạ dữ liệu cho bảng categories.
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Category;
import utils.DBContext;

/**
 * Cung cấp thao tác đọc, ghi và kiểm tra ràng buộc lưu trữ của thể loại bằng JDBC.
 */
public class CategoryDao {

    private static final String ACTIVE_COLUMNS = "id, name, description, created_at, updated_at, "
            + "is_deleted, created_by, updated_by";

    /**
     * Lấy danh sách thể loại chưa xóa theo từ khóa, thứ tự và trang.
     *
     * @param keyword từ khóa đã chuẩn hóa để tìm trong tên danh mục
     * @param sort trường sắp xếp đã được giới hạn bởi service/controller
     * @param order chiều sắp xếp ASC hoặc DESC
     * @param offset vị trí bản ghi đầu tiên, không âm
     * @param limit số bản ghi tối đa
     * @return danh sách thể loại, rỗng nếu không có dữ liệu
     * @throws SQLException khi truy vấn thất bại
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    public List<Category> findAll(String keyword, String sort, String order, int offset, int limit)
            throws SQLException, ClassNotFoundException {
        String sortColumn = "created_at".equals(sort) ? "created_at" : "name";
        String sortOrder = "DESC".equalsIgnoreCase(order) ? "DESC" : "ASC";
        String sql = "SELECT " + ACTIVE_COLUMNS + " FROM categories "
                + "WHERE is_deleted = 0 AND (? = '' OR LOWER(name) LIKE LOWER(?)) "
                + "ORDER BY " + sortColumn + " " + sortOrder + ", id ASC LIMIT ? OFFSET ?";
        List<Category> categories = new ArrayList<>();
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            statement.setString(1, keyword);
            statement.setString(2, searchPattern);
            statement.setInt(3, limit);
            statement.setInt(4, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(mapCategory(resultSet));
                }
            }
        }
        return categories;
    }

    /**
     * Đếm thể loại đang hoạt động phù hợp với từ khóa để phục vụ phân trang.
     *
     * @param keyword từ khóa đã chuẩn hóa để tìm trong tên danh mục
     * @return tổng số thể loại chưa xóa
     * @throws SQLException khi truy vấn thất bại
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    public int count(String keyword) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM categories WHERE is_deleted = 0 "
                + "AND (? = '' OR LOWER(name) LIKE LOWER(?))";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            statement.setString(1, keyword);
            statement.setString(2, searchPattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /**
     * Lấy toàn bộ danh sách thể loại chưa xóa (phục vụ chọn ở Form).
     */
    public List<Category> findAll() throws Exception {
        String sql = "SELECT " + ACTIVE_COLUMNS + " FROM categories WHERE is_deleted = 0 ORDER BY name ASC";
        List<Category> list = new ArrayList<>();
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCategory(rs));
            }
        }
        return list;
    }

    /**
     * Tìm một thể loại chưa xóa theo mã.
     *
     * @param id mã thể loại
     * @return thể loại nếu tồn tại và còn hoạt động
     * @throws SQLException khi truy vấn thất bại
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    public Optional<Category> findById(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT " + ACTIVE_COLUMNS + " FROM categories WHERE id = ? AND is_deleted = 0";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapCategory(resultSet)) : Optional.empty();
            }
        }
    }

    /**
     * Thêm thể loại và trả lại bản ghi đã có mã cùng dữ liệu kiểm toán.
     *
     * @param category dữ liệu đã được service kiểm tra
     * @param actor tài khoản quản trị thực hiện thao tác
     * @return thể loại vừa được tạo
     * @throws SQLException khi thao tác ghi hoặc đọc lại thất bại
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    public Category insert(Category category, String actor) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO categories (name, description, created_by, updated_by) VALUES (?, ?, ?, ?)";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setString(3, actor);
            statement.setString(4, actor);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Không nhận được mã thể loại vừa tạo");
                }
                category.setId(keys.getInt(1));
            }
        }
        return findById(category.getId()).orElseThrow(
                () -> new SQLException("Không đọc lại được thể loại vừa tạo"));
    }

    /**
     * Cập nhật nội dung và tài khoản thực hiện của một thể loại đang hoạt động.
     *
     * @param category dữ liệu cập nhật đã hợp lệ
     * @param actor tài khoản quản trị thực hiện thao tác
     * @return {@code true} nếu có đúng bản ghi được cập nhật
     * @throws SQLException khi cập nhật thất bại
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    public boolean update(Category category, String actor) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE categories SET name = ?, description = ?, updated_by = ? "
                + "WHERE id = ? AND is_deleted = 0";
        String updateBooksSql = "UPDATE books SET category = ? WHERE category_id = ?";
        try (Connection connection = openConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, category.getName());
                    statement.setString(2, category.getDescription());
                    statement.setString(3, actor);
                    statement.setInt(4, category.getId());
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(updateBooksSql)) {
                    statement.setString(1, category.getName());
                    statement.setInt(2, category.getId());
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    /**
     * Xóa mềm một thể loại đang hoạt động sau khi service đã kiểm tra quan hệ sách.
     *
     * @param id mã thể loại
     * @param actor tài khoản quản trị thực hiện thao tác
     * @return {@code true} nếu bản ghi được xóa
     * @throws SQLException khi xóa thất bại
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    public boolean deleteById(int id, String actor) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE categories SET is_deleted = 1, updated_by = ?, updated_at = NOW() "
                + "WHERE id = ? AND is_deleted = 0";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, actor);
            statement.setInt(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Kiểm tra tên đã tồn tại, không phân biệt hoa thường và bỏ qua một mã khi cập nhật.
     *
     * @param name tên đã chuẩn hóa
     * @param excludedId mã cần bỏ qua, bằng 0 khi tạo mới
     * @return {@code true} nếu tên đã được dùng bởi một bản ghi đang hoạt động
     * @throws SQLException khi truy vấn thất bại
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    public boolean existsByName(String name, int excludedId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM categories WHERE LOWER(name) = LOWER(?) "
                + "AND id <> ? AND is_deleted = 0 LIMIT 1";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, excludedId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Khôi phục danh mục xóa mềm từ phiên bản cũ của ứng dụng để có thể tái sử dụng tên và mã hiện có.
     *
     * @param category dữ liệu danh mục mới đã được service kiểm tra
     * @param actor tài khoản quản trị thực hiện thao tác
     * @return danh mục được khôi phục hoặc rỗng nếu không có bản ghi lịch sử phù hợp
     * @throws SQLException khi cập nhật hoặc đọc dữ liệu thất bại
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    public Optional<Category> restoreDeleted(Category category, String actor)
            throws SQLException, ClassNotFoundException {
        String updateSql = "UPDATE categories SET description = ?, is_deleted = 0, updated_by = ? "
                + "WHERE LOWER(name) = LOWER(?) AND is_deleted = 1 LIMIT 1";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setString(1, category.getDescription());
            statement.setString(2, actor);
            statement.setString(3, category.getName());
            if (statement.executeUpdate() == 0) {
                return Optional.empty();
            }
        }

        String selectSql = "SELECT " + ACTIVE_COLUMNS + " FROM categories "
                + "WHERE LOWER(name) = LOWER(?) AND is_deleted = 0 LIMIT 1";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setString(1, category.getName());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapCategory(resultSet)) : Optional.empty();
            }
        }
    }

    /**
     * Kiểm tra thể loại còn được bất kỳ bản ghi sách nào tham chiếu hay không.
     *
     * @param categoryId mã thể loại cần kiểm tra
     * @return {@code true} nếu ít nhất một sách, kể cả sách xóa mềm, sử dụng thể loại
     * @throws SQLException khi truy vấn thất bại
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    public boolean hasBooks(int categoryId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM books WHERE category_id = ? LIMIT 1";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Mở connection theo cơ chế hiện có của project để mỗi thao tác tự đóng tài nguyên.
     *
     * @return connection đang mở
     * @throws SQLException khi không kết nối được cơ sở dữ liệu
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    private Connection openConnection() throws SQLException, ClassNotFoundException {
        return DBContext.getInstance().getConnection();
    }

    /**
     * Ánh xạ hàng kết quả đầy đủ sang mô hình Category.
     *
     * @param resultSet hàng dữ liệu đang được trỏ tới
     * @return mô hình thể loại đã ánh xạ
     * @throws SQLException khi không đọc được cột
     */
    private Category mapCategory(ResultSet resultSet) throws SQLException {
        Category category = new Category();
        category.setId(resultSet.getInt("id"));
        category.setName(resultSet.getString("name"));
        category.setDescription(resultSet.getString("description"));
        category.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        category.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        category.setDeleted(resultSet.getBoolean("is_deleted"));
        category.setCreatedBy(resultSet.getString("created_by"));
        category.setUpdatedBy(resultSet.getString("updated_by"));
        return category;
    }

    /**
     * Chuyển timestamp nullable của JDBC sang kiểu ngày giờ miền.
     *
     * @param timestamp giá trị JDBC có thể null
     * @return LocalDateTime tương ứng hoặc null
     */
    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
