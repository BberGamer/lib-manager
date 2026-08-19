/**
 * Mô hình lưu yêu cầu nhận lại đồ để quên của Reader trong tầng model.
 * Lớp độc lập với HTTP và ánh xạ dữ liệu của bảng found_item_claims.
 */
package model;

import java.time.LocalDateTime;

/**
 * Chứa dữ liệu xác minh do Reader gửi để Thủ thư xem xét yêu cầu nhận đồ.
 */
public class FoundItemClaim {

    private int id;
    private int itemId;
    private int userId;
    private String claimNote;
    private FoundItemClaimStatus status;
    private Integer handledBy;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
    private String readerName;
    private String readerUsername;
    private String itemName;

    /**
     * Khởi tạo yêu cầu mới ở trạng thái chờ xử lý.
     */
    public FoundItemClaim() {
        this.status = FoundItemClaimStatus.PENDING;
    }

    /** @return mã yêu cầu */
    public int getId() { return id; }

    /** @param id mã yêu cầu */
    public void setId(int id) { this.id = id; }

    /** @return mã đồ để quên được yêu cầu nhận */
    public int getItemId() { return itemId; }

    /** @param itemId mã đồ để quên */
    public void setItemId(int itemId) { this.itemId = itemId; }

    /** @return mã Reader gửi yêu cầu */
    public int getUserId() { return userId; }

    /** @param userId mã Reader */
    public void setUserId(int userId) { this.userId = userId; }

    /** @return ghi chú xác minh của Reader */
    public String getClaimNote() { return claimNote; }

    /** @param claimNote ghi chú xác minh */
    public void setClaimNote(String claimNote) { this.claimNote = claimNote; }

    /** @return trạng thái yêu cầu */
    public FoundItemClaimStatus getStatus() { return status; }

    /** @param status trạng thái do nghiệp vụ kiểm soát */
    public void setStatus(FoundItemClaimStatus status) { this.status = status; }

    /** @return mã Thủ thư xử lý, có thể null */
    public Integer getHandledBy() { return handledBy; }

    /** @param handledBy mã Thủ thư xử lý */
    public void setHandledBy(Integer handledBy) { this.handledBy = handledBy; }

    /** @return thời điểm Reader gửi yêu cầu */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @param createdAt thời điểm tạo do cơ sở dữ liệu cung cấp */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** @return thời điểm Thủ thư xử lý, có thể null */
    public LocalDateTime getHandledAt() { return handledAt; }

    /** @param handledAt thời điểm xử lý do cơ sở dữ liệu cung cấp */
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }

    /** @return họ tên Reader dùng để Thủ thư xác minh */
    public String getReaderName() { return readerName; }

    /** @param readerName họ tên Reader từ dữ liệu tài khoản */
    public void setReaderName(String readerName) { this.readerName = readerName; }

    /** @return tên đăng nhập Reader dùng để đối chiếu */
    public String getReaderUsername() { return readerUsername; }

    /** @param readerUsername tên đăng nhập Reader từ dữ liệu tài khoản */
    public void setReaderUsername(String readerUsername) { this.readerUsername = readerUsername; }

    /** @return tên đồ vật gắn với yêu cầu, dùng cho trang Reader */
    public String getItemName() { return itemName; }

    /** @param itemName tên đồ vật từ dữ liệu tiếp nhận */
    public void setItemName(String itemName) { this.itemName = itemName; }
}
