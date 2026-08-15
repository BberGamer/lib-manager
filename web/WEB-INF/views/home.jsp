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

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/home.css?v=<%= System.currentTimeMillis() %>">

<main class="page-wrapper">

    <%-- ===== HERO ===== --%>
    <section class="home-hero">
        <div class="hero-grid-bg"></div>
        <div class="container" style="position:relative; z-index:1;">
            <div class="hero-content">
                <div class="hero-greeting" style="max-width:620px;">
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
                                Chào mừng bạn đến với <strong class="brand-gradient">Thư viện FPT University</strong>.
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
                                <i class="fa-solid fa-graduation-cap"></i> FPT UNIVERSITY LIBRARY SYSTEM
                            </span>
                            <h1 class="hero-title">Kho tri thức<br><span class="brand-gradient">dành cho bạn</span></h1>
                            <p class="hero-sub">
                                Khám phá hàng nghìn đầu sách học thuật, tài liệu chuyên ngành và
                                tạp chí khoa học. Đặt mượn trực tuyến, tra cứu nhanh chóng – mọi lúc, mọi nơi.
                            </p>
                        </c:otherwise>
                    </c:choose>

                    <%-- Thanh tìm kiếm trực tiếp tại Hero --%>
                    <form action="${pageContext.request.contextPath}/books" method="GET" class="hero-search-form">
                        <div class="hero-search-input-wrap">
                            <input type="text" name="keyword" placeholder="Tìm theo tên sách, ISBN, tác giả, nhà xuất bản..." class="hero-search-input" />
                            <button type="submit" class="btn-hero-search">
                                <i class="fa-solid fa-magnifying-glass"></i>
                            </button>
                        </div>
                    </form>

                    <%-- Dải chỉ số thống kê --%>
                    <div class="hero-stats">
                        <div class="hero-stat-item">
                            <span class="hero-stat-num">${not empty totalBooks && totalBooks > 0 ? totalBooks : '450'}+</span>
                            <span class="hero-stat-label">ĐẦU SÁCH</span>
                        </div>
                        <div class="hero-stat-item">
                            <span class="hero-stat-num">${not empty totalCategories && totalCategories > 0 ? totalCategories : '5'}</span>
                            <span class="hero-stat-label">DANH MỤC</span>
                        </div>
                        <div class="hero-stat-item">
                            <span class="hero-stat-num">24/7</span>
                            <span class="hero-stat-label">TRA CỨU ONLINE</span>
                        </div>
                    </div>
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

    <%-- ===== POPULAR BOOKS (Sách được mượn nhiều nhất) ===== --%>
    <c:if test="${not empty popularBooks}">
        <section class="home-popular-books">
            <div class="container">
                <div class="section-header-flex">
                    <div>
                        <span class="section-eyebrow section-eyebrow--hot"><i class="fa-solid fa-fire"></i> Xu hướng đọc</span>
                        <h2 class="section-title" style="margin-bottom:0;">Sách được mượn nhiều nhất</h2>
                    </div>
                    <a href="${pageContext.request.contextPath}/books" class="btn-view-all">
                        Khám phá kho sách <i class="fa-solid fa-arrow-right"></i>
                    </a>
                </div>
                <div class="book-carousel-container" data-carousel-id="popular">
                    <button type="button" class="carousel-btn prev-btn" aria-label="Previous">
                        <i class="fa-solid fa-chevron-left"></i>
                    </button>
                    <div class="book-carousel-viewport">
                        <div class="latest-books-grid book-carousel-track">
                            <c:forEach var="book" items="${popularBooks}" varStatus="status">
                                <c:set var="popCoverSrc" value="${book.coverImage}" />
                                <c:if test="${not empty popCoverSrc && !fn:startsWith(popCoverSrc, 'http://') && !fn:startsWith(popCoverSrc, 'https://')}">
                                    <c:choose>
                                        <c:when test="${fn:startsWith(popCoverSrc, '/')}">
                                            <c:set var="popCoverSrc" value="${pageContext.request.contextPath}${popCoverSrc}" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="popCoverSrc" value="${pageContext.request.contextPath}/${popCoverSrc}" />
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>

                                <div class="home-book-card home-book-card--popular">
                                    <div class="pop-rank-badge pop-rank-badge--${status.index < 3 ? status.index + 1 : 'default'}">
                                        TOP ${status.index + 1}
                                    </div>
                                    <div class="hbc-cover-wrap">
                                        <c:choose>
                                            <c:when test="${not empty book.coverImage}">
                                                <img src="${popCoverSrc}"
                                                     alt=""
                                                     class="hbc-cover"
                                                     onerror="this.style.display='none';this.nextElementSibling.style.display='flex';" />
                                                <div class="hbc-cover-placeholder" style="display:none;">
                                                    <i class="fa-solid fa-book-open"></i>
                                                    <span><c:out value="${book.title}" /></span>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="hbc-cover-placeholder">
                                                    <i class="fa-solid fa-book-open"></i>
                                                    <span><c:out value="${book.title}" /></span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                        <span class="hbc-badge hbc-badge--fire">
                                            <i class="fa-solid fa-fire"></i> ${book.borrowCount > 0 ? book.borrowCount : 1} lượt mượn
                                        </span>
                                    </div>
                                    <div class="hbc-body">
                                        <h3 class="hbc-title">
                                            <a href="${pageContext.request.contextPath}/book/detail?id=${book.id}">
                                                <c:out value="${book.title}" />
                                            </a>
                                        </h3>
                                        <p class="hbc-meta">
                                            <i class="fa-solid fa-layer-group"></i> <c:out value="${not empty book.category ? book.category : 'Sách'}" />
                                            <c:if test="${book.publishYear > 0}"> · ${book.publishYear}</c:if>
                                        </p>
                                        <div class="hbc-footer">
                                            <span class="hbc-status ${book.available > 0 ? 'status-available' : 'status-unavailable'}">
                                                <i class="fa-solid ${book.available > 0 ? 'fa-circle-check' : 'fa-circle-xmark'}"></i>
                                                ${book.available > 0 ? 'Sẵn sàng' : 'Hết sách'} (${book.available}/${book.quantity})
                                            </span>
                                            <a href="${pageContext.request.contextPath}/book/detail?id=${book.id}" class="hbc-btn">
                                                Chi tiết <i class="fa-solid fa-chevron-right"></i>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                    <button type="button" class="carousel-btn next-btn" aria-label="Next">
                        <i class="fa-solid fa-chevron-right"></i>
                    </button>
                </div>
            </div>
        </section>
    </c:if>

    <%-- ===== LATEST BOOKS (Danh sách sách mới nhất) ===== --%>
    <c:if test="${not empty latestBooks}">
        <section class="home-latest-books">
            <div class="container">
                <div class="section-header-flex">
                    <div>
                        <span class="section-eyebrow"><i class="fa-solid fa-sparkles"></i> Mới cập nhật</span>
                        <h2 class="section-title" style="margin-bottom:0;">Danh sách sách mới nhất</h2>
                    </div>
                    <a href="${pageContext.request.contextPath}/books" class="btn-view-all">
                        Xem tất cả sách <i class="fa-solid fa-arrow-right"></i>
                    </a>
                </div>
                <div class="book-carousel-container" data-carousel-id="latest">
                    <button type="button" class="carousel-btn prev-btn" aria-label="Previous">
                        <i class="fa-solid fa-chevron-left"></i>
                    </button>
                    <div class="book-carousel-viewport">
                        <div class="latest-books-grid book-carousel-track">
                            <c:forEach var="book" items="${latestBooks}">
                                <%-- Xử lý đường dẫn ảnh bìa chuẩn --%>
                                <c:set var="coverSrc" value="${book.coverImage}" />
                                <c:if test="${not empty coverSrc && !fn:startsWith(coverSrc, 'http://') && !fn:startsWith(coverSrc, 'https://')}">
                                    <c:choose>
                                        <c:when test="${fn:startsWith(coverSrc, '/')}">
                                            <c:set var="coverSrc" value="${pageContext.request.contextPath}${coverSrc}" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="coverSrc" value="${pageContext.request.contextPath}/${coverSrc}" />
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>

                                <div class="home-book-card">
                                    <div class="hbc-cover-wrap">
                                        <c:choose>
                                            <c:when test="${not empty book.coverImage}">
                                                <img src="${coverSrc}"
                                                     alt=""
                                                     class="hbc-cover"
                                                     onerror="this.style.display='none';this.nextElementSibling.style.display='flex';" />
                                                <div class="hbc-cover-placeholder" style="display:none;">
                                                    <i class="fa-solid fa-book-open"></i>
                                                    <span><c:out value="${book.title}" /></span>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="hbc-cover-placeholder">
                                                    <i class="fa-solid fa-book-open"></i>
                                                    <span><c:out value="${book.title}" /></span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                        <span class="hbc-badge"><c:out value="${not empty book.category ? book.category : 'Sách'}" /></span>
                                    </div>
                                    <div class="hbc-body">
                                        <h3 class="hbc-title">
                                            <a href="${pageContext.request.contextPath}/book/detail?id=${book.id}">
                                                <c:out value="${book.title}" />
                                            </a>
                                        </h3>
                                        <p class="hbc-meta">
                                            <i class="fa-solid fa-building"></i> <c:out value="${not empty book.publisher ? book.publisher : 'NXB FPT'}" />
                                            <c:if test="${book.publishYear > 0}"> · ${book.publishYear}</c:if>
                                        </p>
                                        <div class="hbc-footer">
                                            <span class="hbc-status ${book.available > 0 ? 'status-available' : 'status-unavailable'}">
                                                <i class="fa-solid ${book.available > 0 ? 'fa-circle-check' : 'fa-circle-xmark'}"></i>
                                                ${book.available > 0 ? 'Sẵn sàng' : 'Hết sách'} (${book.available}/${book.quantity})
                                            </span>
                                            <a href="${pageContext.request.contextPath}/book/detail?id=${book.id}" class="hbc-btn">
                                                Chi tiết <i class="fa-solid fa-chevron-right"></i>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                    <button type="button" class="carousel-btn next-btn" aria-label="Next">
                        <i class="fa-solid fa-chevron-right"></i>
                    </button>
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
