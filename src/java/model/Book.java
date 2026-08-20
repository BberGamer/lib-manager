package model;

import java.sql.Timestamp;

/**
 * Model ánh xạ bảng books trong DB thực tế.
 *
 * Schema:
 *   id, isbn, title, category (VARCHAR), category_id (FK),
 *   publisher (VARCHAR), publish_year, price, quantity, available,
 *   description, cover_image, subject,
 *   created_at, updated_at, is_deleted, created_by, updated_by
 *
 * Trạng thái sách:
 *   - available > 0  → "Còn sách"
 *   - available == 0 AND reservable → "Đặt trước"
 *   - available == 0 AND NOT reservable → "Chưa thể đặt trước" hoặc "Hết sách"
 */
public class Book {

    private int     id;
    private String  isbn;
    private String  title;
    private String  category;       // tên danh mục (VARCHAR)
    private int     categoryId;     // FK → categories.id
    private String  publisher;      // tên NXB (VARCHAR)
    private Integer publishYear;
    private Integer price;
    private int     quantity;       // tổng số bản
    private int     available;      // số bản còn cho mượn
    private boolean reservable;     // có ít nhất một bản đang mượn đúng hạn để dự kiến ngày trả
    private String  description;
    private String  coverImage;
    private String  subject;        // môn học liên quan
    private int     borrowCount;    // số lượt mượn (dùng cho thống kê xu hướng)

    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean   isDeleted;   // soft delete flag
    private String    createdBy;   // tài khoản tạo
    private String    updatedBy;   // tài khoản cập nhật gần nhất

    public Book() {}

    // ---- Getters & Setters ----
    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public String getIsbn()                 { return isbn; }
    public void setIsbn(String isbn)        { this.isbn = isbn; }

    public String getTitle()                { return title; }
    public void setTitle(String title)      { this.title = title; }

    public String getCategory()             { return category; }
    public void setCategory(String v)       { this.category = v; }

    public int getCategoryId()              { return categoryId; }
    public void setCategoryId(int v)        { this.categoryId = v; }

    public String getPublisher()            { return publisher; }
    public void setPublisher(String v)      { this.publisher = v; }

    public Integer getPublishYear()         { return publishYear; }
    public void setPublishYear(Integer v)   { this.publishYear = v; }

    public Integer getPrice()               { return price; }
    public void setPrice(Integer v)         { this.price = v; }

    public int getQuantity()                { return quantity; }
    public void setQuantity(int v)          { this.quantity = v; }

    public int getAvailable()               { return available; }
    public void setAvailable(int v)         { this.available = v; }

    /** @return {@code true} khi đầu sách có lịch trả đúng hạn để nhận đặt trước */
    public boolean isReservable()           { return reservable; }
    /** @param reservable khả năng nhận yêu cầu đặt trước theo lịch trả hiện tại */
    public void setReservable(boolean reservable) { this.reservable = reservable; }

    public String getDescription()          { return description; }
    public void setDescription(String v)    { this.description = v; }

    public String getCoverImage()           { return coverImage; }
    public void setCoverImage(String v)     { this.coverImage = v; }

    public String getSubject()              { return subject; }
    public void setSubject(String v)        { this.subject = v; }

    /** Trả về số lượt mượn của cuốn sách. */
    public int getBorrowCount()             { return borrowCount; }
    /** Thiết lập số lượt mượn của cuốn sách. */
    public void setBorrowCount(int v)       { this.borrowCount = v; }



    public Timestamp getCreatedAt()         { return createdAt; }
    public void setCreatedAt(Timestamp v)   { this.createdAt = v; }

    public Timestamp getUpdatedAt()         { return updatedAt; }
    public void setUpdatedAt(Timestamp v)   { this.updatedAt = v; }

    public boolean isDeleted()               { return isDeleted; }
    public void setDeleted(boolean v)        { this.isDeleted = v; }

    public String getCreatedBy()             { return createdBy; }
    public void setCreatedBy(String v)       { this.createdBy = v; }

    public String getUpdatedBy()             { return updatedBy; }
    public void setUpdatedBy(String v)       { this.updatedBy = v; }

    /**
     * Trả về nhãn khả dụng được tổng hợp từ số bản có thể cho mượn.
     * @return nhãn còn sách, đặt trước, chưa thể đặt trước hoặc hết sách
     */
    public String getAvailabilityLabel() {
        if (available > 0)                   return "Còn sách";
        if (reservable)                      return "Đặt trước";
        if (quantity > 0)                    return "Chưa thể đặt trước";
        return "Hết sách";
    }

    /**
     * Trả về CSS class tương ứng với mức khả dụng, không đại diện tình trạng vật lý.
     *
     * @return CSS class dùng để trình bày mức khả dụng
     */
    public String getAvailabilityClass() {
        if (available > 0)                   return "status-available";
        if (reservable)                      return "status-reserve";
        return "status-unavailable";
    }

    /**
     * Định dạng giá tiền VNĐ.
     */
    public String getFormattedPrice() {
        if (price == null || price == 0) return "Miễn phí";
        return String.format("%,d đ", price).replace(',', '.');
    }

    @Override
    public String toString() {
        return "Book{id=" + id + ", isbn='" + isbn + "', title='" + title + "'}";
    }
}
