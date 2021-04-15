package com.exeloncorp.notificationserver;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

import org.json.*;

public class EverbridgePoller
{
    private static String ExelonOrganizationId = "454514914099365";
    private static HttpClient EverbridgeClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(20)).build();
    private static HttpRequest EverbridgeNotificationRequest = HttpRequest.newBuilder().uri(URI.create("http://api.everbridge.net/rest/incidents/" + ExelonOrganizationId + "?onlyOpen=true&incidentType=All")).header("Authorization", "cHN1ZXhlbG9uY2Fwc3RvbmU6R3JhbmRIYXQxMzI=").GET().build();
    private static Map<String, Timer> notificationTimers = new HashMap<>();

    public static void Poll() {
        HttpResponse<String> response;
        try {
            response = EverbridgeClient.send(EverbridgeNotificationRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        if(response != null) {
            JSONObject json = new JSONObject(response.body());
            JSONArray data = json.getJSONObject("page").getJSONArray("data");

            List<EverbridgeNotification> notifications = new ArrayList<>();

            for(int i = 0; i < data.length(); i++) {
                JSONObject incident = data.getJSONObject(i);

                JSONArray notificationIds = incident.getJSONArray("notificationIds");

                List<String> notifIds = new ArrayList<>();
                for(Object notificationId : notificationIds) {
                    notifIds.add(notificationId.toString());
                }

                notifications.addAll(GetNotifications(notifIds));
            }

            if(!notifications.isEmpty()) {
                for(EverbridgeNotification notification : notifications) {
                    boolean added = DatabaseConnection.InsertNotification(notification);

                    if(added && !notificationTimers.containsKey(notification.GetId())) {
                        notificationTimers.put(notification.GetId(), CreateNotificationTimer(notification.GetId()));
                    }
                }
            }
        }
    }

    private static void DatabaseSweep(String notificationId) {
        ResultSet activeNotifications = DatabaseConnection.GetActiveNotifications(notificationId);
        Set<String> exelonIds = new HashSet<>();
        String message = null;

        while(true) {
            try {
                if (!activeNotifications.next()) break;

                if(activeNotifications.getByte("resp_outstanding") == 1) {
                    exelonIds.add(activeNotifications.getString("EB_n_id"));
                }

                if(message == null)
                    message = activeNotifications.getString("message");

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        if(exelonIds.isEmpty()) {
            notificationTimers.get(notificationId).cancel();
            notificationTimers.get(notificationId).purge();
        } else {
            MobileNotificationService.SendAPN(message, exelonIds);
            MobileNotificationService.SendFCM(message, exelonIds);
        }
    }

    private static List<EverbridgeNotification> GetNotifications(List<String> notificationIds) {
        List<EverbridgeNotification> notifications = new ArrayList<>();
        for(String notificationId : notificationIds) {
            try {
                HttpRequest notificationRequest = HttpRequest.newBuilder().uri(URI.create("http://api.everbridge.net/rest/notifications/" + ExelonOrganizationId + "/" +notificationId)).header("Authorization", "cHN1ZXhlbG9uY2Fwc3RvbmU6R3JhbmRIYXQxMzI=").GET().build();
                HttpResponse<String> response = EverbridgeClient.send(notificationRequest, HttpResponse.BodyHandlers.ofString());

                if(response != null) {
                    JSONObject json = new JSONObject(response.body());
                    String name = json.getJSONObject("result").getJSONObject("message").getString("title");
                    String ts = json.getJSONObject("result").getJSONObject("message").getString("createdDate");

                    EverbridgeNotification notification = new EverbridgeNotification(notificationId, name, ts);
                    notification.SetExelonIds(GetExternalIds(notificationId));
                    notifications.add(notification);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return notifications;
            } catch (IOException e) {
                e.printStackTrace();
                return notifications;
            }
        }

        return notifications;
    }

    private static List<String> GetExternalIds(String notificationId) {
        List<String> externalIds = new ArrayList<>();
        try {
            HttpRequest notificationReportRequest = HttpRequest.newBuilder().uri(URI.create("http://api.everbridge.net/rest/notificationReports/" + ExelonOrganizationId + "/" + notificationId)).header("Authorization", "cHN1ZXhlbG9uY2Fwc3RvbmU6R3JhbmRIYXQxMzI=").GET().build();
            HttpResponse<String> response = EverbridgeClient.send(notificationReportRequest, HttpResponse.BodyHandlers.ofString());

            if(response != null) {
                JSONObject json = new JSONObject(response.body());
                JSONArray data = json.getJSONObject("page").getJSONArray("data");

                for(int i = 0; i < data.length(); i++) {
                    JSONObject user = data.getJSONObject(i);
                    externalIds.add(user.getString("externalId"));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return externalIds;
        } catch (IOException e) {
            e.printStackTrace();
            return externalIds;
        }

        return externalIds;
    }

    private static Timer CreateNotificationTimer(String notificationId) {
        Timer timer = new Timer();
        int begin = 0;
        int interval = 60000;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EverbridgePoller.DatabaseSweep(notificationId);
            }
        }, begin, interval);

        return timer;
    }

    public static void main(String[] args) {
        Timer timer = new Timer();
        int begin = 0;
        int interval = 5000;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EverbridgePoller.Poll();
            }
        }, begin, interval);
    }
}
