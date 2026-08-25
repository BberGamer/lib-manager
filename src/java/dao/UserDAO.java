package dao;

import utils.DBContext;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.DBContext;

public class UserDAO {

    // Lấy thông tin người dùng theo ID
    public User getUserById(int id) throws Exception {
        String sql = "SELECT id, username, password, full_name, email, phone, student_id, avatar, role, active, is_deleted FROM users WHERE id = ?";
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

    // Lấy thông tin người dùng theo Tên đăng nhập (username) hoặc Email
    public User getUserByUsername(String username) throws Exception {
        String sql = "SELECT id, username, password, full_name, email, phone, student_id, avatar, role, active, is_deleted FROM users WHERE (username = ? OR email = ?)";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    // Tìm kiếm danh sách người dùng đơn giản theo từ khóa, role, active
    public List<User> searchUsers(String q, String role, Integer active) throws Exception {
        List<User> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT id, username, password, full_name, email, phone, student_id, avatar, role, active FROM users WHERE 1=1 ");
        if (q != null && !q.trim().isEmpty()) {
            sb.append(" AND (username LIKE ? OR full_name LIKE ? OR email LIKE ?)");
        }
        if (role != null && !role.trim().isEmpty()) {
            sb.append(" AND role = ?");
        }
        if (active != null) {
            sb.append(" AND active = ?");
        }
        sb.append(" ORDER BY id DESC");

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (q != null && !q.trim().isEmpty()) {
                String like = "%" + q.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (role != null && !role.trim().isEmpty()) {
                ps.setString(idx++, role);
            }
            if (active != null) {
                ps.setInt(idx++, active);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // Tạo mới người dùng và trả về ID tự tăng vừa sinh (sửa nếu hiển thị trường trong add)
    public int createUser(User user, String rawPassword) throws Exception {
        String sql = "INSERT INTO users (username, password, full_name, email, phone, student_id, avatar, role, active) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, rawPassword);
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getStudentId());
            ps.setString(7, user.getAvatar());
            ps.setString(8, user.getRole());
            ps.setInt(9, user.getActive());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                return -1;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    // Cập nhật thông tin chi tiết của người dùng (sửa nếu hiển thị trường trong update)
    public boolean updateUser(User user) throws Exception {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, student_id = ?, avatar = ?, role = ?, active = ? WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getStudentId());
            ps.setString(5, user.getAvatar());
            ps.setString(6, user.getRole());
            ps.setInt(7, user.getActive());
            ps.setInt(8, user.getId()); // id cuối
            return ps.executeUpdate() > 0;
        }
    }

    // Cập nhật mật khẩu băm mới theo ID người dùng
    public boolean updatePassword(int userId, String hashedPassword) throws Exception {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // Kiểm tra xem người dùng có giao dịch dở dang nào không (sách đang mượn, tiền phạt chưa trả, sách đang đặt)
    // ... (Nếu count > 0 -> Trả về thông báo lỗi)
    public String checkUserPendingTransactions(int userId) throws Exception {
        try (Connection conn = DBContext.getInstance().getConnection()) {
            // 1. Kiểm tra sách đang mượn hoặc chờ lấy chưa trả
            String sqlBorrow = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND status IN ('PENDING_PICKUP', 'BORROWED', 'OVERDUE')";
            try (PreparedStatement ps = conn.prepareStatement(sqlBorrow)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return "Không thể xóa: Người dùng đang có " + rs.getInt(1) + " cuốn sách đang mượn hoặc chờ lấy chưa hoàn trả.";
                    }
                }
            }

            // 2. Kiểm tra khoản phạt chưa thanh toán
            String sqlFine = "SELECT COUNT(*) FROM fines WHERE user_id = ? AND status = 'UNPAID'";
            try (PreparedStatement ps = conn.prepareStatement(sqlFine)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return "Không thể xóa: Người dùng còn " + rs.getInt(1) + " khoản phạt chưa thanh toán.";
                    }
                }
            }

            // 3. Kiểm tra phiếu đặt mượn sách chưa hoàn tất
            String sqlReservation = "SELECT COUNT(*) FROM book_reservations WHERE user_id = ? AND status IN ('WAITING', 'READY_FOR_PICKUP')";
            try (PreparedStatement ps = conn.prepareStatement(sqlReservation)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return "Không thể xóa: Người dùng đang có " + rs.getInt(1) + " đơn đặt sách chưa hoàn thành.";
                    }
                }
            }
        }
        return null; // Không có giao dịch dở dang nào
    }

    /**
     * Xóa mềm người dùng theo ID bằng cách cập nhật is_deleted = 1.
     * Tách biệt hoàn toàn với tính năng Khóa/Mở khóa tài khoản (active = 0).
     * Giúp bảo tồn bản ghi trong CSDL tránh vi phạm ràng buộc khóa ngoại.
     *
     * @param userId ID người dùng cần xóa mềm
     * @return true nếu cập nhật trạng thái xóa mềm thành công, ngược lại false
     * @throws Exception khi xảy ra lỗi kết nối hoặc truy vấn CSDL
     */
    public boolean deleteUser(int userId) throws Exception {
        // Thực hiện Xóa mềm bằng cột is_deleted = 1 
        String sql = "UPDATE users SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // Khóa (0) hoặc Mở khóa (1) tài khoản người dùng
    public boolean setActive(int userId, int active) throws Exception {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // Ánh xạ một dòng kết quả ResultSet thành đối tượng Model User
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setStudentId(rs.getString("student_id"));
        u.setAvatar(rs.getString("avatar"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getInt("active")); // thêm nếu thêm trường
        return u;
    }
    //

    // Check email tồn tại và lấy thông tin User
    public User getUserByEmail(String email) throws Exception {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    // Cập nhật mật khẩu theo email
    public boolean updatePasswordByEmail(String email, String hashedPassword) throws Exception {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        }
    }
    // Check studentId đã tồn tại chưa, lấy thông tin User đang sở hữu studentId đó (nếu có)

    public User getUserByStudentId(String studentId) throws Exception {
        String sql = "SELECT id, username, password, full_name, email, phone, student_id, avatar, role, active, is_deleted FROM users WHERE student_id = ?";
        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Whitelist các cột được phép ORDER BY. Bắt buộc phải có whitelist vì ORDER
     * BY không dùng được PreparedStatement (?) cho tên cột — nếu nối thẳng
     * sortField (lấy từ URL, người dùng tự sửa được) vào câu SQL mà không kiểm
     * tra thì sẽ dính SQL Injection. Key = giá trị "sort" trên URL
     * (?sort=full_name), Value = tên cột thật trong DB.
     */
    // thêm nếu thêm trường sắp xếp mới (sortOption jsp)
    private static final java.util.Map<String, String> SORTABLE_COLUMNS = java.util.Map.of(
            "username", "username",
            "full_name", "full_name",
            "role", "role",
            "active", "active"
    );

    // Tìm kiếm danh sách người dùng nâng cao (5 tham số)
    public List<User> searchUsers(String q, String role, Integer active, String sortField, String sortOrder) throws Exception {
        List<User> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        // Chỉ hiển thị các người dùng chưa bị xóa mềm (is_deleted = 0)
        sb.append("SELECT id, username, password, full_name, email, phone, student_id, avatar, role, active, is_deleted FROM users WHERE (is_deleted IS NULL OR is_deleted = 0) ");
        if (q != null && !q.trim().isEmpty()) {
            sb.append(" AND (username LIKE ? OR full_name LIKE ? OR email LIKE ?)"); // nếu thêm lọc thanh tìm
        }
        if (role != null && !role.trim().isEmpty()) {
            sb.append(" AND role = ?");
        }
        if (active != null) {
            sb.append(" AND active = ?");
        }

        // Lấy tên cột sắp xếp hợp lệ từ Whitelist SORTABLE_COLUMNS để bảo mật
        String column = SORTABLE_COLUMNS.getOrDefault(sortField, "username");
        String order = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC"; // nếu sắp xếp giảm ASC
        sb.append(" ORDER BY ").append(column).append(" ").append(order).append(", id DESC");

        try (Connection conn = DBContext.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            // Gán giá trị tham số động tương ứng với vị trí dấu ?
            if (q != null && !q.trim().isEmpty()) {
                String like = "%" + q.trim() + "%";
                ps.setString(idx++, like); // username
                ps.setString(idx++, like); // full_name
                ps.setString(idx++, like); // email (thêm dưới nếu thêm lọc thanh tìm)
            }
            if (role != null && !role.trim().isEmpty()) {
                ps.setString(idx++, role);
            }
            if (active != null) {
                ps.setInt(idx++, active);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
//                    User u = mapRow(rs);
                    // Chỉ thêm vào danh sách nếu là READER VÀ có ID là 12, 13, hoặc 14
//                    if ("READER".equalsIgnoreCase(u.getRole()) && (u.getId() == 12 || u.getId() == 13 || u.getId() == 14)) {
//                        list.add(u);
                   list.add(mapRow(rs));
                }
            }
            return list;
        }
    }
}
// ORDER BY FIELD(role, 'ADMIN', 'LIBRARIAN', 'READER'), id DESC
