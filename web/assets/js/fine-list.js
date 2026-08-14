/*
 * fine-list.js – Xử lý modal thanh toán khoản phạt cho Quản trị viên / Thủ thư
 */

function openPaymentModal(id, name, amount) {
    const payFineId = document.getElementById('payFineId');
    const payReaderName = document.getElementById('payReaderName');
    const payAmountText = document.getElementById('payAmountText');
    const modal = document.getElementById('paymentModal');

    if (payFineId) payFineId.value = id;
    if (payReaderName) payReaderName.value = name;

    const formatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
    if (payAmountText) payAmountText.value = formatter.format(amount);

    if (modal) {
        modal.hidden = false;
        modal.style.display = 'flex';
    }
}

function closePaymentModal() {
    const modal = document.getElementById('paymentModal');
    if (modal) {
        modal.hidden = true;
        modal.style.display = 'none';
    }
}

window.addEventListener('click', function(event) {
    const modal = document.getElementById('paymentModal');
    if (modal && event.target === modal) {
        closePaymentModal();
    }
});
