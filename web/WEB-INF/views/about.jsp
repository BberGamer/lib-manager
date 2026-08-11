<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<main class="page-wrapper">

    <!-- ==================== ABOUT HERO ==================== -->
    <section class="about-hero">
        <div class="hero-grid-bg"></div>
        <div class="container" style="position:relative;z-index:1;">
            <div style="max-width:620px;">
                <div class="hero-eyebrow">
                    <i class="fa-solid fa-circle-info"></i>
                    Về chúng tôi
                </div>
                <h1 style="font-family:'Be Vietnam Pro',sans-serif; font-size:clamp(1.9rem,3.5vw,3rem);
                           font-weight:800; color:var(--text-primary); line-height:1.15;
                           margin-bottom:18px; letter-spacing:-0.5px;">
                    Thư viện<br>
                    <span style="background:linear-gradient(to right,var(--primary),var(--primary-light));
                                 -webkit-background-clip:text;-webkit-text-fill-color:transparent;">
                        FPT University
                    </span>
                </h1>
                <p style="font-size:1rem; color:var(--text-secondary); line-height:1.8; max-width:500px;">
                    Là trung tâm tri thức của cộng đồng FPT, chúng tôi cung cấp nguồn tài liệu
                    học thuật phong phú và môi trường học tập hiện đại.
                </p>
            </div>
        </div>
    </section>

    <!-- ==================== MISSION & VISION ==================== -->
    <section class="about-section">
        <div class="container">
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:48px; align-items:center;">
                <div>
                    <h2 class="section-title" style="margin-bottom:24px;">Sứ mệnh &amp; Tầm nhìn</h2>
                    <p style="color:var(--text-secondary); line-height:1.85; margin-bottom:20px;">
                        <strong style="color:var(--primary);">Sứ mệnh:</strong>
                        Cung cấp môi trường học tập, nghiên cứu chất lượng cao thông qua hệ thống
                        tài liệu phong phú, dịch vụ chuyên nghiệp và công nghệ hiện đại, góp phần
                        thúc đẩy sự phát triển tri thức của sinh viên và giảng viên FPT University.
                    </p>
                    <p style="color:var(--text-secondary); line-height:1.85;">
                        <strong style="color:var(--primary);">Tầm nhìn:</strong>
                        Trở thành thư viện đại học hàng đầu Việt Nam, nơi mọi thành viên FPT
                        đều có thể tiếp cận tri thức toàn cầu một cách dễ dàng và hiệu quả.
                    </p>
                </div>
                <div class="info-grid" style="grid-template-columns:1fr 1fr;">
                    <div class="info-card" style="text-align:center;">
                        <div style="font-size:2.2rem; font-weight:800; color:var(--primary); margin-bottom:6px; font-family:'Be Vietnam Pro',sans-serif;">300+</div>
                        <div style="font-size:0.82rem; color:var(--text-muted); text-transform:uppercase; letter-spacing:0.5px;">Đầu sách học thuật</div>
                    </div>
                    <div class="info-card" style="text-align:center;">
                        <div style="font-size:2.2rem; font-weight:800; color:var(--accent); margin-bottom:6px; font-family:'Be Vietnam Pro',sans-serif;">10+</div>
                        <div style="font-size:0.82rem; color:var(--text-muted); text-transform:uppercase; letter-spacing:0.5px;">Danh mục chuyên ngành</div>
                    </div>
                    <div class="info-card" style="text-align:center;">
                        <div style="font-size:2.2rem; font-weight:800; color:var(--success); margin-bottom:6px; font-family:'Be Vietnam Pro',sans-serif;">50+</div>
                        <div style="font-size:0.82rem; color:var(--text-muted); text-transform:uppercase; letter-spacing:0.5px;">Tác giả trong &amp; ngoài nước</div>
                    </div>
                    <div class="info-card" style="text-align:center;">
                        <div style="font-size:2.2rem; font-weight:800; color:var(--info); margin-bottom:6px; font-family:'Be Vietnam Pro',sans-serif;">24/7</div>
                        <div style="font-size:0.82rem; color:var(--text-muted); text-transform:uppercase; letter-spacing:0.5px;">Tra cứu trực tuyến</div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- ==================== CORE VALUES ==================== -->
    <section class="about-section">
        <div class="container">
            <div style="text-align:center; margin-bottom:44px;">
                <h2 class="section-title" style="display:inline-block;">Giá trị cốt lõi</h2>
                <p class="section-subtitle" style="margin-top:14px;">Những nguyên tắc định hướng mọi hoạt động của chúng tôi</p>
            </div>
            <div class="info-grid">
                <div class="info-card">
                    <div class="info-card-icon">🎯</div>
                    <h3>Chính xác &amp; Tin cậy</h3>
                    <p>Mọi thông tin về sách, trạng thái và vị trí đều được cập nhật chính xác, kịp thời.</p>
                </div>
                <div class="info-card">
                    <div class="info-card-icon">🤝</div>
                    <h3>Phục vụ tận tâm</h3>
                    <p>Đội ngũ thủ thư nhiệt tình, sẵn sàng hỗ trợ sinh viên tìm kiếm tài liệu học tập.</p>
                </div>
                <div class="info-card">
                    <div class="info-card-icon">💡</div>
                    <h3>Đổi mới liên tục</h3>
                    <p>Ứng dụng công nghệ số để nâng cao trải nghiệm người dùng và hiệu quả quản lý.</p>
                </div>
                <div class="info-card">
                    <div class="info-card-icon">🌱</div>
                    <h3>Phát triển bền vững</h3>
                    <p>Không ngừng bổ sung tài liệu mới, đáp ứng nhu cầu học tập và nghiên cứu ngày càng cao.</p>
                </div>
            </div>
        </div>
    </section>

    <!-- ==================== HOURS & CONTACT ==================== -->
    <section class="about-section" style="background:var(--bg-card); border-top:1px solid var(--border); border-bottom:1px solid var(--border);">
        <div class="container">
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:48px;">
                <!-- Hours -->
                <div>
                    <h2 class="section-title" style="margin-bottom:28px;">Giờ hoạt động</h2>
                    <div style="display:flex; flex-direction:column; gap:12px;">
                        <div style="display:flex; justify-content:space-between; align-items:center;
                                    padding:16px 20px; background:var(--bg-surface); border:1px solid var(--border);
                                    border-radius:var(--radius-sm); border-left:3px solid var(--success);">
                            <span style="font-weight:600; color:var(--text-primary);">
                                <i class="fa-regular fa-calendar" style="color:var(--success); margin-right:8px;"></i>
                                Thứ 2 – Thứ 6
                            </span>
                            <span class="badge badge-success">7:30 – 20:00</span>
                        </div>
                        <div style="display:flex; justify-content:space-between; align-items:center;
                                    padding:16px 20px; background:var(--bg-surface); border:1px solid var(--border);
                                    border-radius:var(--radius-sm); border-left:3px solid var(--info);">
                            <span style="font-weight:600; color:var(--text-primary);">
                                <i class="fa-regular fa-calendar" style="color:var(--info); margin-right:8px;"></i>
                                Thứ 7
                            </span>
                            <span class="badge badge-info">7:30 – 17:30</span>
                        </div>
                        <div style="display:flex; justify-content:space-between; align-items:center;
                                    padding:16px 20px; background:var(--bg-surface); border:1px solid var(--border);
                                    border-radius:var(--radius-sm); border-left:3px solid var(--danger);">
                            <span style="font-weight:600; color:var(--text-primary);">
                                <i class="fa-regular fa-calendar-xmark" style="color:var(--danger); margin-right:8px;"></i>
                                Chủ nhật &amp; Lễ
                            </span>
                            <span class="badge badge-danger">Đóng cửa</span>
                        </div>
                        <div style="padding:14px 18px; background:rgba(244,121,32,0.06);
                                    border:1px solid rgba(244,121,32,0.2); border-radius:var(--radius-sm);">
                            <div style="font-size:0.85rem; color:var(--primary);">
                                <i class="fa-solid fa-circle-info fa-xs"></i>
                                <strong>Tra cứu trực tuyến</strong> hoạt động 24/7 qua hệ thống này.
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Contact -->
                <div>
                    <h2 class="section-title" style="margin-bottom:28px;">Thông tin liên hệ</h2>
                    <div style="display:flex; flex-direction:column; gap:12px;">
                        <div class="contact-item">
                            <div class="ci-icon"><i class="fa-solid fa-location-dot"></i></div>
                            <div>
                                <div class="ci-label">Địa chỉ</div>
                                <div class="ci-value">Khu Giáo dục và Đào tạo - Khu Công nghệ cao Hòa Lạc, Km29 Đại lộ Thăng Long, xã Hòa Lạc, TP. Hà Nội</div>
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
                                <div class="ci-value">Tòa nhà Delta – Tầng 1, FPT University Hà Nội</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
     </section>

     <!-- ==================== MAP ==================== -->
     <section style="padding: 0;">
        <div style="height:340px; background:var(--bg-surface); border-top:1px solid var(--border);
                    position:relative; overflow:hidden;">
            <iframe
                src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3724.4859333596767!2d105.52487567503102!3d21.01323398063189!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31345b465a4e65fb%3A0xa682f77ff2e3f53a!2zVHLGsOG7nW5nIMSQ4bqhaSBo4buNYyBGUFQgSMOgIE7hu5lp!5e0!3m2!1svi!2s!4v1700000000000"
                width="100%" height="340"
                style="border:0; opacity:0.9;"
                allowfullscreen="" loading="lazy"
                referrerpolicy="no-referrer-when-downgrade"
                title="Bản đồ FPT University Hà Nội">
            </iframe>
            <div style="position:absolute; bottom:16px; right:16px; background:var(--bg-card);
                        border:1px solid var(--border); border-radius:var(--radius-sm);
                        padding:10px 16px; font-size:0.82rem; color:var(--primary);
                        backdrop-filter:blur(8px);">
                <i class="fa-solid fa-location-dot"></i>
                FPT University Hà Nội
            </div>
        </div>
     </section>

</main>

<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>

<style>
@media (max-width: 768px) {
    .about-section > .container > div[style*="grid-template-columns:1fr 1fr"],
    .about-section > .container > div[style*="grid-template-columns: 1fr 1fr"] {
        grid-template-columns: 1fr !important;
    }
}
</style>
