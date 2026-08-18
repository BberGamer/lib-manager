<%--
  Mục đích: Trang yêu cầu gửi liên kết đặt lại mật khẩu qua Email.
  Tầng: Presentation Layer (JSP View - WEB-INF/views/requestPassword.jsp)
  Phụ trách bởi: RequestPasswordServlet (/requestPassword)
  Thuộc tính nhận: request.mess
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">

<main class="page-wrapper">
    <section class="login-section">
        <div class="container">
            <div class="login-container">
                <!-- Request Password Form -->
                <div class="login-card">
                    <div class="login-header">
                        <h1>Quên mật khẩu</h1>
                        <p>Nhập email của bạn để nhận liên kết khôi phục mật khẩu</p>
                    </div>

                    <%-- Khối 1: Hiển thị thông báo phản hồi từ RequestPasswordServlet --%>
                    <c:if test="${not empty mess}">
                        <c:choose>
                            <c:when test="${fn:contains(mess, 'success')}">
                                <div class="alert alert-success">
                                    <i class="fa-solid fa-circle-check"></i>
                                    Gửi yêu cầu thành công! Vui lòng kiểm tra email của bạn.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="alert alert-danger">
                                    <i class="fa-solid fa-circle-exclamation"></i>
                                    <c:out value="${mess}" />
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:if>

                    <%-- Form gửi Email yêu cầu nhận Token khôi phục tới /requestPassword --%>
                    <form method="POST" action="${pageContext.request.contextPath}/requestPassword" class="login-form">
                        <%-- Ô nhập Email với kiểm tra loại email chuẩn HTML5 --%>
                        <div class="form-group">
                            <label for="email">Địa chỉ Email</label>
                            <input type="email" id="email" name="email" value="${param.email}"
                                   required placeholder="Nhập địa chỉ email của bạn" class="form-control">
                        </div>

                        <button type="submit" class="btn btn-primary btn-block">
                            <i class="fa-solid fa-paper-plane"></i> Gửi yêu cầu
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
                        <p>Hệ thống hỗ trợ:</p>
                        <ul>
                            <li><i class="fa-solid fa-check"></i> Tìm kiếm sách và tài liệu</li>
                            <li><i class="fa-solid fa-check"></i> Xem và quản lý hồ sơ cá nhân</li>
                            <li><i class="fa-solid fa-check"></i> Khôi phục mật khẩu nhanh chóng</li>
                            <li><i class="fa-solid fa-check"></i> Bảo vệ tài khoản người dùng</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </section>
</main>
                           <%@ include file="/WEB-INF/views/fragments/footer.jsp" %>