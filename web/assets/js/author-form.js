/* Hỗ trợ biểu mẫu tác giả bằng bộ đếm ký tự tiểu sử. */
const authorBio = document.querySelector('#author-bio');
const bioCounter = document.querySelector('[data-bio-counter]');

/** Cập nhật số ký tự tiểu sử còn lại. @returns {void} */
function updateBioCounter() {
    if (!authorBio || !bioCounter) {
        return;
    }
    const remainingCharacters = 2000 - authorBio.value.length;
    bioCounter.textContent = `${remainingCharacters} ký tự còn lại`;
    bioCounter.classList.toggle('warning', remainingCharacters < 100);
}

if (authorBio && bioCounter) {
    authorBio.addEventListener('input', updateBioCounter);
    updateBioCounter();
}
