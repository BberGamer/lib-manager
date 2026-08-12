/**
 * Ngoại lệ nghiệp vụ tổng quát của module quản lý tác giả tại tầng service.
 */
package service;

/**
 * Bao bọc lỗi persistence để controller không phụ thuộc vào SQLException.
 */
public class AuthorException extends Exception {

    /**
     * Khởi tạo lỗi với thông điệp và nguyên nhân gốc.
     * @param message mô tả thao tác thất bại
     * @param cause nguyên nhân persistence
     */
    public AuthorException(String message, Throwable cause) {
        super(message, cause);
    }
}
