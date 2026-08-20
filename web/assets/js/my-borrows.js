/*
 * Điều khiển popup xác nhận báo mất sách trên trang mượn cá nhân của Reader.
 */

/**
 * Khởi tạo thao tác mở, đóng và điền dữ liệu cho popup báo mất sách.
 *
 * @returns {void}
 */
(function initializeLostBookDialog() {
    const dialog = document.querySelector('[data-lost-dialog]');
    if (!dialog) {
        return;
    }

    const borrowIdInput = dialog.querySelector('[data-lost-borrow-id]');
    const bookTitle = dialog.querySelector('[data-lost-book-title]');

    /**
     * Mở popup bằng dữ liệu của lượt mượn được chọn.
     *
     * @param {HTMLButtonElement} trigger nút báo mất trong dòng sách
     * @returns {void}
     */
    function openLostDialog(trigger) {
        const row = trigger.closest('tr');
        const titleElement = row ? row.querySelector('td[data-label="Sách"] strong') : null;
        borrowIdInput.value = trigger.dataset.borrowRecordId || '';
        bookTitle.textContent = titleElement ? titleElement.textContent.trim() : 'sách này';
        dialog.showModal();
    }

    /**
     * Đóng popup mà không gửi yêu cầu báo mất.
     *
     * @returns {void}
     */
    function closeLostDialog() {
        dialog.close();
    }

    /**
     * Điều phối nút mở và đóng popup từ sự kiện nhấp trên trang.
     *
     * @param {MouseEvent} event sự kiện nhấp cần xử lý
     * @returns {void}
     */
    function handleDocumentClick(event) {
        const openTrigger = event.target.closest('[data-open-lost-dialog]');
        const closeTrigger = event.target.closest('[data-close-lost-dialog]');
        if (openTrigger) {
            openLostDialog(openTrigger);
        } else if (closeTrigger) {
            closeLostDialog();
        }
    }

    /**
     * Đóng popup khi Reader nhấp trực tiếp lên vùng nền bên ngoài nội dung.
     *
     * @param {MouseEvent} event sự kiện nhấp trong phần tử dialog
     * @returns {void}
     */
    function handleDialogBackdropClick(event) {
        if (event.target === dialog) {
            closeLostDialog();
        }
    }

    document.addEventListener('click', handleDocumentClick);
    dialog.addEventListener('click', handleDialogBackdropClick);
}());
