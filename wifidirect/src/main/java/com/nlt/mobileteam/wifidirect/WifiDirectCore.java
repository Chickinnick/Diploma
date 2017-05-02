package com.nlt.mobileteam.wifidirect;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import com.nlt.mobileteam.wifidirect.model.InstanceCode;


public class WifiDirectCore {

    public static WifiP2pDevice thisDevice;
    public static volatile int cameraSessionInstanceCode;

    public static final String SERVICE_INSTANCE_KEY = "_SERVICE_INSTANCE";
    public static final String SERVICE_INSTANCE_ASSISTANT = "_a.";
    public static final String SERVICE_INSTANCE_DIRECTOR = "_d.";

    public static final String SERVICE_REG_TYPE = "_cinamaker._tcp";


    public static boolean isAssistant(int instanceCode) {
        return instanceCode == InstanceCode.ASSISTANT ||
                instanceCode == InstanceCode.SONY_CAM ||
                instanceCode == InstanceCode.GO_PRO;
    }
}
