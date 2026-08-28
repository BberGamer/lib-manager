/* Service quản lý ngày nhận dự kiến, hàng chờ và thông báo của luồng đặt trước sách. */
package service;

import dao.BookDAO;
import dao.BookDAOImpl;
import dao.BorrowRecordDAO;
import dao.FineDAO;
import dao.ReservationDAO;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import model.Book;
import model.BorrowRecord;
import model.ReservationRecord;

/**
 * Sở hữu validation và cách phân bổ slot trả sách cho yêu cầu đặt trước; phối hợp
 * {@link ReservationDAO}, {@link BorrowRecordDAO} và {@link NotificationService}.
 */
public class ReservationService {

    public static final int READY_HOLD_HOURS = 24;
    public static final int MAXIMUM_ADVANCE_YEARS = 1;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ReservationDAO reservationDao = new ReservationDAO();
    private final BookDAO bookDao = new BookDAOImpl();
    private final BorrowRecordDAO borrowRecordDao = new BorrowRecordDAO();
    private final FineDAO fineDao = new FineDAO();
    private final NotificationService notificationService = new NotificationService();

    /**
     * Chuẩn bị dữ liệu form và gợi ý ngày nhận sớm nhất theo lịch trả hiện hành.
     *
     * @param userId mã độc giả đang đăng nhập
     * @param bookId mã đầu sách muốn đặt trước
     * @return dữ liệu hiển thị form xác nhận
     * @throws ReservationValidationException khi đầu sách hoặc độc giả không đủ điều kiện
     * @throws Exception khi không thể đọc dữ liệu
     */
    public CreationInfo getCreationInfo(int userId, int bookId)
            throws ReservationValidationException, Exception {
        expireExpiredReadyReservations();
        Book book = requireReservableBook(userId, bookId);
        LocalDate today = businessToday();
        LocalDate maximumDate = today.plusYears(MAXIMUM_ADVANCE_YEARS);
        SlotSchedule schedule = loadSlotSchedule(bookId);
        LocalDate earliestAvailableDate = findEarliestAvailableStart(
                schedule, today, maximumDate);
        if (earliestAvailableDate == null) {
            throw new ReservationValidationException(
                    "Không còn slot 7 ngày phù hợp trong giới hạn đặt trước 1 năm.");
        }
        LocalDate defaultEndDate = earliestAvailableDate.plusDays(
                BorrowService.LOAN_PERIOD_DAYS);
        int availableCapacity = calculateAvailableCapacity(
                schedule.getEligibleCopyCount(), schedule.getOccupiedPeriods(),
                earliestAvailableDate, defaultEndDate);
        int waitingCount = reservationDao.countActiveByBook(bookId);
        return new CreationInfo(book, waitingCount, waitingCount + 1,
                today, maximumDate, earliestAvailableDate,
                earliestAvailableDate, earliestAvailableDate, availableCapacity);
    }

    /**
     * Tính lại ngày dự kiến khi độc giả thay đổi ngày nhận trên form.
     *
     * @param userId mã độc giả
     * @param bookId mã đầu sách
     * @param requestedPickupDate ngày nhận mong muốn
     * @return ngày có sách sớm nhất và ngày dự kiến theo lựa chọn hiện tại
     * @throws ReservationValidationException khi ngày hoặc điều kiện đặt trước không hợp lệ
     * @throws Exception khi không thể đọc dữ liệu
     */
    public PickupEstimate estimatePickupDate(int userId, int bookId,
            LocalDate requestedPickupDate)
            throws ReservationValidationException, Exception {
        requireReservableBook(userId, bookId);
        validateRequestedDate(requestedPickupDate);
        LocalDate endDate = requestedPickupDate.plusDays(BorrowService.LOAN_PERIOD_DAYS);
        SlotSchedule schedule = loadSlotSchedule(bookId);
        LocalDate earliestAvailableDate = findEarliestAvailableStart(
                schedule, businessToday(), businessToday().plusYears(MAXIMUM_ADVANCE_YEARS));
        if (earliestAvailableDate == null) {
            throw new ReservationValidationException(
                    "Không còn slot 7 ngày phù hợp trong giới hạn đặt trước 1 năm.");
        }
        int availableCapacity = calculateAvailableCapacity(
                schedule.getEligibleCopyCount(), schedule.getOccupiedPeriods(),
                requestedPickupDate, endDate);
        if (availableCapacity <= 0) {
            throw new ReservationValidationException(
                    "Khoảng ngày đã chọn không còn bản sao trống trong toàn bộ 7 ngày.");
        }
        return new PickupEstimate(earliestAvailableDate, requestedPickupDate,
                endDate, availableCapacity);
    }

    /**
     * Tạo yêu cầu sau khi kiểm tra lại ngày và điều kiện ngay trước lúc ghi dữ liệu.
     *
     * @param userId mã độc giả
     * @param bookId mã đầu sách
     * @param requestedPickupDate ngày nhận mong muốn
     * @return kết quả chứa ngày mong muốn và ngày hệ thống đã phân bổ
     * @throws ReservationValidationException khi yêu cầu không còn hợp lệ
     * @throws Exception khi không thể lưu yêu cầu
     */
    public CreationResult createReservation(int userId, int bookId, LocalDate requestedPickupDate)
            throws ReservationValidationException, Exception {
        expireExpiredReadyReservations();
        requireReservableBook(userId, bookId);
        validateRequestedDate(requestedPickupDate);
        LocalDate endDate = requestedPickupDate.plusDays(BorrowService.LOAN_PERIOD_DAYS);
        try (Connection connection = reservationDao.openTransactionConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!reservationDao.lockBook(connection, bookId)) {
                    throw new ReservationValidationException(
                            "Không tìm thấy đầu sách cần đặt trước.");
                }
                if (reservationDao.hasActiveForUser(connection, userId, bookId)
                        || borrowRecordDao.hasActiveForUserAndBook(connection, userId, bookId)) {
                    throw new ReservationValidationException(
                            "Bạn đã có yêu cầu đặt trước hoặc lượt mượn đang hoạt động cho sách này.");
                }
                SlotSchedule schedule = loadSlotSchedule(connection, bookId);
                int availableCapacity = calculateAvailableCapacity(
                        schedule.getEligibleCopyCount(), schedule.getOccupiedPeriods(),
                        requestedPickupDate, endDate);
                if (availableCapacity <= 0) {
                    throw new ReservationValidationException(
                            "Khoảng ngày đã chọn vừa hết slot. Vui lòng chọn ngày bắt đầu khác.");
                }
                if (!reservationDao.insertWaiting(connection, userId, bookId,
                        requestedPickupDate, requestedPickupDate)) {
                    throw new ReservationValidationException(
                            "Không thể lưu yêu cầu đặt trước lúc này.");
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        return new CreationResult(requestedPickupDate, requestedPickupDate, endDate);
    }

    /**
     * Lấy lịch sử và hàng chờ thuộc độc giả.
     *
     * @param userId mã độc giả
     * @return danh sách đặt trước mới nhất trước
     * @throws Exception khi không thể đọc dữ liệu
     */
    public List<ReservationRecord> getMyReservations(int userId) throws Exception {
        expireExpiredReadyReservations();
        List<ReservationRecord> records = reservationDao.findByUserId(userId);
        for (ReservationRecord record : records) {
            if (record.getExpectedPickupDate() != null) {
                record.setExpectedEndDate(record.getExpectedPickupDate()
                        .plusDays(BorrowService.LOAN_PERIOD_DAYS));
            }
        }
        return records;
    }

    /**
     * Lấy một trang reservation cho Admin hoặc Librarian theo bộ lọc quản lý.
     *
     * @param status trạng thái reservation cần lọc
     * @param keyword từ khóa tên độc giả, tài khoản hoặc tên sách
     * @param sortOrder thứ tự ưu tiên {@code ASC}, {@code DESC} hoặc {@code NEWEST}
     * @param pageNumber số trang bắt đầu từ 1
     * @param pageSize số bản ghi tối đa trên trang
     * @return danh sách reservation đã kèm vị trí hàng chờ động
     * @throws Exception khi không thể đọc dữ liệu
     */
    public List<ReservationRecord> getReservationsForManagement(String status, String keyword,
            String sortOrder, int pageNumber, int pageSize) throws Exception {
        return reservationDao.searchReservations(
                status, keyword, sortOrder, pageNumber, pageSize);
    }

    /**
     * Đếm reservation cho Admin hoặc Librarian theo cùng bộ lọc danh sách.
     *
     * @param status trạng thái reservation cần lọc
     * @param keyword từ khóa tên độc giả, tài khoản hoặc tên sách
     * @return tổng số reservation phù hợp
     * @throws Exception khi không thể đọc dữ liệu
     */
    public int countReservationsForManagement(String status, String keyword) throws Exception {
        return reservationDao.countReservations(status, keyword);
    }

    /**
     * Hủy yêu cầu còn hoạt động thuộc đúng độc giả.
     *
     * @param reservationId mã yêu cầu
     * @param userId mã chủ sở hữu yêu cầu
     * @return {@code true} khi hủy thành công
     * @throws Exception khi không thể cập nhật dữ liệu
     */
    public boolean cancelReservation(int reservationId, int userId) throws Exception {
        return reservationId > 0 && reservationDao.cancelOwned(reservationId, userId);
    }

    /**
     * Hủy yêu cầu đang hoạt động theo thao tác của Admin hoặc Librarian.
     *
     * @param reservationId mã yêu cầu cần hủy
     * @return {@code true} khi reservation và lượt chờ nhận liên quan đã được hủy
     * @throws Exception khi không thể cập nhật dữ liệu
     */
    public boolean cancelReservationByStaff(int reservationId) throws Exception {
        return reservationId > 0 && reservationDao.cancelByStaff(reservationId);
    }

    /**
     * Chuyển reservation sang sẵn sàng nhận, tạo lượt chờ nhận và gửi thông báo.
     * Việc xác nhận độc giả đã nhận sách vẫn thuộc luồng mượn trả.
     *
     * @param reservationId mã yêu cầu đang chờ
     * @param operator tài khoản nhân viên thực hiện
     * @param sendEmail có gửi email ngoài thông báo trong hệ thống hay không
     * @return {@code true} khi đã giữ được bản sao cho reservation
     * @throws Exception khi không thể cập nhật dữ liệu
     */
    public boolean markReservationReady(int reservationId, String operator, boolean sendEmail)
            throws Exception {
        if (reservationId <= 0) {
            return false;
        }
        ReservationRecord record = reservationDao.manuallyReadyReservation(
                reservationId, operator);
        if (record == null) {
            return false;
        }
        notifyReservationReady(record, sendEmail);
        return true;
    }

    /**
     * Kích hoạt người chờ đã đến lịch khi một bản sách vừa được hoàn trả.
     *
     * @param bookId mã đầu sách vừa có bản trả
     * @return {@code true} khi đã tạo slot chờ nhận cho một yêu cầu
     * @throws Exception khi không thể cập nhật dữ liệu
     */
    public boolean activateNextReservation(int bookId) throws Exception {
        return reservationDao.activateNext(bookId, READY_HOLD_HOURS);
    }

    /**
     * Thử tạo slot chờ nhận cho mọi yêu cầu đã đến ngày dự kiến và gửi thông báo nhận sách.
     *
     * @param sendEmail có gửi thêm email ngoài thông báo trong hệ thống hay không
     * @return số yêu cầu được chuyển sang sẵn sàng nhận
     * @throws Exception khi truy cập dữ liệu thất bại
     */
    public int activateDueReservations(boolean sendEmail) throws Exception {
        int activatedCount = 0;
        for (Integer reservationId : reservationDao.findDueWaitingIds()) {
            ReservationRecord record = reservationDao.manuallyReadyReservation(reservationId, "SYSTEM");
            if (record != null) {
                activatedCount++;
                notifyReservationReady(record, sendEmail);
            }
        }
        return activatedCount;
    }

    /**
     * Gửi một lần thông báo cho yêu cầu lỡ ngày dự kiến do sách vẫn chưa được hoàn trả.
     *
     * @param sendEmail có gửi kèm email hay không
     * @return số yêu cầu đã tạo thông báo thành công
     * @throws Exception khi không thể đọc hoặc đánh dấu dữ liệu đặt trước
     */
    public int notifyDelayedReservations(boolean sendEmail) throws Exception {
        int notifiedCount = 0;
        for (ReservationRecord record : reservationDao.findUnnotifiedDelayedReservations()) {
            String title = "Sách đặt trước chưa được hoàn trả – FPT Library";
            String message = "Xin chào " + record.getUser().getFullName() + ",\n\n"
                    + "Do hiện tại chưa có sách '" + record.getBook().getTitle()
                    + "' bạn đặt trước. Thư viện sẽ gửi thông báo có sách lại đến cho bạn "
                    + "trong thời gian sớm nhất.";
            String email = sendEmail ? record.getUser().getEmail() : null;
            boolean created = notificationService.createAndSendNotification(
                    record.getUserId(), title, message, "RESERVATION",
                    record.getId(), "reservation", email);
            if (created && reservationDao.markDelayNotified(record.getId())) {
                notifiedCount++;
            }
        }
        return notifiedCount;
    }

    /**
     * Đóng đồng thời các lượt chờ nhận và reservation đã quá hạn giữ.
     *
     * @return số lượt chờ nhận đã hết hạn
     * @throws Exception khi tầng lưu trữ không thể cập nhật dữ liệu
     */
    public int expireExpiredReadyReservations() throws Exception {
        return borrowRecordDao.expirePendingRequests();
    }

    /**
     * Kiểm tra đầu sách, yêu cầu trùng, khoản phạt và khả năng dự kiến ngày trả.
     *
     * @param userId mã độc giả
     * @param bookId mã đầu sách
     * @return đầu sách đủ điều kiện đặt trước
     * @throws ReservationValidationException khi vi phạm điều kiện nghiệp vụ
     * @throws Exception khi không thể đọc dữ liệu
     */
    private Book requireReservableBook(int userId, int bookId)
            throws ReservationValidationException, Exception {
        if (userId <= 0 || bookId <= 0) {
            throw new ReservationValidationException("Mã độc giả hoặc đầu sách không hợp lệ.");
        }
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new ReservationValidationException("Không tìm thấy đầu sách cần đặt trước.");
        }
        if (reservationDao.findActive(userId, bookId) != null) {
            throw new ReservationValidationException("Bạn đã có yêu cầu đặt trước đang hoạt động cho sách này.");
        }
        if (borrowRecordDao.hasActiveForUserAndBook(userId, bookId)) {
            throw new ReservationValidationException(
                    "Bạn đang có lượt mượn hoặc chờ nhận đang hoạt động cho sách này.");
        }
        if (!fineDao.searchByUser(userId, "UNPAID", null).isEmpty()) {
            throw new ReservationValidationException(
                    "Bạn phải thanh toán hết các khoản phạt trước khi đặt trước sách.");
        }
        return book;
    }

    /**
     * Bảo đảm ngày độc giả chọn nằm từ hôm nay đến tối đa một năm sau.
     *
     * @param requestedPickupDate ngày cần kiểm tra
     * @throws ReservationValidationException khi ngày trống, trong quá khứ hoặc quá xa
     */
    private void validateRequestedDate(LocalDate requestedPickupDate)
            throws ReservationValidationException {
        LocalDate today = businessToday();
        if (requestedPickupDate == null) {
            throw new ReservationValidationException("Vui lòng chọn ngày bạn muốn nhận sách.");
        }
        if (requestedPickupDate.isBefore(today)) {
            throw new ReservationValidationException("Ngày nhận sách không được nằm trong quá khứ.");
        }
        if (requestedPickupDate.isAfter(today.plusYears(MAXIMUM_ADVANCE_YEARS))) {
            throw new ReservationValidationException(
                    "Ngày nhận sách phải nằm trong vòng 1 năm kể từ hôm nay.");
        }
    }

    /**
     * Kiểm tra một khoảng có còn sức chứa bằng ảnh chụp lịch mới từ cơ sở dữ liệu.
     *
     * @param bookId mã đầu sách
     * @param startDate đầu khoảng cần kiểm tra
     * @param endDate cuối khoảng không bao gồm ngày này
     * @return {@code true} khi còn ít nhất một bản sao cho toàn bộ khoảng
     * @throws Exception khi không thể đọc lịch
     */
    public boolean isSlotAvailable(int bookId, LocalDate startDate, LocalDate endDate)
            throws Exception {
        try (Connection connection = reservationDao.openTransactionConnection()) {
            return isSlotAvailable(connection, bookId, startDate, endDate);
        }
    }

    /**
     * Kiểm tra một khoảng bằng đúng kết nối mà service gọi đang dùng trong giao dịch.
     *
     * @param connection kết nối chứa ảnh chụp lịch cần dùng
     * @param bookId mã đầu sách
     * @param startDate đầu khoảng cần kiểm tra
     * @param endDate cuối khoảng không bao gồm ngày này
     * @return {@code true} khi còn sức chứa cho toàn bộ khoảng
     * @throws Exception khi không thể đọc lịch
     */
    public boolean isSlotAvailable(Connection connection, int bookId,
            LocalDate startDate, LocalDate endDate) throws Exception {
        return getAvailableCapacity(connection, bookId, startDate, endDate) > 0;
    }

    /**
     * Tính số bản còn có thể nhận thêm một lượt trong toàn bộ khoảng được yêu cầu.
     *
     * @param connection kết nối chứa ảnh chụp lịch cần dùng
     * @param bookId mã đầu sách
     * @param startDate đầu khoảng cần kiểm tra
     * @param endDate cuối khoảng không bao gồm ngày này
     * @return số lượt 7 ngày còn có thể bắt đầu mà không vượt số bản sao
     * @throws Exception khi không thể đọc lịch
     */
    public int getAvailableCapacity(Connection connection, int bookId,
            LocalDate startDate, LocalDate endDate) throws Exception {
        SlotSchedule schedule = loadSlotSchedule(connection, bookId);
        return calculateAvailableCapacity(schedule.getEligibleCopyCount(),
                schedule.getOccupiedPeriods(), startDate, endDate);
    }

    /**
     * Cập nhật {@code available} của một đầu sách theo khả năng mượn ngay trong 7 ngày tới.
     *
     * @param book đầu sách cần chuẩn bị cho giao diện
     * @throws Exception khi không thể đọc lịch
     */
    public void applyImmediateAvailability(Book book) throws Exception {
        if (book == null) {
            return;
        }
        applyImmediateAvailability(List.of(book));
    }

    /**
     * Cập nhật {@code available} và khả năng đặt trước cho danh sách sách bằng cùng thuật toán slot.
     *
     * @param books danh sách đầu sách cần chuẩn bị cho giao diện
     * @throws Exception khi không thể đọc lịch
     */
    public void applyImmediateAvailability(List<Book> books) throws Exception {
        if (books == null || books.isEmpty()) {
            return;
        }
        LocalDate today = businessToday();
        LocalDate immediateEndDate = today.plusDays(BorrowService.LOAN_PERIOD_DAYS);
        LocalDate maximumDate = today.plusYears(MAXIMUM_ADVANCE_YEARS);
        try (Connection connection = reservationDao.openTransactionConnection()) {
            for (Book book : books) {
                if (book == null || book.getId() <= 0) {
                    continue;
                }
                SlotSchedule schedule = loadSlotSchedule(connection, book.getId());
                book.setAvailable(calculateAvailableCapacity(
                        schedule.getEligibleCopyCount(), schedule.getOccupiedPeriods(),
                        today, immediateEndDate));
                book.setReservable(findEarliestAvailableStart(
                        schedule, today, maximumDate) != null);
            }
        }
    }

    /**
     * Tải lịch bằng một kết nối riêng cho các thao tác chỉ đọc.
     *
     * @param bookId mã đầu sách
     * @return ảnh chụp sức chứa và các khoảng đã chiếm
     * @throws Exception khi không thể đọc lịch
     */
    private SlotSchedule loadSlotSchedule(int bookId) throws Exception {
        try (Connection connection = reservationDao.openTransactionConnection()) {
            return loadSlotSchedule(connection, bookId);
        }
    }

    /**
     * Dựng lịch thống nhất từ bản sao đủ điều kiện, reservation và lượt mượn đang hoạt động.
     * Reservation READY_FOR_PICKUP đã được biểu diễn bằng slot dự kiến nên lượt PENDING_PICKUP
     * tương ứng không được tính lặp lần thứ hai.
     *
     * @param connection kết nối dùng để đọc cùng một ảnh chụp dữ liệu
     * @param bookId mã đầu sách
     * @return lịch khoảng thời gian của đầu sách
     * @throws Exception khi không thể đọc dữ liệu lịch
     */
    private SlotSchedule loadSlotSchedule(Connection connection, int bookId) throws Exception {
        int eligibleCopyCount = reservationDao.countEligibleCopies(connection, bookId);
        List<SlotPeriod> occupiedPeriods = new ArrayList<>();
        Set<Integer> readyReservationUsers = new HashSet<>();
        LocalDate today = businessToday();

        for (ReservationRecord reservation
                : reservationDao.findSchedulingReservations(connection, bookId)) {
            if (!isActiveReservationStatus(reservation.getStatus())
                    || reservation.getExpectedPickupDate() == null) {
                continue;
            }
            LocalDate startDate = reservation.getExpectedPickupDate();
            if ("READY_FOR_PICKUP".equalsIgnoreCase(reservation.getStatus())
                    && startDate.isBefore(today)) {
                startDate = today;
            }
            occupiedPeriods.add(new SlotPeriod(startDate,
                    startDate.plusDays(BorrowService.LOAN_PERIOD_DAYS)));
            if ("READY_FOR_PICKUP".equalsIgnoreCase(reservation.getStatus())) {
                readyReservationUsers.add(reservation.getUserId());
            }
        }
        for (BorrowRecord borrow : borrowRecordDao.findSchedulingBorrows(connection, bookId)) {
            if ("PENDING_PICKUP".equalsIgnoreCase(borrow.getStatus())) {
                if (readyReservationUsers.contains(borrow.getUserId())) {
                    continue;
                }
                LocalDate requestDate = borrow.getRequestDate() == null
                        ? today : borrow.getRequestDate().toLocalDate();
                LocalDate startDate = requestDate.isBefore(today) ? today : requestDate;
                occupiedPeriods.add(new SlotPeriod(startDate,
                        startDate.plusDays(BorrowService.LOAN_PERIOD_DAYS)));
            } else if ("BORROWED".equalsIgnoreCase(borrow.getStatus())) {
                LocalDate endDate = borrow.getDueDate();
                if (endDate == null || endDate.isBefore(today)) {
                    occupiedPeriods.add(new SlotPeriod(today, null));
                } else if (endDate.isAfter(today)) {
                    occupiedPeriods.add(new SlotPeriod(today, endDate));
                }
            } else if ("OVERDUE".equalsIgnoreCase(borrow.getStatus())) {
                occupiedPeriods.add(new SlotPeriod(today, null));
            }
        }
        return new SlotSchedule(eligibleCopyCount, occupiedPeriods);
    }

    /**
     * Cho biết trạng thái reservation có chiếm một slot lịch hay không.
     *
     * @param status trạng thái lưu trữ cần kiểm tra
     * @return {@code true} chỉ với WAITING hoặc READY_FOR_PICKUP
     */
    public static boolean isActiveReservationStatus(String status) {
        return "WAITING".equalsIgnoreCase(status)
                || "READY_FOR_PICKUP".equalsIgnoreCase(status);
    }

    /**
     * Kiểm tra sức chứa của một khoảng nửa mở bằng phép quét sự kiện trên các khoảng giao nhau.
     * Hai khoảng có điểm biên cuối/đầu bằng nhau không giao nhau.
     *
     * @param eligibleCopyCount tổng số bản sao đủ điều kiện
     * @param occupiedPeriods các khoảng đã được lượt mượn hoặc reservation chiếm
     * @param candidateStart đầu khoảng cần thêm
     * @param candidateEnd cuối khoảng cần thêm, không bao gồm ngày này
     * @return {@code true} khi thêm khoảng mới không làm số lượt đồng thời vượt sức chứa
     */
    public static boolean hasAvailableCapacity(int eligibleCopyCount,
            List<SlotPeriod> occupiedPeriods, LocalDate candidateStart,
            LocalDate candidateEnd) {
        return calculateAvailableCapacity(eligibleCopyCount, occupiedPeriods,
                candidateStart, candidateEnd) > 0;
    }

    /**
     * Tính capacity còn lại bằng mức chiếm dụng cao nhất trong khoảng nửa mở.
     * Các lượt không giao nhau có thể dùng nối tiếp cùng một bản sao nên không bị trừ lặp.
     *
     * @param eligibleCopyCount tổng số bản sao đủ điều kiện
     * @param occupiedPeriods các khoảng đã được lượt mượn hoặc reservation chiếm
     * @param candidateStart đầu khoảng cần kiểm tra
     * @param candidateEnd cuối khoảng không bao gồm ngày này
     * @return số bản còn có thể cấp thêm cho toàn bộ khoảng, không bao giờ âm
     */
    public static int calculateAvailableCapacity(int eligibleCopyCount,
            List<SlotPeriod> occupiedPeriods, LocalDate candidateStart,
            LocalDate candidateEnd) {
        if (eligibleCopyCount <= 0 || occupiedPeriods == null
                || candidateStart == null || candidateEnd == null
                || !candidateStart.isBefore(candidateEnd)) {
            return 0;
        }
        Map<LocalDate, Integer> events = new TreeMap<>();
        for (SlotPeriod period : occupiedPeriods) {
            if (period == null || period.getStartDate() == null) {
                continue;
            }
            LocalDate periodEnd = period.getEndDate() == null
                    ? candidateEnd : period.getEndDate();
            if (!period.getStartDate().isBefore(periodEnd)) {
                continue;
            }
            if (!period.getStartDate().isBefore(candidateEnd)
                    || !candidateStart.isBefore(periodEnd)) {
                continue;
            }
            LocalDate overlapStart = period.getStartDate().isAfter(candidateStart)
                    ? period.getStartDate() : candidateStart;
            LocalDate overlapEnd = periodEnd.isBefore(candidateEnd)
                    ? periodEnd : candidateEnd;
            events.merge(overlapStart, 1, Integer::sum);
            events.merge(overlapEnd, -1, Integer::sum);
        }

        int occupiedCount = 0;
        int maximumOccupiedCount = 0;
        for (Map.Entry<LocalDate, Integer> event : events.entrySet()) {
            occupiedCount += event.getValue();
            if (event.getKey().isBefore(candidateEnd)) {
                maximumOccupiedCount = Math.max(maximumOccupiedCount, occupiedCount);
            }
        }
        return Math.max(0, eligibleCopyCount - maximumOccupiedCount);
    }

    /**
     * Tìm ngày bắt đầu đầu tiên có đủ sức chứa trong giới hạn cho phép.
     *
     * @param schedule lịch cần kiểm tra
     * @param minimumDate ngày bắt đầu tìm kiếm
     * @param maximumDate ngày bắt đầu muộn nhất được chấp nhận
     * @return ngày đầu tiên còn slot hoặc {@code null} khi không tìm thấy
     */
    private LocalDate findEarliestAvailableStart(SlotSchedule schedule,
            LocalDate minimumDate, LocalDate maximumDate) {
        for (LocalDate candidate = minimumDate; !candidate.isAfter(maximumDate);
                candidate = candidate.plusDays(1)) {
            if (hasAvailableCapacity(schedule.getEligibleCopyCount(),
                    schedule.getOccupiedPeriods(), candidate,
                    candidate.plusDays(BorrowService.LOAN_PERIOD_DAYS))) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Gửi thông báo sách đã có bản sẵn sàng cho người được phân bổ.
     *
     * @param record yêu cầu vừa được kích hoạt
     * @param sendEmail có gửi thêm email hay không
     * @return {@code true} khi thông báo trong hệ thống được tạo thành công
     */
    public boolean notifyReservationReady(ReservationRecord record, boolean sendEmail) {
        return notifyReservationReady(record.getId(), record.getUserId(),
                record.getUser().getFullName(), record.getUser().getEmail(),
                record.getBook().getTitle(), sendEmail);
    }

    /**
     * Tạo thông báo sẵn sàng từ dữ liệu trả sách mà không buộc controller tự ghép nội dung.
     *
     * @param reservationId mã yêu cầu vừa được kích hoạt
     * @param userId mã độc giả nhận thông báo
     * @param fullName tên hiển thị của độc giả
     * @param email địa chỉ email, có thể trống
     * @param bookTitle tên đầu sách
     * @param sendEmail có gửi thêm email hay không
     * @return {@code true} khi thông báo trong hệ thống được tạo thành công
     */
    public boolean notifyReservationReady(int reservationId, int userId, String fullName,
            String email, String bookTitle, boolean sendEmail) {
        String title = "Sách đặt trước đã sẵn sàng – FPT Library";
        String message = "Xin chào " + fullName + ",\n\n"
                + "Cuốn sách '" + bookTitle
                + "' bạn đặt trước hiện đã sẵn sàng để mượn.\n"
                + "Vui lòng đến thư viện để nhận sách trong vòng 24 giờ kể từ thời điểm này.";
        String recipientEmail = sendEmail ? email : null;
        return notificationService.createAndSendNotification(
                userId, title, message, "RESERVATION",
                reservationId, "reservation", recipientEmail);
    }

    /** @return ngày hiện tại theo múi giờ nghiệp vụ của thư viện */
    public LocalDate businessToday() {
        return LocalDate.now(BUSINESS_ZONE);
    }

    /**
     * Ảnh chụp chỉ đọc của sức chứa và các khoảng đã chiếm của một đầu sách.
     */
    private static final class SlotSchedule {

        private final int eligibleCopyCount;
        private final List<SlotPeriod> occupiedPeriods;

        /**
         * Tạo ảnh chụp lịch từ số bản sao và các khoảng đã được phân bổ.
         *
         * @param eligibleCopyCount số bản sao đủ điều kiện
         * @param occupiedPeriods các khoảng đang chiếm sức chứa
         */
        private SlotSchedule(int eligibleCopyCount, List<SlotPeriod> occupiedPeriods) {
            this.eligibleCopyCount = eligibleCopyCount;
            this.occupiedPeriods = occupiedPeriods;
        }

        /** @return số bản sao đủ điều kiện dùng làm sức chứa lịch */
        private int getEligibleCopyCount() {
            return eligibleCopyCount;
        }

        /** @return các khoảng đã chiếm sức chứa */
        private List<SlotPeriod> getOccupiedPeriods() {
            return occupiedPeriods;
        }
    }

    /**
     * Khoảng ngày nửa mở dùng chung cho kiểm tra slot đặt trước và gia hạn.
     */
    public static final class SlotPeriod {

        private final LocalDate startDate;
        private final LocalDate endDate;

        /**
         * Tạo khoảng nửa mở; {@code endDate} bằng {@code null} biểu diễn chưa biết ngày kết thúc.
         *
         * @param startDate ngày đầu khoảng
         * @param endDate ngày cuối không thuộc khoảng, hoặc {@code null}
         */
        public SlotPeriod(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        /** @return ngày đầu thuộc khoảng */
        public LocalDate getStartDate() {
            return startDate;
        }

        /** @return ngày cuối không thuộc khoảng, hoặc {@code null} nếu chưa xác định */
        public LocalDate getEndDate() {
            return endDate;
        }
    }

    /** DTO chỉ đọc cho trang xác nhận đặt trước. */
    public static final class CreationInfo {

        private final Book book;
        private final int waitingCount;
        private final int expectedPosition;
        private final LocalDate minimumPickupDate;
        private final LocalDate maximumPickupDate;
        private final LocalDate requestedPickupDate;
        private final LocalDate earliestAvailableDate;
        private final LocalDate expectedPickupDate;
        private final int availableCapacity;

        /**
         * Tạo dữ liệu hiển thị form từ kết quả kiểm tra và phân bổ hiện tại.
         *
         * @param book đầu sách
         * @param waitingCount số người đang chờ
         * @param expectedPosition vị trí dự kiến
         * @param minimumPickupDate ngày được chọn sớm nhất
         * @param maximumPickupDate ngày được chọn muộn nhất
         * @param requestedPickupDate ngày mong muốn mặc định
         * @param earliestAvailableDate ngày sớm nhất có slot sau khi xét hàng chờ
         * @param expectedPickupDate ngày dự kiến mặc định
         * @param availableCapacity số bản còn cấp được trong toàn bộ slot mặc định
         */
        public CreationInfo(Book book, int waitingCount, int expectedPosition,
                LocalDate minimumPickupDate, LocalDate maximumPickupDate,
                LocalDate requestedPickupDate, LocalDate earliestAvailableDate,
                LocalDate expectedPickupDate, int availableCapacity) {
            this.book = book;
            this.waitingCount = waitingCount;
            this.expectedPosition = expectedPosition;
            this.minimumPickupDate = minimumPickupDate;
            this.maximumPickupDate = maximumPickupDate;
            this.requestedPickupDate = requestedPickupDate;
            this.earliestAvailableDate = earliestAvailableDate;
            this.expectedPickupDate = expectedPickupDate;
            this.availableCapacity = availableCapacity;
        }

        /** @return sách đặt trước */
        public Book getBook() {
            return book;
        }

        /** @return số người đang chờ */
        public int getWaitingCount() {
            return waitingCount;
        }

        /** @return vị trí dự kiến */
        public int getExpectedPosition() {
            return expectedPosition;
        }

        /** @return ngày được chọn sớm nhất */
        public LocalDate getMinimumPickupDate() {
            return minimumPickupDate;
        }

        /** @return ngày được chọn muộn nhất */
        public LocalDate getMaximumPickupDate() {
            return maximumPickupDate;
        }

        /** @return ngày mong muốn mặc định */
        public LocalDate getRequestedPickupDate() {
            return requestedPickupDate;
        }

        /** @return ngày sớm nhất dự kiến có sách sau khi xét lịch trả và hàng chờ */
        public LocalDate getEarliestAvailableDate() {
            return earliestAvailableDate;
        }

        /** @return ngày dự kiến mặc định */
        public LocalDate getExpectedPickupDate() {
            return expectedPickupDate;
        }

        /** @return ngày kết thúc dự kiến, không thuộc khoảng đặt trước */
        public LocalDate getExpectedEndDate() {
            return expectedPickupDate.plusDays(BorrowService.LOAN_PERIOD_DAYS);
        }

        /** @return số bản còn có thể đặt trong toàn bộ khoảng mặc định */
        public int getAvailableCapacity() {
            return availableCapacity;
        }
    }

    /** DTO chỉ đọc tách ngày có sách sớm nhất khỏi ngày dự kiến theo lựa chọn. */
    public static final class PickupEstimate {

        private final LocalDate earliestAvailableDate;
        private final LocalDate expectedPickupDate;
        private final LocalDate expectedEndDate;
        private final int availableCapacity;

        /**
         * Lưu kết quả tính lịch tại cùng một thời điểm để giao diện hiển thị nhất quán.
         *
         * @param earliestAvailableDate ngày sớm nhất còn slot cho người tiếp theo
         * @param expectedPickupDate ngày dự kiến sau khi xét ngày độc giả chọn
         * @param expectedEndDate ngày kết thúc không thuộc slot
         * @param availableCapacity số bản còn có thể đặt trong toàn bộ khoảng đã chọn
         */
        public PickupEstimate(LocalDate earliestAvailableDate, LocalDate expectedPickupDate,
                LocalDate expectedEndDate, int availableCapacity) {
            this.earliestAvailableDate = earliestAvailableDate;
            this.expectedPickupDate = expectedPickupDate;
            this.expectedEndDate = expectedEndDate;
            this.availableCapacity = availableCapacity;
        }

        /** @return ngày sớm nhất dự kiến có sách */
        public LocalDate getEarliestAvailableDate() {
            return earliestAvailableDate;
        }

        /** @return ngày dự kiến áp dụng cho ngày nhận độc giả đã chọn */
        public LocalDate getExpectedPickupDate() {
            return expectedPickupDate;
        }

        /** @return ngày kết thúc dự kiến, không thuộc khoảng slot */
        public LocalDate getExpectedEndDate() {
            return expectedEndDate;
        }

        /** @return số bản còn có thể đặt trong toàn bộ khoảng đã chọn */
        public int getAvailableCapacity() {
            return availableCapacity;
        }
    }

    /** DTO chỉ đọc trả về sau khi tạo yêu cầu thành công. */
    public static final class CreationResult {

        private final LocalDate requestedPickupDate;
        private final LocalDate expectedPickupDate;
        private final LocalDate expectedEndDate;

        /**
         * Lưu hai mốc ngày đã được xác nhận trong giao dịch tạo yêu cầu.
         *
         * @param requestedPickupDate ngày độc giả chọn
         * @param expectedPickupDate ngày hệ thống dự kiến
         * @param expectedEndDate ngày kết thúc không thuộc slot
         */
        public CreationResult(LocalDate requestedPickupDate, LocalDate expectedPickupDate,
                LocalDate expectedEndDate) {
            this.requestedPickupDate = requestedPickupDate;
            this.expectedPickupDate = expectedPickupDate;
            this.expectedEndDate = expectedEndDate;
        }

        /** @return ngày độc giả chọn */
        public LocalDate getRequestedPickupDate() {
            return requestedPickupDate;
        }

        /** @return ngày hệ thống dự kiến */
        public LocalDate getExpectedPickupDate() {
            return expectedPickupDate;
        }

        /** @return ngày kết thúc dự kiến, không thuộc khoảng slot */
        public LocalDate getExpectedEndDate() {
            return expectedEndDate;
        }
    }
}
