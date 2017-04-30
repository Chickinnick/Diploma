package com.nlt.mobileteam.wifidirect.controller.socket;

import android.util.Log;

import com.nlt.mobileteam.wifidirect.WifiDirectCore;
import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.chat.PingPongerChief;
import com.nlt.mobileteam.wifidirect.controller.chat.callback.PingPongerCallBack;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;

import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_PING;

public class PingPonger implements Runnable {

    private static final String TAG = PingPonger.class.getSimpleName();
    private static final boolean VERBOSE = false;

    private int delay = 1600; //1.6 sec ping Delay
    int delayRate = HEALTH_RATE;
    private static final int HEALTH_RATE = 3; // need to be configured within 3-5, desirable value is 3
    private final CountDownLatch countDownLatch;
    private int health = HEALTH_RATE;

    private PingPongerChief pingPongerChief;
    int indexToInsert = -1;
    private Object syncObject = new Object();
    private Thread thread;

    private boolean interrupted;
    private int count = 0;


    PingPonger(Socket clientSocket, WiFiP2pService deviceToConnect) {
        this.countDownLatch = new CountDownLatch(1);
        initPingPongerChief(clientSocket, deviceToConnect);
        startRunnable(this.pingPongerChief, "PingPongerChief Chat Manager Thread");
    }

    private void initPingPongerChief(final Socket clientSocket, final WiFiP2pService deviceToConnect) {
        this.pingPongerChief = new PingPongerChief(clientSocket, deviceToConnect, new PingPongerCallBack() {
            @Override
            public void processMessage(int count) {
                if (VERBOSE) {
                    Log.v(TAG, "processMessage(MESSAGE_PONG) health = " + health + " Assistants count = " + count);
                }
                if (WifiDirectCore.cameraSessionInstanceCode == WifiDirectCore.INSTANCE_CODE_DIRECTOR_DDP) {
                    health = HEALTH_RATE; //re-init health in soft mode to avoid ping issue due to file transmission etc.
                } else {
                    health = HEALTH_RATE + 2;
                }
                if (countDownLatch.getCount() > 0) {
                    if (VERBOSE) Log.i(TAG, "before countDownLatch.countDown()");
                    countDownLatch.countDown();
                    if (VERBOSE) Log.i(TAG, "after countDownLatch.countDown()");
                }
            }
        });
        indexToInsert = CommunicationController.get().addPingPongChatManager(this.pingPongerChief);
        Log.i(TAG, "initPingPongerChief, indexToInsert = " + indexToInsert);
    }

    private void checkHealth() {
        if (health <= 0) {
            release();
        }
    }

    public void release() {
        interrupted = true;
        if (thread != null) {
            thread.interrupt();
            thread = null;
            Log.e(TAG, "dropConnection, Health is " + health);
            CommunicationController.get().removePingPongerManager(pingPongerChief);
            if (health == 0) {
                Log.e(TAG, "!!!!!!!!!!!!!PingPonger drop connection!!!!!!!!!!!!!");
            }
            pingPongerChief.closeSocketConnection();
            pingPongerChief = null;
        }
    }

    @Override
    public void run() {
        while (!interrupted) {
            health -= 1;
            checkHealth();
            count += 1;
            if (pingPongerChief != null && !pingPongerChief.isClosing) {
                pingPongerChief.write(MESSAGE_PING, count);
                synchronized (syncObject) {
                    try {
                        if (VERBOSE) Log.i(TAG, "before wait, delay = " + delay * delayRate);
                        syncObject.wait(delay * delayRate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                release();
            }
        }
    }

    private void startRunnable(Runnable runnable, String message) {
        thread = new Thread(runnable);
        thread.setName(message);
        thread.start();
    }

    CountDownLatch getPingerCountDownLatch() {
        return countDownLatch;
    }
}
