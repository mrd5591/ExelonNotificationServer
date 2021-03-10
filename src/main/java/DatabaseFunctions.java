import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseFunctions {
    private final String connectionUrl = "jdbc:sqlserver://localhost:1433;" +
            "databaseName=AdventureWorks;user=MyUserName;password=*****;";
    private Connection con = null;

    public DatabaseFunctions() {
        try {
            con = DriverManager.getConnection(connectionUrl);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String
}
