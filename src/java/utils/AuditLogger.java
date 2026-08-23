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

    /**
     * Ghi nhận Admin sử dụng lại điều lệ đã lưu trữ.
     * @param performedBy tài khoản thực hiện
     * @param policyId mã định danh điều lệ
     * @param title tiêu đề điều lệ tại thời điểm thao tác
     */
    public static void logPolicyReuse(String performedBy, int policyId, String title) {
        log("POLICY_REUSE", performedBy, 0,
                "Mã điều lệ #" + policyId + " | Sử dụng lại điều lệ: " + title);
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
}
