<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Book, model.BookCopy, java.util.List" %>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    String formMode = (String) request.getAttribute("formMode");
    if (formMode == null) formMode = "add";
    boolean isEdit = "edit".equals(formMode);

    Book book = (Book) request.getAttribute("book");
    BookCopy copy = (BookCopy) request.getAttribute("copy");

    List<String> errors = (List<String>) request.getAttribute("errors");
    String ctx = request.getContextPath();

    // Determine current values (either from request attributes after validation failure, or from the model)
    String barcode = (String) request.getAttribute("barcode");
    if (barcode == null) {
        barcode = (isEdit && copy != null) ? copy.getBarcode() : "";
    }

    String selectedCondition = (String) request.getAttribute("selectedCondition");
    if (selectedCondition == null) {
        selectedCondition = (isEdit && copy != null) ? copy.getBookCondition() : "GOOD";
    }

    String selectedStatus = (String) request.getAttribute("selectedStatus");
    if (selectedStatus == null) {
        selectedStatus = (isEdit && copy != null) ? copy.getStatus() : "AVAILABLE";
    }
%>

<%

    String area = (String) request.getAttribute("area");
    if (area == null) {
        area = (isEdit && copy != null && copy.getArea() != null) ? copy.getArea() : "";
    }

    String shelf = (String) request.getAttribute("shelf");
    if (shelf == null) {
        shelf = (isEdit && copy != null && copy.getShelf() != null) ? copy.getShelf() : "";
    }

    String slot = (String) request.getAttribute("slot");
    if (slot == null) {
        slot = (isEdit && copy != null && copy.getSlot() != null) ? copy.getSlot() : "";
    }

    String note = (String) request.getAttribute("note");
    if (note == null) {
        note = (isEdit && copy != null && copy.getNote() != null) ? copy.getNote() : "";
    }

    int bookId = (book != null) ? book.getId() : 0;
    int copyId = (copy != null) ? copy.getId() : 0;

    String rolePath = (String) request.getAttribute("rolePath");
    if (rolePath == null) rolePath = "";
%>

<main class="page-wrapper">

<!-- ===== PAGE HEADER ===== -->
<div class="books-page-header">
    <div class="container">
        <div class="books-page-header-inner">
            <div>
                <div class="hero-eyebrow" style="margin-bottom:10px;">
                    <i class="fa-solid fa-<%= isEdit ? "pen-to-square" : "plus" %>"></i>
                    <%= isEdit ? "Chỉnh sửa bản sao" : "Thêm bản sao vật lý" %>
                </div>
                <h1 class="books-page-title"><%= isEdit ? "Chỉnh sửa: " + barcode : "Thêm bản sao mới" %></h1>
                <p class="books-page-subtitle">
                    <span style="color: var(--text-muted);">Đầu sách:</span> 
                    <strong style="color: var(--text-primary);"><%= book != null ? book.getTitle() : "" %></strong> 
                    <span style="margin: 0 8px; color: var(--border-light);">|</span> 
                    <span style="color: var(--text-muted);">ISBN:</span> 
                    <code><%= book != null ? book.getIsbn() : "" %></code>
                </p>
            </div>
            <div class="books-page-stats">
                <a href="<%= ctx %><%= rolePath %>/book/copies?bookId=<%= bookId %>" class="back-btn-custom">
                    <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách bản sao
                </a>
            </div>
        </div>
    </div>
</div>

<div class="container" style="padding-top:28px; padding-bottom:40px; max-width: 800px;">

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

    <!-- ===== FORM ===== -->
    <form id="copyForm" action="<%= isEdit ? ctx + rolePath + "/book/copy/edit" : ctx + rolePath + "/book/copy/add" %>" method="post" novalidate>
        <% if (isEdit) { %>
            <input type="hidden" name="id" value="<%= copyId %>">
        <% } %>
        <input type="hidden" name="bookId" value="<%= bookId %>">

        <div class="detail-card">
            <div class="detail-card-header">
                <i class="fa-solid fa-barcode"></i> Thông tin bản sao
            </div>
            <div class="detail-card-body">
                
                <!-- Barcode -->
                <div class="form-group">
                    <label for="barcodeInput" class="form-label">
                        Mã bản sao (Barcode) <span class="required">*</span>
                    </label>
                    <input type="text" id="barcodeInput" name="barcode" class="form-control"
                           value="<%= barcode %>" placeholder="Nhập mã vạch barcode bản sao..."
                           maxlength="50" required
                           <%= isEdit ? "readonly style=\"background:var(--bg-surface); cursor:not-allowed; opacity:0.8;\"" : "" %>>
                    <% if (isEdit) { %>
                        <span class="form-hint" style="color:var(--warning);">
                            <i class="fa-solid fa-lock fa-xs"></i> Mã bản sao không thể thay đổi để tránh đứt gãy lịch sử mượn trả.
                        </span>
                    <% } else { %>
                        <span class="form-hint">Mã barcode duy nhất của cuốn sách này trong thư viện, tối đa 50 ký tự</span>
                    <% } %>
                </div>

                <!-- Condition and Status Row -->
                <div class="form-row">
                    <!-- Book Condition -->
                    <div class="form-group">
                        <label for="conditionSelect" class="form-label">Tình trạng (Condition)</label>
                        <select id="conditionSelect" name="bookCondition" class="form-select">
                            <option value="GOOD" <%= "GOOD".equals(selectedCondition) ? "selected" : "" %>>GOOD (Tốt)</option>
                            <option value="WORN" <%= "WORN".equals(selectedCondition) ? "selected" : "" %>>WORN (Hao mòn)</option>
                            <option value="DAMAGED" <%= "DAMAGED".equals(selectedCondition) ? "selected" : "" %>>DAMAGED (Hỏng)</option>
                            <option value="LOST" <%= "LOST".equals(selectedCondition) ? "selected" : "" %>>LOST (Mất)</option>
                        </select>
                    </div>

                    <!-- Status -->
                    <div class="form-group">
                        <label for="statusSelect" class="form-label">Trạng thái (Status)</label>
                        <select id="statusSelect" name="status" class="form-select">
                            <option value="AVAILABLE" <%= "AVAILABLE".equals(selectedStatus) ? "selected" : "" %>>AVAILABLE (Sẵn sàng)</option>
                            <option value="BORROWED" <%= "BORROWED".equals(selectedStatus) ? "selected" : "" %>>BORROWED (Đang mượn)</option>
                            <option value="MAINTENANCE" <%= "MAINTENANCE".equals(selectedStatus) ? "selected" : "" %>>MAINTENANCE (Bảo trì)</option>
                            <option value="LOST" <%= "LOST".equals(selectedStatus) ? "selected" : "" %>>LOST (Đã mất)</option>
                        </select>
                    </div>
                </div>

                <!-- Location Details Section -->
                <div style="margin: 24px 0 12px 0; border-top: 1px solid var(--border-light); padding-top: 20px;">
                    <h4 style="font-size: 0.95rem; font-weight: 600; color: var(--text-primary); margin-bottom: 16px;">
                        <i class="fa-solid fa-location-dot" style="color: var(--primary); margin-right: 6px;"></i> Vị trí kệ lưu trữ
                    </h4>
                </div>

                <div class="form-row">
                    <!-- Area -->
                    <div class="form-group">
                        <label for="areaInput" class="form-label">Khu vực / Tầng</label>
                        <input type="text" id="areaInput" name="area" class="form-control"
                               value="<%= area %>" placeholder="VD: Tầng 1"
                               maxlength="50">
                        <span class="form-hint">Tầng 1, Tầng 2, Khu A...</span>
                    </div>

                    <!-- Shelf -->
                    <div class="form-group">
                        <label for="shelfInput" class="form-label">Kệ (Shelf)</label>
                        <input type="text" id="shelfInput" name="shelf" class="form-control"
                               value="<%= shelf %>" placeholder="VD: K01"
                               maxlength="20">
                    </div>

                    <!-- Slot -->
                    <div class="form-group">
                        <label for="slotInput" class="form-label">Ngăn (Slot)</label>
                        <input type="text" id="slotInput" name="slot" class="form-control"
                               value="<%= slot %>" placeholder="VD: N02"
                               maxlength="20">
                    </div>
                </div>

                <!-- Note -->
                <div class="form-group" style="margin-top: 16px;">
                    <label for="noteInput" class="form-label">Ghi chú thêm</label>
                    <textarea id="noteInput" name="note" class="form-control" rows="3"
                              placeholder="Nhập ghi chú hoặc lý do thay đổi tình trạng (nếu có)..."
                              maxlength="255"><%= note %></textarea>
                    <span class="form-hint">Tối đa 255 ký tự</span>
                </div>

            </div>
        </div>

        <!-- Submit Buttons -->
        <div class="book-form-buttons" style="margin-top: 28px; display: flex; justify-content: flex-end; gap: 12px; border-top: 1px solid var(--border-light); padding-top: 20px;">
            <a href="<%= ctx %><%= rolePath %>/book/copies?bookId=<%= bookId %>" class="btn-cancel-custom">
                <i class="fa-solid fa-xmark"></i> Hủy
            </a>
            <button type="submit" class="btn-submit-custom" id="submitBtn">
                <i class="fa-solid fa-<%= isEdit ? "floppy-disk" : "plus" %>"></i>
                <%= isEdit ? "Lưu thay đổi" : "Thêm bản sao" %>
            </button>
        </div>
    </form>

</div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>

<script>
document.addEventListener('DOMContentLoaded', function() {
    var conditionSelect = document.getElementById('conditionSelect');
    var statusSelect = document.getElementById('statusSelect');
    var originalSelectedStatus = "<%= selectedStatus %>";

    function updateStatusOptions() {
        var condition = conditionSelect.value;
        var currentStatus = statusSelect.value || originalSelectedStatus;
        
        var allStatuses = [
            { value: 'AVAILABLE', text: 'AVAILABLE (Sẵn sàng)' },
            { value: 'BORROWED', text: 'BORROWED (Đang mượn)' },
            { value: 'MAINTENANCE', text: 'MAINTENANCE (Bảo trì)' },
            { value: 'LOST', text: 'LOST (Đã mất)' }
        ];
        
        var allowedValues = [];
        if (condition === 'GOOD') {
            allowedValues = ['AVAILABLE', 'BORROWED'];
        } else if (condition === 'WORN' || condition === 'DAMAGED') {
            allowedValues = ['MAINTENANCE'];
        } else if (condition === 'LOST') {
            allowedValues = ['LOST'];
        }
        
        statusSelect.innerHTML = '';
        
        allStatuses.forEach(function(status) {
            if (allowedValues.indexOf(status.value) !== -1) {
                var opt = document.createElement('option');
                opt.value = status.value;
                opt.textContent = status.text;
                if (status.value === currentStatus) {
                    opt.selected = true;
                }
                statusSelect.appendChild(opt);
            }
        });
        
        // If current selected status is no longer valid, select the first allowed status
        if (allowedValues.indexOf(statusSelect.value) === -1 && allowedValues.length > 0) {
            statusSelect.value = allowedValues[0];
        }
    }

    conditionSelect.addEventListener('change', function() {
        originalSelectedStatus = ""; // Reset on manual change
        updateStatusOptions();
    });

    updateStatusOptions();
});

document.getElementById('copyForm').addEventListener('submit', function(e) {
    var barcodeVal = document.getElementById('barcodeInput').value.trim();
    var areaVal = document.getElementById('areaInput').value.trim();
    var shelfVal = document.getElementById('shelfInput').value.trim();
    var slotVal = document.getElementById('slotInput').value.trim();
    var noteVal = document.getElementById('noteInput').value.trim();
    var errors = [];

    if (!barcodeVal) {
        errors.push('Mã bản sao (Barcode) không được để trống.');
    } else if (barcodeVal.length > 50) {
        errors.push('Mã bản sao (Barcode) không được vượt quá 50 ký tự.');
    }

    if (areaVal.length > 50) {
        errors.push('Tên khu vực không được vượt quá 50 ký tự.');
    }
    if (shelfVal.length > 20) {
        errors.push('Ký hiệu kệ không được vượt quá 20 ký tự.');
    }
    if (slotVal.length > 20) {
        errors.push('Ký hiệu ngăn không được vượt quá 20 ký tự.');
    }
    if (noteVal.length > 255) {
        errors.push('Nội dung ghi chú không được vượt quá 255 ký tự.');
    }

    if (errors.length > 0) {
        e.preventDefault();
        alert(errors.join('\n'));
    }
});
</script>
