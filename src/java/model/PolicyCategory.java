/**
 * Danh mục điều lệ được giới hạn tại tầng mô hình để dữ liệu và bộ lọc nhất quán.
 */
package model;

/** Phân nhóm điều lệ theo các nghiệp vụ chính của thư viện. */
public enum PolicyCategory {
    GENERAL("Quy định chung"),
    BORROWING("Mượn sách"),
    RETURNING("Trả sách"),
    FINES("Phí và xử phạt"),
    MEMBERSHIP("Thẻ và tài khoản");

    private final String label;

    /** @param label nhãn tiếng Việt dùng trên giao diện */
    PolicyCategory(String label) {
        this.label = label;
    }

    /** @return nhãn tiếng Việt của danh mục */
    public String getLabel() {
        return label;
    }
}
