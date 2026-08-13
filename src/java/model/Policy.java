/**
 * Mô hình điều lệ thuộc tầng domain, ánh xạ một phiên bản trong bảng policies.
 */
package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Chứa nội dung, vòng đời, khoảng hiệu lực và thông tin kiểm toán của một điều lệ. */
public class Policy {

    private int id;
    private String policyCode;
    private int version = 1;
    private String title;
    private String content;
    private PolicyCategory category;
    private PolicyPublicationStatus publicationStatus = PolicyPublicationStatus.DRAFT;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean deleted;
    private String createdBy;
    private String updatedBy;
    private String publishedBy;
    private String archivedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime archivedAt;
    private String effectiveStatus;

    /** Khởi tạo mô hình rỗng cho biểu mẫu và mapper JDBC. */
    public Policy() {
    }

    /** @return mã định danh của phiên bản điều lệ */
    public int getId() { return id; }
    /** @param id mã định danh cần gán */
    public void setId(int id) { this.id = id; }
    /** @return mã nghiệp vụ dùng chung giữa các phiên bản */
    public String getPolicyCode() { return policyCode; }
    /** @param policyCode mã nghiệp vụ cần gán */
    public void setPolicyCode(String policyCode) { this.policyCode = policyCode; }
    /** @return số phiên bản dương */
    public int getVersion() { return version; }
    /** @param version số phiên bản cần gán */
    public void setVersion(int version) { this.version = version; }
    /** @return tiêu đề điều lệ */
    public String getTitle() { return title; }
    /** @param title tiêu đề cần gán */
    public void setTitle(String title) { this.title = title; }
    /** @return nội dung văn bản thuần */
    public String getContent() { return content; }
    /** @param content nội dung cần gán */
    public void setContent(String content) { this.content = content; }
    /** @return danh mục nghiệp vụ */
    public PolicyCategory getCategory() { return category; }
    /** @param category danh mục cần gán */
    public void setCategory(PolicyCategory category) { this.category = category; }
    /** @return trạng thái xuất bản lưu trong database */
    public PolicyPublicationStatus getPublicationStatus() { return publicationStatus; }
    /** @param publicationStatus trạng thái xuất bản cần gán */
    public void setPublicationStatus(PolicyPublicationStatus publicationStatus) {
        this.publicationStatus = publicationStatus;
    }
    /** @return ngày bắt đầu hiệu lực */
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    /** @param effectiveFrom ngày bắt đầu cần gán */
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    /** @return ngày kết thúc hiệu lực, có thể null */
    public LocalDate getEffectiveTo() { return effectiveTo; }
    /** @param effectiveTo ngày kết thúc cần gán */
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    /** @return true nếu bản ghi đã xóa mềm */
    public boolean isDeleted() { return deleted; }
    /** @param deleted trạng thái xóa mềm cần gán */
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    /** @return tài khoản tạo */
    public String getCreatedBy() { return createdBy; }
    /** @param createdBy tài khoản tạo cần gán */
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    /** @return tài khoản cập nhật gần nhất */
    public String getUpdatedBy() { return updatedBy; }
    /** @param updatedBy tài khoản cập nhật cần gán */
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    /** @return tài khoản xuất bản */
    public String getPublishedBy() { return publishedBy; }
    /** @param publishedBy tài khoản xuất bản cần gán */
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }
    /** @return tài khoản lưu trữ */
    public String getArchivedBy() { return archivedBy; }
    /** @param archivedBy tài khoản lưu trữ cần gán */
    public void setArchivedBy(String archivedBy) { this.archivedBy = archivedBy; }
    /** @return thời điểm tạo */
    public LocalDateTime getCreatedAt() { return createdAt; }
    /** @param createdAt thời điểm tạo cần gán */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    /** @return thời điểm cập nhật gần nhất */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    /** @param updatedAt thời điểm cập nhật cần gán */
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    /** @return thời điểm xuất bản */
    public LocalDateTime getPublishedAt() { return publishedAt; }
    /** @param publishedAt thời điểm xuất bản cần gán */
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    /** @return thời điểm lưu trữ */
    public LocalDateTime getArchivedAt() { return archivedAt; }
    /** @param archivedAt thời điểm lưu trữ cần gán */
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
    /** @return nhãn hiệu lực đã được service chuẩn bị cho view */
    public String getEffectiveStatus() { return effectiveStatus; }
    /** @param effectiveStatus nhãn hiệu lực cần gán */
    public void setEffectiveStatus(String effectiveStatus) { this.effectiveStatus = effectiveStatus; }
}
