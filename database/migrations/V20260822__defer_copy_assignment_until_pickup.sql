-- Giải phóng các bản sao từng được gán sớm; barcode chỉ được liên kết khi Librarian giao sách.
UPDATE borrow_records
SET copy_id = NULL,
    updated_at = NOW()
WHERE status = 'PENDING_PICKUP'
  AND borrow_date IS NULL
  AND copy_id IS NOT NULL;
