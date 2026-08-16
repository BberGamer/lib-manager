/*
 * fine-list.js – Xử lý modal thanh toán khoản phạt cho Quản trị viên / Thủ thư
 */

/**
 * Mở hộp thoại và điền dữ liệu của khoản phạt được chọn.
 *
 * @param {string} id mã khoản phạt
 * @param {string} name tên độc giả
 * @param {string} amount số tiền cần thanh toán
 * @returns {void}
 */
function openPaymentModal(id, name, amount) {
    const payFineId = document.getElementById('payFineId');
    const payReaderName = document.getElementById('payReaderName');
    const payAmountText = document.getElementById('payAmountText');
    const modal = document.getElementById('paymentModal');

    if (payFineId) {
        payFineId.value = id;
    }
    if (payReaderName) {
        payReaderName.value = name;
    }

    const formatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
    if (payAmountText) {
        payAmountText.value = formatter.format(amount);
    }

    if (modal) {
        modal.hidden = false;
        modal.classList.add('is-open');
    }
}

/**
 * Đóng hộp thoại thanh toán khoản phạt.
 *
 * @returns {void}
 */
function closePaymentModal() {
    const modal = document.getElementById('paymentModal');
    if (modal) {
        modal.classList.remove('is-open');
        modal.hidden = true;
    }
}

document.querySelectorAll('[data-open-payment]').forEach((button) => {
    button.addEventListener('click', () => {
        openPaymentModal(button.dataset.fineId, button.dataset.readerName, button.dataset.fineAmount);
    });
});

document.querySelectorAll('[data-close-payment]').forEach((button) => {
    button.addEventListener('click', closePaymentModal);
});

document.querySelectorAll('[data-waive-fine]').forEach((form) => {
    form.addEventListener('submit', (event) => {
        if (!window.confirm('Bạn có chắc chắn muốn miễn giảm khoản tiền phạt này không?')) {
            event.preventDefault();
        }
    });
});

window.addEventListener('click', (event) => {
    const modal = document.getElementById('paymentModal');
    if (modal && event.target === modal) {
        closePaymentModal();
    }
});
