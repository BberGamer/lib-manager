-- Chuẩn hóa model bản sao: trạng thái mượn thuộc borrow_records,
-- book_copies chỉ lưu tình trạng vật lý trong book_condition.
SET @drop_book_copy_status_sql = (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE book_copies DROP COLUMN status',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'book_copies'
      AND COLUMN_NAME = 'status'
);
PREPARE drop_book_copy_status_statement FROM @drop_book_copy_status_sql;
EXECUTE drop_book_copy_status_statement;
DEALLOCATE PREPARE drop_book_copy_status_statement;

SET @create_borrow_period_index_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'CREATE INDEX idx_borrow_copy_period ON borrow_records (copy_id, borrow_date, due_date, return_date)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'borrow_records'
      AND INDEX_NAME = 'idx_borrow_copy_period'
);
PREPARE create_borrow_period_index_statement FROM @create_borrow_period_index_sql;
EXECUTE create_borrow_period_index_statement;
DEALLOCATE PREPARE create_borrow_period_index_statement;
