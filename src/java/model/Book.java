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
 *   - available == 0 AND quantity > 0 → "Đặt trước"
 *   - quantity == 0  → "Hết sách"
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
     * Trả về nhãn trạng thái sách.
     * @return "Còn sách" | "Đặt trước" | "Hết sách"
     */
    public String getStatusLabel() {
        if (available > 0)                   return "Còn sách";
        if (quantity > 0 && available == 0)  return "Đặt trước";
        return "Hết sách";
    }

    /**
     * CSS class tương ứng trạng thái.
     */
    public String getStatusClass() {
        if (available > 0)                   return "status-available";
        if (quantity > 0 && available == 0)  return "status-reserve";
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
