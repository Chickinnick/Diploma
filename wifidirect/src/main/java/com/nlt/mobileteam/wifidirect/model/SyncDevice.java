package com.nlt.mobileteam.wifidirect.model;

public class SyncDevice {
    private volatile long networkSpeed;
    private volatile long timeDiff;
    private volatile long trimEnd = 0;

    public long getNetworkSpeed() {
        return networkSpeed;
    }

    public void setNetworkSpeed(long networkSpeed) {
        this.networkSpeed = networkSpeed;
    }

    public long getTimeDiff() {
        return timeDiff;
    }

    public void setTimeDiff(long timeDiff) {
        this.timeDiff = timeDiff;
    }

    public void setTrimEnd(long trimEnd) {
        this.trimEnd = trimEnd;
    }

    public long getTrimOffset() {
        return trimEnd;
    }
}
