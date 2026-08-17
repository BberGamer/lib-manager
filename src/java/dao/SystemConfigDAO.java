package dao;

import utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SystemConfigDAO {

    public SystemConfigDAO() {
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS system_settings ("
                + "setting_key VARCHAR(100) PRIMARY KEY, "
                + "setting_value TEXT, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            // Ignore if table already exists or permission issues
        }
    }

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString("setting_value");
                    return val != null ? val : defaultValue;
                }
            }
        } catch (Exception e) {
            // fallback to default
        }
        return defaultValue;
    }

    public boolean setSetting(String key, String value) {
        String sql = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), updated_at = NOW()";
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isAutoEmailEnabled() {
        return "true".equalsIgnoreCase(getSetting("AUTO_EMAIL_ENABLED", "true"));
    }

    public void setAutoEmailEnabled(boolean enabled) {
        setSetting("AUTO_EMAIL_ENABLED", String.valueOf(enabled));
    }

    public boolean isAutoJobEnabled() {
        return "true".equalsIgnoreCase(getSetting("AUTO_JOB_ENABLED", "true"));
    }

    public void setAutoJobEnabled(boolean enabled) {
        setSetting("AUTO_JOB_ENABLED", String.valueOf(enabled));
    }
}
