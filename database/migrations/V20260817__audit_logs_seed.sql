-- =============================================================================
-- Migration: Tạo bảng audit_logs và nạp dữ liệu mẫu (Sample Seed Data)
-- =============================================================================

CREATE TABLE IF NOT EXISTS `audit_logs` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `action` VARCHAR(100) NOT NULL,
    `performed_by` VARCHAR(100) NOT NULL,
    `target_user_id` INT DEFAULT 0,
    `detail` TEXT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_action` (`action`),
    INDEX `idx_performed_by` (`performed_by`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Nạp dữ liệu mẫu đa dạng các hành vi can thiệp hệ thống
INSERT INTO `audit_logs` (`action`, `performed_by`, `target_user_id`, `detail`, `created_at`) VALUES
('WAIVE_FINE', 'admin', 2, 'fineId=14, amount=50000 VND, note=Độc giả có giấy xác nhận nghỉ ốm nằm viện điều trị — Miễn giảm 100% tiền phạt', NOW() - INTERVAL 1 HOUR),
('OVERRIDE_BORROW_LIMIT', 'librarian1', 5, 'userId=5, currentCount=5, maxLimit=5 — Override approved cho mượn thêm cuốn Clean Code (Lý do: Giảng viên hướng dẫn đồ án tốt nghiệp)', NOW() - INTERVAL 3 HOUR),
('LOCK_ACCOUNT', 'admin', 8, 'reason=Phát hiện hành vi cố tình làm rách và tráo đổi mã vạch sách mượn nhiều lần', NOW() - INTERVAL 6 HOUR),
('APPLY_DAMAGE_FINE', 'librarian1', 3, 'borrowRecordId=19, amount=30000 VND, reason=Sách bị ướt và rách 3 trang phụ lục cuối cuốn', NOW() - INTERVAL 12 HOUR),
('CONFIRM_RESERVATION', 'librarian2', 4, 'reservationId=7, bookTitle=Nhập môn Trí tuệ nhân tạo — Xác nhận giữ sách tại quầy trong 48h', NOW() - INTERVAL 1 DAY),
('UNLOCK_ACCOUNT', 'admin', 8, 'reason=Độc giả đã lên văn phòng thư viện làm bản cam kết và bồi thường thiệt hại', NOW() - INTERVAL 1 DAY - INTERVAL 2 HOUR),
('APPLY_LOST_FINE', 'librarian1', 6, 'borrowRecordId=22, amount=150000 VND, reason=Báo mất sách Kỹ thuật lập trình Java nâng cao — Phạt 100% giá bìa sách', NOW() - INTERVAL 2 DAY),
('OVERRIDE_BORROW_LIMIT', 'admin', 9, 'userId=9, currentCount=5, maxLimit=5 — Override duyệt mượn thêm tài liệu nghiên cứu khoa học cấp trường', NOW() - INTERVAL 2 DAY - INTERVAL 5 HOUR),
('WAIVE_FINE', 'admin', 7, 'fineId=8, amount=20000 VND, note=Hệ thống thư viện gặp sự cố đường truyền mạng trong ngày trả sách', NOW() - INTERVAL 3 DAY),
('CONFIRM_RESERVATION', 'librarian1', 2, 'reservationId=3, bookTitle=Thiết kế kiến trúc phần mềm — Sách đã về kho và sẵn sàng nhận tại quầy', NOW() - INTERVAL 4 DAY),
('APPLY_DAMAGE_FINE', 'librarian2', 11, 'borrowRecordId=31, amount=45000 VND, reason=Bìa sách bị gãy nếp và vẽ bẩn lên trang mục lục', NOW() - INTERVAL 5 DAY),
('WAIVE_FINE', 'admin', 12, 'fineId=25, amount=10000 VND, note=Sinh viên tình nguyện hỗ trợ thư viện tổng kiểm kê đầu kỳ', NOW() - INTERVAL 6 DAY);
