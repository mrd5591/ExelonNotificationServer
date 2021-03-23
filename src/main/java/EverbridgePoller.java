import java.io.IOException;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.json.*;

public class EverbridgePoller
{
    private static String ExelonOrganizationId = "454514914099365";
    private static HttpClient EverbridgeClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(20)).build();
    private static HttpRequest EverbridgeNotificationRequest = HttpRequest.newBuilder().uri(URI.create("http://api.everbridge.net/rest/incidents/" + ExelonOrganizationId + "?onlyOpen=true&incidentType=All")).header("Authorization", "cHN1ZXhlbG9uY2Fwc3RvbmU6R3JhbmRIYXQxMzI=").GET().build();

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
            JSONObject json = new JSONObject(response);
            JSONArray data = json.getJSONObject("page").getJSONArray("data");

            List<String> notificationNames = new ArrayList<>();

            for(int i = 0; i < data.length(); i++) {
                JSONObject incident = data.getJSONObject(i);

                String incidentType = incident.getString("incidentType");

                if(incidentType.equals("Scenario")) {
                    JSONArray notificationIds = incident.getJSONArray("notificationIds");

                    List<String> notifIds = new ArrayList<>();
                    for(Object notificationId : notificationIds) {
                        notifIds.add(notificationId.toString());
                    }

                    notificationNames.addAll(GetNotifications(notifIds));
                } else if(incidentType.equals("Incident")) {
                    String name = incident.getString("name");
                    notificationNames.add(name);
                }
            }

            if(!notificationNames.isEmpty()) {
                //send push notification
            }
        }
    }

    private static List<String> GetNotifications(List<String> notificationIds) {
        List<String> notificationNames = new ArrayList<>();
        for(String notificationId : notificationIds) {
            try {
                HttpRequest notificationRequest = HttpRequest.newBuilder().uri(URI.create("http://api.everbridge.net/rest/notifications/" + ExelonOrganizationId + "/" +notificationId)).header("Authorization", "cHN1ZXhlbG9uY2Fwc3RvbmU6R3JhbmRIYXQxMzI=").GET().build();
                HttpResponse<String> response = EverbridgeClient.send(notificationRequest, HttpResponse.BodyHandlers.ofString());

                if(response != null) {
                    JSONObject json = new JSONObject(response);
                    String name = json.getJSONObject("result").getJSONObject("message").getString("title");

                    notificationNames.add(name);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return notificationNames;
            } catch (IOException e) {
                e.printStackTrace();
                return notificationNames;
            }
        }

        return notificationNames;
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
