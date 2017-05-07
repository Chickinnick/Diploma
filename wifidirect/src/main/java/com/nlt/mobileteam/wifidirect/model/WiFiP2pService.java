package com.nlt.mobileteam.wifidirect.model;

import android.net.wifi.p2p.WifiP2pDevice;

public class WiFiP2pService {
	public WifiP2pDevice device;
	public String instanceName = null;
	public int index = 0;
	public int status = -1;

	public static final int FOUND = 0;
	public static final int SOCKET_CONNECTED = 1;
	public static final int SYNCRONIZED = 2;

	@Override
	public boolean equals(Object o) {
        boolean result = false;
        if(o instanceof WiFiP2pService) {
            WiFiP2pService item = (WiFiP2pService) o;
            result =  device.deviceName.equals(item.device.deviceName) && comparePattern(instanceName);
        }
        return result;
	}

	private boolean comparePattern(String instanceName) {
		return this.instanceName.substring(1, 2).equalsIgnoreCase(instanceName.substring(1, 2));
	}


}
