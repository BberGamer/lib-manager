/** Servlet điều phối CRUD, chi tiết và bản đồ kệ cho Admin. */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;
import model.Shelf;
import model.User;
import service.*;
import utils.RoleGuard;

/** Chuyển request quản lý kệ tới ShelfService và các JSP được bảo vệ. */
@WebServlet(urlPatterns = {
    "/admin/shelf", "/admin/shelf/new", "/admin/shelf/edit", "/admin/shelf/detail",
    "/admin/shelf/map", "/admin/shelf/create", "/admin/shelf/update", "/admin/shelf/delete",
    "/librarian/shelf", "/librarian/shelf/new", "/librarian/shelf/edit", "/librarian/shelf/detail",
    "/librarian/shelf/map", "/librarian/shelf/create", "/librarian/shelf/update",
    "/librarian/shelf/delete"
})
public class ShelfManagementServlet extends HttpServlet {
    private static final Logger LOGGER=Logger.getLogger(ShelfManagementServlet.class.getName());
    private static final String LIST="/WEB-INF/views/admin/shelf-list.jsp",FORM="/WEB-INF/views/admin/shelf-form.jsp",DETAIL="/WEB-INF/views/admin/shelf-detail.jsp",MAP="/WEB-INF/views/admin/shelf-map.jsp";
    private final ShelfService service=new ShelfService();
    /** Điều phối các trang đọc sau khi yêu cầu quyền Admin. */
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse p)throws ServletException,IOException{prepare(q,p);if(!authorize(q,p))return;prepareRolePath(q);try{String path=q.getServletPath();if(path.endsWith("/shelf/new")){form(q,p,new Shelf(),"create");}else if(path.endsWith("/shelf/edit")){showEdit(q,p);}else if(path.endsWith("/shelf/detail")){showDetail(q,p);}else if(path.endsWith("/shelf/map")){showMap(q,p);}else{showList(q,p);}}catch(ShelfException e){serverError(p,e);}}
    /** Điều phối thao tác ghi và áp dụng Post/Redirect/Get. */
    @Override protected void doPost(HttpServletRequest q,HttpServletResponse p)throws ServletException,IOException{prepare(q,p);if(!authorize(q,p))return;prepareRolePath(q);try{String path=q.getServletPath();if(path.endsWith("/shelf/create")){create(q,p);}else if(path.endsWith("/shelf/update")){update(q,p);}else if(path.endsWith("/shelf/delete")){delete(q,p);}else{p.sendError(405);}}catch(ShelfException e){serverError(p,e);}}
    /** Thiết lập UTF-8 trước khi đọc tham số. */ private void prepare(HttpServletRequest q,HttpServletResponse p)throws IOException{q.setCharacterEncoding("UTF-8");p.setContentType("text/html;charset=UTF-8");}
    /** Kiểm tra đăng nhập và vai trò Admin hoặc Librarian. */ private boolean authorize(HttpServletRequest q,HttpServletResponse p)throws IOException{User u=RoleGuard.requireLogin(q,p);return u!=null&&RoleGuard.requireLibrarianOrAdmin(q,p,u);}
    /** Chuẩn bị prefix route theo vai trò đang truy cập. */ private void prepareRolePath(HttpServletRequest q){q.setAttribute("rolePath",q.getServletPath().startsWith("/librarian")?"/librarian":"/admin");}
    /** Hiển thị danh sách có lọc và phân trang. */ private void showList(HttpServletRequest q,HttpServletResponse p)throws ServletException,IOException,ShelfException{String k=trim(q.getParameter("keyword")),a=trim(q.getParameter("area")),s=trim(q.getParameter("status"));int requested=parse(q.getParameter("page"),1);int count=service.count(k,a,s),pages=Math.max(1,(int)Math.ceil((double)count/ShelfService.PAGE_SIZE)),page=Math.min(requested,pages);q.setAttribute("shelfList",service.getShelves(k,a,s,page));q.setAttribute("areas",service.getAreas());q.setAttribute("keyword",k);q.setAttribute("selectedArea",a);q.setAttribute("selectedStatus",s);q.setAttribute("totalShelves",count);q.setAttribute("totalPages",pages);q.setAttribute("currentPage",page);flash(q,"flashSuccess");flash(q,"flashError");q.getRequestDispatcher(LIST).forward(q,p);}
    /** Hiển thị form dùng chung. */ private void form(HttpServletRequest q,HttpServletResponse p,Shelf shelf,String mode)throws ServletException,IOException{q.setAttribute("shelf",shelf);q.setAttribute("formMode",mode);q.getRequestDispatcher(FORM).forward(q,p);}
    /** Tải form sửa. */ private void showEdit(HttpServletRequest q,HttpServletResponse p)throws ServletException,IOException,ShelfException{Optional<Shelf>s=require(q,p);if(s.isPresent())form(q,p,s.get(),"update");}
    /** Tải chi tiết và danh sách bản sao. */ private void showDetail(HttpServletRequest q,HttpServletResponse p)throws ServletException,IOException,ShelfException{Optional<Shelf>s=require(q,p);if(s.isPresent()){q.setAttribute("shelf",s.get());q.getRequestDispatcher(DETAIL).forward(q,p);}}
    /** Hiển thị bản đồ từ dữ liệu thật. */ private void showMap(HttpServletRequest q,HttpServletResponse p)throws ServletException,IOException,ShelfException{q.setAttribute("shelfList",service.getMap());q.getRequestDispatcher(MAP).forward(q,p);}
    /** Tạo kệ hoặc trả form với lỗi theo trường. */ private void create(HttpServletRequest q,HttpServletResponse p)throws ServletException,IOException,ShelfException{Shelf s=read(q,0);try{service.create(s,actor(q));success(q,"Thêm kệ sách thành công.");redirect(q,p);}catch(ShelfValidationException e){invalid(q,p,s,"create",e);}}
    /** Cập nhật kệ hoặc trả 404. */ private void update(HttpServletRequest q,HttpServletResponse p)throws ServletException,IOException,ShelfException{int id=parse(q.getParameter("id"),-1);if(id<1){p.sendError(400,"Mã kệ không hợp lệ.");return;}Shelf s=read(q,id);try{if(!service.update(s,actor(q))){p.sendError(404);return;}success(q,"Cập nhật kệ sách thành công.");redirect(q,p);}catch(ShelfValidationException e){invalid(q,p,s,"update",e);}}
    /** Xóa kệ trống và chuyển thông báo qua session. */ private void delete(HttpServletRequest q,HttpServletResponse p)throws IOException,ShelfException{int id=parse(q.getParameter("id"),-1);if(id<1){p.sendError(400,"Mã kệ không hợp lệ.");return;}try{boolean ok=service.delete(id,actor(q));q.getSession(false).setAttribute(ok?"flashSuccess":"flashError",ok?"Xóa kệ sách thành công.":"Không tìm thấy kệ sách.");}catch(ShelfValidationException e){q.getSession(false).setAttribute("flashError",e.getValidationErrors().get("delete"));}redirect(q,p);}
    /** Đọc biểu mẫu thành model, parse số sai thành giá trị validation không hợp lệ. */ private Shelf read(HttpServletRequest q,int id){return new Shelf(id,q.getParameter("code"),q.getParameter("name"),q.getParameter("area"),parse(q.getParameter("floorNumber"),0),parse(q.getParameter("capacity"),0),q.getParameter("description"),q.getParameter("status"));}
    /** Yêu cầu id hợp lệ và bản ghi tồn tại. */ private Optional<Shelf> require(HttpServletRequest q,HttpServletResponse p)throws IOException,ShelfException{int id=parse(q.getParameter("id"),-1);if(id<1){p.sendError(400,"Mã kệ không hợp lệ.");return Optional.empty();}Optional<Shelf>s=service.find(id);if(s.isEmpty())p.sendError(404);return s;}
    /** Forward form lỗi với HTTP 400. */ private void invalid(HttpServletRequest q,HttpServletResponse p,Shelf s,String mode,ShelfValidationException e)throws ServletException,IOException{p.setStatus(400);q.setAttribute("validationErrors",e.getValidationErrors());form(q,p,s,mode);}
    /** Ghi flash thành công. */ private void success(HttpServletRequest q,String value){q.getSession(false).setAttribute("flashSuccess",value);}
    /** Chuyển flash sang request rồi xóa. */ private void flash(HttpServletRequest q,String key){Object value=q.getSession(false).getAttribute(key);if(value!=null){q.setAttribute(key,value);q.getSession(false).removeAttribute(key);}}
    /** Lấy actor đã xác thực. */ private String actor(HttpServletRequest q){String value=RoleGuard.getLoggedUser(q).getUsername();return value.length()>50?value.substring(0,50):value;}
    /** Redirect về danh sách đúng namespace vai trò. */ private void redirect(HttpServletRequest q,HttpServletResponse p)throws IOException{p.sendRedirect(q.getContextPath()+q.getAttribute("rolePath")+"/shelf");}
    /** Parse số nguyên có fallback. */ private int parse(String v,int fallback){try{return Integer.parseInt(v);}catch(Exception e){return fallback;}}
    /** Trim chuỗi nullable. */ private String trim(String v){return v==null?"":v.trim();}
    /** Log lỗi tại biên HTTP và không lộ nội dung nội bộ. */ private void serverError(HttpServletResponse p,ShelfException e)throws IOException{LOGGER.log(Level.SEVERE,e.getMessage(),e);p.sendError(500,"Không thể xử lý yêu cầu kệ sách vào lúc này.");}
}
