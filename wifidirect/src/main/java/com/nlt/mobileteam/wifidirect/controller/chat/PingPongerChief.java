package com.nlt.mobileteam.wifidirect.controller.chat;

import android.util.Log;

import com.nlt.mobileteam.cinacore.BroadcastManager;
import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.Message;
import com.nlt.mobileteam.wifidirect.controller.chat.callback.PingPongerCallBack;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pDirector;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;

import java.io.IOException;
import java.net.Socket;

import static com.nlt.mobileteam.cinacore.Action.COMM_NOTIFY_DEVICE_LIST;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_DIRECTOR_DISCONNECTING;

public class PingPongerChief extends ChatManager {

    protected static final String TAG = PingPongerChief.class.getSimpleName();
    private static final boolean VERBOSE = false;

    private WiFiP2pService connectedDevice;
    public volatile boolean isClosing = false;
    private PingPongerCallBack callBack;

    public PingPongerChief(Socket socket, WiFiP2pService connectedDevice, PingPongerCallBack callBack) {
        super(socket);
        this.connectedDevice = connectedDevice;
        this.callBack = callBack;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted) {
                parseMessage(iStream.readInt());
            }
        } catch (IOException e) {
            if (VERBOSE) Log.e(TAG, "WiFiP2pDirector.get().removeDevice(connectedDevice)");

            WiFiP2pDirector.get().removeDevice(connectedDevice);
            if (!isClosing) {
                closeSocketConnection();
                BroadcastManager.get().send(COMM_NOTIFY_DEVICE_LIST);
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeSocketConnection() {
        isInterrupted = true;
        if (!isClosing) {
            isClosing = true;
            if (VERBOSE)
                Log.w(TAG, "Director sending MESSAGE_DIRECTOR_DISCONNECTING to Assistant");
            write(MESSAGE_DIRECTOR_DISCONNECTING);
            processMessageAssistantDisconnecting();
        }
    }

    private void parseMessage(int message) throws IOException {
        Message command;
        try {
            command = Message.valueOfOrdinal(message);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message ");
            return;
        }

        switch (command) {
            case MESSAGE_PONG: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_PONG");
                processMessagePong(iStream.readInt());
                break;
            }
        }
    }

    private void processMessagePong(int count) {
        if (VERBOSE) Log.w(TAG, "callBack != null = " + (callBack != null));
        if (callBack != null) {
            callBack.processMessage(count);
        }
    }

    private void processMessageAssistantDisconnecting() {
        if (VERBOSE) Log.e(TAG, "processMessageAssistantDisconnecting()");
        WiFiP2pDirector.get().removeDevice(connectedDevice);
        BroadcastManager.get().send(COMM_NOTIFY_DEVICE_LIST);

        if (VERBOSE)
            Log.i(TAG, "processMessageAssistantDisconnecting removeMediaManager for index = " +
                    (connectedDevice.index - 1));

        int index = CommunicationController.get().getIndexByChatManager(this);
        if (index >= 0) {
            CommunicationController.get().removeManagersAndCloseConnectionsByIndex(index);
        }
        super.closeSocketConnection();
    }
}
