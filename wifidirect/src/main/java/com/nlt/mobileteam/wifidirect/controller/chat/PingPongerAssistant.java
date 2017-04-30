package com.nlt.mobileteam.wifidirect.controller.chat;

import android.util.Log;

import com.nlt.mobileteam.cinacore.BroadcastManager;
import com.nlt.mobileteam.wifidirect.controller.Message;
import com.nlt.mobileteam.wifidirect.utils.Callback;

import java.io.IOException;
import java.net.Socket;

import static com.nlt.mobileteam.cinacore.Action.COMM_DIRECTOR_DISCONNECTING;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_ASSISTANT_DISCONNECTING;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_PONG;

public class PingPongerAssistant extends ChatManager {

    protected static final String TAG = PingPongerAssistant.class.getSimpleName();
    private static final boolean VERBOSE = false;

    private volatile boolean isClosing = false;
    private int count = 0;

    public PingPongerAssistant(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted) {
                parseMessage(iStream.readInt());
            }
        } catch (IOException e) {
            if (VERBOSE) Log.w(TAG, "COMM_DIRECTOR_DISCONNECTING");
            if (!isClosing) {
                closeSocketConnection();
                BroadcastManager.get().send(COMM_DIRECTOR_DISCONNECTING);
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
        closeSocketConnection(null);
    }

    @Override
    public void closeSocketConnection(Callback callback) {
        isInterrupted = true;
        if (!isClosing) {
            isClosing = true;
            if (VERBOSE) Log.w(TAG, "sending MESSAGE_ASSISTANT_DISCONNECTING to Director");
            write(MESSAGE_ASSISTANT_DISCONNECTING);
            super.closeSocketConnection(callback);
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
            case MESSAGE_PING: {
                count += 1;
                int directorCount = iStream.readInt();
                if (VERBOSE) {
                    Log.w(TAG, "MESSAGE_PING got Director count = " +
                            directorCount + " Sending count = " + count);
                }
                write(MESSAGE_PONG, count);
                break;
            }
        }
    }
}
