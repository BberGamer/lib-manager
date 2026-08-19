/**
 * Ngoại lệ chứa lỗi theo từng trường của form đồ để quên.
 * Lớp thuộc tầng service để controller chỉ chuyển lỗi sang JSP.
 */
package exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cung cấp danh sách lỗi validation bất biến để hiển thị cạnh từng trường nhập liệu.
 */
public class FoundItemValidationException extends Exception {

    private final Map<String, String> validationErrors;

    /**
     * Khởi tạo ngoại lệ từ các lỗi đã được service tổng hợp.
     *
     * @param validationErrors lỗi theo tên trường form
     */
    public FoundItemValidationException(Map<String, String> validationErrors) {
        super("Dữ liệu đồ để quên không hợp lệ.");
        this.validationErrors = Collections.unmodifiableMap(new LinkedHashMap<>(validationErrors));
    }

    /**
     * Trả về lỗi theo trường ở chế độ chỉ đọc.
     *
     * @return bản đồ lỗi validation
     */
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
