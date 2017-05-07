package com.nlt.mobileteam.wifidirect.wifiP2pListeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.text.TextUtils;
import android.util.Log;

import com.nlt.mobileteam.wifidirect.controller.socket.SocketHandler;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pAssistant;
import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorDisconnect;
import com.nlt.mobileteam.wifidirect.service.PeerBroadcastService;

import org.greenrobot.eventbus.EventBus;

import java.net.InetAddress;

import static android.net.wifi.p2p.WifiP2pDevice.AVAILABLE;
import static android.net.wifi.p2p.WifiP2pDevice.CONNECTED;
import static android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import static android.net.wifi.p2p.WifiP2pManager.EXTRA_NETWORK_INFO;
import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_P2P_DEVICE;
import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_P2P_GROUP;
import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_P2P_INFO;
import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_STATE;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION;
import static android.text.TextUtils.isEmpty;

public class WiFiP2pReceiverAssistant extends BroadcastReceiver {
    private static final String TAG = "_BROADCAST";
    private static final String DIRECTOR_STUB_NAME = "director";
    private static final boolean VERBOSE = true;

    private WifiP2pManager manager;
    private Channel channel;
    private boolean isInited = true;
    private boolean isConnected = false;
    private volatile boolean isRegistered;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     */
    public WiFiP2pReceiverAssistant(WifiP2pManager manager, Channel channel) {
        super();
        this.manager = manager;
        this.channel = channel;
//        this.logger = (ChatActivity)activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.w(TAG, action);
        if (WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            processConnectionChangedAction(intent);
        } else if (WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            processThisDeviceChangedAction(intent);
        } else if (WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            processStateChangedAction(intent);
        }
    }

    private void processStateChangedAction(Intent intent) {
        int state = intent.getIntExtra(EXTRA_WIFI_STATE, 0);
        if (VERBOSE) Log.w(TAG, "state: " + state);
    }

    private void processThisDeviceChangedAction(Intent intent) {
        WifiP2pDevice device = intent.getParcelableExtra(EXTRA_WIFI_P2P_DEVICE);
        WiFiP2pAssistant.get().setThisDevice(device);
        //Log.w(ChatActivity.TAG, "Device status -" + device.status);
    }

    private void processConnectionChangedAction(Intent intent) {
        if (manager == null) {
            return;
        }
        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(EXTRA_NETWORK_INFO);
        WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(EXTRA_WIFI_P2P_INFO);
        WifiP2pGroup group = intent.getParcelableExtra(EXTRA_WIFI_P2P_GROUP);

        if (VERBOSE) Log.w(TAG, "is connected " + networkInfo.isConnected());
        if (networkInfo.isConnected() && group.getOwner() != null) {
            //manager.requestConnectionInfo(channel, WiFiP2pController.get().getConnectionInfoListener());
            if (isInited) {
                if (VERBOSE) Log.w(TAG, "Cleaning unfinished session");
                WiFiP2pAssistant.get().closeConnection();
                isInited = false;
                WiFiP2pAssistant.get().setOwnerName("");

            } else {
                String ownerName = group.getOwner().deviceName;
                WiFiP2pAssistant.get().setOwnerName(!TextUtils.isEmpty(ownerName) ? ownerName : DIRECTOR_STUB_NAME);
            }
        } else {
            // It's a disconnect
        }

        isInited = false;

        if (VERBOSE)
            Log.w(TAG, "GCreated: " + group.getNetworkName() + " GOwner: " + group.isGroupOwner() + " clients: ");

        if (VERBOSE) {
            for (WifiP2pDevice device : group.getClientList()) {
                if (VERBOSE) Log.w(TAG, device.deviceName + " status " + device.status);
            }
        }

        WifiP2pDevice thisDevice = WiFiP2pAssistant.get().getThisDevice();
        if (thisDevice != null) {
            if (isEmpty(group.getNetworkName()) && thisDevice.status == AVAILABLE && !networkInfo.isConnected()) {
                if (isConnected) {
                    if (VERBOSE) Log.w(TAG, "COMM_DIRECTOR_DISCONNECTING");
                    EventBus.getDefault().post(new DirectorDisconnect());
                    isConnected = false;
                }
            } else if (!isEmpty(group.getNetworkName()) && thisDevice.status == AVAILABLE && !networkInfo.isConnected()) {
                PeerBroadcastService.setBroadcastStatus(PeerBroadcastService.START);
            } else if (!isEmpty(group.getNetworkName()) && thisDevice.status == CONNECTED && networkInfo.isConnected()) {
                PeerBroadcastService.setBroadcastStatus(PeerBroadcastService.STOP);
                manager.requestConnectionInfo(channel, new ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        if (info.groupFormed) {
                            setupSocketHandler(info.groupOwnerAddress);
                            isConnected = true;
                        }
                    }
                });
            }
        }
    }

    public void register(Context context, IntentFilter filter) {
        try {
            context.registerReceiver(this, filter);
        } catch (Exception e) {
            if (VERBOSE) Log.e(TAG, "error unregistering receiver");
//            e.printStackTrace();
        }
    }

    public void unregister(Context context) {
        try {
            context.unregisterReceiver(this);
            isRegistered = false;
        } catch (Exception e) {
            if (VERBOSE) Log.e(TAG, "error unregistering receiver");
        }
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    private void setupSocketHandler(InetAddress groupOwnerAddress) {
        SocketHandler.getClient(groupOwnerAddress);
    }
}

