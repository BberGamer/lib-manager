-- Migration một lần cho hàng chờ đặt trước; chạy sau migration Borrow Pickup.
ALTER TABLE book_reservations
    MODIFY status ENUM('PENDING','READY','WAITING','READY_FOR_PICKUP','COMPLETED','CANCELLED','EXPIRED')
        NOT NULL DEFAULT 'WAITING';

UPDATE book_reservations
SET status=CASE
    WHEN status='PENDING' THEN 'WAITING'
    WHEN status='READY' THEN 'READY_FOR_PICKUP'
    ELSE status
END
WHERE id>0;

ALTER TABLE book_reservations
    MODIFY status ENUM('WAITING','READY_FOR_PICKUP','COMPLETED','CANCELLED','EXPIRED')
        NOT NULL DEFAULT 'WAITING';

CREATE INDEX idx_reservation_queue
    ON book_reservations(book_id,status,created_at,id);
