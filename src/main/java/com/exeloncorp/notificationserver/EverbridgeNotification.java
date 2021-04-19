package com.exeloncorp.notificationserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EverbridgeNotification
{
    private String id;
    private String message;
    private long timestamp;
    private List<String> exelonIds;
    private Map<String, OperatingSystem> deviceIds;

    public EverbridgeNotification(String id, String message, long timestamp) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        exelonIds = new ArrayList<>();
        deviceIds = new HashMap<>();
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

    public long GetTimestamp() {
        return timestamp;
    }

    public List<String> GetExelonIds() {
        return exelonIds;
    }

    public Map<String, OperatingSystem> GetDeviceIds() {
        return deviceIds;
    }

    public void SetDeviceIds(Map<String, OperatingSystem> deviceIds) {
        this.deviceIds = deviceIds;
    }
}
