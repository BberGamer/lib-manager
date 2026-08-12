<%--
    Trang chủ công khai của hệ thống thư viện FPT.
    Controller render: HomeServlet (GET /home) — không yêu cầu đăng nhập.
    Attributes yêu cầu:
      - request: "activePage" = "home"
    Attributes tuỳ chọn:
      - session: "loggedUser" (model.User) — nếu đã đăng nhập
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="activePage" value="home" scope="request" />
<c:set var="pageTitle" value="Trang chủ — FPT Library" scope="request" />

<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/home.css">

<main class="page-wrapper">

    <%-- ===== HERO ===== --%>
    <section class="home-hero">
        <div class="hero-grid-bg"></div>
        <div class="container" style="position:relative; z-index:1;">
            <div class="hero-content">
                <div class="hero-greeting">
                    <c:choose>
                        <c:when test="${not empty sessionScope.loggedUser}">
                            <span class="greeting-badge">
                                <i class="fa-solid fa-sun"></i> Xin chào
                            </span>
                            <h1 class="hero-title">
                                <c:out value="${not empty sessionScope.loggedUser.fullName
                                    ? sessionScope.loggedUser.fullName
                                    : sessionScope.loggedUser.username}" />!
                            </h1>
                            <p class="hero-sub">
                                Chào mừng bạn đến với Hệ thống Thư viện FPT University.
                                <c:choose>
                                    <c:when test="${sessionScope.loggedUser.admin}">
                                        Bạn đang đăng nhập với quyền <strong>Quản trị viên</strong>.
                                    </c:when>
                                    <c:when test="${sessionScope.loggedUser.librarian}">
                                        Bạn đang đăng nhập với quyền <strong>Thủ thư</strong>.
                                    </c:when>
                                    <c:otherwise>
                                        Hãy khám phá kho sách phong phú của chúng tôi.
                                    </c:otherwise>
                                </c:choose>
                            </p>
                        </c:when>
                        <c:otherwise>
                            <span class="greeting-badge">
                                <i class="fa-solid fa-book"></i> Thư viện FPT University
                            </span>
                            <h1 class="hero-title">Chào mừng đến với FPT Library!</h1>
                            <p class="hero-sub">
                                Khám phá kho tàng tri thức với hàng nghìn đầu sách học thuật.
                                Đăng nhập để mượn sách, đặt trước và theo dõi lịch sử mượn trả.
                            </p>
                            <div class="hero-cta">
                                <a href="${pageContext.request.contextPath}/login"
                                   class="btn-hero-primary">
                                    <i class="fa-solid fa-right-to-bracket"></i> Đăng nhập ngay
                                </a>
                                <a href="${pageContext.request.contextPath}/books"
                                   class="btn-hero-secondary">
                                    <i class="fa-solid fa-magnifying-glass"></i> Tìm kiếm sách
                                </a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="hero-illustration">
                    <i class="fa-solid fa-book-open-reader hero-icon"></i>
                </div>
            </div>
        </div>
    </section>

    <%-- ===== QUICK ACTIONS (chỉ hiện khi đã đăng nhập) ===== --%>
    <c:if test="${not empty sessionScope.loggedUser}">
        <section class="home-quick-actions">
            <div class="container">
                <h2 class="section-title">Truy cập nhanh</h2>
                <div class="quick-grid">

                    <a href="${pageContext.request.contextPath}/books" class="quick-card quick-card--blue">
                        <div class="quick-card-icon"><i class="fa-solid fa-magnifying-glass"></i></div>
                        <div class="quick-card-body">
                            <h3>Tìm kiếm sách</h3>
                            <p>Tra cứu và khám phá kho tàng tri thức</p>
                        </div>
                        <i class="fa-solid fa-arrow-right quick-card-arrow"></i>
                    </a>

                    <c:choose>
                        <c:when test="${sessionScope.loggedUser.adminOrLibrarian}">
                            <a href="${pageContext.request.contextPath}/borrow/list" class="quick-card quick-card--green">
                                <div class="quick-card-icon"><i class="fa-solid fa-hand-holding-hand"></i></div>
                                <div class="quick-card-body">
                                    <h3>Quản lý mượn trả</h3>
                                    <p>Xử lý phiếu mượn và trả sách</p>
                                </div>
                                <i class="fa-solid fa-arrow-right quick-card-arrow"></i>
                            </a>
                            <a href="${pageContext.request.contextPath}/reservation/list" class="quick-card quick-card--purple">
                                <div class="quick-card-icon"><i class="fa-solid fa-list-check"></i></div>
                                <div class="quick-card-body">
                                    <h3>Quản lý đặt trước</h3>
                                    <p>Xác nhận và xử lý phiếu đặt trước</p>
                                </div>
                                <i class="fa-solid fa-arrow-right quick-card-arrow"></i>
                            </a>
                            <a href="${pageContext.request.contextPath}/fine/list" class="quick-card quick-card--orange">
                                <div class="quick-card-icon"><i class="fa-solid fa-file-invoice-dollar"></i></div>
                                <div class="quick-card-body">
                                    <h3>Quản lý phạt</h3>
                                    <p>Tạo và xử lý phiếu phạt</p>
                                </div>
                                <i class="fa-solid fa-arrow-right quick-card-arrow"></i>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/borrow/my" class="quick-card quick-card--green">
                                <div class="quick-card-icon"><i class="fa-solid fa-book-open"></i></div>
                                <div class="quick-card-body">
                                    <h3>Sách đang mượn</h3>
                                    <p>Xem danh sách sách bạn đang mượn</p>
                                </div>
                                <i class="fa-solid fa-arrow-right quick-card-arrow"></i>
                            </a>
                            <a href="${pageContext.request.contextPath}/reservation/my" class="quick-card quick-card--purple">
                                <div class="quick-card-icon"><i class="fa-solid fa-bookmark"></i></div>
                                <div class="quick-card-body">
                                    <h3>Đặt trước của tôi</h3>
                                    <p>Quản lý phiếu đặt trước sách</p>
                                </div>
                                <i class="fa-solid fa-arrow-right quick-card-arrow"></i>
                            </a>
                            <a href="${pageContext.request.contextPath}/fine/my" class="quick-card quick-card--orange">
                                <div class="quick-card-icon"><i class="fa-solid fa-coins"></i></div>
                                <div class="quick-card-body">
                                    <h3>Phí phạt</h3>
                                    <p>Kiểm tra và thanh toán phí phạt</p>
                                </div>
                                <i class="fa-solid fa-arrow-right quick-card-arrow"></i>
                            </a>
                        </c:otherwise>
                    </c:choose>

                    <a href="${pageContext.request.contextPath}/user/profile" class="quick-card quick-card--teal">
                        <div class="quick-card-icon"><i class="fa-solid fa-user-circle"></i></div>
                        <div class="quick-card-body">
                            <h3>Hồ sơ cá nhân</h3>
                            <p>Xem và cập nhật thông tin tài khoản</p>
                        </div>
                        <i class="fa-solid fa-arrow-right quick-card-arrow"></i>
                    </a>

                </div>
            </div>
        </section>
    </c:if>

    <%-- ===== FEATURES (hiện khi chưa đăng nhập) ===== --%>
    <c:if test="${empty sessionScope.loggedUser}">
        <section class="home-features">
            <div class="container">
                <h2 class="section-title tc">Tính năng nổi bật</h2>
                <div class="features-grid">
                    <div class="feature-card">
                        <div class="feature-icon feature-icon--blue">
                            <i class="fa-solid fa-magnifying-glass"></i>
                        </div>
                        <h3>Tìm kiếm sách</h3>
                        <p>Tra cứu nhanh chóng với bộ lọc theo danh mục, tác giả, môn học</p>
                    </div>
                    <div class="feature-card">
                        <div class="feature-icon feature-icon--green">
                            <i class="fa-solid fa-book-open"></i>
                        </div>
                        <h3>Mượn trả online</h3>
                        <p>Đặt mượn và theo dõi trạng thái mượn trả mọi lúc mọi nơi</p>
                    </div>
                    <div class="feature-card">
                        <div class="feature-icon feature-icon--purple">
                            <i class="fa-solid fa-bookmark"></i>
                        </div>
                        <h3>Đặt trước sách</h3>
                        <p>Đặt giữ sách khi đang hết — nhận thông báo khi có sẵn</p>
                    </div>
                    <div class="feature-card">
                        <div class="feature-icon feature-icon--orange">
                            <i class="fa-solid fa-bell"></i>
                        </div>
                        <h3>Thông báo tự động</h3>
                        <p>Nhắc nhở hạn trả, thông báo sách mới và phiếu phạt</p>
                    </div>
                </div>
            </div>
        </section>
    </c:if>

    <%-- ===== LATEST BOOKS ===== --%>
    <c:if test="${not empty latestBooks}">
        <section class="home-latest-books" style="padding: 56px 0; background: var(--bg-dark); border-top: 1px solid var(--border); border-bottom: 1px solid var(--border);">
            <div class="container">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:28px;">
                    <h2 class="section-title" style="margin:0;">Sách mới nhất</h2>
                    <a href="${pageContext.request.contextPath}/books" class="btn btn-outline" style="font-size:0.9rem; padding: 8px 16px; display:inline-flex; align-items:center; gap:6px;">
                        Xem tất cả <i class="fa-solid fa-arrow-right"></i>
                    </a>
                </div>
                
                <div class="books-grid">
                    <c:forEach var="book" items="${latestBooks}">
                        <div class="book-card">
                            <a href="${pageContext.request.contextPath}/book/detail?id=${book.id}" class="book-cover">
                                <c:choose>
                                    <c:when test="${not empty book.coverImage}">
                                        <img src="${pageContext.request.contextPath}/uploads/${book.coverImage}" 
                                             alt="${book.title}" 
                                             onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="" style="display:none;" onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
                                    </c:otherwise>
                                </c:choose>
                                <div class="book-cover-placeholder" style="display:none;">
                                    <i class="fa-solid fa-book"></i>
                                    <span><c:out value="${book.title}" /></span>
                                </div>
                                <span class="book-status-tag ${book.available > 0 ? 'status-available' : 'status-unavailable'}">
                                    ${book.available > 0 ? 'Còn sách' : 'Hết sách'}
                                </span>
                            </a>
                            <div class="book-body">
                                <span class="book-category"><c:out value="${book.category}" /></span>
                                <a href="${pageContext.request.contextPath}/book/detail?id=${book.id}" class="book-title" title="${book.title}">
                                    <c:out value="${book.title}" />
                                </a>
                                <div class="book-publisher">Năm XB: ${book.publishYear}</div>
                                <div class="book-price">Giá: ${book.price} VNĐ</div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </section>
    </c:if>

    <%-- ===== INFO BAR ===== --%>
    <section class="home-info">
        <div class="container">
            <div class="info-cards">
                <div class="info-card">
                    <i class="fa-solid fa-clock-rotate-left info-card-icon"></i>
                    <div>
                        <h4>Giờ mở cửa</h4>
                        <p>Thứ 2 – Thứ 7: 7:30 – 20:00</p>
                    </div>
                </div>
                <div class="info-card">
                    <i class="fa-solid fa-location-dot info-card-icon"></i>
                    <div>
                        <h4>Địa chỉ</h4>
                        <p>Khu CNC Hòa Lạc, Thạch Thất, Hà Nội</p>
                    </div>
                </div>
                <div class="info-card">
                    <i class="fa-solid fa-phone info-card-icon"></i>
                    <div>
                        <h4>Liên hệ</h4>
                        <p>0247.3005.588</p>
                    </div>
                </div>
            </div>
        </div>
    </section>

</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
<script src="${pageContext.request.contextPath}/assets/js/home.js" defer></script>
