package dao;

import model.Author;
import java.util.List;

public interface AuthorDAO {
    List<Author> findAll() throws Exception;
    Author findById(int id) throws Exception;
}
