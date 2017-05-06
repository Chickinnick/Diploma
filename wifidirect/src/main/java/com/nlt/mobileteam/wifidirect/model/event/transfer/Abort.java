package com.nlt.mobileteam.wifidirect.model.event.transfer;


public class Abort {
    private int deviceIndex;

    public Abort() {
    }

    public Abort(int deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    public int getDeviceIndex() {
        return deviceIndex;
    }
}
