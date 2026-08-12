package controller;

import dao.BookCopyDAO;
import model.BookCopy;
import model.User;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "ShelfManagementServlet", urlPatterns = {
    "/admin/shelf", "/admin/shelf/update",
    "/librarian/shelf", "/librarian/shelf/update"
})
public class ShelfManagementServlet extends HttpServlet {

    private final BookCopyDAO bookCopyDAO = new BookCopyDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User loggedUser = (User) session.getAttribute("loggedUser");
        if (!loggedUser.isAdminOrLibrarian()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này.");
            return;
        }

        String servletPath = request.getServletPath();
        String rolePath = servletPath.startsWith("/admin") ? "/admin" : "/librarian";
        request.setAttribute("rolePath", rolePath);

        String area = request.getParameter("area");
        String keyword = request.getParameter("keyword");
        
        int page = 1;
        try {
            String p = request.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p.trim()));
        } catch (NumberFormatException ignored) {}

        int pageSize = 15;
        try {
            List<BookCopy> list = bookCopyDAO.getAllCopies(area, keyword, page, pageSize);
            int totalRecords = bookCopyDAO.countAllCopies(area, keyword);
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            if (totalPages < 1) totalPages = 1;

            List<String> distinctAreas = bookCopyDAO.getDistinctAreas();

            request.setAttribute("copyList", list);
            request.setAttribute("distinctAreas", distinctAreas);
            request.setAttribute("totalRecords", totalRecords);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("currentPageNum", page);
            request.setAttribute("selectedArea", area != null ? area : "");
            request.setAttribute("keyword", keyword != null ? keyword : "");
            request.setAttribute("isManagePageAttr", true);
            request.setAttribute("activePage", "shelf");
            request.setAttribute("pageTitle", "Sơ đồ bố trí kho sách – FPT Library");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải thông tin vị trí kho sách: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/shelf-map.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User loggedUser = (User) session.getAttribute("loggedUser");
        if (!loggedUser.isAdminOrLibrarian()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện hành động này.");
            return;
        }

        String path = request.getServletPath();
        String prefix = path.startsWith("/admin") ? "/admin" : "/librarian";
        
        try {
            if (path.endsWith("/shelf/update")) {
                int id = Integer.parseInt(request.getParameter("id"));
                String area = request.getParameter("area");
                String shelf = request.getParameter("shelf");
                String slot = request.getParameter("slot");

                BookCopy copy = bookCopyDAO.findById(id);
                if (copy != null) {
                    copy.setArea(area);
                    copy.setShelf(shelf);
                    copy.setSlot(slot);
                    copy.setUpdatedBy(loggedUser.getUsername());
                    
                    boolean success = bookCopyDAO.updateCopy(copy);
                    if (success) {
                        session.setAttribute("successMsg", "Cập nhật vị trí bản sao sách thành công!");
                    } else {
                        session.setAttribute("errorMsg", "Cập nhật vị trí bản sao sách thất bại!");
                    }
                } else {
                    session.setAttribute("errorMsg", "Không tìm thấy bản sao sách này!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + prefix + "/shelf");
    }
}
