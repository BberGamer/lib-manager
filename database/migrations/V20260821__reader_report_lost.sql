-- Bổ sung trạng thái LOST cho lượt mượn khi Reader xác nhận làm mất sách.
-- Migration chỉ mở rộng ENUM; dữ liệu lượt mượn và bản sao vẫn được giữ để tra cứu lịch sử.

ALTER TABLE borrow_records
    MODIFY status ENUM(
        'PENDING_PICKUP',
        'BORROWED',
        'RETURNED',
        'EXPIRED',
        'CANCELLED',
        'OVERDUE',
        'LOST'
    ) NOT NULL DEFAULT 'PENDING_PICKUP';
