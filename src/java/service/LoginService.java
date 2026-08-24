/**
 * Lớp dịch vụ xử lý logic đăng nhập hệ thống.
 * Thuộc tầng service, đảm nhận kiểm tra thông tin người dùng và trạng thái tài khoản.
 */
package service;

import dao.UserDAO;
import model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Dịch vụ xử lý đăng nhập cho người dùng hệ thống.
 *
 * Lớp này chịu trách nhiệm xác thực tên đăng nhập, mật khẩu và kiểm tra trạng
 * thái hoạt động của tài khoản người dùng trước khi cấp quyền đăng nhập.
 */
public class LoginService {

    /**
     * Ngoại lệ khi tài khoản người dùng bị khóa.
     */
    public static class AccountLockedException extends Exception {

        public AccountLockedException(String message) {
            super(message);
        }
    }

    public static class EmptyInputException extends Exception {

        public EmptyInputException(String message) {
            super(message);
        }
    }

    public static class InvalidCredentialsException extends Exception {

        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    /**
     * Thực hiện xác thực đăng nhập người dùng.
     *
     * @param username tên đăng nhập
     * @param password mật khẩu thô
     * @return đối tượng User nếu đăng nhập thành công
     * @throws EmptyInputException khi tên đăng nhập hoặc mật khẩu bị trống
     * @throws InvalidCredentialsException khi tên đăng nhập hoặc mật khẩu không
     * chính xác
     * @throws AccountLockedException khi tài khoản người dùng đang bị khóa
     * @throws Exception khi có lỗi truy vấn dữ liệu từ DAO
     */
    public User login(String username, String password)
            throws EmptyInputException, InvalidCredentialsException, AccountLockedException, Exception {

        if (username != null) {
            username = username.trim();
        }

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new EmptyInputException("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
        }

        UserDAO dao = new UserDAO();
        User user = dao.getUserByUsername(username);

        // 1. Kiểm tra tồn tại và kiểm tra trạng thái Xóa mềm (is_deleted = 1)
        if (user == null || user.getIsDeleted() == 1) {
            throw new InvalidCredentialsException("Tên đăng nhập hoặc mật khẩu không đúng!");
        }

        // 2. Kiểm tra mật khẩu
        if (user.getPassword() == null || !checkPassword(password, user.getPassword())) {
            throw new InvalidCredentialsException("Tên đăng nhập hoặc mật khẩu không đúng!");
        }

        // 3. Kiểm tra trạng thái Khóa tài khoản (active != 1)
        if (user.getActive() != 1) {
            throw new AccountLockedException("Tài khoản của bạn đã bị khoá. Vui lòng liên hệ thủ thư!");
        }

        return user;
    }

    /**
     * Kiểm tra mật khẩu nhập vào so với mật khẩu lưu trong cơ sở dữ liệu.
     *
     * @param raw mật khẩu dạng thô
     * @param hashed mật khẩu đã mã hóa hoặc chuỗi lưu trong DB
     * @return true nếu mật khẩu khớp, ngược lại false
     */
    private boolean checkPassword(String raw, String hashed) {
        if (raw == null || hashed == null) {
            return false;
        }
        return raw.equals(hashed) || hashPassword(raw).equals(hashed);
    }

    /**
     * Mã hóa chuỗi mật khẩu sang định dạng MD5.
     *
     * @param raw mật khẩu dạng thô
     * @return chuỗi băm hex MD5
     */
    private String hashPassword(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }
}
