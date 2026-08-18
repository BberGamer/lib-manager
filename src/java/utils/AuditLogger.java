package utils;

import utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * AuditLogger — ghi lại tất cả hành động override/ngoại lệ theo §4.3 và §5 của spec.
 *
 * Các hành động cần log theo spec:
 *   - Override vượt ngưỡng mượn (§1.2, §4.3)
 *   - Tạo phạt Damage/Lost (§2.3, §4.3)
 *   - Miễn giảm phạt (waive fine)
 *   - Khóa/mở tài khoản (§4.3)
 *   - Xác nhận phiếu đặt trước
 *
 * Ghi vào bảng DB `audit_logs`.
 * Script tạo bảng: alter_add_audit_logs.sql
 */
public class AuditLogger {

    private AuditLogger() {}

    /**
     * Ghi một audit log entry.
     *
     * @param action        Tên hành động (VD: "OVERRIDE_BORROW_LIMIT", "WAIVE_FINE")
     * @param performedBy   Username người thực hiện
     * @param targetUserId  User ID bị ảnh hưởng (0 nếu không liên quan đến user cụ thể)
     * @param detail        Mô tả chi tiết (VD: "borrowRecordId=5, reason=override by librarian")
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

    // -------------------------------------------------------------------------
    // Convenience methods cho từng loại hành động
    // -------------------------------------------------------------------------

    /** Override cảnh báo vượt ngưỡng số lượng mượn */
    public static void logOverrideBorrowLimit(String performedBy, int targetUserId, int currentCount, int maxLimit) {
        log("OVERRIDE_BORROW_LIMIT", performedBy, targetUserId,
                "Đang mượn: " + currentCount + "/" + maxLimit + " cuốn — Thủ thư " + performedBy + " đã duyệt vượt hạn mức");
    }

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

    /** Khóa tài khoản */
    public static void logLockAccount(String performedBy, int targetUserId, String reason) {
        log("LOCK_ACCOUNT", performedBy, targetUserId,
                "Đã khóa tài khoản | Lý do: " + (reason != null && !reason.isEmpty() ? reason : "Khóa bởi Quản trị viên"));
    }

    /** Mở khóa tài khoản */
    public static void logUnlockAccount(String performedBy, int targetUserId) {
        log("UNLOCK_ACCOUNT", performedBy, targetUserId,
                "Đã mở khóa tài khoản hoạt động trở lại");
    }

    /** Xác nhận phiếu đặt trước */
    public static void logConfirmReservation(String performedBy, int targetUserId, int reservationId) {
        log("CONFIRM_RESERVATION", performedBy, targetUserId,
                "Mã đặt trước #" + reservationId + " | Đã duyệt giữ sách tại quầy cho độc giả");
    }
}
