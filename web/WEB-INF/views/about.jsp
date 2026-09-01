<%-- Trang thông tin giới thiệu về Hệ thống Thư viện FPT University. Controller render: AboutServlet (GET /about) —
    không yêu cầu đăng nhập. --%>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

            <c:set var="activePage" value="about" scope="request" />
            <c:set var="pageTitle" value="Giới thiệu – FPT University Library" scope="request" />
            <c:set var="pageStylesheet" value="/assets/css/about.css" scope="request" />
            <%@ include file="/WEB-INF/views/fragments/header.jsp" %>

                <main class="page-wrapper">

                    <!-- ==================== ABOUT HERO ==================== -->
                    <section class="about-hero">
                        <div class="hero-grid-bg"></div>
                        <div class="container">
                            <div class="about-hero-container">
                                <div class="hero-eyebrow">
                                    <i class="fa-solid fa-circle-info"></i>
                                    Về chúng tôi
                                </div>
                                <h1 class="hero-title">
                                    Thư viện<br>
                                    <span class="brand-gradient">FPT University</span>
                                </h1>
                                <p class="hero-sub">
                                    Là trung tâm tri thức của cộng đồng FPT, chúng tôi cung cấp nguồn tài liệu
                                    học thuật phong phú và môi trường học tập hiện đại.
                                </p>
                            </div>
                        </div>
                    </section>

                    <!-- ==================== MISSION & VISION ==================== -->
                    <section class="about-section">
                        <div class="container">
                            <div class="mission-grid">
                                <div>
                                    <h2 class="section-title mission-title">Sứ mệnh &amp; Tầm nhìn</h2>
                                    <p class="mission-text">
                                        <strong class="mission-highlight">Sứ mệnh:</strong>
                                        Cung cấp môi trường học tập, nghiên cứu chất lượng cao thông qua hệ thống
                                        tài liệu phong phú, dịch vụ chuyên nghiệp và công nghệ hiện đại, góp phần
                                        thúc đẩy sự phát triển tri thức của sinh viên và giảng viên FPT University.
                                    </p>
                                    <p class="mission-text-last">
                                        <strong class="mission-highlight">Tầm nhìn:</strong>
                                        Trở thành thư viện đại học hàng đầu Việt Nam, nơi mọi thành viên FPT
                                        đều có thể tiếp cận tri thức toàn cầu một cách dễ dàng và hiệu quả.
                                    </p>
                                </div>
                                <div class="info-grid stats-grid">
                                    <div class="info-card stat-card-item">
                                        <div class="stat-number-primary">300+</div>
                                        <div class="stat-label-item">Đầu sách học thuật</div>
                                    </div>
                                    <div class="info-card stat-card-item">
                                        <div class="stat-number-accent">10+</div>
                                        <div class="stat-label-item">Danh mục chuyên ngành</div>
                                    </div>
                                    <div class="info-card stat-card-item">
                                        <div class="stat-number-success">50+</div>
                                        <div class="stat-label-item">Tác giả trong &amp; ngoài nước</div>
                                    </div>
                                    <div class="info-card stat-card-item">
                                        <div class="stat-number-info">24/7</div>
                                        <div class="stat-label-item">Tra cứu trực tuyến</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>

                    <!-- ==================== CORE VALUES ==================== -->
                    <section class="about-section">
                        <div class="container">
                            <div class="values-header">
                                <h2 class="section-title">Giá trị cốt lõi</h2>
                                <p class="section-subtitle">Những nguyên tắc định hướng mọi hoạt động của chúng tôi</p>
                            </div>
                            <div class="info-grid">
                                <div class="info-card">
                                    <div class="info-card-icon">🎯</div>
                                    <h3>Chính xác &amp; Tin cậy</h3>
                                    <p>Mọi thông tin về sách, trạng thái và vị trí đều được cập nhật chính xác, kịp
                                        thời.</p>
                                </div>
                                <div class="info-card">
                                    <div class="info-card-icon">🤝</div>
                                    <h3>Phục vụ tận tâm</h3>
                                    <p>Đội ngũ thủ thư nhiệt tình, sẵn sàng hỗ trợ sinh viên tìm kiếm tài liệu học tập.
                                    </p>
                                </div>
                                <div class="info-card">
                                    <div class="info-card-icon">💡</div>
                                    <h3>Đổi mới liên tục</h3>
                                    <p>Ứng dụng công nghệ số để nâng cao trải nghiệm người dùng và hiệu quả quản lý.</p>
                                </div>
                                <div class="info-card">
                                    <div class="info-card-icon">🌱</div>
                                    <h3>Phát triển bền vững</h3>
                                    <p>Không ngừng bổ sung tài liệu mới, đáp ứng nhu cầu học tập và nghiên cứu ngày càng
                                        cao.</p>
                                </div>
                            </div>
                        </div>
                    </section>

                    <!-- ==================== HOURS & CONTACT ==================== -->
                    <section class="about-section hours-contact-section">
                        <div class="container">
                            <div class="hours-contact-grid">
                                <!-- Hours -->
                                <div>
                                    <h2 class="section-title" style="margin-bottom: 28px;">Giờ hoạt động</h2>
                                    <div class="hours-list">
                                        <div class="hours-item weekday">
                                            <span class="hours-item-name">
                                                <i class="fa-regular fa-calendar"
                                                    style="color: var(--success); margin-right: 8px;"></i>
                                                Thứ 2 – Thứ 6
                                            </span>
                                            <span class="badge badge-success">7:30 – 20:00</span>
                                        </div>
                                        <div class="hours-item saturday">
                                            <span class="hours-item-name">
                                                <i class="fa-regular fa-calendar"
                                                    style="color: var(--info); margin-right: 8px;"></i>
                                                Thứ 7
                                            </span>
                                            <span class="badge badge-info">7:30 – 17:30</span>
                                        </div>
                                        <div class="hours-item sunday">
                                            <span class="hours-item-name">
                                                <i class="fa-regular fa-calendar-xmark"
                                                    style="color: var(--danger); margin-right: 8px;"></i>
                                                Chủ nhật &amp; Lễ
                                            </span>
                                            <span class="badge badge-danger">Đóng cửa</span>
                                        </div>
                                        <div class="hours-notice">
                                            <i class="fa-solid fa-circle-info fa-xs"></i>
                                            <strong>Tra cứu trực tuyến</strong> hoạt động 24/7 qua hệ thống này.
                                        </div>
                                    </div>
                                </div>

                                <!-- Contact -->
                                <div>
                                    <h2 class="section-title" style="margin-bottom: 28px;">Thông tin liên hệ</h2>
                                    <div class="contact-list">
                                        <div class="contact-item">
                                            <div class="ci-icon"><i class="fa-solid fa-location-dot"></i></div>
                                            <div>
                                                <div class="ci-label">Địa chỉ</div>
                                                <div class="ci-value">Khu Giáo dục và Đào tạo - Khu Công nghệ cao Hòa
                                                    Lạc, Km29 Đại lộ Thăng Long, xã Hòa Lạc, TP. Hà Nội</div>
                                            </div>
                                        </div>
                                        <div class="contact-item">
                                            <div class="ci-icon"><i class="fa-solid fa-phone"></i></div>
                                            <div>
                                                <div class="ci-label">Điện thoại</div>
                                                <div class="ci-value">0247.3005.588</div>
                                            </div>
                                        </div>
                                        <div class="contact-item">
                                            <div class="ci-icon"><i class="fa-solid fa-envelope"></i></div>
                                            <div>
                                                <div class="ci-label">Email</div>
                                                <div class="ci-value">quaswp391@gmail.com</div>
                                            </div>
                                        </div>
                                        <div class="contact-item">
                                            <div class="ci-icon"><i class="fa-solid fa-building"></i></div>
                                            <div>
                                                <div class="ci-label">Cơ sở</div>
                                                <div class="ci-value">Tòa nhà Delta – Tầng 1, FPT University Hà Nội
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>

                    <!-- ==================== MAP ==================== -->
                    <section style="padding: 0;">
                        <div class="map-section-wrapper">
                            <iframe
                                src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3724.4859333596767!2d105.52487567503102!3d21.01323398063189!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31345b465a4e65fb%3A0xa682f77ff2e3f53a!2zVHLGsOG7nW5nIMSQ4bqhaSBo4buNYyBGUFQgSMOgIE7hu5lp!5e0!3m2!1svi!2s!4v1700000000000"
                                width="100%" height="340" style="border:0; opacity:0.9;" allowfullscreen=""
                                loading="lazy" referrerpolicy="no-referrer-when-downgrade"
                                title="Bản đồ FPT University Hà Nội">
                            </iframe>
                            <div class="map-badge">
                                <i class="fa-solid fa-location-dot"></i>
                                FPT University Hà Nội
                            </div>
                        </div>
                    </section>

                </main>

                <%@ include file="/WEB-INF/views/fragments/footer.jsp" %>