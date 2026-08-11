<%--
  Tên file: resetPassword.jsp
  Mục đích: Trang đặt lại mật khẩu mới cho người dùng.
  Tầng: Giao diện (Presentation Layer - JSP View)
  Trách nhiệm: Hiển thị form cho phép người dùng nhập Email và Mật khẩu mới để cập nhật tài khoản, đồng bộ với login.jsp.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">

<main class="page-wrapper">
    <section class="login-section">
        <div class="container">
            <div class="login-container">
                <!-- Reset Password Form -->
                <div class="login-card">
                    <div class="login-header">
                        <h1>Đặt lại mật khẩu</h1>
                        <p>Nhập địa chỉ email và mật khẩu mới của bạn</p>
                    </div>

                    <c:if test="${not empty mess}">
                        <div class="alert alert-danger">
                            <i class="fa-solid fa-circle-exclamation"></i>
                            <c:out value="${mess}" />
                        </div>
                    </c:if>

                    <form method="POST" action="${pageContext.request.contextPath}/resetPassword" class="login-form">
                        <div class="form-group">
                            <label for="email">Địa chỉ Email</label>
                            <input type="email" id="email" name="email" value="${email}"
                                   required placeholder="Nhập địa chỉ email" class="form-control">
                        </div>

                        <div class="form-group">
                            <label for="password">Mật khẩu mới</label>
                            <input type="password" id="password" name="password"
                                   required placeholder="Nhập mật khẩu mới" class="form-control">
                        </div>

                        <div class="form-group">
                            <label for="confirm_password">Xác nhận mật khẩu mới</label>
                            <input type="password" id="confirm_password" name="confirm_password"
                                   required placeholder="Nhập lại mật khẩu mới" class="form-control">
                        </div>

                        <button type="submit" class="btn btn-primary btn-block">
                            <i class="fa-solid fa-key"></i> Đặt lại mật khẩu
                        </button>
                    </form>

                    <div class="form-actions-sub">
                        <a href="${pageContext.request.contextPath}/login" class="btn-forgot-password">
                            <i class="fa-solid fa-arrow-left"></i> Quay lại Đăng nhập
                        </a>
                    </div>
                </div>

                <!-- Info Section -->
                <div class="login-info">
                    <div class="info-box">
                        <div class="info-icon info-logo-wrap">
                            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="FPT University" class="info-logo-img">
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