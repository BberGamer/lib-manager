/* Hỗ trợ biểu mẫu thêm và sửa sách chọn nhiều tác giả bằng thao tác nhấp từng dòng. */

/**
 * Khởi tạo thao tác chọn/bỏ chọn độc lập cho mỗi tác giả trong danh sách nhiều lựa chọn.
 * Việc phát sự kiện change giúp cơ chế tìm kiếm tác giả hiện có giữ đúng trạng thái đã chọn.
 *
 * @returns {void}
 */
function initializeAuthorMultiSelect() {
    const authorSelect = document.querySelector('[data-author-multi-select]');

    if (!authorSelect) {
        return;
    }

    authorSelect.addEventListener('mousedown', toggleAuthorOption);
    authorSelect.addEventListener('change', updateSelectedAuthorsDisplay);
    updateSelectedAuthorsDisplay();
}

/**
 * Đảo trạng thái lựa chọn của tác giả được nhấp và đồng bộ với bộ lọc tìm kiếm hiện có.
 *
 * @param {MouseEvent} event sự kiện chuột phát sinh trên danh sách tác giả
 * @returns {void}
 */
function toggleAuthorOption(event) {
    const authorSelect = event.currentTarget;
    const option = event.target;

    if (!(option instanceof HTMLOptionElement)) {
        return;
    }

    event.preventDefault();
    option.selected = !option.selected;
    authorSelect.dispatchEvent(new Event('change', { bubbles: true }));
}

/**
 * Hiển thị tên tất cả tác giả đang được chọn mà không làm thay đổi nội dung ô tìm kiếm.
 *
 * @returns {void}
 */
function updateSelectedAuthorsDisplay() {
    const selectedAuthorsDisplay = document.querySelector('[data-selected-authors-display]');

    if (!selectedAuthorsDisplay) {
        return;
    }

    const originalOptions = window.originalAuthorOptions || [];
    const selectedOptions = originalOptions.length > 0
        ? originalOptions
        : Array.from(document.querySelectorAll('[data-author-multi-select] option'));
    const selectedNames = getSelectedAuthorNames(selectedOptions);

    selectedAuthorsDisplay.value = selectedNames.join(', ');
}

/**
 * Lấy tên các tác giả đang được đánh dấu từ danh sách tùy chọn ban đầu hoặc đang hiển thị.
 *
 * @param {Array} authorOptions danh sách đối tượng option của tác giả
 * @returns {Array<string>} tên các tác giả được chọn theo thứ tự hiển thị
 */
function getSelectedAuthorNames(authorOptions) {
    const selectedNames = [];

    for (let index = 0; index < authorOptions.length; index += 1) {
        if (authorOptions[index].selected) {
            selectedNames.push(authorOptions[index].text);
        }
    }

    return selectedNames;
}

document.addEventListener('DOMContentLoaded', initializeAuthorMultiSelect);
