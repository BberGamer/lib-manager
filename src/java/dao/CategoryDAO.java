package dao;

import model.Category;
import java.util.List;

public interface CategoryDAO {
    List<Category> findAll() throws Exception;
    Category findById(int id) throws Exception;
}
