/** DAO lưu metadata kệ và tổng hợp số bản sao từ book_copies. */
package dao;

import model.Book;
import model.BookCopy;
import model.Shelf;

import utils.DBContext;

import java.sql.*;
import java.util.*;

/** Sở hữu toàn bộ SQL của chức năng quản lý kệ. */
public class ShelfDao {
    private static final String COLUMNS =
            "s.id,s.code,s.name,s.area,s.floor_number,s.capacity,"
                    + "s.description,s.status,s.created_at,s.updated_at,COUNT(bc.id) book_count";

    /** Tìm kiếm, lọc và phân trang kệ. */
    public List<Shelf> findAll(String keyword, String area, String status, int offset, int limit)
            throws SQLException, ClassNotFoundException {
        StringBuilder sql =
                new StringBuilder("SELECT ")
                        .append(COLUMNS)
                        .append(
                                " FROM shelves s LEFT JOIN book_copies bc ON bc.shelf=s.code AND"
                                        + " bc.is_deleted=0 ")
                        .append("WHERE s.is_deleted=0 ");
        List<String> params = appendFilters(sql, keyword, area, status);
        sql.append("GROUP BY s.id ORDER BY s.floor_number,s.area,s.code LIMIT ?,?");
        try (Connection c = connection();
                PreparedStatement p = c.prepareStatement(sql.toString())) {
            int index = bind(p, params);
            p.setInt(index++, offset);
            p.setInt(index, limit);
            try (ResultSet r = p.executeQuery()) {
                List<Shelf> result = new ArrayList<>();
                while (r.next()) result.add(map(r));
                return result;
            }
        }
    }
    /** Đếm kệ theo cùng bộ lọc danh sách. */
    public int count(String keyword, String area, String status)
            throws SQLException, ClassNotFoundException {
        StringBuilder sql =
                new StringBuilder("SELECT COUNT(*) FROM shelves s WHERE s.is_deleted=0 ");
        List<String> params = appendFilters(sql, keyword, area, status);
        try (Connection c = connection();
                PreparedStatement p = c.prepareStatement(sql.toString())) {
            bind(p, params);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }
    /** Lấy tất cả khu vực hiện hành. */
    public List<String> findAreas() throws SQLException, ClassNotFoundException {
        String sql = "SELECT DISTINCT area FROM shelves WHERE is_deleted=0 ORDER BY area";
        try (Connection c = connection();
                PreparedStatement p = c.prepareStatement(sql);
                ResultSet r = p.executeQuery()) {
            List<String> values = new ArrayList<>();
            while (r.next()) values.add(r.getString(1));
            return values;
        }
    }
    /** Lấy toàn bộ kệ cho bản đồ. */
    public List<Shelf> findMap() throws SQLException, ClassNotFoundException {
        return findAll("", "", "", 0, Integer.MAX_VALUE);
    }
    /** Tìm metadata kệ cùng tổng số bản sao đang gắn vào mã kệ. */
    public Optional<Shelf> findById(int id) throws SQLException, ClassNotFoundException {
        String sql =
                "SELECT "
                        + COLUMNS
                        + " FROM shelves s LEFT JOIN book_copies bc ON bc.shelf=s.code AND"
                        + " bc.is_deleted=0 WHERE s.id=? AND s.is_deleted=0 GROUP BY s.id";
        try (Connection c = connection();
                PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return Optional.empty();
                return Optional.of(map(r));
            }
        }
    }
    /** Kiểm tra mã kệ duy nhất. */
    public boolean existsCode(String code, int excludedId)
            throws SQLException, ClassNotFoundException {
        try (Connection c = connection();
                PreparedStatement p =
                        c.prepareStatement(
                                "SELECT 1 FROM shelves WHERE LOWER(code)=LOWER(?) AND id<>? AND"
                                        + " is_deleted=0 LIMIT 1")) {
            p.setString(1, code);
            p.setInt(2, excludedId);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }
    /** Tạo kệ mới. */
    public Shelf insert(Shelf s, String actor) throws SQLException, ClassNotFoundException {
        String sql =
                "INSERT INTO"
                    + " shelves(code,name,area,floor_number,capacity,description,status,created_by,updated_by)"
                    + " VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = connection();
                PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindShelf(p, s, actor);
            p.setString(9, actor);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (r.next()) s.setId(r.getInt(1));
            }
        }
        return findById(s.getId())
                .orElseThrow(() -> new SQLException("Không đọc lại được kệ vừa tạo"));
    }
    /** Cập nhật kệ và đổi mã tham chiếu của BookCopy trong cùng transaction. */
    public boolean update(Shelf s, String oldCode, String actor)
            throws SQLException, ClassNotFoundException {
        try (Connection c = connection()) {
            boolean auto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p =
                        c.prepareStatement(
                                "UPDATE shelves SET"
                                        + " code=?,name=?,area=?,floor_number=?,capacity=?,"
                                        + "description=?,status=?,updated_by=?"
                                        + " WHERE id=? AND is_deleted=0")) {
                    bindShelf(p, s, actor);
                    p.setInt(9, s.getId());
                    if (p.executeUpdate() != 1) {
                        c.rollback();
                        return false;
                    }
                }
                if (!oldCode.equals(s.getCode()))
                    try (PreparedStatement p =
                            c.prepareStatement(
                                    "UPDATE book_copies SET shelf=?,updated_by=?,updated_at=NOW()"
                                            + " WHERE shelf=? AND is_deleted=0")) {
                        p.setString(1, s.getCode());
                        p.setString(2, actor);
                        p.setString(3, oldCode);
                        p.executeUpdate();
                    }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(auto);
            }
        }
    }
    /** Xóa mềm kệ trống. */
    public boolean delete(int id, String actor) throws SQLException, ClassNotFoundException {
        String sql =
                "UPDATE shelves s SET s.is_deleted=1,s.updated_by=? "
                        + "WHERE s.id=? AND s.is_deleted=0 AND NOT EXISTS ("
                        + "SELECT 1 FROM book_copies bc WHERE bc.shelf=s.code AND bc.is_deleted=0)";
        try (Connection c = connection();
                PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, actor);
            p.setInt(2, id);
            return p.executeUpdate() == 1;
        }
    }
    /** Kiểm tra kệ còn bản sao tham chiếu. */
    public boolean hasCopies(String code) throws SQLException, ClassNotFoundException {
        try (Connection c = connection();
                PreparedStatement p =
                        c.prepareStatement(
                                "SELECT 1 FROM book_copies WHERE shelf=? AND is_deleted=0 LIMIT"
                                        + " 1")) {
            p.setString(1, code);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    private List<String> appendFilters(
            StringBuilder sql, String keyword, String area, String status) {
        List<String> p = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (s.code LIKE ? OR s.name LIKE ?) ");
            p.add("%" + keyword + "%");
            p.add("%" + keyword + "%");
        }
        if (area != null && !area.isBlank()) {
            sql.append("AND s.area=? ");
            p.add(area);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND s.status=? ");
            p.add(status);
        }
        return p;
    }

    private int bind(PreparedStatement p, List<String> values) throws SQLException {
        int i = 1;
        for (String value : values) p.setString(i++, value);
        return i;
    }

    private void bindShelf(PreparedStatement p, Shelf s, String actor) throws SQLException {
        p.setString(1, s.getCode());
        p.setString(2, s.getName());
        p.setString(3, s.getArea());
        p.setInt(4, s.getFloorNumber());
        p.setInt(5, s.getCapacity());
        p.setString(6, s.getDescription());
        p.setString(7, s.getStatus());
        p.setString(8, actor);
    }

    private Shelf map(ResultSet r) throws SQLException {
        Shelf s =
                new Shelf(
                        r.getInt("id"),
                        r.getString("code"),
                        r.getString("name"),
                        r.getString("area"),
                        r.getInt("floor_number"),
                        r.getInt("capacity"),
                        r.getString("description"),
                        r.getString("status"));
        s.setBookCount(r.getInt("book_count"));
        Timestamp a = r.getTimestamp("created_at"), u = r.getTimestamp("updated_at");
        s.setCreatedAt(a == null ? null : a.toLocalDateTime());
        s.setUpdatedAt(u == null ? null : u.toLocalDateTime());
        return s;
    }

    /** Lấy một trang bản sao đang nằm trên kệ theo vị trí ngăn và barcode. */
    public List<BookCopy> findCopies(String code, int offset, int limit)
            throws SQLException, ClassNotFoundException {
        String sql =
                "SELECT bc.id,bc.barcode,'AVAILABLE' AS status,bc.slot,b.id book_id,b.title,b.isbn FROM"
                        + " book_copies bc JOIN books b ON b.id=bc.book_id WHERE bc.shelf=? AND"
                        + " bc.is_deleted=0 ORDER BY bc.slot,bc.barcode LIMIT ?,?";
        try (Connection c = connection();
                PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, code);
            p.setInt(2, offset);
            p.setInt(3, limit);
            try (ResultSet r = p.executeQuery()) {
                List<BookCopy> list = new ArrayList<>();
                while (r.next()) {
                    BookCopy bc = new BookCopy();
                    bc.setId(r.getInt("id"));
                    bc.setBarcode(r.getString("barcode"));
                    bc.setSlot(r.getString("slot"));
                    Book b = new Book();
                    b.setId(r.getInt("book_id"));
                    b.setTitle(r.getString("title"));
                    b.setIsbn(r.getString("isbn"));
                    bc.setBook(b);
                    list.add(bc);
                }
                return list;
            }
        }
    }

    private Connection connection() throws SQLException, ClassNotFoundException {
        return DBContext.getInstance().getConnection();
    }
}
