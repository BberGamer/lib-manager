/*
 * Script cho trang chủ chính (home.jsp).
 * Hỗ trợ các tương tác trên trang chủ, đặc biệt là tính năng trượt ngang (carousel) hiển thị sách.
 */

/**
 * Trượt carousel hiển thị sách ngang.
 * @param {HTMLElement} container Container của carousel
 * @param {number} direction Hướng trượt (-1: Sang trái, 1: Sang phải)
 */
function slideCarousel(container, direction) {
    const track = container.querySelector('.book-carousel-track');
    const viewport = container.querySelector('.book-carousel-viewport');
    if (!track || !viewport) return;

    const totalItems = track.children.length;
    if (totalItems <= 0) return;

    // Lấy chiều rộng của một phần tử và khoảng cách gap
    const firstItem = track.children[0];
    const itemWidth = firstItem.getBoundingClientRect().width;
    const computedStyle = window.getComputedStyle(track);
    const gap = parseFloat(computedStyle.gap) || 0;
    const stepWidth = itemWidth + gap;

    // Xác định số lượng cột hiển thị trong viewport
    const viewportWidth = viewport.getBoundingClientRect().width;
    const visibleCount = Math.round(viewportWidth / stepWidth) || 1;

    // Giới hạn chỉ số trượt tối đa
    const maxIndex = Math.max(0, totalItems - visibleCount);

    // Lấy chỉ số hiện tại và cập nhật
    let currentIndex = parseInt(container.getAttribute('data-current-index') || '0', 10);
    currentIndex += direction * visibleCount;

    if (currentIndex < 0) {
        currentIndex = 0;
    } else if (currentIndex > maxIndex) {
        currentIndex = maxIndex;
    }

    container.setAttribute('data-current-index', currentIndex);

    const translateX = -currentIndex * stepWidth;
    track.style.transform = `translateX(${translateX}px)`;

    updateCarouselButtons(container, currentIndex, maxIndex);
}

/**
 * Cập nhật trạng thái hiển thị của các nút prev/next.
 * @param {HTMLElement} container
 * @param {number} currentIndex
 * @param {number} maxIndex
 */
function updateCarouselButtons(container, currentIndex, maxIndex) {
    const prevBtn = container.querySelector('.prev-btn');
    const nextBtn = container.querySelector('.next-btn');

    if (prevBtn) {
        if (currentIndex === 0) {
            prevBtn.style.visibility = 'hidden';
            prevBtn.style.opacity = '0';
        } else {
            prevBtn.style.visibility = 'visible';
            prevBtn.style.opacity = '1';
        }
    }

    if (nextBtn) {
        if (currentIndex >= maxIndex) {
            nextBtn.style.visibility = 'hidden';
            nextBtn.style.opacity = '0';
        } else {
            nextBtn.style.visibility = 'visible';
            nextBtn.style.opacity = '1';
        }
    }
}

/**
 * Cập nhật trạng thái và ẩn nút điều hướng nếu tổng số phần tử nhỏ hơn số phần tử hiển thị.
 * @param {HTMLElement} container
 */
function updateCarouselState(container) {
    const track = container.querySelector('.book-carousel-track');
    const viewport = container.querySelector('.book-carousel-viewport');
    if (!track || !viewport) return;

    const totalItems = track.children.length;
    if (totalItems === 0) {
        container.classList.add('hide-nav');
        return;
    }

    const firstItem = track.children[0];
    const itemWidth = firstItem.getBoundingClientRect().width;
    const computedStyle = window.getComputedStyle(track);
    const gap = parseFloat(computedStyle.gap) || 0;
    const stepWidth = itemWidth + gap;
    const viewportWidth = viewport.getBoundingClientRect().width;
    const visibleCount = Math.round(viewportWidth / stepWidth) || 1;

    const maxIndex = Math.max(0, totalItems - visibleCount);

    if (totalItems <= visibleCount) {
        container.classList.add('hide-nav');
    } else {
        container.classList.remove('hide-nav');
    }

    const currentIndex = parseInt(container.getAttribute('data-current-index') || '0', 10);
    updateCarouselButtons(container, currentIndex, maxIndex);
}

/**
 * Khởi tạo các carousel trên trang chủ
 */
function initCarousels() {
    const containers = document.querySelectorAll('.book-carousel-container');
    containers.forEach(container => {
        container.setAttribute('data-current-index', '0');
        
        const track = container.querySelector('.book-carousel-track');
        const viewport = container.querySelector('.book-carousel-viewport');
        const prevBtn = container.querySelector('.prev-btn');
        const nextBtn = container.querySelector('.next-btn');
        
        if (!track || !viewport) return;

        // Đăng ký sự kiện click cho các nút qua addEventListener (tuân thủ quy định AGENTS.md)
        if (prevBtn) {
            prevBtn.addEventListener('click', () => {
                slideCarousel(container, -1);
            });
        }
        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                slideCarousel(container, 1);
            });
        }

        // Tính toán hiển thị ban đầu
        updateCarouselState(container);
    });
}

// Đăng ký sự kiện khi DOM sẵn sàng
document.addEventListener('DOMContentLoaded', function () {
    // Khởi tạo các slider carousel
    initCarousels();

    // Lắng nghe sự kiện resize màn hình để tính toán lại giới hạn hiển thị của slider
    let resizeTimeout;
    window.addEventListener('resize', function () {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(() => {
            // Reset vị trí slider về 0 để tránh lỗi lệch hiển thị khi thay đổi kích thước màn hình
            const containers = document.querySelectorAll('.book-carousel-container');
            containers.forEach(container => {
                const track = container.querySelector('.book-carousel-track');
                if (track) {
                    track.style.transform = 'translateX(0px)';
                }
                container.setAttribute('data-current-index', '0');
                updateCarouselState(container);
            });
        }, 150);
    });
});
