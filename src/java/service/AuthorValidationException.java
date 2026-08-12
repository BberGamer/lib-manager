/**
 * Ngoại lệ chứa lỗi validation theo trường của biểu mẫu tác giả.
 */
package service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Truyền các lỗi người dùng có thể sửa từ service về controller và JSP.
 */
public class AuthorValidationException extends Exception {

    private final Map<String, String> validationErrors;

    /**
     * Khởi tạo ngoại lệ với bản sao bất biến của lỗi.
     * @param validationErrors lỗi theo tên trường hoặc thao tác
     */
    public AuthorValidationException(Map<String, String> validationErrors) {
        super("Dữ liệu tác giả không hợp lệ");
        this.validationErrors = Collections.unmodifiableMap(new LinkedHashMap<>(validationErrors));
    }

    /** @return bản đồ lỗi không thể chỉnh sửa */
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
