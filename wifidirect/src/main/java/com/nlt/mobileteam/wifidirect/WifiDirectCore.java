package com.nlt.mobileteam.wifidirect;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;


public class WifiDirectCore {
    public static final int INSTANCE_CODE_DIRECTOR_DDP = 0x400 + 103;
    public static final int INSTANCE_CODE_DIRECTOR = 0x400 + 102;
    public static final int INSTANCE_CODE_ASSISTANT = 0x400 + 101;
    public static final int INSTANCE_CODE_GO_PRO = 0x400 + 104;
    public static final int INSTANCE_CODE_SONY_CAM = 0x400 + 105;

    public static int devicesCount = 3;
    public static WifiP2pDevice thisDevice;
    public static volatile int cameraSessionInstanceCode;
    public static boolean sessionFormed = false;
    private static Context context;

    public static final String SERVICE_INSTANCE_KEY = "_SERVICE_INSTANCE";
    public static final String SERVICE_INSTANCE_ASSISTANT = "_a.";
    public static final String SERVICE_INSTANCE_DIRECTOR = "_d.";

    public static final String SERVICE_REG_TYPE = "_cinamaker._tcp";

    public static void init(Context context) {
        WifiDirectCore.context = context;
    }

    public static Context getAppContext() {
        return context;
    }

    public static void enableWifi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Log.i("WifiManager", "enableWifi");
            wifiManager.setWifiEnabled(true);
        }
    }
    public static void disableWifi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            Log.i("WifiManager", "disableWifi");
            wifiManager.setWifiEnabled(false);
        }
    }

    public static boolean isAssistant(int instanceCode) {
        return instanceCode == INSTANCE_CODE_ASSISTANT ||
                instanceCode == INSTANCE_CODE_SONY_CAM ||
                instanceCode == INSTANCE_CODE_GO_PRO;
    }
}
