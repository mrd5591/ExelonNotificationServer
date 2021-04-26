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
        String password = params.get("password");

        JsonObject jsonResp = new JsonObject();

        boolean result = false;
        String errorMessage = "";
        int errorNum = -1;
        if(exelonId != null && email != null && firstName != null && lastName != null && password != null) {
            if(!exelonId.matches("[0-9]+") || exelonId.length() != 6) {
                errorMessage = "The employee ID format is incorrect! It must be 6 numbers.";
                errorNum = 0;
            } else if(!firstName.matches("(?i)(^[a-z])((?![ .,'-]$)[a-z .,'-]){0,24}$")) {
                errorMessage = "This name is invalid!";
                errorNum = 1;
            } else if(!lastName.matches("(?i)(^[a-z])((?![ .,'-]$)[a-z .,'-]){0,24}$")) {
                errorMessage = "This name is invalid!";
                errorNum = 2;
            } else if(!Util.passwordIsValid(password)) {
                errorMessage = "The password is invalid!";
                errorNum = 3;
            } else if(!Util.emailIsValid(email)) {
                errorMessage = "The email is invalid!";
                errorNum = 4;
            } else {
                password = SCryptUtil.scrypt(password, 16384, 8, 1);

                Map<String, String> signUpParams = new HashMap<>();
                signUpParams.put("firstName", firstName.toUpperCase());
                signUpParams.put("lastName", lastName.toUpperCase());
                signUpParams.put("exelonId", exelonId);
                signUpParams.put("email", email);
                signUpParams.put("password", password);

                result = DatabaseConnection.SignUp(signUpParams);
            }
        }

        if(!result) {
            errorNum = 5;
            errorMessage = "There was an unexpected server error. Please try again.";
        }


        if(!result) {
            jsonResp.addProperty("result", false);
            jsonResp.addProperty("errorMessage", errorMessage);
            jsonResp.addProperty("errorNum", errorNum);
        } else {
            jsonResp.addProperty("result", true);
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
        String pnsToken = params.get("pnsToken");
        OperatingSystem os;
        try {
            os = OperatingSystem.valueOf(params.get("os"));
        } catch (IllegalArgumentException e) {
            os = null;
        }


        JsonObject jsonResp = new JsonObject();

        boolean result = false;
        String token = null;
        if(exelonId != null && password != null && os != null && deviceId != null && pnsToken != null && Util.isInteger(exelonId)) {
            String hashed = DatabaseConnection.Login(exelonId.trim());

            if(hashed != null) {
                result = SCryptUtil.check(password, hashed);
            }

            if(result) {
                token = Util.generateNewToken();

                if(token != null && DatabaseConnection.InsertToken(token, exelonId)) {
                    if(os == OperatingSystem.iOS) {
                        MobileNotificationService.RegisteriOS(deviceId.trim(), exelonId.trim());
                    } else if(os == OperatingSystem.Android) {
                        MobileNotificationService.RegisterAndroid(deviceId.trim(), exelonId.trim(), pnsToken.trim());
                    }
                }
            }
        }

        jsonResp.addProperty("result", result);
        jsonResp.addProperty("token", token);

        return Response.status(200).expires(new Date(System.currentTimeMillis() + 10000)).type(MediaType.APPLICATION_JSON).entity(jsonResp.toString()).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String Get() {
        return "This works";
    }
}
