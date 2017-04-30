package com.nlt.mobileteam.wifidirect.controller.wifi;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.nlt.mobileteam.cinacore.BroadcastManager;
import com.nlt.mobileteam.cinacore.CinaCoreModule;
import com.nlt.mobileteam.wifidirect.WifiDirectCore;
import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;
import com.nlt.mobileteam.wifidirect.service.PeerBroadcastService;
import com.nlt.mobileteam.wifidirect.utils.Callback;
import com.nlt.mobileteam.wifidirect.wifiP2pListeners.ChannelListener;
import com.nlt.mobileteam.wifidirect.wifiP2pListeners.DnsSdRLAssistant;
import com.nlt.mobileteam.wifidirect.wifiP2pListeners.WiFiP2pReceiverAssistant;

import java.util.HashMap;
import java.util.Map;

import static com.nlt.mobileteam.cinacore.Action.COMM_SET_OWNER;

public class WiFiP2pAssistant {
    private static final String TAG = "_WiFiP2pAssistant";
    private volatile static WiFiP2pAssistant instance;
    private static Context context;
    private String ownerName;
    private static final Object initSynObj = new Object();
    private static final boolean VERBOSE = true;
    private WiFiP2pAssistantCallback callback;
    private volatile boolean isConnecting;

    private WiFiP2pAssistant(WiFiP2pAssistantCallback callback) {
        this.callback = callback;
        context = WifiDirectCore.getAppContext();

        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(context, context.getMainLooper(), channelListener);
        receiver = new WiFiP2pReceiverAssistant(manager, channel);
    }


    public static WiFiP2pAssistant get() {
        if (instance == null) {
            synchronized (initSynObj) {
                if (instance == null) {
                    instance = new WiFiP2pAssistant(new WiFiP2pAssistantCallback() {
                        @Override
                        public void restoreService() {
                            instance.disconnectingWifiIsProgress = false;
                            if (instance.isCameraActivity) {
                                if (VERBOSE) Log.w(TAG, "restoreService");
                                instance.prepareService();
                            }
                        }
                    });
                }
            }
        }
        return instance;
    }

    private static String SERVICE_INSTANCE = WifiDirectCore.SERVICE_INSTANCE_ASSISTANT;
    private static final String TXTRECORD_PROP_AVAILABLE = "available";

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDnsSdServiceInfo localService;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private final WiFiP2pReceiverAssistant receiver;
    private WifiP2pDevice thisDevice;
    private ChannelListener channelListener;
    private boolean inGroup = false;
    private volatile boolean disconnectingWifiIsProgress;
    private volatile boolean isCameraActivity;
    private Thread closeConnectionAndCallPrepareServiceThread;

    interface WiFiP2pAssistantCallback {
        void restoreService();
    }

    public void prepareService() {
        Log.w(TAG, "prepareService, disconnectingWifiIsProgress = " + disconnectingWifiIsProgress);
        if (disconnectingWifiIsProgress) {
            closeConnectionAndCallPrepareService();
        } else {
            channelListener = new ChannelListener();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

            clearManagerCallbacks();

            if (!receiver.isRegistered()) {
                receiver.register(context, intentFilter);
            }
            serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(WifiDirectCore.SERVICE_REG_TYPE);
            setupDnsResponseListener(new DnsSdRLAssistant());
        }
    }

    /**
     * This method close connection if wifi disconnecting in progress
     * To be sure we wait 6 seconds for disconnecting finishes
     * In case of disconnection still in progress after wait(probably stuck somewhere)
     * Change the disconnection flag and call prepareService() (for obtaining new connection)
     * Otherwise prepareService() has already called from callback
     */
    private void closeConnectionAndCallPrepareService() {
        if (WiFiP2pAssistant.get().isDisconnectingWifiIsProgress()) {
            closeConnectionAndCallPrepareServiceThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        try {
                            wait(16000);
                            if (isDisconnectingWifiIsProgress()) {
                                setDisconnectingWifiIsProgress(false);
                                prepareService();
                            }
                        } catch (InterruptedException e) {
                            Log.w(TAG, "closeConnectionAndCallPrepareServiceThread is interrupted " + e);
                            //e.printStackTrace();
                        }

                    }
                }
            });
            closeConnectionAndCallPrepareServiceThread.start();
        }
    }

    public void unregisterReceiver() {
        try {
            if (context != null) {
                receiver.unregister(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDisconnectingWifiIsProgress() {
        return disconnectingWifiIsProgress;
    }

    public boolean isCameraActivity() {
        return isCameraActivity;
    }

    public void setDisconnectingWifiIsProgress(boolean disconnectingWifiIsProgress) {
        this.disconnectingWifiIsProgress = disconnectingWifiIsProgress;
    }

    public void setCameraActivity(boolean cameraActivity) {
        isCameraActivity = cameraActivity;
    }

    public WifiP2pDevice getThisDevice() {
        return thisDevice;
    }

    public void setThisDevice(WifiP2pDevice thisDevice) {
        if (this.thisDevice == null && !TextUtils.isEmpty(thisDevice.deviceName)) {
            initService(thisDevice);
        }
        this.thisDevice = thisDevice;
        WifiDirectCore.thisDevice = thisDevice;
        if (VERBOSE)
            Log.w(TAG, "this device name: " + this.thisDevice.deviceName + " status " + thisDevice.status);
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    private void initService(WifiP2pDevice thisDevice) {
        Map<String, String> record = new HashMap<>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");
        localService = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE + "_" + CinaCoreModule.SESSION_KEY_VALUE + "._" + thisDevice.deviceName,
                WifiDirectCore.SERVICE_REG_TYPE, record);
        //serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(WifiDirectCore.SERVICE_REG_TYPE);
        //setupDnsResponseListener(new DnsSdRLAssistant());


        //CommunicationController.get().initController(activityContext);

        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (VERBOSE) Log.w(TAG, "clearLocalServices success");
                manager.addLocalService(channel, localService, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        if (VERBOSE) Log.w(TAG, "addLocalService success");
                        startBroadcastService();
                    }

                    @Override
                    public void onFailure(int reason) {
                        if (VERBOSE) Log.w(TAG, "addLocalService fail: " + reason);
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                if (VERBOSE) Log.w(TAG, "clearLocalServices fail: " + reason);
            }
        });
    }

    private void startBroadcastService() {
        context.startService(new Intent(context, PeerBroadcastService.class));
    }

    public void discoverPeers() {
        if (channel != null) {
            manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.w(TAG, "cancelConnect success");
                    stopDiscover(new DiscoverRestartCallback() {
                        @Override
                        public void restart() {
                            startDiscovering();
                        }
                    });
                }

                @Override
                public void onFailure(int reason) {
                    Log.w(TAG, "cancelConnect fail: " + reason);
                    stopDiscover(new DiscoverRestartCallback() {
                        @Override
                        public void restart() {
                            startDiscovering();
                        }
                    });
                }
            });
        }
    }

    public void discoverPeers_samsung() {
        if (channel != null) {
            manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.w(TAG, "cancelConnect success");
                    stopDiscoveringSgh(new DiscoverRestartCallback() {
                        @Override
                        public void restart() {
                            startDiscoveringSgh();
                        }
                    });
                }

                @Override
                public void onFailure(int reason) {
                    Log.w(TAG, "cancelConnect fail: " + reason);
                    stopDiscover(new DiscoverRestartCallback() {
                        @Override
                        public void restart() {
                            stopDiscoveringSgh(new DiscoverRestartCallback() {
                                @Override
                                public void restart() {
                                    startDiscoveringSgh();
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    public void stop() {
        if (disconnectingWifiIsProgress) {
            return;
        }
        if (closeConnectionAndCallPrepareServiceThread != null) {
            closeConnectionAndCallPrepareServiceThread.interrupt();
            closeConnectionAndCallPrepareServiceThread = null;
        }
        disconnectingWifiIsProgress = true;
        PeerBroadcastService.setBroadcastStatus(PeerBroadcastService.STOP_SERVICE);
        unregisterReceiver();
        CommunicationController.get().closeConnections(new Callback() {
            @Override
            public void apply() {
                cleanUpWiFi();
            }
        });

        ownerName = null;
        thisDevice = null;
        inGroup = false;

        manager.setDnsSdResponseListeners(channel, null, null);
        clearManagerCallbacks();
    }

    private void cleanUpWiFi() {
        clearLocalService();
        closeConnection();
    }

    private void clearManagerCallbacks() {
        if (VERBOSE) Log.w(TAG, "clearManagerCallbacks");
        if (channel == null) {
            return;
        }
        manager.clearLocalServices(channel, null);
        manager.stopPeerDiscovery(channel, null);
        manager.discoverPeers(channel, null);
        manager.discoverServices(channel, null);
        manager.requestGroupInfo(channel, null);

        if (localService != null) {
            manager.addLocalService(channel, localService, null);
        }

        if (serviceRequest != null) {
            manager.addServiceRequest(channel, serviceRequest, null);
            manager.removeServiceRequest(channel, serviceRequest, null);
        }
    }

    private void clearLocalService() {
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (VERBOSE) Log.w(TAG, "clearLocalServices success");
            }

            @Override
            public void onFailure(int reason) {
                if (VERBOSE) Log.w(TAG, "clearLocalServices fail: " + reason);
            }
        });
    }

    public void stopBroadcast() {
        manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (VERBOSE) Log.w(TAG, "stopPeerDiscovery success stopBroadcast");
                callback.restoreService();
            }

            @Override
            public void onFailure(int reason) {
                if (VERBOSE) Log.w(TAG, "stopPeerDiscovery fail: " + reason);
                callback.restoreService();
            }
        });
    }

    public void stopDiscover() {
        setupDnsResponseListener(null);
        if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            WiFiP2pAssistant.get().stopDiscoverPeers_samsung();
        } else {
            WiFiP2pAssistant.get().stopDiscoverPeers();
        }
    }


    private void setupDnsResponseListener(DnsSdRLAssistant dnsSdRLAssistant) {
        manager.setDnsSdResponseListeners(channel, dnsSdRLAssistant, null);
    }

    private void stopDiscoverPeers() {

        if (channel != null) {
            stopDiscover(null);
        }
    }

    private void stopDiscover(final DiscoverRestartCallback callback) {
        if (channel != null && serviceRequest != null) {
            manager.removeServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    if (VERBOSE) Log.w(TAG, "discovering, removeServiceRequest success");
                    manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            if (null != callback) {
                                callback.restart();
                            }
                        }

                        @Override
                        public void onFailure(int reason) {
                            if (VERBOSE) Log.w(TAG, "stopPeerDiscovery fail: " + reason);
                        }
                    });
                }

                @Override
                public void onFailure(int reason) {
                    if (VERBOSE) Log.w(TAG, "clearServiceRequests fail: " + reason);
                }
            });
        }
    }

    private void startDiscovering() {
        //Log.w(TAG, "discovering, stopPeerDiscovery success");
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.w(TAG, "discovering, discoverPeers success");
                manager.addServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.w(TAG, "discovering, addServiceRequest success");
                        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.w(TAG, "discoverServices success");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.w(TAG, "discoverServices fail: " + reason);
                            }
                        });
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.w(TAG, "addServiceRequest fail: " + reason);
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.w(TAG, "discoverPeers fail: " + reason);
            }
        });
    }

    private void startDiscoveringSgh() {
        Log.w(TAG, "discovering, removeServiceRequest success");
        manager.addServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.w(TAG, "discovering, addServiceRequest success");
                manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.w(TAG, "discoverServices success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.w(TAG, "discoverServices fail: " + reason);
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    private void stopDiscoveringSgh(final DiscoverRestartCallback callback) {
        manager.removeServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (null != callback)
                    callback.restart();
            }

            @Override
            public void onFailure(int reason) {
                Log.w(TAG, "clearServiceRequests fail: " + reason);
            }
        });
    }

    private void stopDiscoverPeers_samsung() {

        if (channel != null) {
            stopDiscoveringSgh(null);
        }
    }

    public void connect(final WiFiP2pService service) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (!isConnecting())
            initConnection(service, config);
    }

    private void initConnection(final WiFiP2pService service, final WifiP2pConfig config) {
        isConnecting = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "ASSISTANT sending connect invite to " + service.device.deviceName + " attempt ");
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.w(TAG, "manger.onSuccess with " + service.device.deviceName);
                        isConnecting = false;
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        Log.w(TAG, "Failed connecting to service " + errorCode);
                        isConnecting = false;
                    }
                });
            }
        }).start();
    }

    public void closeConnection() {
        removeGroup();
    }

    private void removeGroup() {
        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(final WifiP2pGroup group) {
                if (group != null && !TextUtils.isEmpty(group.getNetworkName()) && manager != null && channel != null) {
                    manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            if (VERBOSE) Log.w(TAG, "removeGroup success");
                            setOwnerName("");
                        }

                        @Override
                        public void onFailure(int reason) {
                            if (VERBOSE) Log.w(TAG, "removeGroup fail: " + reason);
                        }
                    });
                }
            }
        });
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void broadcastOwnerName() {
        if (context != null) {
            BroadcastManager.get().sendString(COMM_SET_OWNER, ownerName);
        }
    }

    public boolean isInGroup() {
        return inGroup;
    }

    public void setInGroup(boolean inGroup) {
        this.inGroup = inGroup;
    }
}