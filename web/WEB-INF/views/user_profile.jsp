<%-- Mục đích: Trang xem và chỉnh sửa thông tin hồ sơ cá nhân người dùng. Tầng: Presentation Layer (JSP View -
    WEB-INF/views/user_profile.jsp) Phụ trách bởi: UserProfileServlet (/user/profile) Thuộc tính nhận:
    request.profileUser, session.loggedUser, request.error, request.success --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User" %>
<% String contextPath=request.getContextPath(); User profile=(User) request.getAttribute("profileUser");
    User logged=(User) session.getAttribute("loggedUser"); boolean isAdmin=logged !=null && "admin"
    .equalsIgnoreCase(logged.getRole()); boolean isAdminEditingOther=isAdmin && profile !=null &&
    logged.getId() !=profile.getId(); %>
<jsp:include page="/WEB-INF/views/fragments/header.jsp" />

<link rel="stylesheet" href="<%= contextPath %>/assets/css/user_profile.css">

<main class="page-wrapper">
    <section class="profile-section">
        <div class="container">
            <div class="profile-header">
                <h1>Hồ sơ cá nhân</h1>
                <p>Quản lý thông tin tài khoản và bảo mật</p>
            </div>

            <%-- Hiển thị thông báo Lỗi hoặc Thành công phản hồi từ Controller --%>
            <% if (request.getAttribute("error") !=null) { %>
            <div class="alert alert-danger">
                <%= request.getAttribute("error") %>
            </div>
            <% } %>
            <% if (request.getAttribute("success") !=null) { %>
            <div class="alert alert-success">
                <%= request.getAttribute("success") %>
            </div>
            <% } %>

            <% if (profile==null) { %>
            <div class="alert alert-warning">Không tìm thấy thông tin người dùng.
            </div>
            <% } else { %>
            <div class="profile-grid">

                <%-- Khối Form 1: Cập nhật thông tin cá nhân (phương thức POST gửi tới /user/profile với action=updateProfile) --%>
                <div class="profile-card">
                    <form method="POST"
                          action="<%= contextPath %>/user/profile">
                        <input type="hidden" name="action"
                               value="updateProfile" />
                        <input type="hidden" name="id"
                               value="<%= profile.getId() %>" />

                        <h3>
                            <%= profile.getFullName() !=null ?
                                                                        profile.getFullName() : profile.getUsername() %>
                        </h3>
                        <p class="user-role-text">
                            <%= profile.getRole() %>
                        </p>
                        <hr class="divider" />

                        <div class="form-group">
                            <label>Tên đăng nhập</label>
                            <input type="text" class="form-control"
                                   value="<%= profile.getUsername() %>" readonly
                                   disabled />
                        </div>

                        <!-- fullName: bắt buộc, tối thiểu 2 - tối đa 100 ký tự -->
                        <div class="form-group">
                            <label for="fullName">Họ và tên *</label>
                            <input type="text" id="fullName" name="fullName"
                                   class="form-control"
                                   value="<%= profile.getFullName() != null ? profile.getFullName() : "" %>"
                                   required minlength="3" maxlength="100"
                                   placeholder="Nhập họ và tên..." />
                        </div>

                        <!-- email: bắt buộc, maxlength đồng bộ với DB (100) -->
                        <div class="form-group">
                            <label for="email">Email *</label>
                            <input type="email" id="email" name="email"
                                   class="form-control"
                                   value="<%= profile.getEmail() != null ? profile.getEmail() : "" %>"
                                   required maxlength="100"
                                   placeholder="example@email.com" />
                        </div>

                        <!-- phone: KHÔNG bắt buộc  (Service xử lý format) -->
                        <div class="form-group">
                            <label for="phone">Số điện thoại</label>
                            <input type="text" id="phone" name="phone"
                                   class="form-control"
                                   value="<%= profile.getPhone() != null ? profile.getPhone() : "" %>"
                                   maxlength="15" placeholder="0xxxxxxxxx" />
                        </div>

                        <!-- studentId: KHÔNG bắt buộc, chỉ hiện khi role hiện tại = READER -->
                        <% if ("READER".equalsIgnoreCase(profile.getRole())) {
                        %>
                        <div class="form-group">
                            <label for="studentId">Mã số sinh viên
                                (MSSV)</label>
                            <input type="text" id="studentId"
                                   name="studentId" class="form-control"
                                   value="<%= profile.getStudentId() != null ? profile.getStudentId() : "" %>"
                                   maxlength="20"
                                   placeholder="Ví dụ: SS170001" />
                        </div>
                        <% } %>

                        <% if (isAdmin) { %>
                        <div class="admin-controls-section">
                            <h4>Quản trị viên điều khiển</h4>
                            <div class="form-group">
                                <label for="role">Vai trò</label>
                                <select id="role" name="role"
                                        class="form-select">
                                    <option value="ADMIN" <%="ADMIN"
                                        .equalsIgnoreCase(profile.getRole())
                                        ? "selected" : "" %>>ADMIN
                                    </option>
                                    <option value="LIBRARIAN"
                                            <%="LIBRARIAN"
                                            .equalsIgnoreCase(profile.getRole())
                                            ? "selected" : "" %>
                                            >LIBRARIAN</option>
                                    <option value="READER"
                                            <%="READER"
                                            .equalsIgnoreCase(profile.getRole())
                                            ? "selected" : "" %>>READER
                                    </option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="active">Trạng thái tài
                                    khoản</label>
                                <select id="active" name="active"
                                        class="form-select">
                                    <option value="1"
                                            <%=profile.getActive()==1
                                                                                            ? "selected" : "" %>>Hoạt
                                        động</option>
                                    <option value="0"
                                            <%=profile.getActive()==0
                                                                                            ? "selected" : "" %>>Khóa
                                    </option>
                                </select>
                            </div>
                        </div>
                        <% } %>

                        <div class="form-footer">
                            <button type="submit"
                                    class="btn btn-primary">Lưu
                                thông tin</button>
                        </div>
                    </form>
                </div>

                <!-- Đổi mật khẩu -->
                <div class="profile-card">
                    <h2>Đổi mật khẩu</h2>
                    <p>
                        <%= isAdminEditingOther
                            ? "Đặt mật khẩu mới cho người dùng này"
                            : "Quản lý và cập nhật mật khẩu đăng nhập" %>
                    </p>

                    <% if (isAdminEditingOther) { %>
                    <div class="info-bubble">
                        Bạn đang đặt mật khẩu mới cho người dùng khác với
                        quyền quản trị viên, không cần nhập mật khẩu hiện
                        tại của họ.
                    </div>
                    <% } %>

                    <form method="POST"
                          action="<%= contextPath %>/user/profile"
                          autocomplete="off">
                        <input type="hidden" name="action"
                               value="changePassword" />
                        <input type="hidden" name="id"
                               value="<%= profile.getId() %>" />

                        <!-- oldPassword: bắt buộc, chỉ render khi tự đổi mật khẩu của chính mình -->
                        <% if (!isAdminEditingOther) { %>
                        <div class="form-group">
                            <label for="oldPassword">Mật khẩu hiện
                                tại *</label>
                            <input type="password" id="oldPassword"
                                   name="oldPassword"
                                   class="form-control"
                                   autocomplete="new-password"
                                   placeholder="Nhập mật khẩu hiện tại"
                                   required />
                        </div>
                        <% } %>

                        <!-- newPassword: bắt buộc, tối thiểu 5 ký tự -->
                        <div class="form-group">
                            <label for="newPassword">Mật khẩu
                                mới *</label>
                            <input type="password"
                                   id="newPassword"
                                   name="newPassword"
                                   class="form-control"
                                   autocomplete="new-password"
                                   required minlength="5"
                                   placeholder="Tối thiểu 5 ký tự" />
                        </div>

                        <div class="form-group">
                            <label for="confirmPassword">Xác
                                nhận mật khẩu mới *</label>
                            <input type="password"
                                   id="confirmPassword"
                                   name="confirmPassword"
                                   class="form-control"
                                   autocomplete="new-password"
                                   placeholder="Nhập lại mật khẩu mới"
                                   required />
                        </div>

                        <div class="form-footer">
                            <button type="submit"
                                    class="btn btn-outline btn-block">
                                <%= isAdminEditingOther
                                    ? "Đặt mật khẩu mới"
                                    : "Cập nhật mật khẩu" %>
                            </button>
                        </div>
                    </form>
                </div>
            </div>
            <% } %>
        </div>
    </section>
</main>

<jsp:include page="/WEB-INF/views/fragments/footer.jsp" />