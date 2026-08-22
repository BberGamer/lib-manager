/* Enum miền nghiệp vụ mô tả kết quả gia hạn để service và controller trao đổi rõ nguyên nhân. */
package model;

/**
 * Biểu diễn các kết quả đóng của thao tác gia hạn sách, gồm thành công, bị chặn bởi
 * hàng đặt trước hoặc không còn đủ điều kiện gia hạn.
 */
public enum BorrowRenewalResult {

    /** Lượt mượn đã được cộng thêm thời gian thành công. */
    SUCCESS,

    /** Đầu sách đã có người đặt trước nên phải giữ nguyên hạn trả hiện tại. */
    BLOCKED_BY_RESERVATION,

    /** Lượt mượn quá hạn, vượt số lần gia hạn hoặc không thuộc độc giả thao tác. */
    NOT_ELIGIBLE
}
