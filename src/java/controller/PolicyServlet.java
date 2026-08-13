/**
 * Servlet tầng controller cung cấp danh sách và chi tiết điều lệ đang hiệu lực.
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Policy;
import model.PolicyCategory;
import exception.PolicyException;
import service.PolicyService;

/** Phục vụ route đọc `/policies` mà không để lộ draft hoặc bản ngoài thời gian hiệu lực. */
@WebServlet(urlPatterns = {"/policies", "/policies/view"})
public class PolicyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PolicyServlet.class.getName());
    private static final String LIST_VIEW = "/WEB-INF/views/policy-list.jsp";
    private static final String DETAIL_VIEW = "/WEB-INF/views/policy-view.jsp";
    private final PolicyService policyService = new PolicyService();

    /** Hiển thị danh sách hoặc chi tiết điều lệ đang hiệu lực. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        try {
            if ("/policies/view".equals(request.getServletPath())) {
                showDetail(request, response);
            } else {
                showList(request, response);
            }
        } catch (PolicyException exception) {
            LOGGER.log(Level.SEVERE, "Không thể tải điều lệ công khai", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tải điều lệ lúc này.");
        }
    }

    /** Tải danh sách đang hiệu lực theo từ khóa, danh mục và trang. */
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, PolicyException {
        String keyword = normalizeText(request.getParameter("keyword"));
        PolicyCategory category = parseCategory(request.getParameter("category"));
        int requestedPage = parsePositiveInt(request.getParameter("page")).orElse(1);
        int totalPages = policyService.getEffectiveTotalPages(keyword, category);
        int currentPage = Math.min(requestedPage, totalPages);
        request.setAttribute("policyList", policyService.getEffectivePolicies(keyword, category, currentPage));
        request.setAttribute("totalPolicies", policyService.countEffectivePolicies(keyword, category));
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedCategory", category);
        request.setAttribute("policyCategories", PolicyCategory.values());
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.getRequestDispatcher(LIST_VIEW).forward(request, response);
    }

    /** Hiển thị chi tiết nếu ID đang có hiệu lực, ngược lại trả 404. */
    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, PolicyException {
        Optional<Integer> id = parsePositiveInt(request.getParameter("id"));
        if (id.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã điều lệ không hợp lệ.");
            return;
        }
        Optional<Policy> policy = policyService.findEffectivePolicy(id.get());
        if (policy.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("policy", policy.get());
        request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
    }

    /** @return danh mục hợp lệ hoặc null để bỏ lọc */
    private PolicyCategory parseCategory(String value) {
        try {
            return PolicyCategory.valueOf(normalizeText(value).toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
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

    /** @return chuỗi đã trim hoặc rỗng */
    private String normalizeText(String value) { return value == null ? "" : value.trim(); }
}
