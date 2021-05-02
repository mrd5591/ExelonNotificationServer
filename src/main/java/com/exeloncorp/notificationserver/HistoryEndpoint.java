package com.exeloncorp.notificationserver;

import com.google.appengine.repackaged.com.google.gson.JsonArray;
import com.google.appengine.repackaged.com.google.gson.JsonObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

@Path("/history/{exelonId}")
public class HistoryEndpoint {
    @GET
    @Produces
    public Response GetHistory(@HeaderParam("Authorization") String token, @PathParam("exelonId") String exelonId) {

        if(token == null)
            return Response.status(401).build();

        String type = null;
        String value = null;

        String[] tokenArr = token.split(" ");

        if(tokenArr.length == 2 && tokenArr[0].equals("Bearer")) {
            type = "Bearer";
            value = tokenArr[1];
        }

        JsonObject jsonResp = new JsonObject();
        JsonArray arr = new JsonArray();
        boolean result = false;
        if(exelonId != null && type != null && value != null) {
            arr = DatabaseConnection.GetAccountHistory(exelonId, value);

            if(arr != null) {
                result = true;
            } else {
                result = false;
            }
        }

        if(result) {
            jsonResp.addProperty("result", true);
            jsonResp.add("resultSet", arr);
        } else {
            jsonResp.addProperty("result", false);
        }

        return Response.status(200).expires(new Date(System.currentTimeMillis() + 10000)).type(MediaType.APPLICATION_JSON).entity(jsonResp.toString()).build();
    }
}
