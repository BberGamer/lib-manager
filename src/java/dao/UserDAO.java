package dao;

import utils.DBContext;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.DBContext;

public class UserDAO {

    public User getUserById(int id) throws Exception {
        String sql = "SELECT id, username, password, full_name, email, phone, student_id, avatar, role, active FROM users WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public User getUserByUsername(String username) throws Exception {
        String sql = "SELECT id, username, password, full_name, email, phone, student_id, avatar, role, active FROM users WHERE username = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

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

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
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

    public int createUser(User user, String rawPassword) throws Exception {
        String sql = "INSERT INTO users (username, password, full_name, email, phone, student_id, avatar, role, active) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            if (affected == 0) return -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateUser(User user) throws Exception {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, student_id = ?, avatar = ?, role = ?, active = ? WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getStudentId());
            ps.setString(5, user.getAvatar());
            ps.setString(6, user.getRole());
            ps.setInt(7, user.getActive());
            ps.setInt(8, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int userId, String hashedPassword) throws Exception {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(int userId) throws Exception {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception("Không thể xóa người dùng này vì có dữ liệu liên quan (lịch sử mượn, phạt, v.v.). Vui lòng Khóa tài khoản thay thế.");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("foreign key constraint")) {
                throw new Exception("Không thể xóa người dùng này vì có dữ liệu liên quan (lịch sử mượn, phạt, v.v.). Vui lòng Khóa tài khoản thay thế.");
            }
            throw e;
        }
    }

    public boolean setActive(int userId, int active) throws Exception {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

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
        u.setActive(rs.getInt("active"));
        return u;
    }
    //
    
    // Check email tồn tại và lấy thông tin User
    public User getUserByEmail(String email) throws Exception {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // Cập nhật mật khẩu theo email
    public boolean updatePasswordByEmail(String email, String hashedPassword) throws Exception {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        }
    }
    // Check studentId đã tồn tại chưa, lấy thông tin User đang sở hữu studentId đó (nếu có)
public User getUserByStudentId(String studentId) throws Exception {
    String sql = "SELECT id, username, password, full_name, email, phone, student_id, avatar, role, active FROM users WHERE student_id = ?";
    try (Connection conn = DBContext.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, studentId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return mapRow(rs);
        }
    }
    return null;
}
}

