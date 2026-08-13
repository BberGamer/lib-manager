<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Book, model.Author, model.Category, model.User, java.util.List" %>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    String formMode = (String) request.getAttribute("formMode");
    if (formMode == null) formMode = "add";
    boolean isEdit = "edit".equals(formMode);

    Book book = (Book) request.getAttribute("book");
    List<Category> categoriesList = (List<Category>) request.getAttribute("categoriesList");
    List<Author> authorsList = (List<Author>) request.getAttribute("authorsList");
    List<Integer> selectedAuthorIds = (List<Integer>) request.getAttribute("selectedAuthorIds");
    Boolean hasCopies = (Boolean) request.getAttribute("hasCopies");
    if (hasCopies == null) hasCopies = false;

    List<String> errors = (List<String>) request.getAttribute("errors");
    String errorMsg = (String) request.getAttribute("errorMsg");

    String ctx = request.getContextPath();

    String rolePath = (String) request.getAttribute("rolePath");
    if (rolePath == null) rolePath = "";

    // Default values
    String title       = book != null && book.getTitle() != null ? book.getTitle() : "";
    String isbn        = book != null && book.getIsbn() != null ? book.getIsbn() : "";
    String category    = book != null && book.getCategory() != null ? book.getCategory() : "";
    int    categoryId  = book != null ? book.getCategoryId() : 0;
    String publisher   = book != null && book.getPublisher() != null ? book.getPublisher() : "";
    String publishYear = book != null && book.getPublishYear() != null ? String.valueOf(book.getPublishYear()) : "";
    String price       = book != null && book.getPrice() != null ? String.valueOf(book.getPrice()) : "";
    String quantity    = book != null ? String.valueOf(book.getQuantity()) : "0";
    String available   = book != null ? String.valueOf(book.getAvailable()) : "0";
    String description = book != null && book.getDescription() != null ? book.getDescription() : "";
    String coverImage  = book != null && book.getCoverImage() != null ? book.getCoverImage() : "";
    String subject     = book != null && book.getSubject() != null ? book.getSubject() : "";

    int    bookId      = book != null ? book.getId() : 0;
    int    currentYear = java.time.Year.now().getValue();
%>

<main class="page-wrapper">

<!-- ===== PAGE HEADER ===== -->
<div class="books-page-header">
    <div class="container">
        <div class="books-page-header-inner">
            <div>
                <div class="hero-eyebrow" style="margin-bottom:10px;">
                    <i class="fa-solid fa-<%= isEdit ? "pen-to-square" : "plus" %>"></i>
                    <%= isEdit ? "Chỉnh sửa sách" : "Thêm sách mới" %>
                </div>
                <h1 class="books-page-title"><%= isEdit ? "Chỉnh sửa: " + title : "Thêm sách mới" %></h1>
                <p class="books-page-subtitle">
                    <a href="<%= isEdit ? ctx + rolePath + "/book/detail?id=" + bookId : ctx + rolePath + "/books" %>" style="color:var(--primary);">
                        <i class="fa-solid fa-arrow-left"></i> <%= isEdit ? "Quay lại chi tiết" : "Quay lại danh sách" %>
                    </a>
                </p>
            </div>
        </div>
    </div>
</div>

<div class="container" style="padding-top:28px; padding-bottom:40px;">

    <!-- Error Messages -->
    <% if (errors != null && !errors.isEmpty()) { %>
        <div class="alert alert-danger">
            <i class="fa-solid fa-circle-xmark"></i>
            <div>
                <strong>Vui lòng sửa các lỗi sau:</strong>
                <ul style="margin:8px 0 0 16px; list-style:disc;">
                    <% for (String err : errors) { %>
                        <li><%= err %></li>
                    <% } %>
                </ul>
            </div>
        </div>
    <% } %>
    <% if (errorMsg != null) { %>
        <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark"></i> <%= errorMsg %></div>
    <% } %>

    <!-- ===== BOOK FORM ===== -->
    <form id="bookForm" action="<%= isEdit ? ctx + rolePath + "/book/edit" : ctx + rolePath + "/book/add" %>" method="post" enctype="multipart/form-data" novalidate>
        <input type="hidden" name="action" value="<%= isEdit ? "update" : "create" %>">
        <% if (isEdit) { %>
            <input type="hidden" name="id" value="<%= bookId %>">
        <% } %>

        <div class="book-form-grid">
            <!-- ===== LEFT COLUMN: Basic Info ===== -->
            <div class="book-form-column">
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa-solid fa-circle-info"></i> Thông tin cơ bản
                    </div>
                    <div class="detail-card-body">
                        <!-- Title -->
                        <div class="form-group">
                            <label for="titleInput" class="form-label">
                                Tiêu đề sách <span class="required">*</span>
                            </label>
                            <input type="text" id="titleInput" name="title" class="form-control"
                                   value="<%= title %>" placeholder="Nhập tiêu đề sách..."
                                   maxlength="255" required>
                            <div id="titleFeedback" style="display: flex; justify-content: space-between; margin-top: 4px; font-size: 13px;">
                                <span id="titleError" style="color: var(--danger); display: none; font-weight: 500;">
                                    <i class="fa-solid fa-circle-exclamation"></i> Đã đạt giới hạn tối đa 255 ký tự.
                                </span>
                                <span id="titleCounter" style="color: var(--text-muted); margin-left: auto;">0 / 255 ký tự</span>
                            </div>
                        </div>

                        <!-- ISBN -->
                        <div class="form-group">
                            <label for="isbnInput" class="form-label">
                                ISBN <span class="required">*</span>
                            </label>
                            <input type="text" id="isbnInput" name="isbn" class="form-control"
                                   value="<%= isbn %>" placeholder="VD: 978-604-12345"
                                   maxlength="20" required
                                   <%= (isEdit && hasCopies) ? "readonly style=\"background:var(--bg-surface); cursor:not-allowed;\"" : "" %>>
                            <% if (isEdit && hasCopies) { %>
                                <span class="form-hint" style="color:var(--warning);">
                                    <i class="fa-solid fa-lock fa-xs"></i> ISBN không thể sửa vì đã có bản sao vật lý liên kết.
                                </span>
                            <% } else { %>
                                <span class="form-hint">Mã ISBN duy nhất, tối đa 20 ký tự</span>
                            <% } %>
                        </div>

                        <!-- Category -->
                        <div class="form-group">
                            <label for="categorySelect" class="form-label">
                                Danh mục <span class="required">*</span>
                            </label>
                            <select id="categorySelect" name="category" class="form-select" required onchange="updateCategoryId()">
                                <option value="">-- Chọn danh mục --</option>
                                <% if (categoriesList != null) {
                                    for (Category cat : categoriesList) {
                                        String sel = cat.getName().equals(category) ? "selected" : "";
                                %>
                                    <option value="<%= cat.getName() %>" data-id="<%= cat.getId() %>" <%= sel %>><%= cat.getName() %></option>
                                <% }} %>
                            </select>
                            <input type="hidden" id="categoryIdInput" name="categoryId" value="<%= categoryId %>">
                        </div>

                        <!-- Authors -->
                        <div class="form-group">
                            <label for="authorSelect" class="form-label">Tác giả <span class="required">*</span></label>
                            <input type="text" id="authorSearch" placeholder="🔍 Nhập để tìm kiếm tác giả nhanh..." class="form-control" style="margin-bottom: 8px; font-size: 13.5px; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px;">
                            <select id="authorSelect" name="authorIds" class="form-select" multiple
                                    size="5" style="min-height:120px;">
                                <% if (authorsList != null) {
                                    for (Author a : authorsList) {
                                        boolean selected = selectedAuthorIds != null && selectedAuthorIds.contains(a.getId());
                                %>
                                    <option value="<%= a.getId() %>" <%= selected ? "selected" : "" %>><%= a.getName() %></option>
                                <% }} %>
                            </select>
                            <span class="form-hint">Giữ Ctrl để chọn nhiều tác giả</span>
                        </div>

                        <!-- Publisher -->
                        <div class="form-group">
                            <label for="publisherInput" class="form-label">Nhà xuất bản</label>
                            <input type="text" id="publisherInput" name="publisher" class="form-control"
                                   value="<%= publisher %>" placeholder="VD: NXB Bách Khoa"
                                   maxlength="150">
                        </div>

                        <!-- Publish Year & Price -->
                        <div class="form-row">
                            <div class="form-group">
                                <label for="publishYearInput" class="form-label">Năm xuất bản</label>
                                <input type="number" id="publishYearInput" name="publishYear" class="form-control"
                                       value="<%= publishYear %>" placeholder="VD: 2024"
                                       min="1000" max="<%= currentYear %>">
                            </div>
                            <div class="form-group">
                                <label for="priceInput" class="form-label">Giá (VNĐ)</label>
                                <input type="number" id="priceInput" name="price" class="form-control"
                                       value="<%= price %>" placeholder="VD: 150000"
                                       min="0">
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ===== RIGHT COLUMN: Inventory & Description ===== -->
            <div class="book-form-column">
                <!-- Inventory -->
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa-solid fa-boxes-stacked"></i> Tồn kho & Vị trí
                    </div>
                    <div class="detail-card-body">
                        <div class="form-row">
                            <div class="form-group">
                                <label for="quantityInput" class="form-label">Tổng số lượng</label>
                                <input type="number" id="quantityInput" name="quantity" class="form-control"
                                       value="<%= quantity %>" min="0" onchange="syncAvailable()"
                                       <%= hasCopies ? "readonly style='background-color: var(--bg-card); cursor: not-allowed; opacity: 0.85; border-color: var(--border-light);'" : "" %>>
                            </div>
                            <div class="form-group">
                                <label for="availableInput" class="form-label">Số lượng có sẵn</label>
                                <input type="number" id="availableInput" name="available" class="form-control"
                                       value="<%= available %>" min="0"
                                       <%= hasCopies ? "readonly style='background-color: var(--bg-card); cursor: not-allowed; opacity: 0.85; border-color: var(--border-light);'" : "" %>>
                            </div>
                        </div>
                        <% if (hasCopies) { %>
                            <div style="margin-top: 10px; font-size: 0.82rem; color: #f5a623; display: flex; align-items: center; gap: 6px;">
                                <i class="fa-solid fa-triangle-exclamation"></i>
                                <span>Số lượng được đồng bộ tự động từ các bản sao vật lý.</span>
                            </div>
                        <% } %>

                        <div class="form-group">
                            <label for="subjectInput" class="form-label">Môn học liên quan</label>
                            <input type="text" id="subjectInput" name="subject" class="form-control"
                                   value="<%= subject %>" placeholder="VD: Lập trình Web"
                                   maxlength="100">
                        </div>


                    </div>
                </div>

                <!-- Description & Cover -->
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa-solid fa-align-left"></i> Mô tả & Ảnh bìa
                    </div>
                    <div class="detail-card-body">
                        <div class="form-group">
                            <label for="descriptionInput" class="form-label">Mô tả sách</label>
                            <textarea id="descriptionInput" name="description" class="form-control"
                                      rows="5" placeholder="Nhập mô tả chi tiết về sách..."
                                      maxlength="5000"><%= description %></textarea>
                            <span class="form-hint">Tối đa 5000 ký tự</span>
                        </div>

                        <div class="form-group">
                            <label for="coverImageFile" class="form-label">Ảnh bìa sách</label>
                            <input type="file" id="coverImageFile" name="coverImageFile" class="form-control" accept="image/*" onchange="previewImage(this, 'coverImagePreview')">
                            <input type="hidden" name="existingCoverImage" value="<%= coverImage %>">
                            <div style="margin-top: 10px;">
                                <img id="coverImagePreview" src="<%= coverImage != null && !coverImage.isEmpty() ? utils.UploadUtility.resolveUrl(coverImage, request.getContextPath()) : "" %>" style="max-height: 150px; display: <%= coverImage != null && !coverImage.isEmpty() ? "block" : "none" %>; border-radius: 6px; border: 1px solid #d1d5db;" alt="Xem trước ảnh bìa">
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Submit Buttons -->
                <div class="book-form-buttons">
                    <a href="<%= isEdit ? ctx + rolePath + "/book/detail?id=" + bookId : ctx + rolePath + "/books" %>" class="btn btn-outline">
                        <i class="fa-solid fa-xmark"></i> Hủy
                    </a>
                    <button type="submit" class="btn btn-primary btn-lg" id="submitBtn">
                        <i class="fa-solid fa-<%= isEdit ? "floppy-disk" : "plus" %>"></i>
                        <%= isEdit ? "Lưu thay đổi" : "Thêm sách" %>
                    </button>
                </div>
            </div>
        </div>
    </form>

</div><!-- /container -->
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>

<script>
// ---- Title live character counter & validation ----
(function() {
    var titleInput = document.getElementById('titleInput');
    var titleCounter = document.getElementById('titleCounter');
    var titleError = document.getElementById('titleError');

    if (!titleInput || !titleCounter || !titleError) return;

    function checkLength() {
        var len = titleInput.value.length;
        titleCounter.textContent = len + ' / 255 ký tự';

        if (len >= 255) {
            titleInput.style.borderColor = 'var(--danger)';
            titleInput.style.boxShadow = '0 0 0 3px rgba(255, 71, 87, 0.15)';
            titleCounter.style.color = 'var(--danger)';
            titleCounter.style.fontWeight = 'bold';
            titleError.style.display = 'inline-block';
        } else {
            titleInput.style.borderColor = '';
            titleInput.style.boxShadow = '';
            titleCounter.style.color = 'var(--text-muted)';
            titleCounter.style.fontWeight = 'normal';
            titleError.style.display = 'none';
        }
    }

    titleInput.addEventListener('input', checkLength);
    // Initialize on load
    checkLength();
})();

// ---- Category ID sync ----
function updateCategoryId() {
    var sel = document.getElementById('categorySelect');
    var opt = sel.options[sel.selectedIndex];
    document.getElementById('categoryIdInput').value = opt ? (opt.getAttribute('data-id') || '') : '';
}

// ---- Available auto-sync (for new books) ----
function syncAvailable() {
    <% if (!isEdit) { %>
    var qty = parseInt(document.getElementById('quantityInput').value) || 0;
    document.getElementById('availableInput').value = qty;
    <% } %>
}

// ---- Client-side validation ----
document.getElementById('bookForm').addEventListener('submit', function(e) {
    // Restore all original author options to DOM before validation and submission
    var select = document.getElementById('authorSelect');
    if (select && window.originalAuthorOptions && window.originalAuthorOptions.length > 0) {
        select.innerHTML = '';
        for (var i = 0; i < window.originalAuthorOptions.length; i++) {
            var opt = window.originalAuthorOptions[i];
            var el = document.createElement('option');
            el.value = opt.value;
            el.text = opt.text;
            el.selected = opt.selected;
            select.appendChild(el);
        }
    }

    var titleVal = document.getElementById('titleInput').value.trim();
    var isbnVal  = document.getElementById('isbnInput').value.trim();
    var catVal   = document.getElementById('categorySelect').value;
    var errors   = [];

    if (!titleVal)        errors.push('Tiêu đề sách không được để trống.');
    if (titleVal.length > 255) errors.push('Tiêu đề không vượt quá 255 ký tự.');
    if (!isbnVal)         errors.push('ISBN không được để trống.');
    if (isbnVal.length > 20)  errors.push('ISBN không vượt quá 20 ký tự.');
    if (!catVal)          errors.push('Vui lòng chọn danh mục.');

    // Enforce author selection
    var selectedAuthorsCount = 0;
    if (select) {
        for (var i = 0; i < select.options.length; i++) {
            if (select.options[i].selected) {
                selectedAuthorsCount++;
            }
        }
    }
    if (selectedAuthorsCount === 0) {
        errors.push('Vui lòng chọn ít nhất một tác giả.');
    }

    var yearVal = document.getElementById('publishYearInput').value.trim();
    if (yearVal) {
        var y = parseInt(yearVal);
        var currentYear = new Date().getFullYear();
        if (isNaN(y) || y < 1000 || y > currentYear) errors.push('Năm xuất bản phải từ 1000 đến ' + currentYear + '.');
    }

    var priceVal = document.getElementById('priceInput').value.trim();
    if (priceVal && parseInt(priceVal) < 0) errors.push('Giá sách không được âm.');

    var qtyVal = parseInt(document.getElementById('quantityInput').value) || 0;
    var availVal = parseInt(document.getElementById('availableInput').value) || 0;
    if (qtyVal < 0) errors.push('Số lượng không được âm.');
    if (availVal < 0) errors.push('Số lượng có sẵn không được âm.');
    if (availVal > qtyVal) errors.push('Số lượng có sẵn không được lớn hơn tổng số lượng.');

    var coverImageFile = document.getElementById('coverImageFile');
    if (coverImageFile && coverImageFile.files && coverImageFile.files[0]) {
        var file = coverImageFile.files[0];
        if (!file.type.startsWith('image/')) {
            errors.push('Ảnh bìa phải là định dạng hình ảnh hợp lệ.');
        } else if (file.size > 5 * 1024 * 1024) {
            errors.push('Kích thước ảnh bìa không được vượt quá 5MB.');
        }
    }

    if (errors.length > 0) {
        e.preventDefault();
        alert(errors.join('\n'));

        // Re-apply search filter after blocking submit so UI doesn't reset unexpectedly
        var searchInput = document.getElementById('authorSearch');
        if (searchInput && select) {
            var filter = searchInput.value.toLowerCase().trim();
            select.innerHTML = '';
            for (var i = 0; i < window.originalAuthorOptions.length; i++) {
                var opt = window.originalAuthorOptions[i];
                if (opt.text.toLowerCase().indexOf(filter) > -1) {
                    var el = document.createElement('option');
                    el.value = opt.value;
                    el.text = opt.text;
                    el.selected = opt.selected;
                    select.appendChild(el);
                }
            }
        }
    }
});

// ---- Searchable Author Select Box ----
(function() {
    var searchInput = document.getElementById('authorSearch');
    var select = document.getElementById('authorSelect');
    if (!searchInput || !select) return;

    window.originalAuthorOptions = [];
    for (var i = 0; i < select.options.length; i++) {
        window.originalAuthorOptions.push({
            value: select.options[i].value,
            text: select.options[i].text,
            selected: select.options[i].selected
        });
    }

    // Sync selected state on manual selection changes
    select.addEventListener('change', function() {
        for (var i = 0; i < select.options.length; i++) {
            var val = select.options[i].value;
            var isSel = select.options[i].selected;
            for (var j = 0; j < window.originalAuthorOptions.length; j++) {
                if (window.originalAuthorOptions[j].value === val) {
                    window.originalAuthorOptions[j].selected = isSel;
                    break;
                }
            }
        }
    });

    // Rebuild options on search input
    searchInput.addEventListener('input', function() {
        var filter = this.value.toLowerCase().trim();
        select.innerHTML = '';
        for (var i = 0; i < window.originalAuthorOptions.length; i++) {
            var opt = window.originalAuthorOptions[i];
            if (opt.text.toLowerCase().indexOf(filter) > -1) {
                var el = document.createElement('option');
                el.value = opt.value;
                el.text = opt.text;
                el.selected = opt.selected;
                select.appendChild(el);
            }
        }
    });
})();

function previewImage(input, previewId) {
    var preview = document.getElementById(previewId);
    if (input.files && input.files[0]) {
        var file = input.files[0];
        if (!file.type.startsWith('image/')) {
            alert('Vui lòng chọn một tệp hình ảnh hợp lệ (JPG, PNG, GIF, WEBP, etc.)!');
            input.value = '';
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            alert('Kích thước ảnh không được vượt quá 5MB!');
            input.value = '';
            return;
        }
        var reader = new FileReader();
        reader.onload = function(e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
        }
        reader.readAsDataURL(input.files[0]);
    }
}
</script>
