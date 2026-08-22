/* Service quản lý ngày nhận dự kiến, hàng chờ và thông báo của luồng đặt trước sách. */
package service;

import dao.BookDAO;
import dao.BookDAOImpl;
import dao.BorrowRecordDAO;
import dao.FineDAO;
import dao.ReservationDAO;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.PriorityQueue;
import model.Book;
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
        List<LocalDate> availabilityDates
                = reservationDao.findProjectedAvailabilityDates(
                        bookId, BorrowService.LOAN_PERIOD_DAYS);
        if (availabilityDates.isEmpty()) {
            throw new ReservationValidationException(
                    "Chưa có bản sách khả dụng hoặc lịch trả hợp lệ để nhận đặt trước.");
        }
        PickupEstimate estimate = calculatePickupEstimate(
                bookId, today, availabilityDates);
        LocalDate earliestAvailableDate = estimate.getEarliestAvailableDate();
        if (earliestAvailableDate.isAfter(maximumDate)) {
            throw new ReservationValidationException(
                    "Ngày sớm nhất dự kiến có sách nằm ngoài giới hạn đặt trước 1 năm.");
        }
        LocalDate requestedDate = earliestAvailableDate;
        int waitingCount = reservationDao.countActiveByBook(bookId);
        return new CreationInfo(book, waitingCount, waitingCount + 1,
                earliestAvailableDate, maximumDate, requestedDate,
                earliestAvailableDate, requestedDate);
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
        List<LocalDate> availabilityDates
                = reservationDao.findProjectedAvailabilityDates(
                        bookId, BorrowService.LOAN_PERIOD_DAYS);
        if (availabilityDates.isEmpty()) {
            throw new ReservationValidationException(
                    "Chưa có bản sách khả dụng hoặc lịch trả phù hợp để dự kiến ngày nhận.");
        }
        PickupEstimate estimate = calculatePickupEstimate(
                bookId, requestedPickupDate, availabilityDates);
        validateRequestedDateAgainstAvailability(
                requestedPickupDate, estimate.getEarliestAvailableDate());
        return estimate;
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
        List<LocalDate> availabilityDates
                = reservationDao.findProjectedAvailabilityDates(
                        bookId, BorrowService.LOAN_PERIOD_DAYS);
        if (availabilityDates.isEmpty()) {
            throw new ReservationValidationException(
                    "Chưa có bản sách khả dụng hoặc lịch trả phù hợp; hiện chưa thể đặt trước.");
        }
        PickupEstimate estimate = calculatePickupEstimate(
                bookId, requestedPickupDate, availabilityDates);
        validateRequestedDateAgainstAvailability(
                requestedPickupDate, estimate.getEarliestAvailableDate());
        LocalDate expectedPickupDate = estimate.getExpectedPickupDate();
        if (!reservationDao.createWaiting(
                userId, bookId, requestedPickupDate, expectedPickupDate)) {
            throw new ReservationValidationException(
                    "Không thể đặt trước vì trạng thái sách vừa thay đổi hoặc bạn đã có yêu cầu đang hoạt động.");
        }
        return new CreationResult(requestedPickupDate, expectedPickupDate);
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
        return reservationDao.findByUserId(userId);
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
        if (book.getAvailable() <= 0 && !book.isReservable()) {
            throw new ReservationValidationException(
                    "Hiện không có bản sách khả dụng hoặc lượt mượn đúng hạn để dự kiến ngày nhận.");
        }
        if (reservationDao.findActive(userId, bookId) != null) {
            throw new ReservationValidationException("Bạn đã có yêu cầu đặt trước đang hoạt động cho sách này.");
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
     * Bảo đảm ngày độc giả muốn nhận không sớm hơn slot đầu tiên còn lại của hàng chờ.
     *
     * @param requestedPickupDate ngày độc giả chọn
     * @param earliestAvailableDate ngày sớm nhất hệ thống dự kiến có sách
     * @throws ReservationValidationException khi ngày chọn sớm hơn ngày có sách
     */
    private void validateRequestedDateAgainstAvailability(LocalDate requestedPickupDate,
            LocalDate earliestAvailableDate) throws ReservationValidationException {
        if (requestedPickupDate.isBefore(earliestAvailableDate)) {
            throw new ReservationValidationException(
                    "Ngày muốn nhận sách không được trước ngày dự kiến có sách: "
                            + earliestAvailableDate + ".");
        }
    }

    /**
     * Dựng lịch còn lại sau hàng chờ rồi tách ngày có sách sớm nhất và ngày theo lựa chọn mới.
     *
     * @param bookId mã đầu sách
     * @param requestedPickupDate ngày mong muốn của yêu cầu mới
     * @param initialAvailabilityDates các slot gốc từ bản đang rảnh hoặc hạn trả
     * @return hai ngày dự kiến được tính từ cùng một ảnh chụp lịch slot
     * @throws Exception khi không thể đọc các yêu cầu đang chờ
     */
    private PickupEstimate calculatePickupEstimate(int bookId, LocalDate requestedPickupDate,
            List<LocalDate> initialAvailabilityDates) throws Exception {
        PriorityQueue<LocalDate> slots = new PriorityQueue<>(initialAvailabilityDates);
        for (ReservationRecord waiting : reservationDao.findWaitingByBook(bookId)) {
            LocalDate desiredDate = waiting.getRequestedPickupDate();
            if (desiredDate == null) {
                desiredDate = waiting.getExpectedPickupDate() == null
                        ? businessToday() : waiting.getExpectedPickupDate();
            }
            allocateSlot(slots, desiredDate);
        }
        LocalDate earliestAvailableDate = slots.element();
        LocalDate expectedPickupDate = earliestAvailableDate.isBefore(requestedPickupDate)
                ? requestedPickupDate : earliestAvailableDate;
        return new PickupEstimate(earliestAvailableDate, expectedPickupDate);
    }

    /**
     * Lấy bản có lịch sớm nhất, tôn trọng ngày mong muốn và tạo chu kỳ trả kế tiếp.
     *
     * @param slots hàng ưu tiên ngày có thể nhận của từng bản
     * @param desiredDate ngày không được phân bổ sớm hơn
     * @return ngày đã phân bổ
     */
    private LocalDate allocateSlot(PriorityQueue<LocalDate> slots, LocalDate desiredDate) {
        LocalDate availableDate = slots.remove();
        LocalDate allocatedDate = availableDate.isBefore(desiredDate) ? desiredDate : availableDate;
        slots.add(allocatedDate.plusDays(BorrowService.LOAN_PERIOD_DAYS));
        return allocatedDate;
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
    private LocalDate businessToday() {
        return LocalDate.now(BUSINESS_ZONE);
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
         */
        public CreationInfo(Book book, int waitingCount, int expectedPosition,
                LocalDate minimumPickupDate, LocalDate maximumPickupDate,
                LocalDate requestedPickupDate, LocalDate earliestAvailableDate,
                LocalDate expectedPickupDate) {
            this.book = book;
            this.waitingCount = waitingCount;
            this.expectedPosition = expectedPosition;
            this.minimumPickupDate = minimumPickupDate;
            this.maximumPickupDate = maximumPickupDate;
            this.requestedPickupDate = requestedPickupDate;
            this.earliestAvailableDate = earliestAvailableDate;
            this.expectedPickupDate = expectedPickupDate;
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
    }

    /** DTO chỉ đọc tách ngày có sách sớm nhất khỏi ngày dự kiến theo lựa chọn. */
    public static final class PickupEstimate {

        private final LocalDate earliestAvailableDate;
        private final LocalDate expectedPickupDate;

        /**
         * Lưu kết quả tính lịch tại cùng một thời điểm để giao diện hiển thị nhất quán.
         *
         * @param earliestAvailableDate ngày sớm nhất còn slot cho người tiếp theo
         * @param expectedPickupDate ngày dự kiến sau khi xét ngày độc giả chọn
         */
        public PickupEstimate(LocalDate earliestAvailableDate, LocalDate expectedPickupDate) {
            this.earliestAvailableDate = earliestAvailableDate;
            this.expectedPickupDate = expectedPickupDate;
        }

        /** @return ngày sớm nhất dự kiến có sách */
        public LocalDate getEarliestAvailableDate() {
            return earliestAvailableDate;
        }

        /** @return ngày dự kiến áp dụng cho ngày nhận độc giả đã chọn */
        public LocalDate getExpectedPickupDate() {
            return expectedPickupDate;
        }
    }

    /** DTO chỉ đọc trả về sau khi tạo yêu cầu thành công. */
    public static final class CreationResult {

        private final LocalDate requestedPickupDate;
        private final LocalDate expectedPickupDate;

        /**
         * Lưu hai mốc ngày đã được xác nhận trong giao dịch tạo yêu cầu.
         *
         * @param requestedPickupDate ngày độc giả chọn
         * @param expectedPickupDate ngày hệ thống dự kiến
         */
        public CreationResult(LocalDate requestedPickupDate, LocalDate expectedPickupDate) {
            this.requestedPickupDate = requestedPickupDate;
            this.expectedPickupDate = expectedPickupDate;
        }

        /** @return ngày độc giả chọn */
        public LocalDate getRequestedPickupDate() {
            return requestedPickupDate;
        }

        /** @return ngày hệ thống dự kiến */
        public LocalDate getExpectedPickupDate() {
            return expectedPickupDate;
        }
    }
}
