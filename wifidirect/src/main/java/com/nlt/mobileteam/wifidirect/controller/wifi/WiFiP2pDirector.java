package com.nlt.mobileteam.wifidirect.controller.wifi;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.text.TextUtils;
import android.util.Log;

import com.nlt.mobileteam.wifidirect.WifiDirect;
import com.nlt.mobileteam.wifidirect.WifiDirectCore;
import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.socket.AbstractGroupOwnerSocketHandler;
import com.nlt.mobileteam.wifidirect.controller.socket.DirectorSocketHandlerType;
import com.nlt.mobileteam.wifidirect.controller.socket.GroupOwnerSocketHandler;
import com.nlt.mobileteam.wifidirect.controller.socket.SocketHandler;
import com.nlt.mobileteam.wifidirect.model.InstanceCode;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;
import com.nlt.mobileteam.wifidirect.model.event.director.NotifyDeviceList;
import com.nlt.mobileteam.wifidirect.utils.Callback;
import com.nlt.mobileteam.wifidirect.utils.DeviceList;
import com.nlt.mobileteam.wifidirect.utils.exception.SetupSocketHandlerException;
import com.nlt.mobileteam.wifidirect.wifiP2pListeners.ChannelListener;
import com.nlt.mobileteam.wifidirect.wifiP2pListeners.WiFiP2pReceiverDirector;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



/**
 * Singleton, used by Director instance to manage WiFi - Direct connections.
 */
public class WiFiP2pDirector {
    public final static String TAG = "_WiFiP2pDirector";
    private Context context;
    private static final Object initSynObj = new Object();
    private static final boolean VERBOSE = true;

    private static String SERVICE_INSTANCE = WifiDirectCore.SERVICE_INSTANCE_DIRECTOR;
    public static final String TXTRECORD_PROP_AVAILABLE = "available";

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private final WiFiP2pReceiverDirector receiver;
    private IntentFilter filter;
    private WifiP2pDnsSdServiceInfo localService;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private WifiP2pDevice thisDevice;
    private ChannelListener channelListener;
    private DeviceList devicesList;
    private int communicatorsStopped = 0;
    private Thread threadConnector;
    private Queue<WiFiP2pService> toConnect;
    private Lock lock = new ReentrantLock();
    private boolean isLocalServiceStarted;



    private WiFiP2pDirector(Context context) {
        this.context = context;
        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(context, context.getMainLooper(), channelListener);
        receiver = new WiFiP2pReceiverDirector(manager);
    }

    private static WiFiP2pDirector instance;


    public static void init(Context context) {
        if (instance == null) {
            synchronized (initSynObj) {
                if (instance == null) {
                    instance = new WiFiP2pDirector(context);
                }
            }
        }
    }
    public static WiFiP2pDirector get() {
        return instance;
    }

    /**
     * Makes all required work to initialize wi-fi direct framework.
     * <p>Initializes instance of {@link WifiP2pDnsSdServiceInfo} that will represent current
     * devices as WiFi - Direct service.<br>
     * Initializes instance of {@link WifiP2pDnsSdServiceRequest} that will be required to
     * start services descovery.<br>
     * WiFi - Direct framework when any corresponding device will be found.
     * </p>
     * <p>If no instance of {@link WifiP2pDnsSdServiceInfo} would'nt be
     * registered, then connection with this device won't be available</p>
     */
    public void prepareService() {
        toConnect = new LinkedBlockingQueue<>();
        filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        channelListener = new ChannelListener();
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(WifiDirectCore.SERVICE_REG_TYPE);

        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");
        localService = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE + "_" + WifiDirect.SESSION_KEY_VALUE, WifiDirectCore.SERVICE_REG_TYPE, record);

        setupSocketHandler();
        //setupDnsResponseListener(new DnsSdRLDirector());
        clearManagerCallbacks();
        preClearServices();

        devicesList = new DeviceList(WifiDirect.DEVICES_COUNT);
    }

    /**
     *
     * @param dnsSdRLDirector If null - no callback about discovered device will be received.
     */
//    public void setupDnsResponseListener(DnsSdRLDirector dnsSdRLDirector) {
//        manager.setDnsSdResponseListeners(channel, dnsSdRLDirector, null);
//    }

    /**
     * Initializes instance of {@link AbstractGroupOwnerSocketHandler}.<br>
     * If we are using regular CinaMaker director - {@link GroupOwnerSocketHandler}
     * will be initialised. <br>
     */
    private void setupSocketHandler() {
        AbstractGroupOwnerSocketHandler serverSocketHandler;
        if (WifiDirectCore.cameraSessionInstanceCode == InstanceCode.DIRECTOR) {
            serverSocketHandler = SocketHandler.getServer(DirectorSocketHandlerType.CINAMAKER_DIRECTOR);
        }  else {
            throw new SetupSocketHandlerException("Wrong WifiDirectCore.cameraSessionInstanceCode");
        }
        CommunicationController.get().setServerSocketHandler(serverSocketHandler);
    }

    /**
     * <p>Initial waypoint in WiFi - Direct workflow.<br>
     * The very first thing we need to do is to clear possibly registered services to avid any <br>
     * unexpected circumstance.
     * If services are successfully cleared, then {@link WiFiP2pDirector#addLocalService} is called.
     * </p>
     */
    public void preClearServices() {
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.w(TAG, "clearLocalServices success");
                isLocalServiceStarted = false;
                addLocalService(true);
            }

            @Override
            public void onFailure(int reason) {
                if (VERBOSE) Log.w(TAG, "clearLocalServices fail: " + reason);
            }
        });
    }

    /**
     * <p>
     * Registers instance of {@link WifiP2pDnsSdServiceInfo} into WiFi-Direct framework.<br>
     *
     * @param prepareGroup if group preparation required.
     */
    public void addLocalService(final boolean prepareGroup) {
        if (isLocalServiceStarted) {
            return;
        }
        manager.addLocalService(channel, localService, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.w(TAG, "addLocalService success");
                isLocalServiceStarted = true;
                if (prepareGroup) {
                    prepareGroup();
                }
            }

            @Override
            public void onFailure(int reason) {
                if (VERBOSE) Log.w(TAG, "addLocalService fail: " + reason);
                preClearServices();
            }
        });
    }

    private void prepareGroup() {
        if (VERBOSE) Log.w(TAG, "prepareGroup requestGroupInfo");
        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (VERBOSE) Log.w(TAG, "prepareGroup onGroupInfoAvailable " + group);
                if (group != null) {
                    manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            if (VERBOSE) Log.w(TAG, "prepareGroup removeGroup success");
                            createGroup();
                        }

                        @Override
                        public void onFailure(int reason) {
                            if (VERBOSE) Log.w(TAG, "prepareGroup removeGroup fial " + reason);
                        }
                    });
                } else {
                    createGroup();
                }
            }
        });
    }

    private void createGroup() {
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (VERBOSE) Log.w(TAG, "createGroup success");
                registerReceiver();
            }

            @Override
            public void onFailure(int reason) {
                if (VERBOSE) Log.w(TAG, "createGroup fail: " + reason);
                if (reason == 2) {
                    manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    isLocalServiceStarted = false;
                                    addLocalService(true);
                                }

                                @Override
                                public void onFailure(int reason) {
                                    if (VERBOSE) Log.w(TAG, "clearLocalServices fail: " + reason);
                                    preClearServices();
                                }
                            });
                        }

                        @Override
                        public void onFailure(int reason) {
                            if (VERBOSE) Log.w(TAG, "removeGroup fail: " + reason);
                            preClearServices();
                        }
                    });
                }
            }
        });
    }

    public void clearManagerCallbacks() {
        if (VERBOSE) Log.w(TAG, "clearManagerCallbacks");
        if (channel == null) {
            return;
        }
        manager.clearLocalServices(channel, null);
        manager.requestGroupInfo(channel, null);
        manager.removeGroup(channel, null);
        manager.createGroup(channel, null);

        manager.clearServiceRequests(channel, null);

        if (localService != null) {
            manager.addLocalService(channel, localService, null);
        }

        if (serviceRequest != null) {
            manager.addServiceRequest(channel, serviceRequest, null);
            manager.removeServiceRequest(channel, serviceRequest, null);
        }
    }

    private void registerReceiver() {
        if (!receiver.isRegistered()) {
            receiver.register(context, filter);
        }
    }

    private void unregisterReceiver() {
        if (context != null && receiver != null) {
            try {
                lock.lock();
                if (context != null && receiver != null) {
                    receiver.unregister(context);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * <p>Finalizes all WiFi-Direct actions.</p>
     * Breaks ongoing connection requests, calls socket connection close.
     */
    public void stop() {
        if (toConnect != null) {
            toConnect.clear();
        }
        stopConnector(null);
        if (VERBOSE) Log.w(TAG, "closing socket connections");
        CommunicationController.get().closeConnections(new Callback() {
            @Override
            public void apply() {
                if (devicesList.size() == 0) {
                    cleanUpWiFi();
                }
            }
        });

        unregisterReceiver();
        manager.setDnsSdResponseListeners(channel, null, null);
        thisDevice = null;
    }

    /**
     * Removes registered {@link WifiP2pDnsSdServiceRequest} and deletes {@link WifiP2pGroup}
     */
    public void cleanUpWiFi() {
        if (VERBOSE) Log.w(TAG, "calling wifi cleanup");
        if (++communicatorsStopped >= devicesList.size()) {

            manager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    if (VERBOSE) Log.w(TAG, "clearServiceRequests success");
                    manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                        @Override
                        public void onGroupInfoAvailable(final WifiP2pGroup group) {
                            if (group != null && !TextUtils.isEmpty(group.getNetworkName()) && group.isGroupOwner() && manager != null && channel != null) {
                                manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        if (VERBOSE) Log.w(TAG, "removeGroup success");
//                                        deletePersistentGroup(group);
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        if (VERBOSE) Log.w(TAG, "removeGroup fail: " + reason);
                                    }
                                });
                            }
                        }

//                        @Override
//                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
//
//                        }
                    });
                }

                @Override
                public void onFailure(int reason) {
                    if (VERBOSE) Log.w(TAG, "clearServiceRequests fail: " + reason);
                }
            });
            clearLocalServices();

            communicatorsStopped = 0;
        }
    }

    /**
     * Clears registered in WiFi-Direct framework instances of {@link WifiP2pDnsSdServiceInfo}.
     */
    public void clearLocalServices() {
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.w(TAG, "clearLocalServices success");
                isLocalServiceStarted = false;
            }

            @Override
            public void onFailure(int reason) {
                Log.w(TAG, "clearLocalServices fail: " + reason);
            }
        });
    }

    /**
     * Invokes connection request to specified device.
     *
     * @param service specifies remote devices connect to.
     */
    public void connect(WiFiP2pService service) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.wps.pin = "0000";
        if (threadConnector == null) {
            toConnect.add(service);
            initConnection(service, config);
        }
    }

    private void initConnection(final WiFiP2pService service, final WifiP2pConfig config) {

        if (!devicesList.contains(service) || devicesList.get(devicesList.indexOf(service)).status < WiFiP2pService.SOCKET_CONNECTED) {
            threadConnector = new Thread(new Runnable() {
                private static final int CONNECTION_ATTEMPTS = 4;
                private static final int TIMEOUT = 10_000;

                @Override
                public void run() {
                    for (int i = 0; i < CONNECTION_ATTEMPTS; ++i) {
                        if (!threadConnector.isInterrupted()) {
                            if (VERBOSE)
                                Log.w(TAG, "DIRECTOR sending connect invite to " + service.device.deviceName + " attempt " + i);
                            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    if (VERBOSE)
                                        Log.w(TAG, "manger.onSuccess with " + service.device.deviceName);
                                }

                                @Override
                                public void onFailure(int errorCode) {
                                    if (VERBOSE)
                                        Log.w(TAG, "Failed connecting to service " + errorCode);

                                }
                            });

                            synchronized (this) {
                                try {
                                    this.wait(TIMEOUT);
                                } catch (InterruptedException e) {
                                    if (VERBOSE) Log.e(TAG, "interrupted, leaving ");
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    if (threadConnector != null && !threadConnector.isInterrupted()) {
                        synchronized (WiFiP2pDirector.this) {
                            threadConnector = null;
                        }
                        toConnect.remove(service);
                        if (toConnect.size() > 1) {
                            connect(toConnect.peek());
                        }
                    }
                }
            });
            threadConnector.setName("Connecting to " + service.device.deviceName);
            threadConnector.start();
        }
    }

    public void stopConnector(WiFiP2pService service) {
        synchronized (this) {
            if (threadConnector != null && service == null
                    || threadConnector != null && threadConnector.isAlive()) {

                if (toConnect.peek() == null || toConnect.peek().equals(service)) {
                    threadConnector.interrupt();
                    threadConnector = null;
                }
            }
            if (toConnect.contains(service)) {
                toConnect.remove(service);
            }
        }
    }

    public boolean updateDeviceList(WiFiP2pService service) {
        boolean isNew = false;
        if (devicesList.size() < WifiDirect.DEVICES_COUNT) {
            if (!devicesList.contains(service)) {
                if (VERBOSE) Log.w(TAG, "adding " + service.device.deviceName);
                devicesList.add(service);
                service.index = devicesList.indexOf(service) + 1;
                Log.e(TAG, "updateDeviceList, inserted index + 1 = " + service.index);
                isNew = true;
                EventBus.getDefault().post(new NotifyDeviceList());

            }
        }
        return isNew;
    }

    /**
     * Identifies current device.
     *
     * @param thisDevice current device info.
     */
    public void setThisDevice(WifiP2pDevice thisDevice) {
        this.thisDevice = thisDevice;
        WifiDirectCore.thisDevice = thisDevice;
        if (VERBOSE)
            Log.w(TAG, "this device name: " + this.thisDevice.deviceName + " status " + thisDevice.status);
    }

    public int getDeviceCount() {
        if (devicesList != null) {
            return devicesList.size();
        } else {
            return 0;
        }
    }

    public WifiP2pDevice getThisDevice() {
        return thisDevice;
    }

    public DeviceList getDevices() {
        return devicesList;
    }

    public void removeDevice(WiFiP2pService service) {
        if (devicesList.contains(service)) {
//            removeFromGroup(service.device);
            devicesList.remove(service);
            EventBus.getDefault().post(new NotifyDeviceList());

            updateIndexes();
        }
    }

    /**
     * When one of assistants disconnects we need to update all assistant indexes to set proper one<br>
     * to newly connected assistant.
     */
    private void updateIndexes() {
        for (WiFiP2pService service : devicesList) {
            service.index = devicesList.indexOf(service) + 1;
        }
    }

    public boolean contains(String deviceName) {
        boolean result = false;
        for (WiFiP2pService service : devicesList) {
            if (service.device.deviceName.equals(deviceName)) {
                result = true;
                service.status = WiFiP2pService.SOCKET_CONNECTED;
                EventBus.getDefault().post(new NotifyDeviceList());

                break;
            }
        }

        return result;
    }

    public void refreshClientList(Collection<WifiP2pDevice> newClientList) {
        if (newClientList == null) {
            return;
        }

        boolean update = false;
        if (devicesList != null) {
            if (newClientList.isEmpty()) {
                devicesList.clear();
                update = true;
            } else {
                WiFiP2pService emptyCommunicator = devicesList.getEmptyCommunicator();
                for (WiFiP2pService service : devicesList) {
                    if (!newClientList.contains(service.device) && !service.equals(emptyCommunicator)) {
                        //remove disconnected client(service) from the list
                        Log.i(TAG, "refreshClientList devicesList.remove(service)");
                        devicesList.remove(service);
                        update = true;
                    }
                }
            }

            if (update) {
                EventBus.getDefault().post(new NotifyDeviceList(WiFiP2pDirector.get().getDevices()));

            }
        }
    }


    public boolean isNoConnectedDevices() {
        return devicesList.size() == 0;
    }

}