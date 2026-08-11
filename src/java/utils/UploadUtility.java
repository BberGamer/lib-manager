package utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class UploadUtility {

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
