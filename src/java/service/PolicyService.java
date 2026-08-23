/**
 * Service sở hữu validation, vòng đời xuất bản và truy vấn hiệu lực của Policy.
 */
package service;

import exception.PolicyException;
import exception.PolicyValidationException;

import dao.PolicyDao;
import dao.PolicyDao.PublishResult;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import model.Policy;
import model.PolicyCategory;
import model.PolicyPublicationStatus;

/** Điều phối PolicyDao và bảo vệ mọi invariant trước khi ghi database. */
public class PolicyService {

    public static final int CODE_MAX_LENGTH = 50;
    public static final int TITLE_MIN_LENGTH = 3;
    public static final int TITLE_MAX_LENGTH = 200;
    public static final int CONTENT_MAX_LENGTH = 10000;
    public static final int PAGE_SIZE = 10;
    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Z][A-Z0-9_]{1,49}");
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final PolicyDao policyDao;
    private final Clock clock;

    /** Khởi tạo service với DAO mặc định và đồng hồ nghiệp vụ Việt Nam. */
    public PolicyService() {
        this(new PolicyDao(), Clock.system(BUSINESS_ZONE));
    }

    /**
     * Khởi tạo service có thể kiểm thử cô lập.
     * @param policyDao DAO lưu trữ điều lệ
     * @param clock đồng hồ xác định ngày nghiệp vụ
     */
    public PolicyService(PolicyDao policyDao, Clock clock) {
        this.policyDao = policyDao;
        this.clock = clock;
    }

    /** @return danh sách đầy đủ dành cho Admin theo trang và bộ lọc */
    public List<Policy> getAdminPolicies(String keyword, PolicyCategory category,
            PolicyPublicationStatus status, int page) throws PolicyException {
        try {
            List<Policy> policies = policyDao.findAll(normalizeKeyword(keyword), category, status,
                    offset(page), PAGE_SIZE);
            policies.forEach(this::decorateEffectiveStatus);
            return policies;
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể tải danh sách điều lệ", exception);
        }
    }

    /** @return tổng số bản ghi quản trị phù hợp bộ lọc */
    public int countAdminPolicies(String keyword, PolicyCategory category,
            PolicyPublicationStatus status) throws PolicyException {
        try {
            return policyDao.count(normalizeKeyword(keyword), category, status);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể đếm điều lệ", exception);
        }
    }

    /** @return tổng số trang quản trị, tối thiểu một trang */
    public int getAdminTotalPages(String keyword, PolicyCategory category,
            PolicyPublicationStatus status) throws PolicyException {
        return totalPages(countAdminPolicies(keyword, category, status));
    }

    /** @return một điều lệ bất kỳ chưa xóa cho Admin */
    public Optional<Policy> findAdminPolicy(int id) throws PolicyException {
        try {
            Optional<Policy> policy = policyDao.findById(id);
            policy.ifPresent(this::decorateEffectiveStatus);
            return policy;
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể tải điều lệ mã " + id, exception);
        }
    }

    /** @return danh sách công khai đang có hiệu lực */
    public List<Policy> getEffectivePolicies(String keyword, PolicyCategory category, int page)
            throws PolicyException {
        try {
            List<Policy> policies = policyDao.findEffective(normalizeKeyword(keyword), category,
                    businessToday(), offset(page), PAGE_SIZE);
            policies.forEach(this::decorateEffectiveStatus);
            return policies;
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể tải điều lệ đang áp dụng", exception);
        }
    }

    /** @return tổng số điều lệ đang có hiệu lực */
    public int countEffectivePolicies(String keyword, PolicyCategory category) throws PolicyException {
        try {
            return policyDao.countEffective(normalizeKeyword(keyword), category, businessToday());
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể đếm điều lệ đang áp dụng", exception);
        }
    }

    /** @return tổng số trang công khai, tối thiểu một trang */
    public int getEffectiveTotalPages(String keyword, PolicyCategory category) throws PolicyException {
        return totalPages(countEffectivePolicies(keyword, category));
    }

    /** @return điều lệ theo ID khi đang hiệu lực, ngược lại rỗng */
    public Optional<Policy> findEffectivePolicy(int id) throws PolicyException {
        try {
            Optional<Policy> policy = policyDao.findEffectiveById(id, businessToday());
            policy.ifPresent(this::decorateEffectiveStatus);
            return policy;
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể tải điều lệ đang áp dụng mã " + id, exception);
        }
    }

    /** Kiểm tra và tạo một bản draft mới. */
    public Policy createDraft(Policy policy, String actor)
            throws PolicyValidationException, PolicyException {
        policy.setVersion(1);
        normalize(policy);
        Map<String, String> errors = validateDraft(policy);
        try {
            validateUniqueVersion(policy, errors);
            rejectInvalid(errors);
            return policyDao.insert(policy, actor);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể tạo bản nháp điều lệ", exception);
        }
    }

    /** Kiểm tra và cập nhật bản draft; bản đã xuất bản không thể sửa. */
    public boolean updateDraft(Policy policy, String actor)
            throws PolicyValidationException, PolicyException {
        try {
            Policy existingDraft = policyDao.findById(policy.getId()).orElse(null);
            if (existingDraft == null
                    || existingDraft.getPublicationStatus() != PolicyPublicationStatus.DRAFT) {
                return false;
            }
            policy.setVersion(existingDraft.getVersion());
            normalize(policy);
            Map<String, String> errors = validateDraft(policy);
            validateUniqueVersion(policy, errors);
            rejectInvalid(errors);
            return policyDao.updateDraft(policy, actor);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể cập nhật bản nháp điều lệ", exception);
        }
    }

    /**
     * Tạo draft phiên bản kế tiếp từ điều lệ đã xuất bản để giữ lịch sử công khai.
     * @param sourceId ID phiên bản đã xuất bản
     * @param revision nội dung phiên bản mới nhận từ form và đã được kiểm tra
     * @param actor tài khoản thực hiện
     * @return draft mới hoặc draft cùng mã đang tồn tại
     * @throws PolicyValidationException khi nguồn không phải bản đã xuất bản
     * @throws PolicyException khi persistence thất bại
     */
    public Policy createRevision(int sourceId, Policy revision, String actor)
            throws PolicyValidationException, PolicyException {
        Policy source = findAdminPolicy(sourceId).orElse(null);
        Map<String, String> errors = new LinkedHashMap<>();
        if (source == null || source.getPublicationStatus() != PolicyPublicationStatus.PUBLISHED) {
            errors.put("action", "Chỉ tạo được phiên bản mới từ điều lệ đang ở trạng thái đã xuất bản.");
            rejectInvalid(errors);
        }
        revision.setPolicyCode(source.getPolicyCode());
        normalize(revision);
        errors.putAll(validateDraft(revision));
        rejectInvalid(errors);
        try {
            Optional<Policy> savedRevision = policyDao.createRevision(sourceId, revision, actor);
            if (savedRevision.isEmpty()) {
                throw actionError("Chỉ tạo được phiên bản mới từ điều lệ đang ở trạng thái đã xuất bản.");
            }
            return savedRevision.get();
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể tạo phiên bản mới từ điều lệ mã " + sourceId, exception);
        }
    }

    /** @return draft cùng mã đang tồn tại để Admin tiếp tục chỉnh sửa */
    public Optional<Policy> findDraftByCode(String policyCode) throws PolicyException {
        try {
            return policyDao.findDraftByCode(policyCode);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể tìm bản nháp của mã điều lệ " + policyCode, exception);
        }
    }

    /** Xuất bản draft và tự lưu trữ phiên bản đang xuất bản cùng mã trong một giao dịch. */
    public void publishPolicy(int id, String actor)
            throws PolicyValidationException, PolicyException {
        Policy policy = findAdminPolicy(id).orElse(null);
        Map<String, String> errors = new LinkedHashMap<>();
        if (policy == null || policy.getPublicationStatus() != PolicyPublicationStatus.DRAFT) {
            errors.put("action", "Chỉ bản nháp đang tồn tại mới được xuất bản.");
        } else if (policy.getEffectiveFrom() == null) {
            errors.put("effectiveFrom", "Cần nhập ngày hiệu lực trước khi xuất bản.");
        }
        rejectInvalid(errors);
        try {
            PublishResult result = policyDao.publish(id, actor);
            if (result == PublishResult.INVALID_STATE) {
                errors.put("action", "Trạng thái điều lệ đã thay đổi; vui lòng tải lại trang.");
            }
            rejectInvalid(errors);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể xuất bản điều lệ mã " + id, exception);
        }
    }

    /** Lưu trữ bản đã xuất bản và làm nó biến mất khỏi trang công khai. */
    public void archivePolicy(int id, String actor)
            throws PolicyValidationException, PolicyException {
        try {
            if (!policyDao.archive(id, actor)) {
                throw actionError("Chỉ điều lệ đã xuất bản mới được lưu trữ.");
            }
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể lưu trữ điều lệ mã " + id, exception);
        }
    }

    /**
     * Sử dụng lại điều lệ đã lưu trữ khi mã điều lệ đó chỉ có một phiên bản trong lịch sử,
     * đồng thời giữ nguyên khoảng hiệu lực đã thiết lập.
     * @param id mã định danh bản điều lệ cần sử dụng lại
     * @param actor tài khoản thực hiện thao tác
     * @throws PolicyValidationException khi điều lệ không đủ điều kiện sử dụng lại
     * @throws PolicyException khi không thể cập nhật dữ liệu
     */
    public void reuseArchivedPolicy(int id, String actor)
            throws PolicyValidationException, PolicyException {
        try {
            if (!policyDao.reuseArchivedSingleVersion(id, actor)) {
                throw actionError("Chỉ có thể sử dụng lại điều lệ đã lưu trữ khi điều lệ chỉ có một phiên bản.");
            }
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể sử dụng lại điều lệ mã " + id, exception);
        }
    }

    /** Xóa mềm bản draft; không cho xóa bản đã xuất bản hoặc lưu trữ. */
    public void deleteDraft(int id, String actor)
            throws PolicyValidationException, PolicyException {
        try {
            if (!policyDao.deleteDraft(id, actor)) {
                throw actionError("Chỉ bản nháp mới được xóa.");
            }
        } catch (SQLException | ClassNotFoundException exception) {
            throw new PolicyException("Không thể xóa bản nháp điều lệ mã " + id, exception);
        }
    }

    /** Chuẩn hóa mã, tiêu đề và nội dung trước khi validation. */
    private void normalize(Policy policy) {
        policy.setPolicyCode(normalizeKeyword(policy.getPolicyCode()).toUpperCase());
        policy.setTitle(normalizeKeyword(policy.getTitle()));
        String content = policy.getContent();
        policy.setContent(content == null ? "" : content.strip());
    }

    /** @return lỗi theo trường của một draft */
    private Map<String, String> validateDraft(Policy policy) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (!CODE_PATTERN.matcher(policy.getPolicyCode()).matches()) {
            errors.put("policyCode", "Mã gồm 2–50 ký tự in hoa, số hoặc dấu gạch dưới và bắt đầu bằng chữ.");
        }
        if (policy.getTitle().length() < TITLE_MIN_LENGTH || policy.getTitle().length() > TITLE_MAX_LENGTH) {
            errors.put("title", "Tiêu đề phải có từ 3 đến 200 ký tự.");
        }
        if (policy.getContent().isEmpty()) {
            errors.put("content", "Nội dung điều lệ là bắt buộc.");
        } else if (policy.getContent().length() > CONTENT_MAX_LENGTH) {
            errors.put("content", "Nội dung không được vượt quá 10000 ký tự.");
        }
        if (policy.getCategory() == null) {
            errors.put("category", "Danh mục điều lệ không hợp lệ.");
        }
        if (policy.getEffectiveTo() != null && policy.getEffectiveFrom() == null) {
            errors.put("effectiveFrom", "Cần nhập ngày bắt đầu khi có ngày kết thúc.");
        } else if (policy.getEffectiveFrom() != null && policy.getEffectiveTo() != null
                && policy.getEffectiveTo().isBefore(policy.getEffectiveFrom())) {
            errors.put("effectiveTo", "Ngày kết thúc không được trước ngày bắt đầu.");
        }
        return errors;
    }

    /** Thêm lỗi nếu mã và phiên bản đã được sử dụng. */
    private void validateUniqueVersion(Policy policy, Map<String, String> errors)
            throws SQLException, ClassNotFoundException {
        if (!errors.containsKey("policyCode")
                && policyDao.existsByCodeAndVersion(policy.getPolicyCode(), policy.getVersion(), policy.getId())) {
            errors.put("policyCode", "Mã điều lệ này đã tồn tại ở cùng phiên bản.");
        }
    }

    /** Chuẩn bị nhãn trạng thái hiệu lực để JSP không chứa business logic. */
    private void decorateEffectiveStatus(Policy policy) {
        policy.setReusable(policy.getPublicationStatus() == PolicyPublicationStatus.ARCHIVED
                && policy.isOnlyVersion());
        if (policy.getPublicationStatus() == PolicyPublicationStatus.DRAFT) {
            policy.setEffectiveStatus("Bản nháp");
            return;
        }
        if (policy.getPublicationStatus() == PolicyPublicationStatus.ARCHIVED) {
            policy.setEffectiveStatus("Đã lưu trữ");
            return;
        }
        LocalDate today = businessToday();
        if (policy.getEffectiveFrom() != null && policy.getEffectiveFrom().isAfter(today)) {
            policy.setEffectiveStatus("Sắp hiệu lực");
        } else if (policy.getEffectiveTo() != null && policy.getEffectiveTo().isBefore(today)) {
            policy.setEffectiveStatus("Hết hiệu lực");
        } else {
            policy.setEffectiveStatus("Đang hiệu lực");
        }
    }

    /** @return ngày nghiệp vụ theo múi giờ Việt Nam */
    private LocalDate businessToday() { return LocalDate.now(clock); }
    /** @return từ khóa đã trim hoặc chuỗi rỗng */
    private String normalizeKeyword(String value) { return value == null ? "" : value.trim(); }
    /** @return offset an toàn cho trang bắt đầu từ 1 */
    private int offset(int page) { return (Math.max(page, 1) - 1) * PAGE_SIZE; }
    /** @return tổng trang tối thiểu một */
    private int totalPages(int total) { return Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE)); }

    /** @throws PolicyValidationException khi có ít nhất một lỗi */
    private void rejectInvalid(Map<String, String> errors) throws PolicyValidationException {
        if (!errors.isEmpty()) {
            throw new PolicyValidationException(errors);
        }
    }

    /** @return ngoại lệ validation cho hành động không hợp lệ */
    private PolicyValidationException actionError(String message) {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("action", message);
        return new PolicyValidationException(errors);
    }
}
