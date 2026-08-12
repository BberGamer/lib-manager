/*
 * Service điều phối nghiệp vụ mượn sách dành cho độc giả và bảo vệ quy tắc gia hạn.
 */
package service;

import dao.BorrowRecordDAO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import model.BorrowRecord;

/**
 * Cung cấp dữ liệu trang mượn cá nhân và thực hiện gia hạn qua {@link BorrowRecordDAO}.
 */
public class BorrowService {

    public static final int MAXIMUM_ACTIVE_BORROWS = 3;
    public static final int MAXIMUM_RENEWALS = 4;
    public static final int RENEWAL_EXTENSION_DAYS = 7;
    public static final int UPCOMING_DUE_DAYS = 7;

    private final BorrowRecordDAO borrowRecordDao;

    /**
     * Khởi tạo service với DAO mặc định của ứng dụng.
     */
    public BorrowService() {
        this(new BorrowRecordDAO());
    }

    /**
     * Khởi tạo service với DAO được truyền vào để tái sử dụng và kiểm thử.
     *
     * @param borrowRecordDao DAO quản lý dữ liệu lượt mượn
     */
    public BorrowService(BorrowRecordDAO borrowRecordDao) {
        this.borrowRecordDao = borrowRecordDao;
    }

    /**
     * Tổng hợp lượt đang mượn, lịch sử và các số liệu cảnh báo của một độc giả.
     *
     * @param userId mã độc giả hợp lệ
     * @return dữ liệu hoàn chỉnh để controller chuyển cho JSP
     * @throws Exception khi tầng lưu trữ không thể đọc dữ liệu
     */
    public BorrowPageData getBorrowPage(int userId) throws Exception {
        List<BorrowRecord> activeRecords = new ArrayList<>();
        List<BorrowRecord> historyRecords = new ArrayList<>();
        int upcomingDueCount = 0;
        LocalDate today = LocalDate.now();

        for (BorrowRecord record : borrowRecordDao.findByUserId(userId)) {
            if (isActive(record)) {
                activeRecords.add(record);
                if (isUpcoming(record, today)) {
                    upcomingDueCount++;
                }
            } else {
                historyRecords.add(record);
            }
        }
        return new BorrowPageData(activeRecords, historyRecords, upcomingDueCount);
    }

    /**
     * Gia hạn lượt mượn của chính độc giả, không cho phép gia hạn quá hạn hoặc vượt giới hạn.
     *
     * @param borrowRecordId mã lượt mượn dương
     * @param userId mã độc giả đang đăng nhập
     * @return {@code true} nếu gia hạn thành công
     * @throws Exception khi tầng lưu trữ không thể cập nhật dữ liệu
     */
    public boolean renewBorrow(int borrowRecordId, int userId) throws Exception {
        if (borrowRecordId <= 0 || userId <= 0) {
            return false;
        }
        return borrowRecordDao.renewForUser(
                borrowRecordId, userId, MAXIMUM_RENEWALS, RENEWAL_EXTENSION_DAYS);
    }

    /**
     * Xác định lượt mượn vẫn đang hoạt động theo trạng thái lưu trữ.
     *
     * @param record lượt mượn cần kiểm tra
     * @return {@code true} với trạng thái BORROWING hoặc OVERDUE
     */
    private boolean isActive(BorrowRecord record) {
        return "BORROWING".equalsIgnoreCase(record.getStatus())
                || "OVERDUE".equalsIgnoreCase(record.getStatus());
    }

    /**
     * Kiểm tra lượt mượn đến hạn trong khoảng cảnh báo, không tính sách đã quá hạn.
     *
     * @param record lượt mượn đang hoạt động
     * @param today ngày hiện tại của máy chủ
     * @return {@code true} nếu còn từ 0 đến 7 ngày trước hạn trả
     */
    private boolean isUpcoming(BorrowRecord record, LocalDate today) {
        if (record.getDueDate() == null) {
            return false;
        }
        long remainingDays = ChronoUnit.DAYS.between(today, record.getDueDate());
        return remainingDays >= 0 && remainingDays <= UPCOMING_DUE_DAYS;
    }

    /**
     * DTO chỉ đọc chứa dữ liệu đã được service phân nhóm cho trang mượn cá nhân.
     */
    public static final class BorrowPageData {

        private final List<BorrowRecord> activeRecords;
        private final List<BorrowRecord> historyRecords;
        private final int upcomingDueCount;

        /**
         * Tạo dữ liệu trang từ các nhóm lượt mượn đã phân loại.
         *
         * @param activeRecords các lượt đang mượn hoặc quá hạn
         * @param historyRecords các lượt đã kết thúc
         * @param upcomingDueCount số lượt sắp đến hạn
         */
        public BorrowPageData(List<BorrowRecord> activeRecords,
                List<BorrowRecord> historyRecords, int upcomingDueCount) {
            this.activeRecords = activeRecords;
            this.historyRecords = historyRecords;
            this.upcomingDueCount = upcomingDueCount;
        }

        /** @return các lượt mượn đang hoạt động */
        public List<BorrowRecord> getActiveRecords() {
            return activeRecords;
        }

        /** @return các lượt mượn đã kết thúc */
        public List<BorrowRecord> getHistoryRecords() {
            return historyRecords;
        }

        /** @return số lượt sẽ đến hạn trong bảy ngày */
        public int getUpcomingDueCount() {
            return upcomingDueCount;
        }
    }
}
