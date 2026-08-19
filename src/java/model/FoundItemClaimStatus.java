/**
 * Biểu diễn trạng thái của yêu cầu nhận lại đồ để quên trong tầng model.
 * Enum được dùng để không nhận trạng thái tùy ý từ bên ngoài hệ thống.
 */
package model;

/**
 * Xác định vòng đời cơ bản của một yêu cầu nhận lại đồ để quên.
 */
public enum FoundItemClaimStatus {
    PENDING,
    APPROVED,
    READER_CONFIRMED,
    COMPLETED,
    REJECTED;

    /**
     * Trả về mã ổn định dùng cho giao diện và dữ liệu lưu trữ.
     *
     * @return mã trạng thái
     */
    public String getCode() {
        return name();
    }
}
