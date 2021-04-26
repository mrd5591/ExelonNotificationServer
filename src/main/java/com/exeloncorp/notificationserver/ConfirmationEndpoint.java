package com.exeloncorp.notificationserver;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.appengine.repackaged.com.google.gson.reflect.TypeToken;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;

@Path("/confirm/{notificationId}")
public class ConfirmationEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ConfirmNotification(@HeaderParam("Bearer") String token, @PathParam("notificationId") String notificationId) {
        JsonObject jsonResp = new JsonObject();

        boolean result = false;
        if(token != null && notificationId != null) {
            String exelonId = DatabaseConnection.GetUserFromToken(token);

            if(exelonId != null) {
                result = DatabaseConnection.ConfirmNotification(exelonId, notificationId);
            }
        }

        jsonResp.addProperty("result", result);

        return Response.status(201).expires(new Date(System.currentTimeMillis() + 10000)).type(MediaType.APPLICATION_JSON).entity(jsonResp.toString()).build();
    }
}
