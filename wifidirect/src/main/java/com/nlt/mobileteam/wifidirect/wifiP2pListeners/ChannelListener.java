package com.nlt.mobileteam.wifidirect.wifiP2pListeners;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class ChannelListener implements WifiP2pManager.ChannelListener {
    public final static String TAG = "_ChannelListener";

    @Override
    public void onChannelDisconnected() {
        Log.w(TAG, "Channel disconnected");
    }
}
