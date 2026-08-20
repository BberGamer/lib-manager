/*
 * Service điều phối nghiệp vụ mượn sách dành cho độc giả và bảo vệ quy tắc gia hạn.
 */
package service;

import dao.BorrowRecordDAO;
import dao.BookDAO;
import dao.BookDAOImpl;
import dao.BookReviewDAO;
import dao.FineDAO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import model.BorrowRecord;
import model.BorrowRenewalResult;

/**
 * Cung cấp dữ liệu trang mượn cá nhân và thực hiện gia hạn qua {@link BorrowRecordDAO}.
 */
public class BorrowService {

    public static final int MAXIMUM_ACTIVE_BORROWS = 3;
    public static final int MAXIMUM_RENEWALS = 4;
    public static final int RENEWAL_EXTENSION_DAYS = 7;
    public static final int UPCOMING_DUE_DAYS = 7;
    public static final int PICKUP_HOLD_HOURS = 24;
    public static final int LOAN_PERIOD_DAYS = 7;
    /** Các tình trạng vật lý được chấp nhận khi nhận lại bản sao. */
    private static final Set<String> BOOK_CONDITIONS = Set.of("GOOD", "WORN", "DAMAGED", "LOST");

    private final BorrowRecordDAO borrowRecordDao;
    private final BookDAO bookDao;
    private final BookReviewDAO bookReviewDao = new BookReviewDAO();
    private final FineDAO fineDao = new FineDAO();

    /**
     * Khởi tạo service với DAO mặc định của ứng dụng.
     */
    public BorrowService() {
        this(new BorrowRecordDAO(), new BookDAOImpl());
    }

    /**
     * Khởi tạo service với DAO được truyền vào để tái sử dụng và kiểm thử.
     *
     * @param borrowRecordDao DAO quản lý dữ liệu lượt mượn
     */
    public BorrowService(BorrowRecordDAO borrowRecordDao) {
        this(borrowRecordDao, new BookDAOImpl());
    }

    /** Khởi tạo service với các DAO sở hữu dữ liệu mượn và sách. */
    public BorrowService(BorrowRecordDAO borrowRecordDao, BookDAO bookDao) {
        this.borrowRecordDao = borrowRecordDao;
        this.bookDao = bookDao;
    }

    /**
     * Tổng hợp lượt đang mượn, lịch sử và các số liệu cảnh báo của một độc giả.
     *
     * @param userId mã độc giả hợp lệ
     * @return dữ liệu hoàn chỉnh để controller chuyển cho JSP
     * @throws Exception khi tầng lưu trữ không thể đọc dữ liệu
     */
    public BorrowPageData getBorrowPage(int userId) throws Exception {
        borrowRecordDao.expirePendingRequests();
        borrowRecordDao.markOverdueBorrows();
        List<BorrowRecord> activeRecords = new ArrayList<>();
        List<BorrowRecord> historyRecords = new ArrayList<>();
        Set<Integer> renewalBlockedBorrowIds
                = borrowRecordDao.findRenewalBlockedBorrowIds(userId);
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
        return new BorrowPageData(activeRecords, historyRecords, upcomingDueCount,
                renewalBlockedBorrowIds);
    }

    /**
     * Gia hạn lượt mượn của chính độc giả, đồng thời từ chối khi đầu sách đã có người đặt trước.
     *
     * @param borrowRecordId mã lượt mượn dương
     * @param userId mã độc giả đang đăng nhập
     * @return kết quả cho biết thành công hoặc nguyên nhân không thể gia hạn
     * @throws Exception khi tầng lưu trữ không thể cập nhật dữ liệu
     */
    public BorrowRenewalResult renewBorrow(int borrowRecordId, int userId) throws Exception {
        if (borrowRecordId <= 0 || userId <= 0) {
            return BorrowRenewalResult.NOT_ELIGIBLE;
        }
        if (borrowRecordDao.isRenewalBlockedByReservation(borrowRecordId, userId)) {
            return BorrowRenewalResult.BLOCKED_BY_RESERVATION;
        }
        if (borrowRecordDao.renewForUser(
                borrowRecordId, userId, MAXIMUM_RENEWALS, RENEWAL_EXTENSION_DAYS)) {
            return BorrowRenewalResult.SUCCESS;
        }
        return borrowRecordDao.isRenewalBlockedByReservation(borrowRecordId, userId)
                ? BorrowRenewalResult.BLOCKED_BY_RESERVATION
                : BorrowRenewalResult.NOT_ELIGIBLE;
    }

    /**
     * Đếm số lượt mượn còn hoạt động của độc giả.
     *
     * @param userId mã độc giả
     * @return số lượt chờ nhận, đang mượn hoặc quá hạn
     * @throws Exception khi không thể truy vấn dữ liệu
     */
    public int getActiveBorrowCount(int userId) throws Exception {
        return borrowRecordDao.countActiveByUserId(userId);
    }

    /**
     * Kiểm tra độc giả còn khoản phạt chưa thanh toán trước khi tạo yêu cầu mượn.
     *
     * @param userId mã độc giả
     * @return {@code true} khi còn ít nhất một khoản phạt chưa thanh toán
     * @throws Exception khi không thể truy vấn dữ liệu phạt
     */
    public boolean hasUnpaidFines(int userId) throws Exception {
        return !fineDao.searchByUser(userId, "UNPAID", null).isEmpty();
    }

    /**
     * Lấy các lượt mượn đã được độc giả đánh giá để trang cá nhân hiển thị đúng thao tác.
     *
     * @param userId mã độc giả
     * @return tập mã lượt mượn đã có đánh giá
     * @throws Exception khi không thể truy vấn dữ liệu đánh giá
     */
    public Set<Integer> getReviewedBorrowIds(int userId) throws Exception {
        return bookReviewDao.getReviewedBorrowIds(userId);
    }

    /** Tạo yêu cầu giữ sách nếu sách tồn tại và còn bản sao khả dụng. */
    public boolean createBorrowRequest(int userId, int bookId) throws Exception {
        if (userId <= 0 || bookId <= 0 || bookDao.findById(bookId) == null) return false;
        
        if (getActiveBorrowCount(userId) >= MAXIMUM_ACTIVE_BORROWS) {
            return false;
        }
        
        borrowRecordDao.expirePendingRequests();
        return borrowRecordDao.createPickupRequest(userId, bookId, PICKUP_HOLD_HOURS);
    }

    /** Hủy yêu cầu chờ nhận thuộc chính độc giả. */
    public boolean cancelBorrowRequest(int borrowId, int userId) throws Exception {
        return borrowId > 0 && userId > 0 && borrowRecordDao.cancelPickupRequest(borrowId, userId);
    }

    /** Xác nhận giao sách cho yêu cầu còn hạn nhận. */
    public boolean confirmPickup(int borrowId, String operator) throws Exception {
        borrowRecordDao.expirePendingRequests();
        return borrowId > 0
                && borrowRecordDao.confirmPickup(borrowId, operator, LOAN_PERIOD_DAYS);
    }

    /**
     * Xác nhận trả sách sau khi chuẩn hóa và kiểm tra tình trạng vật lý được phép.
     *
     * @param borrowId mã lượt mượn
     * @param operator tài khoản nhân viên nhận trả
     * @param condition tình trạng vật lý khi nhận lại
     * @param note ghi chú kiểm tra sách
     * @return kết quả trả sách và reservation vừa được kích hoạt nếu có
     * @throws IllegalArgumentException khi tình trạng vật lý không hợp lệ
     * @throws Exception khi tầng lưu trữ không thể cập nhật dữ liệu
     */
    public BorrowRecordDAO.ReturnResult confirmReturn(int borrowId, String operator,
            String condition, String note) throws Exception {
        String normalizedCondition = condition == null
                ? "GOOD" : condition.trim().toUpperCase(Locale.ROOT);
        if (!BOOK_CONDITIONS.contains(normalizedCondition)) {
            throw new IllegalArgumentException("Tình trạng bản sao không hợp lệ.");
        }
        String normalizedNote = note == null ? null : note.trim();
        return borrowRecordDao.confirmReturn(borrowId, operator, normalizedCondition, normalizedNote);
    }

    /** Giải phóng mọi yêu cầu đã quá hạn nhận sách. */
    public int expirePendingBorrowRequests() throws Exception {
        return borrowRecordDao.expirePendingRequests();
    }

    /**
     * Xác định lượt mượn vẫn đang hoạt động theo trạng thái lưu trữ.
     *
     * @param record lượt mượn cần kiểm tra
     * @return {@code true} với lượt chờ nhận, đang mượn hoặc quá hạn
     */
    private boolean isActive(BorrowRecord record) {
        return "PENDING_PICKUP".equalsIgnoreCase(record.getStatus())
                || "BORROWED".equalsIgnoreCase(record.getStatus())
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
        private final Set<Integer> renewalBlockedBorrowIds;

        /**
         * Tạo dữ liệu trang từ các nhóm lượt mượn đã phân loại.
         *
         * @param activeRecords các lượt đang mượn hoặc quá hạn
         * @param historyRecords các lượt đã kết thúc
         * @param upcomingDueCount số lượt sắp đến hạn
         * @param renewalBlockedBorrowIds các lượt không được gia hạn vì đã có đặt trước
         */
        public BorrowPageData(List<BorrowRecord> activeRecords,
                List<BorrowRecord> historyRecords, int upcomingDueCount,
                Set<Integer> renewalBlockedBorrowIds) {
            this.activeRecords = activeRecords;
            this.historyRecords = historyRecords;
            this.upcomingDueCount = upcomingDueCount;
            this.renewalBlockedBorrowIds = renewalBlockedBorrowIds;
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

        /** @return mã các lượt bị chặn gia hạn do đầu sách đã có đặt trước */
        public Set<Integer> getRenewalBlockedBorrowIds() {
            return renewalBlockedBorrowIds;
        }
    }
}
