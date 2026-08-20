-- Bổ sung ngày nhận mong muốn, ngày dự kiến và dấu mốc đã báo trễ cho luồng đặt trước.
-- Chạy sau V20260813__reservation_queue_flow.sql.

ALTER TABLE book_reservations
    ADD COLUMN requested_pickup_date DATE NULL AFTER reserve_date,
    ADD COLUMN expected_pickup_date DATE NULL AFTER requested_pickup_date,
    ADD COLUMN delay_notified_at DATETIME NULL AFTER notified_at;

-- Dữ liệu cũ không có ngày người đọc chọn; dùng ngày tạo làm mốc tương thích.
UPDATE book_reservations
SET requested_pickup_date = DATE(COALESCE(reserve_date, created_at)),
    expected_pickup_date = DATE(COALESCE(reserve_date, created_at))
WHERE id > 0
  AND (requested_pickup_date IS NULL OR expected_pickup_date IS NULL);

ALTER TABLE book_reservations
    MODIFY requested_pickup_date DATE NOT NULL,
    MODIFY expected_pickup_date DATE NOT NULL;

CREATE INDEX idx_reservation_pickup_schedule
    ON book_reservations(book_id, status, expected_pickup_date, created_at, id);
