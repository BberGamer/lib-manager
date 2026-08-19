/**
 * Biểu diễn các trạng thái cơ bản của một đồ để quên trong thư viện.
 * Lớp thuộc tầng model và được FoundItemService dùng để kiểm soát luồng tiếp nhận.
 */
package model;

/**
 * Tập trạng thái đóng của đồ để quên theo schema bảng found_items.
 */
public enum FoundItemStatus {
    AVAILABLE("AVAILABLE", "Có thể nhận", "available"),
    CLAIM_PENDING("CLAIM_PENDING", "Đang chờ xác minh", "claim_pending"),
    RETURNED("RETURNED", "Đã trả", "returned");

    private final String code;
    private final String displayName;
    private final String cssClass;

    /**
     * Khởi tạo trạng thái cùng nhãn hiển thị tiếng Việt.
     *
     * @param code mã lưu trong cơ sở dữ liệu
     * @param displayName nhãn dùng cho giao diện
     * @param cssClass hậu tố CSS an toàn để hiển thị trạng thái
     */
    FoundItemStatus(String code, String displayName, String cssClass) {
        this.code = code;
        this.displayName = displayName;
        this.cssClass = cssClass;
    }

    /**
     * Trả về mã trạng thái dùng để lưu và lọc dữ liệu.
     *
     * @return mã trạng thái
     */
    public String getCode() {
        return code;
    }

    /**
     * Trả về nhãn thân thiện để controller chuẩn bị dữ liệu cho view.
     *
     * @return nhãn tiếng Việt của trạng thái
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Trả về hậu tố CSS để JSP không cần gọi phương thức tùy ý từ EL.
     *
     * @return tên lớp CSS của trạng thái
     */
    public String getCssClass() {
        return cssClass;
    }
}
