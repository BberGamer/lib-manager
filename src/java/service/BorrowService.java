/*
 * Service điều phối nghiệp vụ mượn sách dành cho độc giả và bảo vệ quy tắc gia hạn.
 */
package service;

import dao.BorrowRecordDAO;
import dao.BookDAO;
import dao.BookDAOImpl;
import dao.BookCopyDAO;
import dao.BookReviewDAO;
import dao.FineDAO;
import dao.ReservationDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import model.BorrowRecord;
import model.BorrowRenewalResult;
import model.BookCopy;
import model.Fine;
import utils.AuditLogger;

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
    private static final String LOST_FINE_REASON = "Bồi thường 100% giá sách do độc giả báo mất.";
    /** Các tình trạng vật lý được chấp nhận khi nhận lại bản sao. */
    private static final Set<String> BOOK_CONDITIONS = Set.of("GOOD", "WORN", "DAMAGED", "LOST");

    private final BorrowRecordDAO borrowRecordDao;
    private final BookDAO bookDao;
    private final ReservationDAO reservationDao;
    private final ReservationService reservationService;
    private final BookCopyDAO bookCopyDao = new BookCopyDAO();
    private final BookReviewDAO bookReviewDao = new BookReviewDAO();
    private final FineDAO fineDao = new FineDAO();

    /**
     * Khởi tạo service với DAO mặc định của ứng dụng.
     */
    public BorrowService() {
        this(new BorrowRecordDAO(), new BookDAOImpl(), new ReservationDAO());
    }

    /**
     * Khởi tạo service với DAO được truyền vào để tái sử dụng và kiểm thử.
     *
     * @param borrowRecordDao DAO quản lý dữ liệu lượt mượn
     */
    public BorrowService(BorrowRecordDAO borrowRecordDao) {
        this(borrowRecordDao, new BookDAOImpl(), new ReservationDAO());
    }

    /**
     * Khởi tạo service với các DAO sở hữu dữ liệu mượn và sách.
     *
     * @param borrowRecordDao DAO quản lý lượt mượn
     * @param bookDao DAO quản lý đầu sách
     */
    public BorrowService(BorrowRecordDAO borrowRecordDao, BookDAO bookDao) {
        this(borrowRecordDao, bookDao, new ReservationDAO());
    }

    /**
     * Khởi tạo service với các DAO phục vụ mượn, sách và khóa lịch đặt trước.
     *
     * @param borrowRecordDao DAO quản lý lượt mượn
     * @param bookDao DAO quản lý đầu sách
     * @param reservationDao DAO quản lý reservation và khóa lịch
     */
    public BorrowService(BorrowRecordDAO borrowRecordDao, BookDAO bookDao,
            ReservationDAO reservationDao) {
        this.borrowRecordDao = borrowRecordDao;
        this.bookDao = bookDao;
        this.reservationDao = reservationDao;
        this.reservationService = new ReservationService();
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
        Set<Integer> renewalBlockedBorrowIds = new HashSet<>();
        Set<Integer> renewalEligibleBorrowIds = new HashSet<>();
        int upcomingDueCount = 0;
        LocalDate today = reservationService.businessToday();

        for (BorrowRecord record : borrowRecordDao.findByUserId(userId)) {
            if (isActive(record)) {
                activeRecords.add(record);
                if (isUpcoming(record, today)) {
                    upcomingDueCount++;
                }
                BorrowRenewalResult renewalResult = evaluateRenewal(record, null);
                if (renewalResult == BorrowRenewalResult.BLOCKED_BY_RESERVATION) {
                    renewalBlockedBorrowIds.add(record.getId());
                } else if (renewalResult == BorrowRenewalResult.SUCCESS) {
                    renewalEligibleBorrowIds.add(record.getId());
                }
            } else {
                historyRecords.add(record);
            }
        }
        return new BorrowPageData(activeRecords, historyRecords, upcomingDueCount,
                renewalBlockedBorrowIds, renewalEligibleBorrowIds);
    }

    /**
     * Gia hạn lượt mượn của chính độc giả khi khoảng mới không làm thiếu capacity cho reservation.
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
        try (Connection connection = borrowRecordDao.openTransactionConnection()) {
            connection.setAutoCommit(false);
            try {
                BorrowRecord candidate = borrowRecordDao.findRenewalCandidate(
                        connection, borrowRecordId, userId, false);
                if (candidate == null
                        || !reservationDao.lockBook(connection, candidate.getBookId())) {
                    connection.rollback();
                    return BorrowRenewalResult.NOT_ELIGIBLE;
                }
                BorrowRecord lockedRecord = borrowRecordDao.findRenewalCandidate(
                        connection, borrowRecordId, userId, true);
                BorrowRenewalResult eligibility = evaluateRenewal(lockedRecord, connection);
                if (eligibility != BorrowRenewalResult.SUCCESS) {
                    connection.rollback();
                    return eligibility;
                }
                LocalDate proposedDueDate = lockedRecord.getDueDate()
                        .plusDays(RENEWAL_EXTENSION_DAYS);
                if (!borrowRecordDao.renewLocked(connection, lockedRecord, proposedDueDate)) {
                    connection.rollback();
                    return BorrowRenewalResult.NOT_ELIGIBLE;
                }
                connection.commit();
                return BorrowRenewalResult.SUCCESS;
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Dùng chung một quyết định cho nút gia hạn trên trang và thao tác POST.
     *
     * @param record lượt mượn cần đánh giá
     * @param connection kết nối đang giữ khóa khi cập nhật, hoặc {@code null} khi chỉ hiển thị
     * @return kết quả đủ điều kiện, xung đột reservation hoặc không hợp lệ
     * @throws Exception khi không thể đọc lịch reservation
     */
    private BorrowRenewalResult evaluateRenewal(BorrowRecord record, Connection connection)
            throws Exception {
        LocalDate today = reservationService.businessToday();
        if (!isBaseRenewalEligible(record, today)) {
            return BorrowRenewalResult.NOT_ELIGIBLE;
        }
        LocalDate proposedDueDate = record.getDueDate().plusDays(RENEWAL_EXTENSION_DAYS);
        boolean isAvailable = connection == null
                ? reservationService.isSlotAvailable(record.getBookId(),
                        record.getDueDate(), proposedDueDate)
                : reservationService.isSlotAvailable(connection, record.getBookId(),
                        record.getDueDate(), proposedDueDate);
        return isAvailable ? BorrowRenewalResult.SUCCESS
                : BorrowRenewalResult.BLOCKED_BY_RESERVATION;
    }

    /**
     * Kiểm tra các điều kiện gia hạn không phụ thuộc lịch reservation.
     *
     * @param record lượt mượn cần kiểm tra
     * @param today ngày nghiệp vụ hiện tại
     * @return {@code true} khi lượt đang mượn, chưa quá hạn và chưa vượt số lần gia hạn
     */
    public static boolean isBaseRenewalEligible(BorrowRecord record, LocalDate today) {
        return record != null && today != null
                && "BORROWED".equalsIgnoreCase(record.getStatus())
                && record.getReturnDate() == null && record.getDueDate() != null
                && !record.getDueDate().isBefore(today)
                && record.getRenewalCount() < MAXIMUM_RENEWALS;
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

    /**
     * Tạo yêu cầu giữ sách nếu còn sức chứa trong toàn bộ kỳ mượn dự kiến.
     * Đầu sách được khóa để yêu cầu mượn và đặt trước đồng thời không vượt số bản sao.
     *
     * @param userId mã độc giả
     * @param bookId mã đầu sách
     * @return {@code true} khi yêu cầu được tạo
     * @throws Exception khi không thể đọc hoặc ghi dữ liệu
     */
    public boolean createBorrowRequest(int userId, int bookId) throws Exception {
        if (userId <= 0 || bookId <= 0 || bookDao.findById(bookId) == null) {
            return false;
        }
        borrowRecordDao.expirePendingRequests();
        LocalDate startDate = reservationService.businessToday();
        LocalDate endDate = startDate.plusDays(LOAN_PERIOD_DAYS);
        try (Connection connection = borrowRecordDao.openTransactionConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!reservationDao.lockBook(connection, bookId)
                        || borrowRecordDao.hasActiveForUserAndBook(
                                connection, userId, bookId)
                        || borrowRecordDao.countActiveByUserId(connection, userId)
                                >= MAXIMUM_ACTIVE_BORROWS) {
                    connection.rollback();
                    return false;
                }
                int available = reservationService.getAvailableCapacity(
                        connection, bookId, startDate, endDate);
                if (available <= 0 || !borrowRecordDao.insertPickupRequest(
                        connection, userId, bookId, PICKUP_HOLD_HOURS)) {
                    connection.rollback();
                    return false;
                }
                connection.commit();
                return true;
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /** Hủy yêu cầu chờ nhận thuộc chính độc giả. */
    public boolean cancelBorrowRequest(int borrowId, int userId) throws Exception {
        return borrowId > 0 && userId > 0 && borrowRecordDao.cancelPickupRequest(borrowId, userId);
    }

    /**
     * Ghi nhận độc giả làm mất một bản đang mượn, loại bản đó khỏi tồn kho và tạo vé phạt
     * bằng 100% giá sách trong cùng giao dịch.
     *
     * @param borrowRecordId mã lượt mượn
     * @param userId mã độc giả sở hữu lượt mượn
     * @param operator tài khoản thực hiện thao tác
     * @return {@code true} khi giao dịch báo mất hoàn tất
     * @throws Exception khi không thể cập nhật dữ liệu
     */
    public boolean reportLostBorrow(int borrowRecordId, int userId, String operator)
            throws Exception {
        if (borrowRecordId <= 0 || userId <= 0 || operator == null
                || operator.trim().isEmpty()) {
            return false;
        }
        String normalizedOperator = operator.trim();
        BigDecimal fineAmount;
        try (Connection connection = borrowRecordDao.openTransactionConnection()) {
            connection.setAutoCommit(false);
            try {
                BorrowRecordDAO.LostReportDetails details = borrowRecordDao.reportLostForUser(
                        connection, borrowRecordId, userId, normalizedOperator);
                if (details == null || details.getBookPrice() == null
                        || details.getBookPrice() < 0) {
                    connection.rollback();
                    return false;
                }
                fineAmount = BigDecimal.valueOf(details.getBookPrice());
                Fine fine = new Fine(details.getBorrowRecordId(), details.getUserId(),
                        fineAmount, 0, LOST_FINE_REASON, "UNPAID");
                fine.setBookCondition("LOST");
                if (!fineDao.createFine(connection, fine)) {
                    connection.rollback();
                    return false;
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        AuditLogger.logLostFine(normalizedOperator, userId, borrowRecordId,
                fineAmount.toPlainString());
        return true;
    }

    /**
     * Xác nhận giao sách cho yêu cầu còn hạn nhận và gán bản sao sách dựa trên mã vạch.
     *
     * @param borrowId mã lượt mượn
     * @param barcode mã vạch bản sao được giao
     * @param operator tài khoản thủ thư thực hiện
     * @return {@code true} nếu xác nhận thành công
     * @throws Exception khi xảy ra lỗi dữ liệu
     */
    public boolean confirmPickup(int borrowId, String barcode, String operator) throws Exception {
        borrowRecordDao.expirePendingRequests();
        if (borrowId <= 0 || barcode == null || barcode.trim().isEmpty()) {
            return false;
        }

        BookCopy copy = bookCopyDao.findByBarcode(barcode.trim());
        if (copy == null) {
            return false;
        }

        BorrowRecord record = borrowRecordDao.findById(borrowId);
        if (record == null || record.getBookId() != copy.getBookId()) {
            return false;
        }

        if (bookCopyDao.isCopyBorrowedOrReserved(copy.getId())) {
            return false;
        }

        String condition = copy.getBookCondition();
        if (!"GOOD".equalsIgnoreCase(condition) && !"WORN".equalsIgnoreCase(condition)) {
            return false;
        }

        return borrowRecordDao.confirmPickup(
                borrowId, copy.getId(), operator, LOAN_PERIOD_DAYS);
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
        private final Set<Integer> renewalEligibleBorrowIds;

        /**
         * Tạo dữ liệu trang từ các nhóm lượt mượn đã phân loại.
         *
         * @param activeRecords các lượt đang mượn hoặc quá hạn
         * @param historyRecords các lượt đã kết thúc
         * @param upcomingDueCount số lượt sắp đến hạn
         * @param renewalBlockedBorrowIds các lượt bị xung đột slot đặt trước
         * @param renewalEligibleBorrowIds các lượt được cùng implementation xác nhận có thể gia hạn
         */
        public BorrowPageData(List<BorrowRecord> activeRecords,
                List<BorrowRecord> historyRecords, int upcomingDueCount,
                Set<Integer> renewalBlockedBorrowIds,
                Set<Integer> renewalEligibleBorrowIds) {
            this.activeRecords = activeRecords;
            this.historyRecords = historyRecords;
            this.upcomingDueCount = upcomingDueCount;
            this.renewalBlockedBorrowIds = renewalBlockedBorrowIds;
            this.renewalEligibleBorrowIds = renewalEligibleBorrowIds;
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

        /** @return mã các lượt bị chặn gia hạn do xung đột slot đặt trước */
        public Set<Integer> getRenewalBlockedBorrowIds() {
            return renewalBlockedBorrowIds;
        }

        /** @return mã các lượt đã được service xác nhận có thể hiển thị thao tác gia hạn */
        public Set<Integer> getRenewalEligibleBorrowIds() {
            return renewalEligibleBorrowIds;
        }
    }
}
