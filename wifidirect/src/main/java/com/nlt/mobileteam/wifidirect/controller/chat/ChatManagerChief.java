package com.nlt.mobileteam.wifidirect.controller.chat;

import android.os.Environment;
import android.util.Log;

import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.Message;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pDirector;
import com.nlt.mobileteam.wifidirect.model.SyncDevice;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;
import com.nlt.mobileteam.wifidirect.model.event.director.NotifyDeviceList;
import com.nlt.mobileteam.wifidirect.utils.FileUtils;
import com.nlt.mobileteam.wifidirect.utils.exception.VideoFilePartReceiverException;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_ABORT_VIDEO_SENDING;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_DIRECTOR_DISCONNECTING;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_GET_NEXT_VIDEO_PART;

public class ChatManagerChief extends ChatManager {

    private static final String TAG = ChatManagerChief.class.getSimpleName();
    private static final boolean VERBOSE = true;

    private SyncDevice syncDevice;
    private WiFiP2pService connectedDevice;
    private volatile boolean isClosing = false;
    private VideoFilePartWriter videoFilePartWriter;

    public ChatManagerChief(Socket socket, WiFiP2pService connectedDevice) {
        super(socket);
        this.syncDevice = new SyncDevice();
        this.connectedDevice = connectedDevice;
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
            if (!isClosing || !isInterrupted) {
                closeSocketConnection();
                EventBus.getDefault().post(new NotifyDeviceList());
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
            if (VERBOSE)
                Log.e(TAG, "closeSocketConnection(), Director sending MESSAGE_DIRECTOR_DISCONNECTING");
            write(MESSAGE_DIRECTOR_DISCONNECTING);
            isClosing = true;

            super.closeSocketConnection();
        }
    }

    private void parseMessage(int message) throws IOException {
        Message command;
        try {
            command = Message.valueOfOrdinal(message);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message ", e);
            return;
        }

        switch (command) {

            case MESSAGE_ASSISTANT_DISCONNECTING: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_ASSISTANT_DISCONNECTING");
                processMessageAssistantDisconnecting();
                break;
            }
            case MESSAGE_HEADER_SENDING_VIDEO: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_HEADER_SENDING_VIDEO");
                processMessageHeaderSendingVideo(iStream.readLong());
                break;
            }
            case MESSAGE_PART_OF_VIDEO_SENDING: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_PART_OF_VIDEO_SENDING");
                processMessagePartOfVideoSending(iStream.readInt());
                break;
            }
            case MESSAGE_ABORT_VIDEO_SENDING: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_ABORT_VIDEO_SENDING");
                processMessageAbortVideoSending();
                break;
            }


        }
    }



    private int getDeviceIndex() {
        int index = CommunicationController.get().getIndexByChatManager(this);
        if (index >= 0) {
            //Correction is needed because index should be from 1 to 4
            index += 1;
        } else {
            index = connectedDevice.index;
        }
        return index;
    }



    private void processMessageAbortVideoSending() {
        if (videoFilePartWriter != null) {
            videoFilePartWriter.abort();
            videoFilePartWriter = null;
        }
    }

    private void processMessagePartOfVideoSending(int videoPartLength) {
        byte[] videoPart;

        try {
            videoPart = readBytes(videoPartLength);
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            if (VERBOSE)
                Log.e(TAG, "error receiving file part, send MESSAGE_ABORT_VIDEO_SENDING", e);
            write(MESSAGE_ABORT_VIDEO_SENDING);
            return;
        }

        if (videoPart == null) return;
        if (VERBOSE)
            Log.w(TAG, "Video path received, length: " + videoPartLength +
                    " sender: " + connectedDevice.device.deviceName);

        try {
            videoFilePartWriter.addVideoPart(videoPart);
        } catch (VideoFilePartReceiverException | NullPointerException e) {
            if (VERBOSE) Log.e(TAG, "send MESSAGE_ABORT_VIDEO_SENDING cause ", e);
            write(MESSAGE_ABORT_VIDEO_SENDING);
            return;
        }

        if (!videoFilePartWriter.isAllVideoPartReceived()) {
            write(MESSAGE_GET_NEXT_VIDEO_PART);
            if (VERBOSE) Log.w(TAG, "send MESSAGE_GET_NEXT_VIDEO_PART");
        }
    }

    private byte[] readBytes(int length) throws IOException {
        byte[] bytes = new byte[length];

        int progress = 0;

        while (progress < length) {
            int expectedBytesLength = length - progress;
            progress += iStream.read(bytes, progress, expectedBytesLength);
        }

        return bytes;
    }

    private void processMessageHeaderSendingVideo(long videoFileLength) {
        if (VERBOSE)
            Log.w(TAG, "file length from ass " + connectedDevice.device.deviceName + " " + videoFileLength);

        File assistantVideo = new File(Environment.getExternalStorageDirectory() + File.separator + "wifi");
        videoFilePartWriter = new VideoFilePartWriter(assistantVideo, videoFileLength, connectedDevice.index);
        write(MESSAGE_GET_NEXT_VIDEO_PART);
    }

    private void processMessageAssistantDisconnecting() {
        if (VERBOSE) Log.e(TAG, "processMessageAssistantDisconnecting()");
        WiFiP2pDirector.get().removeDevice(connectedDevice);
        if (!isClosing) {
            EventBus.getDefault().post(new NotifyDeviceList());
            super.closeSocketConnection();

            int index = CommunicationController.get().getIndexByChatManager(this);
            if (index >= 0) {
                CommunicationController.get().removeManagersAndCloseConnectionsByIndex(index);
            }
            isClosing = true;
        }
    }



    public void sendSessionInfo(Message message, String projectInfo) {
        write(message, projectInfo, (connectedDevice.index + 1));
    }
}
