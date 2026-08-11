/**
 * Mô hình miền biểu diễn một thể loại sách và dữ liệu kiểm toán tương ứng.
 */
package model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Đại diện cho bản ghi trong bảng {@code categories}, độc lập với tầng HTTP và JDBC.
 */
public class Category {

    private int id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    private String createdBy;
    private String updatedBy;

    private Set<Book> books = new HashSet<>();
    /**
     * Khởi tạo đối tượng rỗng để JSP và tầng điều phối có thể gán dữ liệu biểu mẫu.
     */
    public Category() {
    }

    public Category(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isDeleted = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    /** @param id mã định danh lấy từ cơ sở dữ liệu */
    public void setId(int id) {
        this.id = id;
    }

    /** @return tên thể loại */
    public String getName() {
        return name;
    }

    /** @param name tên thể loại đã được chuẩn hóa */
    public void setName(String name) {
        this.name = name;
    }

    /** @return mô tả thể loại, có thể là {@code null} */
    public String getDescription() {
        return description;
    }

    /** @param description mô tả tùy chọn */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return thời điểm tạo bản ghi */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @param createdAt thời điểm tạo do cơ sở dữ liệu cung cấp */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** @return thời điểm cập nhật gần nhất */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** @param updatedAt thời điểm cập nhật do cơ sở dữ liệu cung cấp */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** @return {@code true} nếu bản ghi đã bị xóa mềm */
    public boolean isDeleted() {
        return isDeleted;
    }

    /** @param isDeleted trạng thái xóa mềm */
    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    /** @return tài khoản tạo bản ghi */
    public String getCreatedBy() {
        return createdBy;
    }

    /** @param createdBy tài khoản thực hiện thao tác tạo */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** @return tài khoản cập nhật gần nhất */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /** @param updatedBy tài khoản thực hiện cập nhật gần nhất */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + isDeleted +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}
