package dao;

import model.Book;
import model.Author;
import java.util.List;

public interface BookDAO {
    Book findById(int id) throws Exception;
    
    List<Book> searchBooks(String keyword, String category, String sort, String order, int page, int pageSize) throws Exception;
    
    int countBooks(String keyword, String category) throws Exception;
    
    List<String> getAllCategories() throws Exception;
    
    boolean isIsbnExists(String isbn) throws Exception;
    
    boolean isIsbnExistsExcluding(String isbn, int excludeId) throws Exception;
    
    int createBook(Book book) throws Exception;
    
    boolean updateBook(Book book) throws Exception;
    
    boolean deleteBook(int id, String operator) throws Exception;
    
    List<Author> getAuthorsByBookId(int bookId) throws Exception;
    
    List<Integer> getAuthorIdsByBookId(int bookId) throws Exception;
    
    void setBookAuthors(int bookId, List<Integer> authorIds) throws Exception;
    
    boolean hasPhysicalCopies(int bookId) throws Exception;
    
    boolean hasActiveBorrowsOrReservations(int bookId) throws Exception;

    /**
     * Lấy danh sách các đầu sách mới được tạo trong vòng số ngày chỉ định.
     * @param days số ngày tính ngược từ hiện tại (ví dụ 15 ngày)
     * @param limit số lượng sách tối đa cần lấy
     * @return danh sách sách mới nhất
     * @throws Exception khi có lỗi truy vấn dữ liệu
     */
    List<Book> getLatestBooks(int days, int limit) throws Exception;
}
