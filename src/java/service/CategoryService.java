/**
 * Service sở hữu validation và quy tắc nghiệp vụ của chức năng quản lý thể loại.
 */
package service;

import dao.CategoryDao;
import exception.CategoryException;
import exception.CategoryValidationException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import model.Category;

/**
 * Điều phối CategoryDao, kiểm tra dữ liệu, tên duy nhất và điều kiện xóa thể loại.
 */
public class CategoryService {

    public static final int NAME_MAX_LENGTH = 100;
    public static final int DESCRIPTION_MAX_LENGTH = 500;
    public static final int PAGE_SIZE = 10;

    private final CategoryDao categoryDao;

    /** Khởi tạo service với DAO mặc định của ứng dụng. */
    public CategoryService() {
        this(new CategoryDao());
    }

    /**
     * Khởi tạo service với dependency truyền vào để hỗ trợ kiểm thử cô lập.
     * @param categoryDao DAO chịu trách nhiệm lưu trữ thể loại
     */
    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    /**
     * Lấy một trang thể loại theo từ khóa và thứ tự, tự chuẩn hóa số trang nhỏ hơn 1.
     * @param keyword từ khóa tìm theo tên danh mục
     * @param sort trường sắp xếp được controller giới hạn
     * @param order chiều sắp xếp ASC hoặc DESC
     * @param page số trang bắt đầu từ 1
     * @return danh sách thể loại của trang
     * @throws CategoryException khi không đọc được dữ liệu
     */
    public List<Category> getCategories(String keyword, String sort, String order, int page) throws CategoryException {
        int normalizedPage = Math.max(page, 1);
        try {
            return categoryDao.findAll(normalizeKeyword(keyword), sort, order,
                    (normalizedPage - 1) * PAGE_SIZE, PAGE_SIZE);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new CategoryException("Không thể tải danh sách thể loại", exception);
        }
    }

    /**
     * Tính tổng số trang theo từ khóa và luôn trả về tối thiểu một trang cho giao diện.
     * @param keyword từ khóa tìm theo tên danh mục
     * @return tổng số trang
     * @throws CategoryException khi không đếm được dữ liệu
     */
    public int getTotalPages(String keyword) throws CategoryException {
        try {
            int totalItems = categoryDao.count(normalizeKeyword(keyword));
            return Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        } catch (SQLException | ClassNotFoundException exception) {
            throw new CategoryException("Không thể đếm thể loại", exception);
        }
    }

    /**
     * Đếm số thể loại phù hợp với từ khóa để hiển thị tổng kết trên danh sách.
     * @param keyword từ khóa tìm theo tên danh mục
     * @return số thể loại phù hợp
     * @throws CategoryException khi không thể đếm dữ liệu
     */
    public int countCategories(String keyword) throws CategoryException {
        try {
            return categoryDao.count(normalizeKeyword(keyword));
        } catch (SQLException | ClassNotFoundException exception) {
            throw new CategoryException("Không thể đếm thể loại", exception);
        }
    }

    /**
     * Chuẩn hóa từ khóa nullable trước khi chuyển xuống DAO.
     * @param keyword từ khóa từ request
     * @return từ khóa đã bỏ khoảng trắng hoặc chuỗi rỗng
     */
    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    /**
     * Tìm một thể loại đang hoạt động.
     * @param id mã thể loại hợp lệ
     * @return thể loại nếu tồn tại
     * @throws CategoryException khi không đọc được dữ liệu
     */
    public Optional<Category> findCategory(int id) throws CategoryException {
        try {
            return categoryDao.findById(id);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new CategoryException("Không thể tải thể loại mã " + id, exception);
        }
    }

    /**
     * Kiểm tra và tạo thể loại mới.
     * @param category dữ liệu biểu mẫu
     * @param actor tài khoản quản trị thực hiện thao tác
     * @return thể loại đã lưu
     * @throws CategoryValidationException khi dữ liệu hoặc tên không hợp lệ
     * @throws CategoryException khi không thể lưu dữ liệu
     */
    public Category createCategory(Category category, String actor)
            throws CategoryValidationException, CategoryException {
        normalize(category);
        Map<String, String> errors = validate(category);
        try {
            if (!errors.containsKey("name") && categoryDao.existsByName(category.getName(), 0)) {
                errors.put("name", "Tên thể loại đã tồn tại.");
            }
            rejectInvalid(errors);
            return categoryDao.insert(category, actor);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new CategoryException("Không thể tạo thể loại", exception);
        }
    }

    /**
     * Kiểm tra và cập nhật một thể loại đang hoạt động.
     * @param category dữ liệu biểu mẫu có mã hiện hữu
     * @param actor tài khoản quản trị thực hiện thao tác
     * @return {@code true} nếu cập nhật thành công
     * @throws CategoryValidationException khi dữ liệu hoặc tên không hợp lệ
     * @throws CategoryException khi không thể cập nhật
     */
    public boolean updateCategory(Category category, String actor)
            throws CategoryValidationException, CategoryException {
        normalize(category);
        Map<String, String> errors = validate(category);
        try {
            if (!errors.containsKey("name")
                    && categoryDao.existsByName(category.getName(), category.getId())) {
                errors.put("name", "Tên thể loại đã tồn tại.");
            }
            rejectInvalid(errors);
            return categoryDao.update(category, actor);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new CategoryException("Không thể cập nhật thể loại mã " + category.getId(), exception);
        }
    }

    /**
     * Xóa mềm thể loại nếu không còn sách đang hoạt động sử dụng.
     * @param id mã thể loại
     * @param actor tài khoản quản trị thực hiện thao tác
     * @return {@code true} nếu xóa thành công
     * @throws CategoryValidationException khi thể loại còn được sách sử dụng
     * @throws CategoryException khi không thể kiểm tra hoặc xóa dữ liệu
     */
    public boolean deleteCategory(int id, String actor)
            throws CategoryValidationException, CategoryException {
        try {
            Map<String, String> errors = new LinkedHashMap<>();
            if (categoryDao.hasActiveBooks(id)) {
                errors.put("delete", "Không thể xóa thể loại đang được sách sử dụng.");
            }
            rejectInvalid(errors);
            return categoryDao.softDelete(id, actor);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new CategoryException("Không thể xóa thể loại mã " + id, exception);
        }
    }

    /**
     * Chuẩn hóa khoảng trắng và chuyển mô tả rỗng thành null.
     * @param category dữ liệu cần chuẩn hóa tại chỗ
     */
    private void normalize(Category category) {
        String name = category.getName();
        String description = category.getDescription();
        category.setName(name == null ? null : name.trim());
        category.setDescription(description == null || description.trim().isEmpty()
                ? null : description.trim());
    }

    /**
     * Kiểm tra các giới hạn khớp schema categories.
     * @param category dữ liệu đã chuẩn hóa
     * @return bản đồ lỗi theo trường
     */
    private Map<String, String> validate(Category category) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (category.getName() == null || category.getName().isEmpty()) {
            errors.put("name", "Tên thể loại là bắt buộc.");
        } else if (category.getName().length() > NAME_MAX_LENGTH) {
            errors.put("name", "Tên thể loại không được vượt quá 100 ký tự.");
        }
        if (category.getDescription() != null
                && category.getDescription().length() > DESCRIPTION_MAX_LENGTH) {
            errors.put("description", "Mô tả không được vượt quá 500 ký tự.");
        }
        return errors;
    }

    /**
     * Dừng use case khi có ít nhất một lỗi validation.
     * @param errors bản đồ lỗi hiện tại
     * @throws CategoryValidationException khi bản đồ không rỗng
     */
    private void rejectInvalid(Map<String, String> errors) throws CategoryValidationException {
        if (!errors.isEmpty()) {
            throw new CategoryValidationException(errors);
        }
    }
}
