/**
 * Servlet điều phối màn quản lý đồ để quên cho Admin và Librarian.
 * Lớp thuộc tầng controller, chỉ đọc request, gọi FoundItemService và forward JSP bảo vệ.
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
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
import model.FoundItem;
import model.FoundItemClaim;
import model.FoundItemStatus;
import model.User;
import exception.FoundItemException;
import exception.FoundItemValidationException;
import service.FoundItemService;
import utils.RoleGuard;
import utils.UploadUtility;

/**
 * Cung cấp các endpoint danh sách, tiếp nhận và xem chi tiết đồ để quên cho nhân viên thư viện.
 */
@WebServlet(urlPatterns = {
    "/librarian/found-items",
    "/librarian/found-items/new",
    "/librarian/found-items/view",
    "/librarian/found-items/create",
    "/librarian/found-items/verify",
    "/librarian/found-items/complete-handover"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class FoundItemManagementServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FoundItemManagementServlet.class.getName());
    private static final String LIST_VIEW = "/WEB-INF/views/admin/found-item-list.jsp";
    private static final String FORM_VIEW = "/WEB-INF/views/admin/found-item-form.jsp";
    private static final String DETAIL_VIEW = "/WEB-INF/views/admin/found-item-detail.jsp";
    private static final String FLASH_SUCCESS = "flashSuccess";
    private final FoundItemService foundItemService = new FoundItemService();

    /**
     * Xử lý các yêu cầu đọc danh sách, form tiếp nhận và chi tiết đồ vật.
     *
     * @param request request HTTP hiện tại
     * @param response response HTTP hiện tại
     * @throws ServletException khi forward JSP thất bại
     * @throws IOException khi không thể ghi response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareResponse(request, response);
        User user = authorize(request, response);
        if (user == null) {
            return;
        }
        prepareViewAttributes(request);
        try {
            String path = request.getServletPath();
            if (path.endsWith("/new")) {
                showCreateForm(request, response);
            } else if (path.endsWith("/view")) {
                showDetail(request, response);
            } else {
                showList(request, response);
            }
        } catch (FoundItemException exception) {
            handleServerError(response, exception);
        }
    }

    /**
     * Xử lý thao tác tiếp nhận đồ để quên và áp dụng Post/Redirect/Get khi thành công.
     *
     * @param request request HTTP hiện tại
     * @param response response HTTP hiện tại
     * @throws ServletException khi forward lại form lỗi thất bại
     * @throws IOException khi không thể ghi response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareResponse(request, response);
        User user = authorize(request, response);
        if (user == null) {
            return;
        }
        prepareViewAttributes(request);
        if (request.getServletPath().endsWith("/verify")) {
            reviewClaim(request, response, user);
            return;
        }
        if (request.getServletPath().endsWith("/complete-handover")) {
            completeHandover(request, response, user);
            return;
        }
        if (!request.getServletPath().endsWith("/create")) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        createFoundItem(request, response, user);
    }

    /**
     * Thiết lập encoding và content type trước khi đọc tham số request.
     *
     * @param request request cần đặt encoding
     * @param response response cần đặt content type
     */
    private void prepareResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
    }

    /**
     * Kiểm tra đăng nhập và quyền Thủ thư cho nghiệp vụ đồ để quên.
     *
     * @param request request chứa session
     * @param response response dùng để redirect hoặc trả 403
     * @return người dùng đã xác thực, hoặc null nếu request bị chặn
     * @throws IOException khi không gửi được response
     */
    private User authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = RoleGuard.requireLogin(request, response);
        if (user == null || !RoleGuard.requireLibrarian(request, response, user)) {
            return null;
        }
        return user;
    }

    /**
     * Chuẩn bị các thuộc tính dùng chung của layout quản trị.
     *
     * @param request request nhận thuộc tính view
     * @param user nhân viên đã xác thực
     */
    private void prepareViewAttributes(HttpServletRequest request) {
        request.setAttribute("isManagePageAttr", true);
        request.setAttribute("activePage", "found-items");
        request.setAttribute("pageTitle", "Quản lý đồ để quên – FPT Library");
        request.setAttribute("pageStylesheet", "/assets/css/found-item.css");
        request.setAttribute("rolePath", "/librarian");
        request.setAttribute("initialFoundItemStatus", FoundItemStatus.AVAILABLE.getDisplayName());
    }

    /**
     * Hiển thị danh sách có tìm kiếm, lọc trạng thái và phân trang.
     *
     * @param request request chứa điều kiện lọc
     * @param response response dùng để forward JSP
     * @throws ServletException khi forward thất bại
     * @throws IOException khi forward thất bại
     * @throws FoundItemException khi service không truy vấn được dữ liệu
     */
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FoundItemException {
        String keyword = normalize(request.getParameter("keyword"));
        FoundItemStatus status = parseStatus(request.getParameter("status"));
        int requestedPage = parsePositiveInt(request.getParameter("page")).orElse(1);
        int totalPages = foundItemService.getTotalPages(keyword, status);
        int currentPage = Math.min(requestedPage, totalPages);

        request.setAttribute("foundItemList", foundItemService.getFoundItems(keyword, status, currentPage));
        request.setAttribute("totalFoundItems", foundItemService.countFoundItems(keyword, status));
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedStatus", status == null ? "" : status.name());
        request.setAttribute("statusValues", FoundItemStatus.values());
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        moveFlashToRequest(request, FLASH_SUCCESS);
        request.getRequestDispatcher(LIST_VIEW).forward(request, response);
    }

    /**
     * Hiển thị form tiếp nhận với dữ liệu rỗng.
     *
     * @param request request nhận dữ liệu form
     * @param response response dùng để forward JSP
     * @throws ServletException khi forward thất bại
     * @throws IOException khi forward thất bại
     */
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("foundItem", new FoundItem());
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /**
     * Hiển thị chi tiết một đồ để quên hoặc trả lỗi 400/404 phù hợp.
     *
     * @param request request chứa id đồ vật
     * @param response response dùng để forward hoặc trả lỗi
     * @throws ServletException khi forward thất bại
     * @throws IOException khi không thể ghi response
     * @throws FoundItemException khi service không truy vấn được dữ liệu
     */
    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FoundItemException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        Optional<FoundItem> foundItem = foundItemService.findFoundItem(id.get());
        if (foundItem.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("foundItem", foundItem.get());
        Optional<FoundItemClaim> latestClaim = foundItemService.findLatestClaim(foundItem.get().getId());
        request.setAttribute("latestClaim", latestClaim.orElse(null));
        request.setAttribute("imageUrl", UploadUtility.resolveUrl(
                foundItem.get().getImagePath(), request.getContextPath()));
        request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
    }

    /**
     * Tạo đồ để quên hoặc forward lại form khi dữ liệu không hợp lệ.
     *
     * @param request request chứa dữ liệu form
     * @param response response dùng để forward hoặc redirect
     * @param user nhân viên tiếp nhận
     * @throws ServletException khi forward thất bại
     * @throws IOException khi không thể ghi response
     */
    private void createFoundItem(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        FoundItem foundItem = readFoundItem(request);
        try {
            foundItemService.validateForCreate(foundItem, user.getId());
            foundItem.setImagePath(UploadUtility.saveSecureImage(
                    request.getPart("imageFile"), request.getServletContext()));
            FoundItem createdItem = foundItemService.createFoundItem(foundItem, user.getId());
            utils.AuditLogger.logCreateFoundItem(user.getUsername(), createdItem.getId(), createdItem.getItemName());
            request.getSession(false).setAttribute(FLASH_SUCCESS,
                    "Đã tiếp nhận đồ để quên có mã LF-" + createdItem.getId() + ".");
            redirectToList(request, response);
        } catch (FoundItemValidationException exception) {
            forwardInvalidForm(request, response, foundItem, exception.getValidationErrors());
        } catch (IllegalStateException exception) {
            forwardInvalidForm(request, response, foundItem,
                    Map.of("imageFile", "Tổng dung lượng tệp tải lên không được vượt quá 6 MB."));
        } catch (IOException exception) {
            forwardInvalidForm(request, response, foundItem,
                    Map.of("imageFile", exception.getMessage()));
        } catch (FoundItemException exception) {
            handleServerError(response, exception);
        }
    }

    /**
     * Xử lý quyết định xác minh yêu cầu nhận lại của Reader và quay về chi tiết đồ.
     *
     * @param request request chứa mã yêu cầu và quyết định của Thủ thư
     * @param response response dùng để redirect
     * @param user Thủ thư đang xử lý
     * @throws IOException khi không thể redirect hoặc ghi response
     */
    private void reviewClaim(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        int itemId = 0;
        try {
            itemId = parsePositiveInt(request.getParameter("itemId")).orElseThrow(NumberFormatException::new);
            int claimId = parsePositiveInt(request.getParameter("claimId")).orElseThrow(NumberFormatException::new);
            String decision = request.getParameter("decision");
            if (!"APPROVE".equals(decision) && !"REJECT".equals(decision)) {
                throw new NumberFormatException();
            }
            boolean isApproved = "APPROVE".equals(decision);
            foundItemService.reviewClaim(claimId, user.getId(), isApproved);
            utils.AuditLogger.logVerifyFoundItemClaim(user.getUsername(), 0, itemId, isApproved);
            request.getSession(false).setAttribute(FLASH_SUCCESS, isApproved
                    ? "Đã xác minh yêu cầu. Mời Reader đến quầy để nhận đồ."
                    : "Đã từ chối yêu cầu. Đồ vật đã trở lại trạng thái có thể nhận.");
        } catch (NumberFormatException exception) {
            request.getSession(false).setAttribute("flashError", "Yêu cầu xác minh không hợp lệ.");
        } catch (FoundItemValidationException exception) {
            request.getSession(false).setAttribute("flashError",
                    exception.getValidationErrors().getOrDefault("general", "Không thể xử lý yêu cầu."));
        } catch (FoundItemException exception) {
            LOGGER.log(Level.SEVERE, "Không thể xác minh yêu cầu đồ để quên", exception);
            request.getSession(false).setAttribute("flashError", "Không thể xử lý yêu cầu xác minh lúc này.");
        }
        String redirectUrl = itemId > 0
                ? "/librarian/found-items/view?id=" + itemId : "/librarian/found-items";
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

    /**
     * Thủ thư xác nhận hoàn tất sau khi Reader đã xác nhận nhận đồ.
     *
     * @param request request chứa mã đồ và mã yêu cầu
     * @param response response dùng để redirect
     * @param user Thủ thư đang xử lý
     * @throws IOException khi không thể redirect
     */
    private void completeHandover(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        int itemId = 0;
        try {
            itemId = parsePositiveInt(request.getParameter("itemId")).orElseThrow(NumberFormatException::new);
            int claimId = parsePositiveInt(request.getParameter("claimId")).orElseThrow(NumberFormatException::new);
            foundItemService.completeHandover(claimId, user.getId());
            utils.AuditLogger.logHandoverFoundItem(user.getUsername(), 0, itemId);
            request.getSession(false).setAttribute(FLASH_SUCCESS, "Đã xác nhận giao đồ hoàn tất.");
        } catch (NumberFormatException exception) {
            request.getSession(false).setAttribute("flashError", "Yêu cầu bàn giao không hợp lệ.");
        } catch (FoundItemValidationException exception) {
            request.getSession(false).setAttribute("flashError",
                    exception.getValidationErrors().getOrDefault("general", "Không thể hoàn tất bàn giao."));
        } catch (FoundItemException exception) {
            LOGGER.log(Level.SEVERE, "Không thể hoàn tất bàn giao đồ để quên", exception);
            request.getSession(false).setAttribute("flashError", "Không thể hoàn tất bàn giao lúc này.");
        }
        String redirectUrl = itemId > 0
                ? "/librarian/found-items/view?id=" + itemId : "/librarian/found-items";
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

    /**
     * Forward lại form cùng dữ liệu đã nhập và lỗi theo trường.
     *
     * @param request request nhận thuộc tính form
     * @param response response dùng để đặt HTTP 400 và forward
     * @param foundItem dữ liệu người dùng đã nhập
     * @param validationErrors lỗi cần hiển thị
     * @throws ServletException khi forward thất bại
     * @throws IOException khi forward thất bại
     */
    private void forwardInvalidForm(HttpServletRequest request, HttpServletResponse response,
            FoundItem foundItem, Map<String, String> validationErrors) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute("foundItem", foundItem);
        request.setAttribute("validationErrors", new LinkedHashMap<>(validationErrors));
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /**
     * Đọc dữ liệu form thành model mà không thực hiện validation nghiệp vụ.
     *
     * @param request request biểu mẫu
     * @return model chứa dữ liệu người dùng nhập
     */
    private FoundItem readFoundItem(HttpServletRequest request) {
        FoundItem item = new FoundItem(request.getParameter("itemName"), request.getParameter("description"),
                parseDate(request.getParameter("foundDate")));
        return item;
    }

    /**
     * Đọc id dương hoặc trả HTTP 400 khi tham số không hợp lệ.
     *
     * @param request request chứa id
     * @param response response dùng để trả lỗi
     * @return id hợp lệ hoặc Optional rỗng
     * @throws IOException khi không thể gửi lỗi
     */
    private Optional<Integer> requireId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<Integer> id = parsePositiveInt(request.getParameter("id"));
        if (id.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã đồ để quên không hợp lệ.");
        }
        return id;
    }

    /**
     * Chuyển chuỗi trạng thái hợp lệ sang enum, bỏ qua giá trị ngoài whitelist.
     *
     * @param value giá trị status từ query string
     * @return enum hợp lệ hoặc null khi không lọc
     */
    private FoundItemStatus parseStatus(String value) {
        try {
            return value == null || value.isBlank() ? null : FoundItemStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * Chuyển chuỗi ngày ISO từ form sang LocalDate.
     *
     * @param value giá trị ngày từ form
     * @return ngày hợp lệ hoặc null để service trả lỗi theo trường
     */
    private LocalDate parseDate(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    /**
     * Chuyển chuỗi thành số nguyên dương mà không làm lộ lỗi parse.
     *
     * @param value chuỗi cần parse
     * @return số dương hợp lệ hoặc Optional rỗng
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
     * Chuẩn hóa từ khóa tìm kiếm trước khi truyền vào service.
     *
     * @param value giá trị query string có thể null
     * @return chuỗi không null đã trim
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Chuyển flash message từ session sang request và xóa sau một lần hiển thị.
     *
     * @param request request chứa session
     * @param key tên thuộc tính flash
     */
    private void moveFlashToRequest(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        Object message = session == null ? null : session.getAttribute(key);
        if (message != null) {
            request.setAttribute(key, message);
            session.removeAttribute(key);
        }
    }

    /**
     * Redirect về danh sách theo prefix quyền đã được xác thực.
     *
     * @param request request chứa rolePath
     * @param response response dùng để redirect
     * @throws IOException khi không thể redirect
     */
    private void redirectToList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + request.getAttribute("rolePath") + "/found-items");
    }

    /**
     * Ghi lỗi nội bộ một lần và trả thông báo 500 an toàn cho người dùng.
     *
     * @param response response dùng để trả lỗi
     * @param exception lỗi service cần log
     * @throws IOException khi không thể gửi lỗi
     */
    private void handleServerError(HttpServletResponse response, FoundItemException exception) throws IOException {
        LOGGER.log(Level.SEVERE, "Không thể xử lý nghiệp vụ đồ để quên.", exception);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Không thể xử lý yêu cầu đồ để quên vào lúc này.");
    }
}
