<%-- Fragment hiển thị chân trang dùng chung cho các trang do controller chuyển tiếp đến JSP. Mong đợi request attribute
    `isManagePageAttr` để chọn giao diện quản trị và session attribute `loggedUser` để hiển thị liên kết đăng nhập hoặc
    đăng xuất. --%>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
            <c:url var="homeUrl" value="/home" />
            <c:url var="booksUrl" value="/books" />
            <c:url var="aboutUrl" value="/about" />
            <c:url var="policiesUrl" value="/policies" />
            <c:url var="loginUrl" value="/login" />
            <c:url var="logoutUrl" value="/logout" />
            <c:url var="footerLogoUrl" value="/assets/images/logo.png" />

            <c:choose>
                <c:when test="${isManagePageAttr}">
                    <div class="admin-footer">
                        <span>© 2026 <strong>FPT Library Admin Panel</strong> · SWP391</span>
                        <span>Hệ thống Quản lý Thư viện FPT</span>
                    </div>
                    </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <footer class="footer">
                        <div class="container">
                            <div class="footer-grid">
                                <div class="footer-brand">
                                    <div class="brand-name">
                                        <img class="footer-logo" src="${footerLogoUrl}" alt="FPT University">
                                        <span>FPT Library</span>
                                    </div>
                                    <p>Hệ thống quản lý thư viện hiện đại, phục vụ sinh viên và giảng viên
                                        FPT University với kho tài liệu học thuật phong phú.</p>
                                </div>

                                <div>
                                    <div class="footer-heading">Liên kết nhanh</div>
                                    <ul class="footer-links">
                                        <li><a href="${homeUrl}"><i class="fa-solid fa-house fa-xs"></i> Trang chủ</a>
                                        </li>
                                        <li><a href="${booksUrl}"><i class="fa-solid fa-book fa-xs"></i> Danh sách
                                                sách</a></li>
                                        <li><a href="${aboutUrl}"><i class="fa-solid fa-circle-info fa-xs"></i> Giới
                                                thiệu</a></li>
                                        <li><a href="${policiesUrl}"><i class="fa-solid fa-scale-balanced fa-xs"></i>
                                                Điều lệ</a></li>
                                        <c:choose>
                                            <c:when test="${empty sessionScope.loggedUser}">
                                                <li><a href="${loginUrl}"><i
                                                            class="fa-solid fa-right-to-bracket fa-xs"></i> Đăng
                                                        nhập</a></li>
                                            </c:when>
                                            <c:otherwise>
                                                <li><a href="${logoutUrl}"><i
                                                            class="fa-solid fa-right-from-bracket fa-xs"></i> Đăng
                                                        xuất</a></li>
                                            </c:otherwise>
                                        </c:choose>
                                    </ul>
                                </div>

                                <div>
                                    <div class="footer-heading">Liên hệ</div>
                                    <div class="footer-contact-item">
                                        <span class="icon"><i class="fa-solid fa-location-dot"></i></span>
                                        <span>Khu CNC Hòa Lạc, Thạch Thất, Hà Nội</span>
                                    </div>
                                    <div class="footer-contact-item">
                                        <span class="icon"><i class="fa-solid fa-phone"></i></span>
                                        <span>0247.3005.588</span>
                                    </div>
                                    <div class="footer-contact-item">
                                        <span class="icon"><i class="fa-solid fa-envelope"></i></span>
                                        <span>quaswp391@gmail.com</span>
                                    </div>
                                    <div class="footer-contact-item">
                                        <span class="icon"><i class="fa-solid fa-clock"></i></span>
                                        <span>Thứ 2 – Thứ 7: 7:30 – 20:00</span>
                                    </div>
                                </div>
                            </div>

                            <div class="footer-bottom">
                                <span>© 2026 <span class="fpt-tag">FPT Library System</span> · SWP391</span>
                                <span>Thiết kế bởi <span class="fpt-tag">Team SWP391</span></span>
                            </div>
                        </div>
                    </footer>
                </c:otherwise>
            </c:choose>

            </body>

            </html>