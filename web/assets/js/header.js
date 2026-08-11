/*
 * Điều khiển trạng thái thanh điều hướng và sidebar di động của header dùng chung.
 */

/**
 * Bật hoặc tắt sidebar quản trị trên thiết bị di động và đồng bộ trạng thái trợ năng.
 *
 * @param {HTMLElement} sidebar Sidebar quản trị cần thay đổi trạng thái.
 * @param {HTMLElement} backdrop Lớp nền phía sau sidebar.
 * @param {HTMLButtonElement} toggleButton Nút điều khiển sidebar.
 * @returns {void}
 */
function toggleAdminSidebar(sidebar, backdrop, toggleButton) {
    const isVisible = sidebar.classList.toggle('show');
    backdrop.classList.toggle('show', isVisible);
    toggleButton.setAttribute('aria-expanded', String(isVisible));
}

/**
 * Cập nhật kiểu hiển thị của navbar theo vị trí cuộn hiện tại.
 *
 * @param {HTMLElement} navbar Thanh điều hướng công khai.
 * @returns {void}
 */
function updateNavbarScrollState(navbar) {
    navbar.classList.toggle('scrolled', window.scrollY > 10);
}

/**
 * Xử lý sự kiện cuộn bằng cách tìm navbar hiện tại và cập nhật trạng thái của nó.
 *
 * @returns {void}
 */
function handleNavbarScroll() {
    const navbar = document.querySelector('[data-main-navbar]');

    if (navbar) {
        updateNavbarScrollState(navbar);
    }
}

/**
 * Xử lý thao tác mở hoặc đóng sidebar từ nút điều khiển hay lớp nền.
 *
 * @returns {void}
 */
function handleAdminSidebarToggle() {
    const sidebar = document.querySelector('[data-admin-sidebar]');
    const backdrop = document.querySelector('[data-sidebar-backdrop]');
    const toggleButton = document.querySelector('[data-sidebar-toggle]');

    if (sidebar && backdrop && toggleButton) {
        toggleAdminSidebar(sidebar, backdrop, toggleButton);
    }
}

/**
 * Khởi tạo các tương tác phía trình duyệt cho header nếu phần tử tương ứng tồn tại.
 *
 * @returns {void}
 */
function initializeHeader() {
    const navbar = document.querySelector('[data-main-navbar]');
    const sidebar = document.querySelector('[data-admin-sidebar]');
    const backdrop = document.querySelector('[data-sidebar-backdrop]');
    const toggleButton = document.querySelector('[data-sidebar-toggle]');

    if (navbar) {
        updateNavbarScrollState(navbar);
        window.addEventListener('scroll', handleNavbarScroll);
    }

    if (sidebar && backdrop && toggleButton) {
        toggleButton.addEventListener('click', handleAdminSidebarToggle);
        backdrop.addEventListener('click', handleAdminSidebarToggle);
    }
}

document.addEventListener('DOMContentLoaded', initializeHeader);
