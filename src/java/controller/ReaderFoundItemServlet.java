/**
 * Servlet điều phối việc Reader xem và gửi yêu cầu nhận lại đồ để quên.
 * Lớp thuộc tầng controller, gọi FoundItemService và render JSP Reader.
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.FoundItem;
import model.FoundItemStatus;
import model.User;
import exception.FoundItemException;
import exception.FoundItemValidationException;
import service.FoundItemService;
import utils.RoleGuard;
import utils.UploadUtility;

/**
 * Cung cấp danh sách đồ đang có thể nhận và tiếp nhận yêu cầu xác minh từ Reader.
 */
@WebServlet(urlPatterns = {"/found-items", "/found-items/my-claims", "/found-items/claim", "/found-items/confirm-pickup"})
public class ReaderFoundItemServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ReaderFoundItemServlet.class.getName());
    private static final String LIST_VIEW = "/WEB-INF/views/reader/found-item-list.jsp";
    private static final String CLAIM_HISTORY_VIEW = "/WEB-INF/views/reader/found-item-claims.jsp";
    private static final String FLASH_SUCCESS = "foundItemClaimSuccess";
    private static final String FLASH_ERROR = "foundItemClaimError";
    private final FoundItemService foundItemService = new FoundItemService();

    /**
     * Hiển thị các đồ mà Reader còn có thể gửi yêu cầu nhận lại.
     *
     * @param request request HTTP hiện tại
     * @param response response HTTP hiện tại
     * @throws ServletException khi không thể forward JSP
     * @throws IOException khi không thể ghi response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareResponse(request, response);
        User user = requireReader(request, response);
        if (user == null) {
            return;
        }
        try {
            if (request.getServletPath().endsWith("/my-claims")) {
                showClaimHistory(request, response, user);
                return;
            }
            String keyword = normalize(request.getParameter("keyword"));
            List<FoundItem> items = foundItemService.getFoundItems(keyword, FoundItemStatus.AVAILABLE, 1);
            Map<Integer, String> imageUrls = new HashMap<>();
            for (FoundItem item : items) {
                imageUrls.put(item.getId(), UploadUtility.resolveUrl(item.getImagePath(), request.getContextPath()));
            }
            request.setAttribute("foundItems", items);
            request.setAttribute("imageUrls", imageUrls);
            request.setAttribute("keyword", keyword);
            request.setAttribute("activePage", "found-items");
            request.setAttribute("pageTitle", "Đồ để quên – FPT Library");
            request.setAttribute("pageStylesheet", "/assets/css/reader-found-items.css");
            request.getRequestDispatcher(LIST_VIEW).forward(request, response);
        } catch (FoundItemException exception) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách đồ để quên cho Reader", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tải danh sách đồ để quên lúc này.");
        }
    }

    /**
     * Hiển thị lịch sử yêu cầu nhận đồ riêng của Reader.
     *
     * @param request request HTTP hiện tại
     * @param response response HTTP hiện tại
     * @param user Reader đã xác thực
     * @throws ServletException khi không thể forward JSP
     * @throws IOException khi không thể ghi response
     * @throws FoundItemException khi không thể truy vấn dữ liệu
     */
    private void showClaimHistory(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException, FoundItemException {
        request.setAttribute("myClaims", foundItemService.getClaimHistoryForReader(user.getId()));
        request.setAttribute("activePage", "found-items");
        request.setAttribute("pageTitle", "Yêu cầu nhận đồ của tôi – FPT Library");
        request.setAttribute("pageStylesheet", "/assets/css/reader-found-items.css");
        request.getRequestDispatcher(CLAIM_HISTORY_VIEW).forward(request, response);
    }

    /**
     * Gửi yêu cầu nhận lại, sau đó quay về danh sách theo Post/Redirect/Get.
     *
     * @param request request HTTP chứa mã đồ và ghi chú xác minh
     * @param response response HTTP hiện tại
     * @throws IOException khi không thể redirect hoặc ghi response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        prepareResponse(request, response);
        User user = requireReader(request, response);
        if (user == null) {
            return;
        }
        if (request.getServletPath().endsWith("/confirm-pickup")) {
            confirmPickup(request, response, user);
            return;
        }
        if (!request.getServletPath().endsWith("/claim")) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        try {
            int itemId = parsePositiveId(request.getParameter("itemId"));
            foundItemService.submitClaim(itemId, user.getId(), request.getParameter("claimNote"));
            request.getSession().setAttribute(FLASH_SUCCESS,
                    "Đã gửi yêu cầu. Vui lòng chờ Thủ thư xác minh trước khi đến nhận đồ.");
        } catch (NumberFormatException exception) {
            request.getSession().setAttribute(FLASH_ERROR, "Đồ để quên không hợp lệ.");
        } catch (FoundItemValidationException exception) {
            request.getSession().setAttribute(FLASH_ERROR,
                    exception.getValidationErrors().getOrDefault("claimNote",
                            exception.getValidationErrors().getOrDefault("general", "Yêu cầu chưa hợp lệ.")));
        } catch (FoundItemException exception) {
            LOGGER.log(Level.SEVERE, "Không thể gửi yêu cầu nhận lại đồ cho userId=" + user.getId(), exception);
            request.getSession().setAttribute(FLASH_ERROR, "Không thể gửi yêu cầu lúc này. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/found-items");
    }

    /**
     * Reader xác nhận đã nhận đồ tại quầy rồi quay lại trang danh sách.
     *
     * @param request request chứa mã yêu cầu
     * @param response response dùng để redirect
     * @param user Reader đã xác thực
     * @throws IOException khi không thể redirect
     */
    private void confirmPickup(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        try {
            foundItemService.confirmReaderPickup(parsePositiveId(request.getParameter("claimId")), user.getId());
            request.getSession().setAttribute(FLASH_SUCCESS,
                    "Đã xác nhận bạn đã nhận đồ. Vui lòng chờ Thủ thư xác nhận hoàn tất.");
        } catch (NumberFormatException exception) {
            request.getSession().setAttribute(FLASH_ERROR, "Yêu cầu nhận đồ không hợp lệ.");
        } catch (FoundItemValidationException exception) {
            request.getSession().setAttribute(FLASH_ERROR,
                    exception.getValidationErrors().getOrDefault("general", "Không thể xác nhận nhận đồ."));
        } catch (FoundItemException exception) {
            LOGGER.log(Level.SEVERE, "Không thể xác nhận nhận đồ cho userId=" + user.getId(), exception);
            request.getSession().setAttribute(FLASH_ERROR, "Không thể xác nhận lúc này. Vui lòng thử lại.");
        }
        response.sendRedirect(request.getContextPath() + "/found-items");
    }

    /**
     * Thiết lập mã hóa trước khi đọc dữ liệu từ request.
     *
     * @param request request HTTP cần thiết lập encoding
     * @param response response HTTP cần thiết lập content type
     * @throws IOException khi container không thể thiết lập encoding
     */
    private void prepareResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
    }

    /**
     * Kiểm tra người dùng đăng nhập có vai trò Reader.
     *
     * @param request request HTTP chứa session
     * @param response response HTTP để redirect hoặc trả lỗi quyền
     * @return Reader đã xác thực hoặc null khi request bị chặn
     * @throws IOException khi không thể gửi response
     */
    private User requireReader(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = RoleGuard.requireLogin(request, response);
        if (user == null || !RoleGuard.requireReader(request, response, user)) {
            return null;
        }
        return user;
    }

    /**
     * Đọc mã số dương từ tham số request.
     *
     * @param value chuỗi mã nhận từ request
     * @return mã số dương
     * @throws NumberFormatException khi giá trị không hợp lệ
     */
    private int parsePositiveId(String value) {
        int id = Integer.parseInt(value);
        if (id <= 0) {
            throw new NumberFormatException();
        }
        return id;
    }

    /**
     * Chuẩn hóa từ khóa tìm kiếm nullable.
     *
     * @param value chuỗi cần chuẩn hóa
     * @return chuỗi đã trim, không null
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
