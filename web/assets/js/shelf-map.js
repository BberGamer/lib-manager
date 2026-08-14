/*
 * Điều khiển modal cập nhật vị trí bản sao trên trang sơ đồ kho sách.
 */

/**
 * Khởi tạo hành vi mở, đóng và điền dữ liệu cho modal vị trí bản sao.
 *
 * @returns {void}
 */
(function initializeShelfMap() {
    const modal = document.querySelector('[data-location-modal]');
    if (!modal) {
        return;
    }

    /**
     * Gán giá trị cho một trường dữ liệu trong modal vị trí.
     *
     * @param {string} fieldName tên trường cần cập nhật
     * @param {string|number|null|undefined} value giá trị mới
     * @returns {void}
     */
    function setLocationField(fieldName, value) {
        const field = modal.querySelector(`[data-location-field="${fieldName}"]`);
        if (field) {
            field.value = value == null || value === 'null' ? '' : value;
        }
    }

    /**
     * Điền thông tin bản sao từ nút thao tác và hiển thị modal cập nhật.
     *
     * @param {HTMLButtonElement} trigger nút chứa dữ liệu bản sao
     * @returns {void}
     */
    function openLocationModal(trigger) {
        setLocationField('copyId', trigger.dataset.copyId);
        setLocationField('barcode', trigger.dataset.barcode);
        setLocationField('bookTitle', trigger.dataset.bookTitle);
        setLocationField('area', trigger.dataset.area);
        setLocationField('shelf', trigger.dataset.shelf);
        setLocationField('slot', trigger.dataset.slot);
        modal.hidden = false;

        const areaInput = modal.querySelector('[data-location-field="area"]');
        if (areaInput) {
            areaInput.focus();
        }
    }

    /**
     * Đóng modal vị trí mà không gửi biểu mẫu.
     *
     * @returns {void}
     */
    function closeLocationModal() {
        modal.hidden = true;
    }

    /**
     * Điều phối thao tác mở hoặc đóng modal từ sự kiện nhấp chuột.
     *
     * @param {MouseEvent} event sự kiện nhấp chuột cần xử lý
     * @returns {void}
     */
    function handleDocumentClick(event) {
        const openTrigger = event.target.closest('[data-open-location-modal]');
        if (openTrigger) {
            openLocationModal(openTrigger);
        } else if (event.target.closest('[data-close-location-modal]') || event.target === modal) {
            closeLocationModal();
        }
    }

    /**
     * Đóng modal khi người dùng nhấn phím Escape.
     *
     * @param {KeyboardEvent} event sự kiện bàn phím cần xử lý
     * @returns {void}
     */
    function handleDocumentKeydown(event) {
        if (event.key === 'Escape' && !modal.hidden) {
            closeLocationModal();
        }
    }

    document.addEventListener('click', handleDocumentClick);
    document.addEventListener('keydown', handleDocumentKeydown);
}());
