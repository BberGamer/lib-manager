/* Service quản lý điều kiện đặt trước, hàng chờ và trạng thái reservation. */
package service;

import dao.BookDAO;
import dao.BookDAOImpl;
import dao.BorrowRecordDAO;
import dao.ReservationDAO;
import java.util.List;
import model.Book;
import model.ReservationRecord;

/** Điều phối nghiệp vụ đặt trước và giữ controller độc lập với DAO. */
public class ReservationService {
    public static final int READY_HOLD_HOURS=24;
    private final ReservationDAO reservationDao=new ReservationDAO();
    private final BookDAO bookDao=new BookDAOImpl();
    private final BorrowRecordDAO borrowRecordDao=new BorrowRecordDAO();

    /** Lấy thông tin xác nhận đặt trước sau khi kiểm tra các điều kiện đọc. */
    public CreationInfo getCreationInfo(int userId,int bookId)throws Exception{
        expireExpiredReadyReservations();
        Book book=bookDao.findById(bookId);if(book==null||book.getAvailable()>0)return null;
        if(reservationDao.findActive(userId,bookId)!=null)return null;
        int waiting=reservationDao.countActiveByBook(bookId);
        return new CreationInfo(book,waiting,waiting+1);
    }
    /** Tạo reservation; DAO kiểm tra lại mọi điều kiện trong transaction. */
    public boolean createReservation(int userId,int bookId)throws Exception{
        expireExpiredReadyReservations();
        return userId>0&&bookId>0&&reservationDao.createWaiting(userId,bookId);
    }
    /** Lấy lịch sử và hàng chờ thuộc user. */
    public List<ReservationRecord> getMyReservations(int userId)throws Exception{
        expireExpiredReadyReservations();
        return reservationDao.findByUserId(userId);
    }
    /** Hủy reservation active thuộc user. */
    public boolean cancelReservation(int reservationId,int userId)throws Exception{
        return reservationId>0&&reservationDao.cancelOwned(reservationId,userId);
    }
    /** Kích hoạt người chờ đầu tiên khi sách vừa có bản sao khả dụng. */
    public boolean activateNextReservation(int bookId)throws Exception{return reservationDao.activateNext(bookId,READY_HOLD_HOURS);}

    /**
     * Đóng đồng thời các lượt chờ nhận và reservation đã quá hạn giữ.
     *
     * @return số lượt chờ nhận đã hết hạn
     * @throws Exception khi tầng lưu trữ không thể cập nhật dữ liệu
     */
    public int expireExpiredReadyReservations() throws Exception {
        return borrowRecordDao.expirePendingRequests();
    }

    /** DTO thông tin trang xác nhận. */
    public static final class CreationInfo{
        private final Book book;private final int waitingCount;private final int expectedPosition;
        /** Tạo dữ liệu xác nhận. */
        public CreationInfo(Book book,int waitingCount,int expectedPosition){this.book=book;this.waitingCount=waitingCount;this.expectedPosition=expectedPosition;}
        /** @return sách đặt trước */ public Book getBook(){return book;}
        /** @return số người đang chờ */ public int getWaitingCount(){return waitingCount;}
        /** @return vị trí dự kiến */ public int getExpectedPosition(){return expectedPosition;}
    }
}
