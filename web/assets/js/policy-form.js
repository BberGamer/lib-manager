/* Hỗ trợ form Policy: hiển thị validation tại từng trường và đếm ký tự nội dung. */
const policyForm = document.querySelector('[data-policy-form]');
const policyContent = document.querySelector('[data-policy-content]');
const policyCounter = document.querySelector('[data-policy-counter]');

/** Cập nhật bộ đếm và cảnh báo khi nội dung gần đạt giới hạn. */
function updatePolicyCounter() {
    if (!policyContent || !policyCounter) {
        return;
    }
    const remaining = 10000 - policyContent.value.length;
    policyCounter.textContent = `${remaining} ký tự còn lại`;
    policyCounter.classList.toggle('warning', remaining < 500);
}

/** Trả về thông báo tiếng Việt phù hợp với trạng thái không hợp lệ của trường. */
function getPolicyValidationMessage(field) {
    const validity = field.validity;
    if (validity.valueMissing) {
        return 'Vui lòng nhập trường này.';
    }
    if (field.name === 'policyCode' && validity.patternMismatch) {
        return 'Mã gồm 2–50 ký tự in hoa, số hoặc dấu gạch dưới và bắt đầu bằng chữ.';
    }
    if (field.name === 'title' && validity.tooShort) {
        return 'Tiêu đề phải có từ 3 đến 200 ký tự.';
    }
    if (validity.tooLong) {
        return `Giá trị không được vượt quá ${field.maxLength} ký tự.`;
    }
    if (validity.typeMismatch || validity.badInput) {
        return 'Giá trị nhập vào không hợp lệ.';
    }
    return '';
}

/** Hiển thị hoặc xóa lỗi ngay dưới trường đang được kiểm tra. */
function renderPolicyFieldError(field) {
    const fieldContainer = field.closest('[data-policy-field]');
    if (!fieldContainer) {
        return field.checkValidity();
    }
    const errorElement = fieldContainer.querySelector('[data-policy-error]');
    const message = getPolicyValidationMessage(field);
    fieldContainer.classList.toggle('invalid', message !== '');
    field.setAttribute('aria-invalid', message !== '' ? 'true' : 'false');
    if (errorElement) {
        errorElement.textContent = message;
    }
    return message === '';
}

/** Kiểm tra form và đưa con trỏ tới trường không hợp lệ đầu tiên. */
function validatePolicyForm() {
    const fields = Array.from(policyForm.querySelectorAll(
            'input:not([type="hidden"]), select, textarea'));
    let firstInvalidField = null;
    fields.forEach((field) => {
        if (!renderPolicyFieldError(field) && !firstInvalidField) {
            firstInvalidField = field;
        }
    });
    if (firstInvalidField) {
        firstInvalidField.focus();
        return false;
    }
    return true;
}

if (policyContent && policyCounter) {
    policyContent.addEventListener('input', updatePolicyCounter);
    updatePolicyCounter();
}

if (policyForm) {
    const fields = policyForm.querySelectorAll('input:not([type="hidden"]), select, textarea');
    fields.forEach((field) => {
        field.addEventListener('input', () => renderPolicyFieldError(field));
        field.addEventListener('blur', () => renderPolicyFieldError(field));
    });
    policyForm.addEventListener('submit', (event) => {
        if (!validatePolicyForm()) {
            event.preventDefault();
        }
    });
}
