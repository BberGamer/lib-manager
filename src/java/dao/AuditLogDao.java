package dao;

import utils.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object cho quản lý và tra cứu Audit Logs hệ thống.
 */
public class AuditLogDao {

    /**
     * Tìm kiếm và phân trang Audit Logs theo nhiều tiêu chí.
     */
    public List<Map<String, Object>> searchAuditLogs(String action, String performedBy, String fromDate, String toDate, int pageNum, int pageSize) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT al.id, al.action, al.performed_by, al.target_user_id, al.detail, al.created_at, ")
           .append("u.username AS target_username, u.full_name AS target_fullname ")
           .append("FROM audit_logs al ")
           .append("LEFT JOIN users u ON al.target_user_id = u.id ")
           .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (action != null && !action.trim().isEmpty()) {
            sql.append("AND al.action = ? ");
            params.add(action.trim());
        }
        if (performedBy != null && !performedBy.trim().isEmpty()) {
            sql.append("AND al.performed_by LIKE ? ");
            params.add("%" + performedBy.trim() + "%");
        }
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            sql.append("AND al.created_at >= ? ");
            params.add(fromDate.trim() + " 00:00:00");
        }
        if (toDate != null && !toDate.trim().isEmpty()) {
            sql.append("AND al.created_at <= ? ");
            params.add(toDate.trim() + " 23:59:59");
        }

        sql.append("ORDER BY al.id ASC, al.created_at ASC LIMIT ?, ?");
        params.add((pageNum - 1) * pageSize);
        params.add(pageSize);

        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("action", rs.getString("action"));
                    row.put("performed_by", rs.getString("performed_by"));
                    row.put("target_user_id", rs.getInt("target_user_id"));
                    row.put("target_username", rs.getString("target_username"));
                    row.put("target_fullname", rs.getString("target_fullname"));
                    row.put("detail", rs.getString("detail"));
                    row.put("created_at", rs.getTimestamp("created_at"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    /**
     * Đếm tổng số bản ghi khớp điều kiện lọc phục vụ phân trang.
     */
    public int countAuditLogs(String action, String performedBy, String fromDate, String toDate) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM audit_logs WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (action != null && !action.trim().isEmpty()) {
            sql.append("AND action = ? ");
            params.add(action.trim());
        }
        if (performedBy != null && !performedBy.trim().isEmpty()) {
            sql.append("AND performed_by LIKE ? ");
            params.add("%" + performedBy.trim() + "%");
        }
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            sql.append("AND created_at >= ? ");
            params.add(fromDate.trim() + " 00:00:00");
        }
        if (toDate != null && !toDate.trim().isEmpty()) {
            sql.append("AND created_at <= ? ");
            params.add(toDate.trim() + " 23:59:59");
        }

        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Lấy danh sách tất cả các loại Action duy nhất hiện có trong DB để nạp vào dropdown bộ lọc.
     */
    public List<String> getDistinctActions() throws Exception {
        List<String> actions = new ArrayList<>();
        String sql = "SELECT DISTINCT action FROM audit_logs ORDER BY action ASC";
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String act = rs.getString("action");
                if (act != null && !act.trim().isEmpty()) {
                    actions.add(act);
                }
            }
        }
        return actions;
    }
}
