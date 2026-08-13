-- Bổ sung tình trạng sách làm căn cứ tính khoản phạt hỏng hoặc mất sách.
ALTER TABLE fines
    ADD COLUMN book_condition VARCHAR(20) NULL AFTER overdue_days,
    ADD COLUMN fine_type VARCHAR(30) NOT NULL DEFAULT 'LEGACY' AFTER book_condition;
