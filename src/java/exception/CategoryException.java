/**
 * Ngoại lệ tầng ứng dụng dùng để che giấu lỗi lưu trữ của chức năng thể loại.
 */
package exception;

/**
 * Biểu diễn lỗi không thể hoàn tất nghiệp vụ thể loại và giữ nguyên nguyên nhân gốc.
 */
public class CategoryException extends Exception {

    /**
     * Tạo ngoại lệ nghiệp vụ với thông điệp nội bộ và nguyên nhân gốc.
     *
     * @param message mô tả lỗi dành cho log máy chủ
     * @param cause lỗi gốc từ tầng thấp hơn
     */
    public CategoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
