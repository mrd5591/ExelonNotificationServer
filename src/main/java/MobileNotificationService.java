import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

public class MobileNotificationService
{
    private static ApnsClient apnsClient;

    static {
        try {
            apnsClient = new ApnsClientBuilder().setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                    .setSigningKey(ApnsSigningKey.loadFromPkcs8File(new File(""), "TEAMID", "KEYID")).build();
        } catch (IOException e) {
            apnsClient = null;
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            apnsClient = null;
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            apnsClient = null;
            e.printStackTrace();
        }
    }

    public static void PushAPNNotification(SimpleApnsPushNotification notification) {
        final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>
                sendNotificationFuture = apnsClient.sendNotification(notification);

        try {
            final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
                    sendNotificationFuture.get();

            if (pushNotificationResponse.isAccepted()) {
                System.out.println("Push notification accepted by APNs gateway.");
            } else {
                System.out.println("Notification rejected by the APNs gateway: " +
                        pushNotificationResponse.getRejectionReason());

                pushNotificationResponse.getTokenInvalidationTimestamp().ifPresent(timestamp -> {
                    System.out.println("\t…and the token is invalid as of " + timestamp);
                });
            }
        } catch (final ExecutionException e) {
            e.printStackTrace();
            PushAPNNotification(notification);
        } catch (InterruptedException e) {
            e.printStackTrace();
            PushAPNNotification(notification);
        }
    }

    public static SimpleApnsPushNotification BuildNotification(String message) {
        final SimpleApnsPushNotification pushNotification;

        final ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
        payloadBuilder.setAlertBody(message);

        final String payload = payloadBuilder.build();
        final String token = TokenUtil.sanitizeTokenString("<efc7492 bdbd8209>");

        pushNotification = new SimpleApnsPushNotification(token, "com.ExelonCorp", payload);

        return pushNotification;
    }
}
