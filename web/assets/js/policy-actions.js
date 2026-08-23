/* Xác nhận các thao tác xuất bản, lưu trữ, sử dụng lại và xóa điều lệ trên trang quản trị. */
document.querySelectorAll('[data-confirm-action]').forEach((actionForm) => {
    /** Ngăn gửi form khi Admin chưa xác nhận hành động trạng thái. */
    const confirmPolicyAction = (event) => {
        const message = actionForm.dataset.confirmMessage || 'Bạn chắc chắn muốn thực hiện thao tác này?';
        if (!window.confirm(message)) {
            event.preventDefault();
        }
    };
    actionForm.addEventListener('submit', confirmPolicyAction);
});
