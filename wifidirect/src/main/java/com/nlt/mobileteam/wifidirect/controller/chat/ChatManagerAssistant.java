package com.nlt.mobileteam.wifidirect.controller.chat;

import android.os.Looper;
import android.util.Log;

import com.nlt.mobileteam.cinacore.BroadcastManager;
import com.nlt.mobileteam.cinacore.exception.VideoFilePartReaderException;
import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.Message;
import com.nlt.mobileteam.wifidirect.controller.SyncController;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pAssistant;
import com.nlt.mobileteam.wifidirect.service.PeerBroadcastService;
import com.nlt.mobileteam.wifidirect.utils.Callback;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import static com.nlt.mobileteam.cinacore.Action.COMM_ABORT_VIDEO_SENDING;
import static com.nlt.mobileteam.cinacore.Action.COMM_ASSISTANT_INIT_STREAMER;
import static com.nlt.mobileteam.cinacore.Action.COMM_ASSISTANT_START_CINA_STREAM;
import static com.nlt.mobileteam.cinacore.Action.COMM_ASSISTANT_START_SOCIAL_STREAM;
import static com.nlt.mobileteam.cinacore.Action.COMM_ASSISTANT_STOP_CINA_STREAM;
import static com.nlt.mobileteam.cinacore.Action.COMM_ASSISTANT_STOP_SOCIAL_STREAM;
import static com.nlt.mobileteam.cinacore.Action.COMM_CHANGE_CONTROL;
import static com.nlt.mobileteam.cinacore.Action.COMM_CHAT_MESSAGE;
import static com.nlt.mobileteam.cinacore.Action.COMM_DELETE_RECORDED;
import static com.nlt.mobileteam.cinacore.Action.COMM_DIRECTOR_CONNECTED;
import static com.nlt.mobileteam.cinacore.Action.COMM_DIRECTOR_DISCONNECTING;
import static com.nlt.mobileteam.cinacore.Action.COMM_OFF_AUDIO_STREAM;
import static com.nlt.mobileteam.cinacore.Action.COMM_ON_AUDIO_STREAM;
import static com.nlt.mobileteam.cinacore.Action.COMM_PROJ_INFO;
import static com.nlt.mobileteam.cinacore.Action.COMM_REC_START;
import static com.nlt.mobileteam.cinacore.Action.COMM_REC_STOP;
import static com.nlt.mobileteam.cinacore.Action.COMM_REQUEST_VIDEO;
import static com.nlt.mobileteam.cinacore.Action.COMM_SAVE_LOCAL;
import static com.nlt.mobileteam.cinacore.Action.COMM_SYNC_FINISHED;
import static com.nlt.mobileteam.cinacore.Action.COMM_UUID;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_ABORT_VIDEO_SENDING;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_ASSISTANT_DISCONNECTING;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_HEADER_SENDING_VIDEO;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_PART_OF_VIDEO_SENDING;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_READY;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_SYNC_SEND_BACK;

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
        write(MESSAGE_READY, WiFiP2pAssistant.get().getThisDevice().deviceName);
        BroadcastManager.get().send(COMM_DIRECTOR_CONNECTED);

        try {
            while (!isInterrupted) {
                parseMessage(iStream.readInt());
            }
        } catch (IOException e) {
            if (VERBOSE) Log.e(TAG, "disconnected");
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
            if (VERBOSE) Log.w(TAG, "sending close report to Director");
            //TODO: ADD and check: if (USE_PINGER) - DO NOT SEND THIS COMMAND!!!
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
            case MESSAGE_START: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_START");
                processMessageStart(iStream.readLong());
                break;
            }
            case MESSAGE_STOP: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_STOP");
                BroadcastManager.get().send(COMM_REC_STOP);
                break;
            }
            case MESSAGE_UUID: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_UUID");
                BroadcastManager.get().sendString(COMM_UUID, iStream.readUTF());
                break;
            }
            case MESSAGE_DELETE_RECORDED: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_DELETE_RECORDED");
                BroadcastManager.get().send(COMM_DELETE_RECORDED);
                break;
            }
            case MESSAGE_PROJ_INFO: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_PROJ_INFO");
                BroadcastManager.get()
                        .sendStringAndInt(COMM_PROJ_INFO, iStream.readUTF(), iStream.readInt());
                break;
            }
            case MESSAGE_SAVE_LOCAL: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_SAVE_LOCAL");
                BroadcastManager.get().send(COMM_SAVE_LOCAL);
                break;
            }
            case MESSAGE_SYNC_SEND_BACK: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_SYNC_SEND_BACK");
                write(MESSAGE_SYNC_SEND_BACK, iStream.readLong());
                break;
            }
            case MESSAGE_SYNC_TIME_DIFF: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_SYNC_TIME_DIFF");
                processMessageSyncTimeDiff(iStream.readLong(), iStream.readLong());
                break;
            }
            case MESSAGE_SYNC_FINISHED: {
                if (VERBOSE) Log.i(TAG, "MESSAGE_SYNC_FINISHED");
                syncFinishLatch.countDown();
                BroadcastManager.get().send(COMM_SYNC_FINISHED);
                break;
            }
            case MESSAGE_DIRECTOR_DISCONNECTING: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_DIRECTOR_DISCONNECTING");
                BroadcastManager.get().send(COMM_DIRECTOR_DISCONNECTING);

                streamOpen = false;
                break;
            }
            case MESSAGE_CLOSE_DIALOGS: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_CLOSE_DIALOGS");
                //DialogService.get().closeDialog();
                break;
            }
            case MESSAGE_REQUEST_VIDEO: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_REQUEST_VIDEO");
                processMessageRequestVideo();
                break;
            }
            case MESSAGE_GET_NEXT_VIDEO_PART: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_GET_NEXT_VIDEO_PART");
                processMessageGetNextVideoPart();
                break;
            }
            case MESSAGE_ABORT_VIDEO_SENDING: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_ABORT_VIDEO_SENDING");
                processMessageAbortVideoSending();
                BroadcastManager.get().send(COMM_ABORT_VIDEO_SENDING);
                break;
            }
            case MESSAGE_START_CINA_STREAM: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_START_CINA_STREAM");
                BroadcastManager.get().send(COMM_ASSISTANT_START_CINA_STREAM);
                break;
            }
            case MESSAGE_STOP_CINA_STREAM: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_STOP_CINA_STREAM");
                BroadcastManager.get().send(COMM_ASSISTANT_STOP_CINA_STREAM);
                break;
            }
            case MESSAGE_INIT_STREAMER: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_INIT_STREAMER");
                processMessageInitStreamer(iStream.readUTF());
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
            case MESSAGE_START_SOCIAL_STREAM: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_START_SOCIAL_STREAM");
                BroadcastManager.get()
                        .sendSerializable(COMM_ASSISTANT_START_SOCIAL_STREAM, iStream.readUTF());
                break;
            }
            case MESSAGE_STOP_SOCIAL_STREAM: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_STOP_SOCIAL_STREAM");
                BroadcastManager.get().sendSerializable(COMM_ASSISTANT_STOP_SOCIAL_STREAM, iStream.readUTF());
                break;
            }
            case MESSAGE_OFF_AUDIO_STREAM: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_OFF_AUDIO_STREAM");
                BroadcastManager.get().send(COMM_OFF_AUDIO_STREAM);
                break;
            }
            case MESSAGE_ON_AUDIO_STREAM: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_ON_AUDIO_STREAM");
                BroadcastManager.get().send(COMM_ON_AUDIO_STREAM);
                break;
            }
            case CONTROL_CHANGED: {
                if (VERBOSE) Log.w(TAG, "get CONTROL_CHANGED");
                BroadcastManager.get().sendString(COMM_CHANGE_CONTROL, iStream.readUTF());
                break;
            }
            case MESSAGE_CHAT_MESSAGE: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_CHAT");
                BroadcastManager.get().sendString(COMM_CHAT_MESSAGE, iStream.readUTF());
                break;
            }
        }
    }

    private void processMessageInitStreamer(final String qualityType) {
        Thread syncFinishWaiter = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    syncFinishLatch.await();
                    BroadcastManager.get().sendSerializable(COMM_ASSISTANT_INIT_STREAMER, qualityType);
                    Log.i(TAG, "processMessageInitStreamer out");
                } catch (InterruptedException e) {
                    Log.e("SyncFinishWaiter", "processMessageInitStreamer() failed", e);
                }
            }
        });
        syncFinishWaiter.setName("SyncFinishWaiter");
        syncFinishWaiter.start();
    }

    private void processMessageAbortVideoSending() {
        if (videoFilePartReader != null) {
            videoFilePartReader.abort();
            videoFilePartReader = null;
        }
    }

    private void processMessageGetNextVideoPart() {
        try {
            byte[] nextVideoPart = videoFilePartReader.getNextVideoPart();
            write(MESSAGE_PART_OF_VIDEO_SENDING, nextVideoPart.length, nextVideoPart);
            if (VERBOSE) Log.w(TAG, "send MESSAGE_PART_OF_VIDEO_SENDING + bytes");
        } catch (VideoFilePartReaderException | NullPointerException e) {
            write(MESSAGE_ABORT_VIDEO_SENDING);
            if (VERBOSE) Log.w(TAG, "send MESSAGE_ABORT_VIDEO_SENDING cause: ", e);
        }
    }

    private void processMessageRequestVideo() {
        BroadcastManager.get().send(COMM_REQUEST_VIDEO);
    }

    public void sendVideoHader(String recordedVideoPath) {
        File recordedVideo = new File(recordedVideoPath);
        if (!recordedVideo.exists()) {
            if (VERBOSE)
                Log.e(TAG, "recorded file with name doesn't exists. exiting: " + recordedVideoPath);
            return;
        }

        if (VERBOSE) Log.w(TAG, "video File length: " + recordedVideo.length());
        videoFilePartReader = new VideoFilePartReader(recordedVideo);

        write(MESSAGE_HEADER_SENDING_VIDEO, recordedVideo.length());
    }

    private void processMessageSyncTimeDiff(long netSpeed, long receivedDate) {
        long timeDiff = System.nanoTime() - netSpeed - receivedDate;
        SyncController.get().setNetworkSpeed(netSpeed);
        SyncController.get().setTimeDiff(timeDiff);
        if (VERBOSE) Log.w("SOCKET", "net speed: " + netSpeed);
        if (VERBOSE) Log.w("SOCKET", "time diff: " + timeDiff);
        SyncController.get().forecastTime(this);
    }

    private void processMessageStart(long directorStartTime) {
        if (VERBOSE) Log.w(TAG, "MESSAGE_START");
        long launchTime = directorStartTime + SyncController.get().getTimeDiff();
        BroadcastManager.get().sendLong(COMM_REC_START, launchTime);
    }
}
