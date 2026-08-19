package controller;

import dao.*;
import model.Author;
import model.Book;
import model.User;
import model.BookReview;
import utils.UploadUtility;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * BookDetailServlet – xử lý CRUD sách.
 *
 * URL patterns:
 *   /book/detail?id=X          – Xem chi tiết (Tất cả người dùng)
 *   /admin/book/add, /book/add – Form thêm sách (Admin only)
 *   /librarian/book/edit, /admin/book/edit – Form sửa sách (Admin & Librarian)
 *   /admin/book/delete, /book/delete – Xóa sách (Admin only)
 */
@WebServlet(name = "BookDetailServlet", urlPatterns = {
    "/book/detail", "/librarian/book/detail", "/admin/book/detail",
    "/book/add", "/admin/book/add",
    "/book/edit", "/librarian/book/edit", "/admin/book/edit",
    "/book/delete", "/admin/book/delete"
})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class BookDetailServlet extends HttpServlet {

    private String getRedirectBase(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean isManageView = path.startsWith("/admin") || path.startsWith("/librarian");
        if (isManageView) {
            return request.getContextPath() + (path.startsWith("/admin") ? "/admin" : "/librarian");
        }
        return request.getContextPath();
    }

    private void setSidebarAttributes(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean isManageView = path.startsWith("/admin") || path.startsWith("/librarian");
        if (isManageView) {
            request.setAttribute("isManagePageAttr", true);
            request.setAttribute("activePage", "books");
            request.setAttribute("rolePath", path.startsWith("/admin") ? "/admin" : "/librarian");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String path = request.getServletPath();
        String ctx = request.getContextPath();

        switch (path) {
            case "/book/detail":
            case "/librarian/book/detail":
            case "/admin/book/detail":
                showDetail(request, response);
                break;
            case "/book/add":
            case "/admin/book/add":
                showAddForm(request, response);
                break;
            case "/book/edit":
            case "/librarian/book/edit":
            case "/admin/book/edit":
                showEditForm(request, response);
                break;
            case "/book/delete":
            case "/admin/book/delete":
                processDelete(request, response);
                break;
            default:
                response.sendRedirect(ctx + "/books");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");
        if ("create".equals(action)) {
            processCreate(request, response);
        } else if ("update".equals(action)) {
            processUpdate(request, response);
        } else {
            response.sendRedirect(getRedirectBase(request) + "/books");
        }
    }

    // ============================================================
    //  VIEW DETAIL
    // ============================================================
    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("currentPage", "books");

        // Detect management view context (admin or librarian) → show sidebar
        String servletPath = request.getServletPath();
        boolean isManageView = servletPath.startsWith("/admin") || servletPath.startsWith("/librarian");
        if (isManageView) {
            request.setAttribute("isManagePageAttr", true);
            request.setAttribute("activePage", "books");
            if (servletPath.startsWith("/admin")) {
                request.setAttribute("rolePath", "/admin");
            } else {
                request.setAttribute("rolePath", "/librarian");
            }
        }

        int id = parseId(request.getParameter("id"));
        if (id <= 0) {
            String rolePath = (String) request.getAttribute("rolePath");
            response.sendRedirect(request.getContextPath() + (rolePath != null ? rolePath + "/books" : "/books"));
            return;
        }

        try {
            BookDAO dao = new BookDAOImpl();
            Book book = dao.findById(id);
            if (book == null) {
                request.setAttribute("errorMsg", "Không tìm thấy sách với ID: " + id);
                request.setAttribute("pageTitle", "Sách không tồn tại – FPT Library");
                request.getRequestDispatcher("/WEB-INF/views/book_detail.jsp").forward(request, response);
                return;
            }

            List<Author> authors = dao.getAuthorsByBookId(id);

            // Fetch review data
            BookReviewDAO reviewDAO = new BookReviewDAO();
            List<BookReview> reviews = reviewDAO.getReviewsByBookId(id);
            double avgRating = reviewDAO.getAverageRating(id);
            BookReview myReview = null;
            boolean canReview = false;

            HttpSession session = request.getSession(false);
            User loggedUser = (session != null) ? (User) session.getAttribute("loggedUser") : null;
            
            if (loggedUser != null && "READER".equals(loggedUser.getRole())) {
                Integer borrowIdParam = null;
                String borrowIdStr = request.getParameter("borrowId");
                if (borrowIdStr != null && !borrowIdStr.trim().isEmpty()) {
                    try {
                        borrowIdParam = Integer.parseInt(borrowIdStr);
                    } catch (NumberFormatException e) {
                        // Bỏ qua
                    }
                }

                Integer targetBorrowId = null;
                if (borrowIdParam != null) {
                    boolean isValid = reviewDAO.isBorrowEligibleForReview(borrowIdParam, id, loggedUser.getId());
                    if (isValid) {
                        targetBorrowId = borrowIdParam;
                    }
                }

                if (targetBorrowId == null) {
                    targetBorrowId = reviewDAO.getUnreviewedBorrowId(id, loggedUser.getId());
                }

                if (targetBorrowId != null) {
                    canReview = true;
                    request.setAttribute("unreviewedBorrowId", targetBorrowId);
                }
            }

            request.setAttribute("reviews", reviews);
            request.setAttribute("avgRating", String.format("%.1f", avgRating).replace(",", "."));
            request.setAttribute("myReview", myReview);
            request.setAttribute("canReview", canReview);

            request.setAttribute("book", book);
            request.setAttribute("authors", authors);
            request.setAttribute("pageTitle", book.getTitle() + " – FPT Library");
            request.setAttribute("pageDesc", "Chi tiết sách: " + book.getTitle());

            // Check if user is admin (for showing delete buttons)
            request.setAttribute("isAdmin", loggedUser != null && loggedUser.isAdmin());

        } catch (Exception e) {
            request.setAttribute("errorMsg", "Lỗi tải dữ liệu: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/book_detail.jsp").forward(request, response);
    }

    // ============================================================
    //  SHOW ADD FORM (ADMIN ONLY)
    // ============================================================
    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireAdmin(request, response)) return;

        setSidebarAttributes(request);
        request.setAttribute("currentPage", "books");
        request.setAttribute("pageTitle", "Thêm sách mới – FPT Library");
        request.setAttribute("formMode", "add");

        try {
            loadFormData(request);
        } catch (Exception e) {
            request.setAttribute("errorMsg", "Lỗi tải dữ liệu: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/book_form.jsp").forward(request, response);
    }

    // ============================================================
    //  SHOW EDIT FORM
    // ============================================================
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireAdminOrLibrarian(request, response)) return;

        setSidebarAttributes(request);
        request.setAttribute("currentPage", "books");
        request.setAttribute("formMode", "edit");

        String redirectBase = getRedirectBase(request);
        int id = parseId(request.getParameter("id"));
        if (id <= 0) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        try {
            BookDAO bookDao = new BookDAOImpl();
            Book book = bookDao.findById(id);
            if (book == null) {
                response.sendRedirect(redirectBase + "/books");
                return;
            }

            boolean hasCopies = bookDao.hasPhysicalCopies(id);
            List<Integer> selectedAuthorIds = bookDao.getAuthorIdsByBookId(id);

            request.setAttribute("book", book);
            request.setAttribute("hasCopies", hasCopies);
            request.setAttribute("selectedAuthorIds", selectedAuthorIds);
            request.setAttribute("pageTitle", "Sửa sách: " + book.getTitle() + " – FPT Library");

            loadFormData(request);

        } catch (Exception e) {
            request.setAttribute("errorMsg", "Lỗi tải dữ liệu: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/book_form.jsp").forward(request, response);
    }

    // ============================================================
    //  PROCESS CREATE (ADMIN ONLY)
    // ============================================================
    private void processCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireAdmin(request, response)) return;

        String redirectBase = getRedirectBase(request);

        try {
            BookDAO dao = new BookDAOImpl();
            Book book = extractBookFromRequest(request);
            HttpSession session = request.getSession(false);
            User loggedUser = (session != null) ? (User) session.getAttribute("loggedUser") : null;
            if (loggedUser != null) {
                book.setCreatedBy(loggedUser.getUsername());
            }

            // Validate
            List<String> errors = validateBook(book, dao, true, 0);
            List<Integer> authorIds = getSelectedAuthorIds(request);
            if (authorIds.isEmpty()) {
                errors.add("Vui lòng chọn ít nhất một tác giả.");
            }
            if (!errors.isEmpty()) {
                setSidebarAttributes(request);
                request.setAttribute("formMode", "add");
                request.setAttribute("book", book);
                request.setAttribute("errors", errors);
                request.setAttribute("currentPage", "books");
                request.setAttribute("pageTitle", "Thêm sách mới – FPT Library");
                request.setAttribute("selectedAuthorIds", authorIds);
                loadFormData(request);
                request.getRequestDispatcher("/WEB-INF/views/book_form.jsp").forward(request, response);
                return;
            }

            int newId = dao.createBook(book);
            if (newId > 0) {
                // Set authors
                if (!authorIds.isEmpty()) {
                    dao.setBookAuthors(newId, authorIds);
                }
                response.sendRedirect(redirectBase + "/book/detail?id=" + newId + "&success=created");
            } else {
                setSidebarAttributes(request);
                request.setAttribute("formMode", "add");
                request.setAttribute("book", book);
                request.setAttribute("errorMsg", "Thêm sách thất bại.");
                request.setAttribute("currentPage", "books");
                request.setAttribute("pageTitle", "Thêm sách mới – FPT Library");
                loadFormData(request);
                request.getRequestDispatcher("/WEB-INF/views/book_form.jsp").forward(request, response);
            }

        } catch (Exception e) {
            setSidebarAttributes(request);
            request.setAttribute("errorMsg", "Lỗi: " + e.getMessage());
            request.setAttribute("formMode", "add");
            request.setAttribute("currentPage", "books");
            request.setAttribute("pageTitle", "Thêm sách mới – FPT Library");
            try { loadFormData(request); } catch (Exception ex) {}
            request.getRequestDispatcher("/WEB-INF/views/book_form.jsp").forward(request, response);
        }
    }

    // ============================================================
    //  PROCESS UPDATE
    // ============================================================
    private void processUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireAdminOrLibrarian(request, response)) return;

        String redirectBase = getRedirectBase(request);
        int id = parseId(request.getParameter("id"));
        if (id <= 0) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        try {
            BookDAO dao = new BookDAOImpl();
            Book book = extractBookFromRequest(request);
            book.setId(id);
            HttpSession session = request.getSession(false);
            User loggedUser = (session != null) ? (User) session.getAttribute("loggedUser") : null;
            if (loggedUser != null) {
                book.setUpdatedBy(loggedUser.getUsername());
            }

            // If has physical copies, keep original ISBN, quantity and available counts
            boolean hasCopies = dao.hasPhysicalCopies(id);
            if (hasCopies) {
                Book original = dao.findById(id);
                if (original != null) {
                    book.setIsbn(original.getIsbn());
                    book.setQuantity(original.getQuantity());
                    book.setAvailable(original.getAvailable());
                }
            }

            // Validate
            List<String> errors = validateBook(book, dao, false, id);
            List<Integer> authorIds = getSelectedAuthorIds(request);
            if (authorIds.isEmpty()) {
                errors.add("Vui lòng chọn ít nhất một tác giả.");
            }
            if (!errors.isEmpty()) {
                setSidebarAttributes(request);
                request.setAttribute("formMode", "edit");
                request.setAttribute("book", book);
                request.setAttribute("hasCopies", hasCopies);
                request.setAttribute("errors", errors);
                request.setAttribute("currentPage", "books");
                request.setAttribute("pageTitle", "Sửa sách: " + book.getTitle() + " – FPT Library");
                request.setAttribute("selectedAuthorIds", authorIds);
                loadFormData(request);
                request.getRequestDispatcher("/WEB-INF/views/book_form.jsp").forward(request, response);
                return;
            }

            boolean updated = dao.updateBook(book);
            if (updated) {
                // Update authors
                dao.setBookAuthors(id, authorIds);
                response.sendRedirect(redirectBase + "/book/detail?id=" + id + "&success=updated");
            } else {
                setSidebarAttributes(request);
                request.setAttribute("formMode", "edit");
                request.setAttribute("book", book);
                request.setAttribute("hasCopies", hasCopies);
                request.setAttribute("errorMsg", "Cập nhật sách thất bại.");
                request.setAttribute("currentPage", "books");
                request.setAttribute("pageTitle", "Sửa sách: " + book.getTitle() + " – FPT Library");
                request.setAttribute("selectedAuthorIds", authorIds);
                loadFormData(request);
                request.getRequestDispatcher("/WEB-INF/views/book_form.jsp").forward(request, response);
            }

        } catch (Exception e) {
            setSidebarAttributes(request);
            request.setAttribute("errorMsg", "Lỗi: " + e.getMessage());
            request.setAttribute("formMode", "edit");
            request.setAttribute("currentPage", "books");
            request.setAttribute("pageTitle", "Sửa sách – FPT Library");
            try { loadFormData(request); } catch (Exception ex) {}
            request.getRequestDispatcher("/WEB-INF/views/book_form.jsp").forward(request, response);
        }
    }

    // ============================================================
    //  DELETE (ADMIN ONLY)
    // ============================================================
    private void processDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireAdmin(request, response)) return;

        String redirectBase = getRedirectBase(request);
        int id = parseId(request.getParameter("id"));
        if (id <= 0) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        // Lấy username từ session để ghi vào updated_by
        User currentUser = (User) request.getSession().getAttribute("loggedUser");
        String operator = (currentUser != null) ? currentUser.getUsername() : "system";

        try {
            BookDAO dao = new BookDAOImpl();

            // Kiểm tra có lượt mượn/đặt chỗ đang hoạt động không
            if (dao.hasActiveBorrowsOrReservations(id)) {
                response.sendRedirect(redirectBase + "/book/detail?id=" + id + "&error=has_active");
                return;
            }

            // Xóa liên kết tác giả trước
            dao.setBookAuthors(id, new ArrayList<>());

            // deleteBook đã tích hợp guard: nếu còn active copies sẽ throw IllegalStateException
            boolean deleted = dao.deleteBook(id, operator);

            if (deleted) {
                response.sendRedirect(redirectBase + "/books?success=deleted");
            } else {
                response.sendRedirect(redirectBase + "/book/detail?id=" + id + "&error=delete_failed");
            }

        } catch (IllegalStateException e) {
            // Còn bản sao vật lý active
            response.sendRedirect(redirectBase + "/book/detail?id=" + id + "&error=has_copies");
        } catch (Exception e) {
            response.sendRedirect(redirectBase + "/book/detail?id=" + id + "&error=exception");
        }
    }

    // ============================================================
    //  HELPER METHODS
    // ============================================================

    /**
     * Kiểm tra quyền Admin. Redirect nếu không đủ quyền.
     * @return true nếu user là Admin
     */
    private boolean requireAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        User user = (User) session.getAttribute("loggedUser");
        if (!user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Quản trị viên (Admin) mới có quyền xóa sách.");
            return false;
        }
        return true;
    }

    /**
     * Kiểm tra quyền Admin hoặc Librarian. Redirect nếu không đủ quyền.
     * @return true nếu user là Admin hoặc Librarian
     */
    private boolean requireAdminOrLibrarian(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        User user = (User) session.getAttribute("loggedUser");
        if (!user.isAdminOrLibrarian()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
            return false;
        }
        return true;
    }

    /**
     * Load dữ liệu cho form (categories, authors).
     */
    private void loadFormData(HttpServletRequest request) throws Exception {
        CategoryDao catDao = new CategoryDao();
        AuthorDAO authorDao = new AuthorDAOImpl();
        request.setAttribute("categoriesList", catDao.findAll());
        request.setAttribute("authorsList", authorDao.findAll());
    }

    /**
     * Parse book data từ request parameters.
     */
    private Book extractBookFromRequest(HttpServletRequest request) throws ServletException, IOException {
        Book book = new Book();
        book.setTitle(trim(request.getParameter("title")));
        book.setIsbn(trim(request.getParameter("isbn")));
        book.setCategory(trim(request.getParameter("category")));

        String catIdStr = request.getParameter("categoryId");
        if (catIdStr != null && !catIdStr.trim().isEmpty()) {
            try { book.setCategoryId(Integer.parseInt(catIdStr.trim())); } catch (NumberFormatException e) {}
        }

        book.setPublisher(trim(request.getParameter("publisher")));

        String yearStr = request.getParameter("publishYear");
        if (yearStr != null && !yearStr.trim().isEmpty()) {
            try { book.setPublishYear(Integer.valueOf(yearStr.trim())); } catch (NumberFormatException e) {}
        }

        String priceStr = request.getParameter("price");
        if (priceStr != null && !priceStr.trim().isEmpty()) {
            try { book.setPrice(Integer.valueOf(priceStr.trim())); } catch (NumberFormatException e) {}
        }

        String qtyStr = request.getParameter("quantity");
        if (qtyStr != null && !qtyStr.trim().isEmpty()) {
            try { book.setQuantity(Integer.parseInt(qtyStr.trim())); } catch (NumberFormatException e) {}
        }

        // available = quantity for new books
        String availStr = request.getParameter("available");
        if (availStr != null && !availStr.trim().isEmpty()) {
            try { book.setAvailable(Integer.parseInt(availStr.trim())); } catch (NumberFormatException e) {}
        } else {
            book.setAvailable(book.getQuantity());
        }

        book.setDescription(trim(request.getParameter("description")));
        
        // Handle cover image upload using UploadUtility
        String coverImage = trim(request.getParameter("existingCoverImage"));
        try {
            Part filePart = request.getPart("coverImageFile");
            if (filePart != null && filePart.getSize() > 0) {
                String savedPath = UploadUtility.saveFile(filePart, request.getServletContext());
                if (savedPath != null) {
                    coverImage = savedPath;
                }
            }
        } catch (ServletException | IOException e) {
            // Log or ignore
        }
        book.setCoverImage(coverImage);
        
        book.setSubject(trim(request.getParameter("subject")));

        return book;
    }

    /**
     * Lấy danh sách author IDs được chọn.
     */
    private List<Integer> getSelectedAuthorIds(HttpServletRequest request) {
        List<Integer> ids = new ArrayList<>();
        String[] authorIdStrs = request.getParameterValues("authorIds");
        if (authorIdStrs != null) {
            for (String s : authorIdStrs) {
                try { ids.add(Integer.valueOf(s.trim())); } catch (NumberFormatException e) {}
            }
        }
        return ids;
    }

    /**
     * Validate thông tin sách.
     */
    private List<String> validateBook(Book book, BookDAO dao, boolean isCreate, int excludeId) throws Exception {
        List<String> errors = new ArrayList<>();

        // Required fields
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            errors.add("Tiêu đề sách không được để trống.");
        } else if (book.getTitle().length() > 255) {
            errors.add("Tiêu đề sách không được vượt quá 255 ký tự.");
        }

        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            errors.add("ISBN không được để trống.");
        } else if (book.getIsbn().length() > 20) {
            errors.add("ISBN không được vượt quá 20 ký tự.");
        } else {
            // ISBN uniqueness
            if (isCreate) {
                if (dao.isIsbnExists(book.getIsbn())) {
                    errors.add("ISBN '" + book.getIsbn() + "' đã tồn tại trong hệ thống.");
                }
            } else {
                if (dao.isIsbnExistsExcluding(book.getIsbn(), excludeId)) {
                    errors.add("ISBN '" + book.getIsbn() + "' đã được sử dụng bởi sách khác.");
                }
            }
        }

        if (book.getCategory() == null || book.getCategory().trim().isEmpty()) {
            errors.add("Danh mục không được để trống.");
        }

        // Publish year
        if (book.getPublishYear() != null) {
            int currentYear = java.time.Year.now().getValue();
            if (book.getPublishYear() < 1995 || book.getPublishYear() > currentYear) {
                errors.add("Năm xuất bản phải từ 1995 đến " + currentYear + ".");
            }
        }

        // Price
        if (book.getPrice() != null && book.getPrice() < 0) {
            errors.add("Giá sách không được âm.");
        }

        // Quantity
        if (book.getQuantity() < 0) {
            errors.add("Số lượng không được âm.");
        }

        // Available <= Quantity
        if (book.getAvailable() < 0) {
            errors.add("Số lượng có sẵn không được âm.");
        }
        if (book.getAvailable() > book.getQuantity()) {
            errors.add("Số lượng có sẵn không được lớn hơn tổng số lượng.");
        }

        // Description length
        if (book.getDescription() != null && book.getDescription().length() > 5000) {
            errors.add("Mô tả không được vượt quá 5000 ký tự.");
        }

        return errors;
    }

    private int parseId(String s) {
        if (s == null || s.trim().isEmpty()) return -1;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return -1; }
    }

    private String trim(String s) {
        return (s != null) ? s.trim() : null;
    }
}
