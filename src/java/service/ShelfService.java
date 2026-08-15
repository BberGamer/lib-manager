/** Service quản lý validation, sức chứa và điều kiện xóa kệ. */
package service;

import dao.ShelfDao;
import java.sql.SQLException;
import java.util.*;
import model.Shelf;

/** Điều phối ShelfDao theo quy tắc nghiệp vụ của kho sách. */
public class ShelfService {
    public static final int PAGE_SIZE=10;
    private final ShelfDao dao;
    /** Khởi tạo bằng DAO mặc định. */ public ShelfService(){this(new ShelfDao());}
    /** @param dao DAO dùng để lưu dữ liệu */ public ShelfService(ShelfDao dao){this.dao=dao;}
    /** Lấy trang kết quả đã chuẩn hóa. */ public List<Shelf> getShelves(String k,String a,String s,int page)throws ShelfException{try{return dao.findAll(trim(k),trim(a),trim(s),(Math.max(1,page)-1)*PAGE_SIZE,PAGE_SIZE);}catch(SQLException|ClassNotFoundException e){throw new ShelfException("Không thể tải kệ",e);}}
    /** Đếm kết quả. */ public int count(String k,String a,String s)throws ShelfException{try{return dao.count(trim(k),trim(a),trim(s));}catch(SQLException|ClassNotFoundException e){throw new ShelfException("Không thể đếm kệ",e);}}
    /** Lấy khu vực lọc. */ public List<String> getAreas()throws ShelfException{try{return dao.findAreas();}catch(SQLException|ClassNotFoundException e){throw new ShelfException("Không thể tải khu vực",e);}}
    /** Lấy dữ liệu bản đồ. */ public List<Shelf> getMap()throws ShelfException{try{return dao.findMap();}catch(SQLException|ClassNotFoundException e){throw new ShelfException("Không thể tải bản đồ kệ",e);}}
    /** Tìm chi tiết. */ public Optional<Shelf> find(int id)throws ShelfException{try{return dao.findById(id);}catch(SQLException|ClassNotFoundException e){throw new ShelfException("Không thể tải chi tiết kệ",e);}}
    /** Tạo kệ sau validation. */ public Shelf create(Shelf s,String actor)throws ShelfException,ShelfValidationException{normalize(s);validate(s,0);try{return dao.insert(s,actor);}catch(SQLException|ClassNotFoundException e){throw new ShelfException("Không thể tạo kệ",e);}}
    /** Cập nhật kệ và bảo đảm sức chứa không nhỏ hơn số sách. */ public boolean update(Shelf s,String actor)throws ShelfException,ShelfValidationException{normalize(s);Optional<Shelf> old=find(s.getId());if(old.isEmpty())return false;validate(s,s.getId());Map<String,String> errors=new LinkedHashMap<>();if(s.getCapacity()<old.get().getBookCount())errors.put("capacity","Sức chứa không được nhỏ hơn số sách hiện có.");reject(errors);try{return dao.update(s,old.get().getCode(),actor);}catch(SQLException|ClassNotFoundException e){throw new ShelfException("Không thể cập nhật kệ",e);}}
    /** Xóa mềm kệ khi không còn bản sao. */ public boolean delete(int id,String actor)throws ShelfException,ShelfValidationException{Optional<Shelf> shelf=find(id);if(shelf.isEmpty())return false;try{rejectDeleteWhenOccupied(shelf.get().getCode());boolean deleted=dao.delete(id,actor);if(!deleted){rejectDeleteWhenOccupied(shelf.get().getCode());}return deleted;}catch(SQLException|ClassNotFoundException e){throw new ShelfException("Không thể xóa kệ",e);}}
    /** Chặn xóa khi còn ít nhất một BookCopy chưa xóa mềm tham chiếu mã kệ. */ private void rejectDeleteWhenOccupied(String code)throws SQLException,ClassNotFoundException,ShelfValidationException{if(dao.hasCopies(code)){Map<String,String> errors=new LinkedHashMap<>();errors.put("delete","Không thể xóa kệ vì vẫn còn sách đang được lưu tại kệ này.");throw new ShelfValidationException(errors);}}
    private void validate(Shelf s,int excluded)throws ShelfException,ShelfValidationException{Map<String,String> e=new LinkedHashMap<>();if(s.getCode()==null||s.getCode().isEmpty())e.put("code","Mã kệ không được để trống.");else if(s.getCode().length()>20)e.put("code","Mã kệ không vượt quá 20 ký tự.");if(s.getName()==null||s.getName().isEmpty())e.put("name","Tên kệ không được để trống.");else if(s.getName().length()>100)e.put("name","Tên kệ không vượt quá 100 ký tự.");if(s.getArea()==null||s.getArea().isEmpty())e.put("area","Khu vực không được để trống.");if(s.getFloorNumber()<=0||s.getFloorNumber()>100)e.put("floorNumber","Tầng phải nằm trong khoảng từ 1 đến 100.");if(s.getCapacity()<=0)e.put("capacity","Sức chứa phải lớn hơn 0.");if(!"ACTIVE".equals(s.getStatus())&&!"INACTIVE".equals(s.getStatus()))e.put("status","Trạng thái không hợp lệ.");try{if(!e.containsKey("code")&&dao.existsCode(s.getCode(),excluded))e.put("code","Mã kệ đã tồn tại.");}catch(SQLException|ClassNotFoundException x){throw new ShelfException("Không thể kiểm tra mã kệ",x);}reject(e);}
    private void reject(Map<String,String> e)throws ShelfValidationException{if(!e.isEmpty())throw new ShelfValidationException(e);}
    private void normalize(Shelf s){s.setCode(trim(s.getCode()).toUpperCase(Locale.ROOT));s.setName(trim(s.getName()));s.setArea(trim(s.getArea()));s.setDescription(trim(s.getDescription()));s.setStatus(trim(s.getStatus()));if(s.getDescription()!=null&&s.getDescription().isEmpty())s.setDescription(null);}
    private String trim(String v){return v==null?"":v.trim();}
}
