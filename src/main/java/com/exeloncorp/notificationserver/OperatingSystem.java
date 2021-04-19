package com.exeloncorp.notificationserver;

public enum OperatingSystem {
    Android(1),
    iOS(2);

    private int num;

    OperatingSystem(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
