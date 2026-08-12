/**
 * Servlet điều phối HTTP workflow quản lý tác giả và bảo vệ toàn bộ endpoint bằng quyền Admin.
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Author;
import model.User;
import service.AuthorException;
import service.AuthorService;
import service.AuthorValidationException;
import utils.RoleGuard;

/**
 * Nhận request dưới /admin/authors, gọi AuthorService và forward các JSP bảo vệ.
 */
@WebServlet(urlPatterns = {
    "/admin/authors", "/admin/authors/new", "/admin/authors/view", "/admin/authors/edit",
    "/admin/authors/create", "/admin/authors/update", "/admin/authors/delete"
})
public class AuthorServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AuthorServlet.class.getName());
    private static final String LIST_PATH = "/admin/authors";
    private static final String LIST_VIEW = "/WEB-INF/views/admin/author-list.jsp";
    private static final String FORM_VIEW = "/WEB-INF/views/admin/author-form.jsp";
    private static final String DETAIL_VIEW = "/WEB-INF/views/admin/author-view.jsp";
    private static final String FLASH_SUCCESS = "flashSuccess";
    private static final String FLASH_ERROR = "flashError";

    private final AuthorService authorService = new AuthorService();

    /** Điều phối danh sách, chi tiết và biểu mẫu. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareResponse(request, response);
        if (!authorize(request, response)) {
            return;
        }
        try {
            switch (request.getServletPath()) {
                case "/admin/authors/new" -> showCreateForm(request, response);
                case "/admin/authors/view" -> showDetail(request, response);
                case "/admin/authors/edit" -> showEditForm(request, response);
                default -> showList(request, response);
            }
        } catch (AuthorException exception) {
            handleServerError(response, exception);
        }
    }

    /** Điều phối thao tác tạo, cập nhật và xóa. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareResponse(request, response);
        if (!authorize(request, response)) {
            return;
        }
        try {
            switch (request.getServletPath()) {
                case "/admin/authors/create" -> create(request, response);
                case "/admin/authors/update" -> update(request, response);
                case "/admin/authors/delete" -> delete(request, response);
                default -> response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (AuthorException exception) {
            handleServerError(response, exception);
        }
    }

    /** @param request request hiện tại @param response response hiện tại */
    private void prepareResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
    }

    /** @return true nếu người dùng đã đăng nhập với quyền Admin */
    private boolean authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = RoleGuard.requireLogin(request, response);
        return user != null && RoleGuard.requireAdmin(request, response, user);
    }

    /** Tải danh sách theo filter, sắp xếp và phân trang rồi forward sang JSP. */
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, AuthorException {
        String keyword = normalizeText(request.getParameter("keyword"));
        String sort = normalizeSort(request.getParameter("sort"));
        String order = "DESC".equalsIgnoreCase(request.getParameter("order")) ? "DESC" : "ASC";
        int requestedPage = parsePositiveInt(request.getParameter("page")).orElse(1);
        int totalPages = authorService.getTotalPages(keyword);
        int currentPage = Math.min(requestedPage, totalPages);
        request.setAttribute("authorList", authorService.getAuthors(keyword, sort, order, currentPage));
        request.setAttribute("totalAuthors", authorService.countAuthors(keyword));
        request.setAttribute("keyword", keyword);
        request.setAttribute("sortField", sort);
        request.setAttribute("sortOrder", order);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        moveFlashToRequest(request, FLASH_SUCCESS);
        moveFlashToRequest(request, FLASH_ERROR);
        request.getRequestDispatcher(LIST_VIEW).forward(request, response);
    }

    /** Hiển thị form tạo với model rỗng. */
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("author", new Author());
        request.setAttribute("formMode", "create");
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /** Hiển thị form sửa cho mã hợp lệ và đang tồn tại. */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, AuthorException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        Optional<Author> author = authorService.findAuthor(id.get());
        if (author.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("author", author.get());
        request.setAttribute("formMode", "update");
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /** Hiển thị chi tiết tác giả cho mã hợp lệ và đang tồn tại. */
    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, AuthorException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        Optional<Author> author = authorService.findAuthor(id.get());
        if (author.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("author", author.get());
        request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
    }

    /** Tạo tác giả, trả lại form khi lỗi và redirect khi thành công. */
    private void create(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, AuthorException {
        Author author = readAuthor(request, 0);
        Map<String, String> parsingErrors = readParsingErrors(request);
        if (!parsingErrors.isEmpty()) {
            forwardInvalidForm(request, response, author, "create", parsingErrors);
            return;
        }
        try {
            authorService.createAuthor(author, currentActor(request));
            setFlash(request, FLASH_SUCCESS, "Đã thêm tác giả thành công.");
            redirectToList(request, response);
        } catch (AuthorValidationException exception) {
            forwardInvalidForm(request, response, author, "create", exception.getValidationErrors());
        }
    }

    /** Cập nhật tác giả, trả lại form khi lỗi và redirect khi thành công. */
    private void update(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, AuthorException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        Author author = readAuthor(request, id.get());
        Map<String, String> parsingErrors = readParsingErrors(request);
        if (!parsingErrors.isEmpty()) {
            forwardInvalidForm(request, response, author, "update", parsingErrors);
            return;
        }
        try {
            if (!authorService.updateAuthor(author, currentActor(request))) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            setFlash(request, FLASH_SUCCESS, "Đã cập nhật tác giả thành công.");
            redirectToList(request, response);
        } catch (AuthorValidationException exception) {
            forwardInvalidForm(request, response, author, "update", exception.getValidationErrors());
        }
    }

    /** Xóa tác giả không còn liên kết sách và đặt flash message. */
    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, AuthorException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        try {
            boolean deleted = authorService.deleteAuthor(id.get());
            setFlash(request, deleted ? FLASH_SUCCESS : FLASH_ERROR,
                    deleted ? "Đã xóa tác giả thành công." : "Không tìm thấy tác giả cần xóa.");
        } catch (AuthorValidationException exception) {
            setFlash(request, FLASH_ERROR, exception.getValidationErrors().get("delete"));
        }
        redirectToList(request, response);
    }

    /** @return model chứa dữ liệu biểu mẫu; ngày sinh được parse riêng */
    private Author readAuthor(HttpServletRequest request, int id) {
        Author author = new Author();
        author.setId(id);
        author.setName(request.getParameter("name"));
        author.setNationality(request.getParameter("nationality"));
        author.setBio(request.getParameter("bio"));
        author.setAvatarUrl(request.getParameter("avatarUrl"));
        String birthDate = normalizeText(request.getParameter("birthDate"));
        if (!birthDate.isEmpty()) {
            try {
                author.setBirthDate(LocalDate.parse(birthDate));
            } catch (DateTimeParseException exception) {
                author.setBirthDate(null);
            }
        }
        return author;
    }

    /** @return lỗi parse ngày sinh theo dữ liệu request */
    private Map<String, String> readParsingErrors(HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        String birthDate = normalizeText(request.getParameter("birthDate"));
        if (!birthDate.isEmpty()) {
            try {
                LocalDate.parse(birthDate);
            } catch (DateTimeParseException exception) {
                errors.put("birthDate", "Ngày sinh không đúng định dạng.");
            }
        }
        return errors;
    }

    /** Forward lại form với dữ liệu và lỗi theo trường. */
    private void forwardInvalidForm(HttpServletRequest request, HttpServletResponse response,
            Author author, String formMode, Map<String, String> errors)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute("author", author);
        request.setAttribute("birthDateValue", request.getParameter("birthDate"));
        request.setAttribute("formMode", formMode);
        request.setAttribute("validationErrors", errors);
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /** @return id dương hoặc Optional rỗng sau khi gửi HTTP 400 */
    private Optional<Integer> requireId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<Integer> id = parsePositiveInt(request.getParameter("id"));
        if (id.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã tác giả không hợp lệ.");
        }
        return id;
    }

    /** @return số nguyên dương hoặc Optional rỗng */
    private Optional<Integer> parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? Optional.of(parsed) : Optional.empty();
        } catch (NumberFormatException | NullPointerException exception) {
            return Optional.empty();
        }
    }

    /** @return trường sort thuộc whitelist */
    private String normalizeSort(String sort) {
        return switch (sort == null ? "" : sort) {
            case "nationality", "birth_date", "created_at" -> sort;
            default -> "name";
        };
    }

    /** @return chuỗi đã trim hoặc rỗng */
    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    /** @return tên tài khoản hiện tại được giới hạn theo schema audit */
    private String currentActor(HttpServletRequest request) {
        String actor = RoleGuard.getLoggedUser(request).getUsername();
        return actor.length() <= 50 ? actor : actor.substring(0, 50);
    }

    /** Lưu flash message một lần trong session. */
    private void setFlash(HttpServletRequest request, String key, String message) {
        request.getSession(false).setAttribute(key, message);
    }

    /** Chuyển flash message từ session sang request và xóa khỏi session. */
    private void moveFlashToRequest(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        Object message = session.getAttribute(key);
        if (message != null) {
            request.setAttribute(key, message);
            session.removeAttribute(key);
        }
    }

    /** Redirect về danh sách theo Post/Redirect/Get. */
    private void redirectToList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + LIST_PATH);
    }

    /** Log lỗi tại biên HTTP và trả mã 500 không lộ chi tiết nội bộ. */
    private void handleServerError(HttpServletResponse response, AuthorException exception) throws IOException {
        LOGGER.log(Level.SEVERE, "Thao tác quản lý tác giả thất bại", exception);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Không thể xử lý yêu cầu quản lý tác giả lúc này.");
    }
}
