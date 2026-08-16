/* Xử lý xác nhận xóa kệ trên trang danh sách quản trị. */
(function initializeShelfActions() {
    /** Xác nhận thao tác xóa trước khi gửi biểu mẫu. @param {SubmitEvent} event sự kiện submit */
    function confirmShelfDeletion(event) {
        if (!window.confirm('Bạn có chắc chắn muốn xóa kệ này không?')) {
            event.preventDefault();
        }
    }

    document.querySelectorAll('[data-delete-shelf]').forEach(function bindDelete(form) {
        form.addEventListener('submit', confirmShelfDeletion);
    });
}());
