/* Hỗ trợ biểu mẫu thêm và sửa danh mục bằng cách cập nhật số ký tự mô tả còn lại. */
const categoryDescription = document.querySelector('#category-description');
const descriptionCounter = document.querySelector('[data-description-counter]');

/**
 * Cập nhật số ký tự mô tả còn lại và cảnh báo khi gần đạt giới hạn.
 *
 * @returns {void}
 */
function updateDescriptionCounter() {
    if (!categoryDescription || !descriptionCounter) {
        return;
    }
    const remainingCharacters = 500 - categoryDescription.value.length;
    descriptionCounter.textContent = `${remainingCharacters} ký tự còn lại`;
    descriptionCounter.classList.toggle('is-warning', remainingCharacters < 50);
}

if (categoryDescription && descriptionCounter) {
    categoryDescription.addEventListener('input', updateDescriptionCounter);
    updateDescriptionCounter();
}
