package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import utils.UploadUtility;

/**
 * ImageServlet – Xử lý phục vụ các tệp tin hình ảnh tĩnh từ thư mục lưu trữ cục bộ.
 * Phân lớp: Controller.
 * Trách nhiệm chính: Đóng vai trò như một kho lưu trữ đám mây cục bộ (Local Cloud Storage),
 * tiếp nhận các yêu cầu tải ảnh qua đường dẫn /uploads/*, đọc tệp tương ứng từ thư mục cấu hình
 * và trả dữ liệu về trình duyệt.
 */
@WebServlet(name = "ImageServlet", urlPatterns = {"/uploads/*"})
public class ImageServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu GET để lấy và hiển thị hình ảnh từ thư mục lưu trữ.
     *
     * @param request  đối tượng HttpServletRequest chứa thông tin yêu cầu từ client
     * @param response đối tượng HttpServletResponse chứa thông tin phản hồi trả về client
     * @throws ServletException nếu xảy ra lỗi servlet
     * @throws IOException      nếu xảy ra lỗi vào ra dữ liệu
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lấy phần đường dẫn tương đối sau "/uploads"
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Giải mã tên tệp đề phòng trường hợp tên tệp chứa ký tự đặc biệt hoặc tiếng Việt
        String fileName = URLDecoder.decode(pathInfo, StandardCharsets.UTF_8.name());
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }

        // Ngăn chặn lỗi Path Traversal bảo vệ bảo mật hệ thống tệp tin
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Yêu cầu không hợp lệ.");
            return;
        }

        // Lấy thư mục lưu trữ thực tế từ cấu hình của UploadUtility
        String uploadDir = UploadUtility.getUploadDir(request.getServletContext());
        File file = new File(uploadDir, fileName);

        // Kiểm tra tệp tin có tồn tại và có thể đọc được hay không
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Xác định kiểu MIME (Content-Type) phù hợp cho ảnh
        String contentType = getServletContext().getMimeType(file.getName());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        response.setContentLengthLong(file.length());

        // Đọc dữ liệu ảnh từ tệp và ghi trực tiếp vào luồng xuất phản hồi
        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
