package service;

import dao.BorrowRecordDAO;
import dao.FineDAO;
import dao.NotificationDAO;
import dao.SystemConfigDAO;
import model.BorrowRecord;
import model.Fine;
import model.Notification;
import utils.AuditLogger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service xử lý quét tự động hàng loạt (Batch Job / Daily Scheduler)
 * - Quản lý Bật/Tắt tự động gửi email và Batch Job
 * - Tự động đồng bộ tiền phạt theo ngày quá hạn
 * - Tự động xử lý hết hạn giữ sách (expire pending pickups)
 * - Kích hoạt đặt trước đến ngày nhận và báo trễ khi sách chưa được hoàn trả
 * - Tự động gửi thông báo và email nhắc nhở sắp đến hạn trả
 * - Tự động gửi thông báo và email cảnh báo quá hạn
 */
public class AutoReminderService {

    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
    private final FineDAO fineDAO = new FineDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();
    private final NotificationService notificationService = new NotificationService();
    private final FineService fineService = new FineService();
    private final ReservationService reservationService = new ReservationService();

    public static class BatchResult {
        public int nearDueSent = 0;
        public int overdueSent = 0;
        public int expiredPickups = 0;
        public int readyReservations = 0;
        public int delayedReservations = 0;
        public boolean emailSent = true;
        public boolean success = true;
        public String message = "";
        public LocalDateTime executedAt = LocalDateTime.now();
    }

    public boolean isAutoJobEnabled() {
        return systemConfigDAO.isAutoJobEnabled();
    }

    public boolean isAutoEmailEnabled() {
        return systemConfigDAO.isAutoEmailEnabled();
    }

    public void updateAutomationSettings(boolean autoJob, boolean autoEmail, String performedBy) {
        systemConfigDAO.setAutoJobEnabled(autoJob);
        systemConfigDAO.setAutoEmailEnabled(autoEmail);
        String detail = "Cập nhật cấu hình tự động: Lập lịch quét = " 
                + (autoJob ? "BẬT" : "TẮT") + " | Tự động gửi email = " + (autoEmail ? "BẬT" : "TẮT");
        AuditLogger.log("UPDATE_AUTOMATION_SETTING", performedBy != null ? performedBy : "ADMIN", 0, detail);
    }

    /**
     * Chạy quy trình quét tự động toàn bộ hệ thống
     * @param triggeredBy "SYSTEM_SCHEDULER" hoặc username của Thủ thư kích hoạt thủ công
     */
    public BatchResult runBatchReminder(String triggeredBy) {
        BatchResult result = new BatchResult();
        boolean isScheduled = "SYSTEM_SCHEDULER".equalsIgnoreCase(triggeredBy);

        // Nếu là hệ thống chạy định kỳ nhưng tính năng đang bị TẮT
        if (isScheduled && !isAutoJobEnabled()) {
            System.out.println("[AutoReminderService] Scheduler định kỳ đang ở trạng thái TẮT. Bỏ qua lượt quét này.");
            result.success = false;
            result.message = "Batch job định kỳ đang TẮT theo cấu hình của Quản trị viên.";
            return result;
        }

        boolean sendEmail = isAutoEmailEnabled();
        result.emailSent = sendEmail;

        try {
            System.out.println("[AutoReminderService] Bắt đầu chạy Batch Job bởi: " + triggeredBy + " (Gửi Email: " + sendEmail + ")");

            // 1. Đồng bộ trạng thái quá hạn trước khi xét slot đặt trước và gửi thông báo.
            borrowRecordDAO.markOverdueBorrows();
            fineService.synchronizeAllOverdueFines();

            // 2. Xử lý các yêu cầu giữ sách (Pickup Request) đã hết hạn 24h
            try {
                result.expiredPickups = borrowRecordDAO.expirePendingRequests();
            } catch (Exception e) {
                System.err.println("[AutoReminderService] Lỗi khi xử lý expirePendingRequests: " + e.getMessage());
            }

            // 3. Giữ các bản đang rảnh cho yêu cầu đến lịch và báo yêu cầu bị trễ do chưa trả sách.
            result.readyReservations = reservationService.activateDueReservations(sendEmail);
            result.delayedReservations = reservationService.notifyDelayedReservations(sendEmail);

            // 4. Quét và gửi thông báo sách sắp đến hạn (trong vòng 3 ngày)
            List<BorrowRecord> nearDueLoans = borrowRecordDAO.getNearDueLoans(3);
            if (nearDueLoans != null) {
                for (BorrowRecord loan : nearDueLoans) {
                    if (loan.getUser() != null && loan.getBook() != null) {
                        String title = "Nhắc nhở: Sách mượn sắp đến hạn trả – FPT Library";
                        String message = "Xin chào " + loan.getUser().getFullName() + ",\n\n"
                                + "Cuốn sách '" + loan.getBook().getTitle() + "' bạn mượn vào ngày " + loan.getBorrowDate()
                                + " sắp hết hạn trả vào ngày " + loan.getDueDate() + ".\n"
                                + "Vui lòng hoàn trả sách hoặc tiến hành gia hạn trên hệ thống đúng thời hạn để tránh phát sinh phạt.";

                        String emailTo = sendEmail ? loan.getUser().getEmail() : null;
                        boolean sent = notificationService.createAndSendNotification(
                                loan.getUserId(), title, message, "DUE_REMINDER",
                                loan.getId(), "borrow_record", emailTo);
                        if (sent) {
                            result.nearDueSent++;
                        }
                    }
                }
            }

            // 5. Quét và gửi thông báo cảnh báo sách quá hạn
            List<BorrowRecord> overdueLoans = borrowRecordDAO.getOverdueLoans();
            if (overdueLoans != null) {
                for (BorrowRecord loan : overdueLoans) {
                    if (loan.getUser() != null && loan.getBook() != null) {
                        String title = "Cảnh báo: Sách mượn ĐÃ QUÁ HẠN TRẢ – FPT Library";
                        String message = "Xin chào " + loan.getUser().getFullName() + ",\n\n"
                                + "Cuốn sách '" + loan.getBook().getTitle() + "' bạn mượn vào ngày " + loan.getBorrowDate()
                                + " đã quá hạn hoàn trả (Hạn trả là ngày: " + loan.getDueDate() + ").\n"
                                + "Vui lòng trả sách về thư viện sớm nhất có thể để hạn chế mức phạt phát sinh thêm.";

                        String emailTo = sendEmail ? loan.getUser().getEmail() : null;
                        boolean sent = notificationService.createAndSendNotification(
                                loan.getUserId(), title, message, "OVERDUE",
                                loan.getId(), "borrow_record", emailTo);
                        if (sent) {
                            result.overdueSent++;
                        }
                    }
                }
            }

            // 6. Ghi Audit Log cho hệ thống
            String logDetail = "Quét tự động hoàn tất (" + (sendEmail ? "Có gửi Email" : "Chỉ tạo thông báo web") + "): Gửi " 
                    + result.nearDueSent + " nhắc hạn, "
                    + result.overdueSent + " cảnh báo quá hạn, giải phóng " 
                    + result.expiredPickups + " yêu cầu hết hạn giữ, kích hoạt "
                    + result.readyReservations + " đặt trước, báo trễ "
                    + result.delayedReservations + " đặt trước";
            AuditLogger.log("AUTO_BATCH_REMINDR", triggeredBy != null ? triggeredBy : "SYSTEM", 0, logDetail);

            result.success = true;
            result.message = logDetail;
            System.out.println("[AutoReminderService] " + logDetail);

        } catch (Exception e) {
            e.printStackTrace();
            result.success = false;
            result.message = "Lỗi khi chạy quét tự động: " + e.getMessage();
        }
        return result;
    }
}
