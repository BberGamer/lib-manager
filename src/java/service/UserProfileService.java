package service;

import dao.UserDAO;
import model.User;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class UserProfileService {

    private final UserDAO userDAO = new UserDAO();

    // Danh sách role hợp lệ (whitelist) - dùng để chặn role lạ bị gửi thẳng qua request
    private static final List<String> VALID_ROLES = Arrays.asList("ADMIN", "LIBRARIAN", "READER");

    public User getProfile(int userId) throws Exception {
        return userDAO.getUserById(userId);
    }

    // isAdmin = true -> cho phép sửa thêm role/active (chỉ admin mới có quyền này)
    public String updateProfile(User user, String fullName, String email, String phone,
            String studentId, String role, Integer active, boolean isAdmin) throws Exception {

        // ===== 1. VALIDATE fullName =====
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Họ và tên không được để trống.";
        }
        fullName = fullName.trim();
        if (fullName.length() > 100) {
            return "Họ và tên không được vượt quá 100 ký tự.";
        }
        if (!fullName.matches("^[\\p{L} ]+$")) {
            return "Họ và tên chỉ được chứa chữ cái và khoảng trắng.";
        }
        if (fullName.length() < 3) {
            return "Họ và tên phải có ít nhất 3 ký tự.";
        }

        // ===== 2. VALIDATE email =====
        if (email == null || email.trim().isEmpty()) {
            return "Email không được để trống.";
        }
        email = email.trim();
        if (email.length() > 100) {
            return "Email không được vượt quá 100 ký tự.";
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-zA-Z]{2,}$")) {
            return "Email không đúng định dạng.";
        }
        // Chỉ check trùng khi email mới khác email cũ (tránh báo lỗi trùng với chính mình)
        if (!email.equalsIgnoreCase(user.getEmail())) {
            User existingByEmail = userDAO.getUserByEmail(email);
            if (existingByEmail != null && existingByEmail.getId() != user.getId()) {
                return "Email đã được sử dụng bởi tài khoản khác.";
            }
        }

        // ===== 3. VALIDATE phone (không bắt buộc) =====
        if (phone != null && !phone.trim().isEmpty()) {
            phone = phone.trim();
            if (!phone.matches("^0[0-9]{9,10}$")) {
                return "Số điện thoại không đúng định dạng (bắt đầu bằng 0, đủ 10-11 số).";
            }
        } else {
            phone = null;
        }

        // ===== 4. VALIDATE role & active (chỉ admin mới được sửa) =====
        String finalRole = user.getRole();
        if (isAdmin) {
            if (role != null) {
                if (!VALID_ROLES.contains(role.toUpperCase())) {
                    return "Vai trò không hợp lệ.";
                }
                finalRole = role.toUpperCase();
            }
            if (active != null) {
                if (active != 0 && active != 1) {
                    return "Trạng thái tài khoản không hợp lệ.";
                }
                user.setActive(active);
            }
        }
        // isAdmin = false -> bỏ qua hoàn toàn role/active, giữ nguyên giá trị hiện tại

        // ===== 5. VALIDATE studentId (không bắt buộc, phụ thuộc finalRole) =====
        if ("READER".equalsIgnoreCase(finalRole)) {
            if (studentId != null && !studentId.trim().isEmpty()) {
                studentId = studentId.trim();
                if (studentId.length() > 20) {
                    return "Mã số sinh viên không được vượt quá 20 ký tự.";
                }
                if (!studentId.matches("^H[A-Z][0-9]{6}$")) {
                    return "Mã số sinh viên chỉ được chứa chữ và số và đúng định dạng .";
                }
                // Chỉ check trùng khi studentId mới khác studentId cũ
                boolean studentIdChanged = user.getStudentId() == null
                        || !studentId.equalsIgnoreCase(user.getStudentId());
                if (studentIdChanged) {
                    User existingByStudentId = userDAO.getUserByStudentId(studentId);
                    if (existingByStudentId != null && existingByStudentId.getId() != user.getId()) {
                        return "Mã số sinh viên đã tồn tại.";
                    }
                }
            } else {
                studentId = null;
            }
        } else {
            // Role sau cập nhật không còn là READER -> xoá studentId cũ
            studentId = null;
        }

        // ===== 6. Gán dữ liệu đã validate vào object user =====
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStudentId(studentId);
        user.setRole(finalRole);

        boolean ok = userDAO.updateUser(user);
        return ok ? null : "Cập nhật thông tin thất bại.";
    }

    // isAdminEditingOther = true  -> admin đang đổi mật khẩu cho người khác, KHÔNG cần mật khẩu cũ
    // isAdminEditingOther = false -> người dùng tự đổi mật khẩu của mình, BẮT BUỘC nhập đúng mật khẩu cũ
    public String changePassword(User user, String oldPassword, String newPassword,
            String confirmPassword, boolean isAdminEditingOther) throws Exception {

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "Vui lòng nhập mật khẩu mới.";
        }
        if (newPassword.length() < 5) {
            return "Mật khẩu mới phải có ít nhất 5 ký tự.";
        }
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp.";
        }
        if (!isAdminEditingOther) {
            if (oldPassword == null || oldPassword.isEmpty()) {
                return "Vui lòng nhập mật khẩu cũ để xác thực.";
            }
            if (!hashPassword(oldPassword).equals(user.getPassword())) {
                return "Mật khẩu cũ không chính xác.";
            }
        }

        String hashed = hashPassword(newPassword);
        boolean ok = userDAO.updatePassword(user.getId(), hashed);
        if (ok) {
            user.setPassword(hashed);
        }
        return ok ? null : "Cập nhật mật khẩu thất bại.";
    }

    private String hashPassword(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }
}
