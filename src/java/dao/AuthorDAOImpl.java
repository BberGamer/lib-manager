package dao;

import model.Author;
import utils.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorDAOImpl implements AuthorDAO {

    @Override
    public List<Author> findAll() throws Exception {
        List<Author> list = new ArrayList<>();
        String sql = "SELECT id, name, nationality, birth_date, bio, avatar_url FROM authors WHERE is_deleted = 0 ORDER BY name ASC";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Author a = new Author();
                a.setId(rs.getInt("id"));
                a.setName(rs.getString("name"));
                a.setNationality(rs.getString("nationality"));
                Date bd = rs.getDate("birth_date");
                a.setBirthDate(bd != null ? bd.toLocalDate() : null);
                a.setBio(rs.getString("bio"));
                a.setAvatarUrl(rs.getString("avatar_url"));
                list.add(a);
            }
        }
        return list;
    }

    @Override
    public Author findById(int id) throws Exception {
        String sql = "SELECT id, name, nationality, birth_date, bio, avatar_url FROM authors WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Author a = new Author();
                    a.setId(rs.getInt("id"));
                    a.setName(rs.getString("name"));
                    a.setNationality(rs.getString("nationality"));
                    Date bd = rs.getDate("birth_date");
                    a.setBirthDate(bd != null ? bd.toLocalDate() : null);
                    a.setBio(rs.getString("bio"));
                    a.setAvatarUrl(rs.getString("avatar_url"));
                    return a;
                }
            }
        }
        return null;
    }
}
