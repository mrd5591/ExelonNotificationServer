package com.exeloncorp.notificationserver;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.lambdaworks.crypto.SCryptUtil;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.appengine.repackaged.com.google.gson.reflect.TypeToken;
// This is a change
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
        String firstName = params.get("firstName").toUpperCase();
        String lastName = params.get("lastName").toUpperCase();
        String password = params.get("password");

        JsonObject jsonResp = new JsonObject();

        boolean result = false;
        String errorMessage = "";
        boolean error = false;
        if(exelonId != null && email != null && firstName != null && lastName != null && password != null) {
            if(!exelonId.matches("[0-9]+") || exelonId.length() != 6) {
                error = true;
                errorMessage = "The employee ID format is incorrect! It must be 6 numbers.";
            } else if(!firstName.matches("(?i)(^[a-z])((?![ .,'-]$)[a-z .,'-]){0,24}$")) {
                error = true;
                errorMessage = "This name is invalid!";
            } else if(!lastName.matches("(?i)(^[a-z])((?![ .,'-]$)[a-z .,'-]){0,24}$")) {
                error = true;
                errorMessage = "This name is invalid!";
            } else if(!Util.passwordIsValid(password)) {
                error = true;
                errorMessage = "The password is invalid!";
            } else if(!Util.emailIsValid(email)) {
                error = true;
                errorMessage = "The email is invalid!";
            } else {
                password = SCryptUtil.scrypt(password, 16384, 8, 1);

                Map<String, String> signUpParams = new HashMap<>();
                signUpParams.put("firstName", firstName);
                signUpParams.put("lastName", lastName);
                signUpParams.put("exelonId", exelonId);
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

        return Response.status(200).expires(new Date(System.currentTimeMillis() + 10000)).type(MediaType.APPLICATION_JSON).entity(jsonResp.toString()).build();
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
        String deviceId = params.get("deviceId");
        OperatingSystem os;
        try {
            os = OperatingSystem.valueOf(params.get("os"));
        } catch (IllegalArgumentException e) {
            os = null;
        }


        JsonObject jsonResp = new JsonObject();

        boolean result = false;
        if(exelonId != null && password != null && os != null && deviceId != null && Util.isInteger(exelonId)) {
            password = SCryptUtil.scrypt(password, 16384, 8, 1);

            result = DatabaseConnection.Login(exelonId, password);

            if(result) {
                if(os == OperatingSystem.iOS) {
                    MobileNotificationService.RegisteriOS(deviceId);
                } else if(os == OperatingSystem.Android) {
                    MobileNotificationService.RegisterAndroid(deviceId);
                }
            }
        }

        jsonResp.addProperty("result", result);

        return Response.status(200).expires(new Date(System.currentTimeMillis() + 10000)).type(MediaType.APPLICATION_JSON).entity(jsonResp.toString()).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String Get() {
        return "This works";
    }
}
