package com.exeloncorp.notificationserver;

import com.google.appengine.repackaged.org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class EverbridgeNotification
{
    private String id;
    private String message;
    private String timestamp;
    private List<String> exelonIds;

    public EverbridgeNotification(String id, String message, String timestamp) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        exelonIds = new ArrayList<>();
    }

    public void SetExelonIds(List<String> exelonIds) {
        this.exelonIds = exelonIds;
    }

    public String GetMessage() {
        return message;
    }

    public String GetId() {
        return id;
    }

    public String GetTimestamp() {
        return timestamp;
    }

    public List<String> GetExelonIds() {
        return exelonIds;
    }
}
