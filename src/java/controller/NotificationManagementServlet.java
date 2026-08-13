package controller;

import dao.BorrowRecordDAO;
import dao.FineDAO;
import dao.UserDAO;
import dao.NotificationDAO;
import model.BorrowRecord;
import model.Fine;
import model.User;
import model.Notification;
import service.NotificationService;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "NotificationManagementServlet", urlPatterns = {
    "/admin/notification/manage", "/admin/notification/send",
    "/librarian/notification/manage", "/librarian/notification/send"
})
public class NotificationManagementServlet extends HttpServlet {

    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
    private final FineDAO fineDAO = new FineDAO();
    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final NotificationService notificationService = new NotificationService();

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
        System.out.println("DEBUG: User accessed notification/manage: " + (loggedUser != null ? (loggedUser.getUsername() + ", Role: " + loggedUser.getRole() + ", isAdminOrLibrarian: " + loggedUser.isAdminOrLibrarian()) : "null"));
        if (!loggedUser.isAdminOrLibrarian()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này.");
            return;
        }

        String filterType = request.getParameter("filterType");
        int page = 1;
        try {
            String p = request.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p.trim()));
        } catch (NumberFormatException ignored) {}

        int pageSize = 15;

        // Set safe defaults to prevent JSP NullPointerExceptions
        request.setAttribute("nearDueLoans", new java.util.ArrayList<BorrowRecord>());
        request.setAttribute("overdueLoans", new java.util.ArrayList<BorrowRecord>());
        request.setAttribute("unpaidFines", new java.util.ArrayList<Fine>());
        request.setAttribute("sentHistory", new java.util.ArrayList<Notification>());
        request.setAttribute("totalSent", 0);
        request.setAttribute("totalPages", 1);
        request.setAttribute("currentPageNum", page);
        request.setAttribute("selectedFilterType", filterType != null ? filterType : "");
        request.setAttribute("isManagePageAttr", true);
        request.setAttribute("activePage", "notifications");
        request.setAttribute("pageTitle", "Quản lý nhắc nhở & thông báo – FPT Library");

        try {
            // Auto Reminders data
            List<BorrowRecord> nearDueLoans = borrowRecordDAO.getNearDueLoans(3);
            List<BorrowRecord> overdueLoans = borrowRecordDAO.getOverdueLoans();
            List<Fine> unpaidFines = fineDAO.searchFines("UNPAID", null, 1, 100);

            // Custom Notification history data
            List<Notification> sentHistory = notificationDAO.getAllSentNotifications(filterType, page, pageSize);
            int totalSent = notificationDAO.countAllSentNotifications(filterType);
            int totalPages = (int) Math.ceil((double) totalSent / pageSize);
            if (totalPages < 1) totalPages = 1;

            request.setAttribute("nearDueLoans", nearDueLoans);
            request.setAttribute("overdueLoans", overdueLoans);
            request.setAttribute("unpaidFines", unpaidFines);
            request.setAttribute("sentHistory", sentHistory);
            request.setAttribute("totalSent", totalSent);
            request.setAttribute("totalPages", totalPages);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải dữ liệu thông báo: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/notification-manage.jsp").forward(request, response);
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
        String prefix = loggedUser.isAdmin() ? "/admin" : "/librarian";
        
        String action = request.getParameter("action");
        try {
            if ("send-due".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                BorrowRecord loan = borrowRecordDAO.findById(id);
                if (loan != null && loan.getUser() != null) {
                    String title = "Nhắc nhở: Sách mượn sắp đến hạn trả – FPT Library";
                    String message = "Xin chào " + loan.getUser().getFullName() + ",\n\n"
                            + "Cuốn sách '" + loan.getBook().getTitle() + "' bạn mượn vào ngày " + loan.getBorrowDate()
                            + " sắp hết hạn trả vào ngày " + loan.getDueDate() + ".\n"
                            + "Vui lòng hoàn trả sách hoặc tiến hành gia hạn trên hệ thống đúng thời hạn để tránh phát sinh phạt.";
                    
                    boolean sent = notificationService.createAndSendNotification(
                            loan.getUserId(), title, message, "DUE_REMINDER", loan.getId(), "borrow_record", loan.getUser().getEmail());
                    if (sent) {
                        session.setAttribute("successMsg", "Đã gửi thông báo sắp đến hạn tới " + loan.getUser().getFullName() + " thành công!");
                    } else {
                        session.setAttribute("errorMsg", "Gửi thông báo thất bại!");
                    }
                }
            } else if ("send-overdue".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                BorrowRecord loan = borrowRecordDAO.findById(id);
                if (loan != null && loan.getUser() != null) {
                    String title = "Cảnh báo: Sách mượn ĐÃ QUÁ HẠN TRẢ – FPT Library";
                    String message = "Xin chào " + loan.getUser().getFullName() + ",\n\n"
                            + "Cuốn sách '" + loan.getBook().getTitle() + "' bạn mượn vào ngày " + loan.getBorrowDate()
                            + " đã quá hạn hoàn trả (Hạn trả là ngày: " + loan.getDueDate() + ").\n"
                            + "Vui lòng trả sách về thư viện sớm nhất có thể để hạn chế mức phạt phát sinh thêm.";
                    
                    boolean sent = notificationService.createAndSendNotification(
                            loan.getUserId(), title, message, "OVERDUE", loan.getId(), "borrow_record", loan.getUser().getEmail());
                    if (sent) {
                        session.setAttribute("successMsg", "Đã gửi cảnh báo quá hạn tới " + loan.getUser().getFullName() + " thành công!");
                    } else {
                        session.setAttribute("errorMsg", "Gửi thông báo thất bại!");
                    }
                }
            } else if ("send-fine".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Fine fine = fineDAO.findById(id);
                if (fine != null && fine.getUser() != null) {
                    String title = "Thông báo: Phát sinh phí phạt độc giả – FPT Library";
                    String message = "Xin chào " + fine.getUser().getFullName() + ",\n\n"
                            + "Bạn có một khoản phạt chưa thanh toán trị giá " + fine.getAmount() + "đ.\n"
                            + "Lý do phạt: " + fine.getReason() + ".\n"
                            + "Vui lòng đến thư viện để nộp phạt hoặc liên hệ thủ thư để được xử lý.";
                    
                    boolean sent = notificationService.createAndSendNotification(
                            fine.getUserId(), title, message, "FINE", fine.getId(), "fine", fine.getUser().getEmail());
                    if (sent) {
                        session.setAttribute("successMsg", "Đã gửi thông báo phạt tới " + fine.getUser().getFullName() + " thành công!");
                    } else {
                        session.setAttribute("errorMsg", "Gửi thông báo thất bại!");
                    }
                }
            } else if ("create-notification".equals(action)) {
                String title = request.getParameter("title");
                String type = request.getParameter("type");
                String message = request.getParameter("message");
                String recipientInput = request.getParameter("userIds");

                List<User> recipients = new ArrayList<>();
                if (recipientInput == null || recipientInput.trim().isEmpty()) {
                    // Send to all readers
                    recipients = userDAO.searchUsers(null, "READER", 1);
                } else {
                    String[] tokens = recipientInput.split(",");
                    for (String token : tokens) {
                        token = token.trim();
                        if (token.isEmpty()) continue;
                        try {
                            // Try parsing as ID first
                            int id = Integer.parseInt(token);
                            User u = userDAO.getUserById(id);
                            if (u != null) recipients.add(u);
                        } catch (NumberFormatException e) {
                            // Try searching by username
                            User u = userDAO.getUserByUsername(token);
                            if (u != null) recipients.add(u);
                        }
                    }
                }

                if (recipients.isEmpty()) {
                    session.setAttribute("errorMsg", "Không tìm thấy người nhận hợp lệ theo danh sách cung cấp!");
                } else {
                    int successCount = 0;
                    boolean isSystemWide = (recipientInput == null || recipientInput.trim().isEmpty());
                    for (User u : recipients) {
                        String emailTo = isSystemWide ? null : u.getEmail();
                        boolean ok = notificationService.createAndSendNotification(
                                u.getId(), title, message, type, null, null, emailTo);
                        if (ok) successCount++;
                    }
                    session.setAttribute("successMsg", "Đã gửi thông báo thành công tới " + successCount + " người nhận!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + prefix + "/notification/manage");
    }
}
