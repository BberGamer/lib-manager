/**
 * Lớp Service xử lý các quy tắc nghiệp vụ liên quan đến Sự kiện (Event).
 * Thuộc tầng Service / Business Logic.
 *
 * Đồng bộ cấu trúc 100% với UserListService để dễ dàng quản lý và bảo trì.
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
 * Lớp xử lý nghiệp vụ cho Sự kiện.
 */
public class EventService {

    private static final int PAGE_SIZE = 10;
    private static final int TITLE_MIN_LEN = 3;
    private static final int TITLE_MAX_LEN = 200;
    private static final int DESC_MAX_LEN = 1000;

    private final EventDAO eventDAO = new EventDAO();

    /**
     * Đối tượng chứa kết quả tìm kiếm và phân trang để Servlet truyền sang JSP.
     */
    public static class SearchResult {

        public List<Event> events;
        public int totalRecords;
        public int totalPages;
        public int currentPage;
    }

    /**
     * Tìm kiếm, lọc theo trạng thái hiển thị, sắp xếp và phân trang danh sách
     * sự kiện. Đồng bộ chuẩn 5 tham số với UserListService.search().
     * @return Đối tượng SearchResult chứa danh sách trang hiện tại và thông tin
     * phân trang
     */
    public SearchResult search(String q, String statusFilter, String sortField, String sortOrder, int page) {
        List<Event> allEvents;
        try {
            // 1. Tải danh sách sự kiện từ DAO 
            allEvents = eventDAO.searchEvents(q, null, sortField, sortOrder);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tải danh sách sự kiện: " + e.getMessage(), e);
        }

//        // Lọc danh sách sự kiện 
//        List<Event> filteredEvents = new ArrayList<>();
//        for (Event ev : allEvents) {
//            if (ev.getId() >= 1 && ev.getId() <= 5) {
//                filteredEvents.add(ev);
//            }
//        }
//        allEvents = filteredEvents;


// List<Event> upcomingEvents = new ArrayList<>();
// for (Event ev : allEvents) {
//     if ("UPCOMING".equals(ev.getDisplayStatus())) {
//         upcomingEvents.add(ev);
//     }
// }
// allEvents = upcomingEvents;

        // 2. Lọc theo trạng thái hiển thị động (UPCOMING / ONGOING / ENDED / CANCELLED) 
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            String filterUpper = statusFilter.trim().toUpperCase();
            List<Event> filteredList = new ArrayList<>();
            for (Event e : allEvents) {
                if (filterUpper.equals(e.getDisplayStatus())) {
                    filteredList.add(e);
                }
            }
            allEvents = filteredList;
        } else {
            allEvents = new ArrayList<>(allEvents);
        }

        // 3. Sắp xếp bằng Comparator Java để đảm bảo chính xác tuyệt đối cả cho Trạng thái động
        Comparator<Event> comparator;
        String field = (sortField != null) ? sortField.trim().toLowerCase() : "start_time";

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

        // 4. Phân trang bằng subList() trong Java với PAGE_SIZE = 10
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
        if (id <= 0) {
            throw new IllegalArgumentException("Mã sự kiện không hợp lệ.");
        }
        try {
            Event event = eventDAO.findById(id);
            if (event == null) {
                throw new IllegalArgumentException("Sự kiện không tồn tại hoặc đã bị xóa.");
            }
            return event;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi khi tải thông tin sự kiện: " + e.getMessage(), e);
        }
    }

    /**
     * Thêm mới một sự kiện vào hệ thống (Service-side validation).
     *
     * @param event Đối tượng sự kiện cần thêm
     */
    public void createEvent(Event event) {
        validateEvent(event);
        try {
            boolean success = eventDAO.insert(event);
            if (!success) {
                throw new IllegalStateException("Thêm sự kiện thất bại.");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi khi lưu sự kiện: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin sự kiện (Service-side validation).
     *
     * @param event Đối tượng sự kiện cần cập nhật
     */
    public void updateEvent(Event event) {
        if (event == null || event.getId() <= 0) {
            throw new IllegalArgumentException("Thông tin sự kiện không hợp lệ.");
        }
        validateEvent(event);
        try {
            boolean success = eventDAO.update(event);
            if (!success) {
                throw new IllegalStateException("Cập nhật sự kiện thất bại.");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi khi cập nhật sự kiện: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa mềm sự kiện (Soft delete).
     *
     * @param id Mã sự kiện
     * @param updatedBy Tài khoản thực hiện xóa
     */
    public void deleteEvent(int id, String updatedBy) {
        if (id <= 0) {
            throw new IllegalArgumentException("Mã sự kiện không hợp lệ.");
        }

        // Kiểm tra sự kiện tồn tại và không được phép xóa sự kiện đang diễn ra
        Event existing = getEventById(id);
        if ("ONGOING".equalsIgnoreCase(existing.getDisplayStatus())) {
            throw new IllegalArgumentException("Không thể xóa sự kiện đang diễn ra!");
        }

        try {
            boolean success = eventDAO.softDelete(id, updatedBy);
            if (!success) {
                throw new IllegalStateException("Xóa sự kiện thất bại.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi khi xóa sự kiện: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra hợp lệ cho các thông tin của sự kiện. Sử dụng
     * str.trim().isEmpty() cho toàn bộ các trường chuỗi.
     */
    private void validateEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Dữ liệu sự kiện không được để trống.");
        }

        // 1. Kiểm tra Tiêu đề (not blank, length 3 - 200)
        String title = event.getTitle();
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề sự kiện không được để trống.");
        }
        title = title.trim();
        if (title.length() < TITLE_MIN_LEN || title.length() > TITLE_MAX_LEN) {
            throw new IllegalArgumentException("Tiêu đề sự kiện phải từ " + TITLE_MIN_LEN + " đến " + TITLE_MAX_LEN + " ký tự.");
        }
        event.setTitle(title);

        // 2. Kiểm tra Mô tả (nếu có -> max 1000 ký tự)
        String description = event.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            description = description.trim();
            if (description.length() > DESC_MAX_LEN) {
                throw new IllegalArgumentException("Mô tả sự kiện không được vượt quá " + DESC_MAX_LEN + " ký tự.");
            }
            event.setDescription(description);
        } else {
            event.setDescription(null);
        }

        // 3. Kiểm tra Thời gian bắt đầu
        LocalDateTime startTime = event.getStartTime();
        if (startTime == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu không được để trống.");
        }

        // Nếu là thêm mới sự kiện (id <= 0) -> Không cho phép chọn thời gian trong quá khứ
        if (event.getId() <= 0 && startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian bắt đầu sự kiện không được ở trong quá khứ.");
        }

        // 4. Kiểm tra Thời gian kết thúc
        LocalDateTime endTime = event.getEndTime();
        if (endTime == null) {
            throw new IllegalArgumentException("Thời gian kết thúc không được để trống.");
        }

        // 5. Kiểm tra Thời gian kết thúc phải sau Thời gian bắt đầu
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }

        
        
    }
}
