/**
 * Ngoại lệ nghiệp vụ của module đồ để quên.
 * Lớp thuộc tầng service, dùng để che chi tiết lỗi JDBC khỏi controller và giao diện.
 */
package exception;

/**
 * Báo hiệu lỗi không thể hoàn thành thao tác đồ để quên do tầng lưu trữ hoặc quy tắc nghiệp vụ.
 */
public class FoundItemException extends Exception {

    /**
     * Khởi tạo lỗi cùng thông điệp an toàn cho biên HTTP.
     *
     * @param message mô tả lỗi ở mức ứng dụng
     * @param cause nguyên nhân gốc cần được giữ lại để log
     */
    public FoundItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
