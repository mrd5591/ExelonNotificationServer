package com.exeloncorp.notificationserver;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.appengine.repackaged.com.google.gson.reflect.TypeToken;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;

@Path("/confirm")
public class ConfirmationEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ConfirmNotification(String json) {
        HashMap<String, String> params = new Gson().fromJson(json, new TypeToken<HashMap<String, String>>(){}.getType());

        String employeeId = params.get("employeeId");
        String notificiationId = params.get("notificationId");

        JsonObject jsonResp = new JsonObject();

        boolean result = false;
        if(employeeId != null && notificiationId != null) {
            //check database for employeeId and notificationId
        }

        jsonResp.addProperty("result", result);

        return Response.status(201).expires(new Date(System.currentTimeMillis() + 10000)).type(MediaType.APPLICATION_JSON).entity(jsonResp.toString()).build();
    }
}
