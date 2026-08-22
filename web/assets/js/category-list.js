/* Hỗ trợ trang danh sách danh mục xác nhận thao tác xóa mềm mà không dùng sự kiện inline trong JSP. */
document.querySelectorAll('[data-delete-category]').forEach((deleteForm) => {
    /**
     * Yêu cầu người quản trị xác nhận trước khi gửi biểu mẫu xóa mềm danh mục.
     *
     * @param {SubmitEvent} event sự kiện gửi biểu mẫu xóa
     * @returns {void}
     */
    const confirmCategoryDeletion = (event) => {
        const categoryName = deleteForm.dataset.categoryName || 'đã chọn';
        const isConfirmed = window.confirm(
                `Bạn chắc chắn muốn xóa danh mục "${categoryName}"? Danh mục sẽ bị ẩn khỏi danh sách.`);
        if (!isConfirmed) {
            event.preventDefault();
        }
    };

    deleteForm.addEventListener('submit', confirmCategoryDeletion);
});
