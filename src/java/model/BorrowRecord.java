package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class BorrowRecord {
    private int id;
    private int userId;
    private int bookId;
    private Integer copyId; // nullable copy_id
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private int renewalCount;
    private String status; // BORROWING, RETURNED, OVERDUE, LOST
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private User user;
    private BookCopy bookCopy;
    private Book book;
    private Set<Fine> fines = new HashSet<>();

    public BorrowRecord() {}

    public BorrowRecord(int userId, int bookId, Integer copyId, LocalDate borrowDate, LocalDate dueDate, String status) {
        this.userId = userId;
        this.bookId = bookId;
        this.copyId = copyId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
        this.renewalCount = 0;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public Integer getCopyId() {
        return copyId;
    }

    public void setCopyId(Integer copyId) {
        this.copyId = copyId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public int getRenewalCount() {
        return renewalCount;
    }

    public void setRenewalCount(int renewalCount) {
        this.renewalCount = renewalCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Set<Fine> getFines() {
        return fines;
    }

    public void setFines(Set<Fine> fines) {
        this.fines = fines;
    }

    @Override
    public String toString() {
        return "BorrowRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", copyId=" + copyId +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", renewalCount=" + renewalCount +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
