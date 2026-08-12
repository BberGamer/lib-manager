package service;

import dao.UserDAO;
import model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class LoginService {

    public User login(String username, String password) throws Exception {
        UserDAO dao = new UserDAO();
        User user = dao.getUserByUsername(username);
        if (user != null && user.getPassword() != null && checkPassword(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    private boolean checkPassword(String raw, String hashed) {
        if (raw == null || hashed == null) return false;
        return raw.equals(hashed) || hashPassword(raw).equals(hashed);
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
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }
}