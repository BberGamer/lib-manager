package dao;

import utils.DBContext;
import model.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean createNotification(Notification notif) throws Exception {
        String sql = "INSERT INTO notifications (user_id, title, message, type, is_read, reference_id, reference_type, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notif.getUserId());
            ps.setString(2, notif.getTitle());
            ps.setString(3, notif.getMessage());
            ps.setString(4, notif.getType());
            ps.setBoolean(5, notif.isIsRead());
            if (notif.getReferenceId() != null) {
                ps.setInt(6, notif.getReferenceId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, notif.getReferenceType());
            ps.setTimestamp(8, Timestamp.valueOf(notif.getCreatedAt() != null ? notif.getCreatedAt() : LocalDateTime.now()));
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        notif.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public List<Notification> getNotificationsByUserId(int userId, int pageNum, int pageSize) throws Exception {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ?, ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, (pageNum - 1) * pageSize);
            ps.setInt(3, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int countNotificationsByUserId(int userId) throws Exception {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getUnreadCount(int userId) throws Exception {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean markAsRead(int id) throws Exception {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean markAllAsRead(int userId) throws Exception {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setIsRead(rs.getBoolean("is_read"));
        
        int refId = rs.getInt("reference_id");
        if (!rs.wasNull()) {
            n.setReferenceId(refId);
        }
        n.setReferenceType(rs.getString("reference_type"));
        
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) {
            n.setCreatedAt(t.toLocalDateTime());
        }
        return n;
    }

    public List<Notification> getAllSentNotifications(String type, int pageNum, int pageSize) throws Exception {
        List<Notification> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT n.*, u.username, u.full_name, u.email, u.phone ")
          .append("FROM notifications n ")
          .append("INNER JOIN users u ON n.user_id = u.id ")
          .append("WHERE 1=1 ");
        if (type != null && !type.trim().isEmpty()) {
            sb.append("AND n.type = ? ");
        }
        sb.append("ORDER BY n.created_at DESC LIMIT ?, ?");

        try (Connection conn = utils.DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (type != null && !type.trim().isEmpty()) {
                ps.setString(idx++, type.trim());
            }
            ps.setInt(idx++, (pageNum - 1) * pageSize);
            ps.setInt(idx++, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = mapRow(rs);
                    model.User u = new model.User();
                    u.setId(rs.getInt("user_id"));
                    u.setUsername(rs.getString("username"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    n.setUser(u);
                    list.add(n);
                }
            }
        }
        return list;
    }

    public int countAllSentNotifications(String type) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) FROM notifications n WHERE 1=1 ");
        if (type != null && !type.trim().isEmpty()) {
            sb.append("AND n.type = ? ");
        }
        try (Connection conn = utils.DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            if (type != null && !type.trim().isEmpty()) {
                ps.setString(1, type.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
}
