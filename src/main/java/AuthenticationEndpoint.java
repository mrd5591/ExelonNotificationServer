import javax.ws.rs.*;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import com.lambdaworks.crypto.SCryptUtil;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.appengine.repackaged.com.google.gson.reflect.TypeToken;

@Path("/authenticate")
public class AuthenticationEndpoint extends HttpServlet
{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response SignUp(String json) {
        HashMap<String, String> params = new Gson().fromJson(json, new TypeToken<HashMap<String, String>>(){}.getType());

        if(params == null)
            return Response.status(400).build();

        String employeeId = params.get("employeeId");
        String phoneNumber = params.get("phoneNumber");
        String name = params.get("name");
        String password = params.get("password");

        JsonObject jsonResp = new JsonObject();

        boolean result = false;
        String resultMessage = "";
        boolean error = false;
        if(employeeId != null && phoneNumber != null && name != null && password != null) {
            if(!employeeId.matches("[0-9]+")) {
                error = true;
                resultMessage = "The employee ID must only contain numbers!";
            } else if(!phoneNumber.matches("[0-9]+") || phoneNumber.length() != 10) {
                error = true;
                resultMessage = "The phone number must contain 10 numbers!";
            } else if(!name.matches("/^[a-z ,.'-]+$/i") || name.length() < 5) {
                error = true;
                resultMessage = "This name is invalid!";
            } else if(password.length() < 8) {
                error = true;
                resultMessage = "The password must contain at least 8 characters!";
            } else {
                password = SCryptUtil.scrypt(password, 16384, 8, 1);
                //everything is good

                result = true;
            }
        }

        if(error) {
            jsonResp.addProperty("error", true);
            jsonResp.addProperty("errorMessage", resultMessage);
        } else {
            jsonResp.addProperty("result", result);
        }

        return Response.status(201).expires(new Date(System.currentTimeMillis() + 10000)).type(MediaType.APPLICATION_JSON).entity(jsonResp.toString()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response Login(String json) {
        HashMap<String, String> params = new Gson().fromJson(json, new TypeToken<HashMap<String, String>>(){}.getType());

        if(params == null)
            return Response.status(400).build();

        String phoneNumber = params.get("phoneNumber");
        String password = params.get("password");

        JsonObject jsonResp = new JsonObject();

        boolean result = false;
        if(phoneNumber != null && password != null) {
            password = SCryptUtil.scrypt(password, 16384, 8, 1);

            result = true;
        }

        jsonResp.addProperty("result", result);

        return Response.status(200).expires(new Date(System.currentTimeMillis() + 10000)).type(MediaType.APPLICATION_JSON).entity(jsonResp.toString()).build();
    }
}
