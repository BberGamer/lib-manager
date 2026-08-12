package service;

import dao.UserDAO;
import model.User;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class UserProfileService {

    private final UserDAO userDAO = new UserDAO();

    public User getProfile(int userId) throws Exception {
        return userDAO.getUserById(userId);
    }

    // isAdmin = true -> cho phép sửa thêm role/active (chỉ admin mới có quyền này)
    public String updateProfile(User user, String fullName, String email, String phone,
                                 String studentId, String avatar, String role, Integer active, boolean isAdmin) throws Exception {
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStudentId(studentId);
        user.setAvatar(avatar);

        if (isAdmin) {
            if (role != null) user.setRole(role);
            if (active != null) user.setActive(active);
        }

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
        if (!newPassword.equals(confirmPassword)) {
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