/**
 * Lớp DAO quản lý việc truy vấn và thao tác dữ liệu bảng events trong CSDL.
 * Thuộc tầng Persistence (DAO).
 *
 * Theo quy tắc thiết kế:
 * - Sử dụng PreparedStatement và try-with-resources để bảo mật và quản lý tài nguyên.
 * - Chỉ duy nhất 1 phương thức load toàn bộ sự kiện chưa xóa (is_deleted = 0) kết hợp tìm kiếm tiêu đề bằng SQL LIKE.
 * - Xóa sự kiện thực hiện bằng xóa mềm (soft delete: update is_deleted = 1).
 */
package dao;

import model.Event;
import utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp thao tác CSDL cho đối tượng Sự kiện (Event).
 */
public class EventDAO {

    /**
     * Tải tất cả các sự kiện chưa bị xóa mềm (is_deleted = 0) từ CSDL.
     * Hỗ trợ tìm kiếm theo tiêu đề sự kiện bằng câu lệnh SQL LIKE.
     *
     * @param keyword Từ khóa tìm kiếm theo tiêu đề (có thể null hoặc rỗng)
     * @return Danh sách các sự kiện thỏa mãn điều kiện
     * @throws Exception Khi gặp lỗi kết nối hoặc truy vấn CSDL
     */
    public List<Event> findActiveEvents(String keyword) throws Exception {
        List<Event> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, title, description, start_time, end_time, status, is_deleted, "
                + "created_by, updated_by, created_at, updated_at "
                + "FROM events WHERE is_deleted = 0 "
        );

        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        if (hasKeyword) {
            sql.append("AND title LIKE ? ");
        }
        sql.append("ORDER BY id DESC");

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (hasKeyword) {
                ps.setString(1, "%" + keyword.trim() + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Tìm kiếm một sự kiện theo ID (chưa bị xóa mềm).
     *
     * @param id Mã sự kiện
     * @return Đối tượng Event nếu tìm thấy, hoặc null nếu không tồn tại
     * @throws Exception Khi gặp lỗi kết nối hoặc truy vấn CSDL
     */
    public Event findById(int id) throws Exception {
        String sql = "SELECT id, title, description, start_time, end_time, status, is_deleted, "
                + "created_by, updated_by, created_at, updated_at "
                + "FROM events WHERE id = ? AND is_deleted = 0";

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
     * Thêm mới một sự kiện vào CSDL.
     *
     * @param event Đối tượng sự kiện chứa thông tin cần thêm
     * @return true nếu thêm thành công, false nếu thất bại
     * @throws Exception Khi gặp lỗi kết nối hoặc truy vấn CSDL
     */
    public boolean insert(Event event) throws Exception {
        String sql = "INSERT INTO events (title, description, start_time, end_time, status, is_deleted, created_by) "
                + "VALUES (?, ?, ?, ?, ?, 0, ?)";

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(event.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(event.getEndTime()));
            ps.setString(5, event.getStatus() != null ? event.getStatus() : "ACTIVE");
            ps.setString(6, event.getCreatedBy());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật thông tin sự kiện trong CSDL.
     *
     * @param event Đối tượng sự kiện chứa thông tin cập nhật
     * @return true nếu cập nhật thành công, false nếu thất bại
     * @throws Exception Khi gặp lỗi kết nối hoặc truy vấn CSDL
     */
    public boolean update(Event event) throws Exception {
        String sql = "UPDATE events SET title = ?, description = ?, start_time = ?, end_time = ?, "
                + "status = ?, updated_by = ? WHERE id = ? AND is_deleted = 0";

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(event.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(event.getEndTime()));
            ps.setString(5, event.getStatus());
            ps.setString(6, event.getUpdatedBy());
            ps.setInt(7, event.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa mềm sự kiện bằng cách cập nhật is_deleted = 1.
     *
     * @param id Mã sự kiện cần xóa
     * @param updatedBy Tài khoản người thực hiện xóa
     * @return true nếu xóa mềm thành công, false nếu thất bại
     * @throws Exception Khi gặp lỗi kết nối hoặc truy vấn CSDL
     */
    public boolean softDelete(int id, String updatedBy) throws Exception {
        String sql = "UPDATE events SET is_deleted = 1, updated_by = ? WHERE id = ? AND is_deleted = 0";

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, updatedBy);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Ánh xạ một dòng dữ liệu từ ResultSet sang đối tượng Event.
     *
     * @param rs ResultSet tại vị trí con trỏ hiện tại
     * @return Đối tượng Event
     * @throws Exception Khi truy xuất dữ liệu từ ResultSet bị lỗi
     */
    private Event mapRow(ResultSet rs) throws Exception {
        Event event = new Event();
        event.setId(rs.getInt("id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));

        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) {
            event.setStartTime(startTs.toLocalDateTime());
        }

        Timestamp endTs = rs.getTimestamp("end_time");
        if (endTs != null) {
            event.setEndTime(endTs.toLocalDateTime());
        }

        event.setStatus(rs.getString("status"));
        event.setIsDeleted(rs.getInt("is_deleted"));
        event.setCreatedBy(rs.getString("created_by"));
        event.setUpdatedBy(rs.getString("updated_by"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            event.setCreatedAt(createdTs.toLocalDateTime());
        }

        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            event.setUpdatedAt(updatedTs.toLocalDateTime());
        }

        return event;
    }
}
