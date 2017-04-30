package com.nlt.mobileteam.wifidirect.wifiP2pListeners;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.nlt.mobileteam.cinacore.CinaCoreModule;
import com.nlt.mobileteam.wifidirect.WifiDirectCore;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pAssistant;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;

/**
 * <p>This class will handle all callbacks from WiFi-Direct framework about any discovered device.<br>
 * Callback registered by {@link WiFiP2pAssistant}, firstly will initiate connection with director. <br>
 * If already connected to director - will try to connect assistants.
 * </p>
 */
public class DnsSdRLAssistant implements WifiP2pManager.DnsSdServiceResponseListener {
    private final static String TAG = "_DnsResponse";
    public static final String DIRECTOR_PATTERN = "d";
    public static final String ASSISTANT_PATTERN = "a";

    @Override
    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice device) {
        if (registrationType.contains(WifiDirectCore.SERVICE_REG_TYPE)) {
            String[] dataParts = instanceName.split("\\.");
            String sessionKey = "null";
            String deviceName = "";
            if (dataParts.length > 1) {
                sessionKey = dataParts[1].replace("_", "");
                if (dataParts.length > 2) {
                    deviceName = dataParts[2].replace("_", "");
                }
            }
            Log.w(TAG, "device found: " + instanceName + " name: " + deviceName + " " + device.deviceAddress +
                    " session key: " + sessionKey);


            WiFiP2pService toConnect = null;
                if (instanceName.substring(1, 2).equalsIgnoreCase(DIRECTOR_PATTERN) && sessionKey.equals(CinaCoreModule.SESSION_KEY_VALUE)) {
                    toConnect = new WiFiP2pService();
                    toConnect.device = device;
                    toConnect.instanceName = instanceName;
                }
            if (toConnect != null) {
                WiFiP2pAssistant.get().connect(toConnect);
            }
        }
    }
}
