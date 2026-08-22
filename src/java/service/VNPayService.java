/*
 * Service quản lý nghiệp vụ xác thực và xử lý thanh toán khoản phạt qua cổng VNPay.
 */
package service;

import dao.FineDAO;
import java.math.BigDecimal;
import model.Fine;

/**
 * Cung cấp các hàm kiểm tra điều kiện thanh toán và cập nhật kết quả giao dịch VNPay.
 * <p>Lớp này chịu trách nhiệm độc lập cho tính năng tích hợp VNPay Payment Gateway.</p>
 */
public class VNPayService {

    private final FineDAO fineDao;

    /**
     * Khởi tạo service VNPay với DAO truy vấn khoản phạt.
     */
    public VNPayService() {
        this.fineDao = new FineDAO();
    }

    
    public Fine validateFineForPayment(int fineId, int userId) throws Exception {
        // Step 1: Validate sự tồn tại và quyền sở hữu khoản phạt (chính chủ)
        Fine fine = fineDao.findByIdAndUserId(fineId, userId);
        if (fine == null) {
            throw new IllegalArgumentException("Khoản phạt không tồn tại hoặc bạn không có quyền truy cập.");
        }

        // Step 2: Validate trạng thái khoản phạt (phải là UNPAID)
        if (!"UNPAID".equalsIgnoreCase(fine.getStatus())) {
            throw new IllegalArgumentException("Khoản phạt này đã được thanh toán hoặc được miễn.");
        }

        // Step 3: Validate ngày trả sách (bắt buộc độc giả phải trả sách trước khi nộp phạt)
        if (fine.getBorrowRecord() == null || fine.getBorrowRecord().getReturnDate() == null) {
            throw new IllegalArgumentException("Bạn cần phải trả sách cho thư viện trước khi thực hiện thanh toán khoản phạt này.");
        }

        // Step 4: Validate số tiền phạt (phải lớn hơn 0)
        if (fine.getAmount() == null || fine.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền phạt không hợp lệ để thanh toán.");
        }

        return fine;
    }

    /**
     * Lấy chi tiết khoản phạt theo ID phục vụ callback hoặc kiểm tra đơn hàng VNPay.
     *
     * @param fineId mã khoản phạt
     * @return khoản phạt hoặc {@code null} nếu không tìm thấy
     * @throws Exception khi truy vấn thất bại
     */
    public Fine getFineById(int fineId) throws Exception {
        return fineId > 0 ? fineDao.findById(fineId) : null;
    }

    /**
     * Xử lý xác nhận thanh toán khoản phạt thành công qua cổng thanh toán VNPay.
     *
     * @param fineId mã khoản phạt
     * @param paymentMethod phương thức thanh toán ("ONLINE")
     * @param paymentNote ghi chú thông tin giao dịch VNPay
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
}
