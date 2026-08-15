package service;

import dao.UserDAO;
import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Toàn bộ business logic cho quản lý User.
 * Servlet chỉ gọi các hàm ở đây; UserDAO khai báo "throws Exception" ở mọi hàm
 * nên Service bọc mọi lỗi DAO thành IllegalArgumentException/IllegalStateException
 * (unchecked) để servlet chỉ cần catch 2 loại đó, không cần "throws Exception".
 *
 * Ghi chú: student_id trong DB là NULL-able + UNIQUE (xem schema), nên KHÔNG ép
 * buộc phải nhập — chỉ kiểm tra định dạng + trùng nếu người dùng có nhập.
 */
public class UserListService {

    private static final int PAGE_SIZE = 15;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");

    // Email: cho phép ký tự đặc biệt phổ biến trong phần tên (. _ + -), domain có thể
    // nhiều đoạn (a.b.c), phần đuôi (.com, .vn...) tối thiểu 2 chữ cái.
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9,10}$");

    // Mã sinh viên: bắt đầu bằng "H", theo sau là đúng 1 chữ cái in hoa, rồi đúng 6 chữ số.
    // Ví dụ hợp lệ: HA123456, HB000123. Tổng cộng 8 ký tự.
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^H[A-Z][0-9]{6}$");

    private static final int PASSWORD_MIN_LEN = 5;
    private static final int EMAIL_MAX_LEN = 100; // đồng bộ với cột email trong DB

    private final UserDAO dao = new UserDAO();

    /** Kết quả tìm kiếm + phân trang để servlet set thẳng vào request. */
    public static class SearchResult {
        public List<User> users;
        public int totalRecords;
        public int totalPages;
        public int currentPage;
    }

    public SearchResult search(String q, String role, Integer active,
                                String sortField, String sortOrder, int page) {
        List<User> all;
        try {
            all = dao.searchUsers(q, role, active, sortField, sortOrder);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tải danh sách người dùng: " + e.getMessage(), e);
        }

        SearchResult r = new SearchResult();
        r.totalRecords = all.size();
        r.totalPages = Math.max(1, (int) Math.ceil((double) r.totalRecords / PAGE_SIZE));
        int p = Math.min(Math.max(page, 1), r.totalPages);
        int from = (p - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, r.totalRecords);
        r.users = (from < r.totalRecords) ? all.subList(from, to) : all;
        r.currentPage = p;
        return r;
    }

    /** Ném IllegalArgumentException với message thân thiện nếu dữ liệu không hợp lệ. */
    public int createUser(User u, String rawPassword) {
        try {
            // 1. Validate Username (tên đăng nhập: Regex kiểm tra chữ/số/gạch dưới + Check trùng lặp DB)
            String username = u.getUsername() == null ? "" : u.getUsername().trim();
            if (!USERNAME_PATTERN.matcher(username).matches()) {
                throw new IllegalArgumentException(
                    "Tên đăng nhập chỉ chứa chữ cái, số, dấu gạch dưới, tối thiểu 3 ký tự.");
            }
            if (dao.getUserByUsername(username) != null) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
            }

            // 2. Validate Email (Kiểm tra rỗng, độ dài max 100, Regex email + Check trùng DB)
            if (u.getEmail() == null || u.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email không được để trống.");
            }
            String email = u.getEmail().trim();
            if (email.length() > EMAIL_MAX_LEN) {
                throw new IllegalArgumentException("Email không được vượt quá " + EMAIL_MAX_LEN + " ký tự.");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Email không đúng định dạng.");
            }
            if (dao.getUserByEmail(email) != null) {
                throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác.");
            }

            // 3. Validate Số điện thoại (Không bắt buộc; nếu có nhập -> Regex 10-11 số bắt đầu bằng 0)
            if (u.getPhone() != null && !u.getPhone().isBlank()
                    && !PHONE_PATTERN.matcher(u.getPhone().trim()).matches()) {
                throw new IllegalArgumentException("Số điện thoại phải có 10-11 chữ số và bắt đầu bằng 0.");
            }

            // 4. Validate Mã sinh viên student_id (Không bắt buộc; nếu nhập -> đúng chuẩn HA123456 & Check trùng DB)
            String studentId = u.getStudentId() == null ? "" : u.getStudentId().trim();
            if (!studentId.isEmpty()) {
                if (!STUDENT_ID_PATTERN.matcher(studentId).matches()) {
                    throw new IllegalArgumentException(
                        "Mã sinh viên không đúng định dạng (VD: HA123456 - chữ H, 1 chữ cái in hoa, 6 chữ số).");
                }
                if (dao.getUserByStudentId(studentId) != null) {
                    throw new IllegalArgumentException("Mã sinh viên đã được sử dụng bởi tài khoản khác.");
                }
            }

            // 5. Validate Mật khẩu (mặc định 'password' nếu để trống, tối thiểu 5 ký tự)
            String password = (rawPassword == null || rawPassword.isEmpty()) ? "password" : rawPassword;
            if (password.length() < PASSWORD_MIN_LEN) {
                throw new IllegalArgumentException("Mật khẩu phải có ít nhất " + PASSWORD_MIN_LEN + " ký tự.");
            }

            // 6. Gán thuộc tính hợp lệ và gọi DAO lưu DB với mật khẩu băm MD5
            u.setUsername(username);
            u.setEmail(email);
            u.setStudentId(studentId.isEmpty() ? null : studentId); // Để NULL nếu rỗng để không bị vi phạm UNIQUE DB
            u.setActive(1);
            return dao.createUser(u, hashPassword(password));
        } catch (IllegalArgumentException e) {
            throw e; // Lỗi validate -> Giữ nguyên ném ra cho Servlet hiển thị thông báo
        } catch (Exception e) {
            throw new IllegalStateException("Tạo người dùng thất bại: " + e.getMessage(), e);
        }
    }

    /** Load user hiện có trong DB rồi mới áp field mới lên, tránh NPE và tránh ghi đè username/password. */
    public void updateUser(int id, String fullName, String email, String phone,
                            String studentId, String avatar, String role,
                            Integer active, String newPassword) {
        try {
            User u = dao.getUserById(id);
            if (u == null) {
                throw new IllegalArgumentException("Không tìm thấy người dùng với ID = " + id + ".");
            }

            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email không được để trống.");
            }
            String trimmedEmail = email.trim();
            if (trimmedEmail.length() > EMAIL_MAX_LEN) {
                throw new IllegalArgumentException("Email không được vượt quá " + EMAIL_MAX_LEN + " ký tự.");
            }
            if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
                throw new IllegalArgumentException("Email không đúng định dạng.");
            }
            // Chỉ check trùng khi email mới khác email cũ
            if (!trimmedEmail.equalsIgnoreCase(u.getEmail())) {
                User existing = dao.getUserByEmail(trimmedEmail);
                if (existing != null && existing.getId() != id) {
                    throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác.");
                }
            }

            if (phone != null && !phone.isBlank() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
                throw new IllegalArgumentException("Số điện thoại phải có 10-11 chữ số và bắt đầu bằng 0.");
            }

            String sid = studentId == null ? "" : studentId.trim();
            if (!sid.isEmpty()) {
                if (!STUDENT_ID_PATTERN.matcher(sid).matches()) {
                    throw new IllegalArgumentException(
                        "Mã sinh viên không đúng định dạng (VD: HA123456 - chữ H, 1 chữ cái in hoa, 6 chữ số).");
                }
                User owner = dao.getUserByStudentId(sid);
                if (owner != null && owner.getId() != id) {
                    throw new IllegalArgumentException("Mã sinh viên đã được sử dụng bởi tài khoản khác.");
                }
            }

            u.setFullName(fullName);
            u.setEmail(trimmedEmail);
            u.setPhone(phone);
            u.setStudentId(sid.isEmpty() ? null : sid);
            u.setAvatar(avatar);
            u.setRole(role);
            if (active != null) u.setActive(active);

            boolean ok = dao.updateUser(u);
            if (!ok) throw new IllegalStateException("Cập nhật thất bại.");

            if (newPassword != null && !newPassword.isEmpty()) {
                if (newPassword.length() < PASSWORD_MIN_LEN) {
                    throw new IllegalArgumentException("Mật khẩu phải có ít nhất " + PASSWORD_MIN_LEN + " ký tự.");
                }
                dao.updatePassword(id, hashPassword(newPassword));
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Cập nhật thất bại: " + e.getMessage(), e);
        }
    }

    public void setActive(int id, int active) {
        try {
            boolean ok = dao.setActive(id, active);
            if (!ok) throw new IllegalStateException("Thao tác thất bại.");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Thao tác thất bại: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa người dùng (Xóa mềm - Chuyển active = 0).
     * Bổ sung bước 1 kiểm tra dữ liệu liên quan dở dang (sách đang mượn, nợ tiền phạt, sách đang đặt).
     * Nếu có giao dịch dở dang, hệ thống từ chối xóa và ném thông báo chi tiết.
     */
    public void deleteUser(int id) {
        try {
            // Bước 1: Kiểm tra các giao dịch dở dang (sách mượn chưa trả, phạt chưa thanh toán, phiếu đặt sách)
            String pendingErr = dao.checkUserPendingTransactions(id);
            if (pendingErr != null) {
                throw new IllegalStateException(pendingErr);
            }

            // Bước 2: Thực hiện Xóa mềm (UPDATE users SET active = 0)
            boolean ok = dao.deleteUser(id);
            if (!ok) throw new IllegalStateException("Xóa thất bại: không tìm thấy người dùng.");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Giữ MD5 để không phá vỡ dữ liệu/login hiện có (đổi thuật toán hash sẽ làm
     * mọi mật khẩu cũ không đăng nhập được nữa nếu không có bước migrate).
     */
    private String hashPassword(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không thể mã hóa mật khẩu", e);
        }
    }
}