/* Ngoại lệ service biểu diễn lỗi nghiệp vụ khi độc giả tạo yêu cầu đặt trước sách. */
package service;

/**
 * Mang thông báo validation an toàn từ tầng service đến controller của luồng đặt trước.
 */
public class ReservationValidationException extends Exception {

    /**
     * Khởi tạo lỗi nghiệp vụ có thể hiển thị trực tiếp cho độc giả.
     *
     * @param message nội dung lỗi thân thiện, không chứa chi tiết lưu trữ
     */
    public ReservationValidationException(String message) {
        super(message);
    }
}
