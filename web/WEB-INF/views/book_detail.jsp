<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Book, model.Author, model.User, model.BookReview, java.util.List" %>
<%--
    Trang chi tiết sách (Book Detail).
    Controller: BookDetailServlet
    Request attributes:
        - book (Book): Thông tin sách cần hiển thị
        - authors (List<Author>): Danh sách tác giả của sách
        - errorMsg (String): Thông báo lỗi (nếu có)
        - isAdmin (Boolean): Trạng thái admin/thủ thư
        - reviews (List<BookReview>): Danh sách đánh giá sách
        - avgRating (String): Điểm đánh giá trung bình
        - myReview (BookReview): Đánh giá của người dùng hiện tại
        - canReview (Boolean): Cho phép người dùng đánh giá hay không
        - rolePath (String): Đường dẫn phân quyền (ví dụ: /librarian, /admin)
    Session attributes:
        - loggedUser (User): Người dùng đã đăng nhập
--%>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<%
    User loggedUser = (User) session.getAttribute("loggedUser");
    Book book = (Book) request.getAttribute("book");
    List<Author> authors = (List<Author>) request.getAttribute("authors");
    String errorMsg = (String) request.getAttribute("errorMsg");
    Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
    if (isAdmin == null) isAdmin = false;

    List<BookReview> reviews = (List<BookReview>) request.getAttribute("reviews");
    String avgRating = (String) request.getAttribute("avgRating");
    BookReview myReview = (BookReview) request.getAttribute("myReview");
    Boolean canReview = (Boolean) request.getAttribute("canReview");
    if (canReview == null) canReview = false;

    String ctx = request.getContextPath();
    String rolePath = (String) request.getAttribute("rolePath");
    String backUrl = ctx + (rolePath != null ? rolePath + "/books" : "/books");

    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>

<main class="page-wrapper">

    <!-- ===== PAGE HEADER ===== -->
    <div class="books-page-header">
        <div class="container">
            <div class="books-page-header-inner">
                <div>
                    <div class="hero-eyebrow">
                        <i class="fa-solid fa-book-open"></i> Chi tiết sách
                    </div>
                    <h1 class="books-page-title">
                        <%= book != null ? book.getTitle() : "Sách không tồn tại" %>
                    </h1>
                    <p class="books-page-subtitle">
                        <a href="<%= backUrl %>" style="color:var(--primary);"><i class="fa-solid fa-arrow-left"></i> Quay lại danh sách</a>
                    </p>
                </div>
                <% if (book != null) { %>
                    <div class="books-page-stats">
                        <div class="bps-item">
                            <span class="bps-num"><%= book.getAvailable() %>/<%= book.getQuantity() %></span>
                            <span class="bps-lbl">Bản sao còn</span>
                        </div>
                    </div>
                <% } %>
            </div>
        </div>
    </div>

    <div class="container" style="padding-top:28px;">

        <!-- Alerts -->
        <% if ("created".equals(success)) { %>
            <div class="alert alert-success"><i class="fa-solid fa-circle-check"></i> Thêm sách thành công!</div>
        <% } else if ("updated".equals(success)) { %>
            <div class="alert alert-success"><i class="fa-solid fa-circle-check"></i> Cập nhật sách thành công!</div>
        <% } else if ("review_added".equals(success)) { %>
            <div class="alert alert-success"><i class="fa-solid fa-circle-check"></i> Đánh giá sách thành công!</div>
        <% } else if ("review_updated".equals(success)) { %>
            <div class="alert alert-success"><i class="fa-solid fa-circle-check"></i> Cập nhật đánh giá thành công!</div>
        <% } else if ("review_deleted".equals(success)) { %>
            <div class="alert alert-success"><i class="fa-solid fa-circle-check"></i> Xóa đánh giá thành công!</div>
        <% } %>

        <% if ("has_copies".equals(error)) { %>
            <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark"></i> Không thể xóa sách: còn bản sao vật lý liên kết.</div>
        <% } else if ("has_active".equals(error)) { %>
            <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark"></i> Không thể xóa sách: đang có lượt mượn hoặc đặt trước.</div>
        <% } else if ("delete_failed".equals(error) || "exception".equals(error)) { %>
            <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark"></i> Thao tác thất bại do lỗi hệ thống hoặc dữ liệu liên kết.</div>
        <% } else if ("edit_expired".equals(error)) { %>
            <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark"></i> Đã quá hạn 7 ngày kể từ lúc tạo, không thể chỉnh sửa đánh giá này.</div>
        <% } else if ("not_found".equals(error)) { %>
            <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark"></i> Không tìm thấy đánh giá.</div>
        <% } %>

        <% if (errorMsg != null) { %>
            <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark"></i> <%= errorMsg %></div>
        <% } %>

        <% if (book == null) { %>
            <div class="empty-state" style="padding:80px 24px;">
                <div class="empty-icon"><i class="fa-solid fa-book"></i></div>
                <h3>Không tìm thấy sách</h3>
                <p>Sách bạn đang tìm không tồn tại hoặc đã bị xóa.</p>
                <a href="<%= backUrl %>" class="btn btn-outline" style="margin-top:16px;">
                    <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách
                </a>
            </div>
        <% } else { %>

            <!-- ===== BOOK DETAIL CONTENT ===== -->
            <div class="book-detail-layout">
                <!-- Left: Cover -->
                <div class="book-detail-cover-section">
                    <div class="book-detail-cover">
                        <% if (book.getCoverImage() != null && !book.getCoverImage().trim().isEmpty()) { %>
                            <img src="<%= utils.UploadUtility.resolveUrl(book.getCoverImage(), request.getContextPath()) %>"
                                 alt="<%= book.getTitle() %>"
                                 onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
                            <div class="book-detail-cover-placeholder" style="display:none;">
                                <i class="fa-solid fa-book-open"></i>
                                <span><%= book.getTitle() %></span>
                            </div>
                        <% } else { %>
                            <div class="book-detail-cover-placeholder">
                                <i class="fa-solid fa-book-open"></i>
                                <span><%= book.getTitle() %></span>
                            </div>
                        <% } %>
                    </div>
                    <div class="book-detail-status">
                        <span class="badge <%= book.getAvailable() > 0 ? "badge-success" : (book.getQuantity() > 0 ? "badge-warning" : "badge-danger") %>"
                              style="font-size:0.85rem; padding:6px 16px;">
                            <i class="fa-solid <%= book.getAvailable() > 0 ? "fa-circle-check" : "fa-circle-xmark" %>"></i>
                            <%= book.getAvailabilityLabel() %>
                        </span>
                    </div>

                    <!-- User: mượn trực tiếp khi còn bản sao, đặt trước khi đã hết bản sao -->
                    <% if (loggedUser != null && !loggedUser.isAdminOrLibrarian()) { %>
                        <div class="book-detail-actions book-detail-reader-actions">
                            <% if (book.getAvailable() > 0) { %>
                                <form method="post" action="<%= ctx %>/borrow/create" class="book-detail-reader-form">
                                    <input type="hidden" name="bookId" value="<%= book.getId() %>">
                                    <button type="submit" class="btn book-detail-reader-button">
                                        <i class="fa-solid fa-book-open"></i> Mượn sách
                                    </button>
                                </form>
                            <% } else { %>
                                <a href="<%= ctx %>/reservation/create?bookId=<%= book.getId() %>" class="btn book-detail-reader-button">
                                    <i class="fa-solid fa-bookmark"></i> Đặt trước sách này
                                </a>
                            <% } %>
                        </div>
                    <% } %>

                    <!-- Admin Actions -->
                    <% if (isAdmin) { %>
                        <div class="book-detail-actions">
                            <a href="<%= ctx %><%= rolePath != null ? rolePath : "" %>/book/edit?id=<%= book.getId() %>"
                               class="btn btn-primary" style="flex:1;">
                                <i class="fa-solid fa-pen"></i> Chỉnh sửa
                            </a>
                            <button type="button" class="btn btn-danger" style="flex:1;" onclick="confirmDeleteBook()">
                                <i class="fa-solid fa-trash"></i> Xóa sách
                            </button>
                        </div>
                    <% } %>
                </div>

                <!-- Right: Info -->
                <div class="book-detail-info-section">
                    <!-- Basic Info Card -->
                    <div class="detail-card">
                        <div class="detail-card-header">
                            <i class="fa-solid fa-circle-info"></i> Thông tin cơ bản
                        </div>
                        <div class="detail-card-body">
                            <div class="detail-grid">
                                <div class="detail-item">
                                    <span class="detail-label"><i class="fa-solid fa-hashtag"></i> ISBN</span>
                                    <span class="detail-value"><code><%= book.getIsbn() %></code></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label"><i class="fa-solid fa-heading"></i> Tiêu đề</span>
                                    <span class="detail-value" style="font-weight:600;"><%= book.getTitle() %></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label"><i class="fa-solid fa-layer-group"></i> Danh mục</span>
                                    <span class="detail-value">
                                        <a href="<%= ctx %>/books?category=<%= java.net.URLEncoder.encode(book.getCategory() != null ? book.getCategory() : "", "UTF-8") %>"
                                           class="badge badge-primary">
                                            <%= book.getCategory() != null ? book.getCategory() : "—" %>
                                        </a>
                                    </span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label"><i class="fa-solid fa-users"></i> Tác giả</span>
                                    <span class="detail-value">
                                        <% if (authors != null && !authors.isEmpty()) {
                                            StringBuilder authorNames = new StringBuilder();
                                            for (int i = 0; i < authors.size(); i++) {
                                                if (i > 0) authorNames.append(", ");
                                                authorNames.append(authors.get(i).getName());
                                            }
                                        %>
                                            <%= authorNames.toString() %>
                                        <% } else { %>
                                            <span style="color:var(--text-muted);">Chưa có thông tin</span>
                                        <% } %>
                                    </span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label"><i class="fa-solid fa-building"></i> Nhà xuất bản</span>
                                    <span class="detail-value"><%= book.getPublisher() != null ? book.getPublisher() : "—" %></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label"><i class="fa-solid fa-calendar"></i> Năm xuất bản</span>
                                    <span class="detail-value"><%= book.getPublishYear() != null ? book.getPublishYear() : "—" %></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label"><i class="fa-solid fa-tag"></i> Giá</span>
                                    <span class="detail-value" style="color:var(--accent); font-weight:700;"><%= book.getFormattedPrice() %></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label"><i class="fa-solid fa-book"></i> Môn học</span>
                                    <span class="detail-value"><%= book.getSubject() != null ? book.getSubject() : "—" %></span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Inventory Card -->
                    <div class="detail-card">
                        <div class="detail-card-header" style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px;">
                            <span><i class="fa-solid fa-boxes-stacked"></i> Thông tin tồn kho</span>
                            <% if (loggedUser != null && loggedUser.isAdminOrLibrarian()) { %>
                                <a href="<%= ctx %><%= rolePath != null ? rolePath : "" %>/book/copies?bookId=<%= book.getId() %>"
                                   class="btn btn-outline btn-sm" style="font-size: 0.75rem; padding: 4px 10px; font-weight: 500;">
                                    <i class="fa-solid fa-list"></i> Quản lý bản sao
                                </a>
                            <% } %>
                        </div>
                        <div class="detail-card-body">
                            <div class="detail-grid">
                                <div class="detail-item">
                                    <span class="detail-label"><i class="fa-solid fa-cubes"></i> Tổng số bản</span>
                                    <span class="detail-value" style="font-weight:700; font-size:1.1rem;"><%= book.getQuantity() %></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label"><i class="fa-solid fa-check-circle"></i> Khả dụng</span>
                                    <span class="detail-value" style="color:<%= book.getAvailable() > 0 ? "var(--success)" : "var(--danger)" %>; font-weight:700; font-size:1.1rem;"><%= book.getAvailable() %></span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Description Card -->
                    <% if (book.getDescription() != null && !book.getDescription().trim().isEmpty()) { %>
                        <div class="detail-card">
                            <div class="detail-card-header">
                                <i class="fa-solid fa-align-left"></i> Mô tả
                            </div>
                            <div class="detail-card-body">
                                <p style="color:var(--text-secondary); line-height:1.8; font-size:0.92rem;"><%= book.getDescription() %></p>
                            </div>
                        </div>
                    <% } %>
                </div>
            </div>

            <!-- ===== REVIEWS SECTION (Reader only) ===== -->
            <% boolean isAdminOrLib = loggedUser != null && (loggedUser.isAdmin() || "LIBRARIAN".equals(loggedUser.getRole())); %>
            <% if (!isAdminOrLib) { %>
                <div class="book-detail-reviews" style="margin-top: 40px; border-top: 1px solid var(--border-light); padding-top: 30px;">
                    <h3 style="margin-bottom: 25px; font-weight: 700; color: var(--text-primary); display: flex; align-items: center; gap: 10px;">
                        <i class="fa-solid fa-star" style="color: #f5a623;"></i> Đánh giá sách
                    </h3>

                    <!-- Summary -->
                    <div class="detail-card" style="margin-bottom: 30px; text-align: center; max-width: 320px; margin-left: auto; margin-right: auto; padding: 20px;">
                        <div class="detail-card-body" style="padding: 0;">
                            <div style="font-size: 3rem; font-weight: 800; color: #f5a623; line-height: 1;">
                                <%= avgRating != null && !avgRating.equals("0.0") ? avgRating : "0" %>
                                <span style="font-size: 1.5rem; color: var(--text-muted);">/ 5</span>
                            </div>
                            <div style="color: #f5a623; font-size: 1.4rem; margin: 10px 0;">
                                <%
                                    double avgR = 0;
                                    try {
                                        if (avgRating != null) avgR = Double.parseDouble(avgRating);
                                    } catch (Exception e) {}
                                    for (int i = 1; i <= 5; i++) {
                                        if (i <= Math.round(avgR)) {
                                            out.print("★");
                                        } else {
                                            out.print("<span style='color: #e0e0e0;'>★</span>");
                                        }
                                    }
                                %>
                            </div>
                            <div style="color: var(--text-muted); font-weight: 500;">
                                <%= reviews != null ? reviews.size() : 0 %> đánh giá
                            </div>
                        </div>
                    </div>

                    <!-- Review Form for Reader -->
                    <% if (loggedUser != null && "READER".equals(loggedUser.getRole())) { %>
                        <% if (canReview) { %>
                            <div class="detail-card" style="margin-bottom: 35px; box-shadow: 0 4px 20px rgba(0,0,0,0.05);">
                                <div class="detail-card-header" style="background: rgba(245, 166, 35, 0.1); border-bottom: none; color: #d08819; font-weight: 700;">
                                    <i class="fa-solid fa-pen-to-square"></i> Viết đánh giá
                                </div>
                                <div class="detail-card-body">
                                    <form method="post" action="<%= ctx %>/book-review">
                                        <input type="hidden" name="bookId" value="<%= book.getId() %>">
                                        <input type="hidden" name="action" value="add">
                                        <%
                                            Integer unreviewedBorrowId = (Integer) request.getAttribute("unreviewedBorrowId");
                                            if (unreviewedBorrowId != null) {
                                        %>
                                            <input type="hidden" name="borrowId" value="<%= unreviewedBorrowId %>">
                                        <% } %>

                                        <div style="margin-bottom: 20px;">
                                            <label style="font-weight: 600; margin-bottom: 12px; display: block; color: var(--text-primary);">
                                                Số sao <span style="color:var(--danger)">*</span>
                                            </label>
                                            <div style="display: flex; gap: 8px;" class="star-rating-input-group">
                                                <% for (int star = 1; star <= 5; star++) { %>
                                                    <i class="fa-regular fa-star rating-star"
                                                       data-value="<%= star %>"
                                                       style="cursor:pointer; font-size:2.2rem; color:#f5a623; transition: transform 0.1s;"></i>
                                                <% } %>
                                                <input type="hidden" name="rating" id="ratingInput" required>
                                            </div>
                                        </div>

                                        <div style="margin-bottom: 20px;">
                                            <label style="font-weight: 600; margin-bottom: 10px; display: block; color: var(--text-primary);">Nhận xét</label>
                                            <textarea name="comment" rows="3"
                                                      style="width:100%; padding: 12px; border: 1px solid var(--border-light); border-radius: 8px; font-family: inherit; font-size: 0.95rem; resize: vertical;"
                                                      placeholder="Chia sẻ cảm nhận của bạn..."></textarea>
                                        </div>

                                        <div style="display: flex; gap: 12px; align-items: center;">
                                            <button type="submit" class="btn btn-primary"
                                                    style="background: linear-gradient(135deg, #f5a623, #f18d00); border: none; font-weight: 600;">
                                                Gửi đánh giá
                                            </button>
                                        </div>
                                    </form>

                                    <script>
                                        document.querySelectorAll('.rating-star').forEach(star => {
                                            star.addEventListener('mouseover', function () {
                                                let val = this.getAttribute('data-value');
                                                document.querySelectorAll('.rating-star').forEach(s => {
                                                    if (s.getAttribute('data-value') <= val) {
                                                        s.classList.remove('fa-regular');
                                                        s.classList.add('fa-solid');
                                                    } else {
                                                        s.classList.add('fa-regular');
                                                        s.classList.remove('fa-solid');
                                                    }
                                                });
                                            });

                                            star.addEventListener('click', function () {
                                                document.getElementById('ratingInput').value = this.getAttribute('data-value');
                                                this.style.transform = 'scale(1.2)';
                                                setTimeout(() => this.style.transform = 'scale(1)', 150);
                                            });
                                        });

                                        document.querySelector('.star-rating-input-group').addEventListener('mouseleave', function () {
                                            let selectedVal = document.getElementById('ratingInput').value;
                                            document.querySelectorAll('.rating-star').forEach(s => {
                                                if (selectedVal && s.getAttribute('data-value') <= selectedVal) {
                                                    s.classList.remove('fa-regular');
                                                    s.classList.add('fa-solid');
                                                } else {
                                                    s.classList.add('fa-regular');
                                                    s.classList.remove('fa-solid');
                                                }
                                            });
                                        });

                                        document.querySelector('form[action="<%= ctx %>/book-review"]').addEventListener('submit', function (e) {
                                            if (!document.getElementById('ratingInput').value) {
                                                e.preventDefault();
                                                alert('Vui lòng chọn số sao đánh giá!');
                                            }
                                        });
                                    </script>
                                </div>
                            </div>
                        <% } else if (myReview == null) { %>
                            <div class="alert alert-info" style="margin-bottom: 35px;">
                                <i class="fa-solid fa-circle-info"></i> Bạn cần mượn và trả sách này trước khi có thể đánh giá.
                            </div>
                        <% } %>
                    <% } %>

                    <!-- List of Reviews -->
                    <h4 style="margin-bottom: 20px; font-weight: 600; font-size: 1.2rem;">Tất cả đánh giá</h4>
                    <% if (reviews == null || reviews.isEmpty()) { %>
                        <div style="text-align: center; color: var(--text-muted); padding: 40px 20px; border: 2px dashed var(--border-light); border-radius: 12px; font-size: 1.05rem;">
                            <i class="fa-regular fa-comment-dots" style="font-size: 2rem; margin-bottom: 10px; display: block;"></i>
                            Chưa có đánh giá nào.
                        </div>
                    <% } else { %>
                        <div style="display: flex; flex-direction: column; gap: 18px;">
                            <% for (BookReview rev : reviews) { %>
                                <div class="detail-card" style="border-left: 5px solid #f5a623; border-radius: 8px;">
                                    <div class="detail-card-body" style="padding: 20px;">
                                        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; flex-wrap: wrap; gap: 10px;">
                                            <div style="display: flex; align-items: center; gap: 10px;">
                                                <div style="width: 36px; height: 36px; border-radius: 50%; background: var(--border-light); display: flex; align-items: center; justify-content: center; font-weight: 700; color: var(--text-secondary);">
                                                    <%= rev.getUserFullName() != null && !rev.getUserFullName().isEmpty() ? rev.getUserFullName().substring(0, 1).toUpperCase() : "?" %>
                                                </div>
                                                <div>
                                                    <span style="font-weight: 700; color: var(--text-primary);"><%= rev.getUserFullName() %></span>
                                                    <% if (rev.getUserStudentId() != null && !rev.getUserStudentId().isEmpty()) { %>
                                                        <span style="color: var(--text-muted); font-size: 0.85rem; margin-left: 5px;">(<%= rev.getUserStudentId() %>)</span>
                                                    <% } %>
                                                    <% if (rev.getBorrowDate() != null) { %>
                                                        <div style="color: #2e7d32; font-size: 0.8rem; font-weight: 600; margin-top: 2px; display: flex; align-items: center; gap: 4px;">
                                                            <i class="fa-solid fa-circle-check" style="font-size: 0.85rem;"></i> Xác thực đã mượn ngày <%= rev.getBorrowDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) %>
                                                        </div>
                                                    <% } %>
                                                </div>
                                            </div>
                                            <div style="display: flex; align-items: center; gap: 10px;">
                                                <span style="color: var(--text-muted); font-size: 0.85rem; background: var(--bg-hover); padding: 4px 10px; border-radius: 20px;">
                                                    <%= new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(rev.getCreatedAt()) %>
                                                </span>
                                                <%
                                                    boolean isOwnReview = loggedUser != null && loggedUser.getId() == rev.getUserId();
                                                    boolean canEdit = false;
                                                    if (isOwnReview) {
                                                        long diff = System.currentTimeMillis() - rev.getCreatedAt().getTime();
                                                        canEdit = diff <= 7L * 24 * 60 * 60 * 1000;
                                                    }
                                                %>
                                                <% if (canEdit) { %>
                                                    <button type="button" title="Sửa đánh giá"
                                                            onclick="openEditReviewModal('<%= rev.getId() %>', '<%= rev.getRating() %>', '<%= rev.getComment() != null ? rev.getComment().replace("'", "\\'").replace("\n", "\\n").replace("\r", "") : "" %>')"
                                                            style="background: none; border: none; color: var(--primary); cursor: pointer; padding: 4px; border-radius: 4px; transition: background 0.2s;"
                                                            onmouseover="this.style.background='rgba(0,0,0,0.05)'"
                                                            onmouseout="this.style.background='none'">
                                                        <i class="fa-solid fa-pen"></i>
                                                    </button>
                                                <% } %>
                                                <% if (isOwnReview || (loggedUser != null && loggedUser.isAdmin())) { %>
                                                    <form method="post" action="<%= ctx %>/book-review" style="margin: 0;" onsubmit="return confirm('Bạn có chắc muốn xóa đánh giá này?')">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="bookId" value="<%= book.getId() %>">
                                                        <input type="hidden" name="reviewId" value="<%= rev.getId() %>">
                                                        <button type="submit" title="Xóa đánh giá"
                                                                style="background: none; border: none; color: var(--danger); cursor: pointer; padding: 4px; border-radius: 4px; transition: background 0.2s;"
                                                                onmouseover="this.style.background='rgba(255,0,0,0.1)'"
                                                                onmouseout="this.style.background='none'">
                                                            <i class="fa-solid fa-trash"></i>
                                                        </button>
                                                    </form>
                                                <% } %>
                                            </div>
                                        </div>
                                        <div style="color: #f5a623; font-size: 1.1rem; margin: 12px 0 10px 46px;">
                                            <% for (int i = 1; i <= 5; i++) {
                                                if (i <= rev.getRating()) {
                                                    out.print("★");
                                                } else {
                                                    out.print("<span style='color: #e0e0e0;'>★</span>");
                                                }
                                            } %>
                                            <span style="color: var(--text-muted); font-size: 0.9rem; margin-left: 8px; font-weight: 500;">(<%= rev.getRating() %>/5)</span>
                                        </div>
                                        <% if (rev.getComment() != null && !rev.getComment().trim().isEmpty()) { %>
                                            <p style="margin: 0 0 0 46px; color: var(--text-secondary); line-height: 1.6; font-size: 0.95rem;">
                                                <%= rev.getComment().replace("\n", "<br>") %>
                                            </p>
                                        <% } %>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    <% } %>
                </div>
            <% } %>

        <% } %>
    </div><!-- /container -->
</main>

    <!-- ===== EDIT REVIEW MODAL ===== -->
    <div id="editReviewModal"
         style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.7); z-index:9999; align-items:center; justify-content:center; backdrop-filter:blur(4px);">
        <div style="background:var(--bg-card); border:1px solid var(--border-light); border-radius:var(--radius-lg); padding:30px; max-width:480px; width:90%; box-shadow:var(--shadow-lg); position:relative;">
            <h3 style="font-size:1.2rem; font-weight:700; color:var(--text-primary); margin-bottom:20px; display:flex; align-items:center; gap:8px;">
                <i class="fa-solid fa-pen-to-square" style="color:#f5a623;"></i> Chỉnh sửa đánh giá
            </h3>
            <form method="post" action="<%= ctx %>/book-review" id="editReviewForm">
                <input type="hidden" name="action" value="edit">
                <input type="hidden" name="bookId" value="<%= book != null ? book.getId() : "" %>">
                <input type="hidden" name="reviewId" id="editReviewId">
                
                <div style="margin-bottom: 20px;">
                    <label style="font-weight: 600; margin-bottom: 12px; display: block; color: var(--text-primary);">
                        Số sao <span style="color:var(--danger)">*</span>
                    </label>
                    <div style="display: flex; gap: 8px;" class="edit-star-rating-group">
                        <% for (int star = 1; star <= 5; star++) { %>
                            <i class="fa-regular fa-star edit-rating-star"
                               data-value="<%= star %>"
                               style="cursor:pointer; font-size:2.2rem; color:#f5a623; transition: transform 0.1s;"></i>
                        <% } %>
                        <input type="hidden" name="rating" id="editRatingInput" required>
                    </div>
                </div>

                <div style="margin-bottom: 20px;">
                    <label style="font-weight: 600; margin-bottom: 10px; display: block; color: var(--text-primary);">Nhận xét</label>
                    <textarea name="comment" id="editCommentInput" rows="3"
                              style="width:100%; padding: 12px; border: 1px solid var(--border-light); border-radius: 8px; font-family: inherit; font-size: 0.95rem; resize: vertical;"
                              placeholder="Chia sẻ cảm nhận của bạn..."></textarea>
                </div>

                <div style="display:flex; gap:12px; justify-content:flex-end;">
                    <button type="button" onclick="closeEditReviewModal()" class="btn btn-outline">Hủy</button>
                    <button type="submit" class="btn btn-primary" style="background: linear-gradient(135deg, #f5a623, #f18d00); border: none; font-weight: 600;">
                        Lưu thay đổi
                    </button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openEditReviewModal(reviewId, rating, comment) {
            document.getElementById('editReviewId').value = reviewId;
            document.getElementById('editRatingInput').value = rating;
            document.getElementById('editCommentInput').value = comment;
            
            updateEditStars(rating);
            
            document.getElementById('editReviewModal').style.display = 'flex';
        }

        function closeEditReviewModal() {
            document.getElementById('editReviewModal').style.display = 'none';
        }

        function updateEditStars(rating) {
            document.querySelectorAll('.edit-rating-star').forEach(s => {
                if (s.getAttribute('data-value') <= rating) {
                    s.classList.remove('fa-regular');
                    s.classList.add('fa-solid');
                } else {
                    s.classList.add('fa-regular');
                    s.classList.remove('fa-solid');
                }
            });
        }

        document.querySelectorAll('.edit-rating-star').forEach(star => {
            star.addEventListener('mouseover', function () {
                let val = this.getAttribute('data-value');
                document.querySelectorAll('.edit-rating-star').forEach(s => {
                    if (s.getAttribute('data-value') <= val) {
                        s.classList.remove('fa-regular');
                        s.classList.add('fa-solid');
                    } else {
                        s.classList.add('fa-regular');
                        s.classList.remove('fa-solid');
                    }
                });
            });

            star.addEventListener('click', function () {
                document.getElementById('editRatingInput').value = this.getAttribute('data-value');
                this.style.transform = 'scale(1.2)';
                setTimeout(() => this.style.transform = 'scale(1)', 150);
            });
        });

        document.querySelector('.edit-star-rating-group').addEventListener('mouseleave', function () {
            let selectedVal = document.getElementById('editRatingInput').value;
            updateEditStars(selectedVal);
        });

        document.getElementById('editReviewModal').addEventListener('click', function (e) {
            if (e.target === this) closeEditReviewModal();
        });
    </script>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>

<% if (book != null && isAdmin) { %>
    <!-- ===== DELETE CONFIRMATION MODAL ===== -->
    <div id="deleteBookModal"
         style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.7); z-index:9999; align-items:center; justify-content:center; backdrop-filter:blur(4px);">
        <div style="background:var(--bg-card); border:1px solid var(--border-light); border-radius:var(--radius-lg); padding:36px; max-width:440px; width:90%; box-shadow:var(--shadow-lg); position:relative;">
            <div style="font-size:2.5rem; margin-bottom:14px; text-align:center;">🗑️</div>
            <h3 style="font-size:1.15rem; font-weight:700; color:var(--text-primary); margin-bottom:10px; text-align:center;">Xác nhận xóa sách</h3>
            <p style="color:var(--text-secondary); font-size:0.9rem; margin-bottom:28px; text-align:center; line-height:1.6;">
                Bạn có chắc muốn xóa sách: "<strong><%= book.getTitle() %></strong>"?<br>
                <span style="color:var(--danger); font-size:0.82rem;">Hành động này không thể hoàn tác.</span>
            </p>
            <div style="display:flex; gap:12px; justify-content:flex-end;">
                <button onclick="document.getElementById('deleteBookModal').style.display='none'" class="btn btn-outline">Hủy</button>
                <a href="<%= ctx %>/book/delete?id=<%= book.getId() %>" class="btn btn-danger">
                    <i class="fa-solid fa-trash"></i> Xóa
                </a>
            </div>
        </div>
    </div>

    <script>
        function confirmDeleteBook() {
            document.getElementById('deleteBookModal').style.display = 'flex';
        }
        document.getElementById('deleteBookModal').addEventListener('click', function (e) {
            if (e.target === this) this.style.display = 'none';
        });
    </script>
<% } %>
