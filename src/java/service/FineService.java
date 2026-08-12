/*
 * Service tổng hợp danh sách, số tiền và trạng thái khoản phạt của độc giả.
 */
package service;

import dao.FineDAO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import model.Fine;

/**
 * Cung cấp nghiệp vụ đọc khoản phạt cá nhân qua {@link FineDAO}.
 */
public class FineService {

    private static final Set<String> SUPPORTED_STATUSES = Set.of("UNPAID", "PAID", "WAIVED");
    private final FineDAO fineDao;

    /** Khởi tạo service với DAO khoản phạt hiện có. */
    public FineService() {
        this.fineDao = new FineDAO();
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
        return fineId > 0 ? fineDao.findByIdAndUserId(fineId, userId) : null;
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
