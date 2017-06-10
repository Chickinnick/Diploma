package com.nlt.mobileteam.wifidirect.controller.chat;

import android.os.Looper;
import android.util.Log;

import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.Message;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pAssistant;
import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorConnect;
import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorDisconnect;
import com.nlt.mobileteam.wifidirect.service.PeerBroadcastService;
import com.nlt.mobileteam.wifidirect.utils.Callback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_ABORT_VIDEO_SENDING;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_ASSISTANT_DISCONNECTING;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_HEADER_SENDING_VIDEO;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_PART_OF_VIDEO_SENDING;

public class ChatManagerAssistant extends ChatManager {

    protected static final String TAG = ChatManagerAssistant.class.getSimpleName();
    private static final boolean VERBOSE = false;

    private volatile boolean isClosing = false;

    private volatile VideoFilePartReader videoFilePartReader;

    private CountDownLatch syncFinishLatch = new CountDownLatch(1);

    public ChatManagerAssistant(Socket socket) {
        super(socket);
        Looper.prepare();
    }

    @Override
    public void run() {
        EventBus.getDefault().post(new DirectorConnect());


        try {
            while (!isInterrupted) {
                parseMessage(iStream.readInt());
            }
        } catch (IOException e) {
            if (VERBOSE) Log.e(TAG, "disconnected");
            if (!isClosing) {
                closeSocketConnection();
                EventBus.getDefault().post(new DirectorDisconnect());
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
            if (VERBOSE) Log.w(TAG, "sending close report to Director");
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
            case MESSAGE_DIRECTOR_DISCONNECTING: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_DIRECTOR_DISCONNECTING");
                EventBus.getDefault().post(new DirectorDisconnect());
                streamOpen = false;
                break;
            }
            case MESSAGE_GET_NEXT_VIDEO_PART: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_GET_NEXT_VIDEO_PART");
                processMessageGetNextVideoPart();
                break;
            }

            case MESSAGE_INIT_MEDIA_SOCKETS: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_INIT_MEDIA_SOCKETS");
                CommunicationController.get().initClientMediaManager();
                break;
            }
            case MESSAGE_START_PEER_BROADCAST_SERVICE: {
                if (VERBOSE) Log.w(TAG, "get START_PEER_BROADCAST_SERVICE");
                //PeerBroadcastService.setBroadcastStatus(PeerBroadcastService.START);
                break;
            }
            case MESSAGE_STOP_PEER_BROADCAST_SERVICE: {
                if (VERBOSE) Log.w(TAG, "get STOP_PEER_BROADCAST_SERVICE");
                PeerBroadcastService.setBroadcastStatus(PeerBroadcastService.STOP);
                WiFiP2pAssistant.get().stopDiscover();
                break;
            }

        }
    }

    private void processMessageGetNextVideoPart() {
        try {
            byte[] nextVideoPart = videoFilePartReader.getNextVideoPart();
            write(MESSAGE_PART_OF_VIDEO_SENDING, nextVideoPart.length, nextVideoPart);
            if (VERBOSE) Log.w(TAG, "send MESSAGE_PART_OF_VIDEO_SENDING + bytes");
        } catch ( NullPointerException e) {
            write(MESSAGE_ABORT_VIDEO_SENDING);
            if (VERBOSE) Log.w(TAG, "send MESSAGE_ABORT_VIDEO_SENDING cause: ", e);
        }
    }


    public void sendVideoHader(File file) {
      Log.w(TAG, "video File length: " + file.length());
        if (!file.exists() || file.length() == 0) {
                Log.e(TAG, "file with name doesn't exists or empty");
        }
        videoFilePartReader = new VideoFilePartReader(file);
        write(MESSAGE_HEADER_SENDING_VIDEO, file.length());
    }


}
