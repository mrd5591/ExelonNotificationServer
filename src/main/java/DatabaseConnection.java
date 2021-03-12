import java.sql.*;
import java.util.Map;

public class DatabaseConnection {
    private static final String connectionUrl = "jdbc:virmire.database.windows.net;" +
            "databaseName=Notification Database;user=VirmireAdmin;password=GrandHat132;";

    public static boolean SignUp(Map<String, String> params) {
        if(CheckAccountExists(Integer.parseInt(params.get("exelonId")))) {
            return false;
        }

        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "INSERT INTO users (fname, lname, exelon_id, method, email, pword) values (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, params.get("firstName"));
            statement.setString(2, params.get("lastName"));
            statement.setInt(3, Integer.parseInt(params.get("exelonId")));
            statement.setString(4, params.get("os"));
            statement.setString(5, params.get("email"));
            statement.setString(6, params.get("password"));
            statement.executeUpdate();

            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean CheckAccountExists(int exelonId){
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT 1 FROM users WHERE exelon_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, exelonId);
            ResultSet rs = statement.executeQuery();

            return rs.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean Login(int exelonId, String password) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT 1 FROM users WHERE exelon_id = ? AND pword = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, exelonId);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();

            return rs.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
