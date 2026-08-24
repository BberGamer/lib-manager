package dao;

import model.BookCopy;
import model.Book;
import utils.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookCopyDAO {

    /** Đếm lượt giữ hoặc mượn chưa kết thúc của bản sao hiện tại. */
    private static final String ACTIVE_BORROW_COUNT_SQL = "(SELECT COUNT(*) FROM borrow_records br "
            + "WHERE br.copy_id=bc.id AND ((br.status='PENDING_PICKUP' "
            + "AND br.pickup_deadline>=NOW()) OR (br.status IN ('BORROWED','OVERDUE') "
            + "AND br.return_date IS NULL)))";

    public BookCopy findById(int id) throws Exception {
        String sql = "SELECT bc.id, bc.book_id, bc.barcode, bc.book_condition, bc.note, bc.area, bc.shelf, bc.slot, " +
                     "b.title, b.isbn, b.category, b.publisher, b.publish_year, b.price, b.quantity, b.available, " +
                     ACTIVE_BORROW_COUNT_SQL + " AS borrowed_or_reserved " +
                     "FROM book_copies bc " +
                     "INNER JOIN books b ON bc.book_id = b.id " +
                     "WHERE bc.id = ? AND bc.is_deleted = 0";
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

    /**
     * Tìm kiếm một bản sao sách chưa bị xóa dựa trên mã vạch (barcode).
     *
     * @param barcode mã vạch bản sao cần tìm
     * @return đối tượng BookCopy tương ứng hoặc null nếu không tồn tại
     * @throws Exception khi truy vấn cơ sở dữ liệu thất bại
     */
    public BookCopy findByBarcode(String barcode) throws Exception {
        if (barcode == null || barcode.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT bc.id, bc.book_id, bc.barcode, bc.book_condition, bc.note, bc.area, bc.shelf, bc.slot, "
                     + "b.title, b.isbn, b.category, b.publisher, b.publish_year, b.price, b.quantity, b.available, "
                     + ACTIVE_BORROW_COUNT_SQL + " AS borrowed_or_reserved "
                     + "FROM book_copies bc "
                     + "INNER JOIN books b ON bc.book_id = b.id "
                     + "WHERE bc.barcode = ? AND bc.is_deleted = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<BookCopy> searchCopies(int bookId, String keyword, String area, int pageNum, int pageSize) throws Exception {
        List<BookCopy> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT bc.id, bc.book_id, bc.barcode, bc.book_condition, bc.note, bc.area, bc.shelf, bc.slot, ")
          .append("b.title, b.isbn, b.category, b.publisher, b.publish_year, b.price, b.quantity, b.available, ")
          .append(ACTIVE_BORROW_COUNT_SQL).append(" AS borrowed_or_reserved ")
          .append("FROM book_copies bc ")
          .append("INNER JOIN books b ON bc.book_id = b.id ")
          .append("WHERE bc.book_id = ? AND bc.is_deleted = 0 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND bc.barcode LIKE ? ");
        }
        if (area != null && !area.trim().isEmpty()) {
            sb.append("AND bc.area = ? ");
        }

        sb.append("ORDER BY bc.barcode ASC ");
        sb.append("LIMIT ?, ?");

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            ps.setInt(idx++, bookId);
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(idx++, "%" + keyword.trim() + "%");
            }
            if (area != null && !area.trim().isEmpty()) {
                ps.setString(idx++, area.trim());
            }
            int offset = (pageNum - 1) * pageSize;
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

    public int countCopies(int bookId, String keyword, String area) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) FROM book_copies bc WHERE bc.book_id = ? AND bc.is_deleted = 0 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND bc.barcode LIKE ? ");
        }
        if (area != null && !area.trim().isEmpty()) {
            sb.append("AND bc.area = ? ");
        }

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            ps.setInt(idx++, bookId);
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(idx++, "%" + keyword.trim() + "%");
            }
            if (area != null && !area.trim().isEmpty()) {
                ps.setString(idx++, area.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<String> getDistinctAreas() throws Exception {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT area FROM book_copies WHERE is_deleted = 0 AND area IS NOT NULL AND area != '' ORDER BY area ASC";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("area"));
            }
        }
        return list;
    }

    public boolean isBarcodeExists(String barcode, int excludeId) throws Exception {
        String sql = "SELECT COUNT(*) FROM book_copies WHERE barcode = ? AND id != ? AND is_deleted = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean addCopy(BookCopy copy) {
        String insertCopySql = "INSERT INTO book_copies (book_id, barcode, book_condition, note, area, shelf, slot, is_deleted, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?, NOW(), NOW())";
        String updateBookQtySql = "UPDATE books SET quantity = quantity + 1, available = available + 1 WHERE id = ?";
        
        try (Connection conn = DBContext.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert Copy
                int copyId = -1;
                try (PreparedStatement ps = conn.prepareStatement(insertCopySql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, copy.getBookId());
                    ps.setString(2, copy.getBarcode());
                    ps.setString(3, copy.getBookCondition());
                    ps.setString(4, copy.getNote());
                    ps.setString(5, copy.getArea());
                    ps.setString(6, copy.getShelf());
                    ps.setString(7, copy.getSlot());
                    ps.setString(8, copy.getCreatedBy());
                    ps.setString(9, copy.getCreatedBy());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            copyId = rs.getInt(1);
                            copy.setId(copyId);
                        }
                    }
                }
                
                // Update Book counts
                try (PreparedStatement ps = conn.prepareStatement(updateBookQtySql)) {
                    ps.setInt(1, copy.getBookId());
                    ps.executeUpdate();
                }
                
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCopy(BookCopy copy) {
        String sql = "UPDATE book_copies SET barcode = ?, book_condition = ?, note = ?, area = ?, shelf = ?, slot = ?, updated_by = ?, updated_at = NOW() WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, copy.getBarcode());
            ps.setString(2, copy.getBookCondition());
            ps.setString(3, copy.getNote());
            ps.setString(4, copy.getArea());
            ps.setString(5, copy.getShelf());
            ps.setString(6, copy.getSlot());
            ps.setString(7, copy.getUpdatedBy());
            ps.setInt(8, copy.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCopy(int id, String operator) {
        String selectCopySql = "SELECT book_id, book_condition FROM book_copies WHERE id = ? AND is_deleted = 0";
        String deleteCopySql = "UPDATE book_copies SET is_deleted = 1, updated_by = ?, updated_at = NOW() WHERE id = ?";
        String updateBookQtySql = "UPDATE books SET quantity = GREATEST(0, quantity - 1), available = GREATEST(0, available - ?) WHERE id = ?";
        
        try (Connection conn = DBContext.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bookId = -1;
                String condition = "GOOD";
                try (PreparedStatement ps = conn.prepareStatement(selectCopySql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            bookId = rs.getInt("book_id");
                            condition = rs.getString("book_condition");
                        }
                    }
                }
                
                if (bookId == -1) {
                    conn.rollback();
                    return false;
                }
                
                // Delete Copy
                try (PreparedStatement ps = conn.prepareStatement(deleteCopySql)) {
                    ps.setString(1, operator);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
                
                // Update Book counts
                boolean wasAvailable = ("GOOD".equals(condition) || "WORN".equals(condition))
                        && !isCopyBorrowedOrReserved(id);
                int availSub = wasAvailable ? 1 : 0;
                try (PreparedStatement ps = conn.prepareStatement(updateBookQtySql)) {
                    ps.setInt(1, availSub);
                    ps.setInt(2, bookId);
                    ps.executeUpdate();
                }
                
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addAuditLog(int copyId, String action, String changedBy, String oldCondition, String newCondition, String note) {
        String sql = "INSERT INTO book_copy_logs (copy_id, action, changed_by, old_status, new_status, old_condition, new_condition, note, created_at) VALUES (?, ?, ?, NULL, NULL, ?, ?, ?, NOW())";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, copyId);
            ps.setString(2, action);
            ps.setString(3, changedBy);
            ps.setString(4, oldCondition);
            ps.setString(5, newCondition);
            ps.setString(6, note);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<BookCopy> getAllCopies(String area, String keyword, int pageNum, int pageSize) throws Exception {
        List<BookCopy> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT bc.id, bc.book_id, bc.barcode, bc.book_condition, bc.note, bc.area, bc.shelf, bc.slot, ")
          .append("b.title, b.isbn, b.category, b.publisher, b.publish_year, b.price, b.quantity, b.available, ")
          .append(ACTIVE_BORROW_COUNT_SQL).append(" AS borrowed_or_reserved ")
          .append("FROM book_copies bc ")
          .append("INNER JOIN books b ON bc.book_id = b.id ")
          .append("WHERE bc.is_deleted = 0 ");

        if (area != null && !area.trim().isEmpty()) {
            sb.append("AND bc.area = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (bc.barcode LIKE ? OR b.title LIKE ?) ");
        }

        sb.append("ORDER BY bc.area ASC, bc.shelf ASC, bc.slot ASC, bc.barcode ASC ");
        sb.append("LIMIT ?, ?");

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (area != null && !area.trim().isEmpty()) {
                ps.setString(idx++, area.trim());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            int offset = (pageNum - 1) * pageSize;
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

    public int countAllCopies(String area, String keyword) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) ")
          .append("FROM book_copies bc ")
          .append("INNER JOIN books b ON bc.book_id = b.id ")
          .append("WHERE bc.is_deleted = 0 ");

        if (area != null && !area.trim().isEmpty()) {
            sb.append("AND bc.area = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (bc.barcode LIKE ? OR b.title LIKE ?) ");
        }

        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (area != null && !area.trim().isEmpty()) {
                ps.setString(idx++, area.trim());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Kiểm tra bản sao có bị một lượt mượn hoặc giữ sách đang hoạt động chiếm dụng hay không.
     *
     * @param copyId mã bản sao cần kiểm tra
     * @return {@code true} nếu bản sao đang bị chiếm dụng
     * @throws Exception khi truy vấn dữ liệu thất bại
     */
    public boolean isCopyBorrowedOrReserved(int copyId) throws Exception {
        String sql = "SELECT COUNT(*) FROM borrow_records br WHERE br.copy_id=? AND "
                + "((br.status='PENDING_PICKUP' AND br.pickup_deadline>=NOW()) OR "
                + "(br.status IN ('BORROWED','OVERDUE') AND br.return_date IS NULL))";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, copyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private BookCopy mapRow(ResultSet rs) throws SQLException {
        BookCopy bc = new BookCopy();
        bc.setId(rs.getInt("id"));
        bc.setBookId(rs.getInt("book_id"));
        bc.setBarcode(rs.getString("barcode"));
        bc.setBookCondition(rs.getString("book_condition"));
        bc.setNote(rs.getString("note"));
        bc.setArea(rs.getString("area"));
        bc.setShelf(rs.getString("shelf"));
        bc.setSlot(rs.getString("slot"));

        try {
            bc.setBorrowedOrReserved(rs.getInt("borrowed_or_reserved") > 0);
        } catch (SQLException ignored) {
        }

        Book b = new Book();
        b.setId(rs.getInt("book_id"));
        b.setTitle(rs.getString("title"));
        b.setIsbn(rs.getString("isbn"));
        b.setCategory(rs.getString("category"));
        b.setPublisher(rs.getString("publisher"));
        b.setPublishYear(rs.getInt("publish_year"));
        if (rs.wasNull()) b.setPublishYear(null);
        b.setPrice(rs.getInt("price"));
        if (rs.wasNull()) b.setPrice(null);
        b.setQuantity(rs.getInt("quantity"));
        b.setAvailable(rs.getInt("available"));
        bc.setBook(b);

        return bc;
    }
}
