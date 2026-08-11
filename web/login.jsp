<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
%>


<main class="page-wrapper">
    <section class="login-section">
        <div class="container">
            <div class="login-container">
                <!-- Login Form -->
                <div class="login-card">
                    <div class="login-header">
                        <h1>Đăng nhập</h1>
                        <p>Vào tài khoản của bạn để truy cập thư viện</p>
                    </div>

                    <% if (request.getAttribute("error") != null) { %>
                        <div class="alert alert-danger">
                            <i class="fa-solid fa-circle-exclamation"></i>
                            <%= request.getAttribute("error") %>
                        </div>
                    <% } %>
                    <% if (request.getAttribute("success") != null) { %>
                        <div class="alert alert-success">
                            <i class="fa-solid fa-circle-check"></i>
                            <%= request.getAttribute("success") %>
                        </div>
                    <% } %>

                    <form method="POST" action="<%= contextPath %>/login" class="login-form">
                        <div class="form-group">
                            <label for="username">Tên đăng nhập</label>
                            <input type="text" id="username" name="username"
                                   value="${param.username}"
                                   required placeholder="Nhập tên đăng nhập"
                                   class="form-control">
                        </div>

                        <div class="form-group">
                            <label for="password">Mật khẩu</label>
                            <input type="password" id="password" name="password"
                                   required placeholder="Nhập mật khẩu"
                                   class="form-control">
                        </div>

                        <button type="submit" class="btn btn-primary btn-block">
                            <i class="fa-solid fa-right-to-bracket"></i> Đăng nhập
                        </button>
                    </form>
                    
                    <div class="form-actions-sub">
                        <a href="<%= contextPath %>/forgot-password" class="btn-forgot-password">
                            <i class="fa-solid fa-key"></i> Quên mật khẩu?
                        </a>
                    </div>

                </div>

                <!-- Info Section -->
                <div class="login-info">
                    <div class="info-box">
                        <div class="info-icon" style="background:none;width:auto;height:auto;margin-bottom:1.5rem;display:flex;justify-content:center;">
                            <img src="<%= contextPath %>/images/logo.png" alt="FPT University" style="height:60px;width:auto;object-fit:contain;">
                        </div>
                        <h3>Thư viện FPT University</h3>
                        <p>Đăng nhập để:</p>
                        <ul>
                            <li><i class="fa-solid fa-check"></i> Tìm kiếm sách và tài liệu</li>
                            <li><i class="fa-solid fa-check"></i> Xem và quản lý hồ sơ cá nhân</li>
                            <li><i class="fa-solid fa-check"></i> Đặt mượn sách trực tuyến</li>
                            <li><i class="fa-solid fa-check"></i> Theo dõi quá trình mượn trả</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </section>
</main>



<style>
.page-wrapper {
    padding-bottom: 0;
}

.login-section {
    padding: 60px 0;
    background: linear-gradient(135deg, #f5f7fa 0%, #f0f4f8 100%);
    min-height: calc(100vh - 200px);
    display: flex;
    align-items: center;
    justify-content: center;
}

.login-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 48px;
    align-items: center;
}

.login-card {
    background: white;
    border-radius: 12px;
    padding: 48px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
}

.form-actions-sub {
    text-align: center;
    margin-top: 20px;
    margin-bottom: 24px;
}

.btn-forgot-password {
    font-size: 14px;
    color: var(--primary, #f47920);
    text-decoration: none;
    transition: all 0.2s;
    font-weight: 600;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.btn-forgot-password:hover {
    color: var(--primary-dark, #d4600a);
    transform: translateY(-2px);
    text-decoration: underline;
}

.login-header {
    margin-bottom: 32px;
    text-align: center;
}

.login-header h1 {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary, #1f2937);
    margin-bottom: 8px;
}

.login-header p {
    font-size: 14px;
    color: var(--text-muted, #6b7280);
}

.login-form {
    margin-bottom: 24px;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 500;
    color: var(--text-primary, #1f2937);
    font-size: 14px;
}

.form-control {
    width: 100%;
    padding: 12px 16px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 14px;
    font-family: inherit;
    transition: all 0.2s;
    box-sizing: border-box;
}

.form-control:focus {
    outline: none;
    border-color: var(--primary, #3b82f6);
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-block {
    width: 100%;
    padding: 12px;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    background: var(--primary, #3b82f6);
    color: white;
}

.btn-block:hover {
    background: var(--primary-dark, #2563eb);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.login-hint {
    padding: 16px;
    background: #fef3c7;
    border: 1px solid #fcd34d;
    border-radius: 8px;
    font-size: 13px;
    color: #92400e;
    text-align: center;
}

.login-hint p {
    margin: 0;
}

.login-info {
    display: flex;
    align-items: center;
    justify-content: center;
}

.info-box {
    text-align: center;
}

.info-icon {
    font-size: 64px;
    margin-bottom: 24px;
}

.info-box h3 {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary, #1f2937);
    margin-bottom: 16px;
}

.info-box p {
    font-size: 14px;
    color: var(--text-muted, #6b7280);
    margin-bottom: 16px;
}

.info-box ul {
    list-style: none;
    padding: 0;
    text-align: left;
    display: inline-block;
}

.info-box li {
    font-size: 14px;
    color: var(--text-secondary, #374151);
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    gap: 10px;
}

.info-box i {
    color: var(--success, #10b981);
    font-weight: bold;
}

.alert {
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 14px;
}

.alert-danger {
    background: #fee2e2;
    border: 1px solid #fecaca;
    color: #991b1b;
}

.alert i {
    font-size: 16px;
}

@media (max-width: 768px) {
    .login-container {
        grid-template-columns: 1fr;
        gap: 32px;
    }

    .login-card {
        padding: 32px;
    }

    .login-header h1 {
        font-size: 24px;
    }

    .info-box {
        display: none;
    }
}
</style>


