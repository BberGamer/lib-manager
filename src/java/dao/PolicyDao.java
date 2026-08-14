/**
 * DAO quản lý SQL, giao dịch chuyển trạng thái và ánh xạ bảng policies.
 */
package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Policy;
import model.PolicyCategory;
import model.PolicyPublicationStatus;
import utils.DBContext;

/** Cung cấp thao tác lưu trữ Policy bằng PreparedStatement và try-with-resources. */
public class PolicyDao {

    private static final String COLUMNS = "id, policy_code, version, title, content, category, "
            + "publication_status, effective_from, effective_to, is_deleted, created_by, updated_by, "
            + "published_by, archived_by, created_at, updated_at, published_at, archived_at";

    /** Kết quả xuất bản có phân biệt xung đột khoảng hiệu lực. */
    public enum PublishResult {
        PUBLISHED, INVALID_STATE
    }

    /**
     * Lấy một trang điều lệ cho Admin theo từ khóa, danh mục và trạng thái.
     * @param keyword từ khóa đã chuẩn hóa
     * @param category danh mục cần lọc hoặc null
     * @param status trạng thái cần lọc hoặc null
     * @param offset vị trí bắt đầu
     * @param limit số bản ghi tối đa
     * @return danh sách điều lệ chưa xóa
     * @throws SQLException khi truy vấn thất bại
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public List<Policy> findAll(String keyword, PolicyCategory category,
            PolicyPublicationStatus status, int offset, int limit)
            throws SQLException, ClassNotFoundException {
        String sql = "SELECT " + COLUMNS + " FROM policies WHERE is_deleted = 0 "
                + "AND (? = '' OR LOWER(title) LIKE LOWER(?) OR LOWER(policy_code) LIKE LOWER(?)) "
                + "AND (? IS NULL OR category = ?) AND (? IS NULL OR publication_status = ?) "
                + "ORDER BY updated_at DESC, id DESC LIMIT ? OFFSET ?";
        List<Policy> policies = new ArrayList<>();
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bindFilters(statement, keyword, category, status);
            statement.setInt(8, limit);
            statement.setInt(9, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    policies.add(mapPolicy(resultSet));
                }
            }
        }
        return policies;
    }

    /** @return tổng số điều lệ quản trị phù hợp bộ lọc */
    public int count(String keyword, PolicyCategory category, PolicyPublicationStatus status)
            throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM policies WHERE is_deleted = 0 "
                + "AND (? = '' OR LOWER(title) LIKE LOWER(?) OR LOWER(policy_code) LIKE LOWER(?)) "
                + "AND (? IS NULL OR category = ?) AND (? IS NULL OR publication_status = ?)";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bindFilters(statement, keyword, category, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /** @return điều lệ chưa xóa theo mã định danh */
    public Optional<Policy> findById(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT " + COLUMNS + " FROM policies WHERE id = ? AND is_deleted = 0";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapPolicy(resultSet)) : Optional.empty();
            }
        }
    }

    /**
     * Lấy danh sách công khai, chỉ gồm điều lệ đang hiệu lực tại ngày nghiệp vụ.
     * @param keyword từ khóa tìm tiêu đề
     * @param category danh mục hoặc null
     * @param today ngày nghiệp vụ Việt Nam
     * @param offset vị trí bắt đầu
     * @param limit số bản ghi tối đa
     * @return danh sách đang hiệu lực
     */
    public List<Policy> findEffective(String keyword, PolicyCategory category, LocalDate today,
            int offset, int limit) throws SQLException, ClassNotFoundException {
        String sql = "SELECT " + COLUMNS + " FROM policies WHERE is_deleted = 0 "
                + "AND publication_status = 'PUBLISHED' AND effective_from <= ? "
                + "AND (effective_to IS NULL OR effective_to >= ?) "
                + "AND (? = '' OR LOWER(title) LIKE LOWER(?)) AND (? IS NULL OR category = ?) "
                + "ORDER BY effective_from DESC, title ASC LIMIT ? OFFSET ?";
        List<Policy> policies = new ArrayList<>();
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(today));
            statement.setDate(2, Date.valueOf(today));
            statement.setString(3, keyword);
            statement.setString(4, "%" + keyword + "%");
            bindNullableCategory(statement, 5, 6, category);
            statement.setInt(7, limit);
            statement.setInt(8, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    policies.add(mapPolicy(resultSet));
                }
            }
        }
        return policies;
    }

    /** @return số điều lệ công khai đang hiệu lực */
    public int countEffective(String keyword, PolicyCategory category, LocalDate today)
            throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM policies WHERE is_deleted = 0 "
                + "AND publication_status = 'PUBLISHED' AND effective_from <= ? "
                + "AND (effective_to IS NULL OR effective_to >= ?) "
                + "AND (? = '' OR LOWER(title) LIKE LOWER(?)) AND (? IS NULL OR category = ?)";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(today));
            statement.setDate(2, Date.valueOf(today));
            statement.setString(3, keyword);
            statement.setString(4, "%" + keyword + "%");
            bindNullableCategory(statement, 5, 6, category);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /** @return điều lệ theo id nếu đang có hiệu lực, ngược lại Optional rỗng */
    public Optional<Policy> findEffectiveById(int id, LocalDate today)
            throws SQLException, ClassNotFoundException {
        String sql = "SELECT " + COLUMNS + " FROM policies WHERE id = ? AND is_deleted = 0 "
                + "AND publication_status = 'PUBLISHED' AND effective_from <= ? "
                + "AND (effective_to IS NULL OR effective_to >= ?)";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setDate(2, Date.valueOf(today));
            statement.setDate(3, Date.valueOf(today));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapPolicy(resultSet)) : Optional.empty();
            }
        }
    }

    /** @return true nếu cặp mã và phiên bản đã tồn tại ở bản ghi chưa xóa */
    public boolean existsByCodeAndVersion(String code, int version, int excludedId)
            throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM policies WHERE LOWER(policy_code) = LOWER(?) "
                + "AND version = ? AND id <> ? AND is_deleted = 0 LIMIT 1";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            statement.setInt(2, version);
            statement.setInt(3, excludedId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /** @return điều lệ draft vừa được tạo và đọc lại */
    public Policy insert(Policy policy, String actor) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO policies (policy_code, version, title, content, category, "
                + "publication_status, effective_from, effective_to, created_by, updated_by) "
                + "VALUES (?, ?, ?, ?, ?, 'DRAFT', ?, ?, ?, ?)";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindInsertFields(statement, policy);
            statement.setString(8, actor);
            statement.setString(9, actor);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Không nhận được mã điều lệ vừa tạo");
                }
                policy.setId(keys.getInt(1));
            }
        }
        return findById(policy.getId()).orElseThrow(
                () -> new SQLException("Không đọc lại được điều lệ vừa tạo"));
    }

    /** @return true nếu cập nhật đúng một bản draft */
    public boolean updateDraft(Policy policy, String actor) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE policies SET policy_code = ?, title = ?, content = ?, "
                + "category = ?, effective_from = ?, effective_to = ?, updated_by = ? "
                + "WHERE id = ? AND publication_status = 'DRAFT' AND is_deleted = 0";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bindUpdateFields(statement, policy);
            statement.setString(7, actor);
            statement.setInt(8, policy.getId());
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Sao chép bản đã xuất bản thành draft với số phiên bản kế tiếp trong cùng giao dịch.
     * @param sourceId ID phiên bản đã xuất bản
     * @param revision nội dung Admin đã xác nhận trên form revision
     * @param actor tài khoản tạo phiên bản mới
     * @return draft mới hoặc draft cùng mã đang tồn tại; rỗng nếu nguồn không hợp lệ
     * @throws SQLException khi thao tác database thất bại
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public Optional<Policy> createRevision(int sourceId, Policy revision, String actor)
            throws SQLException, ClassNotFoundException {
        Connection connection = openConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        int revisionId = 0;
        Policy existingDraft = null;
        try {
            connection.setAutoCommit(false);
            Policy source = findPublishedForUpdate(connection, sourceId);
            if (source == null) {
                connection.rollback();
                return Optional.empty();
            }
            existingDraft = findDraftByCodeForUpdate(connection, source.getPolicyCode());
            if (existingDraft != null) {
                connection.commit();
            } else {
                int nextVersion = findNextVersion(connection, source.getPolicyCode());
                String sql = "INSERT INTO policies (policy_code, version, title, content, category, "
                        + "publication_status, effective_from, effective_to, created_by, updated_by) "
                        + "VALUES (?, ?, ?, ?, ?, 'DRAFT', ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, source.getPolicyCode());
                    statement.setInt(2, nextVersion);
                    statement.setString(3, revision.getTitle());
                    statement.setString(4, revision.getContent());
                    statement.setString(5, revision.getCategory().name());
                    statement.setDate(6, toSqlDate(revision.getEffectiveFrom()));
                    statement.setDate(7, toSqlDate(revision.getEffectiveTo()));
                    statement.setString(8, actor);
                    statement.setString(9, actor);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Không nhận được ID phiên bản điều lệ mới");
                        }
                        revisionId = keys.getInt(1);
                    }
                }
                connection.commit();
            }
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
            connection.close();
        }

        if (existingDraft != null) {
            return Optional.of(existingDraft);
        }
        return findById(revisionId);
    }

    /**
     * Tìm draft chưa xóa của một mã điều lệ để tiếp tục chỉnh sửa thay vì tạo trùng.
     * @param policyCode mã nghiệp vụ của điều lệ
     * @return draft hiện tại hoặc Optional rỗng
     * @throws SQLException khi truy vấn thất bại
     * @throws ClassNotFoundException khi thiếu JDBC driver
     */
    public Optional<Policy> findDraftByCode(String policyCode)
            throws SQLException, ClassNotFoundException {
        String sql = "SELECT " + COLUMNS + " FROM policies WHERE policy_code = ? "
                + "AND publication_status = 'DRAFT' AND is_deleted = 0 "
                + "ORDER BY version DESC LIMIT 1";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, policyCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapPolicy(resultSet)) : Optional.empty();
            }
        }
    }

    /**
     * Xuất bản draft trong giao dịch và khóa các phiên bản cùng mã khi kiểm tra chồng ngày.
     * @return kết quả phân biệt thành công, trạng thái sai hoặc chồng khoảng hiệu lực
     */
    public PublishResult publish(int id, String actor) throws SQLException, ClassNotFoundException {
        Connection connection = openConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            Policy target = findDraftForUpdate(connection, id);
            if (target == null) {
                connection.rollback();
                return PublishResult.INVALID_STATE;
            }
            archivePublishedVersions(connection, target, actor);
            String sql = "UPDATE policies SET publication_status = 'PUBLISHED', published_by = ?, "
                    + "published_at = NOW(), updated_by = ? WHERE id = ? "
                    + "AND publication_status = 'DRAFT' AND is_deleted = 0";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, actor);
                statement.setString(2, actor);
                statement.setInt(3, id);
                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return PublishResult.INVALID_STATE;
                }
            }
            connection.commit();
            return PublishResult.PUBLISHED;
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
            connection.close();
        }
    }

    /** @return true nếu lưu trữ đúng một bản đã xuất bản */
    public boolean archive(int id, String actor) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE policies SET publication_status = 'ARCHIVED', archived_by = ?, "
                + "archived_at = NOW(), updated_by = ? WHERE id = ? "
                + "AND publication_status = 'PUBLISHED' AND is_deleted = 0";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, actor);
            statement.setString(2, actor);
            statement.setInt(3, id);
            return statement.executeUpdate() == 1;
        }
    }

    /** @return true nếu xóa mềm đúng một bản draft */
    public boolean deleteDraft(int id, String actor) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE policies SET is_deleted = 1, updated_by = ? WHERE id = ? "
                + "AND publication_status = 'DRAFT' AND is_deleted = 0";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, actor);
            statement.setInt(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    /** Gán bộ lọc quản trị vào statement theo đúng thứ tự placeholder. */
    private void bindFilters(PreparedStatement statement, String keyword, PolicyCategory category,
            PolicyPublicationStatus status) throws SQLException {
        String searchPattern = "%" + keyword + "%";
        statement.setString(1, keyword);
        statement.setString(2, searchPattern);
        statement.setString(3, searchPattern);
        bindNullableCategory(statement, 4, 5, category);
        String statusValue = status == null ? null : status.name();
        statement.setString(6, statusValue);
        statement.setString(7, statusValue);
    }

    /** Gán hai placeholder của bộ lọc category nullable. */
    private void bindNullableCategory(PreparedStatement statement, int nullIndex, int valueIndex,
            PolicyCategory category) throws SQLException {
        String value = category == null ? null : category.name();
        statement.setString(nullIndex, value);
        statement.setString(valueIndex, value);
    }

    /** Gán các trường khi tạo draft mới, bao gồm phiên bản do service quản lý. */
    private void bindInsertFields(PreparedStatement statement, Policy policy) throws SQLException {
        statement.setString(1, policy.getPolicyCode());
        statement.setInt(2, policy.getVersion());
        statement.setString(3, policy.getTitle());
        statement.setString(4, policy.getContent());
        statement.setString(5, policy.getCategory().name());
        statement.setDate(6, toSqlDate(policy.getEffectiveFrom()));
        statement.setDate(7, toSqlDate(policy.getEffectiveTo()));
    }

    /** Gán các trường được phép sửa của draft và không thay đổi số phiên bản. */
    private void bindUpdateFields(PreparedStatement statement, Policy policy) throws SQLException {
        statement.setString(1, policy.getPolicyCode());
        statement.setString(2, policy.getTitle());
        statement.setString(3, policy.getContent());
        statement.setString(4, policy.getCategory().name());
        statement.setDate(5, toSqlDate(policy.getEffectiveFrom()));
        statement.setDate(6, toSqlDate(policy.getEffectiveTo()));
    }

    /** @return draft đã khóa hoặc null nếu không tồn tại/không đúng trạng thái */
    private Policy findDraftForUpdate(Connection connection, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM policies WHERE id = ? AND is_deleted = 0 "
                + "AND publication_status = 'DRAFT' FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapPolicy(resultSet) : null;
            }
        }
    }

    /** @return bản xuất bản đã khóa, hoặc null nếu không tồn tại/không đúng trạng thái */
    private Policy findPublishedForUpdate(Connection connection, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM policies WHERE id = ? AND is_deleted = 0 "
                + "AND publication_status = 'PUBLISHED' FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapPolicy(resultSet) : null;
            }
        }
    }

    /** @return draft cùng mã đã khóa, hoặc null nếu chưa tồn tại */
    private Policy findDraftByCodeForUpdate(Connection connection, String policyCode)
            throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM policies WHERE policy_code = ? "
                + "AND publication_status = 'DRAFT' AND is_deleted = 0 "
                + "ORDER BY version DESC LIMIT 1 FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, policyCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapPolicy(resultSet) : null;
            }
        }
    }

    /**
     * Tính số phiên bản kế tiếp trên toàn bộ lịch sử, kể cả bản đã xóa mềm, để không tái sử dụng
     * cặp mã và phiên bản đang được unique constraint của database bảo vệ.
     * @param connection kết nối thuộc giao dịch tạo revision
     * @param policyCode mã nghiệp vụ của điều lệ
     * @return số phiên bản chưa từng được sử dụng của cùng mã
     * @throws SQLException khi không thể đọc lịch sử phiên bản
     */
    private int findNextVersion(Connection connection, String policyCode) throws SQLException {
        String sql = "SELECT COALESCE(MAX(version), 0) + 1 FROM policies "
                + "WHERE policy_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, policyCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /** Lưu trữ phiên bản đang xuất bản cùng mã trước khi kích hoạt bản thay thế. */
    private void archivePublishedVersions(Connection connection, Policy target, String actor)
            throws SQLException {
        String sql = "UPDATE policies SET publication_status = 'ARCHIVED', archived_by = ?, "
                + "archived_at = NOW(), updated_by = ? WHERE policy_code = ? AND id <> ? "
                + "AND publication_status = 'PUBLISHED' AND is_deleted = 0";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, actor);
            statement.setString(2, actor);
            statement.setString(3, target.getPolicyCode());
            statement.setInt(4, target.getId());
            statement.executeUpdate();
        }
    }

    /** @return connection mới theo cơ chế hiện có của dự án */
    private Connection openConnection() throws SQLException, ClassNotFoundException {
        return DBContext.getInstance().getConnection();
    }

    /** @return java.sql.Date tương ứng hoặc null */
    private Date toSqlDate(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }

    /** Ánh xạ hàng kết quả sang Policy và kiểm tra enum lưu trong database. */
    private Policy mapPolicy(ResultSet resultSet) throws SQLException {
        Policy policy = new Policy();
        policy.setId(resultSet.getInt("id"));
        policy.setPolicyCode(resultSet.getString("policy_code"));
        policy.setVersion(resultSet.getInt("version"));
        policy.setTitle(resultSet.getString("title"));
        policy.setContent(resultSet.getString("content"));
        try {
            policy.setCategory(PolicyCategory.valueOf(resultSet.getString("category")));
            policy.setPublicationStatus(PolicyPublicationStatus.valueOf(
                    resultSet.getString("publication_status")));
        } catch (IllegalArgumentException exception) {
            throw new SQLException("Dữ liệu enum của điều lệ không hợp lệ", exception);
        }
        Date effectiveFrom = resultSet.getDate("effective_from");
        Date effectiveTo = resultSet.getDate("effective_to");
        policy.setEffectiveFrom(effectiveFrom == null ? null : effectiveFrom.toLocalDate());
        policy.setEffectiveTo(effectiveTo == null ? null : effectiveTo.toLocalDate());
        policy.setDeleted(resultSet.getBoolean("is_deleted"));
        policy.setCreatedBy(resultSet.getString("created_by"));
        policy.setUpdatedBy(resultSet.getString("updated_by"));
        policy.setPublishedBy(resultSet.getString("published_by"));
        policy.setArchivedBy(resultSet.getString("archived_by"));
        policy.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        policy.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        policy.setPublishedAt(toLocalDateTime(resultSet.getTimestamp("published_at")));
        policy.setArchivedAt(toLocalDateTime(resultSet.getTimestamp("archived_at")));
        return policy;
    }

    /** @return LocalDateTime tương ứng hoặc null */
    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
