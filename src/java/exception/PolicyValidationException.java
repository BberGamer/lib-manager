/**
 * Ngoại lệ validation của module Policy, mang lỗi theo từng trường hoặc hành động.
 */
package exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Cung cấp lỗi nghiệp vụ có cấu trúc để controller hiển thị an toàn trên giao diện. */
public class PolicyValidationException extends Exception {

    private final Map<String, String> validationErrors;

    /**
     * Sao chép lỗi validation sang bản đồ chỉ đọc để caller không thể thay đổi kết quả.
     * @param validationErrors lỗi được ánh xạ theo tên trường hoặc hành động
     */
    public PolicyValidationException(Map<String, String> validationErrors) {
        super("Dữ liệu điều lệ không hợp lệ");
        this.validationErrors = Collections.unmodifiableMap(new LinkedHashMap<>(validationErrors));
    }

    /**
     * Trả về các lỗi validation để controller đưa vào request scope.
     * @return bản đồ lỗi chỉ đọc
     */
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
