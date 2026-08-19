/**
 * DAO quản lý ghi nhận yêu cầu nhận lại đồ để quên.
 * Lớp chỉ chứa thao tác JDBC với bảng found_item_claims.
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import model.FoundItemClaim;
import model.FoundItemClaimStatus;

/**
 * Ghi yêu cầu nhận lại của Reader trong giao dịch do FoundItemService điều phối.
 */
public class FoundItemClaimDao {

    /**
     * Thêm một yêu cầu nhận lại đang chờ Thủ thư xác minh.
     *
     * @param connection kết nối thuộc giao dịch hiện tại
     * @param claim yêu cầu đã được service kiểm tra
     * @throws SQLException khi không thể ghi dữ liệu
     */
    public void insertPending(Connection connection, FoundItemClaim claim) throws SQLException {
        String sql = "INSERT INTO found_item_claims (item_id, user_id, claim_note, status) "
                + "VALUES (?, ?, ?, 'PENDING')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, claim.getItemId());
            statement.setInt(2, claim.getUserId());
            statement.setString(3, claim.getClaimNote());
            statement.executeUpdate();
        }
    }

    /**
     * Đọc yêu cầu đang chờ xác minh của một đồ để quên cùng thông tin Reader.
     *
     * @param itemId mã đồ để quên
     * @return yêu cầu pending nếu có
     * @throws SQLException khi không thể đọc dữ liệu
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public Optional<FoundItemClaim> findPendingByItemId(int itemId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT c.id, c.item_id, c.user_id, c.claim_note, c.status, c.handled_by, "
                + "c.created_at, c.handled_at, u.full_name, u.username "
                + "FROM found_item_claims c INNER JOIN users u ON u.id = c.user_id "
                + "WHERE c.item_id = ? AND c.status = 'PENDING' ORDER BY c.created_at DESC LIMIT 1";
        try (Connection connection = utils.DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapClaim(resultSet, true)) : Optional.empty();
            }
        }
    }

    /**
     * Đọc yêu cầu gần nhất của một đồ để hiển thị đúng bước bàn giao.
     *
     * @param itemId mã đồ để quên
     * @return yêu cầu gần nhất nếu có
     * @throws SQLException khi không thể đọc dữ liệu
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public Optional<FoundItemClaim> findLatestByItemId(int itemId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT c.id, c.item_id, c.user_id, c.claim_note, c.status, c.handled_by, "
                + "c.created_at, c.handled_at, u.full_name, u.username "
                + "FROM found_item_claims c INNER JOIN users u ON u.id = c.user_id "
                + "WHERE c.item_id = ? ORDER BY c.created_at DESC LIMIT 1";
        try (Connection connection = utils.DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapClaim(resultSet, true)) : Optional.empty();
            }
        }
    }

    /**
     * Lấy các yêu cầu còn cần theo dõi của chính Reader.
     *
     * @param userId mã Reader
     * @return danh sách yêu cầu đang xử lý
     * @throws SQLException khi không thể đọc dữ liệu
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public java.util.List<FoundItemClaim> findOpenByUserId(int userId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT c.id, c.item_id, c.user_id, c.claim_note, c.status, c.handled_by, "
                + "c.created_at, c.handled_at, f.item_name "
                + "FROM found_item_claims c INNER JOIN found_items f ON f.id = c.item_id "
                + "WHERE c.user_id = ? AND c.status IN ('PENDING', 'APPROVED', 'READER_CONFIRMED') "
                + "ORDER BY c.created_at DESC";
        java.util.List<FoundItemClaim> claims = new java.util.ArrayList<>();
        try (Connection connection = utils.DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    FoundItemClaim claim = mapClaim(resultSet, false);
                    claim.setItemName(resultSet.getString("item_name"));
                    claims.add(claim);
                }
            }
        }
        return claims;
    }

    /**
     * Lấy toàn bộ lịch sử yêu cầu của Reader, gồm cả yêu cầu đã hoàn tất hoặc bị từ chối.
     *
     * @param userId mã Reader
     * @return danh sách yêu cầu theo thứ tự mới nhất
     * @throws SQLException khi không thể đọc dữ liệu
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public java.util.List<FoundItemClaim> findAllByUserId(int userId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT c.id, c.item_id, c.user_id, c.claim_note, c.status, c.handled_by, "
                + "c.created_at, c.handled_at, f.item_name "
                + "FROM found_item_claims c INNER JOIN found_items f ON f.id = c.item_id "
                + "WHERE c.user_id = ? ORDER BY c.created_at DESC";
        java.util.List<FoundItemClaim> claims = new java.util.ArrayList<>();
        try (Connection connection = utils.DBContext.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    FoundItemClaim claim = mapClaim(resultSet, false);
                    claim.setItemName(resultSet.getString("item_name"));
                    claims.add(claim);
                }
            }
        }
        return claims;
    }

    /**
     * Khóa yêu cầu pending để service quyết định chấp nhận hoặc từ chối an toàn.
     *
     * @param connection kết nối thuộc giao dịch hiện tại
     * @param claimId mã yêu cầu
     * @return yêu cầu pending nếu có
     * @throws SQLException khi không thể khóa dữ liệu
     */
    public Optional<FoundItemClaim> findPendingByIdForUpdate(Connection connection, int claimId) throws SQLException {
        String sql = "SELECT id, item_id, user_id, claim_note, status, handled_by, created_at, handled_at "
                + "FROM found_item_claims WHERE id = ? AND status = 'PENDING' FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, claimId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapClaim(resultSet, false)) : Optional.empty();
            }
        }
    }

    /**
     * Khóa một yêu cầu để chuyển sang bước bàn giao kế tiếp.
     *
     * @param connection kết nối thuộc giao dịch hiện tại
     * @param claimId mã yêu cầu
     * @return yêu cầu nếu tồn tại
     * @throws SQLException khi không thể khóa dữ liệu
     */
    public Optional<FoundItemClaim> findByIdForUpdate(Connection connection, int claimId) throws SQLException {
        String sql = "SELECT id, item_id, user_id, claim_note, status, handled_by, created_at, handled_at "
                + "FROM found_item_claims WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, claimId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapClaim(resultSet, false)) : Optional.empty();
            }
        }
    }

    /**
     * Lưu kết quả xác minh do Thủ thư thực hiện.
     *
     * @param connection kết nối thuộc giao dịch hiện tại
     * @param claimId mã yêu cầu
     * @param staffUserId mã Thủ thư xác minh
     * @param status kết quả APPROVED hoặc REJECTED
     * @throws SQLException khi cập nhật thất bại
     */
    public void updateDecision(Connection connection, int claimId, int staffUserId, FoundItemClaimStatus status)
            throws SQLException {
        String sql = "UPDATE found_item_claims SET status = ?, handled_by = ?, handled_at = NOW() "
                + "WHERE id = ? AND status = 'PENDING'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, staffUserId);
            statement.setInt(3, claimId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Yêu cầu đã được xử lý bởi thao tác khác.");
            }
        }
    }

    /**
     * Chuyển trạng thái yêu cầu khi đúng trạng thái hiện tại đã xác định.
     *
     * @param connection kết nối thuộc giao dịch hiện tại
     * @param claimId mã yêu cầu
     * @param expectedStatus trạng thái phải có trước khi chuyển
     * @param targetStatus trạng thái cần chuyển đến
     * @param handledBy mã Thủ thư hoặc null khi Reader tự xác nhận
     * @throws SQLException khi trạng thái đã bị thao tác khác thay đổi
     */
    public void transitionStatus(Connection connection, int claimId, FoundItemClaimStatus expectedStatus,
            FoundItemClaimStatus targetStatus, Integer handledBy) throws SQLException {
        String sql = "UPDATE found_item_claims SET status = ?, handled_by = COALESCE(?, handled_by), "
                + "handled_at = CASE WHEN ? IS NULL THEN handled_at ELSE NOW() END "
                + "WHERE id = ? AND status = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, targetStatus.name());
            if (handledBy == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, handledBy);
                statement.setInt(3, handledBy);
            }
            statement.setInt(4, claimId);
            statement.setString(5, expectedStatus.name());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Yêu cầu đã được xử lý bởi thao tác khác.");
            }
        }
    }

    /**
     * Ánh xạ một hàng truy vấn sang yêu cầu nhận đồ.
     *
     * @param resultSet hàng dữ liệu hiện tại
     * @return yêu cầu đã ánh xạ
     * @throws SQLException khi không đọc được cột dữ liệu
     */
    private FoundItemClaim mapClaim(ResultSet resultSet, boolean includesReader) throws SQLException {
        FoundItemClaim claim = new FoundItemClaim();
        claim.setId(resultSet.getInt("id"));
        claim.setItemId(resultSet.getInt("item_id"));
        claim.setUserId(resultSet.getInt("user_id"));
        claim.setClaimNote(resultSet.getString("claim_note"));
        claim.setStatus(FoundItemClaimStatus.valueOf(resultSet.getString("status")));
        int handledBy = resultSet.getInt("handled_by");
        claim.setHandledBy(resultSet.wasNull() ? null : handledBy);
        claim.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        claim.setHandledAt(toLocalDateTime(resultSet.getTimestamp("handled_at")));
        if (includesReader) {
            claim.setReaderName(resultSet.getString("full_name"));
            claim.setReaderUsername(resultSet.getString("username"));
        }
        return claim;
    }

    /**
     * Chuyển Timestamp nullable sang LocalDateTime.
     *
     * @param timestamp thời điểm JDBC có thể null
     * @return thời điểm tương ứng hoặc null
     */
    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
