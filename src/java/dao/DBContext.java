package dao;
    

    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.SQLException;
    import java.util.ArrayList;
    import java.util.List;

    public class DBContext {

        private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
        private static final String URL = "jdbc:mysql://localhost:3306/library_management_swp391"
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        private static final String USER = "root";
        private static final String PASS = "haihbvn12345"; // <-- ?i?n password MySQL c?a b?n

        private static DBContext instance;
        protected Connection connection;

        private DBContext() throws ClassNotFoundException, SQLException {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(URL, USER, PASS);
        }

        public static DBContext getInstance() throws ClassNotFoundException, SQLException {
            if (instance == null || instance.connection.isClosed()) {
                instance = new DBContext();
            }
            return instance;
        }

        public Connection getConnection() {
            return connection;
        }

        public void closeConnection() {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static void main(String[] args) {
            try {
                Connection conn = DBContext.getInstance().getConnection();
                if (conn != null && !conn.isClosed()) {
                    System.out.println("Ket noi MySQL thanh cong!");
                    System.out.println("URL: " + URL);
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Khong tim thay JDBC Driver: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Ket noi that bai: " + e.getMessage());
            }
        }
    }
