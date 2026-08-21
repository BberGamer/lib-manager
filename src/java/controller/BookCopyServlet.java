package controller;

import dao.BookCopyDAO;
import dao.BookDAO;
import dao.BookDAOImpl;
import model.Book;
import model.BookCopy;
import model.User;
import model.Shelf;
import service.ShelfService;
import utils.AuditLogger;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "BookCopyServlet", urlPatterns = {
    "/book/copy/add", "/librarian/book/copy/add", "/admin/book/copy/add",
    "/book/copy/edit", "/librarian/book/copy/edit", "/admin/book/copy/edit",
    "/book/copy/delete", "/librarian/book/copy/delete", "/admin/book/copy/delete"
})
public class BookCopyServlet extends HttpServlet {

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

    /**
     * Nạp danh sách tất cả các kệ từ database vào request attributes 
     * để hiển thị trên dropdown của form bản sao.
     * Nếu xảy ra lỗi, gán danh sách rỗng để tránh gây lỗi JSP compilation.
     * 
     * @param request đối tượng HttpServletRequest cần gắn thuộc tính
     */
    private void loadShelvesList(HttpServletRequest request) {
        try {
            ShelfService shelfService = new ShelfService();
            List<Shelf> shelves = shelfService.getAllShelvesForSelection();
            request.setAttribute("shelvesList", shelves);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("shelvesList", new ArrayList<Shelf>());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String path = request.getServletPath();
        String ctx = request.getContextPath();

        // Authorization Check
        HttpSession session = request.getSession(false);
        User loggedUser = (session != null) ? (User) session.getAttribute("loggedUser") : null;
        if (loggedUser == null) {
            response.sendRedirect(ctx + "/login");
            return;
        }

        // Add and Edit require Admin or Librarian
        // Delete requires Admin only
        if (path.endsWith("/book/copy/delete")) {
            if (!loggedUser.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
                return;
            }
            processDelete(request, response, loggedUser.getUsername());
        } else {
            if (!loggedUser.isAdminOrLibrarian()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
                return;
            }
            if (path.endsWith("/book/copy/add")) {
                showAddForm(request, response);
            } else if (path.endsWith("/book/copy/edit")) {
                showEditForm(request, response);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String path = request.getServletPath();
        String ctx = request.getContextPath();

        // Authorization Check
        HttpSession session = request.getSession(false);
        User loggedUser = (session != null) ? (User) session.getAttribute("loggedUser") : null;
        if (loggedUser == null) {
            response.sendRedirect(ctx + "/login");
            return;
        }
        if (!loggedUser.isAdminOrLibrarian()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
            return;
        }

        if (path.endsWith("/book/copy/add")) {
            processAdd(request, response, loggedUser.getUsername());
        } else if (path.endsWith("/book/copy/edit")) {
            processEdit(request, response, loggedUser.getUsername());
        }
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String redirectBase = getRedirectBase(request);
        int bookId = parseId(request.getParameter("bookId"));
        if (bookId <= 0) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        BookDAO bookDao = new BookDAOImpl();
        Book book = null;
        try {
            book = bookDao.findById(bookId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (book == null) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        setSidebarAttributes(request);
        loadShelvesList(request);
        request.setAttribute("formMode", "add");
        request.setAttribute("book", book);
        request.setAttribute("pageTitle", "Thêm bản sao mới – FPT Library");
        request.getRequestDispatcher("/WEB-INF/views/book_copy_form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String redirectBase = getRedirectBase(request);
        int id = parseId(request.getParameter("id"));
        if (id <= 0) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        BookCopyDAO copyDao = new BookCopyDAO();
        BookCopy copy = null;
        try {
            copy = copyDao.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (copy == null) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        setSidebarAttributes(request);
        loadShelvesList(request);
        request.setAttribute("formMode", "edit");
        request.setAttribute("copy", copy);
        request.setAttribute("book", copy.getBook());
        request.setAttribute("pageTitle", "Chỉnh sửa bản sao – FPT Library");
        request.getRequestDispatcher("/WEB-INF/views/book_copy_form.jsp").forward(request, response);
    }

    private void processAdd(HttpServletRequest request, HttpServletResponse response, String operator)
            throws ServletException, IOException {
        String redirectBase = getRedirectBase(request);
        int bookId = parseId(request.getParameter("bookId"));
        if (bookId <= 0) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        BookDAO bookDao = new BookDAOImpl();
        Book book = null;
        try {
            book = bookDao.findById(bookId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (book == null) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        String barcode = trim(request.getParameter("barcode"));
        String condition = "GOOD"; // Tình trạng mặc định là GOOD khi thêm mới
        String area = trim(request.getParameter("area"));
        String shelf = trim(request.getParameter("shelf"));
        String slot = trim(request.getParameter("slot"));
        String note = trim(request.getParameter("note"));

        List<String> errors = new ArrayList<>();
        if (barcode == null || barcode.isEmpty()) {
            errors.add("Mã bản sao (Barcode) không được để trống.");
        } else if (barcode.length() > 50) {
            errors.add("Mã bản sao không được vượt quá 50 ký tự.");
        } else {
            BookCopyDAO copyDao = new BookCopyDAO();
            try {
                if (copyDao.isBarcodeExists(barcode, 0)) {
                    errors.add("Mã bản sao '" + barcode + "' đã tồn tại trong hệ thống.");
                }
            } catch (Exception e) {
                errors.add("Lỗi kiểm tra barcode: " + e.getMessage());
            }
        }

        if (area != null && area.length() > 50) {
            errors.add("Khu vực không được vượt quá 50 ký tự.");
        }
        if (shelf == null || shelf.trim().isEmpty()) {
            errors.add("Vui lòng chọn kệ sách.");
        } else if (shelf.length() > 20) {
            errors.add("Kệ không được vượt quá 20 ký tự.");
        }
        if (slot != null && slot.length() > 20) {
            errors.add("Ngăn không được vượt quá 20 ký tự.");
        }
        if (note != null && note.length() > 255) {
            errors.add("Ghi chú không được vượt quá 255 ký tự.");
        }

        // No status validation required

        if (!errors.isEmpty()) {
            setSidebarAttributes(request);
            loadShelvesList(request);
            request.setAttribute("formMode", "add");
            request.setAttribute("book", book);
            request.setAttribute("errors", errors);
            request.setAttribute("barcode", barcode);
            request.setAttribute("selectedCondition", condition);
            request.setAttribute("area", area);
            request.setAttribute("shelf", shelf);
            request.setAttribute("slot", slot);
            request.setAttribute("note", note);
            request.setAttribute("pageTitle", "Thêm bản sao mới – FPT Library");
            request.getRequestDispatcher("/WEB-INF/views/book_copy_form.jsp").forward(request, response);
            return;
        }

        BookCopy copy = new BookCopy();
        copy.setBookId(bookId);
        copy.setBarcode(barcode);
        copy.setBookCondition(condition == null || condition.isEmpty() ? "GOOD" : condition);
        copy.setArea(area);
        copy.setShelf(shelf);
        copy.setSlot(slot);
        copy.setNote(note);
        copy.setCreatedBy(operator);

        BookCopyDAO copyDao = new BookCopyDAO();
        boolean success = copyDao.addCopy(copy);
        if (success) {
            copyDao.addAuditLog(copy.getId(), "ADD", operator, null, copy.getBookCondition(), "Thêm bản sao mới");
            AuditLogger.logBookCopyAdd(operator, bookId, copy.getBarcode(), copy.getShelf());
            response.sendRedirect(redirectBase + "/book/copies?bookId=" + bookId + "&success=added");
        } else {
            errors.add("Thêm bản sao thất bại do lỗi hệ thống.");
            setSidebarAttributes(request);
            loadShelvesList(request);
            request.setAttribute("formMode", "add");
            request.setAttribute("book", book);
            request.setAttribute("errors", errors);
            request.setAttribute("pageTitle", "Thêm bản sao mới – FPT Library");
            request.getRequestDispatcher("/WEB-INF/views/book_copy_form.jsp").forward(request, response);
        }
    }

    private void processEdit(HttpServletRequest request, HttpServletResponse response, String operator)
            throws ServletException, IOException {
        String redirectBase = getRedirectBase(request);
        int id = parseId(request.getParameter("id"));
        if (id <= 0) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        BookCopyDAO copyDao = new BookCopyDAO();
        BookCopy existingCopy = null;
        try {
            existingCopy = copyDao.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (existingCopy == null) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        String area = trim(request.getParameter("area"));
        String shelf = trim(request.getParameter("shelf"));
        String slot = trim(request.getParameter("slot"));
        String note = trim(request.getParameter("note"));

        String barcode = trim(request.getParameter("barcode"));
        String condition = trim(request.getParameter("bookCondition"));
        List<String> errors = new ArrayList<>();
        if (barcode == null || barcode.isEmpty()) {
            errors.add("Mã bản sao (Barcode) không được để trống.");
        } else if (barcode.length() > 50) {
            errors.add("Mã bản sao không được vượt quá 50 ký tự.");
        } else {
            try {
                if (copyDao.isBarcodeExists(barcode, id)) {
                    errors.add("Mã bản sao '" + barcode + "' đã được sử dụng bởi bản sao khác.");
                }
            } catch (Exception e) {
                errors.add("Lỗi kiểm tra barcode: " + e.getMessage());
            }
        }

        if (area != null && area.length() > 50) {
            errors.add("Khu vực không được vượt quá 50 ký tự.");
        }
        if (shelf == null || shelf.trim().isEmpty()) {
            errors.add("Vui lòng chọn kệ sách.");
        } else if (shelf.length() > 20) {
            errors.add("Kệ không được vượt quá 20 ký tự.");
        }
        if (slot != null && slot.length() > 20) {
            errors.add("Ngăn không được vượt quá 20 ký tự.");
        }
        if (note != null && note.length() > 255) {
            errors.add("Ghi chú không được vượt quá 255 ký tự.");
        }

        // No status validation required

        if (!errors.isEmpty()) {
            setSidebarAttributes(request);
            loadShelvesList(request);
            request.setAttribute("formMode", "edit");
            request.setAttribute("copy", existingCopy);
            request.setAttribute("book", existingCopy.getBook());
            request.setAttribute("errors", errors);
            request.setAttribute("barcode", barcode);
            request.setAttribute("selectedCondition", condition);
            request.setAttribute("area", area);
            request.setAttribute("shelf", shelf);
            request.setAttribute("slot", slot);
            request.setAttribute("note", note);
            request.setAttribute("pageTitle", "Chỉnh sửa bản sao – FPT Library");
            request.getRequestDispatcher("/WEB-INF/views/book_copy_form.jsp").forward(request, response);
            return;
        }

        String oldCondition = existingCopy.getBookCondition();

        existingCopy.setBarcode(barcode);
        existingCopy.setBookCondition(condition);
        existingCopy.setArea(area);
        existingCopy.setShelf(shelf);
        existingCopy.setSlot(slot);
        existingCopy.setNote(note);
        existingCopy.setUpdatedBy(operator);

        boolean success = copyDao.updateCopy(existingCopy);
        if (success) {
            copyDao.addAuditLog(id, "UPDATE", operator, oldCondition, condition, "Cập nhật bản sao");
            AuditLogger.logBookCopyUpdate(operator, existingCopy.getBookId(), existingCopy.getBarcode(), condition);
            response.sendRedirect(redirectBase + "/book/copies?bookId=" + existingCopy.getBookId() + "&success=updated");
        } else {
            errors.add("Cập nhật bản sao thất bại do lỗi hệ thống.");
            setSidebarAttributes(request);
            loadShelvesList(request);
            request.setAttribute("formMode", "edit");
            request.setAttribute("copy", existingCopy);
            request.setAttribute("book", existingCopy.getBook());
            request.setAttribute("errors", errors);
            request.setAttribute("pageTitle", "Chỉnh sửa bản sao – FPT Library");
            request.getRequestDispatcher("/WEB-INF/views/book_copy_form.jsp").forward(request, response);
        }
    }

    private void processDelete(HttpServletRequest request, HttpServletResponse response, String operator)
            throws ServletException, IOException {
        String redirectBase = getRedirectBase(request);
        int id = parseId(request.getParameter("id"));
        if (id <= 0) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        BookCopyDAO copyDao = new BookCopyDAO();
        BookCopy copy = null;
        try {
            copy = copyDao.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (copy == null) {
            response.sendRedirect(redirectBase + "/books");
            return;
        }

        int bookId = copy.getBookId();
        String copyBarcode = copy.getBarcode();

        // Constraint check: Cannot delete BORROWED or RESERVED copy
        boolean isBorrowedOrReserved = false;
        try {
            isBorrowedOrReserved = copyDao.isCopyBorrowedOrReserved(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isBorrowedOrReserved) {
            response.sendRedirect(redirectBase + "/book/copies?bookId=" + bookId + "&error=cannot_delete");
            return;
        }

        boolean success = copyDao.deleteCopy(id, operator);
        if (success) {
            copyDao.addAuditLog(id, "DELETE", operator, copy.getBookCondition(), null, "Xóa bản sao");
            AuditLogger.logBookCopyDelete(operator, bookId, copyBarcode);
            response.sendRedirect(redirectBase + "/book/copies?bookId=" + bookId + "&success=deleted");
        } else {
            response.sendRedirect(redirectBase + "/book/copies?bookId=" + bookId + "&error=delete_failed");
        }
    }

    private int parseId(String s) {
        if (s == null || s.trim().isEmpty()) return -1;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return -1; }
    }

    private String trim(String s) {
        return (s != null) ? s.trim() : null;
    }
}
