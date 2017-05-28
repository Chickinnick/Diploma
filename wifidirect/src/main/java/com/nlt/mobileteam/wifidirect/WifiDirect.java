package com.nlt.mobileteam.wifidirect;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pAssistant;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pDirector;
import com.nlt.mobileteam.wifidirect.listeners.TransferingActionListener;
import com.nlt.mobileteam.wifidirect.listeners.WifiDirectActionListener;
import com.nlt.mobileteam.wifidirect.model.InstanceCode;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Abort;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Progress;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Success;
import com.nlt.mobileteam.wifidirect.receiver.WifiStateChangedReceiver;
import com.nlt.mobileteam.wifidirect.service.PeerBroadcastService;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_STOP_PEER_BROADCAST_SERVICE;

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
        disablePeers();
    }


    public void destroy() {
        switch (mInstanceCode) {
            case InstanceCode.DIRECTOR: {
                WiFiP2pDirector.get().stop();
                break;
            }
            case InstanceCode.ASSISTANT: {
                WiFiP2pAssistant.get().stop();
                break;
            }
        }
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

    public static void disablePeers() {
        switch (WifiDirectCore.cameraSessionInstanceCode) {
            case InstanceCode.DIRECTOR: {
                CommunicationController.get().sendMessageToAll(MESSAGE_STOP_PEER_BROADCAST_SERVICE);
                break;
            }
            case InstanceCode.ASSISTANT: {
                PeerBroadcastService.setBroadcastStatus(PeerBroadcastService.STOP);
                WiFiP2pAssistant.get().stopDiscover();
                break;
            }
        }
    }

    public void setActionListener(WifiDirectActionListener actionListener) {
        this.actionListener = actionListener;
        EventBus.getDefault().register(actionListener);

    }

    public void sendFile(File file, TransferingActionListener subscriber ){
        CommunicationController.get().sendRecordedVideo(file);
        EventBus.getDefault().register(subscriber);
    }

    public void addOnFileTransferListener(TransferingActionListener transferingActionListener) {
        EventBus.getDefault().register(transferingActionListener);
    }
}
