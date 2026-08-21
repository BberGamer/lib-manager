/**
 * Servlet điều phối toàn bộ HTTP workflow quản trị thể loại và bảo vệ quyền Admin.
 */
package controller;

import exception.CategoryException;
import exception.CategoryValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Category;
import model.User;
import service.CategoryService;
import utils.AuditLogger;
import utils.RoleGuard;

/**
 * Nhận request dưới /admin/categories, gọi CategoryService và forward sang JSP bảo vệ.
 * Servlet tái sử dụng RoleGuard và session loggedUser do LoginServlet thiết lập.
 */
@WebServlet(urlPatterns = {
    "/admin/categories",
    "/admin/categories/new",
    "/admin/categories/edit",
    "/admin/categories/view",
    "/admin/categories/create",
    "/admin/categories/update",
    "/admin/categories/delete"
})
public class CategoryServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CategoryServlet.class.getName());
    private static final String LIST_PATH = "/admin/categories";
    private static final String LIST_VIEW = "/WEB-INF/views/admin/category-list.jsp";
    private static final String FORM_VIEW = "/WEB-INF/views/admin/category-form.jsp";
    private static final String DETAIL_VIEW = "/WEB-INF/views/admin/category-view.jsp";
    private static final String FLASH_SUCCESS = "flashSuccess";
    private static final String FLASH_ERROR = "flashError";

    private final CategoryService categoryService = new CategoryService();

    /**
     * Điều phối các thao tác đọc, hiển thị form và chi tiết sau khi kiểm tra Admin.
     * @param request request HTTP hiện tại
     * @param response response HTTP hiện tại
     * @throws ServletException khi forward view thất bại
     * @throws IOException khi ghi response thất bại
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareResponse(request, response);
        if (!authorize(request, response)) {
            return;
        }
        try {
            String path = request.getServletPath();
            switch (path) {
                case "/admin/categories/new" -> showCreateForm(request, response);
                case "/admin/categories/edit" -> showEditForm(request, response);
                case "/admin/categories/view" -> showDetail(request, response);
                default -> showList(request, response);
            }
        } catch (CategoryException exception) {
            handleServerError(response, exception);
        }
    }

    /**
     * Điều phối thao tác tạo, cập nhật và xóa sau khi kiểm tra quyền Admin.
     * @param request request HTTP hiện tại
     * @param response response HTTP hiện tại
     * @throws ServletException khi forward lại form thất bại
     * @throws IOException khi ghi response thất bại
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareResponse(request, response);
        if (!authorize(request, response)) {
            return;
        }
        try {
            switch (request.getServletPath()) {
                case "/admin/categories/create" -> create(request, response);
                case "/admin/categories/update" -> update(request, response);
                case "/admin/categories/delete" -> delete(request, response);
                default -> response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (CategoryException exception) {
            handleServerError(response, exception);
        }
    }

    /**
     * Chuẩn bị encoding và content type trước khi đọc tham số.
     * @param request request cần cấu hình encoding
     * @param response response cần cấu hình content type
     * @throws IOException khi container không thể thiết lập response
     */
    private void prepareResponse(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
    }

    /**
     * Xác minh session đăng nhập và vai trò ADMIN.
     * @param request request chứa session
     * @param response response dùng để redirect hoặc trả 403
     * @return {@code true} nếu request được phép tiếp tục
     * @throws IOException khi không thể gửi response
     */
    private boolean authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = RoleGuard.requireLogin(request, response);
        if (user == null) {
            return false;
        }
        if (!RoleGuard.requireAdmin(request, response, user)) {
            return false;
        }
        return true;
    }

    /**
     * Tải dữ liệu phân trang và hiển thị danh sách.
     * @param request request nhận attributes của view
     * @param response response dùng để forward
     * @throws ServletException khi forward thất bại
     * @throws IOException khi forward thất bại
     * @throws CategoryException khi service không đọc được dữ liệu
     */
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CategoryException {
        String keyword = normalizeKeyword(request.getParameter("keyword"));
        String sort = "created_at".equals(request.getParameter("sort")) ? "created_at" : "name";
        String order = "DESC".equalsIgnoreCase(request.getParameter("order")) ? "DESC" : "ASC";
        int requestedPage = parsePositiveInt(request.getParameter("page")).orElse(1);
        int totalPages = categoryService.getTotalPages(keyword);
        int currentPage = Math.min(requestedPage, totalPages);
        request.setAttribute("categoryList", categoryService.getCategories(keyword, sort, order, currentPage));
        request.setAttribute("totalCategories", categoryService.countCategories(keyword));
        request.setAttribute("keyword", keyword);
        request.setAttribute("sortField", sort);
        request.setAttribute("sortOrder", order);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        moveFlashToRequest(request, FLASH_SUCCESS);
        moveFlashToRequest(request, FLASH_ERROR);
        request.getRequestDispatcher(LIST_VIEW).forward(request, response);
    }

    /**
     * Chuẩn hóa từ khóa tìm kiếm để giữ request attribute nhất quán.
     * @param keyword từ khóa có thể null
     * @return từ khóa đã bỏ khoảng trắng hoặc chuỗi rỗng
     */
    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    /**
     * Hiển thị biểu mẫu tạo với mô hình rỗng.
     * @param request request nhận attributes của form
     * @param response response dùng để forward
     * @throws ServletException khi forward thất bại
     * @throws IOException khi forward thất bại
     */
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("category", new Category());
        request.setAttribute("formMode", "create");
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /**
     * Hiển thị biểu mẫu sửa hoặc trả 400/404 cho mã không hợp lệ.
     * @param request request chứa mã thể loại
     * @param response response dùng để forward hoặc báo lỗi
     * @throws ServletException khi forward thất bại
     * @throws IOException khi ghi response thất bại
     * @throws CategoryException khi service không đọc được dữ liệu
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CategoryException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        Optional<Category> category = categoryService.findCategory(id.get());
        if (category.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("category", category.get());
        request.setAttribute("formMode", "update");
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /**
     * Hiển thị chi tiết hoặc trả 400/404 cho mã không hợp lệ.
     * @param request request chứa mã thể loại
     * @param response response dùng để forward hoặc báo lỗi
     * @throws ServletException khi forward thất bại
     * @throws IOException khi ghi response thất bại
     * @throws CategoryException khi service không đọc được dữ liệu
     */
    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CategoryException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        Optional<Category> category = categoryService.findCategory(id.get());
        if (category.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("category", category.get());
        request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
    }

    /**
     * Tạo thể loại, forward lại form khi validation lỗi và redirect khi thành công.
     * @param request request chứa dữ liệu biểu mẫu
     * @param response response dùng để forward hoặc redirect
     * @throws ServletException khi forward thất bại
     * @throws IOException khi ghi response thất bại
     * @throws CategoryException khi service không lưu được dữ liệu
     */
    private void create(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CategoryException {
        Category category = readCategory(request, 0);
        try {
            Category created = categoryService.createCategory(category, currentActor(request));
            AuditLogger.logCategoryCreate(currentActor(request), created != null ? created.getId() : 0, category.getName());
            setFlash(request, FLASH_SUCCESS, "Đã thêm thể loại thành công.");
            redirectToList(request, response);
        } catch (CategoryValidationException exception) {
            forwardInvalidForm(request, response, category, "create", exception);
        }
    }

    /**
     * Cập nhật thể loại hoặc trả lỗi khi mã không hợp lệ/không tồn tại.
     * @param request request chứa dữ liệu biểu mẫu
     * @param response response dùng để forward hoặc redirect
     * @throws ServletException khi forward thất bại
     * @throws IOException khi ghi response thất bại
     * @throws CategoryException khi service không cập nhật được dữ liệu
     */
    private void update(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CategoryException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        Category category = readCategory(request, id.get());
        try {
            if (!categoryService.updateCategory(category, currentActor(request))) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            AuditLogger.logCategoryUpdate(currentActor(request), id.get(), category.getName());
            setFlash(request, FLASH_SUCCESS, "Đã cập nhật thể loại thành công.");
            redirectToList(request, response);
        } catch (CategoryValidationException exception) {
            forwardInvalidForm(request, response, category, "update", exception);
        }
    }

    /**
     * Xóa mềm thể loại không còn sách tham chiếu và chuyển thông báo qua flash session.
     * @param request request chứa mã thể loại
     * @param response response dùng để redirect
     * @throws IOException khi redirect thất bại
     * @throws CategoryException khi service không xóa được dữ liệu
     */
    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, CategoryException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        String categoryName = categoryService.findCategory(id.get()).map(Category::getName).orElse("ID#" + id.get());
        try {
<<<<<<< HEAD
            boolean deleted = categoryService.deleteCategory(id.get());
            if (deleted) {
                AuditLogger.logCategoryDelete(currentActor(request), id.get(), categoryName);
            }
=======
            boolean deleted = categoryService.deleteCategory(id.get(), currentActor(request));
>>>>>>> 9065f3e1cafc318d532d930377c873bed3229d2c
            setFlash(request, deleted ? FLASH_SUCCESS : FLASH_ERROR,
                    deleted ? "Đã xóa thể loại thành công." : "Không tìm thấy thể loại cần xóa.");
        } catch (CategoryValidationException exception) {
            setFlash(request, FLASH_ERROR, exception.getValidationErrors().get("delete"));
        }
        redirectToList(request, response);
    }

    /**
     * Đọc trường name và description thành mô hình không chứa logic nghiệp vụ.
     * @param request request biểu mẫu
     * @param id mã thể loại hoặc 0 khi tạo mới
     * @return mô hình chứa nguyên dữ liệu người dùng nhập
     */
    private Category readCategory(HttpServletRequest request, int id) {
        return new Category(id, request.getParameter("name"), request.getParameter("description"));
    }

    /**
     * Parse mã dương và trả lỗi 400 nếu thiếu hoặc sai định dạng.
     * @param request request chứa tham số id
     * @param response response dùng để báo lỗi
     * @return mã hợp lệ hoặc Optional rỗng
     * @throws IOException khi gửi lỗi thất bại
     */
    private Optional<Integer> requireId(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Optional<Integer> id = parsePositiveInt(request.getParameter("id"));
        if (id.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã thể loại không hợp lệ.");
        }
        return id;
    }

    /**
     * Chuyển chuỗi thành số nguyên dương mà không làm lộ lỗi parse.
     * @param value chuỗi cần parse
     * @return số nguyên dương hoặc Optional rỗng
     */
    private Optional<Integer> parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? Optional.of(parsed) : Optional.empty();
        } catch (NumberFormatException | NullPointerException exception) {
            return Optional.empty();
        }
    }

    /**
     * Forward lại form cùng dữ liệu đã nhập và lỗi theo trường.
     * @param request request nhận attributes
     * @param response response dùng để forward
     * @param category dữ liệu đã nhập
     * @param formMode create hoặc update
     * @param exception ngoại lệ chứa lỗi validation
     * @throws ServletException khi forward thất bại
     * @throws IOException khi forward thất bại
     */
    private void forwardInvalidForm(HttpServletRequest request, HttpServletResponse response,
            Category category, String formMode, CategoryValidationException exception)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute("category", category);
        request.setAttribute("formMode", formMode);
        request.setAttribute("validationErrors", exception.getValidationErrors());
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /**
     * Lấy tên tài khoản đã xác thực để ghi audit.
     * @param request request có session Admin
     * @return chuỗi tài khoản, giới hạn theo cột created_by/updated_by
     */
    private String currentActor(HttpServletRequest request) {
        User user = RoleGuard.getLoggedUser(request);
        String actor = user.getUsername();
        return actor.length() <= 50 ? actor : actor.substring(0, 50);
    }

    /**
     * Lưu thông báo một lần trong session.
     * @param request request hiện tại
     * @param key tên flash attribute
     * @param message nội dung thân thiện với người dùng
     */
    private void setFlash(HttpServletRequest request, String key, String message) {
        request.getSession(false).setAttribute(key, message);
    }

    /**
     * Chuyển flash attribute từ session sang request rồi xóa khỏi session.
     * @param request request nhận thông báo
     * @param key tên flash attribute
     */
    private void moveFlashToRequest(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        Object message = session.getAttribute(key);
        if (message != null) {
            request.setAttribute(key, message);
            session.removeAttribute(key);
        }
    }

    /**
     * Redirect về danh sách theo Post/Redirect/Get.
     * @param request request dùng để lấy context path
     * @param response response dùng để redirect
     * @throws IOException khi redirect thất bại
     */
    private void redirectToList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + LIST_PATH);
    }

    /**
     * Log lỗi nội bộ một lần tại biên HTTP và trả mã 500 không lộ chi tiết.
     * @param response response dùng để trả lỗi
     * @param exception lỗi ứng dụng cần log
     * @throws IOException khi gửi lỗi thất bại
     */
    private void handleServerError(HttpServletResponse response, CategoryException exception)
            throws IOException {
        LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Không thể xử lý yêu cầu thể loại vào lúc này.");
    }
}
