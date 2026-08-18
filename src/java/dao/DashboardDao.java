package dao;

import utils.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO phục vụ lấy các chỉ số thống kê cho Dashboard.
 */
public class DashboardDao {

    // =========================================================================
    // LIBRARY STATISTICS
    // =========================================================================

    public int getTotalBooks() throws Exception {
        String sql = "SELECT COUNT(*) FROM books WHERE is_deleted = 0";
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getTotalCopies() throws Exception {
        String sql = "SELECT COUNT(*) FROM book_copies WHERE is_deleted = 0";
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public Map<String, Integer> getCopiesCountByStatus() throws Exception {
        String sql = "SELECT status, COUNT(*) FROM book_copies WHERE is_deleted = 0 GROUP BY status";
        Map<String, Integer> map = new HashMap<>();
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
        }
        return map;
    }

    public Map<String, Integer> getCopiesCountByCondition() throws Exception {
        String sql = "SELECT book_condition, COUNT(*) FROM book_copies WHERE is_deleted = 0 GROUP BY book_condition";
        Map<String, Integer> map = new HashMap<>();
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
        }
        return map;
    }

    public List<Map<String, Object>> getTopBorrowedBooks(int limit) throws Exception {
        String sql = "SELECT b.id, b.title, b.isbn, COUNT(br.id) AS borrow_count "
                   + "FROM borrow_records br "
                   + "JOIN books b ON br.book_id = b.id "
                   + "GROUP BY b.id, b.title, b.isbn "
                   + "ORDER BY borrow_count DESC LIMIT ?";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("title", rs.getString("title"));
                    row.put("isbn", rs.getString("isbn"));
                    row.put("borrow_count", rs.getInt("borrow_count"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public List<Map<String, Object>> getTopOverdueUsers(int limit) throws Exception {
        String sql = "SELECT u.id, u.username, u.full_name, COUNT(br.id) AS overdue_count "
                   + "FROM borrow_records br "
                   + "JOIN users u ON br.user_id = u.id "
                   + "WHERE br.status = 'OVERDUE' "
                   + "GROUP BY u.id, u.username, u.full_name "
                   + "ORDER BY overdue_count DESC LIMIT ?";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("username", rs.getString("username"));
                    row.put("full_name", rs.getString("full_name"));
                    row.put("overdue_count", rs.getInt("overdue_count"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    // =========================================================================
    // ADMIN STATISTICS
    // =========================================================================

    public Map<String, Integer> getUsersCountByRole() throws Exception {
        String sql = "SELECT role, COUNT(*) FROM users GROUP BY role";
        Map<String, Integer> map = new HashMap<>();
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
        }
        return map;
    }

    public Map<String, java.math.BigDecimal> getFinesStats() throws Exception {
        String sql = "SELECT status, SUM(amount) FROM fines GROUP BY status";
        Map<String, java.math.BigDecimal> map = new HashMap<>();
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String status = rs.getString(1);
                java.math.BigDecimal amount = rs.getBigDecimal(2);
                map.put(status, amount != null ? amount : java.math.BigDecimal.ZERO);
            }
        }
        return map;
    }

    public List<Map<String, Object>> getRecentAuditLogs(int limit) throws Exception {
        String sql = "SELECT id, action, performed_by, target_user_id, detail, created_at "
                   + "FROM audit_logs ORDER BY id ASC, created_at ASC LIMIT ?";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("action", rs.getString("action"));
                    row.put("performed_by", rs.getString("performed_by"));
                    row.put("target_user_id", rs.getInt("target_user_id"));
                    row.put("detail", rs.getString("detail"));
                    row.put("created_at", rs.getTimestamp("created_at"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    /** Trả về danh sách {action, count} theo tần suất giảm dần để vẽ Donut Chart */
    public List<Map<String, Object>> getAuditLogActionCounts() throws Exception {
        String sql = "SELECT action, COUNT(*) AS cnt FROM audit_logs GROUP BY action ORDER BY cnt DESC";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("action", rs.getString("action"));
                row.put("count", rs.getInt("cnt"));
                list.add(row);
            }
        }
        return list;
    }
}
