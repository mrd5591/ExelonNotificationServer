import java.io.IOException;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class EverbridgePoller
{
    private static String ExelonOrganizationId = "";
    private static HttpClient EverbridgeClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(20)).authenticator(Authenticator.getDefault()).build();
    private static HttpRequest EverbridgeNotificationRequest = HttpRequest.newBuilder().uri(URI.create("http://api.everbridge.net/notifications/" + ExelonOrganizationId)).GET().build();

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
            System.out.println(response);
        }
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
