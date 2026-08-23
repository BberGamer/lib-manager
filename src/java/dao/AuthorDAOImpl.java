/**
 * DAO JDBC hiện thực toàn bộ truy vấn dữ liệu cho module quản lý tác giả.
 */
package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Author;
import utils.DBContext;

/**
 * Truy cập bảng authors bằng PreparedStatement và ánh xạ kết quả sang Author.
 */
public class AuthorDAOImpl implements AuthorDAO {

    private static final String COLUMNS = "id, name, nationality, birth_date, bio, avatar_url, "
            + "created_at, updated_at, is_deleted, created_by, updated_by";

    /** {@inheritDoc} */
    @Override
    public List<Author> findAll() throws SQLException, ClassNotFoundException {
        String sql = "SELECT " + COLUMNS + " FROM authors WHERE is_deleted = 0 ORDER BY name ASC";
        List<Author> authors = new ArrayList<>();
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                authors.add(mapAuthor(resultSet));
            }
        }
        return authors;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Author> findById(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT " + COLUMNS + " FROM authors WHERE id = ? AND is_deleted = 0";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapAuthor(resultSet)) : Optional.empty();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Author> search(String keyword, String sort, String order, int offset, int limit)
            throws SQLException, ClassNotFoundException {
        String sortColumn = switch (sort) {
            case "nationality" -> "nationality";
            case "birth_date" -> "birth_date";
            case "created_at" -> "created_at";
            default -> "name";
        };
        String sortOrder = "DESC".equalsIgnoreCase(order) ? "DESC" : "ASC";
        String sql = "SELECT " + COLUMNS + " FROM authors WHERE is_deleted = 0 "
                + "AND (? = '' OR LOWER(name) LIKE LOWER(?)) ORDER BY " + sortColumn + " "
                + sortOrder + ", id ASC LIMIT ? OFFSET ?";
        List<Author> authors = new ArrayList<>();
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            statement.setString(1, keyword);
            statement.setString(2, pattern);
            statement.setInt(3, limit);
            statement.setInt(4, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    authors.add(mapAuthor(resultSet));
                }
            }
        }
        return authors;
    }

    /** {@inheritDoc} */
    @Override
    public int count(String keyword) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM authors WHERE is_deleted = 0 "
                + "AND (? = '' OR LOWER(name) LIKE LOWER(?))";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            statement.setString(1, keyword);
            statement.setString(2, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByName(String name, int excludedId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM authors WHERE LOWER(name) = LOWER(?) AND id <> ? AND is_deleted = 0 LIMIT 1";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, excludedId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Author insert(Author author, String actor) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO authors (name, nationality, birth_date, bio, avatar_url, created_by, updated_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindAuthor(statement, author);
            statement.setString(6, actor);
            statement.setString(7, actor);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Không nhận được mã tác giả vừa tạo");
                }
                author.setId(keys.getInt(1));
            }
        }
        return findById(author.getId()).orElseThrow(() -> new SQLException("Không đọc lại được tác giả vừa tạo"));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Author> restoreDeleted(Author author, String actor)
            throws SQLException, ClassNotFoundException {
        String updateSql = "UPDATE authors SET name = ?, nationality = ?, birth_date = ?, bio = ?, avatar_url = ?, "
                + "is_deleted = 0, updated_by = ?, updated_at = NOW() "
                + "WHERE LOWER(name) = LOWER(?) AND is_deleted = 1 LIMIT 1";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(updateSql)) {
            bindAuthor(statement, author);
            statement.setString(6, actor);
            statement.setString(7, author.getName());
            if (statement.executeUpdate() == 0) {
                return Optional.empty();
            }
        }

        String selectSql = "SELECT " + COLUMNS + " FROM authors "
                + "WHERE LOWER(name) = LOWER(?) AND is_deleted = 0 LIMIT 1";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setString(1, author.getName());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapAuthor(resultSet)) : Optional.empty();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean update(Author author, String actor) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE authors SET name = ?, nationality = ?, birth_date = ?, bio = ?, avatar_url = ?, "
                + "updated_by = ? WHERE id = ? AND is_deleted = 0";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bindAuthor(statement, author);
            statement.setString(6, actor);
            statement.setInt(7, author.getId());
            return statement.executeUpdate() == 1;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasBooks(int authorId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM book_authors WHERE author_id = ? LIMIT 1";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, authorId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteById(int id, String actor) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE authors SET is_deleted = 1, updated_by = ?, updated_at = NOW() "
                + "WHERE id = ? AND is_deleted = 0";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, actor);
            statement.setInt(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Gán các trường nội dung chung cho câu lệnh thêm và cập nhật.
     * @param statement câu lệnh cần gán năm tham số đầu
     * @param author dữ liệu tác giả hợp lệ
     * @throws SQLException khi driver từ chối tham số
     */
    private void bindAuthor(PreparedStatement statement, Author author) throws SQLException {
        statement.setString(1, author.getName());
        statement.setString(2, author.getNationality());
        if (author.getBirthDate() == null) {
            statement.setNull(3, java.sql.Types.DATE);
        } else {
            statement.setDate(3, Date.valueOf(author.getBirthDate()));
        }
        statement.setString(4, author.getBio());
        statement.setString(5, author.getAvatarUrl());
    }

    /** @return connection theo cấu hình hiện tại của ứng dụng */
    private Connection openConnection() throws SQLException, ClassNotFoundException {
        return DBContext.getInstance().getConnection();
    }

    /**
     * Ánh xạ một dòng kết quả thành model Author đầy đủ.
     * @param resultSet dòng kết quả hiện tại
     * @return tác giả đã ánh xạ
     * @throws SQLException khi không đọc được cột
     */
    private Author mapAuthor(ResultSet resultSet) throws SQLException {
        Author author = new Author();
        author.setId(resultSet.getInt("id"));
        author.setName(resultSet.getString("name"));
        author.setNationality(resultSet.getString("nationality"));
        Date birthDate = resultSet.getDate("birth_date");
        author.setBirthDate(birthDate == null ? null : birthDate.toLocalDate());
        author.setBio(resultSet.getString("bio"));
        author.setAvatarUrl(resultSet.getString("avatar_url"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        author.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        author.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        author.setDeleted(resultSet.getBoolean("is_deleted"));
        author.setCreatedBy(resultSet.getString("created_by"));
        author.setUpdatedBy(resultSet.getString("updated_by"));
        return author;
    }
}
