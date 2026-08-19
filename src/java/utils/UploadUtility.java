package utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

public class UploadUtility {

    private static final long MAX_IMAGE_SIZE = 5L * 1024L * 1024L;
    private static String cachedUploadDir;
    private static String cachedBaseUrl;

    static {
        loadConfig();
    }

    private static synchronized void loadConfig() {
        Properties props = new Properties();
        try (InputStream in = UploadUtility.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
                cachedUploadDir = props.getProperty("upload.dir");
                cachedBaseUrl = props.getProperty("upload.baseurl");
            }
        } catch (Exception e) {
            System.err.println("Could not load application.properties: " + e.getMessage());
        }

        // Apply defaults if empty
        if (cachedUploadDir == null || cachedUploadDir.trim().isEmpty()) {
            cachedUploadDir = System.getProperty("user.home") + File.separator + "swp_uploads";
        } else {
            cachedUploadDir = cachedUploadDir.trim();
        }
        
        if (cachedBaseUrl != null) {
            cachedBaseUrl = cachedBaseUrl.trim();
        } else {
            cachedBaseUrl = "";
        }
    }

    public static String getUploadDir(ServletContext ctx) {
        if (cachedUploadDir == null) {
            loadConfig();
        }
        // Ensure directory exists
        File dir = new File(cachedUploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return cachedUploadDir;
    }

    public static String getBaseUrl(String contextPath) {
        if (cachedBaseUrl == null) {
            loadConfig();
        }
        if (!cachedBaseUrl.isEmpty()) {
            return cachedBaseUrl;
        }
        return contextPath + "/uploads/";
    }

    /**
     * Saves an uploaded file to the configured upload directory.
     * @return the relative database path (e.g. "uploads/1721012345_avatar.png")
     */
    public static String saveFile(Part filePart, ServletContext ctx) throws IOException {
        if (filePart == null || filePart.getSize() == 0) {
            return null;
        }
        String fileName = getFileName(filePart);
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        // Validate content type to ensure it is an image
        String contentType = filePart.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Tệp tải lên không phải là định dạng ảnh hợp lệ.");
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
        String uploadDirPath = getUploadDir(ctx);
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Write the file
        filePart.write(uploadDirPath + File.separator + uniqueFileName);
        
        // Store in DB under relative path format "uploads/uniqueFileName"
        return "uploads/" + uniqueFileName;
    }

    /**
     * Lưu ảnh JPEG hoặc PNG bằng tên do server sinh để dùng cho đồ để quên.
     * Phương thức xác thực MIME, kích thước và chữ ký tệp trước khi ghi vào thư mục cấu hình.
     *
     * @param filePart tệp ảnh lấy từ multipart request, có thể rỗng
     * @param context ServletContext của ứng dụng hiện tại
     * @return đường dẫn tương đối để lưu DB, hoặc null khi người dùng không chọn ảnh
     * @throws IOException khi ảnh không hợp lệ hoặc không thể lưu tệp
     */
    public static String saveSecureImage(Part filePart, ServletContext context) throws IOException {
        if (filePart == null || filePart.getSize() == 0) {
            return null;
        }
        if (filePart.getSize() > MAX_IMAGE_SIZE) {
            throw new IOException("Ảnh không được vượt quá 5 MB.");
        }

        String contentType = filePart.getContentType();
        String extension = getImageExtension(contentType);
        if (extension == null || !hasValidImageSignature(filePart, contentType)) {
            throw new IOException("Chỉ chấp nhận ảnh JPEG hoặc PNG hợp lệ.");
        }

        Path uploadDirectory = Path.of(getUploadDir(context)).toAbsolutePath().normalize();
        Files.createDirectories(uploadDirectory);
        String generatedFileName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path targetPath = uploadDirectory.resolve(generatedFileName).normalize();
        if (!targetPath.getParent().equals(uploadDirectory)) {
            throw new IOException("Đường dẫn lưu ảnh không hợp lệ.");
        }

        filePart.write(targetPath.toString());
        return "uploads/" + generatedFileName;
    }

    /**
     * Xác định phần mở rộng an toàn từ MIME type được phép.
     *
     * @param contentType MIME type do container cung cấp
     * @return phần mở rộng do server quyết định, hoặc null nếu không được phép
     */
    private static String getImageExtension(String contentType) {
        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            return ".jpg";
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        return null;
    }

    /**
     * Kiểm tra chữ ký nhị phân để tránh chỉ tin MIME type do client khai báo.
     *
     * @param filePart tệp cần kiểm tra
     * @param contentType MIME type đã được allowlist
     * @return true khi chữ ký phù hợp JPEG hoặc PNG
     * @throws IOException khi không đọc được tệp tải lên
     */
    private static boolean hasValidImageSignature(Part filePart, String contentType) throws IOException {
        try (InputStream inputStream = filePart.getInputStream()) {
            byte[] header = inputStream.readNBytes(8);
            if ("image/jpeg".equalsIgnoreCase(contentType)) {
                return header.length >= 3
                        && (header[0] & 0xFF) == 0xFF
                        && (header[1] & 0xFF) == 0xD8
                        && (header[2] & 0xFF) == 0xFF;
            }
            return header.length == 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 0x50
                    && header[2] == 0x4E
                    && header[3] == 0x47
                    && header[4] == 0x0D
                    && header[5] == 0x0A
                    && header[6] == 0x1A
                    && header[7] == 0x0A;
        }
    }

    /**
     * Lưu tệp ảnh tải lên vào thư mục cấu hình với tên gốc của tệp (không thêm tiền tố thời gian).
     * Phù hợp khi muốn lưu trữ ảnh khớp chính xác với đường dẫn trong DB (ví dụ: "uploads/tai-chinh-doanh-nghiep.png").
     *
     * @param filePart đối tượng Part chứa tệp tải lên từ request
     * @param ctx ServletContext của ứng dụng
     * @return đường dẫn tương đối lưu trong cơ sở dữ liệu dạng "uploads/fileName"
     * @throws IOException nếu xảy ra lỗi ghi tệp hoặc tệp không phải định dạng ảnh hợp lệ
     */
    public static String saveFileWithOriginalName(Part filePart, ServletContext ctx) throws IOException {
        if (filePart == null || filePart.getSize() == 0) {
            return null;
        }
        String fileName = getFileName(filePart);
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        // Kiểm tra định dạng ảnh
        String contentType = filePart.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Tệp tải lên không phải là định dạng ảnh hợp lệ.");
        }

        String uploadDirPath = getUploadDir(ctx);
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Ghi tệp với tên gốc vào thư mục lưu trữ
        filePart.write(uploadDirPath + File.separator + fileName);
        
        // Trả về đường dẫn lưu trong DB dạng "uploads/fileName"
        return "uploads/" + fileName;
    }

    public static String resolveUrl(String dbPath, String contextPath) {
        if (dbPath == null || dbPath.trim().isEmpty()) {
            return "";
        }
        if (dbPath.startsWith("http://") || dbPath.startsWith("https://")) {
            return dbPath;
        }

        // If path starts with context path (e.g. legacy local uploads in war like /swpproject/uploads/...)
        if (contextPath != null && !contextPath.isEmpty() && dbPath.startsWith(contextPath)) {
            return dbPath;
        }

        String baseUrl = getBaseUrl(contextPath);
        
        // Extract filename
        String fileName = dbPath;
        if (fileName.startsWith("/uploads/")) {
            fileName = fileName.substring(9);
        } else if (fileName.startsWith("uploads/")) {
            fileName = fileName.substring(8);
        } else if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl + fileName;
    }

    private static String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                String name = token.substring(token.indexOf("=") + 2, token.length() - 1);
                // Clean IE/Windows path separators if present
                if (name.contains(File.separator)) {
                    name = name.substring(name.lastIndexOf(File.separator) + 1);
                } else if (name.contains("/")) {
                    name = name.substring(name.lastIndexOf("/") + 1);
                }
                return name;
            }
        }
        return "";
    }
}
