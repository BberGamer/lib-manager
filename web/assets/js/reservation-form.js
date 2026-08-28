/* Hỗ trợ form đặt trước: kiểm tra ngày chọn và cập nhật ngày nhận dự kiến từ server. */
const reservationForm = document.querySelector('[data-reservation-form]');

/**
 * Định dạng ngày ISO thành dd/MM/yyyy cho phần dự kiến trên form.
 *
 * @param {string} isoDate ngày theo định dạng yyyy-MM-dd
 * @returns {string} ngày hiển thị theo định dạng Việt Nam
 */
function formatReservationDate(isoDate) {
    const [year, month, day] = isoDate.split('-');
    return `${day}/${month}/${year}`;
}

/**
 * Gọi server tính lại slot và phản ánh trạng thái hợp lệ của form.
 *
 * @returns {Promise<void>} hoàn tất sau khi giao diện được cập nhật
 */
async function refreshExpectedPickupDate() {
    const pickupInput = reservationForm.querySelector('[data-pickup-date]');
    const earliestDate = reservationForm.querySelector('[data-earliest-date]');
    const expectedDate = reservationForm.querySelector('[data-expected-date]');
    const expectedEndDate = reservationForm.querySelector('[data-expected-end-date]');
    const availableCapacity = reservationForm.querySelector('[data-available-capacity]');
    const message = reservationForm.querySelector('[data-estimate-message]');
    const submitButton = reservationForm.querySelector('[data-reservation-submit]');
    const bookId = reservationForm.querySelector('input[name="bookId"]').value;

    if (!pickupInput.reportValidity()) {
        return;
    }

    const query = new URLSearchParams({
        bookId,
        requestedPickupDate: pickupInput.value
    });
    submitButton.disabled = true;
    message.textContent = 'Đang tính lại ngày dự kiến…';

    try {
        const response = await fetch(`${reservationForm.dataset.estimateUrl}?${query}`, {
            headers: {'Accept': 'application/json'}
        });
        const payload = await response.json();
        if (!response.ok) {
            throw new Error(payload.message || 'Ngày nhận sách không hợp lệ.');
        }
        earliestDate.dateTime = payload.earliestAvailableDate;
        earliestDate.textContent = formatReservationDate(payload.earliestAvailableDate);
        expectedDate.dateTime = payload.expectedPickupDate;
        expectedDate.textContent = formatReservationDate(payload.expectedPickupDate);
        expectedEndDate.dateTime = payload.expectedEndDate;
        expectedEndDate.textContent = formatReservationDate(payload.expectedEndDate);
        availableCapacity.textContent = payload.availableCapacity;
        message.textContent = `Còn ${payload.availableCapacity} bản trong toàn bộ khoảng 7 ngày đã chọn.`;
        submitButton.disabled = false;
    } catch (error) {
        message.textContent = error.message;
    }
}

if (reservationForm) {
    const pickupInput = reservationForm.querySelector('[data-pickup-date]');
    const earliestDate = reservationForm.querySelector('[data-earliest-date]');
    const expectedDate = reservationForm.querySelector('[data-expected-date]');
    const expectedEndDate = reservationForm.querySelector('[data-expected-end-date]');
    earliestDate.textContent = formatReservationDate(earliestDate.dateTime);
    expectedDate.textContent = formatReservationDate(expectedDate.dateTime);
    expectedEndDate.textContent = formatReservationDate(expectedEndDate.dateTime);
    pickupInput.addEventListener('change', refreshExpectedPickupDate);
}
