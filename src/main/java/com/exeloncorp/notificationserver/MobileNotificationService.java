package com.exeloncorp.notificationserver;

import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.windowsazure.messaging.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MobileNotificationService
{
    private static final String connectionString = "Endpoint=sb://psuexelon.servicebus.windows.net/;SharedAccessKeyName=DefaultFullSharedAccessSignature;SharedAccessKey=k/5GsPQ+ROsduyxys0GPSUp1sbSduK2Ph2pdg85q8oU=";
    private static final String hubName = "ExelonHub";
    private static NotificationHub hub;
    static {
        hub = new NotificationHub(connectionString, hubName);
    }

    public static boolean SendFCM(String data, Set<String> userIds) {
        JsonObject message = new JsonObject();
        JsonObject body = new JsonObject();
        body.addProperty("title", "Everbridge Alert");
        body.addProperty("body", data);
        message.add("notification", body);

        Notification n = Notification.createFcmNotification(message.toString());
        Set<String> tags = new HashSet<>();
        for(String user : userIds) {
            tags.add("$UserId:{" + user + "}");
        }

        try {
            hub.sendNotification(n, tags);
        } catch (NotificationHubsException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean SendAPN(String data, Set<String> userIds) {
        String alert = "{\"aps\":{\"alert\": \"" + data + "\" }}";
        Notification n = Notification.createAppleNotification(alert);

        Set<String> tags = new HashSet<>();
        for(String user : userIds) {
            tags.add("$UserId:{" + user + "}");
        }

        try {
            hub.sendNotification(n, tags);
        } catch (NotificationHubsException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean RegisteriOS(String deviceToken, String exelonId) {
        Installation installation = new Installation(deviceToken, NotificationPlatform.Apns, deviceToken);
        installation.setUserId(exelonId);

        try {
            hub.createOrUpdateInstallation(installation);
        } catch (NotificationHubsException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean RegisterAndroid(String deviceToken, String exelonId, String pnsToken) {
        Installation installation = new Installation(deviceToken, NotificationPlatform.Gcm, pnsToken);
        installation.setUserId(exelonId);

        try {
            hub.createOrUpdateInstallation(installation);
        } catch (NotificationHubsException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
