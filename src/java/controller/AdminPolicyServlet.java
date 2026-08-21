/**
 * Servlet tầng controller điều phối toàn bộ workflow quản trị điều lệ dành cho Admin.
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
import model.Policy;
import model.PolicyCategory;
import model.PolicyPublicationStatus;
import model.User;
import exception.PolicyException;
import service.PolicyService;
import exception.PolicyValidationException;
import utils.AuditLogger;
import utils.RoleGuard;

/** Nhận route `/admin/policies`, gọi PolicyService và forward các JSP được bảo vệ. */
@WebServlet(urlPatterns = {
    "/admin/policies", "/admin/policies/new", "/admin/policies/view", "/admin/policies/edit",
    "/admin/policies/create", "/admin/policies/update", "/admin/policies/publish",
    "/admin/policies/archive", "/admin/policies/delete", "/admin/policies/revise"
})
public class AdminPolicyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminPolicyServlet.class.getName());
    private static final String LIST_PATH = "/admin/policies";
    private static final String LIST_VIEW = "/WEB-INF/views/admin/policy-list.jsp";
    private static final String FORM_VIEW = "/WEB-INF/views/admin/policy-form.jsp";
    private static final String DETAIL_VIEW = "/WEB-INF/views/admin/policy-view.jsp";
    private static final String FLASH_SUCCESS = "flashSuccess";
    private static final String FLASH_ERROR = "flashError";

    private final PolicyService policyService = new PolicyService();

    /** Điều phối danh sách, chi tiết và biểu mẫu sau khi xác thực Admin. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareResponse(request, response);
        if (!authorize(request, response)) {
            return;
        }
        try {
            switch (request.getServletPath()) {
                case "/admin/policies/new" -> showCreateForm(request, response);
                case "/admin/policies/edit" -> showEditForm(request, response);
                case "/admin/policies/view" -> showDetail(request, response);
                case "/admin/policies/revise" -> showRevisionForm(request, response);
                default -> showList(request, response);
            }
        } catch (PolicyException exception) {
            handleServerError(response, exception);
        }
    }

    /** Điều phối các hành động thay đổi trạng thái bằng POST. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareResponse(request, response);
        if (!authorize(request, response)) {
            return;
        }
        try {
            switch (request.getServletPath()) {
                case "/admin/policies/create" -> create(request, response);
                case "/admin/policies/update" -> update(request, response);
                case "/admin/policies/publish" -> publish(request, response);
                case "/admin/policies/archive" -> archive(request, response);
                case "/admin/policies/delete" -> delete(request, response);
                case "/admin/policies/revise" -> revise(request, response);
                default -> response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (PolicyException exception) {
            handleServerError(response, exception);
        }
    }

    /** Chuẩn bị UTF-8 trước khi đọc request parameter. */
    private void prepareResponse(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
    }

    /** @return true nếu session hiện tại thuộc Admin */
    private boolean authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = RoleGuard.requireLogin(request, response);
        return user != null && RoleGuard.requireAdmin(request, response, user);
    }

    /** Tải danh sách quản trị theo bộ lọc và phân trang. */
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, PolicyException {
        String keyword = normalizeText(request.getParameter("keyword"));
        PolicyCategory category = parseEnum(request.getParameter("category"), PolicyCategory.class);
        PolicyPublicationStatus status = parseEnum(
                request.getParameter("status"), PolicyPublicationStatus.class);
        int requestedPage = parsePositiveInt(request.getParameter("page")).orElse(1);
        int totalPages = policyService.getAdminTotalPages(keyword, category, status);
        int currentPage = Math.min(requestedPage, totalPages);
        request.setAttribute("policyList", policyService.getAdminPolicies(
                keyword, category, status, currentPage));
        request.setAttribute("totalPolicies", policyService.countAdminPolicies(keyword, category, status));
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedCategory", category);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        prepareOptions(request);
        moveFlashToRequest(request, FLASH_SUCCESS);
        moveFlashToRequest(request, FLASH_ERROR);
        request.getRequestDispatcher(LIST_VIEW).forward(request, response);
    }

    /** Hiển thị form tạo draft rỗng. */
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Policy policy = new Policy();
        policy.setCategory(PolicyCategory.GENERAL);
        request.setAttribute("policy", policy);
        request.setAttribute("formMode", "create");
        prepareOptions(request);
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /** Hiển thị form sửa nếu ID là một draft đang tồn tại. */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, PolicyException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        Optional<Policy> policy = policyService.findAdminPolicy(id.get());
        if (policy.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (policy.get().getPublicationStatus() != PolicyPublicationStatus.DRAFT) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Chỉ bản nháp mới được chỉnh sửa.");
            return;
        }
        request.setAttribute("policy", policy.get());
        request.setAttribute("formMode", "update");
        prepareOptions(request);
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /** Mở form revision từ bản xuất bản mà chưa ghi thêm bản ghi vào database. */
    private void showRevisionForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, PolicyException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        Optional<Policy> source = policyService.findAdminPolicy(id.get());
        if (source.isEmpty() || source.get().getPublicationStatus() != PolicyPublicationStatus.PUBLISHED) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Chỉ tạo được phiên bản mới từ điều lệ đã xuất bản.");
            return;
        }
        Optional<Policy> existingDraft = policyService.findDraftByCode(source.get().getPolicyCode());
        if (existingDraft.isPresent()) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/policies/edit?id=" + existingDraft.get().getId());
            return;
        }
        Policy revision = copyRevisionForm(source.get());
        request.setAttribute("policy", revision);
        request.setAttribute("formMode", "revision");
        request.setAttribute("sourceId", source.get().getId());
        prepareOptions(request);
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /** Hiển thị chi tiết một điều lệ cho Admin. */
    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, PolicyException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        Optional<Policy> policy = policyService.findAdminPolicy(id.get());
        if (policy.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("policy", policy.get());
        request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
    }

    /** Tạo draft và dùng PRG khi thành công. */
    private void create(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, PolicyException {
        Policy policy = readPolicy(request, 0);
        Map<String, String> parsingErrors = readParsingErrors(request);
        if (!parsingErrors.isEmpty()) {
            forwardInvalidForm(request, response, policy, "create", parsingErrors);
            return;
        }
        try {
            policyService.createDraft(policy, currentActor(request));
            AuditLogger.logPolicyCreate(currentActor(request), policy.getId(), policy.getTitle() != null ? policy.getTitle() : "");
            setFlash(request, FLASH_SUCCESS, "Đã tạo bản nháp điều lệ thành công.");
            redirectToList(request, response);
        } catch (PolicyValidationException exception) {
            forwardInvalidForm(request, response, policy, "create", exception.getValidationErrors());
        }
    }

    /** Cập nhật draft và dùng PRG khi thành công. */
    private void update(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, PolicyException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        Policy policy = readPolicy(request, id.get());
        Map<String, String> parsingErrors = readParsingErrors(request);
        if (!parsingErrors.isEmpty()) {
            forwardInvalidForm(request, response, policy, "update", parsingErrors);
            return;
        }
        try {
            if (!policyService.updateDraft(policy, currentActor(request))) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            setFlash(request, FLASH_SUCCESS, "Đã cập nhật bản nháp điều lệ.");
            redirectToList(request, response);
        } catch (PolicyValidationException exception) {
            forwardInvalidForm(request, response, policy, "update", exception.getValidationErrors());
        }
    }

    /** Xuất bản draft sau khi service kiểm tra vòng đời và khoảng hiệu lực. */
    private void publish(HttpServletRequest request, HttpServletResponse response)
            throws IOException, PolicyException {
        performAction(request, response, "publish");
    }

    /** Lưu trữ một bản đã xuất bản. */
    private void archive(HttpServletRequest request, HttpServletResponse response)
            throws IOException, PolicyException {
        performAction(request, response, "archive");
    }

    /** Xóa mềm một draft. */
    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, PolicyException {
        performAction(request, response, "delete");
    }

    /** Lưu revision sau khi Admin xác nhận bằng nút Lưu bản nháp. */
    private void revise(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, PolicyException {
        Optional<Integer> sourceId = parsePositiveInt(request.getParameter("sourceId"));
        if (sourceId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Điều lệ nguồn không hợp lệ.");
            return;
        }
        Policy revision = readPolicy(request, 0);
        request.setAttribute("sourceId", sourceId.get());
        Map<String, String> parsingErrors = readParsingErrors(request);
        if (!parsingErrors.isEmpty()) {
            forwardInvalidForm(request, response, revision, "revision", parsingErrors);
            return;
        }
        try {
            Policy savedRevision = policyService.createRevision(
                    sourceId.get(), revision, currentActor(request));
            AuditLogger.logPolicyRevise(currentActor(request), sourceId.get(), savedRevision.getId(),
                    savedRevision.getTitle() != null ? savedRevision.getTitle() : "");
            setFlash(request, FLASH_SUCCESS,
                    "Đã tạo bản nháp phiên bản " + savedRevision.getVersion() + ".");
            redirectToList(request, response);
        } catch (PolicyValidationException exception) {
            forwardInvalidForm(request, response, revision,
                    "revision", exception.getValidationErrors());
        }
    }

    /** @return bản sao chỉ dùng để hiển thị form revision, chưa có ID database */
    private Policy copyRevisionForm(Policy source) {
        Policy revision = new Policy();
        revision.setPolicyCode(source.getPolicyCode());
        revision.setTitle(source.getTitle());
        revision.setContent(source.getContent());
        revision.setCategory(source.getCategory());
        revision.setEffectiveFrom(source.getEffectiveFrom());
        revision.setEffectiveTo(source.getEffectiveTo());
        return revision;
    }

    /** Thực hiện action trạng thái, chuyển lỗi nghiệp vụ thành flash message. */
    private void performAction(HttpServletRequest request, HttpServletResponse response, String action)
            throws IOException, PolicyException {
        Optional<Integer> id = requireId(request, response);
        if (id.isEmpty()) {
            return;
        }
        try {
            String successMessage;
            switch (action) {
                case "publish" -> {
                    policyService.publishPolicy(id.get(), currentActor(request));
                    successMessage = "Đã xuất bản điều lệ thành công.";
                    // Lấy policy sau publish để ghi title
                    policyService.findAdminPolicy(id.get()).ifPresent(p ->
                        AuditLogger.logPolicyPublish(currentActor(request), p.getId(), p.getTitle() != null ? p.getTitle() : ""));
                }
                case "archive" -> {
                    // Lấy title trước khi archive
                    String archiveTitle = policyService.findAdminPolicy(id.get())
                            .map(p -> p.getTitle() != null ? p.getTitle() : "").orElse("");
                    policyService.archivePolicy(id.get(), currentActor(request));
                    successMessage = "Đã lưu trữ điều lệ thành công.";
                    AuditLogger.logPolicyArchive(currentActor(request), id.get(), archiveTitle);
                }
                default -> {
                    // delete: lấy title trước khi xóa
                    String deleteTitle = policyService.findAdminPolicy(id.get())
                            .map(p -> p.getTitle() != null ? p.getTitle() : "").orElse("");
                    policyService.deleteDraft(id.get(), currentActor(request));
                    successMessage = "Đã xóa bản nháp điều lệ.";
                    AuditLogger.logPolicyDelete(currentActor(request), id.get(), deleteTitle);
                }
            }
            setFlash(request, FLASH_SUCCESS, successMessage);
        } catch (PolicyValidationException exception) {
            String message = exception.getValidationErrors().values().stream().findFirst()
                    .orElse("Không thể thực hiện thao tác điều lệ.");
            setFlash(request, FLASH_ERROR, message);
        }
        redirectToList(request, response);
    }

    /** Đọc request thành model; lỗi parse được thu thập riêng. */
    private Policy readPolicy(HttpServletRequest request, int id) {
        Policy policy = new Policy();
        policy.setId(id);
        policy.setPolicyCode(request.getParameter("policyCode"));
        policy.setTitle(request.getParameter("title"));
        policy.setContent(request.getParameter("content"));
        policy.setCategory(parseEnum(request.getParameter("category"), PolicyCategory.class));
        policy.setEffectiveFrom(parseDate(request.getParameter("effectiveFrom")));
        policy.setEffectiveTo(parseDate(request.getParameter("effectiveTo")));
        return policy;
    }

    /** @return lỗi parse số phiên bản, enum và ngày theo từng trường */
    private Map<String, String> readParsingErrors(HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (parseEnum(request.getParameter("category"), PolicyCategory.class) == null) {
            errors.put("category", "Danh mục điều lệ không hợp lệ.");
        }
        validateDateParameter(request, "effectiveFrom", "Ngày bắt đầu", errors);
        validateDateParameter(request, "effectiveTo", "Ngày kết thúc", errors);
        return errors;
    }

    /** Thêm lỗi nếu một tham số ngày không rỗng nhưng sai định dạng. */
    private void validateDateParameter(HttpServletRequest request, String name, String label,
            Map<String, String> errors) {
        String value = normalizeText(request.getParameter(name));
        if (!value.isEmpty() && parseDate(value) == null) {
            errors.put(name, label + " không đúng định dạng.");
        }
    }

    /** Forward form lỗi, giữ nguyên các giá trị ngày người dùng nhập. */
    private void forwardInvalidForm(HttpServletRequest request, HttpServletResponse response,
            Policy policy, String formMode, Map<String, String> errors)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute("policy", policy);
        request.setAttribute("formMode", formMode);
        request.setAttribute("effectiveFromValue", request.getParameter("effectiveFrom"));
        request.setAttribute("effectiveToValue", request.getParameter("effectiveTo"));
        request.setAttribute("validationErrors", errors);
        prepareOptions(request);
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    /** Đưa enum options vào request để JSP chỉ làm nhiệm vụ trình bày. */
    private void prepareOptions(HttpServletRequest request) {
        request.setAttribute("policyCategories", PolicyCategory.values());
        request.setAttribute("policyStatuses", PolicyPublicationStatus.values());
    }

    /** @return ID dương hoặc Optional rỗng sau khi gửi HTTP 400 */
    private Optional<Integer> requireId(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Optional<Integer> id = parsePositiveInt(request.getParameter("id"));
        if (id.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã điều lệ không hợp lệ.");
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

    /** @return LocalDate hoặc null nếu rỗng/sai định dạng */
    private LocalDate parseDate(String value) {
        String normalized = normalizeText(value);
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    /** @return enum hợp lệ hoặc null */
    private <T extends Enum<T>> T parseEnum(String value, Class<T> enumType) {
        try {
            return Enum.valueOf(enumType, normalizeText(value).toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /** @return chuỗi đã trim hoặc rỗng */
    private String normalizeText(String value) { return value == null ? "" : value.trim(); }

    /** @return username hiện tại giới hạn theo schema */
    private String currentActor(HttpServletRequest request) {
        String actor = RoleGuard.getLoggedUser(request).getUsername();
        return actor.length() <= 50 ? actor : actor.substring(0, 50);
    }

    /** Lưu flash message trong session hiện tại. */
    private void setFlash(HttpServletRequest request, String key, String message) {
        request.getSession(false).setAttribute(key, message);
    }

    /** Chuyển và xóa flash message một lần. */
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

    /** Ghi log lỗi tại HTTP boundary và trả thông báo an toàn. */
    private void handleServerError(HttpServletResponse response, PolicyException exception) throws IOException {
        LOGGER.log(Level.SEVERE, "Thao tác quản lý điều lệ thất bại", exception);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Không thể xử lý yêu cầu điều lệ lúc này.");
    }
}
