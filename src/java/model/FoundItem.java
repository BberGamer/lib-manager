/**
 * Mô hình biểu diễn một đồ để quên được nhân viên thư viện tiếp nhận.
 * Lớp thuộc tầng model, độc lập với HTTP và ánh xạ bảng found_items.
 */
package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Chứa dữ liệu hiện tại của đồ để quên để phục vụ tiếp nhận, tra cứu và bàn giao.
 */
public class FoundItem {

    private int id;
    private String itemName;
    private String description;
    private LocalDate foundDate;
    private String imagePath;
    private FoundItemStatus status;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Khởi tạo đối tượng rỗng cho form và mapper JDBC.
     */
    public FoundItem() {
        this.status = FoundItemStatus.AVAILABLE;
    }

    /**
     * Khởi tạo dữ liệu nhập từ form tiếp nhận đồ để quên.
     *
     * @param itemName tên ngắn gọn của đồ vật
     * @param description mô tả bổ sung, có thể để trống
     * @param foundDate ngày phát hiện đồ vật
     */
    public FoundItem(String itemName, String description, LocalDate foundDate) {
        this();
        this.itemName = itemName;
        this.description = description;
        this.foundDate = foundDate;
    }

    /** @return mã định danh của đồ vật */
    public int getId() { return id; }

    /** @param id mã định danh lấy từ cơ sở dữ liệu */
    public void setId(int id) { this.id = id; }

    /** @return tên hiển thị của đồ vật */
    public String getItemName() { return itemName; }

    /** @param itemName tên đã được chuẩn hóa từ form */
    public void setItemName(String itemName) { this.itemName = itemName; }

    /** @return mô tả bổ sung, có thể là null */
    public String getDescription() { return description; }

    /** @param description mô tả bổ sung của đồ vật */
    public void setDescription(String description) { this.description = description; }

    /** @return ngày tìm thấy đồ vật */
    public LocalDate getFoundDate() { return foundDate; }

    /** @param foundDate ngày tìm thấy hợp lệ */
    public void setFoundDate(LocalDate foundDate) { this.foundDate = foundDate; }

    /** @return đường dẫn ảnh đã được hệ thống lưu, có thể là null */
    public String getImagePath() { return imagePath; }

    /** @param imagePath đường dẫn ảnh do tầng tải tệp tạo ra */
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    /** @return trạng thái nghiệp vụ hiện tại */
    public FoundItemStatus getStatus() { return status; }

    /** @param status trạng thái đã được kiểm soát bởi service */
    public void setStatus(FoundItemStatus status) { this.status = status; }

    /** @return mã người tiếp nhận đồ vật */
    public int getCreatedBy() { return createdBy; }

    /** @param createdBy mã tài khoản nhân viên tiếp nhận */
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    /** @return thời điểm tạo bản ghi */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @param createdAt thời điểm do cơ sở dữ liệu cung cấp */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** @return thời điểm cập nhật gần nhất */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /** @param updatedAt thời điểm do cơ sở dữ liệu cung cấp */
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
