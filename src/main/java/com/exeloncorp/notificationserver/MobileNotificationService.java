package com.exeloncorp.notificationserver;

import com.windowsazure.messaging.*;

import java.util.HashSet;
import java.util.Set;

public class MobileNotificationService
{
    private static final String connectionString = "sb://psuexelon.servicebus.windows.net/;SharedAccessKeyName=DefaultFullSharedAccessSignature;SharedAccessKey=k/5GsPQ+ROsduyxys0GPSUp1sbSduK2Ph2pdg85q8oU=";
    private static final String hubName = "ExelonHub";
    private static NotificationHub hub;
    static {
        hub = new NotificationHub(connectionString, hubName);
    }

    public static boolean SendFCM(String data, Set<String> userIds) {
        String message = "{\"data\":{\"msg\":" + data + " }}";
        Notification n = Notification.createFcmNotification(message);

        Set<String> tags = new HashSet<>();
        for(String user : userIds) {
            tags.add("$UserId:" + user);
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
        String alert = "{\"aps\":{\"alert\": " + data + " }}";
        Notification n = Notification.createAppleNotification(alert);

        Set<String> tags = new HashSet<>();
        for(String user : userIds) {
            tags.add("$UserId:" + user);
        }

        try {
            hub.sendNotification(n, tags);
        } catch (NotificationHubsException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean RegisteriOS(String deviceToken) {
        AppleRegistration reg = new AppleRegistration(deviceToken);
        try {
            hub.createRegistration(reg);
        } catch (NotificationHubsException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean RegisterAndroid(String deviceToken) {
        FcmRegistration reg = new FcmRegistration(deviceToken);
        try {
            hub.createRegistration(reg);
        } catch (NotificationHubsException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
