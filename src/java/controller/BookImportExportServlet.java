package controller;

import dao.BookDAO;
import dao.BookDAOImpl;
import model.Book;
import model.User;
import service.BookExcelService;
import service.BookExcelService.ImportResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servlet xử lý Xuất file CSV, Tải file mẫu và Nhập dữ liệu sách từ file CSV/Excel.
 */
@WebServlet(name = "BookImportExportServlet", urlPatterns = {
    "/book/export", "/admin/book/export", "/librarian/book/export",
    "/book/template", "/admin/book/template", "/librarian/book/template",
    "/book/import", "/admin/book/import", "/librarian/book/import"
})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1 MB
    maxFileSize = 10 * 1024 * 1024,       // 10 MB
    maxRequestSize = 20 * 1024 * 1024     // 20 MB
)
public class BookImportExportServlet extends HttpServlet {

    private final BookExcelService bookExcelService = new BookExcelService();
    private final BookDAO bookDAO = new BookDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loggedUser = getAuthorizedStaff(request, response);
        if (loggedUser == null) return;

        String path = request.getServletPath();

        if (path.endsWith("/export")) {
            handleExport(request, response);
        } else if (path.endsWith("/template")) {
            handleTemplate(response);
        } else {
            response.sendRedirect(getBooksRedirectUrl(request));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loggedUser = getAuthorizedStaff(request, response);
        if (loggedUser == null) return;

        String path = request.getServletPath();
        if (path.endsWith("/import")) {
            handleImport(request, response, loggedUser);
        } else {
            response.sendRedirect(getBooksRedirectUrl(request));
        }
    }

    // =========================================================================
    // EXPORT
    // =========================================================================
    private void handleExport(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keyword = request.getParameter("keyword");
        String category = request.getParameter("category");
        String sort = request.getParameter("sort");
        String order = request.getParameter("order");

        try {
            // Lấy tối đa 10000 sách phù hợp bộ lọc hiện tại
            List<Book> books = bookDAO.searchBooks(keyword, category, sort, order, 1, 10000);

            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            String fileName = "Danh_sach_sach_FPT_Library_" + timestamp + ".csv";

            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

            bookExcelService.exportBooksToCsv(response.getOutputStream(), books);

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("dbError", "Lỗi khi xuất file danh sách sách: " + e.getMessage());
            response.sendRedirect(getBooksRedirectUrl(request));
        }
    }

    // =========================================================================
    // TEMPLATE DOWNLOAD
    // =========================================================================
    private void handleTemplate(HttpServletResponse response) throws IOException {
        try {
            String fileName = "Mau_nhap_sach_FPT_Library.csv";
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

            bookExcelService.generateTemplate(response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể tạo file mẫu: " + e.getMessage());
        }
    }

    // =========================================================================
    // IMPORT
    // =========================================================================
    private void handleImport(HttpServletRequest request, HttpServletResponse response, User loggedUser)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        try {
            Part filePart = request.getPart("excelFile");
            if (filePart == null || filePart.getSize() == 0) {
                session.setAttribute("dbError", "Vui lòng chọn một file CSV/Excel hợp lệ để tải lên.");
                response.sendRedirect(getBooksRedirectUrl(request));
                return;
            }

            String submittedFileName = filePart.getSubmittedFileName();
            if (submittedFileName == null || (!submittedFileName.toLowerCase().endsWith(".csv") && !submittedFileName.toLowerCase().endsWith(".txt"))) {
                session.setAttribute("dbError", "Hệ thống hỗ trợ file định dạng .csv chuẩn UTF-8. Vui lòng kiểm tra lại định dạng file.");
                response.sendRedirect(getBooksRedirectUrl(request));
                return;
            }

            ImportResult result = bookExcelService.importBooksFromCsv(filePart.getInputStream(), loggedUser.getUsername());
            session.setAttribute("importResult", result);

            if (result.getSuccessCount() > 0) {
                session.setAttribute("successMsg", "Đã nhập thành công " + result.getSuccessCount() + "/" + result.getTotalRows() + " đầu sách vào hệ thống!");
            } else if (result.getFailureCount() > 0) {
                session.setAttribute("dbError", "Quá trình nhập file có " + result.getFailureCount() + " dòng lỗi. Vui lòng xem bảng báo cáo chi tiết.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("dbError", "Lỗi xử lý file tải lên: " + e.getMessage());
        }

        response.sendRedirect(getBooksRedirectUrl(request));
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    private User getAuthorizedStaff(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User loggedUser = (session != null) ? (User) session.getAttribute("loggedUser") : null;
        if (loggedUser == null || (!loggedUser.isAdmin() && !loggedUser.isLibrarian())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Quản trị viên và Thủ thư mới có quyền thực hiện chức năng này.");
            return null;
        }
        return loggedUser;
    }

    private String getBooksRedirectUrl(HttpServletRequest request) {
        String path = request.getServletPath();
        String contextPath = request.getContextPath();
        if (path.startsWith("/admin")) {
            return contextPath + "/admin/books";
        } else if (path.startsWith("/librarian")) {
            return contextPath + "/librarian/books";
        }
        return contextPath + "/books";
    }
}
