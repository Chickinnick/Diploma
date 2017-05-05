package com.nlt.mobileteam.wifidirect.model.event.assistant;

/**
 * Created by Nick on 02.05.2017.
 */

public class DirectorConnect {
    private String deviceName;

    public DirectorConnect(String deviceName) {

        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
