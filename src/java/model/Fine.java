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
    private String reason;
    private String status; // UNPAID, PENDING_VERIFY, PAID, WAIVED
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
