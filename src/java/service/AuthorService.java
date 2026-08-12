/**
 * Service sở hữu validation và quy tắc nghiệp vụ của module quản lý tác giả.
 */
package service;

import dao.AuthorDAO;
import dao.AuthorDAOImpl;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import model.Author;

/**
 * Điều phối AuthorDAO, chuẩn hóa dữ liệu và bảo vệ ràng buộc quan hệ sách–tác giả.
 */
public class AuthorService {

    public static final int PAGE_SIZE = 8;
    public static final int NAME_MAX_LENGTH = 150;
    public static final int NATIONALITY_MAX_LENGTH = 100;
    public static final int BIO_MAX_LENGTH = 2000;
    public static final int AVATAR_URL_MAX_LENGTH = 500;

    private final AuthorDAO authorDao;

    /** Khởi tạo service với DAO hiện có của ứng dụng. */
    public AuthorService() {
        this(new AuthorDAOImpl());
    }

    /** @param authorDao DAO tác giả dùng cho persistence và kiểm thử */
    public AuthorService(AuthorDAO authorDao) {
        this.authorDao = authorDao;
    }

    /**
     * Lấy một trang tác giả theo filter.
     * @param keyword từ khóa tìm trong tên tác giả
     * @param sort trường sắp xếp
     * @param order chiều sắp xếp
     * @param page trang bắt đầu từ 1
     * @return danh sách tác giả
     * @throws AuthorException khi truy vấn thất bại
     */
    public List<Author> getAuthors(String keyword, String sort, String order, int page) throws AuthorException {
        try {
            int normalizedPage = Math.max(1, page);
            return authorDao.search(normalizeText(keyword), sort, order,
                    (normalizedPage - 1) * PAGE_SIZE, PAGE_SIZE);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new AuthorException("Không thể tải danh sách tác giả", exception);
        }
    }

    /** @param keyword từ khóa tìm trong tên tác giả @return tổng bản ghi phù hợp @throws AuthorException khi đếm thất bại */
    public int countAuthors(String keyword) throws AuthorException {
        try {
            return authorDao.count(normalizeText(keyword));
        } catch (SQLException | ClassNotFoundException exception) {
            throw new AuthorException("Không thể đếm tác giả", exception);
        }
    }

    /** @param keyword từ khóa tìm trong tên tác giả @return tổng trang, tối thiểu một @throws AuthorException khi đếm thất bại */
    public int getTotalPages(String keyword) throws AuthorException {
        int totalAuthors = countAuthors(keyword);
        return Math.max(1, (int) Math.ceil((double) totalAuthors / PAGE_SIZE));
    }

    /** @param id mã tác giả @return tác giả nếu tồn tại @throws AuthorException khi đọc thất bại */
    public Optional<Author> findAuthor(int id) throws AuthorException {
        try {
            return authorDao.findById(id);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new AuthorException("Không thể tải tác giả mã " + id, exception);
        }
    }

    /**
     * Chuẩn hóa, kiểm tra và tạo tác giả.
     * @param author dữ liệu biểu mẫu
     * @param actor tài khoản Admin
     * @return tác giả đã lưu
     * @throws AuthorValidationException khi dữ liệu không hợp lệ
     * @throws AuthorException khi lưu thất bại
     */
    public Author createAuthor(Author author, String actor)
            throws AuthorValidationException, AuthorException {
        normalize(author);
        Map<String, String> errors = validate(author);
        try {
            if (!errors.containsKey("name") && authorDao.existsByName(author.getName(), 0)) {
                errors.put("name", "Tên tác giả đã tồn tại.");
            }
            rejectInvalid(errors);
            return authorDao.insert(author, actor);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new AuthorException("Không thể tạo tác giả", exception);
        }
    }

    /**
     * Chuẩn hóa, kiểm tra và cập nhật tác giả.
     * @param author dữ liệu biểu mẫu có mã
     * @param actor tài khoản Admin
     * @return true nếu cập nhật thành công
     * @throws AuthorValidationException khi dữ liệu không hợp lệ
     * @throws AuthorException khi cập nhật thất bại
     */
    public boolean updateAuthor(Author author, String actor)
            throws AuthorValidationException, AuthorException {
        normalize(author);
        Map<String, String> errors = validate(author);
        try {
            if (!errors.containsKey("name") && authorDao.existsByName(author.getName(), author.getId())) {
                errors.put("name", "Tên tác giả đã tồn tại.");
            }
            rejectInvalid(errors);
            return authorDao.update(author, actor);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new AuthorException("Không thể cập nhật tác giả mã " + author.getId(), exception);
        }
    }

    /**
     * Xóa vật lý tác giả khi không còn quan hệ với sách.
     * @param id mã tác giả
     * @return true nếu xóa thành công
     * @throws AuthorValidationException khi còn sách tham chiếu
     * @throws AuthorException khi kiểm tra hoặc xóa thất bại
     */
    public boolean deleteAuthor(int id) throws AuthorValidationException, AuthorException {
        try {
            Map<String, String> errors = new LinkedHashMap<>();
            if (authorDao.hasBooks(id)) {
                errors.put("delete", "Không thể xóa tác giả vì vẫn còn sách liên kết.");
            }
            rejectInvalid(errors);
            return authorDao.deleteById(id);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new AuthorException("Không thể xóa tác giả mã " + id, exception);
        }
    }

    /** @param value chuỗi nullable @return chuỗi đã trim hoặc rỗng */
    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    /** @param value chuỗi nullable @return null nếu rỗng, ngược lại là chuỗi đã trim */
    private String normalizeOptional(String value) {
        String normalized = normalizeText(value);
        return normalized.isEmpty() ? null : normalized;
    }

    /** @param author dữ liệu cần chuẩn hóa tại chỗ */
    private void normalize(Author author) {
        author.setName(normalizeText(author.getName()));
        author.setNationality(normalizeOptional(author.getNationality()));
        author.setBio(normalizeOptional(author.getBio()));
        author.setAvatarUrl(normalizeOptional(author.getAvatarUrl()));
    }

    /** @param author dữ liệu đã chuẩn hóa @return lỗi theo trường */
    private Map<String, String> validate(Author author) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (author.getName().isEmpty()) {
            errors.put("name", "Tên tác giả là bắt buộc.");
        } else if (author.getName().length() > NAME_MAX_LENGTH) {
            errors.put("name", "Tên tác giả không được vượt quá 150 ký tự.");
        }
        if (author.getNationality() != null && author.getNationality().length() > NATIONALITY_MAX_LENGTH) {
            errors.put("nationality", "Quốc tịch không được vượt quá 100 ký tự.");
        }
        if (author.getBirthDate() != null && author.getBirthDate().isAfter(LocalDate.now())) {
            errors.put("birthDate", "Ngày sinh không được nằm trong tương lai.");
        }
        if (author.getBio() != null && author.getBio().length() > BIO_MAX_LENGTH) {
            errors.put("bio", "Tiểu sử không được vượt quá 2000 ký tự.");
        }
        if (author.getAvatarUrl() != null) {
            if (author.getAvatarUrl().length() > AVATAR_URL_MAX_LENGTH) {
                errors.put("avatarUrl", "URL ảnh không được vượt quá 500 ký tự.");
            } else if (!isHttpUrl(author.getAvatarUrl())) {
                errors.put("avatarUrl", "URL ảnh phải sử dụng http hoặc https.");
            }
        }
        return errors;
    }

    /** @param value URL cần kiểm tra @return true nếu là HTTP(S) URL hợp lệ */
    private boolean isHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            return uri.getHost() != null && ("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()));
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    /** @param errors lỗi hiện tại @throws AuthorValidationException khi có lỗi */
    private void rejectInvalid(Map<String, String> errors) throws AuthorValidationException {
        if (!errors.isEmpty()) {
            throw new AuthorValidationException(errors);
        }
    }
}
