/*
 * Service tổng hợp danh sách, số tiền và trạng thái khoản phạt của độc giả.
 */
package service;

import dao.FineDAO;
import dao.BorrowRecordDAO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import model.Fine;
import model.BorrowRecord;

/**
 * Cung cấp nghiệp vụ lập khoản phạt và đọc khoản phạt cá nhân qua {@link FineDAO}.
 */
public class FineService {

    private static final Set<String> SUPPORTED_STATUSES = Set.of("UNPAID", "PAID", "WAIVED");
    private static final BigDecimal OVERDUE_DAILY_RATE = BigDecimal.valueOf(5_000);
    private final FineDAO fineDao;
    private final BorrowRecordDAO borrowRecordDao;

    /** Khởi tạo service với DAO khoản phạt hiện có. */
    public FineService() {
        this.fineDao = new FineDAO();
        this.borrowRecordDao = new BorrowRecordDAO();
    }

    /**
     * Lập khoản phạt hỏng hoặc mất sách từ giá sách lưu trong cơ sở dữ liệu.
     *
     * @param borrowRecordId mã lượt mượn bị phạt
     * @param userId mã độc giả do biểu mẫu gửi lên để đối chiếu
     * @param bookCondition tình trạng {@code DAMAGED} hoặc {@code LOST}
     * @param amount số tiền đã được tự động tính và có thể được thủ thư điều chỉnh
     * @param reason lý do bổ sung do thủ thư nhập
     * @return {@code true} khi khoản phạt được tạo
     * @throws IllegalArgumentException khi dữ liệu không hợp lệ hoặc không khớp lượt mượn
     * @throws Exception khi không thể đọc hoặc ghi dữ liệu
     */
    public boolean createBookConditionFine(int borrowRecordId, int userId,
            String bookCondition, BigDecimal amount, String reason) throws Exception {
        BorrowRecord record = borrowRecordDao.findById(borrowRecordId);
        if (record == null || record.getUserId() != userId || record.getBook() == null) {
            throw new IllegalArgumentException("Lượt mượn hoặc độc giả không hợp lệ.");
        }
        if (!"BORROWED".equals(record.getStatus()) && !"OVERDUE".equals(record.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể phạt lượt mượn đang mượn hoặc quá hạn.");
        }
        String normalizedCondition = bookCondition == null
                ? "" : bookCondition.trim().toUpperCase(Locale.ROOT);
        if (!"DAMAGED".equals(normalizedCondition) && !"LOST".equals(normalizedCondition)) {
            throw new IllegalArgumentException("Tình trạng cuốn sách không hợp lệ.");
        }
        Integer bookPrice = record.getBook().getPrice();
        if (bookPrice == null || bookPrice < 0) {
            throw new IllegalArgumentException("Cuốn sách chưa có giá hợp lệ để tính tiền phạt.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền phạt phải lớn hơn 0.");
        }
        String normalizedReason = reason == null ? "" : reason.trim();
        if (normalizedReason.isEmpty() || normalizedReason.length() > 255) {
            throw new IllegalArgumentException("Lý do phạt phải có từ 1 đến 255 ký tự.");
        }
        Fine fine = new Fine(borrowRecordId, userId, amount, 0, normalizedReason, "UNPAID");
        fine.setBookCondition(normalizedCondition);
        return fineDao.createFine(fine);
    }

    /**
     * Chuẩn hóa trạng thái filter; giá trị không hợp lệ được xem là tất cả.
     *
     * @param status trạng thái từ query string
     * @return trạng thái chuẩn hoặc {@code null}
     */
    public String normalizeStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) return null;
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return SUPPORTED_STATUSES.contains(normalized) ? normalized : null;
    }

    /**
     * Lấy danh sách đã lọc và số liệu tổng hợp trên toàn bộ fine của người dùng.
     *
     * @param userId mã người dùng đăng nhập
     * @param status trạng thái filter đã chuẩn hóa
     * @param keyword từ khóa tìm kiếm
     * @return dữ liệu cho trang danh sách
     * @throws Exception khi DAO không thể đọc dữ liệu
     */
    public FinePageData getMyFines(int userId, String status, String keyword) throws Exception {
        fineDao.synchronizeOverdueFines(userId, OVERDUE_DAILY_RATE);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.length() > 100) {
            normalizedKeyword = normalizedKeyword.substring(0, 100);
        }
        List<Fine> allFines = fineDao.searchByUser(userId, null, "");
        List<Fine> filteredFines = fineDao.searchByUser(userId, status, normalizedKeyword);
        BigDecimal unpaidAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        int unpaidCount = 0;
        for (Fine fine : allFines) {
            BigDecimal amount = fine.getAmount() == null ? BigDecimal.ZERO : fine.getAmount();
            if ("UNPAID".equalsIgnoreCase(fine.getStatus())) {
                unpaidAmount = unpaidAmount.add(amount);
                unpaidCount++;
            } else if ("PAID".equalsIgnoreCase(fine.getStatus())) {
                paidAmount = paidAmount.add(amount);
            }
        }
        return new FinePageData(filteredFines, allFines.size(), unpaidAmount, paidAmount, unpaidCount);
    }

    /**
     * Lấy chi tiết fine đồng thời kiểm tra ownership tại câu truy vấn.
     *
     * @param fineId mã khoản phạt
     * @param userId mã người dùng đăng nhập
     * @return khoản phạt thuộc người dùng hoặc {@code null}
     * @throws Exception khi DAO không thể đọc dữ liệu
     */
    public Fine getOwnedFine(int fineId, int userId) throws Exception {
        fineDao.synchronizeOverdueFines(userId, OVERDUE_DAILY_RATE);
        return fineId > 0 ? fineDao.findByIdAndUserId(fineId, userId) : null;
    }

    /**
     * Lấy khoản phạt theo ID.
     *
     * @param fineId mã khoản phạt
     * @return khoản phạt hoặc {@code null}
     * @throws Exception khi truy vấn thất bại
     */
    public Fine getFineById(int fineId) throws Exception {
        return fineId > 0 ? fineDao.findById(fineId) : null;
    }

    /**
     * Xử lý xác nhận thanh toán khoản phạt thành công qua cổng thanh toán.
     *
     * @param fineId mã khoản phạt
     * @param paymentMethod phương thức thanh toán (ví dụ "VNPay")
     * @param paymentNote ghi chú thông tin giao dịch
     * @param operator tên người/hệ thống thực hiện xác nhận
     * @return {@code true} khi cập nhật thành công
     * @throws Exception khi truy vấn hoặc cập nhật cơ sở dữ liệu thất bại
     */
    public boolean processPaymentSuccess(int fineId, String paymentMethod, String paymentNote, String operator) throws Exception {
        Fine fine = fineDao.findById(fineId);
        if (fine == null) {
            return false;
        }
        if ("PAID".equalsIgnoreCase(fine.getStatus())) {
            return true;
        }
        return fineDao.updateStatus(fineId, "PAID", paymentMethod, paymentNote, operator);
    }

    /** Đồng bộ tiền phạt quá hạn cho toàn bộ lượt mượn để trang quản lý hiển thị số mới nhất. */
    public void synchronizeAllOverdueFines() throws Exception {
        fineDao.synchronizeOverdueFines(null, OVERDUE_DAILY_RATE);
    }

    /** DTO chỉ đọc phục vụ trang danh sách fine cá nhân. */
    public static final class FinePageData {
        private final List<Fine> fines;
        private final int totalFines;
        private final BigDecimal unpaidAmount;
        private final BigDecimal paidAmount;
        private final int unpaidCount;

        /**
         * Tạo dữ liệu trang từ danh sách và các số liệu đã tính.
         *
         * @param fines kết quả filter/search
         * @param totalFines tổng số fine
         * @param unpaidAmount tổng tiền chưa trả
         * @param paidAmount tổng tiền đã trả
         * @param unpaidCount số fine chưa trả
         */
        public FinePageData(List<Fine> fines, int totalFines, BigDecimal unpaidAmount,
                BigDecimal paidAmount, int unpaidCount) {
            this.fines = fines; this.totalFines = totalFines; this.unpaidAmount = unpaidAmount;
            this.paidAmount = paidAmount; this.unpaidCount = unpaidCount;
        }
        /** @return kết quả fine đã lọc */ public List<Fine> getFines() { return fines; }
        /** @return tổng số fine */ public int getTotalFines() { return totalFines; }
        /** @return tổng tiền chưa trả */ public BigDecimal getUnpaidAmount() { return unpaidAmount; }
        /** @return tổng tiền đã trả */ public BigDecimal getPaidAmount() { return paidAmount; }
        /** @return số fine chưa trả */ public int getUnpaidCount() { return unpaidCount; }
    }
}
