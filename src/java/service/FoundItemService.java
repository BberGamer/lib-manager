/**
 * Service quản lý thao tác tiếp nhận và tra cứu đồ để quên.
 * Lớp thuộc tầng service, sở hữu validation nghiệp vụ trước khi gọi FoundItemDao.
 */
package service;

import dao.FoundItemDao;
import dao.FoundItemClaimDao;
import exception.FoundItemException;
import exception.FoundItemValidationException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import model.FoundItem;
import model.FoundItemClaim;
import model.FoundItemClaimStatus;
import model.FoundItemStatus;

/**
 * Điều phối việc tạo và tra cứu đồ để quên, không phụ thuộc HTTP hay JSP.
 */
public class FoundItemService {

    public static final int PAGE_SIZE = 10;
    private static final int ITEM_NAME_MAX_LENGTH = 150;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;
    private static final int CLAIM_NOTE_MAX_LENGTH = 1000;
    private final FoundItemDao foundItemDao;
    private final FoundItemClaimDao foundItemClaimDao;

    /**
     * Khởi tạo service với DAO mặc định của module.
     */
    public FoundItemService() {
        this(new FoundItemDao());
    }

    /**
     * Khởi tạo service với DAO để hỗ trợ kiểm thử và tái sử dụng.
     *
     * @param foundItemDao DAO quản lý dữ liệu đồ để quên
     */
    public FoundItemService(FoundItemDao foundItemDao) {
        this.foundItemDao = foundItemDao;
        this.foundItemClaimDao = new FoundItemClaimDao();
    }

    /**
     * Lấy một trang đồ để quên theo điều kiện tìm kiếm.
     *
     * @param keyword từ khóa tìm kiếm
     * @param status trạng thái lọc, có thể null
     * @param page trang cần lấy, bắt đầu từ 1
     * @return danh sách đồ vật của trang
     * @throws FoundItemException khi không truy vấn được cơ sở dữ liệu
     */
    public List<FoundItem> getFoundItems(String keyword, FoundItemStatus status, int page)
            throws FoundItemException {
        try {
            int normalizedPage = Math.max(1, page);
            return foundItemDao.findAll(normalize(keyword), status,
                    (normalizedPage - 1) * PAGE_SIZE, PAGE_SIZE);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new FoundItemException("Không thể tải danh sách đồ để quên.", exception);
        }
    }

    /**
     * Đếm đồ để quên theo điều kiện tìm kiếm.
     *
     * @param keyword từ khóa tìm kiếm
     * @param status trạng thái lọc, có thể null
     * @return tổng số đồ vật phù hợp
     * @throws FoundItemException khi không truy vấn được cơ sở dữ liệu
     */
    public int countFoundItems(String keyword, FoundItemStatus status) throws FoundItemException {
        try {
            return foundItemDao.count(normalize(keyword), status);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new FoundItemException("Không thể đếm đồ để quên.", exception);
        }
    }

    /**
     * Tính số trang tối thiểu là một để giao diện phân trang ổn định.
     *
     * @param keyword từ khóa tìm kiếm
     * @param status trạng thái lọc, có thể null
     * @return tổng số trang
     * @throws FoundItemException khi không truy vấn được cơ sở dữ liệu
     */
    public int getTotalPages(String keyword, FoundItemStatus status) throws FoundItemException {
        return Math.max(1, (int) Math.ceil((double) countFoundItems(keyword, status) / PAGE_SIZE));
    }

    /**
     * Tìm chi tiết một đồ để quên.
     *
     * @param id mã đồ vật
     * @return đồ vật nếu tồn tại
     * @throws FoundItemException khi không truy vấn được cơ sở dữ liệu
     */
    public Optional<FoundItem> findFoundItem(int id) throws FoundItemException {
        try {
            return foundItemDao.findById(id);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new FoundItemException("Không thể tải chi tiết đồ để quên.", exception);
        }
    }

    /**
     * Tiếp nhận một đồ để quên mới ở trạng thái AVAILABLE.
     *
     * @param item dữ liệu nhập từ form
     * @param actorUserId mã nhân viên đang tiếp nhận
     * @return đồ vật đã được lưu
     * @throws FoundItemValidationException khi dữ liệu không hợp lệ
     * @throws FoundItemException khi không thể lưu dữ liệu
     */
    public FoundItem createFoundItem(FoundItem item, int actorUserId)
            throws FoundItemValidationException, FoundItemException {
        validateForCreate(item, actorUserId);
        item.setStatus(FoundItemStatus.AVAILABLE);
        try {
            return foundItemDao.insert(item, actorUserId);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new FoundItemException("Không thể tiếp nhận đồ để quên.", exception);
        }
    }

    /**
     * Chuẩn hóa và kiểm tra dữ liệu tiếp nhận trước các bước kỹ thuật như tải ảnh.
     *
     * @param item dữ liệu đồ để quên từ form
     * @param actorUserId mã nhân viên tiếp nhận
     * @throws FoundItemValidationException khi dữ liệu không hợp lệ
     */
    public void validateForCreate(FoundItem item, int actorUserId) throws FoundItemValidationException {
        normalizeItem(item);
        validate(item, actorUserId);
    }

    /**
     * Ghi yêu cầu nhận lại của Reader và khóa đồ ở trạng thái chờ xác minh.
     *
     * @param itemId mã đồ Reader muốn nhận lại
     * @param readerUserId mã tài khoản Reader lấy từ session
     * @param claimNote đặc điểm dùng để xác minh quyền sở hữu
     * @throws FoundItemValidationException khi dữ liệu hoặc trạng thái không hợp lệ
     * @throws FoundItemException khi không thể hoàn tất giao dịch
     */
    public void submitClaim(int itemId, int readerUserId, String claimNote)
            throws FoundItemValidationException, FoundItemException {
        String normalizedNote = normalize(claimNote);
        Map<String, String> errors = new LinkedHashMap<>();
        if (itemId <= 0) {
            errors.put("general", "Đồ để quên không hợp lệ.");
        }
        if (readerUserId <= 0) {
            errors.put("general", "Không xác định được tài khoản nhận đồ.");
        }
        if (normalizedNote.isEmpty()) {
            errors.put("claimNote", "Vui lòng mô tả đặc điểm để Thủ thư xác minh.");
        } else if (normalizedNote.length() > CLAIM_NOTE_MAX_LENGTH) {
            errors.put("claimNote", "Mô tả xác minh không được vượt quá 1000 ký tự.");
        }
        if (!errors.isEmpty()) {
            throw new FoundItemValidationException(errors);
        }

        try (Connection connection = foundItemDao.openTransactionConnection()) {
            connection.setAutoCommit(false);
            try {
                FoundItem item = foundItemDao.findByIdForUpdate(connection, itemId).orElse(null);
                if (item == null) {
                    throw new FoundItemValidationException(Map.of("general", "Không tìm thấy đồ để quên."));
                }
                if (item.getStatus() != FoundItemStatus.AVAILABLE) {
                    throw new FoundItemValidationException(Map.of("general",
                            "Đồ này đang được xác minh hoặc đã trả cho người nhận."));
                }
                FoundItemClaim claim = new FoundItemClaim();
                claim.setItemId(itemId);
                claim.setUserId(readerUserId);
                claim.setClaimNote(normalizedNote);
                foundItemClaimDao.insertPending(connection, claim);
                foundItemDao.updateStatus(connection, itemId, FoundItemStatus.CLAIM_PENDING);
                connection.commit();
            } catch (FoundItemValidationException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new FoundItemException("Không thể gửi yêu cầu nhận lại đồ.", exception);
            }
        } catch (FoundItemValidationException exception) {
            throw exception;
        } catch (SQLException | ClassNotFoundException exception) {
            throw new FoundItemException("Không thể gửi yêu cầu nhận lại đồ.", exception);
        }
    }

    /**
     * Lấy yêu cầu Reader đang chờ xác minh cho một đồ để quên.
     *
     * @param itemId mã đồ để quên
     * @return yêu cầu pending nếu có
     * @throws FoundItemException khi không thể truy vấn dữ liệu
     */
    public Optional<FoundItemClaim> findPendingClaim(int itemId) throws FoundItemException {
        try {
            return foundItemClaimDao.findPendingByItemId(itemId);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new FoundItemException("Không thể tải yêu cầu nhận lại đồ.", exception);
        }
    }

    /**
     * Lấy yêu cầu mới nhất của đồ để hiển thị bước bàn giao hiện tại cho Thủ thư.
     *
     * @param itemId mã đồ để quên
     * @return yêu cầu gần nhất nếu có
     * @throws FoundItemException khi không thể truy vấn dữ liệu
     */
    public Optional<FoundItemClaim> findLatestClaim(int itemId) throws FoundItemException {
        try {
            return foundItemClaimDao.findLatestByItemId(itemId);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new FoundItemException("Không thể tải trạng thái yêu cầu nhận đồ.", exception);
        }
    }

    /**
     * Lấy yêu cầu chưa hoàn tất của Reader để hiển thị trong khu vực cá nhân.
     *
     * @param readerUserId mã Reader đăng nhập
     * @return danh sách yêu cầu đang xử lý
     * @throws FoundItemException khi không thể truy vấn dữ liệu
     */
    public List<FoundItemClaim> getOpenClaimsForReader(int readerUserId) throws FoundItemException {
        try {
            return foundItemClaimDao.findOpenByUserId(readerUserId);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new FoundItemException("Không thể tải yêu cầu nhận đồ của bạn.", exception);
        }
    }

    /**
     * Lấy lịch sử đầy đủ yêu cầu nhận đồ của Reader.
     *
     * @param readerUserId mã Reader đăng nhập
     * @return toàn bộ lịch sử yêu cầu
     * @throws FoundItemException khi không thể truy vấn dữ liệu
     */
    public List<FoundItemClaim> getClaimHistoryForReader(int readerUserId) throws FoundItemException {
        try {
            return foundItemClaimDao.findAllByUserId(readerUserId);
        } catch (SQLException | ClassNotFoundException exception) {
            throw new FoundItemException("Không thể tải lịch sử nhận đồ của bạn.", exception);
        }
    }

    /**
     * Reader xác nhận đã nhận đồ sau khi đã gặp Thủ thư tại quầy.
     *
     * @param claimId mã yêu cầu
     * @param readerUserId mã Reader từ session
     * @throws FoundItemValidationException khi yêu cầu không thuộc Reader hoặc sai trạng thái
     * @throws FoundItemException khi giao dịch thất bại
     */
    public void confirmReaderPickup(int claimId, int readerUserId)
            throws FoundItemValidationException, FoundItemException {
        transitionClaim(claimId, readerUserId, FoundItemClaimStatus.APPROVED,
                FoundItemClaimStatus.READER_CONFIRMED, false);
    }

    /**
     * Thủ thư xác nhận đã giao đồ, đóng yêu cầu và đánh dấu đồ đã trả.
     *
     * @param claimId mã yêu cầu
     * @param staffUserId mã Thủ thư từ session
     * @throws FoundItemValidationException khi yêu cầu chưa được Reader xác nhận
     * @throws FoundItemException khi giao dịch thất bại
     */
    public void completeHandover(int claimId, int staffUserId)
            throws FoundItemValidationException, FoundItemException {
        transitionClaim(claimId, staffUserId, FoundItemClaimStatus.READER_CONFIRMED,
                FoundItemClaimStatus.COMPLETED, true);
    }

    /**
     * Chuyển yêu cầu sang bước tiếp theo và cập nhật đồ khi Thủ thư hoàn tất giao.
     *
     * @param claimId mã yêu cầu
     * @param actorUserId mã người thực hiện
     * @param expectedStatus trạng thái trước khi chuyển
     * @param targetStatus trạng thái sau khi chuyển
     * @param isStaffAction true khi thao tác là của Thủ thư
     * @throws FoundItemValidationException khi ownership hoặc trạng thái sai
     * @throws FoundItemException khi giao dịch thất bại
     */
    private void transitionClaim(int claimId, int actorUserId, FoundItemClaimStatus expectedStatus,
            FoundItemClaimStatus targetStatus, boolean isStaffAction)
            throws FoundItemValidationException, FoundItemException {
        if (claimId <= 0 || actorUserId <= 0) {
            throw new FoundItemValidationException(Map.of("general", "Yêu cầu nhận đồ không hợp lệ."));
        }
        try (Connection connection = foundItemDao.openTransactionConnection()) {
            connection.setAutoCommit(false);
            try {
                FoundItemClaim claim = foundItemClaimDao.findByIdForUpdate(connection, claimId).orElse(null);
                if (claim == null || claim.getStatus() != expectedStatus
                        || (!isStaffAction && claim.getUserId() != actorUserId)) {
                    throw new FoundItemValidationException(Map.of("general", "Yêu cầu không ở đúng bước để xác nhận."));
                }
                FoundItem item = foundItemDao.findByIdForUpdate(connection, claim.getItemId()).orElse(null);
                if (item == null || item.getStatus() != FoundItemStatus.CLAIM_PENDING) {
                    throw new FoundItemValidationException(Map.of("general", "Đồ để quên không còn chờ bàn giao."));
                }
                foundItemClaimDao.transitionStatus(connection, claimId, expectedStatus, targetStatus,
                        isStaffAction ? actorUserId : null);
                if (targetStatus == FoundItemClaimStatus.COMPLETED) {
                    foundItemDao.updateStatus(connection, item.getId(), FoundItemStatus.RETURNED);
                }
                connection.commit();
            } catch (FoundItemValidationException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new FoundItemException("Không thể xác nhận bàn giao đồ.", exception);
            }
        } catch (FoundItemValidationException exception) {
            throw exception;
        } catch (SQLException | ClassNotFoundException exception) {
            throw new FoundItemException("Không thể xác nhận bàn giao đồ.", exception);
        }
    }

    /**
     * Xác minh yêu cầu nhận đồ; khi từ chối thì đưa đồ về trạng thái có thể nhận.
     *
     * @param claimId mã yêu cầu cần xử lý
     * @param staffUserId mã Thủ thư đang xử lý
     * @param isApproved true để chấp nhận, false để từ chối
     * @throws FoundItemValidationException khi yêu cầu không còn hợp lệ để xử lý
     * @throws FoundItemException khi không thể hoàn tất giao dịch
     */
    public void reviewClaim(int claimId, int staffUserId, boolean isApproved)
            throws FoundItemValidationException, FoundItemException {
        if (claimId <= 0 || staffUserId <= 0) {
            throw new FoundItemValidationException(Map.of("general", "Yêu cầu xác minh không hợp lệ."));
        }
        try (Connection connection = foundItemDao.openTransactionConnection()) {
            connection.setAutoCommit(false);
            try {
                FoundItemClaim claim = foundItemClaimDao.findPendingByIdForUpdate(connection, claimId).orElse(null);
                if (claim == null) {
                    throw new FoundItemValidationException(Map.of("general",
                            "Yêu cầu này đã được xử lý hoặc không tồn tại."));
                }
                FoundItem item = foundItemDao.findByIdForUpdate(connection, claim.getItemId()).orElse(null);
                if (item == null || item.getStatus() != FoundItemStatus.CLAIM_PENDING) {
                    throw new FoundItemValidationException(Map.of("general",
                            "Đồ để quên không còn ở trạng thái chờ xác minh."));
                }
                FoundItemClaimStatus decision = isApproved
                        ? FoundItemClaimStatus.APPROVED : FoundItemClaimStatus.REJECTED;
                foundItemClaimDao.updateDecision(connection, claimId, staffUserId, decision);
                if (!isApproved) {
                    foundItemDao.updateStatus(connection, item.getId(), FoundItemStatus.AVAILABLE);
                }
                connection.commit();
            } catch (FoundItemValidationException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new FoundItemException("Không thể xử lý yêu cầu xác minh.", exception);
            }
        } catch (FoundItemValidationException exception) {
            throw exception;
        } catch (SQLException | ClassNotFoundException exception) {
            throw new FoundItemException("Không thể xử lý yêu cầu xác minh.", exception);
        }
    }

    /**
     * Chuẩn hóa text để không lưu khoảng trắng thừa.
     *
     * @param item đồ vật cần chuẩn hóa
     */
    private void normalizeItem(FoundItem item) {
        item.setItemName(normalize(item.getItemName()));
        String description = normalize(item.getDescription());
        item.setDescription(description.isEmpty() ? null : description);
    }

    /**
     * Kiểm tra dữ liệu tiếp nhận trước khi ghi cơ sở dữ liệu.
     *
     * @param item đồ vật đã chuẩn hóa
     * @param actorUserId mã người tiếp nhận
     * @throws FoundItemValidationException khi có ít nhất một trường không hợp lệ
     */
    private void validate(FoundItem item, int actorUserId) throws FoundItemValidationException {
        Map<String, String> errors = new LinkedHashMap<>();
        if (item.getItemName().isEmpty()) {
            errors.put("itemName", "Tên đồ vật không được để trống.");
        } else if (item.getItemName().length() > ITEM_NAME_MAX_LENGTH) {
            errors.put("itemName", "Tên đồ vật không được vượt quá 150 ký tự.");
        }
        if (item.getDescription() != null && item.getDescription().length() > DESCRIPTION_MAX_LENGTH) {
            errors.put("description", "Mô tả không được vượt quá 2000 ký tự.");
        }
        if (item.getFoundDate() == null) {
            errors.put("foundDate", "Vui lòng chọn ngày tìm thấy đồ vật.");
        } else if (item.getFoundDate().isAfter(LocalDate.now())) {
            errors.put("foundDate", "Ngày tìm thấy không được ở tương lai.");
        }
        if (actorUserId <= 0) {
            errors.put("general", "Không xác định được nhân viên tiếp nhận.");
        }
        if (!errors.isEmpty()) {
            throw new FoundItemValidationException(errors);
        }
    }

    /**
     * Chuẩn hóa chuỗi nullable về chuỗi đã trim.
     *
     * @param value chuỗi đầu vào có thể null
     * @return chuỗi không null
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
