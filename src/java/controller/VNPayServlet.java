/*
 * Controller HTTP tiếp nhận thanh toán, xử lý redirect return và IPN callback từ VNPAY.
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Fine;
import model.User;
import service.VNPayService;
import utils.VNPayConfig;

/**
 * Điều phối các đường dẫn liên quan đến thanh toán VNPay Sandbox:
 * <ul>
 *   <li>{@code /vnpay-pay}: Khởi tạo giao dịch thanh toán khoản phạt.</li>
 *   <li>{@code /vnpay-return}: Tiếp nhận và xử lý kết quả khi độc giả quay lại từ VNPay.</li>
 *   <li>{@code /vnpay-ipn}: Phản hồi IPN callback Server-to-Server từ cổng VNPay.</li>
 * </ul>
 */
@WebServlet(name = "VNPayServlet", urlPatterns = {"/vnpay-pay", "/vnpay-return", "/vnpay-ipn"})
public class VNPayServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(VNPayServlet.class.getName());
    private final VNPayService vnPayService = new VNPayService();

    /**
     * Tiếp nhận yêu cầu GET theo servlet path tương ứng.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @throws ServletException khi xử lý Servlet thất bại
     * @throws IOException khi gửi dữ liệu phản hồi thất bại
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String path = request.getServletPath();

        try {
            if ("/vnpay-pay".equals(path)) {
                handlePayment(request, response);
            } else if ("/vnpay-return".equals(path)) {
                handleReturn(request, response);
            } else if ("/vnpay-ipn".equals(path)) {
                handleIpn(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Lỗi xử lý yêu cầu VNPay servlet path=" + path, exception);
            if ("/vnpay-ipn".equals(path)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"RspCode\":\"99\",\"Message\":\"Unknown Error\"}");
            } else {
                String detail = exception.getMessage() != null ? " (" + exception.getMessage() + ")" : "";
                setFlashMessage(request, "fineErrorMessage", "Đã xảy ra lỗi trong quá trình xử lý thanh toán VNPay" + detail + ".");
                response.sendRedirect(request.getContextPath() + "/fine/my");
            }
        }
    }

    /**
     * Xử lý khởi tạo liên kết thanh toán VNPay cho khoản phạt của độc giả.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @throws Exception khi đọc fine hoặc khởi tạo URL thất bại
     */
    private void handlePayment(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int fineId;
        try {
            fineId = Integer.parseInt(request.getParameter("fineId"));
        } catch (NumberFormatException e) {
            setFlashMessage(request, "fineErrorMessage", "Mã khoản phạt không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/fine/my");
            return;
        }

        Fine fine;
        try {
            fine = vnPayService.validateFineForPayment(fineId, user.getId());
        } catch (IllegalArgumentException e) {
            setFlashMessage(request, "fineErrorMessage", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/fine/my");
            return;
        }

        BigDecimal amount = fine.getAmount();
        long amountInCents = amount.multiply(new BigDecimal(100)).longValue();
        String txnRef = fine.getId() + "_" + System.currentTimeMillis();
        String orderInfo = "Thanh toan khoan phat F" + fine.getId();

        String returnUrl = request.getScheme() + "://" + request.getServerName() + ":"
                + request.getServerPort() + request.getContextPath() + "/vnpay-return";

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        cld.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cld.getTime());

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", VNPayConfig.VNP_VERSION);
        vnpParams.put("vnp_Command", VNPayConfig.VNP_COMMAND);
        vnpParams.put("vnp_TmnCode", VNPayConfig.VNP_TMN_CODE);
        vnpParams.put("vnp_Amount", String.valueOf(amountInCents));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", VNPayConfig.VNP_ORDER_TYPE);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", VNPayConfig.getIpAddress(request));
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        String paymentUrl = VNPayConfig.buildPaymentUrl(vnpParams);
        LOGGER.info("Tạo VNPay payment URL thành công cho fineId=" + fineId + ", txnRef=" + txnRef);
        response.sendRedirect(paymentUrl);
    }

    /**
     * Tiếp nhận kết quả thanh toán từ VNPay khi trình duyệt độc giả quay lại.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @throws Exception khi xác thực chữ ký hoặc cập nhật dữ liệu thất bại
     */
    private void handleReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> fields = extractVnPayParameters(request);
        String secureHash = request.getParameter("vnp_SecureHash");
        boolean isValidSignature = VNPayConfig.verifySignature(fields, secureHash);

        String txnRef = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionNo = request.getParameter("vnp_TransactionNo");

        int fineId = parseFineIdFromTxnRef(txnRef);

        if (isValidSignature) {
            if ("00".equals(responseCode)) {
                if (fineId > 0) {
                    boolean updated = vnPayService.processPaymentSuccess(
                            fineId, "ONLINE", "Thanh toán thành công qua VNPay (Mã GD: " + transactionNo + ")", "VNPay Client Return");
                    if (updated) {
                        // Ghi audit log thanh toán phạt online thành công
                        model.Fine paidFine = vnPayService.getFineById(fineId);
                        int targetUserId = paidFine != null ? paidFine.getUserId() : 0;
                        utils.AuditLogger.logFinePaidOnline(targetUserId, fineId, transactionNo);
                        setFlashMessage(request, "fineSuccessMessage",
                                "Thanh toán khoản phạt #F" + fineId + " thành công qua VNPay! Mã giao dịch: " + transactionNo);
                    } else {
                        setFlashMessage(request, "fineErrorMessage", "Khoản phạt đã được ghi nhận thanh toán trước đó.");
                    }
                } else {
                    setFlashMessage(request, "fineErrorMessage", "Không xác định được mã khoản phạt thanh toán.");
                }
            } else {
                setFlashMessage(request, "fineErrorMessage",
                        "Thanh toán khoản phạt qua VNPay không thành công hoặc bị hủy (Mã phản hồi: " + responseCode + ").");
            }
        } else {
            LOGGER.warning("Chữ ký VNPay Return không hợp lệ cho txnRef=" + txnRef);
            setFlashMessage(request, "fineErrorMessage", "Chữ ký xác thực thanh toán VNPay không hợp lệ.");
        }

        response.sendRedirect(request.getContextPath() + "/fine/my");
    }

    /**
     * Tiếp nhận callback Server-to-Server (IPN) từ VNPay để cập nhật trạng thái giao dịch.
     *
     * @param request yêu cầu HTTP
     * @param response phản hồi HTTP
     * @throws Exception khi đọc hoặc ghi dữ liệu IPN thất bại
     */
    private void handleIpn(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> fields = extractVnPayParameters(request);
        String secureHash = request.getParameter("vnp_SecureHash");
        boolean isValidSignature = VNPayConfig.verifySignature(fields, secureHash);

        if (!isValidSignature) {
            LOGGER.warning("VNPay IPN checksum không hợp lệ.");
            response.getWriter().write("{\"RspCode\":\"97\",\"Message\":\"Invalid Checksum\"}");
            return;
        }

        String txnRef = request.getParameter("vnp_TxnRef");
        int fineId = parseFineIdFromTxnRef(txnRef);
        Fine fine = vnPayService.getFineById(fineId);

        if (fine == null) {
            response.getWriter().write("{\"RspCode\":\"01\",\"Message\":\"Order not found\"}");
            return;
        }

        String vnpAmountStr = request.getParameter("vnp_Amount");
        long vnpAmount = vnpAmountStr != null ? Long.parseLong(vnpAmountStr) : 0;
        long expectedAmount = fine.getAmount() != null ? fine.getAmount().multiply(new BigDecimal(100)).longValue() : 0;

        if (vnpAmount != expectedAmount) {
            response.getWriter().write("{\"RspCode\":\"04\",\"Message\":\"Invalid Amount\"}");
            return;
        }

        if ("PAID".equalsIgnoreCase(fine.getStatus())) {
            response.getWriter().write("{\"RspCode\":\"02\",\"Message\":\"Order already confirmed\"}");
            return;
        }

        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionNo = request.getParameter("vnp_TransactionNo");

        if ("00".equals(responseCode)) {
            vnPayService.processPaymentSuccess(
                    fineId, "ONLINE", "Xác nhận thanh toán qua VNPay IPN (Mã GD: " + transactionNo + ")", "VNPay IPN Callback");
        }

        response.getWriter().write("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
    }

    /**
     * Trích xuất các tham số dạng key-value từ request do VNPay gửi về.
     *
     * @param request yêu cầu HTTP
     * @return bảng ánh xạ tham số
     */
    private Map<String, String> extractVnPayParameters(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String paramName = params.nextElement();
            String paramValue = request.getParameter(paramName);
            if (paramValue != null && !paramValue.trim().isEmpty()) {
                fields.put(paramName, paramValue);
            }
        }
        return fields;
    }

    /**
     * Đọc mã fineId từ chuỗi giao dịch tham chiếu {@code txnRef} (dạng fineId_timestamp).
     *
     * @param txnRef chuỗi mã giao dịch tham chiếu
     * @return mã khoản phạt hoặc -1 nếu không hợp lệ
     */
    private int parseFineIdFromTxnRef(String txnRef) {
        if (txnRef == null || txnRef.trim().isEmpty()) {
            return -1;
        }
        try {
            String[] parts = txnRef.split("_");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Lấy thông tin tài khoản người dùng đã đăng nhập từ session.
     *
     * @param request yêu cầu HTTP
     * @return người dùng hoặc {@code null} nếu chưa đăng nhập
     */
    private User getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedUser") instanceof User) {
            return (User) session.getAttribute("loggedUser");
        }
        return null;
    }

    /**
     * Ghi thông tin thông báo flash ngắn hạn vào session.
     *
     * @param request yêu cầu HTTP
     * @param key tên biến thông báo
     * @param message nội dung thông báo
     */
    private void setFlashMessage(HttpServletRequest request, String key, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute(key, message);
    }
}
