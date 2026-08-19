package service;

import dao.AuthorDAO;
import dao.AuthorDAOImpl;
import dao.BookDAO;
import dao.BookDAOImpl;
import dao.CategoryDao;
import model.Author;
import model.Book;
import model.Category;
import utils.AuditLogger;
import utils.DBContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/**
 * Service xử lý Import / Export Excel (CSV UTF-8 BOM) cho Quản lý Sách (Books).
 */
public class BookExcelService {

    private final BookDAO bookDAO;
    private final CategoryDao categoryDAO;
    private final AuthorDAO authorDAO;

    public BookExcelService() {
        this.bookDAO = new BookDAOImpl();
        this.categoryDAO = new CategoryDao();
        this.authorDAO = new AuthorDAOImpl();
    }

    public static class ImportResult {
        private int totalRows = 0;
        private int successCount = 0;
        private int failureCount = 0;
        private final List<String> errorList = new ArrayList<>();
        private final List<String> successList = new ArrayList<>();

        public int getTotalRows() { return totalRows; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public List<String> getErrorList() { return errorList; }
        public List<String> getSuccessList() { return successList; }
    }

    // =========================================================================
    // 1. EXPORT BOOKS TO CSV (UTF-8 BOM)
    // =========================================================================
    public void exportBooksToCsv(OutputStream out, List<Book> books) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        // Ghi UTF-8 BOM để Excel tự động nhận diện tiếng Việt
        writer.write('\uFEFF');

        // Header
        writer.write("Mã sách (ID),Mã ISBN,Tên sách,Tác giả,Danh mục,Nhà xuất bản,Năm XB,Giá tiền (VNĐ),Tổng bản sao,Còn sẵn,Môn học,Mô tả");
        writer.newLine();

        for (Book b : books) {
            List<Author> authors = bookDAO.getAuthorsByBookId(b.getId());
            StringBuilder authorNames = new StringBuilder();
            if (authors != null) {
                for (int i = 0; i < authors.size(); i++) {
                    if (i > 0) authorNames.append("; ");
                    authorNames.append(authors.get(i).getName());
                }
            }

            String[] row = new String[]{
                String.valueOf(b.getId()),
                b.getIsbn() != null ? b.getIsbn() : "",
                b.getTitle() != null ? b.getTitle() : "",
                authorNames.toString(),
                b.getCategory() != null ? b.getCategory() : "",
                b.getPublisher() != null ? b.getPublisher() : "",
                b.getPublishYear() != null ? String.valueOf(b.getPublishYear()) : "",
                b.getPrice() != null ? String.valueOf(b.getPrice()) : "0",
                String.valueOf(b.getQuantity()),
                String.valueOf(b.getAvailable()),
                b.getSubject() != null ? b.getSubject() : "",
                b.getDescription() != null ? b.getDescription().replace("\n", " ").replace("\r", "") : ""
            };

            writeCsvRow(writer, row);
        }

        writer.flush();
    }

    // =========================================================================
    // 2. GENERATE SAMPLE TEMPLATE (UTF-8 BOM)
    // =========================================================================
    public void generateTemplate(OutputStream out) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writer.write('\uFEFF');

        // Headers
        writer.write("Mã ISBN,Tên sách,Tác giả (cách nhau dấu ;),Danh mục,Nhà xuất bản,Năm XB,Giá tiền (VNĐ),Số lượng bản sao,Môn học,Mô tả");
        writer.newLine();

        // Sample rows
        String[][] sampleRows = new String[][]{
            {
                "978-604-2-00123-1",
                "Clean Code - Nghệ thuật viết mã sạch",
                "Robert C. Martin",
                "Công nghệ thông tin",
                "NXB Trẻ",
                "2022",
                "185000",
                "3",
                "SWE102, PRO192",
                "Cẩm nang kinh điển về phong cách viết code chuyên nghiệp và bảo trì hệ thống"
            },
            {
                "978-604-0-45678-2",
                "Lập trình hướng đối tượng Java toàn tập",
                "James Gosling; Herbert Schildt",
                "Giáo trình đại học",
                "NXB Đại học Quốc gia",
                "2023",
                "120000",
                "5",
                "PRO192, PRF192",
                "Tài liệu toàn diện từ căn bản đến nâng cao về lập trình Java và cấu trúc dữ liệu"
            },
            {
                "978-604-3-89101-3",
                "Kỹ nghệ phần mềm & Quản trị dự án Agile/Scrum",
                "Ian Sommerville; Ken Schwaber",
                "Khoa học máy tính",
                "NXB Thông tin và Truyền thông",
                "2024",
                "210000",
                "2",
                "SWE201, SWP391",
                "Phương pháp phát triển phần mềm hiện đại và quản lý quy trình Scrum"
            }
        };

        for (String[] row : sampleRows) {
            writeCsvRow(writer, row);
        }

        writer.flush();
    }

    // =========================================================================
    // =========================================================================
    // 3. IMPORT BOOKS FROM CSV (TỰ ĐỘNG THÊM MỚI HOẶC BỔ SUNG BẢN SAO KHI TRÙNG ISBN)
    // =========================================================================
    public ImportResult importBooksFromCsv(InputStream in, String operatorUsername) {
        ImportResult result = new ImportResult();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                // Bỏ qua dòng trống
                if (line.trim().isEmpty()) continue;

                // Xóa BOM ở dòng đầu nếu có
                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                // Dòng đầu tiên là tiêu đề (header)
                if (lineNumber == 1) {
                    continue;
                }

                result.totalRows++;
                List<String> columns = parseCsvLine(line);
                if (columns.isEmpty()) continue;

                String isbn = columns.size() > 0 ? columns.get(0).trim() : "";
                String title = columns.size() > 1 ? columns.get(1).trim() : "";
                String authorsStr = columns.size() > 2 ? columns.get(2).trim() : "";
                String categoryName = columns.size() > 3 ? columns.get(3).trim() : "";
                String publisher = columns.size() > 4 ? columns.get(4).trim() : "";
                String publishYearStr = columns.size() > 5 ? columns.get(5).trim() : "";
                String priceStr = columns.size() > 6 ? columns.get(6).trim() : "";
                String quantityStr = columns.size() > 7 ? columns.get(7).trim() : "1";
                String subject = columns.size() > 8 ? columns.get(8).trim() : "";
                String description = columns.size() > 9 ? columns.get(9).trim() : "";

                // Validate ISBN
                if (isbn.isEmpty()) {
                    result.failureCount++;
                    result.errorList.add("Dòng " + lineNumber + ": Thiếu mã ISBN.");
                    continue;
                }

                // Parse numeric fields
                Integer publishYear = null;
                if (!publishYearStr.isEmpty()) {
                    try {
                        publishYear = Integer.parseInt(publishYearStr.replaceAll("[^0-9]", ""));
                    } catch (Exception ignored) {}
                }

                Integer price = null;
                if (!priceStr.isEmpty()) {
                    try {
                        price = Integer.parseInt(priceStr.replaceAll("[^0-9]", ""));
                    } catch (Exception ignored) {}
                }

                int quantity = 1;
                if (!quantityStr.isEmpty()) {
                    try {
                        quantity = Integer.parseInt(quantityStr.replaceAll("[^0-9]", ""));
                        if (quantity <= 0) quantity = 1;
                    } catch (Exception ignored) {
                        quantity = 1;
                    }
                }

                // Import hoặc bổ sung bản sao
                try {
                    int[] res = importOrUpdateBook(isbn, title, authorsStr, categoryName, publisher, publishYear, price, quantity, subject, description, operatorUsername);
                    boolean isNew = res[1] == 1;
                    int totalQty = res[2];
                    result.successCount++;
                    if (isNew) {
                        result.successList.add("Dòng " + lineNumber + ": Thêm mới sách \"" + (title.isEmpty() ? isbn : title) + "\" (" + quantity + " bản sao).");
                    } else {
                        result.successList.add("Dòng " + lineNumber + ": Đã bổ sung +" + quantity + " bản sao cho sách \"" + (title.isEmpty() ? isbn : title) + "\" (ISBN: " + isbn + ") — Tổng bản sao hiện có: " + totalQty + ".");
                    }
                } catch (Exception ex) {
                    result.failureCount++;
                    result.errorList.add("Dòng " + lineNumber + " (ISBN: " + isbn + "): Lỗi lưu dữ liệu: " + ex.getMessage());
                }
            }

            // Ghi audit log nếu có sách được import thành công
            if (result.successCount > 0) {
                AuditLogger.log("IMPORT_BOOKS", operatorUsername, 0,
                        "Xử lý thành công " + result.successCount + "/" + result.totalRows + " dòng từ file Excel/CSV (Thêm mới & bổ sung bản sao)");
            }

        } catch (Exception e) {
            result.errorList.add("Lỗi đọc file: " + e.getMessage());
        }

        return result;
    }

    private int[] importOrUpdateBook(String isbn, String title, String authorsStr, String categoryName,
                                     String publisher, Integer publishYear, Integer price, int quantity,
                                     String subject, String description, String operator) throws Exception {

        try (Connection conn = DBContext.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Kiểm tra sách đã tồn tại theo ISBN chưa
                String findBookSql = "SELECT id, title, category, category_id, publisher, publish_year, price, quantity, available FROM books WHERE isbn = ? AND is_deleted = 0 LIMIT 1";
                int bookId = -1;
                String existingTitle = title;
                int currentQty = 0;
                int currentAvailable = 0;
                boolean isNew = true;

                try (PreparedStatement ps = conn.prepareStatement(findBookSql)) {
                    ps.setString(1, isbn);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            bookId = rs.getInt("id");
                            existingTitle = rs.getString("title");
                            currentQty = rs.getInt("quantity");
                            currentAvailable = rs.getInt("available");
                            isNew = false;
                        }
                    }
                }

                // 2. Tìm hoặc tạo danh mục
                int categoryId = 0;
                if (!categoryName.isEmpty()) {
                    categoryId = findOrCreateCategory(conn, categoryName, operator);
                }

                if (isNew) {
                    // Nếu là sách mới thì bắt buộc phải có tên sách
                    if (title.isEmpty()) {
                        throw new IllegalArgumentException("Sách mới cần phải có tên sách.");
                    }

                    String insertBookSql = "INSERT INTO books (isbn, title, category, category_id, publisher, publish_year, price, quantity, available, description, cover_image, subject, is_deleted, created_at, updated_at, created_by, updated_by) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?, 0, NOW(), NOW(), ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertBookSql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, isbn);
                        ps.setString(2, title);
                        ps.setString(3, categoryName);
                        if (categoryId > 0) ps.setInt(4, categoryId); else ps.setNull(4, Types.INTEGER);
                        ps.setString(5, publisher);
                        if (publishYear != null) ps.setInt(6, publishYear); else ps.setNull(6, Types.INTEGER);
                        if (price != null) ps.setInt(7, price); else ps.setNull(7, Types.INTEGER);
                        ps.setInt(8, quantity);
                        ps.setInt(9, quantity);
                        ps.setString(10, description);
                        ps.setString(11, subject);
                        ps.setString(12, operator != null ? operator : "admin");
                        ps.setString(13, operator != null ? operator : "admin");

                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                bookId = rs.getInt(1);
                            }
                        }
                    }

                    if (bookId <= 0) {
                        throw new SQLException("Không thể tạo bản ghi sách mới");
                    }
                    currentQty = quantity;
                } else {
                    // Nếu đã có sách: Cập nhật thêm bản sao và thông tin nếu có truyền vào
                    StringBuilder updateSql = new StringBuilder("UPDATE books SET quantity = quantity + ?, available = available + ?, updated_by = ?, updated_at = NOW()");
                    if (!title.isEmpty()) updateSql.append(", title = ?");
                    if (!categoryName.isEmpty()) updateSql.append(", category = ?, category_id = ?");
                    if (!publisher.isEmpty()) updateSql.append(", publisher = ?");
                    if (publishYear != null) updateSql.append(", publish_year = ?");
                    if (price != null) updateSql.append(", price = ?");
                    if (!subject.isEmpty()) updateSql.append(", subject = ?");
                    if (!description.isEmpty()) updateSql.append(", description = ?");
                    updateSql.append(" WHERE id = ?");

                    try (PreparedStatement ps = conn.prepareStatement(updateSql.toString())) {
                        int paramIdx = 1;
                        ps.setInt(paramIdx++, quantity);
                        ps.setInt(paramIdx++, quantity);
                        ps.setString(paramIdx++, operator != null ? operator : "admin");

                        if (!title.isEmpty()) ps.setString(paramIdx++, title);
                        if (!categoryName.isEmpty()) {
                            ps.setString(paramIdx++, categoryName);
                            if (categoryId > 0) ps.setInt(paramIdx++, categoryId); else ps.setNull(paramIdx++, Types.INTEGER);
                        }
                        if (!publisher.isEmpty()) ps.setString(paramIdx++, publisher);
                        if (publishYear != null) ps.setInt(paramIdx++, publishYear);
                        if (price != null) ps.setInt(paramIdx++, price);
                        if (!subject.isEmpty()) ps.setString(paramIdx++, subject);
                        if (!description.isEmpty()) ps.setString(paramIdx++, description);
                        ps.setInt(paramIdx++, bookId);

                        ps.executeUpdate();
                    }
                    currentQty = currentQty + quantity;
                }

                // 3. Liên kết tác giả
                if (!authorsStr.isEmpty()) {
                    String[] authorArray = authorsStr.split("[;,]");
                    for (String aName : authorArray) {
                        String nameTrim = aName.trim();
                        if (!nameTrim.isEmpty()) {
                            int authorId = findOrCreateAuthor(conn, nameTrim, operator);
                            if (authorId > 0) {
                                String linkSql = "INSERT IGNORE INTO book_authors (book_id, author_id) VALUES (?, ?)";
                                try (PreparedStatement psLink = conn.prepareStatement(linkSql)) {
                                    psLink.setInt(1, bookId);
                                    psLink.setInt(2, authorId);
                                    psLink.executeUpdate();
                                }
                            }
                        }
                    }
                }

                // 4. Xác định số bản sao hiện tại để đánh mã barcode tiếp theo
                int existingCopiesCount = 0;
                String countCopiesSql = "SELECT COUNT(*) FROM book_copies WHERE book_id = ?";
                try (PreparedStatement psCount = conn.prepareStatement(countCopiesSql)) {
                    psCount.setInt(1, bookId);
                    try (ResultSet rsCount = psCount.executeQuery()) {
                        if (rsCount.next()) {
                            existingCopiesCount = rsCount.getInt(1);
                        }
                    }
                }

                // 5. Sinh đúng `quantity` bản sao mới
                String insertCopySql = "INSERT INTO book_copies (book_id, barcode, book_condition, note, is_deleted, created_by, updated_by, created_at, updated_at) "
                        + "VALUES (?, ?, 'GOOD', ?, 0, ?, ?, NOW(), NOW())";
                String copyNote = isNew ? "Khởi tạo từ Import Excel" : "Bổ sung từ Import Excel";
                try (PreparedStatement psCopy = conn.prepareStatement(insertCopySql)) {
                    for (int i = 1; i <= quantity; i++) {
                        int copyIndex = existingCopiesCount + i;
                        String barcode = generateUniqueBarcode(conn, bookId, copyIndex);
                        psCopy.setInt(1, bookId);
                        psCopy.setString(2, barcode);
                        psCopy.setString(3, copyNote);
                        psCopy.setString(4, operator != null ? operator : "admin");
                        psCopy.setString(5, operator != null ? operator : "admin");
                        psCopy.addBatch();
                    }
                    psCopy.executeBatch();
                }

                conn.commit();
                return new int[]{bookId, isNew ? 1 : 0, currentQty};
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private int findOrCreateCategory(Connection conn, String name, String operator) throws SQLException {
        String findSql = "SELECT id FROM categories WHERE LOWER(name) = LOWER(?) AND is_deleted = 0 LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        String insertSql = "INSERT INTO categories (name, description, is_deleted, created_by, updated_by, created_at, updated_at) VALUES (?, 'Tự động tạo từ Import Sách', 0, ?, ?, NOW(), NOW())";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, operator != null ? operator : "admin");
            ps.setString(3, operator != null ? operator : "admin");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private int findOrCreateAuthor(Connection conn, String name, String operator) throws SQLException {
        String findSql = "SELECT id FROM authors WHERE LOWER(name) = LOWER(?) AND is_deleted = 0 LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        String insertSql = "INSERT INTO authors (name, bio, is_deleted, created_by, updated_by, created_at, updated_at) VALUES (?, 'Tự động tạo từ Import Sách', 0, ?, ?, NOW(), NOW())";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, operator != null ? operator : "admin");
            ps.setString(3, operator != null ? operator : "admin");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private String generateUniqueBarcode(Connection conn, int bookId, int copyIndex) throws SQLException {
        String baseBarcode = String.format("BC%04d_%02d", bookId, copyIndex);
        String testBarcode = baseBarcode;
        int attempt = 1;

        while (true) {
            String checkSql = "SELECT 1 FROM book_copies WHERE barcode = ? AND is_deleted = 0 LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, testBarcode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return testBarcode;
                    }
                }
            }
            testBarcode = baseBarcode + "_" + attempt;
            attempt++;
        }
    }

    // =========================================================================
    // CSV HELPER METHODS
    // =========================================================================
    private void writeCsvRow(BufferedWriter writer, String[] values) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(",");
            String val = values[i] != null ? values[i] : "";
            // Escape quote nếu có chứa dấu phẩy hoặc ngoặc kép hoặc xuống dòng
            if (val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r")) {
                val = "\"" + val.replace("\"", "\"\"") + "\"";
            }
            sb.append(val);
        }
        writer.write(sb.toString());
        writer.newLine();
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null) return result;

        StringBuilder curVal = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (inQuotes) {
                if (ch == '\"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                        curVal.append('\"');
                        i++; // Bỏ qua escape double quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    curVal.append(ch);
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    result.add(curVal.toString().trim());
                    curVal = new StringBuilder();
                } else {
                    curVal.append(ch);
                }
            }
        }
        result.add(curVal.toString().trim());

        return result;
    }
}
