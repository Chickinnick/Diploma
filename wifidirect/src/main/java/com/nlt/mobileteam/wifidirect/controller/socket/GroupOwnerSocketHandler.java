package com.nlt.mobileteam.wifidirect.controller.socket;

import android.util.Log;

import com.nlt.mobileteam.cinacore.BroadcastManager;
import com.nlt.mobileteam.wifidirect.ConnectSocketException;
import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.chat.ChatManager;
import com.nlt.mobileteam.wifidirect.controller.chat.ChatManagerChief;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;

import java.net.Socket;

import static com.nlt.mobileteam.cinacore.Action.COMM_DISABLE_REC;

public class GroupOwnerSocketHandler extends AbstractGroupOwnerSocketHandler {
    private static final String TAG = GroupOwnerSocketHandler.class.getSimpleName();

    @Override
    protected void processConnecting() {
        try {
            processCommandSocketConnecting();
        } catch (ConnectSocketException e) {
            Log.e(TAG, "processCommandSocketConnecting() != success", e);
        }
    }

    @Override
    protected int launchChatManager(Socket clientSocket, WiFiP2pService deviceToConnect, int indexToInsert) {
        int result = -1;
        if (clientSocket != null) {
            BroadcastManager.get().send(COMM_DISABLE_REC);
            ChatManager manager = new ChatManagerChief(clientSocket, deviceToConnect);
            pool.execute(manager);

            if (indexToInsert >= 0) {
                result = CommunicationController.get().addMessageChatManager(manager, indexToInsert);
            } else {
                result = CommunicationController.get().addMessageChatManager(manager);
            }
        }
        return result;
    }
}
