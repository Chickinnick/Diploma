package com.nlt.mobileteam.wifidirect.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pAssistant;

import java.util.Random;


public class PeerBroadcastService extends Service {
    public static PeerBroadcastService instance;
    private static final String TAG = "PeerBroadcastService";
    private static final boolean VERBOSE = true;

    public static final int START = 0x10 + 1;
    public static final int STOP = 0x10 + 2;
    public static final int STOP_SERVICE = 0x10 + 3;
    public static int DISCOVER_TIME_OUT = 6000;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private Thread peerBroadcast;

    private static class ServiceHandler extends Handler implements Runnable {

        private boolean isBroadcasting = false;
        private boolean isStopping = false;

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void run() {
            while (true) {
                synchronized (this) {
                    if (isStopping || !WiFiP2pAssistant.get().isCameraActivity()) {
                        break;
                    } else
                    if (isBroadcasting && !WiFiP2pAssistant.get().isConnecting()) {
//                        WiFiP2pAssistant.get().setConnecting(true);
                        if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
                            WiFiP2pAssistant.get().discoverPeers_samsung();
                        } else {
                            WiFiP2pAssistant.get().discoverPeers();
                        }
                    } else {
                        try {
                            Thread.sleep(DISCOVER_TIME_OUT, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(DISCOVER_TIME_OUT, 0);
                    DISCOVER_TIME_OUT = Math.abs(new Random(System.currentTimeMillis()).nextInt(10000)) + 8000;
                    if (VERBOSE) Log.w(TAG, "DISCOVER TIME OUT " + DISCOVER_TIME_OUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            WiFiP2pAssistant.get().stopBroadcast();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START: {
                    if (VERBOSE) Log.w("SOCKET", "PeerBroadcastService START");
                    synchronized (this) {
                        isBroadcasting = true;
                    }
                    break;
                }
                case STOP: {
                    if (VERBOSE) Log.w("SOCKET", "PeerBroadcastService PAUSE");
                    synchronized (this) {
                        isBroadcasting = false;
                    }
                    break;
                }
                case STOP_SERVICE: {
                    if (VERBOSE) Log.w("SOCKET", "PeerBroadcastService STOP_SERVICE");
                    synchronized (this) {
                        isBroadcasting = false;
                        isStopping = true;
                        this.notify();
                    }
                    break;
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        peerBroadcast.start();
        Message msg = mServiceHandler.obtainMessage();
        msg.what = START;
        mServiceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("ServiceStartArguments");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        if (VERBOSE) Log.w("SOCKET", "Service on create");

        peerBroadcast = new Thread(mServiceHandler);
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Message msg = mServiceHandler.obtainMessage();
        msg.what = STOP_SERVICE;
        mServiceHandler.sendMessage(msg);
        if (VERBOSE) Log.w("SOCKET", "Service on destroy");
        instance = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void internalSetBroadcastStatus(int status) {
        if (VERBOSE) Log.w(TAG, "setting broadcast status: " + status);
        Message msg = mServiceHandler.obtainMessage();
        msg.what = status;
        mServiceHandler.sendMessage(msg);
        if (status == STOP_SERVICE) {
            stopSelf();
        }
    }

    public static void setBroadcastStatus(int status) {
        if (instance != null) {
            instance.internalSetBroadcastStatus(status);
        }
    }
}
