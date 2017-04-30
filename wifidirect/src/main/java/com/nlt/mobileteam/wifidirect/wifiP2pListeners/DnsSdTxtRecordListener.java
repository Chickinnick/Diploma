package com.nlt.mobileteam.wifidirect.wifiP2pListeners;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.Map;

public class DnsSdTxtRecordListener implements WifiP2pManager.DnsSdTxtRecordListener {
    private final static String TAG = "_DnsTxtRecord";



    public DnsSdTxtRecordListener() {
    }

    @Override
    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice device) {


    }
}
