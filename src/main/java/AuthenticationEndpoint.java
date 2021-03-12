import javax.ws.rs.*;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        String errorMessage = "";
        boolean error = false;
        if(exelonId != null && email != null && firstName != null && lastName != null && os != null && password != null) {
            if(!exelonId.matches("[0-9]+") || exelonId.length() != 6) {
                error = true;
                errorMessage = "The employee ID format is incorrect! It must be 6 numbers.";
            } else if(!firstName.matches("/^[a-z ,.'-]+$/i") || firstName.length() < 2) {
                error = true;
                errorMessage = "This name is invalid!";
            } else if(!lastName.matches("/^[a-z ,.'-]+$/i") || lastName.length() < 2) {
                error = true;
                errorMessage = "This name is invalid!";
            } else if(!Util.passwordIsValid(password)) {
                error = true;
                errorMessage = "The password is invalid!";
            } else if(!Util.emailIsValid(email)) {
                error = true;
                errorMessage = "The email is invalid!";
            } else if(!OperatingSystem.Android.name().equals(os) && !OperatingSystem.iOS.name().equals(os)) {
                error = true;
                errorMessage = "The operating system is invalid";
            } else {
                password = SCryptUtil.scrypt(password, 16384, 8, 1);

                Map<String, String> signUpParams = new HashMap<>();
                signUpParams.put("firstName", firstName);
                signUpParams.put("lastName", lastName);
                signUpParams.put("exelonId", exelonId);
                signUpParams.put("os", os);
                signUpParams.put("email", email);
                signUpParams.put("password", password);

                result = DatabaseConnection.SignUp(signUpParams);
            }
        }

        if(error) {
            jsonResp.addProperty("error", true);
            jsonResp.addProperty("errorMessage", errorMessage);
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

        String exelonId = params.get("exelonId");
        String password = params.get("password");

        JsonObject jsonResp = new JsonObject();

        boolean result = false;
        if(exelonId != null && password != null && Util.isInteger(exelonId)) {
            password = SCryptUtil.scrypt(password, 16384, 8, 1);
            int id = Integer.parseInt(exelonId);

            result = DatabaseConnection.Login(id, password);
        }

        jsonResp.addProperty("result", result);

        return Response.status(200).expires(new Date(System.currentTimeMillis() + 10000)).type(MediaType.APPLICATION_JSON).entity(jsonResp.toString()).build();
    }
}
