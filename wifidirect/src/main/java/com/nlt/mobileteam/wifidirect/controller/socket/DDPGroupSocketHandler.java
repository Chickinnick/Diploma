package com.nlt.mobileteam.wifidirect.controller.socket;

import android.util.Log;

import com.nlt.mobileteam.cinacore.BroadcastManager;
import com.nlt.mobileteam.cinacore.CinaCoreModule;
import com.nlt.mobileteam.wifidirect.ConnectSocketException;
import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.Message;
import com.nlt.mobileteam.wifidirect.controller.chat.ChatManager;
import com.nlt.mobileteam.wifidirect.controller.chat.ChatManagerChief;
import com.nlt.mobileteam.wifidirect.controller.chat.MediaManager;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;
import com.nlt.mobileteam.wifidirect.utils.exception.MessageControllerException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static com.nlt.mobileteam.cinacore.Action.COMM_ASSISTANT_CONNECTED;
import static com.nlt.mobileteam.cinacore.Action.COMM_DISABLE_REC;

public class DDPGroupSocketHandler extends AbstractGroupOwnerSocketHandler {
    private static final String TAG = DDPGroupSocketHandler.class.getSimpleName();
    private static final int ACCEPT_CONNECTION_ATTEMPT_LIMIT = 3;

    public DDPGroupSocketHandler() {
        super();
    }

    @Override
    protected void processConnecting() {
        try {
            int indexToInsert = processCommandSocketConnecting();
            processMediaSocketConnections(indexToInsert);
        } catch (ConnectSocketException e) {
            Log.e(TAG, "processCommandSocketConnecting() != success", e);
        }
    }

    @Override
    protected int launchChatManager(Socket clientSocket, WiFiP2pService deviceToConnect, int indexToInsert) {
        int deviceIndex = -1;
        Log.i(TAG, "launchChatManager, indexToInsert = " + indexToInsert);
        if (clientSocket != null) {
            BroadcastManager.get().send(COMM_DISABLE_REC);
            ChatManager manager = new ChatManagerChief(clientSocket, deviceToConnect);
            pool.execute(manager);

            if (CinaCoreModule.USE_PINGER && indexToInsert != -1) {
                deviceIndex = CommunicationController.get().addMessageChatManager(manager, indexToInsert);
            } else {
                deviceIndex = CommunicationController.get().addMessageChatManager(manager);
            }

            try {
                CommunicationController.get().sendMessage(deviceIndex, Message.MESSAGE_INIT_MEDIA_SOCKETS);
            } catch (MessageControllerException e) {
                //Connection Lost
                Log.e(TAG, "Connection With Assistant Lost, deviceIndex " + deviceIndex ,e);
                //Index will be corrected decreased while processing this message!!! (deviceIndex - 1 + 1) = deviceIndex
                CommunicationController.get().removeManagersAndCloseConnectionsByIndex(deviceIndex);
            }
        }
        return deviceIndex;
    }

    private void processMediaSocketConnections(int indexToInsert) {
        ServerSocket serverVideoSocket = null;
        ServerSocket serverAudioSocket = null;

        int deviceIndex = -1;
        try {
            serverVideoSocket = getOpenServerSocket(SERVER_VIDEO_PORT);
            serverVideoSocket.setSoTimeout(SOCKET_TIMEOUT_LONG);
            Socket clientVideoSocket = acceptConnection(serverVideoSocket);
            Log.i(TAG, "clientVideoSocket: " + clientVideoSocket.toString());

            serverAudioSocket = getOpenServerSocket(SERVER_AUDIO_PORT);
            serverAudioSocket.setSoTimeout(SOCKET_TIMEOUT_LONG);
            Socket clientAudioSocket = acceptConnection(serverAudioSocket);
            Log.i(TAG, "clientAudioSocket: " + clientAudioSocket.toString());

            if (indexToInsert >= 0) {
                deviceIndex = CommunicationController
                        .get()
                        .addMediaManager(
                                new MediaManager(clientVideoSocket, clientAudioSocket), indexToInsert);
            } else {
                deviceIndex =
                        CommunicationController
                                .get()
                                .addMediaManager(
                                        new MediaManager(clientVideoSocket, clientAudioSocket));
            }

            BroadcastManager.get().sendInt(COMM_ASSISTANT_CONNECTED , deviceIndex);

        } catch (IOException e) {
            Log.e(TAG, "processMediaSocketConnections failed", e);
        } catch (MessageControllerException e) {
            //Connection Lost
            Log.e(TAG, "Connection With Assistant Lost, deviceIndex " + deviceIndex, e);
            //Index will be corrected (decreased) while processing this message!!! (index - 1 + 1) = index
            CommunicationController.get().removeManagersAndCloseConnectionsByIndex(deviceIndex);
        } finally {
            closeSocket(serverVideoSocket);
            closeSocket(serverAudioSocket);
        }
    }

    private Socket acceptConnection(ServerSocket serverSocket) throws IOException {
        int attempt = 0;
        while (true) {
            try {
                return serverSocket.accept();
            } catch (SocketTimeoutException e) {
                if (++attempt > ACCEPT_CONNECTION_ATTEMPT_LIMIT) {
                    throw e;
                }
            }
        }
    }
}
