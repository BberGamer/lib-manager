/* Hỗ trợ danh sách tác giả xác nhận thao tác xóa trước khi gửi request. */
document.querySelectorAll('[data-delete-author]').forEach((deleteForm) => {
    /** @param {SubmitEvent} event sự kiện gửi form xóa @returns {void} */
    const confirmAuthorDeletion = (event) => {
        const authorName = deleteForm.dataset.authorName || 'đã chọn';
        if (!window.confirm(`Bạn chắc chắn muốn xóa tác giả "${authorName}"?`)) {
            event.preventDefault();
        }
    };
    deleteForm.addEventListener('submit', confirmAuthorDeletion);
});
