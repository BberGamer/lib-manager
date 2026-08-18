/**
 * Quản lý toàn bộ logic nghiệp vụ cho tính năng Sự kiện (Event Management).
 * Thuộc tầng Service (Business Logic).
 *
 * Chịu trách nhiệm:
 * - Kiểm tra hợp lệ dữ liệu (Validate) nghiêm ngặt trước khi ghi DB (sử dụng str.trim().isEmpty()).
 * - Lọc trạng thái hiển thị động (UPCOMING, ONGOING, ENDED, CANCELLED) bằng Java.
 * - Sắp xếp danh sách bằng Comparator trong Java (title, start_time, status).
 * - Cắt trang phân trang bằng subList() với PAGE_SIZE = 10 (giống UserListService).
 */
package service;

import dao.EventDAO;
import model.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lớp dịch vụ quản lý nghiệp vụ cho Sự kiện.
 */
public class EventService {

    public static final int PAGE_SIZE = 10;
    private final EventDAO eventDAO = new EventDAO();

    /**
     * DTO kết quả tìm kiếm và phân trang sự kiện.
     */
    public static class SearchResult {
        public List<Event> events;
        public int totalRecords;
        public int totalPages;
        public int currentPage;
    }

    /**
     * Tìm kiếm, lọc theo trạng thái hiển thị, sắp xếp và phân trang danh sách sự kiện.
     *
     * @param q            Từ khóa tìm kiếm theo tiêu đề (SQL LIKE)
     * @param statusFilter Trạng thái hiển thị cần lọc ("UPCOMING", "ONGOING", "ENDED", "CANCELLED" hoặc null/rỗng)
     * @param sortBy       Trường cần sắp xếp ("title", "start_time", "status")
     * @param sortOrder    Thứ tự sắp xếp ("ASC" hoặc "DESC")
     * @param page         Trang hiện tại (1-indexed)
     * @return Đối tượng SearchResult chứa danh sách trang hiện tại và thông tin phân trang
     */
    public SearchResult search(String q, String statusFilter, String sortBy, String sortOrder, int page) {
        List<Event> allEvents;
        try {
            allEvents = eventDAO.findActiveEvents(q);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tải danh sách sự kiện: " + e.getMessage(), e);
        }

        // 1. Lọc theo trạng thái hiển thị động (UPCOMING / ONGOING / ENDED / CANCELLED) bằng Java
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            String filterUpper = statusFilter.trim().toUpperCase();
            allEvents = allEvents.stream()
                    .filter(e -> filterUpper.equals(e.getDisplayStatus()))
                    .collect(Collectors.toList());
        } else {
            allEvents = new ArrayList<>(allEvents);
        }

        // 2. Sắp xếp bằng Comparator trong Java (không sắp xếp bằng câu SQL ORDER BY)
        Comparator<Event> comparator;
        String field = (sortBy != null) ? sortBy.trim().toLowerCase() : "start_time";

        switch (field) {
            case "title":
                comparator = Comparator.comparing(e -> e.getTitle() != null ? e.getTitle().toLowerCase() : "");
                break;
            case "status":
                comparator = Comparator.comparing(e -> e.getDisplayStatus() != null ? e.getDisplayStatus() : "");
                break;
            case "start_time":
            default:
                comparator = Comparator.comparing(Event::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
        }

        if ("DESC".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        allEvents.sort(comparator);

        // 3. Phân trang bằng subList() trong Java với PAGE_SIZE = 10
        SearchResult result = new SearchResult();
        result.totalRecords = allEvents.size();
        result.totalPages = Math.max(1, (int) Math.ceil((double) result.totalRecords / PAGE_SIZE));

        int currentPage = Math.min(Math.max(page, 1), result.totalPages);
        int fromIndex = (currentPage - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, result.totalRecords);

        result.events = (fromIndex < result.totalRecords) ? allEvents.subList(fromIndex, toIndex) : new ArrayList<>();
        result.currentPage = currentPage;

        return result;
    }

    /**
     * Lấy thông tin một sự kiện theo ID.
     *
     * @param id Mã sự kiện
     * @return Đối tượng Event
     */
    public Event getEventById(int id) {
        try {
            Event event = eventDAO.findById(id);
            if (event == null) {
                throw new IllegalArgumentException("Không tìm thấy sự kiện có mã #" + id);
            }
            return event;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tải thông tin sự kiện: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo mới một sự kiện. Thực hiện validate dữ liệu bắt buộc ở tầng Service.
     *
     * @param event Đối tượng sự kiện chứa thông tin nhập vào
     */
    public void createEvent(Event event) {
        validateEvent(event, true);
        try {
            boolean success = eventDAO.insert(event);
            if (!success) {
                throw new IllegalStateException("Thêm mới sự kiện thất bại. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi hệ thống khi thêm sự kiện: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin sự kiện. Thực hiện validate dữ liệu bắt buộc ở tầng Service.
     *
     * @param event Đối tượng sự kiện chứa thông tin cập nhật
     */
    public void updateEvent(Event event) {
        if (event.getId() <= 0) {
            throw new IllegalArgumentException("Mã sự kiện không hợp lệ.");
        }
        // Kiểm tra sự kiện có tồn tại không
        getEventById(event.getId());

        validateEvent(event, false);

        try {
            boolean success = eventDAO.update(event);
            if (!success) {
                throw new IllegalStateException("Cập nhật sự kiện thất bại. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi hệ thống khi cập nhật sự kiện: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa mềm sự kiện (cập nhật is_deleted = 1).
     *
     * @param id        Mã sự kiện cần xóa
     * @param updatedBy Tài khoản thực hiện xóa
     */
    public void deleteEvent(int id, String updatedBy) {
        if (id <= 0) {
            throw new IllegalArgumentException("Mã sự kiện không hợp lệ.");
        }
        getEventById(id);

        try {
            boolean success = eventDAO.softDelete(id, updatedBy);
            if (!success) {
                throw new IllegalStateException("Xóa sự kiện thất bại. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi hệ thống khi xóa sự kiện: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra hợp lệ (Validate) dữ liệu sự kiện theo đúng quy tắc nghiệp vụ.
     * Toàn bộ điều kiện kiểm tra rỗng phải dùng str.trim().isEmpty().
     *
     * @param event    Đối tượng sự kiện cần kiểm tra
     * @param isCreate true nếu là thao tác tạo mới, false nếu là thao tác cập nhật
     */
    private void validateEvent(Event event, boolean isCreate) {
        if (event == null) {
            throw new IllegalArgumentException("Dữ liệu sự kiện không được để trống.");
        }

        // 1. Validate title: Bắt buộc, trim() không rỗng, độ dài 3–200 ký tự
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề sự kiện không được để trống.");
        }
        String trimmedTitle = event.getTitle().trim();
        if (trimmedTitle.length() < 3 || trimmedTitle.length() > 200) {
            throw new IllegalArgumentException("Tiêu đề sự kiện phải có độ dài từ 3 đến 200 ký tự.");
        }
        event.setTitle(trimmedTitle);

        // 2. Validate description: Không bắt buộc; nếu có, trim() sau đó <= 1000 ký tự
        if (event.getDescription() != null && !event.getDescription().trim().isEmpty()) {
            String trimmedDesc = event.getDescription().trim();
            if (trimmedDesc.length() > 1000) {
                throw new IllegalArgumentException("Mô tả sự kiện không được vượt quá 1000 ký tự.");
            }
            event.setDescription(trimmedDesc);
        } else {
            event.setDescription(null);
        }

        // 3. Validate start_time: Bắt buộc
        if (event.getStartTime() == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu sự kiện không được để trống.");
        }
        if (isCreate) {
            // Khi tạo mới: phải là thời điểm tương lai (sau thời điểm hiện tại)
            if (!event.getStartTime().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Thời gian bắt đầu sự kiện tạo mới phải ở tương lai.");
            }
        }

        // 4. Validate end_time: Bắt buộc, phải sau start_time
        if (event.getEndTime() == null) {
            throw new IllegalArgumentException("Thời gian kết thúc sự kiện không được để trống.");
        }
        if (!event.getEndTime().isAfter(event.getStartTime())) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu sự kiện.");
        }

        // 5. Validate status: Chỉ nhận ACTIVE hoặc CANCELLED
        if (event.getStatus() != null && !event.getStatus().trim().isEmpty()) {
            String statusUpper = event.getStatus().trim().toUpperCase();
            if (!"ACTIVE".equals(statusUpper) && !"CANCELLED".equals(statusUpper)) {
                throw new IllegalArgumentException("Trạng thái sự kiện không hợp lệ (chỉ nhận ACTIVE hoặc CANCELLED).");
            }
            event.setStatus(statusUpper);
        } else {
            event.setStatus("ACTIVE");
        }
    }
}
