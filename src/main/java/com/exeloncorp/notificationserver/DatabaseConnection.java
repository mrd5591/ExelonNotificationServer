package com.exeloncorp.notificationserver;

import com.google.appengine.repackaged.com.google.gson.JsonArray;
import com.google.appengine.repackaged.com.google.gson.JsonObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
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

    public static int SignUp(Map<String, String> params) {

        if(CheckAccountExists(params.get("exelonId"))) {
            return 1;
        }

        try (Connection connection = DriverManager.getConnection(connectionUrl)) {
            String sql = "INSERT INTO users (fname, lname, exelon_id, method, email, pword) values (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, params.get("firstName"));
            statement.setString(2, params.get("lastName"));
            statement.setString(3, params.get("exelonId"));
            statement.setString(4, params.get("os"));
            statement.setString(5, params.get("email"));
            statement.setString(6, params.get("password"));
            statement.executeUpdate();

            return 0;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return 2;
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

    public static String Login(String exelonId) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT TOP 1 pword FROM users WHERE exelon_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, exelonId);
            ResultSet rs = statement.executeQuery();

            if(rs.next())
                return rs.getString("pword");
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public static JsonArray GetAccountHistory(String exelonId, String token) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT TOP 1 * FROM users WHERE exelon_id = ? AND login_token = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, exelonId);
            statement.setString(2, token);
            ResultSet rs = statement.executeQuery();

            if(rs.next()) {
                sql = "SELECT * FROM notifications WHERE exelon_id = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, exelonId);
                ResultSet notifs = statement.executeQuery();

                JsonArray arr = new JsonArray();
                while (notifs.next()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("message", notifs.getString("msg"));
                    obj.addProperty("notificationId", notifs.getString("EB_n_id"));
                    obj.addProperty("timestamp", notifs.getString("t_stamp"));
                    obj.addProperty("confirm", notifs.getByte("resp_outstanding"));
                    arr.add(obj);
                }

                return arr;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public static EverbridgeNotification GetActiveNotifications(String notificationId) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT * FROM notifications WHERE EB_n_id = ? AND resp_outstanding != 0";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, notificationId);
            ResultSet rs = statement.executeQuery();

            EverbridgeNotification notification = null;
            List<String> exelonIds = new ArrayList<>();
            while(rs.next()) {
                if(rs.getByte("resp_outstanding") != 0) {
                    exelonIds.add(rs.getString("exelon_id"));
                }

                if(notification == null)
                    notification = new EverbridgeNotification(notificationId, rs.getString("msg"), rs.getLong("t_stamp"));
            }

            if(notification != null) {
                notification.SetExelonIds(exelonIds);
                notification.SetDeviceIds(GetUserOS(exelonIds));
            }

            return notification;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, OperatingSystem> GetUserOS(List<String> exelonIds) {
        Map<String, OperatingSystem> deviceIds = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(connectionUrl)) {
            String ids = "?,".repeat(exelonIds.size());
            ids = ids.substring(0, ids.length()-1);
            String sql = "SELECT * FROM users WHERE exelon_id IN (" + ids + ")";
            PreparedStatement statement = connection.prepareStatement(sql);
            for(int i = 0; i < exelonIds.size(); i++) {
                statement.setString(i + 1, exelonIds.get(i));
            }

            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                deviceIds.put(rs.getString("exelon_id"), rs.getByte("os") == 1 ? OperatingSystem.Android : OperatingSystem.iOS);
            }

            return deviceIds;
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

    public static String GetUserFromToken(String token) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "SELECT 1 FROM users WHERE login_token = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, token);
            ResultSet rs = statement.executeQuery();

            return rs.getString("exelon_id");
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean InsertNotification(EverbridgeNotification notification) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String notificationId = notification.GetId();
            long timestamp = notification.GetTimestamp();
            String message = notification.GetMessage();
            List<String> exelonIds = notification.GetExelonIds();

            String params = ("(" + notificationId + ", " + timestamp + ", ?, " + 2 +", '" + message + "'),").repeat(exelonIds.size());
            params = params.substring(0, params.length()-1);

            String sql = "IF NOT EXISTS (SELECT * FROM notifications WHERE EB_n_id = ?) INSERT INTO notifications (EB_n_id, t_stamp, exelon_id, resp_outstanding, msg) VALUES " + params;
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, notificationId);
            for(int i = 0; i < exelonIds.size(); i++)
                statement.setString(i + 2, exelonIds.get(i));

            int rows = statement.executeUpdate();

            return rows != -1;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean DecrementPNCount(String notificationId) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "UPDATE notifications SET resp_outstanding = resp_outstanding - 1 WHERE EB_n_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, notificationId);

            statement.executeUpdate();

            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean ConfirmNotification(String exelonId, String notificationId) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "UPDATE notifications SET resp_outstanding = 0 WHERE EB_n_id = ? AND exelon_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, notificationId);
            statement.setString(2, exelonId);

            return statement.executeUpdate() == 1;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean InsertToken(String token, String exelonId) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);) {
            String sql = "UPDATE users SET login_token = ? WHERE exelon_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, token);
            statement.setString(2, exelonId);

            return statement.executeUpdate() == 1;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
