package dao;

import model.Book;
import model.Author;
import utils.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAOImpl implements BookDAO {

    @Override
    public Book findById(int id) throws Exception {
        String sql = "SELECT id, isbn, title, category, category_id, publisher, publish_year, price, quantity, available, description, cover_image, subject, is_deleted FROM books WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Book> searchBooks(String keyword, String category, String sort, String order, int page, int pageSize) throws Exception {
        List<Book> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT DISTINCT b.id, b.isbn, b.title, b.category, b.category_id, b.publisher, b.publish_year, b.price, b.quantity, b.available, b.description, b.cover_image, b.subject ")
          .append("FROM books b ")
          .append("LEFT JOIN book_authors ba ON b.id = ba.book_id ")
          .append("LEFT JOIN authors a ON ba.author_id = a.id AND a.is_deleted = 0 ")
          .append("WHERE b.is_deleted = 0 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (b.title LIKE ? OR b.isbn LIKE ? OR a.name LIKE ?) ");
        }
        if (category != null && !category.trim().isEmpty()) {
            sb.append("AND b.category = ? ");
        }

        // Sorting
        String sortCol = "b.title";
        if ("publish_year".equals(sort)) sortCol = "b.publish_year";
        else if ("available".equals(sort)) sortCol = "b.available";
        else if ("price".equals(sort)) sortCol = "b.price";
        else if ("created_at".equals(sort)) sortCol = "b.created_at";
        else if ("id".equals(sort)) sortCol = "b.id";
        
        String sortOrder = "ASC";
        if ("DESC".equalsIgnoreCase(order)) sortOrder = "DESC";

        sb.append("ORDER BY ").append(sortCol).append(" ").append(sortOrder).append(" ");
        sb.append("LIMIT ?, ?");

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lk = "%" + keyword.trim() + "%";
                ps.setString(idx++, lk);
                ps.setString(idx++, lk);
                ps.setString(idx++, lk);
            }
            if (category != null && !category.trim().isEmpty()) {
                ps.setString(idx++, category.trim());
            }
            int offset = (page - 1) * pageSize;
            ps.setInt(idx++, offset);
            ps.setInt(idx++, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public int countBooks(String keyword, String category) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(DISTINCT b.id) FROM books b ")
          .append("LEFT JOIN book_authors ba ON b.id = ba.book_id ")
          .append("LEFT JOIN authors a ON ba.author_id = a.id AND a.is_deleted = 0 ")
          .append("WHERE b.is_deleted = 0 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (b.title LIKE ? OR b.isbn LIKE ? OR a.name LIKE ?) ");
        }
        if (category != null && !category.trim().isEmpty()) {
            sb.append("AND b.category = ? ");
        }

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lk = "%" + keyword.trim() + "%";
                ps.setString(idx++, lk);
                ps.setString(idx++, lk);
                ps.setString(idx++, lk);
            }
            if (category != null && !category.trim().isEmpty()) {
                ps.setString(idx++, category.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public List<String> getAllCategories() throws Exception {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM books WHERE is_deleted = 0 AND category IS NOT NULL AND category != '' ORDER BY category ASC";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("category"));
            }
        }
        return list;
    }

    @Override
    public boolean isIsbnExists(String isbn) throws Exception {
        String sql = "SELECT COUNT(*) FROM books WHERE isbn = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public boolean isIsbnExistsExcluding(String isbn, int excludeId) throws Exception {
        String sql = "SELECT COUNT(*) FROM books WHERE isbn = ? AND id != ? AND is_deleted = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public int createBook(Book book) throws Exception {
        String sql = "INSERT INTO books (isbn, title, category, category_id, publisher, publish_year, price, quantity, available, description, cover_image, subject, is_deleted, created_at, updated_at, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, NOW(), NOW(), ?, ?)";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getCategory());
            if (book.getCategoryId() > 0) ps.setInt(4, book.getCategoryId());
            else ps.setNull(4, Types.INTEGER);
            ps.setString(5, book.getPublisher());
            if (book.getPublishYear() != null) ps.setInt(6, book.getPublishYear());
            else ps.setNull(6, Types.INTEGER);
            if (book.getPrice() != null) ps.setInt(7, book.getPrice());
            else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, book.getQuantity());
            ps.setInt(9, book.getAvailable());
            ps.setString(10, book.getDescription());
            ps.setString(11, book.getCoverImage());
            ps.setString(12, book.getSubject());
            ps.setString(13, book.getCreatedBy());
            ps.setString(14, book.getUpdatedBy());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    @Override
    public boolean updateBook(Book book) throws Exception {
        String sql = "UPDATE books SET isbn = ?, title = ?, category = ?, category_id = ?, publisher = ?, publish_year = ?, price = ?, quantity = ?, available = ?, description = ?, cover_image = ?, subject = ?, updated_at = NOW(), updated_by = ? WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getCategory());
            if (book.getCategoryId() > 0) ps.setInt(4, book.getCategoryId());
            else ps.setNull(4, Types.INTEGER);
            ps.setString(5, book.getPublisher());
            if (book.getPublishYear() != null) ps.setInt(6, book.getPublishYear());
            else ps.setNull(6, Types.INTEGER);
            if (book.getPrice() != null) ps.setInt(7, book.getPrice());
            else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, book.getQuantity());
            ps.setInt(9, book.getAvailable());
            ps.setString(10, book.getDescription());
            ps.setString(11, book.getCoverImage());
            ps.setString(12, book.getSubject());
            ps.setString(13, book.getUpdatedBy());
            ps.setInt(14, book.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteBook(int id, String operator) throws Exception {
        if (hasPhysicalCopies(id)) {
            throw new IllegalStateException("Không thể xóa sách: còn bản sao vật lý liên kết.");
        }
        String sql = "UPDATE books SET is_deleted = 1, updated_by = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, operator);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Author> getAuthorsByBookId(int bookId) throws Exception {
        List<Author> list = new ArrayList<>();
        String sql = "SELECT a.id, a.name, a.nationality, a.birth_date, a.bio, a.avatar_url FROM authors a INNER JOIN book_authors ba ON a.id = ba.author_id WHERE ba.book_id = ? AND a.is_deleted = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Author a = new Author();
                    a.setId(rs.getInt("id"));
                    a.setName(rs.getString("name"));
                    a.setNationality(rs.getString("nationality"));
                    Date bd = rs.getDate("birth_date");
                    a.setBirthDate(bd != null ? bd.toLocalDate() : null);
                    a.setBio(rs.getString("bio"));
                    a.setAvatarUrl(rs.getString("avatar_url"));
                    list.add(a);
                }
            }
        }
        return list;
    }

    @Override
    public List<Integer> getAuthorIdsByBookId(int bookId) throws Exception {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT author_id FROM book_authors WHERE book_id = ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("author_id"));
                }
            }
        }
        return list;
    }

    @Override
    public void setBookAuthors(int bookId, List<Integer> authorIds) throws Exception {
        try (Connection conn = DBContext.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete existing relationships
                String deleteSql = "DELETE FROM book_authors WHERE book_id = ?";
                try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                    deletePs.setInt(1, bookId);
                    deletePs.executeUpdate();
                }
                
                // Insert new ones
                if (authorIds != null && !authorIds.isEmpty()) {
                    String insertSql = "INSERT INTO book_authors (book_id, author_id, role) VALUES (?, ?, 'AUTHOR')";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        for (int aid : authorIds) {
                            insertPs.setInt(1, bookId);
                            insertPs.setInt(2, aid);
                            insertPs.addBatch();
                        }
                        insertPs.executeBatch();
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public boolean hasPhysicalCopies(int bookId) throws Exception {
        String sql = "SELECT COUNT(*) FROM book_copies WHERE book_id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public boolean hasActiveBorrowsOrReservations(int bookId) throws Exception {
        String borrowSql = "SELECT COUNT(*) FROM borrow_records WHERE book_id = ? AND status != 'RETURNED'";
        String reserveSql = "SELECT COUNT(*) FROM book_reservations WHERE book_id = ? AND status = 'PENDING'";
        
        try (Connection conn = DBContext.getInstance().getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(borrowSql)) {
                ps1.setInt(1, bookId);
                try (ResultSet rs1 = ps1.executeQuery()) {
                    if (rs1.next() && rs1.getInt(1) > 0) return true;
                }
            }
            try (PreparedStatement ps2 = conn.prepareStatement(reserveSql)) {
                ps2.setInt(1, bookId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next() && rs2.getInt(1) > 0) return true;
                }
            }
        }
        return false;
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setCategory(rs.getString("category"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setPublisher(rs.getString("publisher"));
        b.setPublishYear(rs.getInt("publish_year"));
        if (rs.wasNull()) b.setPublishYear(null);
        b.setPrice(rs.getInt("price"));
        if (rs.wasNull()) b.setPrice(null);
        b.setQuantity(rs.getInt("quantity"));
        b.setAvailable(rs.getInt("available"));
        b.setDescription(rs.getString("description"));
        b.setCoverImage(rs.getString("cover_image"));
        b.setSubject(rs.getString("subject"));
        return b;
    }
}
