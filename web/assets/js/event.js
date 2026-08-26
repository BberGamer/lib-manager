/**
 * Script xử lý tương tác giao diện người dùng cho trang Quản lý sự kiện.
 * Đảm nhận các chức năng:
 * - Lắng nghe sự kiện click mở/đóng Modal Xem chi tiết, Thêm mới, Chỉnh sửa, Xóa sự kiện.
 * - Điền dữ liệu từ data attributes vào form Modal tương ứng mà không dùng scriptlet hay event inline.
 */

document.addEventListener('DOMContentLoaded', function () {
    // Khởi tạo các bộ lắng nghe sự kiện
    initModalTriggers();
});

/**
 * Đăng ký các bộ lắng nghe sự kiện cho tất cả các nút kích hoạt Modal và đóng Modal.
 */
function initModalTriggers() {

    // Nút mở Modal Thêm sự kiện mới
    const addBtn = document.querySelector('[data-action="open-add-modal"]');
    if (addBtn) {
        addBtn.addEventListener('click', function () {
            openModal('addEventModal');
        });
    }

    // Nút Xem chi tiết sự kiện
    const viewBtns = document.querySelectorAll('[data-action="open-view-modal"]');
    viewBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            populateViewModal(btn);
            openModal('viewEventModal');
        });
    });

    // Nút Sửa sự kiện
    const editBtns = document.querySelectorAll('[data-action="open-edit-modal"]');
    editBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            populateEditModal(btn);
            openModal('editEventModal');
        });
    });

    // Nút Xóa sự kiện
    const deleteBtns = document.querySelectorAll('[data-action="open-delete-modal"]');
    deleteBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            populateDeleteModal(btn);
            openModal('deleteEventModal');
        });
    });

    // Đóng Modal khi bấm nút đóng hoặc nút hủy
    const closeBtns = document.querySelectorAll('[data-action="close-modal"]');
    closeBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            const modal = btn.closest('.modal-backdrop');
            if (modal) {
                closeModal(modal.id);
            }
        });
    });

    // Đóng Modal khi bấm ra vùng phông nền ngoài Modal (backdrop)
    const modals = document.querySelectorAll('.modal-backdrop');
    modals.forEach(function (modal) {
        modal.addEventListener('click', function (e) {
            if (e.target === modal) {
                closeModal(modal.id);
            }
        });
    });
}

/**
 * Hiển thị Modal theo ID.
 *
 * @param {string} modalId ID của thẻ Modal container
 */
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('show');
    }
}

/**
 * Ẩn Modal theo ID.
 *
 * @param {string} modalId ID của thẻ Modal container
 */
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('show');
    }
}

/**
 * Đọc dữ liệu từ data attributes của nút Xem và nạp vào Modal Xem chi tiết.
 *
 * @param {HTMLElement} btn Nút xem được nhấn
 */
function populateViewModal(btn) {
    document.getElementById('viewTitle').textContent = btn.getAttribute('data-title') || '';
    document.getElementById('viewDescription').textContent = btn.getAttribute('data-description') || '(Không có mô tả)';
    document.getElementById('viewStartTime').textContent = btn.getAttribute('data-start-time') || '';
    document.getElementById('viewEndTime').textContent = btn.getAttribute('data-end-time') || '';
    const createdByEl = document.getElementById('viewCreatedBy');
    if (createdByEl) createdByEl.textContent = btn.getAttribute('data-created-by') || '';

    const updatedByEl = document.getElementById('viewUpdatedBy');
    if (updatedByEl) updatedByEl.textContent = btn.getAttribute('data-updated-by') || 'N/A';

    const displayStatus = btn.getAttribute('data-display-status') || '';
    const statusContainer = document.getElementById('viewDisplayStatus');

    let badgeClass = 'badge-status ';
    let statusText = displayStatus;

    switch (displayStatus) {
        case 'UPCOMING':
            badgeClass += 'badge-upcoming';
            statusText = 'Sắp diễn ra';
            break;
        case 'ONGOING':
            badgeClass += 'badge-ongoing';
            statusText = 'Đang diễn ra';
            break;
        case 'ENDED':
            badgeClass += 'badge-ended';
            statusText = 'Đã kết thúc';
            break;
        case 'CANCELLED':
            badgeClass += 'badge-cancelled';
            statusText = 'Đã hủy';
            break;
        default:
            badgeClass += 'badge-ended';
            break;
    }

    if (statusContainer) {
        statusContainer.className = badgeClass;
        statusContainer.textContent = statusText;
    }
}

/**
 * Đọc dữ liệu từ data attributes của nút Sửa và nạp vào Form Modal Chỉnh sửa.
 *
 * @param {HTMLElement} btn Nút sửa được nhấn
 */
function populateEditModal(btn) {
    document.getElementById('editId').value = btn.getAttribute('data-id') || '';
    document.getElementById('editTitle').value = btn.getAttribute('data-title') || '';
    document.getElementById('editDescription').value = btn.getAttribute('data-description') || '';
    document.getElementById('editStartTime').value = btn.getAttribute('data-start-time-raw') || '';
    document.getElementById('editEndTime').value = btn.getAttribute('data-end-time-raw') || '';

    const statusSelect = document.getElementById('editStatus');
    if (statusSelect) {
        statusSelect.value = btn.getAttribute('data-status') || 'ACTIVE';
    }
}

/**
 * Đọc dữ liệu từ nút Xóa và nạp vào Form Confirm Delete Modal.
 *
 * @param {HTMLElement} btn Nút xóa được nhấn
 */
function populateDeleteModal(btn) {
    document.getElementById('deleteId').value = btn.getAttribute('data-id') || '';
    document.getElementById('deleteEventTitle').textContent = btn.getAttribute('data-title') || '';
}
