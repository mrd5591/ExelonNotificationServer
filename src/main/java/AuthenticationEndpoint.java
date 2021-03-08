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
public class AuthenticationEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response SignUp(String json) {
        HashMap<String, String> params = new Gson().fromJson(json, new TypeToken<HashMap<String, String>>(){}.getType());

        if(params == null)
            return Response.status(400).build();

        String exelonId = params.get("exelonId");
        String email = params.get("email");
        String firstName = params.get("firstName");
        String lastName = params.get("lastName");
        String os = params.get("os");
        String password = params.get("password");

        JsonObject jsonResp = new JsonObject();

        boolean result = false;
        String resultMessage = "";
        boolean error = false;
        if(exelonId != null && email != null && firstName != null && lastName != null && os != null && password != null) {
            if(!exelonId.matches("[0-9]+") || exelonId.length() != 6) {
                error = true;
                resultMessage = "The employee ID format is incorrect! It must be 6 numbers.";
            } else if(!firstName.matches("/^[a-z ,.'-]+$/i") || firstName.length() < 2) {
                error = true;
                resultMessage = "This name is invalid!";
            } else if(!lastName.matches("/^[a-z ,.'-]+$/i") || lastName.length() < 2) {
                error = true;
                resultMessage = "This name is invalid!";
            } else if(!Util.passwordIsValid(password)) {
                error = true;
                resultMessage = "The password is invalid!";
            } else if(!Util.emailIsValid(email)) {
                error = true;
                resultMessage = "The email is invalid!";
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
