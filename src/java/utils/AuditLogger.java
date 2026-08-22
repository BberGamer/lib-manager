package utils;

import utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * AuditLogger — ghi lại tất cả hành động nghiệp vụ quan trọng của Admin và Librarian.
 *
 * Phạm vi ghi log:
 *   - Override vượt ngưỡng mượn (§1.2, §4.3)
 *   - Tạo phạt Damage/Lost (§2.3, §4.3)
 *   - Miễn giảm phạt / Thu tiền mặt / Thanh toán online
 *   - Khóa/mở tài khoản, Tạo/cập nhật tài khoản (§4.3)
 *   - Giao sách / Nhận trả sách (mượn trả cốt lõi)
 *   - Xác nhận / Hủy đặt trước
 *   - Xuất bản / Lưu trữ / Xóa / Sửa điều lệ
 *   - Thêm / Xóa sách, Nhập hàng loạt sách từ file
 *   - Đồ để quên (tiếp nhận, duyệt yêu cầu, bàn giao)
 *
 * Ghi vào bảng DB `audit_logs`.
 */
public class AuditLogger {

    private AuditLogger() {}

    /**
     * Ghi một audit log entry.
     *
     * @param action        Tên hành động (VD: "OVERRIDE_BORROW_LIMIT", "WAIVE_FINE")
     * @param performedBy   Username người thực hiện
     * @param targetUserId  User ID bị ảnh hưởng (0 nếu không liên quan đến user cụ thể)
     * @param detail        Mô tả chi tiết
     */
    public static void log(String action, String performedBy, int targetUserId, String detail) {
        String sql = "INSERT INTO audit_logs (action, performed_by, target_user_id, detail, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBContext.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setString(2, performedBy != null ? performedBy : "system");
            ps.setInt(3, targetUserId);
            ps.setString(4, detail != null ? detail : "");
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (Exception e) {
            // Log failure không được làm gián đoạn business flow
            System.err.println("[AuditLogger] Failed to write audit log: action=" + action + ", error=" + e.getMessage());
        }
    }

    // =========================================================================
    // Tài khoản người dùng
    // =========================================================================

    /** Khóa tài khoản */
    public static void logLockAccount(String performedBy, int targetUserId, String reason) {
        log("LOCK_ACCOUNT", performedBy, targetUserId,
                "Đã khóa tài khoản | Lý do: " + (reason != null && !reason.isEmpty() ? reason : "Khóa bởi Quản trị viên"));
    }

    /** Mở khóa tài khoản */
    public static void logUnlockAccount(String performedBy, int targetUserId) {
        log("UNLOCK_ACCOUNT", performedBy, targetUserId, "Đã mở khóa tài khoản hoạt động trở lại");
    }

    /** Admin tạo tài khoản mới */
    public static void logCreateUser(String performedBy, int newUserId, String username, String role) {
        log("CREATE_USER", performedBy, newUserId,
                "Tạo tài khoản mới: " + username + " | Vai trò: " + role);
    }

    /** Admin cập nhật thông tin tài khoản (bao gồm đổi vai trò) */
    public static void logUpdateUser(String performedBy, int targetUserId, String detail) {
        log("UPDATE_USER", performedBy, targetUserId, detail != null ? detail : "Cập nhật thông tin tài khoản");
    }

    // =========================================================================
    // Mượn trả sách (nghiệp vụ cốt lõi)
    // =========================================================================

    /** Thủ thư xác nhận giao sách vật lý cho độc giả */
    public static void logBorrowConfirmPickup(String performedBy, int targetUserId, int borrowId, String barcode) {
        log("BORROW_CONFIRM_PICKUP", performedBy, targetUserId,
                "Mã mượn #" + borrowId + " | Đã giao sách vật lý, mã vạch bản sao: " + barcode);
    }

    /** Thủ thư xác nhận nhận trả sách thành công */
    public static void logBorrowConfirmReturn(String performedBy, int targetUserId, int borrowId, String condition) {
        String conditionText = "GOOD".equalsIgnoreCase(condition) ? "Tốt"
                : "DAMAGE".equalsIgnoreCase(condition) ? "Hư hỏng"
                : "LOST".equalsIgnoreCase(condition) ? "Mất sách"
                : condition;
        log("BORROW_CONFIRM_RETURN", performedBy, targetUserId,
                "Mã mượn #" + borrowId + " | Nhận trả sách thành công | Tình trạng: " + conditionText);
    }

    /** Override cảnh báo vượt ngưỡng số lượng mượn */
    public static void logOverrideBorrowLimit(String performedBy, int targetUserId, int currentCount, int maxLimit) {
        log("OVERRIDE_BORROW_LIMIT", performedBy, targetUserId,
                "Đang mượn: " + currentCount + "/" + maxLimit + " cuốn — Thủ thư " + performedBy + " đã duyệt vượt hạn mức");
    }

    // =========================================================================
    // Đặt trước sách
    // =========================================================================

    /** Thủ thư xác nhận sách đã về, chuyển trạng thái sang READY */
    public static void logConfirmReservation(String performedBy, int targetUserId, int reservationId) {
        log("CONFIRM_RESERVATION", performedBy, targetUserId,
                "Mã đặt trước #" + reservationId + " | Đã duyệt giữ sách tại quầy cho độc giả");
    }

    /** Thủ thư/Admin hủy phiếu đặt trước thay cho độc giả */
    public static void logCancelReservationByStaff(String performedBy, int targetUserId, int reservationId) {
        log("CANCEL_RESERVATION_BY_STAFF", performedBy, targetUserId,
                "Mã đặt trước #" + reservationId + " | Nhân viên hủy phiếu đặt trước của độc giả");
    }

    // =========================================================================
    // Khoản phạt
    // =========================================================================

    /** Tạo phạt hư hỏng */
    public static void logDamageFine(String performedBy, int targetUserId, int borrowRecordId, String amount) {
        log("APPLY_DAMAGE_FINE", performedBy, targetUserId,
                "Mã mượn #" + borrowRecordId + " | Phạt tiền hư hỏng sách: " + amount + " ₫");
    }

    /** Tạo phạt mất sách */
    public static void logLostFine(String performedBy, int targetUserId, int borrowRecordId, String amount) {
        log("APPLY_LOST_FINE", performedBy, targetUserId,
                "Mã mượn #" + borrowRecordId + " | Phạt tiền mất sách: " + amount + " ₫");
    }

    /** Admin miễn giảm phạt */
    public static void logWaiveFine(String performedBy, int targetUserId, int fineId, String note) {
        log("WAIVE_FINE", performedBy, targetUserId,
                "Mã phạt #" + fineId + " | Lý do miễn giảm: " + (note != null && !note.isEmpty() ? note : "Đã duyệt miễn phạt"));
    }

    /** Thủ thư thu tiền phạt bằng tiền mặt */
    public static void logFinePaidCash(String performedBy, int targetUserId, int fineId, String amount) {
        log("FINE_PAID_CASH", performedBy, targetUserId,
                "Mã phạt #" + fineId + " | Thu tiền phạt bằng tiền mặt: " + (amount != null ? amount : "0") + " ₫");
    }

    /** Độc giả thanh toán phạt thành công qua VNPay */
    public static void logFinePaidOnline(int targetUserId, int fineId, String transactionNo) {
        log("FINE_PAID_ONLINE", "VNPay", targetUserId,
                "Mã phạt #" + fineId + " | Thanh toán thành công qua VNPay | Mã GD: " + transactionNo);
    }

    // =========================================================================
    // Quản lý sách
    // =========================================================================

    /** Admin thêm đầu sách mới */
    public static void logBookCreate(String performedBy, int bookId, String title) {
        log("BOOK_CREATE", performedBy, 0,
                "Mã sách #" + bookId + " | Thêm đầu sách mới: " + title);
    }

    /** Admin xóa đầu sách */
    public static void logBookDelete(String performedBy, int bookId, String title) {
        log("BOOK_DELETE", performedBy, 0,
                "Mã sách #" + bookId + " | Xóa đầu sách: " + title);
    }

    /** Admin/Librarian nhập hàng loạt sách từ file CSV */
    public static void logBookBulkImport(String performedBy, int successCount, int failureCount, int totalRows) {
        log("BOOK_BULK_IMPORT", performedBy, 0,
                "Nhập file CSV: " + successCount + "/" + totalRows + " đầu sách thành công"
                        + (failureCount > 0 ? ", " + failureCount + " dòng lỗi" : ""));
    }

    // =========================================================================
    // Quản lý điều lệ
    // =========================================================================

    /** Admin tạo bản nháp điều lệ mới */
    public static void logPolicyCreate(String performedBy, int policyId, String title) {
        log("POLICY_CREATE", performedBy, 0,
                "Mã điều lệ #" + policyId + " | Tạo bản nháp mới: " + title);
    }

    /** Admin xuất bản điều lệ chính thức */
    public static void logPolicyPublish(String performedBy, int policyId, String title) {
        log("POLICY_PUBLISH", performedBy, 0,
                "Mã điều lệ #" + policyId + " | Xuất bản điều lệ có hiệu lực: " + title);
    }

    /** Admin lưu trữ điều lệ */
    public static void logPolicyArchive(String performedBy, int policyId, String title) {
        log("POLICY_ARCHIVE", performedBy, 0,
                "Mã điều lệ #" + policyId + " | Lưu trữ điều lệ: " + title);
    }

    /** Admin xóa bản nháp điều lệ */
    public static void logPolicyDelete(String performedBy, int policyId, String title) {
        log("POLICY_DELETE", performedBy, 0,
                "Mã điều lệ #" + policyId + " | Xóa bản nháp điều lệ: " + title);
    }

    /** Admin tạo phiên bản sửa đổi từ điều lệ đang hiệu lực */
    public static void logPolicyRevise(String performedBy, int sourceId, int newDraftId, String title) {
        log("POLICY_REVISE", performedBy, 0,
                "Điều lệ nguồn #" + sourceId + " → Bản nháp #" + newDraftId + " | Tạo phiên bản sửa đổi: " + title);
    }

    /** Admin cập nhật thông tin đầu sách */
    public static void logBookUpdate(String performedBy, int bookId, String title) {
        log("BOOK_UPDATE", performedBy, 0,
                "Mã sách #" + bookId + " | Cập nhật thông tin đầu sách: " + title);
    }

    /** Thêm bản sao vật lý mới */
    public static void logBookCopyAdd(String performedBy, int bookId, String barcode, String shelf) {
        log("BOOK_COPY_ADD", performedBy, 0,
                "Mã sách #" + bookId + " | Thêm bản sao: " + barcode + (shelf != null && !shelf.isEmpty() ? " (Kệ: " + shelf + ")" : ""));
    }

    /** Cập nhật bản sao vật lý */
    public static void logBookCopyUpdate(String performedBy, int bookId, String barcode, String condition) {
        log("BOOK_COPY_UPDATE", performedBy, 0,
                "Mã sách #" + bookId + " | Cập nhật bản sao: " + barcode + " | Tình trạng: " + condition);
    }

    /** Xóa bản sao vật lý */
    public static void logBookCopyDelete(String performedBy, int bookId, String barcode) {
        log("BOOK_COPY_DELETE", performedBy, 0,
                "Mã sách #" + bookId + " | Xóa bản sao vật lý: " + barcode);
    }

    // =========================================================================
    // Quản lý vị trí kệ sách
    // =========================================================================

    /** Thêm kệ sách mới */
    public static void logShelfCreate(String performedBy, int shelfId, String code, String name) {
        log("SHELF_CREATE", performedBy, 0,
                "Mã kệ #" + shelfId + " | Thêm kệ sách: " + code + " (" + name + ")");
    }

    /** Cập nhật kệ sách */
    public static void logShelfUpdate(String performedBy, int shelfId, String code, String name) {
        log("SHELF_UPDATE", performedBy, 0,
                "Mã kệ #" + shelfId + " | Cập nhật kệ sách: " + code + " (" + name + ")");
    }

    /** Xóa kệ sách */
    public static void logShelfDelete(String performedBy, int shelfId, String code) {
        log("SHELF_DELETE", performedBy, 0,
                "Mã kệ #" + shelfId + " | Xóa kệ sách: " + code);
    }

    // =========================================================================
    // Quản lý tác giả
    // =========================================================================

    /** Thêm tác giả mới */
    public static void logAuthorCreate(String performedBy, int authorId, String name) {
        log("AUTHOR_CREATE", performedBy, 0,
                "Mã tác giả #" + authorId + " | Thêm tác giả: " + name);
    }

    /** Cập nhật tác giả */
    public static void logAuthorUpdate(String performedBy, int authorId, String name) {
        log("AUTHOR_UPDATE", performedBy, 0,
                "Mã tác giả #" + authorId + " | Cập nhật tác giả: " + name);
    }

    /** Xóa tác giả */
    public static void logAuthorDelete(String performedBy, int authorId, String name) {
        log("AUTHOR_DELETE", performedBy, 0,
                "Mã tác giả #" + authorId + " | Xóa tác giả: " + name);
    }

    // =========================================================================
    // Quản lý danh mục / Thể loại
    // =========================================================================

    /** Thêm thể loại mới */
    public static void logCategoryCreate(String performedBy, int categoryId, String name) {
        log("CATEGORY_CREATE", performedBy, 0,
                "Mã danh mục #" + categoryId + " | Thêm danh mục: " + name);
    }

    /** Cập nhật thể loại */
    public static void logCategoryUpdate(String performedBy, int categoryId, String name) {
        log("CATEGORY_UPDATE", performedBy, 0,
                "Mã danh mục #" + categoryId + " | Cập nhật danh mục: " + name);
    }

    /** Xóa thể loại */
    public static void logCategoryDelete(String performedBy, int categoryId, String name) {
        log("CATEGORY_DELETE", performedBy, 0,
                "Mã danh mục #" + categoryId + " | Xóa danh mục: " + name);
    }

    // =========================================================================
    // Quản lý sự kiện
    // =========================================================================

    /** Thêm sự kiện mới */
    public static void logEventCreate(String performedBy, int eventId, String title) {
        log("EVENT_CREATE", performedBy, 0,
                "Mã sự kiện #" + eventId + " | Thêm sự kiện: " + title);
    }

    /** Cập nhật sự kiện */
    public static void logEventUpdate(String performedBy, int eventId, String title) {
        log("EVENT_UPDATE", performedBy, 0,
                "Mã sự kiện #" + eventId + " | Cập nhật sự kiện: " + title);
    }

    /** Xóa sự kiện */
    public static void logEventDelete(String performedBy, int eventId, String title) {
        log("EVENT_DELETE", performedBy, 0,
                "Mã sự kiện #" + eventId + " | Xóa sự kiện: " + title);
    }

    // =========================================================================
    // Đồ để quên
    // =========================================================================

    /** Tiếp nhận đồ để quên mới */
    public static void logCreateFoundItem(String performedBy, int itemId, String itemName) {
        log("FOUND_ITEM_CREATE", performedBy, 0,
                "Mã đồ LF-" + itemId + " | Tiếp nhận đồ để quên: " + itemName);
    }

    /** Duyệt hoặc từ chối yêu cầu nhận lại đồ */
    public static void logVerifyFoundItemClaim(String performedBy, int targetUserId, int itemId, boolean approved) {
        log(approved ? "FOUND_ITEM_VERIFY_APPROVE" : "FOUND_ITEM_VERIFY_REJECT", performedBy, targetUserId,
                "Mã đồ LF-" + itemId + " | " + (approved ? "Chấp nhận yêu cầu nhận lại đồ của độc giả" : "Từ chối yêu cầu nhận lại đồ"));
    }

    /** Hoàn tất bàn giao đồ cho độc giả */
    public static void logHandoverFoundItem(String performedBy, int targetUserId, int itemId) {
        log("FOUND_ITEM_HANDOVER_COMPLETE", performedBy, targetUserId,
                "Mã đồ LF-" + itemId + " | Đã xác nhận hoàn tất bàn giao đồ để quên cho độc giả");
    }

    // =========================================================================
    // Quản lý thông báo & Nhắc nhở
    // =========================================================================

    /** Gửi thông báo broadcast/tới nhiều độc giả */
    public static void logNotificationBroadcast(String performedBy, int recipientCount, String title, String type) {
        log("NOTIFICATION_BROADCAST", performedBy, 0,
                "Gửi thông báo [" + type + "] tới " + recipientCount + " độc giả | Tiêu đề: " + title);
    }

    /** Gửi thông báo đích danh tới 1 độc giả */
    public static void logNotificationSend(String performedBy, int targetUserId, String title, String type) {
        log("NOTIFICATION_SEND", performedBy, targetUserId,
                "Gửi thông báo [" + type + "] đích danh | Tiêu đề: " + title);
    }

    /** Gửi nhắc nhở sách sắp đến hạn trả */
    public static void logDueReminder(String performedBy, int targetUserId, int borrowId, String bookTitle) {
        log("SEND_DUE_REMINDER", performedBy, targetUserId,
                "Mã mượn #" + borrowId + " | Gửi nhắc nhở sách sắp đến hạn: " + bookTitle);
    }

    /** Gửi cảnh báo sách đã quá hạn */
    public static void logOverdueWarning(String performedBy, int targetUserId, int borrowId, String bookTitle) {
        log("SEND_OVERDUE_WARNING", performedBy, targetUserId,
                "Mã mượn #" + borrowId + " | Gửi cảnh báo sách quá hạn: " + bookTitle);
    }

    /** Gửi nhắc nhở nợ phí phạt */
    public static void logFineReminder(String performedBy, int targetUserId, int fineId, String amount) {
        log("SEND_FINE_REMINDER", performedBy, targetUserId,
                "Mã phạt #" + fineId + " | Gửi nhắc nợ phạt: " + amount + " ₫");
    }
}
