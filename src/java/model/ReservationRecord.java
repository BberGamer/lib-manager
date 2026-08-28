package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationRecord {
    private int id;
    private int bookId;
    private int userId;
    private LocalDateTime reserveDate;
    private LocalDate requestedPickupDate;
    private LocalDate expectedPickupDate;
    private LocalDate expectedEndDate;
    private LocalDateTime expiryDate;
    private String status; // WAITING, READY_FOR_PICKUP, COMPLETED, CANCELLED, EXPIRED
    private int queuePosition;
    private LocalDateTime notifiedAt;
    private LocalDateTime delayNotifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private User user;
    private Book book;

    public ReservationRecord() {}

    public ReservationRecord(int userId, int bookId, String status) {
        this.userId = userId;
        this.bookId = bookId;
        this.status = status;
        this.reserveDate = LocalDateTime.now();
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

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(LocalDateTime reserveDate) {
        this.reserveDate = reserveDate;
    }

    /** @return ngày độc giả mong muốn đến nhận sách */
    public LocalDate getRequestedPickupDate() {
        return requestedPickupDate;
    }

    /** @param requestedPickupDate ngày nhận do độc giả lựa chọn */
    public void setRequestedPickupDate(LocalDate requestedPickupDate) {
        this.requestedPickupDate = requestedPickupDate;
    }

    /** @return ngày hệ thống dự kiến có slot sách cho yêu cầu */
    public LocalDate getExpectedPickupDate() {
        return expectedPickupDate;
    }

    /** @param expectedPickupDate ngày dự kiến được phân bổ từ lịch trả sách */
    public void setExpectedPickupDate(LocalDate expectedPickupDate) {
        this.expectedPickupDate = expectedPickupDate;
    }

    /** @return ngày kết thúc dự kiến, không thuộc khoảng đặt trước */
    public LocalDate getExpectedEndDate() {
        return expectedEndDate;
    }

    /** @param expectedEndDate ngày kết thúc do service chuẩn bị cho giao diện */
    public void setExpectedEndDate(LocalDate expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /** @return vị trí hiện tại trong hàng chờ, bằng 0 nếu không còn chờ */
    public int getQueuePosition() { return queuePosition; }
    /** @param queuePosition vị trí được DAO tính động */
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    /** @return thời điểm đã thông báo lịch nhận bị trễ, hoặc {@code null} nếu chưa báo */
    public LocalDateTime getDelayNotifiedAt() {
        return delayNotifiedAt;
    }

    /** @param delayNotifiedAt thời điểm hệ thống ghi nhận đã báo trễ */
    public void setDelayNotifiedAt(LocalDateTime delayNotifiedAt) {
        this.delayNotifiedAt = delayNotifiedAt;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "ReservationRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", status='" + status + '\'' +
                ", reserveDate=" + reserveDate +
                ", requestedPickupDate=" + requestedPickupDate +
                ", expectedPickupDate=" + expectedPickupDate +
                ", expiryDate=" + expiryDate +
                ", notifiedAt=" + notifiedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
