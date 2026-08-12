/* Hỗ trợ trang danh sách danh mục xác nhận thao tác xóa mà không dùng sự kiện inline trong JSP. */
document.querySelectorAll('[data-delete-category]').forEach((deleteForm) => {
    /**
     * Yêu cầu người quản trị xác nhận trước khi gửi biểu mẫu xóa danh mục.
     *
     * @param {SubmitEvent} event sự kiện gửi biểu mẫu xóa
     * @returns {void}
     */
    const confirmCategoryDeletion = (event) => {
        const categoryName = deleteForm.dataset.categoryName || 'đã chọn';
        const isConfirmed = window.confirm(
                `Bạn chắc chắn muốn xóa danh mục "${categoryName}"? Thao tác này không thể hoàn tác.`);
        if (!isConfirmed) {
            event.preventDefault();
        }
    };

    deleteForm.addEventListener('submit', confirmCategoryDeletion);
});
