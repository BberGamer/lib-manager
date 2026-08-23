/**
 * Lớp DAO quản lý việc truy vấn và thao tác dữ liệu bảng events trong CSDL.
 * Thuộc tầng Persistence (DAO).
 *
 * Đồng bộ cấu trúc 100% với UserDAO để dễ dàng quản lý và bảo trì.
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
import java.util.Map;

/**
 * Lớp thao tác CSDL cho đối tượng Sự kiện (Event).
 */
public class EventDAO {

    /**
     * Danh sách các cột hợp lệ được phép sắp xếp (Whitelisting chống SQL Injection).
     * thêm nếu thêm trường sắp xếp mới (sortOption jsp)
     */
    private static final Map<String, String> SORTABLE_COLUMNS = Map.of(
            "start_time", "start_time",
            "title", "title",
            "status", "status"
    );

    /**
     * Tìm kiếm danh sách sự kiện nâng cao (chưa bị xóa mềm).
     *
     * @param q            Từ khóa tìm kiếm theo tiêu đề (SQL LIKE)
     * @param statusFilter Trạng thái quản trị ("ACTIVE", "CANCELLED" hoặc null/rỗng)
     * @param sortField    Tên trường cần sắp xếp (lấy từ URL)
     * @param sortOrder    Thứ tự sắp xếp ("ASC" hoặc "DESC")
     * @return Danh sách sự kiện thỏa mãn điều kiện
     * @throws Exception Khi gặp lỗi kết nối hoặc truy vấn CSDL
     */
    public List<Event> searchEvents(String q, String statusFilter, String sortField, String sortOrder) throws Exception {
        List<Event> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, title, description, start_time, end_time, status, is_deleted, "
                + "created_by, updated_by, created_at, updated_at "
                + "FROM events WHERE is_deleted = 0 "
        );

        boolean hasKeyword = (q != null && !q.trim().isEmpty());
        if (hasKeyword) {
            sql.append("AND title LIKE ? ");
        }

        boolean hasStatus = (statusFilter != null && !statusFilter.trim().isEmpty());
        if (hasStatus) {
            sql.append("AND status = ? ");
        }

        // Lấy tên cột sắp xếp hợp lệ từ Whitelist SORTABLE_COLUMNS để bảo mật
        String column = SORTABLE_COLUMNS.getOrDefault(sortField, "start_time");
        String order = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        sql.append("ORDER BY ").append(column).append(" ").append(order).append(", id DESC");

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (hasKeyword) {
                ps.setString(idx++, "%" + q.trim() + "%");
            }
            if (hasStatus) {
                ps.setString(idx++, statusFilter.trim());
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
     * Tải tất cả các sự kiện chưa bị xóa mềm (dùng cho Service nếu cần).
     *
     * @param keyword Từ khóa tìm kiếm theo tiêu đề
     * @return Danh sách sự kiện
     * @throws Exception Khi gặp lỗi truy vấn
     */
    public List<Event> findActiveEvents(String keyword) throws Exception {
        return searchEvents(keyword, null, "start_time", "ASC");
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
     * @param event Đối tượng sự kiện chứa thông tin mới
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
     * Thao tác Xóa mềm (soft delete) một sự kiện (gán is_deleted = 1).
     *
     * @param id        Mã sự kiện cần xóa
     * @param updatedBy Tài khoản thực hiện xóa
     * @return true nếu xóa thành công, false nếu thất bại
     * @throws Exception Khi gặp lỗi kết nối hoặc truy vấn CSDL
     */
    public boolean softDelete(int id, String updatedBy) throws Exception {
        String sql = "UPDATE events SET is_deleted = 1, updated_by = ? WHERE id = ?";

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, updatedBy);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Hàm helper ánh xạ một dòng từ ResultSet sang đối tượng Event.
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
