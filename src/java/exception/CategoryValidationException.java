/**
 * Ngoại lệ validation mang lỗi theo từng trường của biểu mẫu thể loại.
 */
package exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Biểu diễn dữ liệu thể loại không hợp lệ để controller trả lại đúng biểu mẫu.
 */
public class CategoryValidationException extends Exception {

    private final Map<String, String> validationErrors;

    /**
     * Lưu bản sao bất biến của các lỗi validation.
     *
     * @param validationErrors lỗi được lập chỉ mục theo tên trường
     */
    public CategoryValidationException(Map<String, String> validationErrors) {
        super("Dữ liệu thể loại không hợp lệ");
        this.validationErrors = Collections.unmodifiableMap(new LinkedHashMap<>(validationErrors));
    }

    /**
     * Trả về lỗi để controller chuyển cho JSP.
     *
     * @return bản đồ lỗi bất biến
     */
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
