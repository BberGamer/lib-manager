/**
 * Trạng thái xuất bản cố định của điều lệ trong tầng mô hình.
 */
package model;

/** Đại diện vòng đời bản nháp, bản đã công bố và bản đã lưu trữ. */
public enum PolicyPublicationStatus {
    DRAFT("Bản nháp"),
    PUBLISHED("Đã xuất bản"),
    ARCHIVED("Đã lưu trữ");

    private final String label;

    /** @param label nhãn tiếng Việt dùng trên giao diện */
    PolicyPublicationStatus(String label) {
        this.label = label;
    }

    /** @return nhãn tiếng Việt của trạng thái */
    public String getLabel() {
        return label;
    }
}
