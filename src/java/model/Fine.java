package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Fine {
    private int id;
    private int borrowRecordId;
    private int userId;
    private BigDecimal amount;
    private int overdueDays;
    private String bookCondition;
    private String fineType;
    private String reason;
    private String status; // Trạng thái hợp lệ: UNPAID, PAID, WAIVED.
    private String paymentMethod; // CASH, ONLINE
    private String paymentNote;
    private LocalDate paidDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private User user;
    private BorrowRecord borrowRecord;

    public Fine() {}

    public Fine(int borrowRecordId, int userId, BigDecimal amount, int overdueDays, String reason, String status) {
        this.borrowRecordId = borrowRecordId;
        this.userId = userId;
        this.amount = amount;
        this.overdueDays = overdueDays;
        this.reason = reason;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBorrowRecordId() {
        return borrowRecordId;
    }

    public void setBorrowRecordId(int borrowRecordId) {
        this.borrowRecordId = borrowRecordId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(int overdueDays) {
        this.overdueDays = overdueDays;
    }

    /**
     * Lấy tình trạng cuốn sách làm phát sinh khoản phạt.
     *
     * @return mã tình trạng {@code DAMAGED}, {@code LOST} hoặc {@code null} với dữ liệu cũ
     */
    public String getBookCondition() {
        return bookCondition;
    }

    /**
     * Gán tình trạng cuốn sách làm căn cứ tính tiền phạt.
     *
     * @param bookCondition mã tình trạng cuốn sách đã được service kiểm tra
     */
    public void setBookCondition(String bookCondition) {
        this.bookCondition = bookCondition;
    }

    /**
     * Cung cấp nhãn tiếng Việt để các trang quản lý và độc giả hiển thị thống nhất.
     *
     * @return nhãn tình trạng cuốn sách
     */
    public String getBookConditionLabel() {
        if ("WORN".equals(bookCondition)) {
            return "Hỏng nhẹ (30% giá trị sách)";
        }
        if ("DAMAGED".equals(bookCondition)) {
            return "Hỏng nặng (100% giá trị sách)";
        }
        if ("LOST".equals(bookCondition)) {
            return "Mất sách (100% giá trị sách)";
        }
        return "Không xác định";
    }

    /**
     * Lấy loại khoản phạt để phân biệt quá hạn, tình trạng sách và dữ liệu cũ.
     *
     * @return mã loại khoản phạt
     */
    public String getFineType() {
        return fineType;
    }

    /**
     * Gán loại khoản phạt đọc từ cơ sở dữ liệu.
     *
     * @param fineType mã loại khoản phạt
     */
    public void setFineType(String fineType) {
        this.fineType = fineType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentNote() {
        return paymentNote;
    }

    public void setPaymentNote(String paymentNote) {
        this.paymentNote = paymentNote;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Cung cấp riêng phần ngày tạo để JSP hiển thị mà không gọi hàm chuyển đổi trong EL.
     *
     * @return ngày tạo khoản phạt hoặc {@code null} nếu chưa có thời điểm tạo
     */
    public LocalDate getCreatedDate() {
        return createdAt == null ? null : createdAt.toLocalDate();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BorrowRecord getBorrowRecord() {
        return borrowRecord;
    }

    public void setBorrowRecord(BorrowRecord borrowRecord) {
        this.borrowRecord = borrowRecord;
    }

    @Override
    public String toString() {
        return "Fine{" +
                "id=" + id +
                ", borrowRecordId=" + borrowRecordId +
                ", userId=" + userId +
                ", amount=" + amount +
                ", overdueDays=" + overdueDays +
                ", reason='" + reason + '\'' +
                ", status='" + status + '\'' +
                ", paidDate=" + paidDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
