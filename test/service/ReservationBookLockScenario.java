/*
 * Kịch bản tích hợp chỉ đọc xác minh khóa đầu sách tuần tự hóa hai giao dịch đồng thời.
 * Lớp thuộc tầng kiểm thử và luôn rollback nên không làm thay đổi dữ liệu thư viện.
 */
package service;

import dao.BookDAO;
import dao.BookDAOImpl;
import dao.ReservationDAO;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import model.Book;

/**
 * Xác minh hai request cùng đầu sách không thể đồng thời đi vào vùng kiểm tra và ghi slot.
 */
public final class ReservationBookLockScenario {

    private static final long BLOCK_CHECK_MILLISECONDS = 300L;
    private static final long COMPLETION_TIMEOUT_SECONDS = 5L;

    /** Ngăn khởi tạo vì lớp chỉ cung cấp kịch bản tích hợp qua phương thức main. */
    private ReservationBookLockScenario() {
    }

    /**
     * Chạy hai giao dịch khóa cùng một hàng sách và yêu cầu giao dịch thứ hai phải chờ.
     *
     * @param arguments tham số dòng lệnh không được sử dụng
     * @throws Exception khi kết nối, đồng bộ luồng hoặc điều kiện khóa thất bại
     */
    public static void main(String[] arguments) throws Exception {
        ReservationDAO reservationDao = new ReservationDAO();
        int bookId = findActiveBookId();
        verifyScheduleCanBeRead(bookId);
        verifyImmediateAvailability(bookId);
        CountDownLatch firstLockAcquired = new CountDownLatch(1);
        CountDownLatch releaseFirstLock = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Void> firstRequest = executor.submit(() -> {
                try (Connection connection = reservationDao.openTransactionConnection()) {
                    connection.setAutoCommit(false);
                    try {
                        requireBookLock(reservationDao, connection, bookId);
                        firstLockAcquired.countDown();
                        if (!releaseFirstLock.await(
                                COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                            throw new AssertionError("Giao dịch giữ khóa không được giải phóng đúng hạn.");
                        }
                    } finally {
                        connection.rollback();
                    }
                }
                return null;
            });
            if (!firstLockAcquired.await(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new AssertionError("Giao dịch đầu tiên không lấy được khóa đầu sách.");
            }

            Future<Void> secondRequest = executor.submit(() -> {
                try (Connection connection = reservationDao.openTransactionConnection()) {
                    connection.setAutoCommit(false);
                    try {
                        requireBookLock(reservationDao, connection, bookId);
                    } finally {
                        connection.rollback();
                    }
                }
                return null;
            });
            assertSecondRequestIsWaiting(secondRequest);
            releaseFirstLock.countDown();
            firstRequest.get(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            secondRequest.get(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } finally {
            releaseFirstLock.countDown();
            executor.shutdownNow();
        }
    }

    /**
     * Chọn một đầu sách đang hoạt động mà không khóa hoặc thay đổi dữ liệu.
     *
     * @return mã đầu sách dùng cho kịch bản khóa
     * @throws Exception khi không thể đọc dữ liệu hoặc không có đầu sách phù hợp
     */
    private static int findActiveBookId() throws Exception {
        BookDAO bookDao = new BookDAOImpl();
        List<Book> books = bookDao.searchBooks(null, null, "id", "ASC", 1, 1);
        if (books.isEmpty()) {
            throw new AssertionError("Không có đầu sách hoạt động để kiểm tra khóa giao dịch.");
        }
        return books.get(0).getId();
    }

    /**
     * Đọc lịch thực tế để xác minh các truy vấn capacity chạy được trên schema hiện tại.
     *
     * @param bookId mã đầu sách dùng cho kiểm tra
     * @throws Exception khi không thể dựng lịch từ reservation, lượt mượn và bản sao
     */
    private static void verifyScheduleCanBeRead(int bookId) throws Exception {
        ReservationService reservationService = new ReservationService();
        LocalDate startDate = reservationService.businessToday();
        reservationService.isSlotAvailable(bookId, startDate,
                startDate.plusDays(BorrowService.LOAN_PERIOD_DAYS));
    }

    /**
     * Xác minh available được service tính lại trong giới hạn tổng số bản vật lý.
     *
     * @param bookId mã đầu sách dùng cho kiểm tra
     * @throws Exception khi không thể đọc hoặc tính capacity
     */
    private static void verifyImmediateAvailability(int bookId) throws Exception {
        Book book = new BookDAOImpl().findById(bookId);
        ReservationService reservationService = new ReservationService();
        reservationService.applyImmediateAvailability(book);
        if (book == null || book.getAvailable() < 0
                || book.getAvailable() > book.getQuantity()) {
            throw new AssertionError("Available sau khi tính slot nằm ngoài số bản vật lý.");
        }
    }

    /**
     * Yêu cầu DAO lấy khóa hàng cho một đầu sách còn hoạt động.
     *
     * @param reservationDao DAO sở hữu câu SQL khóa
     * @param connection kết nối đang tham gia giao dịch
     * @param bookId mã đầu sách cần khóa
     * @throws Exception khi không thể lấy khóa hoặc đầu sách không còn tồn tại
     */
    private static void requireBookLock(ReservationDAO reservationDao,
            Connection connection, int bookId) throws Exception {
        if (!reservationDao.lockBook(connection, bookId)) {
            throw new AssertionError("Không thể khóa đầu sách đang hoạt động.");
        }
    }

    /**
     * Xác minh request thứ hai chưa thể hoàn tất khi request thứ nhất còn giữ khóa.
     *
     * @param secondRequest tác vụ đang chờ khóa cùng đầu sách
     * @throws Exception khi tác vụ kết thúc sớm hoặc lỗi kiểm thử
     */
    private static void assertSecondRequestIsWaiting(Future<Void> secondRequest)
            throws Exception {
        try {
            secondRequest.get(BLOCK_CHECK_MILLISECONDS, TimeUnit.MILLISECONDS);
            throw new AssertionError(
                    "Hai giao dịch đã đồng thời đi qua khóa của cùng một đầu sách.");
        } catch (TimeoutException expected) {
            // Timeout tại đây chứng minh giao dịch thứ hai vẫn bị khóa hàng chặn đúng thiết kế.
        }
    }
}
