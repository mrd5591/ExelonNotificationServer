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
    public Response GetHistory(@HeaderParam("Bearer") String token, @PathParam("exelonId") String exelonId) {
        JsonObject jsonResp = new JsonObject();
        JsonArray arr = new JsonArray();
        boolean result = false;
        if(exelonId != null || token != null) {
            ResultSet rs = DatabaseConnection.GetAccountHistory(exelonId, token);

            if(rs != null) {
                result = true;

                while(true) {
                    try {
                        if (rs.next()) {
                            JsonObject obj = new JsonObject();
                            obj.addProperty("notificationId", rs.getString("EB_n_id"));
                            obj.addProperty("message", rs.getString("msg"));
                            obj.addProperty("timestamp", rs.getString("t_stamp"));
                            obj.addProperty("confirm", rs.getByte("resp_outstanding"));
                            arr.add(obj);
                        } else {
                            break;
                        }
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                        result = false;
                        break;
                    }
                }
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
