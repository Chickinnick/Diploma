package com.nlt.mobileteam.wifidirect.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.nlt.mobileteam.wifidirect.WifiDirect;
import com.nlt.mobileteam.wifidirect.WifiDirectCore;

public class WifiStateChangedReceiver extends BroadcastReceiver {

    WifiDirect wifiDirect;


    public WifiStateChangedReceiver(WifiDirect wifiDirect) {
        this.wifiDirect = wifiDirect;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int extraWifiState =
                intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
        switch (extraWifiState) {
            case WifiManager.WIFI_STATE_DISABLED:
            case WifiManager.WIFI_STATE_DISABLING:
                wifiDirect.enableWifi();
        }
    }
}
