package com.nlt.mobileteam.wifidirect.controller;

import android.util.Log;

import com.nlt.mobileteam.wifidirect.controller.chat.ChatManagerAssistant;
import com.nlt.mobileteam.wifidirect.controller.chat.ChatManagerChief;
import com.nlt.mobileteam.wifidirect.model.SyncDevice;

import java.util.ArrayList;

import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_START;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_SYNC_FINISHED;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_SYNC_FORECASTED;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_SYNC_SEND_BACK;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_SYNC_TIME_DIFF;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_UUID;

public class SyncController {
    private static SyncController instance;
    public static int devicesCount = 3;

    private SyncDevice syncDevice = new SyncDevice();

    public static SyncController get() {
        if (instance == null) {
            instance = new SyncController();
        }
        return instance;
    }

    public void testNetworkSpeed(ChatManagerChief chatManagerChief) {
        chatManagerChief.write(MESSAGE_SYNC_SEND_BACK, System.nanoTime());
        Log.w("SOCKET", "message sent" + MESSAGE_SYNC_SEND_BACK.toString());
    }

    public void sendNetworkSpeed(ChatManagerChief chatManagerChief) {
        chatManagerChief.write(
                MESSAGE_SYNC_TIME_DIFF,
                chatManagerChief.getSyncDevice().getNetworkSpeed(),
                System.nanoTime());
        Log.w("SOCKET", "message sent" + MESSAGE_SYNC_TIME_DIFF.toString());
    }

    public long getNetworkSpeed() {
        return syncDevice.getNetworkSpeed();
    }

    public void setNetworkSpeed(long networkSpeed) {
        this.syncDevice.setNetworkSpeed(networkSpeed);
    }

    public long getTimeDiff() {
        return syncDevice.getTimeDiff();
    }

    public void setTimeDiff(long timeDiff) {
        this.syncDevice.setTimeDiff(timeDiff);
    }

    public void forecastTime(ChatManagerAssistant chatManagerAssistant) {
        long forecastedTime = System.nanoTime() - syncDevice.getTimeDiff() +
                syncDevice.getNetworkSpeed();
        chatManagerAssistant.write(MESSAGE_SYNC_FORECASTED, forecastedTime);
        Log.w("SOCKET", "message sent" + MESSAGE_SYNC_FORECASTED.toString());
    }

    public void sendStartCommand(long recordPressed) {
        long launchTime = recordPressed + 3000000000l;
        CommunicationController.get().sendMessageToAll(MESSAGE_START, launchTime);
        Log.w("SOCKET", "Launch time");
    }

    public SyncDevice getSyncDevice() {
        return syncDevice;
    }

    public void sendSyncFinished(ChatManagerChief chatManagerChief) {
        chatManagerChief.write(MESSAGE_SYNC_FINISHED);
    }

    public void sendUuidCommand(String uuid) {
        CommunicationController.get().sendMessageToAll(MESSAGE_UUID, uuid);
        Log.w("SOCKET", "MESSAGE_UUID");
    }
}
