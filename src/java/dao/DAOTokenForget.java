package dao;

import utils.DBContext;
import model.TokenForgetPassword;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class DAOTokenForget {

    public boolean insertTokenForget(TokenForgetPassword tokenForget) throws Exception {
        String sql = "INSERT INTO tokenForgetPassword (token, expiryTime, isUsed, userId) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenForget.getToken());
            ps.setTimestamp(2, Timestamp.valueOf(tokenForget.getExpiryTime()));
            ps.setBoolean(3, tokenForget.isIsUsed());
            ps.setInt(4, tokenForget.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public TokenForgetPassword getTokenPassword(String token) throws Exception {
        String sql = "SELECT * FROM tokenForgetPassword WHERE token = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TokenForgetPassword(
                            rs.getInt("id"),
                            rs.getInt("userId"),
                            rs.getBoolean("isUsed"),
                            rs.getString("token"),
                            rs.getTimestamp("expiryTime").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }

    public boolean updateStatus(TokenForgetPassword token) throws Exception {
        String sql = "UPDATE tokenForgetPassword SET isUsed = ? WHERE token = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, token.isIsUsed());
            ps.setString(2, token.getToken());
            return ps.executeUpdate() > 0;
        }
    }
}