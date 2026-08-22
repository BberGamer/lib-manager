/* Hỗ trợ danh sách tác giả xác nhận thao tác xóa mềm trước khi gửi request. */
document.querySelectorAll('[data-delete-author]').forEach((deleteForm) => {
    /**
     * Yêu cầu quản trị viên xác nhận trước khi gửi biểu mẫu xóa mềm tác giả.
     *
     * @param {SubmitEvent} event sự kiện gửi biểu mẫu xóa
     * @returns {void}
     */
    const confirmAuthorDeletion = (event) => {
        const authorName = deleteForm.dataset.authorName || 'đã chọn';
        if (!window.confirm(`Bạn chắc chắn muốn xóa tác giả "${authorName}"? Tác giả sẽ bị ẩn khỏi danh sách.`)) {
            event.preventDefault();
        }
    };
    deleteForm.addEventListener('submit', confirmAuthorDeletion);
});
