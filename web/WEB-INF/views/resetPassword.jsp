<%--
  Mục đích: Trang đặt lại mật khẩu mới cho người dùng.
  Tầng: Presentation Layer (JSP View - WEB-INF/views/resetPassword.jsp)
  Phụ trách bởi: ResetPasswordServlet (/resetPassword)
  Thuộc tính nhận: request.mess, request.email
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

                    <%-- Khối 1: Hiển thị thông báo phản hồi từ ResetPasswordServlet --%>
                    <c:if test="${not empty mess}">
                        <div class="alert alert-danger">
                            <i class="fa-solid fa-circle-exclamation"></i>
                            <c:out value="${mess}" />
                        </div>
                    </c:if>

                    <%-- Form gửi mật khẩu mới và mật khẩu xác nhận tới /resetPassword --%>
                    <form method="POST" action="${pageContext.request.contextPath}/resetPassword" class="login-form">
                        <div class="form-group">
                            <label for="email">Địa chỉ Email</label>
                            <input type="email" id="email" name="email" value="${email}"
                                   required placeholder="Nhập địa chỉ email" class="form-control">
                        </div>

                        <%-- Ô nhập Mật khẩu mới --%>
                        <div class="form-group">
                            <label for="password">Mật khẩu mới</label>
                            <input type="password" id="password" name="password"
                                   required placeholder="Nhập mật khẩu mới" class="form-control">
                        </div>

                        <%-- Ô nhập Xác nhận mật khẩu mới (Server sẽ kiểm tra trùng khớp với password) --%>
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