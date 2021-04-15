package com.exeloncorp.notificationserver;

import com.google.appengine.repackaged.org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseConnection {
    private static final String connectionUrl = "jdbc:sqlserver://palaven.database.windows.net:1433;" +
            "databaseName=NotificationsDatabase;user=PalavenAdmin@palaven;password=GrandHat132;encrypt=true";
    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean SignUp(Map<String, String> params) {

        if(CheckAccountExists(params.get("exelonId"))) {
            return false;
        }

        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "INSERT INTO users (fname, lname, exelon_id, method, email, pword) values (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, params.get("firstName"));
            statement.setString(2, params.get("lastName"));
            statement.setString(3, params.get("exelonId"));
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

    public static boolean CheckAccountExists(String exelonId){
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT 1 FROM users WHERE exelon_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, exelonId);
            ResultSet rs = statement.executeQuery();

            return rs.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean Login(String exelonId, String password) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT 1 FROM users WHERE exelon_id = ? AND pword = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, exelonId);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();

            return rs.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ResultSet GetAccountHistory(String exelonId, String token) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT 1 FROM users WHERE exelon_id = ? AND token = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, exelonId);
            statement.setString(2, token);
            ResultSet rs = statement.executeQuery();

            if(rs.next()) {
                sql = "SELECT * FROM notifications WHERE exelon_id = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, exelonId);
                rs = statement.executeQuery();

                return rs;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public static ResultSet GetActiveNotifications(String notificationId) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT * FROM notifications WHERE EB_n_id = ? AND resp_outstanding = 1";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, notificationId);
            ResultSet rs = statement.executeQuery();

            return rs;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet GetUser(String exelonId) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT 1 FROM users WHERE exelon_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, exelonId);
            ResultSet rs = statement.executeQuery();

            return rs;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> GetUsers(String notificationId) {
        List<String> users = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT * FROM notifications WHERE EB_n_id = " + notificationId;
            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                users.add(rs.getString("exelon_id"));
            }

            return users;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return users;
        }
    }

    public static boolean InsertNotification(EverbridgeNotification notification) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String notificationId = notification.GetId();
            String timestamp = notification.GetTimestamp();
            String message = notification.GetMessage();
            List<String> exelonIds = notification.GetExelonIds();

            String params = "(" + notificationId + ", " + timestamp + ", ?, " + 1 +", " + message + "),".repeat(exelonIds.size());
            params = params.substring(0, params.length()-1);

            String sql = "IF NOT EXISTS (SELECT * FROM notifications WHERE EB_n_id = ?) INSERT INTO notifications (EB_n_id, t_stamp, exelon_id, resp_outstanding, msg) VALUES " + params + "";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, notificationId);
            for(int i = 0; i < exelonIds.size(); i++)
                statement.setString(i + 2, exelonIds.get(i));

            int rows = statement.executeUpdate();

            return rows != 0;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
