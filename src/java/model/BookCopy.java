package model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class BookCopy {

    private int id;
    private int bookId;
    private String barcode;
    private String bookCondition; // GOOD, WORN, DAMAGED, LOST
    private String status; // AVAILABLE, BORROWED, RESERVED, MAINTENANCE, LOST
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;   // soft delete flag
    private String  createdBy;   // tài khoản tạo
    private String  updatedBy;   // tài khoản cập nhật gần nhất
    private String area;
    private String shelf;
    private String slot;
    private Book book;
    private Set<BorrowRecord> borrowRecords = new HashSet<>();

    public BookCopy() {
    }

    public BookCopy(int bookId, String barcode, String bookCondition, String status) {
        this.bookId = bookId;
        this.barcode = barcode;
        this.bookCondition = bookCondition;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getShelf() {
        return shelf;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

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

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBookCondition() {
        return bookCondition;
    }

    public void setBookCondition(String bookCondition) {
        this.bookCondition = bookCondition;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }

    public String getCreatedBy()             { return createdBy; }
    public void setCreatedBy(String v)       { this.createdBy = v; }

    public String getUpdatedBy()             { return updatedBy; }
    public void setUpdatedBy(String v)       { this.updatedBy = v; }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Set<BorrowRecord> getBorrowRecords() {
        return borrowRecords;
    }

    public void setBorrowRecords(Set<BorrowRecord> borrowRecords) {
        this.borrowRecords = borrowRecords;
    }

    @Override
    public String toString() {
        return "BookCopy{"
                + "id=" + id
                + ", bookId=" + bookId
                + ", barcode='" + barcode + '\''
                + ", bookCondition='" + bookCondition + '\''
                + ", status='" + status + '\''
                + ", note='" + note + '\''
                + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt
                + '}';
    }
}
