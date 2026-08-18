/*
 * Lớp tiện ích cấu hình và mã hóa dữ liệu cho cổng thanh toán VNPay Sandbox.
 */
package utils;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Quản lý thông tin kết nối VNPay (vnp_TmnCode, vnp_HashSecret, vnp_Url)
 * và cung cấp các hàm hỗ trợ tạo chữ ký checksum HMAC-SHA512.
 */
public class VNPayConfig {

    public static final String VNP_TMN_CODE = "9FZ51WZV";
    public static final String VNP_HASH_SECRET = "QLAYGQVKQQSMIECGADAZHVLKRXWSDJHK";
    public static final String VNP_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND = "pay";
    public static final String VNP_ORDER_TYPE = "other";

    /**
     * Tính mã băm HMAC-SHA512 cho chuỗi dữ liệu đầu vào.
     *
     * @param key chuỗi khóa bí mật (vnp_HashSecret)
     * @param data dữ liệu cần tạo checksum
     * @return chuỗi băm dạng hex viết thường hoặc chuỗi rỗng nếu có lỗi
     */
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                return "";
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Sắp xếp các tham số theo thứ tự bảng chữ cái và tạo payment URL hoàn chỉnh.
     *
     * @param vnpParams bảng các tham số vnp_*
     * @return URL thanh toán chuyển hướng sang VNPay Sandbox
     */
    public static String buildPaymentUrl(Map<String, String> vnpParams) {
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                    query.append('&');
                }
                try {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                         .append('=')
                         .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception ignored) {}
            }
        }
        String vnpSecureHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());
        return VNP_PAY_URL + "?" + query.toString() + "&vnp_SecureHash=" + vnpSecureHash;
    }

    /**
     * Xác thực chữ ký HMAC-SHA512 từ dữ liệu phản hồi của VNPay.
     *
     * @param fields các tham số phản hồi từ VNPay
     * @param secureHash chữ ký vnp_SecureHash do VNPay gửi về
     * @return {@code true} nếu chữ ký hợp lệ
     */
    public static boolean verifySignature(Map<String, String> fields, String secureHash) {
        if (secureHash == null || secureHash.trim().isEmpty()) {
            return false;
        }
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        fieldNames.remove("vnp_SecureHash");
        fieldNames.remove("vnp_SecureHashType");
        Collections.sort(fieldNames);

        // 1. Mã hóa URL mã băm chuẩn theo đặc tả VNPay 2.1.0
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                try {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception ignored) {}
            }
        }
        String calculatedHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());
        if (calculatedHash.equalsIgnoreCase(secureHash)) {
            return true;
        }

        // 2. Dự phòng: Tính băm trên chuỗi giá trị chưa URL-encode
        StringBuilder rawHashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                if (rawHashData.length() > 0) {
                    rawHashData.append('&');
                }
                rawHashData.append(fieldName).append('=').append(fieldValue);
            }
        }
        String calculatedRawHash = hmacSHA512(VNP_HASH_SECRET, rawHashData.toString());
        return calculatedRawHash.equalsIgnoreCase(secureHash);
    }

    /**
     * Lấy địa chỉ IP của máy khách gửi yêu cầu HTTP.
     *
     * @param request yêu cầu HTTP
     * @return địa chỉ IP chuẩn hóa
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
        } catch (Exception e) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }

    /**
     * Tạo ngẫu nhiên một chuỗi số có độ dài cho trước.
     *
     * @param len độ dài chuỗi số
     * @return chuỗi số ngẫu nhiên
     */
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
