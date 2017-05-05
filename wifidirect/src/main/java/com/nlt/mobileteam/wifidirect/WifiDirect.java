package com.nlt.mobileteam.wifidirect;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pAssistant;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pDirector;
import com.nlt.mobileteam.wifidirect.listeners.WifiDirectActionListener;
import com.nlt.mobileteam.wifidirect.model.InstanceCode;
import com.nlt.mobileteam.wifidirect.receiver.WifiStateChangedReceiver;

import org.greenrobot.eventbus.EventBus;

public class WifiDirect {

    public static final int DEVICES_COUNT = 3;
    public static String SESSION_KEY_VALUE = "1";
    private int mInstanceCode;
    private WifiStateChangedReceiver wifiStateChangedReceiver;
    private Context context;
    WifiDirectActionListener actionListener;

    private WifiDirect(Context context, int instanceCode) {
        this.context = context;
        WifiDirectCore.cameraSessionInstanceCode = instanceCode;
        mInstanceCode = instanceCode;
         registerReceivers();
        WiFiP2pAssistant.init(context);
        WiFiP2pDirector.init(context);
        prepareServices();

    }
    /**
     * @param instanceCode must be one of {@link  InstanceCode}
     */
    public static WifiDirect init(Context context, int instanceCode) {
        return new WifiDirect(context, instanceCode);
    }

    private void registerReceivers() {
        wifiStateChangedReceiver = new WifiStateChangedReceiver(this);
        context.registerReceiver(wifiStateChangedReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
    }



    public void onPause() {
        if (mInstanceCode == InstanceCode.ASSISTANT) {
            WiFiP2pAssistant.get().setCameraActivity(false);
        } else if (mInstanceCode == InstanceCode.DIRECTOR) {
            WiFiP2pDirector.get().clearLocalServices();
        }

        context.unregisterReceiver(wifiStateChangedReceiver);

        EventBus.getDefault().unregister(actionListener);
    }


    private void prepareServices() {
        switch (mInstanceCode) {
            case InstanceCode.DIRECTOR: {
                WiFiP2pAssistant.get().unregisterReceiver();
                WiFiP2pDirector.get().prepareService();
                break;
            }
            case InstanceCode.ASSISTANT: {
                WiFiP2pAssistant.get().setCameraActivity(true);
                WiFiP2pAssistant.get().prepareService(); // move to resume
                break;
            }
            case 0: {
                break;
            }
        }
    }

    public void enableWifi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Log.i("WifiManager", "enableWifi");
            wifiManager.setWifiEnabled(true);
        }
    }

    public void disableWifi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            Log.i("WifiManager", "disableWifi");
            wifiManager.setWifiEnabled(false);
        }
    }

    public void setActionListener(WifiDirectActionListener actionListener) {
        this.actionListener = actionListener;
        EventBus.getDefault().register(actionListener);

    }

}
