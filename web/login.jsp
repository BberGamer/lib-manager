<%--
  Tên file: login.jsp
  Mục đích: Trang hiển thị giao diện đăng nhập cho người dùng và thủ thư/quản trị viên.
  Tầng: Giao diện (Presentation Layer - JSP View)
  Trách nhiệm: Hiển thị form đăng nhập, xử lý hiển thị thông báo lỗi/thành công từ controller,
              liên kết stylesheet toàn cục style.css và stylesheet riêng login.css.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>


<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">

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

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">
                            <i class="fa-solid fa-circle-exclamation"></i>
                            <c:out value="${error}" />
                        </div>
                    </c:if>
                    <c:if test="${not empty success}">
                        <div class="alert alert-success">
                            <i class="fa-solid fa-circle-check"></i>
                            <c:out value="${success}" />
                        </div>
                    </c:if>

                    <form method="POST" action="${pageContext.request.contextPath}/login" class="login-form">
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
                        <a href="${pageContext.request.contextPath}/forgot-password" class="btn-forgot-password">
                            <i class="fa-solid fa-key"></i> Quên mật khẩu?
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
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>