/**
 * Model ánh xạ bảng events trong CSDL.
 * Đại diện cho thông tin sự kiện do thư viện tổ chức.
 *
 * Ghi chú: Trạng thái hiển thị (UPCOMING, ONGOING, ENDED, CANCELLED) được tính
 * toán động ở tầng mã nguồn qua phương thức getDisplayStatus(), không lưu trong CSDL.
 */
package model;

import java.time.LocalDateTime;

/**
 * Lớp đại diện cho thực thể Sự kiện (Event).
 * Lưu trữ các thuộc tính cơ bản của sự kiện và tính toán trạng thái hiển thị động.
 */
public class Event {

    private int id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // "ACTIVE" hoặc "CANCELLED"
    private int isDeleted; // 0 = active, 1 = deleted
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Khởi tạo đối tượng Event mặc định.
     */
    public Event() {
    }

    /**
     * Khởi tạo đối tượng Event với đầy đủ thuộc tính.
     *
     * @param id          Mã định danh sự kiện
     * @param title       Tiêu đề sự kiện
     * @param description Mô tả sự kiện
     * @param startTime   Thời gian bắt đầu
     * @param endTime     Thời gian kết thúc
     * @param status      Trạng thái gốc trong CSDL (ACTIVE/CANCELLED)
     * @param isDeleted   Cờ đánh dấu xóa mềm (0/1)
     * @param createdBy   Tài khoản người tạo
     * @param updatedBy   Tài khoản cập nhật gần nhất
     * @param createdAt   Thời gian tạo
     * @param updatedAt   Thời gian cập nhật
     */
    public Event(int id, String title, String description, LocalDateTime startTime,
                 LocalDateTime endTime, String status, int isDeleted,
                 String createdBy, String updatedBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.isDeleted = isDeleted;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Tính toán trạng thái hiển thị động của sự kiện.
     * Trạng thái dựa vào thời điểm hiện tại và thời gian bắt đầu/kết thúc.
     *
     * @return Chuỗi đại diện cho trạng thái hiển thị ("CANCELLED", "UPCOMING", "ENDED", "ONGOING")
     */
    public String getDisplayStatus() {
        if ("CANCELLED".equals(status)) {
            return "CANCELLED";
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return "UPCOMING";
        }
        if (now.isAfter(endTime)) {
            return "ENDED";
        }
        return "ONGOING";
    }

    // ---- Getters và Setters ----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
