/*
 * DAO truy vấn đầu sách và tổng hợp số lượng khả dụng cho các luồng mượn, đặt trước.
 */
package dao;

import model.Book;
import model.Author;
import utils.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Triển khai lưu trữ đầu sách, gồm tìm kiếm và tính khả năng mượn/đặt trước từ dữ liệu bản sao.
 */
public class BookDAOImpl implements BookDAO {

    /** Truy vấn con tính slot logic còn lại, kể cả phiếu chờ nhận chưa được gán bản sao. */
    private static final String LOGICALLY_AVAILABLE_COPY_COUNT_SQL
            = "((SELECT COUNT(*) FROM book_copies bc "
            + "WHERE bc.book_id = b.id AND bc.is_deleted = 0 "
            + "AND bc.book_condition IN ('GOOD', 'WORN')) - "
            + "(SELECT COUNT(*) FROM borrow_records br "
            + "WHERE br.book_id = b.id AND "
            + "((br.status = 'PENDING_PICKUP' AND br.pickup_deadline >= NOW()) "
            + "OR (br.status IN ('BORROWED', 'OVERDUE') AND br.return_date IS NULL))))";

    /** Truy vấn con đếm các yêu cầu đang chờ nhưng chưa được gán bản sao. */
    private static final String WAITING_RESERVATION_COUNT_SQL
            = "(SELECT COUNT(*) FROM book_reservations waiting_reservation "
            + "WHERE waiting_reservation.book_id = b.id "
            + "AND waiting_reservation.status = 'WAITING')";

    /** Số bản có thể mượn ngay sau khi dành sách cho hàng đặt trước hiện tại. */
    private static final String AVAILABLE_COPY_COUNT_SQL = "GREATEST(0, "
            + LOGICALLY_AVAILABLE_COPY_COUNT_SQL + " - "
            + WAITING_RESERVATION_COUNT_SQL + ")";

    /** Cho biết còn slot đúng hạn hoặc bản rảnh đã được phân bổ cho hàng chờ để đặt lượt kế tiếp. */
    private static final String RESERVABLE_SQL = "(EXISTS (SELECT 1 FROM borrow_records future_br "
            + "WHERE future_br.book_id = b.id AND ((future_br.status = 'PENDING_PICKUP' "
            + "AND future_br.pickup_deadline >= NOW()) OR (future_br.status = 'BORROWED' "
            + "AND future_br.return_date IS NULL AND future_br.due_date >= CURDATE()))) OR ("
            + LOGICALLY_AVAILABLE_COPY_COUNT_SQL + " > 0 AND "
            + WAITING_RESERVATION_COUNT_SQL + " > 0))";

    @Override
    public Book findById(int id) throws Exception {
        String sql = "SELECT b.id, b.isbn, b.title, b.category, b.category_id, b.publisher, "
                + "b.publish_year, b.price, b.quantity, " + AVAILABLE_COPY_COUNT_SQL
                + " AS available, " + RESERVABLE_SQL
                + " AS reservable, b.description, b.cover_image, b.subject, b.is_deleted "
                + "FROM books b WHERE b.id = ? AND b.is_deleted = 0";
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
        sb.append("SELECT DISTINCT b.id, b.isbn, b.title, b.category, b.category_id, b.publisher, b.publish_year, b.price, b.quantity, ")
          .append(AVAILABLE_COPY_COUNT_SQL).append(" AS available, ")
          .append(RESERVABLE_SQL).append(" AS reservable, b.description, b.cover_image, b.subject ")
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
        else if ("available".equals(sort)) sortCol = "available";
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
        String sql = "SELECT name FROM categories WHERE is_deleted = 0 ORDER BY name ASC";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("name"));
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
                    String insertSql = "INSERT INTO book_authors (book_id, author_id, role) VALUES (?, ?, ?)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        for (int i = 0; i < authorIds.size(); i++) {
                            insertPs.setInt(1, bookId);
                            insertPs.setInt(2, authorIds.get(i));
                            // Đảm bảo giá trị role nằm trong ENUM('PRIMARY', 'CO_AUTHOR') của bảng book_authors
                            insertPs.setString(3, i == 0 ? "PRIMARY" : "CO_AUTHOR");
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

    @Override
    public List<Book> getTopBorrowedBooks(int limit) throws Exception {
        List<Book> list = new ArrayList<>();
        // Truy vấn danh sách sách được mượn nhiều nhất dựa trên bảng borrow_records
        String sql = "SELECT b.id, b.isbn, b.title, b.category, b.category_id, b.publisher, "
                   + "b.publish_year, b.price, b.quantity, " + AVAILABLE_COPY_COUNT_SQL
                   + " AS available, " + RESERVABLE_SQL
                   + " AS reservable, b.description, b.cover_image, b.subject, "
                   + "COUNT(br.id) AS borrow_count "
                   + "FROM books b "
                   + "LEFT JOIN borrow_records br ON b.id = br.book_id "
                   + "WHERE b.is_deleted = 0 "
                   + "GROUP BY b.id "
                   + "ORDER BY borrow_count DESC, b.id DESC "
                   + "LIMIT ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book b = mapRow(rs);
                    b.setBorrowCount(rs.getInt("borrow_count"));
                    list.add(b);
                }
            }
        }
        return list;
    }

    @Override
    public List<Book> getLatestBooks(int days, int limit) throws Exception {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT b.id, b.isbn, b.title, COALESCE(c.name, b.category) AS category, b.category_id, b.publisher, b.publish_year, b.price, b.quantity, "
                   + AVAILABLE_COPY_COUNT_SQL + " AS available, " + RESERVABLE_SQL
                   + " AS reservable, b.description, b.cover_image, b.subject "
                   + "FROM books b "
                   + "LEFT JOIN categories c ON b.category_id = c.id AND c.is_deleted = 0 "
                   + "WHERE b.is_deleted = 0 AND b.created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) "
                   + "ORDER BY b.created_at DESC, b.id DESC "
                   + "LIMIT ?";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        // Dự phòng nếu không có sách mới thêm trong N ngày gần đây thì lấy danh sách sách mới nhất theo ID
        if (list.isEmpty()) {
            return searchBooks(null, null, "id", "DESC", 1, limit);
        }
        return list;
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
        b.setReservable(rs.getBoolean("reservable"));
        b.setDescription(rs.getString("description"));
        b.setCoverImage(rs.getString("cover_image"));
        b.setSubject(rs.getString("subject"));
        return b;
    }
}
