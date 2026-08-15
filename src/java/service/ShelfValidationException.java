/** Ngoại lệ chứa lỗi validation theo trường của biểu mẫu kệ. */
package service;

import java.util.Map;

/** Truyền lỗi nghiệp vụ thân thiện từ service tới controller. */
public class ShelfValidationException extends Exception {
    private final Map<String, String> validationErrors;
    /**
     * @param validationErrors lỗi theo tên trường
     */
    public ShelfValidationException(Map<String, String> validationErrors) {
        super("Dữ liệu kệ sách không hợp lệ");
        this.validationErrors = validationErrors;
    }
    /**
     * @return bản đồ lỗi validation
     */
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
