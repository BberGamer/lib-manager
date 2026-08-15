-- Bổ sung danh mục kệ độc lập nhưng giữ tương thích với book_copies.shelf hiện có.
CREATE TABLE IF NOT EXISTS shelves (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    area VARCHAR(50) NOT NULL,
    floor_number INT NOT NULL DEFAULT 1,
    capacity INT NOT NULL,
    description VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NULL,
    updated_by VARCHAR(50) NULL,
    UNIQUE KEY uk_shelves_code (code),
    CONSTRAINT chk_shelves_capacity CHECK (capacity > 0),
    CONSTRAINT chk_shelves_floor CHECK (floor_number > 0)
);

-- Khởi tạo metadata tối thiểu từ vị trí bản sao đã tồn tại; capacity có thể chỉnh sau.
INSERT INTO shelves (code, name, area, floor_number, capacity, created_by, updated_by)
SELECT grouped_shelves.code, CONCAT('Kệ ', grouped_shelves.code),
       grouped_shelves.area, 1, grouped_shelves.copy_count, 'migration', 'migration'
FROM (
    SELECT TRIM(bc.shelf) AS code,
           COALESCE(NULLIF(TRIM(MIN(bc.area)), ''), 'Chưa phân khu') AS area,
           GREATEST(COUNT(*), 1) AS copy_count
    FROM book_copies bc
    WHERE bc.is_deleted = 0 AND bc.shelf IS NOT NULL AND TRIM(bc.shelf) <> ''
    GROUP BY TRIM(bc.shelf)
) grouped_shelves
ON DUPLICATE KEY UPDATE code = VALUES(code);
