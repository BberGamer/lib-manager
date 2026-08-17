/*
 * notification.js – Xử lý tương tác giao diện Quản lý thông báo và Hộp thư thông báo
 */

/**
 * Chuyển đổi tab chính (Soạn & Lịch sử gửi vs Nhắc nhở mượn trả & Phạt)
 * @param {string} tabId ID của tab cần hiển thị
 */
function switchNotificationTab(tabId) {
    document.querySelectorAll('.tab-content').forEach(function(el) {
        el.style.display = 'none';
    });
    document.querySelectorAll('.notification-tab-btn').forEach(function(btn) {
        btn.classList.remove('active');
    });

    const targetTab = document.getElementById(tabId);
    if (targetTab) {
        targetTab.style.display = 'block';
    }

    const activeBtn = document.getElementById('tab-' + tabId + '-btn');
    if (activeBtn) {
        activeBtn.classList.add('active');
    }

    // Cập nhật query param trên URL trình duyệt
    try {
        const url = new URL(window.location.href);
        const tabParam = (tabId === 'reminders-tab') ? 'reminders' : 'compose';
        url.searchParams.set('tab', tabParam);
        if (tabParam === 'compose') {
            url.searchParams.delete('sub');
        }
        window.history.pushState({}, '', url.toString());
    } catch (e) {}
}

/**
 * Chuyển đổi sub-tab trong khu vực Nhắc nhở
 * @param {string} subSectionId ID của phân hệ nhắc nhở
 */
function switchReminderSubSection(subSectionId) {
    document.querySelectorAll('.reminder-sub-section').forEach(function(el) {
        el.style.display = 'none';
    });
    const targetSub = document.getElementById(subSectionId);
    if (targetSub) {
        targetSub.style.display = 'block';
    }

    const dueBtn = document.getElementById('due-sub-btn');
    const overdueBtn = document.getElementById('overdue-sub-btn');
    const finesBtn = document.getElementById('fines-sub-btn');

    if (dueBtn) dueBtn.className = 'btn btn-sm btn-outline reminder-sub-btn';
    if (overdueBtn) overdueBtn.className = 'btn btn-sm btn-outline reminder-sub-btn';
    if (finesBtn) finesBtn.className = 'btn btn-sm btn-outline reminder-sub-btn';

    let activeSubBtnId = '';
    let subParam = 'due';
    if (subSectionId === 'due-reminders-sub') {
        activeSubBtnId = 'due-sub-btn';
        subParam = 'due';
    }
    if (subSectionId === 'overdue-reminders-sub') {
        activeSubBtnId = 'overdue-sub-btn';
        subParam = 'overdue';
    }
    if (subSectionId === 'fines-reminders-sub') {
        activeSubBtnId = 'fines-sub-btn';
        subParam = 'fines';
    }

    const activeBtn = document.getElementById(activeSubBtnId);
    if (activeBtn) {
        activeBtn.className = 'btn btn-sm btn-primary reminder-sub-btn';
    }

    // Cập nhật query param sub trên URL trình duyệt
    try {
        const url = new URL(window.location.href);
        url.searchParams.set('tab', 'reminders');
        url.searchParams.set('sub', subParam);
        window.history.pushState({}, '', url.toString());
    } catch (e) {}
}

/**
 * Hiển thị màn hình loading overlay khi gửi biểu mẫu
 * @param {HTMLFormElement} form Form đang gửi
 */
function showNotificationLoading(form) {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.style.display = 'flex';
    }
}

/**
 * Đánh dấu một thông báo là đã đọc bằng AJAX (dành cho Reader Inbox)
 * @param {number} id Mã thông báo
 * @param {boolean} isRead Trạng thái đã đọc hiện tại
 * @param {string} contextPath Đường dẫn gốc của ứng dụng
 */
function markNotificationAsRead(id, isRead, contextPath) {
    if (isRead) return;

    const xhr = new XMLHttpRequest();
    xhr.open('POST', contextPath + '/notification/read', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            const item = document.getElementById('notif-item-' + id);
            if (item) {
                item.classList.remove('unread');
                item.setAttribute('onclick', 'markNotificationAsRead(' + id + ', true, \'' + contextPath + '\')');
            }

            const badge = document.getElementById('badge-' + id);
            if (badge) {
                badge.remove();
            }

            const headerBadge = document.querySelector('.notification-unread-dot');
            if (headerBadge) {
                const visuallyHidden = headerBadge.querySelector('.visually-hidden');
                if (visuallyHidden) {
                    const text = visuallyHidden.textContent;
                    const match = text.match(/\d+/);
                    if (match) {
                        const count = parseInt(match[0], 10) - 1;
                        if (count <= 0) {
                            headerBadge.remove();
                        } else {
                            visuallyHidden.textContent = count + ' thông báo chưa đọc';
                        }
                    }
                } else {
                    headerBadge.remove();
                }
            }
        }
    };
    xhr.send('id=' + id);
}
