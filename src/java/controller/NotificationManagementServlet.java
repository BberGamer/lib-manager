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
        String tab = request.getParameter("tab");
        if (tab == null || (!tab.equals("reminders") && !tab.equals("compose"))) {
            tab = "compose";
        }
        String sub = request.getParameter("sub");
        if (sub == null || (!sub.equals("due") && !sub.equals("overdue") && !sub.equals("fines"))) {
            sub = "due";
        }

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
        request.setAttribute("activeTab", tab);
        request.setAttribute("activeSubTab", sub);
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

            service.AutoReminderService autoReminderService = new service.AutoReminderService();
            request.setAttribute("autoJobEnabled", autoReminderService.isAutoJobEnabled());
            request.setAttribute("autoEmailEnabled", autoReminderService.isAutoEmailEnabled());
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
                        utils.AuditLogger.logDueReminder(loggedUser.getUsername(), loan.getUserId(), loan.getId(), loan.getBook() != null ? loan.getBook().getTitle() : "");
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
                        utils.AuditLogger.logOverdueWarning(loggedUser.getUsername(), loan.getUserId(), loan.getId(), loan.getBook() != null ? loan.getBook().getTitle() : "");
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
                        utils.AuditLogger.logFineReminder(loggedUser.getUsername(), fine.getUserId(), fine.getId(), fine.getAmount() != null ? fine.getAmount().toString() : "0");
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
                    if (isSystemWide || recipients.size() > 1) {
                        utils.AuditLogger.logNotificationBroadcast(loggedUser.getUsername(), successCount, title, type);
                    } else if (recipients.size() == 1) {
                        utils.AuditLogger.logNotificationSend(loggedUser.getUsername(), recipients.get(0).getId(), title, type);
                    }
                    session.setAttribute("successMsg", "Đã gửi thông báo thành công tới " + successCount + " người nhận!");
                }
            } else if ("run-auto-job".equals(action)) {
                service.AutoReminderService autoReminderService = new service.AutoReminderService();
                service.AutoReminderService.BatchResult res = autoReminderService.runBatchReminder(loggedUser.getUsername());
                if (res.success) {
                    session.setAttribute("successMsg", "⚡ Đã hoàn thành quét tự động: Đã gửi " 
                            + res.nearDueSent + " thông báo sắp đến hạn, " 
                            + res.overdueSent + " cảnh báo quá hạn, giải phóng " 
                            + res.expiredPickups + " yêu cầu hết hạn nhận sách!");
                } else {
                    session.setAttribute("errorMsg", "Lỗi khi chạy quét tự động: " + res.message);
                }
            } else if ("toggle-automation".equals(action)) {
                boolean enableJob = "true".equalsIgnoreCase(request.getParameter("enableJob"));
                boolean enableEmail = "true".equalsIgnoreCase(request.getParameter("enableEmail"));
                service.AutoReminderService autoReminderService = new service.AutoReminderService();
                autoReminderService.updateAutomationSettings(enableJob, enableEmail, loggedUser.getUsername());
                // Không hiển thị thông báo - trạng thái bật/tắt đã phản ánh qua giao diện nút gạt
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }

        String redirectUrl = request.getContextPath() + prefix + "/notification/manage";
        if ("send-due".equals(action)) {
            redirectUrl += "?tab=reminders&sub=due";
        } else if ("send-overdue".equals(action)) {
            redirectUrl += "?tab=reminders&sub=overdue";
        } else if ("send-fine".equals(action)) {
            redirectUrl += "?tab=reminders&sub=fines";
        } else if ("run-auto-job".equals(action) || "toggle-automation".equals(action)) {
            String currentSub = request.getParameter("sub");
            redirectUrl += "?tab=reminders" + (currentSub != null ? "&sub=" + currentSub : "");
        } else if ("create-notification".equals(action)) {
            redirectUrl += "?tab=compose";
        }

        response.sendRedirect(redirectUrl);
    }
}
