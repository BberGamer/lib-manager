/** Ngoại lệ tầng service của nghiệp vụ quản lý kệ. */
package service;

/** Bao bọc lỗi lưu trữ để controller không phụ thuộc JDBC. */
public class ShelfException extends Exception {
    /**
     * @param message ngữ cảnh nghiệp vụ @param cause nguyên nhân gốc
     */
    public ShelfException(String message, Throwable cause) {
        super(message, cause);
    }
}
