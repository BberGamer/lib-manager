/*
 * Hỗ trợ modal lập phiếu phạt tại trang quản lý mượn trả.
 * Tự điền tiền phạt theo giá sách và vẫn cho phép thủ thư điều chỉnh thủ công.
 */
(function () {
    const DAMAGED_RATE = 0.3;
    const LOST_RATE = 1;
    let selectedBookPrice = 0;

    /**
     * Tính và điền lại số tiền theo tình trạng sách đang chọn.
     * Việc thay đổi chỉ xảy ra khi mở modal hoặc đổi dropdown, không cản chỉnh sửa thủ công sau đó.
     */
    function fillCalculatedAmount() {
        const conditionSelect = document.getElementById('fineBookCondition');
        const amountInput = document.getElementById('fineAmount');
        if (!conditionSelect || !amountInput) {
            return;
        }
        const rate = conditionSelect.value === 'LOST' ? LOST_RATE : DAMAGED_RATE;
        amountInput.value = Math.round(selectedBookPrice * rate);
    }

    /**
     * Mở modal và chuẩn bị giá trị phạt mặc định từ giá cuốn sách.
     *
     * @param {number} borrowRecordId mã lượt mượn
     * @param {number} userId mã độc giả
     * @param {string} readerName tên độc giả
     * @param {string} bookTitle tên đầu sách
     * @param {number} bookPrice giá cuốn sách
     */
    window.openFineModal = function (borrowRecordId, userId, readerName, bookTitle, bookPrice) {
        selectedBookPrice = Number(bookPrice) || 0;
        document.getElementById('fineBorrowRecordId').value = borrowRecordId;
        document.getElementById('fineUserId').value = userId;
        document.getElementById('fineReaderName').value = readerName;
        document.getElementById('fineBookTitle').value = bookTitle;
        document.getElementById('fineBookCondition').value = 'DAMAGED';
        fillCalculatedAmount();
        document.getElementById('fineModal').style.display = 'flex';
    };

    /** Đóng modal lập phiếu phạt mà không gửi biểu mẫu. */
    window.closeFineModal = function () {
        document.getElementById('fineModal').style.display = 'none';
    };

    const conditionSelect = document.getElementById('fineBookCondition');
    if (conditionSelect) {
        conditionSelect.addEventListener('change', fillCalculatedAmount);
    }
}());
