package com.nlt.mobileteam.wifidirect.wifiP2pListeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.text.TextUtils;
import android.util.Log;

import com.nlt.mobileteam.wifidirect.WifiDirectCore;
import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pDirector;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;

import java.util.Collection;

import static android.net.wifi.p2p.WifiP2pManager.EXTRA_DISCOVERY_STATE;
import static android.net.wifi.p2p.WifiP2pManager.EXTRA_P2P_DEVICE_LIST;
import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_P2P_DEVICE;
import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_P2P_GROUP;
import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_STATE;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_DISABLED;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_ENABLED;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION;

public class WiFiP2pReceiverDirector extends BroadcastReceiver {
    private static final String TAG = WiFiP2pReceiverDirector.class.getSimpleName();
    private static final boolean VERBOSE = true;

    private WifiP2pManager manager;
    private int state = 0;
    private int prevState = 0;
    private WifiP2pDevice toConnect;
    private volatile boolean isRegistered;

    /**
     * @param manager WifiP2pManager system service
     */
    public WiFiP2pReceiverDirector(WifiP2pManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(VERBOSE) Log.w(TAG, action);
        if (WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            processConnectionChangedAction(intent);
        } else if (WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            processThisDeviceChangedAction(intent);
        } else if (WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            processPeersChangedAction(intent);
        } else if (WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            processStateChangedAction(intent);
        } else if (WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            processDiscoveryChangedAction(intent);
        }
    }

    private void processDiscoveryChangedAction(Intent intent) {
        if(VERBOSE) Log.w(TAG, "WIFI_P2P_DISCOVERY_CHANGED_ACTION, state " + intent.getIntExtra(EXTRA_DISCOVERY_STATE, WIFI_P2P_DISCOVERY_STOPPED));
    }

    private void processStateChangedAction(Intent intent) {
        prevState = state;
        state = intent.getIntExtra(EXTRA_WIFI_STATE, 0);
        if(VERBOSE) Log.w(TAG, "state: " + state);
    }

    private void processPeersChangedAction(Intent intent) {
        WifiP2pDeviceList deviceList = intent.getParcelableExtra(EXTRA_P2P_DEVICE_LIST);
        if (deviceList != null) {
            for (WifiP2pDevice device : deviceList.getDeviceList()) {
                if(VERBOSE) Log.w(TAG, device.deviceName + " status " + device.status);
            }
        } else {
            if(VERBOSE) Log.w(TAG, "deviceList = null");
        }
    }

    private void processThisDeviceChangedAction(Intent intent) {
        WifiP2pDevice device = intent.getParcelableExtra(EXTRA_WIFI_P2P_DEVICE);
        WiFiP2pDirector.get().setThisDevice(device);
        //Log.w(ChatActivity.TAG, "Device status -" + device.status);
    }

    private void processConnectionChangedAction(Intent intent) {
        if (manager == null) {
            return;
        }

        WifiP2pGroup group = intent.getParcelableExtra(EXTRA_WIFI_P2P_GROUP);
        if(VERBOSE) Log.w(TAG, "GCreated: " + group.getNetworkName() + " GOwner: " + group.isGroupOwner() + " clients: ");

        Collection<WifiP2pDevice> clientList = group.getClientList();
        if (clientList != null) {
            WiFiP2pDirector.get().refreshClientList(clientList);

            if (VERBOSE) {
                for (WifiP2pDevice device : clientList) {
                    if(VERBOSE) Log.w(TAG, device.deviceName + " status " + device.status);
                    if (device.status == 4 && CommunicationController.get().isNoAssistantsConnected()) {
                        WifiDirectCore.disableWifi();
                    }
                }
            }
        }

        if (TextUtils.isEmpty(group.getNetworkName()) && state == WIFI_P2P_STATE_ENABLED && prevState == WIFI_P2P_STATE_DISABLED) {
            WiFiP2pDirector.get().preClearServices();
        }

        if (group.getClientList() != null && group.isGroupOwner()) {
            for (WifiP2pDevice device : group.getClientList()) {
                WiFiP2pService service = new WiFiP2pService();
                service.device = device;
                service.instanceName = WifiDirectCore.SERVICE_INSTANCE_ASSISTANT;
                service.status = WiFiP2pService.FOUND;
                if (WiFiP2pDirector.get().updateDeviceList(service)) {
                    WiFiP2pDirector.get().stopConnector(service);
                    CommunicationController.get().getServerSocketHandler().setConnectingService(service);
                }
            }
        }

        if (toConnect != null && group.getClientList() != null && group.isGroupOwner()) {
            if (group.getClientList().contains(toConnect)) {
                // WiFiP2pDirector.get().getConnectionStateCallback().setConnectionResult(GroupOwnerSocketHandler.CONNECTION_PEER_CONNECTED);
                toConnect = null;
            }
        }
    }

    public Intent register(Context context, IntentFilter filter) {
        isRegistered = true;
        return context.registerReceiver(this, filter);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            context.unregisterReceiver(this);
            isRegistered = false;
            return true;
        }
        return false;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }
}