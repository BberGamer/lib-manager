-- Migration một lần cho luồng yêu cầu mượn, giữ sách và xác nhận nhận sách.
-- Dùng cho database cũ, trước khi có request_date, pickup_deadline và pickup_date.
-- Không chạy lại file này trên database đã migration thành công.

ALTER TABLE borrow_records
    MODIFY borrow_date DATE NULL,
    MODIFY due_date DATE NULL,
    ADD COLUMN request_date DATETIME NULL AFTER copy_id,
    ADD COLUMN pickup_deadline DATETIME NULL AFTER request_date,
    ADD COLUMN pickup_date DATETIME NULL AFTER pickup_deadline,
    MODIFY status ENUM(
        'PENDING',
        'BORROWING',
        'PENDING_PICKUP',
        'BORROWED',
        'RETURNED',
        'EXPIRED',
        'CANCELLED',
        'OVERDUE',
        'LOST'
    ) NOT NULL DEFAULT 'PENDING_PICKUP';

-- Điều kiện khóa chính giúp câu lệnh chạy được khi MySQL Workbench bật Safe Update Mode.
UPDATE borrow_records
SET request_date = COALESCE(created_at, NOW()),
    pickup_date = CASE
        WHEN status IN ('BORROWING', 'RETURNED', 'OVERDUE') THEN borrow_date
        ELSE NULL
    END,
    status = CASE
        WHEN status = 'PENDING' THEN 'PENDING_PICKUP'
        WHEN status = 'BORROWING' THEN 'BORROWED'
        ELSE status
    END
WHERE id > 0;

ALTER TABLE borrow_records
    MODIFY request_date DATETIME NOT NULL,
    MODIFY pickup_deadline DATETIME NULL,
    MODIFY status ENUM(
        'PENDING_PICKUP',
        'BORROWED',
        'RETURNED',
        'EXPIRED',
        'CANCELLED',
        'OVERDUE'
    ) NOT NULL DEFAULT 'PENDING_PICKUP';

CREATE INDEX idx_borrow_user_book_status
    ON borrow_records (user_id, book_id, status);

CREATE INDEX idx_borrow_pickup_expiration
    ON borrow_records (status, pickup_deadline);

-- Kiểm tra nhanh sau migration.
DESCRIBE borrow_records;
SELECT DISTINCT status FROM borrow_records ORDER BY status;
