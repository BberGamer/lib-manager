/**
 * Mô hình miền của kệ sách, chứa metadata kệ và số liệu sử dụng được tính từ BookCopy.
 */
package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Đại diện một kệ vật lý; mã kệ liên kết với {@code BookCopy.shelf}. */
public class Shelf {
    private int id;
    private String code;
    private String name;
    private String area;
    private int floorNumber;
    private int capacity;
    private String description;
    private String status;
    private int bookCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BookCopy> bookCopies = new ArrayList<>();

    /** Khởi tạo JavaBean rỗng cho tầng điều phối và JSP. */
    public Shelf() { }
    /** Khởi tạo dữ liệu biểu mẫu. */
    public Shelf(int id, String code, String name, String area, int floorNumber,
            int capacity, String description, String status) {
        this.id = id; this.code = code; this.name = name; this.area = area;
        this.floorNumber = floorNumber; this.capacity = capacity;
        this.description = description; this.status = status;
    }
    /** @return mã định danh nội bộ */ public int getId() { return id; }
    /** @param id mã định danh nội bộ */ public void setId(int id) { this.id = id; }
    /** @return mã kệ duy nhất */ public String getCode() { return code; }
    /** @param code mã kệ */ public void setCode(String code) { this.code = code; }
    /** @return tên hiển thị */ public String getName() { return name; }
    /** @param name tên hiển thị */ public void setName(String name) { this.name = name; }
    /** @return khu vực */ public String getArea() { return area; }
    /** @param area khu vực */ public void setArea(String area) { this.area = area; }
    /** @return số tầng */ public int getFloorNumber() { return floorNumber; }
    /** @param floorNumber số tầng dương */ public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }
    /** @return sức chứa tối đa */ public int getCapacity() { return capacity; }
    /** @param capacity sức chứa dương */ public void setCapacity(int capacity) { this.capacity = capacity; }
    /** @return mô tả tùy chọn */ public String getDescription() { return description; }
    /** @param description mô tả tùy chọn */ public void setDescription(String description) { this.description = description; }
    /** @return ACTIVE hoặc INACTIVE */ public String getStatus() { return status; }
    /** @param status trạng thái kệ */ public void setStatus(String status) { this.status = status; }
    /** @return số bản sao đang gắn với kệ */ public int getBookCount() { return bookCount; }
    /** @param bookCount số bản sao được DAO tính */ public void setBookCount(int bookCount) { this.bookCount = bookCount; }
    /** @return số vị trí còn trống, không âm */ public int getAvailableSlots() { return Math.max(0, capacity - bookCount); }
    /** @return thời điểm tạo */ public LocalDateTime getCreatedAt() { return createdAt; }
    /** @param createdAt thời điểm tạo */ public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    /** @return thời điểm cập nhật */ public LocalDateTime getUpdatedAt() { return updatedAt; }
    /** @param updatedAt thời điểm cập nhật */ public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    /** @return danh sách bản sao trên kệ */ public List<BookCopy> getBookCopies() { return bookCopies; }
    /** @param bookCopies danh sách bản sao trên kệ */ public void setBookCopies(List<BookCopy> bookCopies) { this.bookCopies = bookCopies; }
}
