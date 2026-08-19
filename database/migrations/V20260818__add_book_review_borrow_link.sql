-- Đồng bộ cấu trúc đánh giá với luồng mượn: mỗi đánh giá có thể liên kết
-- tới lượt mượn đã tạo ra quyền đánh giá đó.
SET @add_review_borrow_column_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE book_reviews ADD COLUMN borrow_id INT NULL AFTER user_id',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'book_reviews'
      AND COLUMN_NAME = 'borrow_id'
);
PREPARE add_review_borrow_column_statement FROM @add_review_borrow_column_sql;
EXECUTE add_review_borrow_column_statement;
DEALLOCATE PREPARE add_review_borrow_column_statement;

SET @add_review_borrow_index_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'CREATE INDEX idx_reviews_borrow ON book_reviews (borrow_id)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'book_reviews'
      AND INDEX_NAME = 'idx_reviews_borrow'
);
PREPARE add_review_borrow_index_statement FROM @add_review_borrow_index_sql;
EXECUTE add_review_borrow_index_statement;
DEALLOCATE PREPARE add_review_borrow_index_statement;

SET @add_review_borrow_fk_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE book_reviews ADD CONSTRAINT fk_reviews_borrow FOREIGN KEY (borrow_id) REFERENCES borrow_records (id) ON DELETE SET NULL ON UPDATE CASCADE',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'book_reviews'
      AND CONSTRAINT_NAME = 'fk_reviews_borrow'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE add_review_borrow_fk_statement FROM @add_review_borrow_fk_sql;
EXECUTE add_review_borrow_fk_statement;
DEALLOCATE PREPARE add_review_borrow_fk_statement;
