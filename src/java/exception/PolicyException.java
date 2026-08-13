/**
 * Ngoại lệ ứng dụng của module Policy, che giấu chi tiết persistence khỏi controller.
 */
package exception;

/** Đại diện lỗi lưu trữ hoặc điều phối nghiệp vụ trong quá trình quản lý điều lệ. */
public class PolicyException extends Exception {

    /**
     * Khởi tạo lỗi Policy và giữ nguyên nguyên nhân gốc để phục vụ logging.
     * @param message mô tả lỗi an toàn theo ngữ cảnh nghiệp vụ
     * @param cause nguyên nhân kỹ thuật gốc
     */
    public PolicyException(String message, Throwable cause) {
        super(message, cause);
    }
}
