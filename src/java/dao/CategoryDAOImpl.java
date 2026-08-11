package dao;

import model.Category;
import utils.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl implements CategoryDAO {

    @Override
    public List<Category> findAll() throws Exception {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, description FROM categories WHERE is_deleted = 0 ORDER BY name ASC";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category c = new Category();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setDescription(rs.getString("description"));
                list.add(c);
            }
        }
        return list;
    }

    @Override
    public Category findById(int id) throws Exception {
        String sql = "SELECT id, name, description FROM categories WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category c = new Category();
                    c.setId(rs.getInt("id"));
                    c.setName(rs.getString("name"));
                    c.setDescription(rs.getString("description"));
                    return c;
                }
            }
        }
        return null;
    }
}
