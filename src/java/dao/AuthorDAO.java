/**
 * Hợp đồng tầng DAO cho dữ liệu tác giả và quan hệ tác giả với sách.
 */
package dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import model.Author;

/**
 * Định nghĩa các thao tác đọc, tìm kiếm, ghi và kiểm tra ràng buộc của tác giả.
 */
public interface AuthorDAO {

    /** @return toàn bộ tác giả đang hoạt động theo tên */
    List<Author> findAll() throws SQLException, ClassNotFoundException;

    /**
     * Tìm tác giả theo mã.
     * @param id mã tác giả
     * @return tác giả nếu còn hoạt động
     */
    Optional<Author> findById(int id) throws SQLException, ClassNotFoundException;

    /**
     * Tìm một trang tác giả.
     * @param keyword từ khóa tìm trong tên tác giả
     * @param sort trường sắp xếp đã được whitelist
     * @param order chiều sắp xếp đã được whitelist
     * @param offset vị trí bắt đầu
     * @param limit số bản ghi tối đa
     * @return danh sách phù hợp
     */
    List<Author> search(String keyword, String sort, String order, int offset, int limit)
            throws SQLException, ClassNotFoundException;

    /** @param keyword từ khóa tìm trong tên tác giả @return số tác giả phù hợp */
    int count(String keyword) throws SQLException, ClassNotFoundException;

    /** @param name tên tác giả @param excludedId mã bỏ qua @return true nếu tên đang tồn tại */
    boolean existsByName(String name, int excludedId) throws SQLException, ClassNotFoundException;

    /** @param author tác giả hợp lệ @param actor tài khoản thao tác @return tác giả đã lưu */
    Author insert(Author author, String actor) throws SQLException, ClassNotFoundException;

    /**
     * Khôi phục tác giả đã xóa mềm cùng tên và thay thế bằng dữ liệu vừa nhập.
     * @param author dữ liệu tác giả đã được service kiểm tra
     * @param actor tài khoản quản trị thực hiện thao tác
     * @return tác giả đã khôi phục hoặc rỗng nếu không có bản ghi phù hợp
     * @throws SQLException khi cập nhật hoặc đọc dữ liệu thất bại
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    Optional<Author> restoreDeleted(Author author, String actor)
            throws SQLException, ClassNotFoundException;

    /** @param author tác giả hợp lệ @param actor tài khoản thao tác @return true nếu cập nhật thành công */
    boolean update(Author author, String actor) throws SQLException, ClassNotFoundException;

    /** @param authorId mã tác giả @return true nếu còn liên kết với sách */
    boolean hasBooks(int authorId) throws SQLException, ClassNotFoundException;

    /**
     * Xóa mềm tác giả đang hoạt động và lưu thông tin kiểm toán của thao tác.
     * @param id mã tác giả
     * @param actor tài khoản quản trị thực hiện thao tác
     * @return true nếu xóa thành công
     * @throws SQLException khi cập nhật dữ liệu thất bại
     * @throws ClassNotFoundException khi không tải được JDBC driver
     */
    boolean deleteById(int id, String actor) throws SQLException, ClassNotFoundException;
}
