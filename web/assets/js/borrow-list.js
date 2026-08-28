/*
 * Điều khiển các modal giao sách, nhận trả và lập phiếu phạt trên trang quản lý mượn trả.
 */

/**
 * Khởi tạo hành vi tương tác cho trang quản lý mượn trả sau khi tệp được tải.
 *
 * @returns {void}
 */
(function initializeBorrowList() {
    const DAMAGED_RATE = 0.3;
    const LOST_RATE = 1;
    let selectedBookPrice = 0;

    /**
     * Tìm một trường trong modal theo tên dữ liệu và gán giá trị an toàn cho trường đó.
     *
     * @param {HTMLElement} modal modal chứa trường cần cập nhật
     * @param {string} group nhóm trường dữ liệu của modal
     * @param {string} fieldName tên trường cần cập nhật
     * @param {string|number|null|undefined} value giá trị mới
     * @returns {void}
     */
    function setModalField(modal, group, fieldName, value) {
        const field = modal.querySelector(`[data-${group}-field="${fieldName}"]`);
        if (field) {
            field.value = value == null ? '' : value;
        }
    }

    /**
     * Chuyển chuỗi LocalDateTime từ máy chủ sang dạng dễ đọc trên giao diện.
     *
     * @param {string} value chuỗi thời gian cần hiển thị
     * @returns {string} thời gian đã chuẩn hóa hoặc dấu gạch nếu không có dữ liệu
     */
    function formatDateTime(value) {
        if (!value) {
            return '-';
        }
        return value.replace('T', ' ');
    }

    /**
     * Hiển thị modal và đưa tiêu điểm vào phần tử nhập liệu đầu tiên.
     *
     * @param {HTMLElement} modal modal cần mở
     * @returns {void}
     */
    function openModal(modal) {
        modal.hidden = false;
        const firstControl = modal.querySelector('button, input, select, textarea');
        if (firstControl) {
            firstControl.focus();
        }
    }

    /**
     * Đóng modal hiện tại mà không gửi biểu mẫu.
     *
     * @param {HTMLElement} modal modal cần đóng
     * @returns {void}
     */
    function closeModal(modal) {
        modal.hidden = true;
        if (modal.getAttribute('data-borrow-modal') === 'loan') {
            stopScanning();
        }
    }

    /**
     * Tính lại tiền phạt theo giá sách và tình trạng đang chọn.
     *
     * @param {HTMLElement} fineModal modal lập phiếu phạt
     * @returns {void}
     */
    function fillCalculatedFine(fineModal) {
        const condition = fineModal.querySelector('[data-fine-field="condition"]');
        const amount = fineModal.querySelector('[data-fine-field="amount"]');
        if (!condition || !amount) {
            return;
        }
        const rate = (condition.value === 'LOST' || condition.value === 'DAMAGED') ? LOST_RATE : DAMAGED_RATE;
        amount.value = Math.round(selectedBookPrice * rate);
    }

    /**
     * Điền dữ liệu lượt mượn vào modal giao sách rồi hiển thị modal.
     *
     * @param {HTMLButtonElement} trigger nút chứa dữ liệu lượt mượn
     * @param {HTMLElement} modal modal giao sách
     * @returns {void}
     */
    function prepareLoanModal(trigger, modal) {
        setModalField(modal, 'loan', 'recordId', trigger.dataset.recordId);
        setModalField(modal, 'loan', 'requestId', `#${trigger.dataset.recordId}`);
        setModalField(modal, 'loan', 'bookTitle', trigger.dataset.bookTitle);
        setModalField(modal, 'loan', 'readerName', trigger.dataset.readerName);

        const barcodeVal = trigger.dataset.barcode;
        setModalField(modal, 'loan', 'barcode', barcodeVal === '-' ? '' : barcodeVal);

        setModalField(modal, 'loan', 'requestDate', formatDateTime(trigger.dataset.requestDate));
        setModalField(modal, 'loan', 'pickupDeadline', formatDateTime(trigger.dataset.pickupDeadline));
        openModal(modal);
    }

    /**
     * Điền thông tin bản sao vào modal nhận trả rồi hiển thị modal.
     *
     * @param {HTMLButtonElement} trigger nút chứa dữ liệu lượt mượn
     * @param {HTMLElement} modal modal nhận trả
     * @returns {void}
     */
    function prepareReturnModal(trigger, modal) {
        setModalField(modal, 'return', 'recordId', trigger.dataset.recordId);
        setModalField(modal, 'return', 'bookTitle', trigger.dataset.bookTitle);
        setModalField(modal, 'return', 'barcode', trigger.dataset.barcode);
        openModal(modal);
    }

    /**
     * Điền dữ liệu độc giả và giá sách vào modal phạt rồi tính mức phạt mặc định.
     *
     * @param {HTMLButtonElement} trigger nút chứa dữ liệu lập phiếu
     * @param {HTMLElement} modal modal lập phiếu phạt
     * @returns {void}
     */
    function prepareFineModal(trigger, modal) {
        selectedBookPrice = Number(trigger.dataset.bookPrice) || 0;
        setModalField(modal, 'fine', 'recordId', trigger.dataset.recordId);
        setModalField(modal, 'fine', 'userId', trigger.dataset.userId);
        setModalField(modal, 'fine', 'readerName', trigger.dataset.readerName);
        setModalField(modal, 'fine', 'bookTitle', trigger.dataset.bookTitle);
        setModalField(modal, 'fine', 'condition', 'WORN');
        fillCalculatedFine(modal);
        openModal(modal);
    }

    let html5Qrcode = null;

    /**
     * Khởi động camera quét mã QR/Barcode.
     *
     * @returns {void}
     */
    function startScanning() {
        const qrContainer = document.getElementById('qr-reader-container');
        if (!qrContainer) return;
        qrContainer.style.display = 'block';

        if (html5Qrcode) {
            stopScanning();
        }

        html5Qrcode = new Html5Qrcode("qr-reader");
        const config = { fps: 10, qrbox: { width: 250, height: 150 } };

        html5Qrcode.start(
            { facingMode: "environment" },
            config,
            (decodedText) => {
                const input = document.getElementById('loan-barcode-input');
                if (input) {
                    input.value = decodedText;
                }
                stopScanning();
            },
            () => {}
        ).catch((err) => {
            console.error("Lỗi khởi tạo camera:", err);
            alert("Không thể khởi động camera. Vui lòng cấp quyền truy cập camera cho trang web!");
            qrContainer.style.display = 'none';
        });
    }

    /**
     * Tắt camera và giải phóng tài nguyên quét mã.
     *
     * @returns {void}
     */
    function stopScanning() {
        if (html5Qrcode) {
            html5Qrcode.stop().then(() => {
                const qrContainer = document.getElementById('qr-reader-container');
                if (qrContainer) qrContainer.style.display = 'none';
                html5Qrcode = null;
            }).catch((err) => {
                console.error("Lỗi tắt camera:", err);
                html5Qrcode = null;
            });
        }
    }

    /**
     * Điều phối thao tác mở, đóng modal từ các nút và lớp nền trên trang.
     *
     * @param {MouseEvent} event sự kiện nhấp chuột cần xử lý
     * @returns {void}
     */
    function handleDocumentClick(event) {
        const loanTrigger = event.target.closest('[data-open-loan-modal]');
        const returnTrigger = event.target.closest('[data-open-return-modal]');
        const fineTrigger = event.target.closest('[data-open-fine-modal]');
        const closeTrigger = event.target.closest('[data-close-borrow-modal]');

        const startScanBtn = event.target.closest('#start-scan-btn');
        const stopScanBtn = event.target.closest('#stop-scan-btn');

        if (loanTrigger) {
            prepareLoanModal(loanTrigger, document.querySelector('[data-borrow-modal="loan"]'));
        } else if (returnTrigger) {
            prepareReturnModal(returnTrigger, document.querySelector('[data-borrow-modal="return"]'));
        } else if (fineTrigger) {
            prepareFineModal(fineTrigger, document.querySelector('[data-borrow-modal="fine"]'));
        } else if (closeTrigger) {
            closeModal(closeTrigger.closest('[data-borrow-modal]'));
        } else if (startScanBtn) {
            startScanning();
        } else if (stopScanBtn) {
            stopScanning();
        } else if (event.target.matches('[data-borrow-modal]')) {
            closeModal(event.target);
        }
    }

    /**
     * Tính lại số tiền khi thủ thư thay đổi tình trạng sách trong modal phạt.
     *
     * @param {Event} event sự kiện thay đổi trường biểu mẫu
     * @returns {void}
     */
    function handleDocumentChange(event) {
        if (event.target.matches('[data-fine-field="condition"]')) {
            fillCalculatedFine(event.target.closest('[data-borrow-modal="fine"]'));
        }
    }

    /**
     * Đóng mọi modal đang mở khi người dùng nhấn phím Escape.
     *
     * @param {KeyboardEvent} event sự kiện bàn phím cần xử lý
     * @returns {void}
     */
    function handleDocumentKeydown(event) {
        if (event.key === 'Escape') {
            document.querySelectorAll('[data-borrow-modal]:not([hidden])').forEach(closeModal);
        }
    }

    document.addEventListener('click', handleDocumentClick);
    document.addEventListener('change', handleDocumentChange);
    document.addEventListener('keydown', handleDocumentKeydown);
}());
