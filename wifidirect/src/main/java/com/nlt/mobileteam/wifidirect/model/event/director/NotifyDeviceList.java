package com.nlt.mobileteam.wifidirect.model.event.director;


import android.net.wifi.p2p.WifiP2pDevice;

import com.nlt.mobileteam.wifidirect.utils.DeviceList;

import java.util.List;

public class NotifyDeviceList {


    private DeviceList deviceList;

    public NotifyDeviceList() {
    }

    public NotifyDeviceList(DeviceList deviceList) {
        this.deviceList = deviceList;
    }

    public DeviceList getDeviceList() {
        return deviceList;
    }
}
