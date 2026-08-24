-- Migration: Bổ sung cột is_deleted vào bảng users phục vụ cơ chế Xóa mềm (Soft Delete)
-- Mặc định is_deleted = 0 (Chưa xóa), is_deleted = 1 (Đã xóa mềm khỏi hệ thống)
ALTER TABLE users ADD COLUMN is_deleted TINYINT DEFAULT 0;
