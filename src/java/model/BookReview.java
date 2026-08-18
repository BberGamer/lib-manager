package model;

import java.util.Date;

public class BookReview {
    private int id;
    private int bookId;
    private int userId;
    private int rating;
    private String comment;
    private Date createdAt;
    private Date updatedAt;

    // Các field phụ để hiển thị (không có trong DB)
    private String userFullName;
    private String userStudentId;
    private String bookTitle;

    /** Mã lượt mượn liên kết với đánh giá này (mỗi lượt mượn được đánh giá tối đa 1 lần) */
    private Integer borrowId;
    /** Ngày mượn thực tế để hiển thị nhãn tin cậy */
    private java.time.LocalDate borrowDate;

    public BookReview() {}

    public BookReview(int id, int bookId, int userId, int rating, String comment, Date createdAt, Date updatedAt) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { 
        // Validation: rating phải trong khoảng 1-5
        if (rating >= 1 && rating <= 5) {
            this.rating = rating;
        } else {
            throw new IllegalArgumentException("Rating phải nằm trong khoảng 1-5");
        }
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { 
        // Validation: comment không được quá 1000 ký tự
        if (comment != null && comment.length() > 1000) {
            throw new IllegalArgumentException("Comment không được vượt quá 1000 ký tự");
        }
        this.comment = comment;
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getUserStudentId() { return userStudentId; }
    public void setUserStudentId(String userStudentId) { this.userStudentId = userStudentId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    /**
     * Lấy mã lượt mượn sách liên kết.
     * @return mã lượt mượn (nullable)
     */
    public Integer getBorrowId() { return borrowId; }

    /**
     * Gán mã lượt mượn sách liên kết.
     * @param borrowId mã lượt mượn
     */
    public void setBorrowId(Integer borrowId) { this.borrowId = borrowId; }

    /**
     * Lấy ngày mượn thực tế để hiển thị nhãn tin cậy.
     * @return ngày mượn thực tế (nullable)
     */
    public java.time.LocalDate getBorrowDate() { return borrowDate; }

    /**
     * Gán ngày mượn thực tế.
     * @param borrowDate ngày mượn
     */
    public void setBorrowDate(java.time.LocalDate borrowDate) { this.borrowDate = borrowDate; }
}
