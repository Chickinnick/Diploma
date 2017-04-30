package com.nlt.mobileteam.wifidirect.controller.chat;

import android.support.annotation.NonNull;
import android.util.Log;

import com.nlt.mobileteam.cinacore.BroadcastManager;
import com.nlt.mobileteam.cinacore.model.DeviceInfo;
import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.Message;
import com.nlt.mobileteam.wifidirect.controller.SyncController;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pDirector;
import com.nlt.mobileteam.wifidirect.model.SyncDevice;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;
import com.nlt.mobileteam.wifidirect.utils.FileUtils;
import com.nlt.mobileteam.wifidirect.utils.exception.VideoFilePartReceiverException;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import static com.nlt.mobileteam.cinacore.Action.CAM_ASS_CHANGE_CONTROLS;
import static com.nlt.mobileteam.cinacore.Action.CAM_ASS_CHANGE_DEVICE_INFO;
import static com.nlt.mobileteam.cinacore.Action.CAM_ASS_INIT_CONTROLS;
import static com.nlt.mobileteam.cinacore.Action.COMM_DIRECTOR_RECEIVED_MEDIA_FORMAT;
import static com.nlt.mobileteam.cinacore.Action.COMM_DISABLE_REC;
import static com.nlt.mobileteam.cinacore.Action.COMM_ENABLE_REC;
import static com.nlt.mobileteam.cinacore.Action.COMM_NOTIFY_DEVICE_LIST;
import static com.nlt.mobileteam.cinacore.Action.COMM_PROJECT_VIDEO_RECEIVE_ABORTED;
import static com.nlt.mobileteam.cinacore.Action.GOPRO_NOT_CONNECTED_ACTION;
import static com.nlt.mobileteam.cinacore.Action.GOPRO_NO_VIDEO_TO_SENT;
import static com.nlt.mobileteam.cinacore.Action.SONY_DISCONNECT_ACTION;
import static com.nlt.mobileteam.cinacore.Action.SONY_RECONNECT_ACTION;
import static com.nlt.mobileteam.cinacore.serializable.SerializableUtils.gsonFromJson;
import static com.nlt.mobileteam.cinacore.serializable.SerializableUtils.gsonToJson;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_ABORT_VIDEO_SENDING;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_DIRECTOR_DISCONNECTING;
import static com.nlt.mobileteam.wifidirect.controller.Message.MESSAGE_GET_NEXT_VIDEO_PART;

public class ChatManagerChief extends ChatManager {

    private static final String TAG = ChatManagerChief.class.getSimpleName();
    private static final boolean VERBOSE = false;

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
            Log.e(TAG, "Error parsing message ");
            return;
        }

        switch (command) {
            case MESSAGE_READY: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_READY");
                processMessageReady(iStream.readUTF());
                break;
            }
            case MESSAGE_SYNC_SEND_BACK: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_SYNC_SEND_BACK");
                processMessageSyncSendBack(iStream.readLong());
                break;
            }
            case MESSAGE_SYNC_FORECASTED: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_SYNC_FORECASTED");
                processMessageSynForecasted(iStream.readLong());
                break;
            }
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
            case MESSAGE_MEDIA_FORMAT: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_MEDIA_FORMAT");
                processMessageMediaFormat(iStream.readInt());
                break;
            }
            case MESSAGE_CAM_INIT_CONTROLS: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_CAM_INIT_CONTROLS");
                processMessageInitCamControls(iStream.readInt());

                break;
            }
            case MESSAGE_DIRECTOR_CHANGE_CONTROL: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_DIRECTOR_CHANGE_CONTROL");
                String deviceInfo = prepareDeviceInfo();
                BroadcastManager.get().sendStrings(CAM_ASS_CHANGE_CONTROLS, iStream.readUTF(), deviceInfo);
                break;
            }
            case MESSAGE_DIRECTOR_UPDATE_DEVICE_INFO: {
                if (VERBOSE) Log.w(TAG, "get MESSAGE_DIRECTOR_UPDATE_DEVICE_INFO");

                DeviceInfo deviceInfo = gsonFromJson(iStream.readUTF(), DeviceInfo.class);
                int index = getDeviceIndex();

                deviceInfo.setIndex(index);
                deviceInfo.setName(connectedDevice.device.deviceName);
                BroadcastManager.get().sendStrings(CAM_ASS_CHANGE_DEVICE_INFO, " ", gsonToJson(deviceInfo));
                break;
            }
            case MESSAGE_SONY_DISCONNECT: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_SONY_DISCONNECT");
                int deviceIndex = CommunicationController.get().getIndexByChatManager(this);
                BroadcastManager.get().sendInt(SONY_DISCONNECT_ACTION, deviceIndex);
                break;
            }
            case MESSAGE_SONY_RECONNECT: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_SONY_RECONNECT");
                int deviceIndex = CommunicationController.get().getIndexByChatManager(this);
                BroadcastManager.get().sendInt(SONY_RECONNECT_ACTION, deviceIndex);
                break;
            }
            case MESSAGE_GOPRO_NOT_CONNECTED: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_GOPRO_NOT_CONNECTED");
                BroadcastManager.get().send(GOPRO_NOT_CONNECTED_ACTION);
                break;
            }

            case MESSAGE_GOPRO_NO_VIDEO_TO_SENT: {
                if (VERBOSE) Log.w(TAG, "MESSAGE_GOPRO_NO_VIDEO_TO_SENT");
                BroadcastManager.get().send(GOPRO_NO_VIDEO_TO_SENT);
                break;
            }

            case MESSAGE_GOPRO_VIDEO_RECEIVE_ABORTED: {
                int deviceIndex = CommunicationController.get().getIndexByChatManager(this);
                BroadcastManager.get().sendInt(COMM_PROJECT_VIDEO_RECEIVE_ABORTED, deviceIndex);
                break;
            }
        }
    }

    private void processMessageInitCamControls(int length) {
        byte[] bytes;
        try {
            bytes = readBytes(length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String device = new String(bytes);
        String deviceInfo = prepareDeviceInfo();
        BroadcastManager.get().sendStrings(CAM_ASS_INIT_CONTROLS, device, deviceInfo);
    }

    @NonNull
    private String prepareDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIndex(getDeviceIndex());

        return gsonToJson(deviceInfo);
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

    private void processMessageMediaFormat(int length) {
        byte[] bytes;
        try {
            bytes = readBytes(length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int index = CommunicationController.get().getIndexByChatManager(this);
        if (index >= 0) {
            if (VERBOSE)
                Log.w(TAG, "sending COMM_DIRECTOR_RECEIVED_MEDIA_FORMAT, index = " + index);
            //correction index +1 is needed because DDP Director corrects index by decreasing it!!! (onReceive, inside process method).
            BroadcastManager.get().sendSerializableWithInt(COMM_DIRECTOR_RECEIVED_MEDIA_FORMAT, bytes, index + 1);
        } else {
            if (VERBOSE)
                Log.e(TAG, "COMM_DIRECTOR_RECEIVED_MEDIA_FORMAT, OLD WAY getting device index!!!, device.index = " + connectedDevice.index);
            BroadcastManager.get().sendSerializableWithInt(COMM_DIRECTOR_RECEIVED_MEDIA_FORMAT, bytes, connectedDevice.index);
        }
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

        File assistantVideo = new File(FileUtils.getFileNameForAssistant(connectedDevice));
        videoFilePartWriter = new VideoFilePartWriter(assistantVideo, videoFileLength, connectedDevice.index);
        write(MESSAGE_GET_NEXT_VIDEO_PART);
    }

    private void processMessageAssistantDisconnecting() {
        if (VERBOSE) Log.e(TAG, "processMessageAssistantDisconnecting()");
        WiFiP2pDirector.get().removeDevice(connectedDevice);
        if (!isClosing) {
            BroadcastManager.get().send(COMM_NOTIFY_DEVICE_LIST);
            super.closeSocketConnection();

            int index = CommunicationController.get().getIndexByChatManager(this);
            if (index >= 0) {
                CommunicationController.get().removeManagersAndCloseConnectionsByIndex(index);
            }
            isClosing = true;
        }
    }

    private void processMessageSynForecasted(long startTime) {
        long forecastedDiff = System.nanoTime() - startTime;
        if (VERBOSE) Log.w(TAG, "forecastedDiff: " + Math.abs(forecastedDiff));

        if (Math.abs(forecastedDiff) >= 40000000) {
            if (VERBOSE) Log.w(TAG, "forecastedDiff is > 40 ms");
            SyncController.get().testNetworkSpeed(this);
        } else if (Math.abs(forecastedDiff) < 40000000) {
            SyncController.get().sendSyncFinished(this);
            BroadcastManager.get().send(COMM_ENABLE_REC);
            connectedDevice.status = WiFiP2pService.SYNCRONIZED;
            BroadcastManager.get().send(COMM_NOTIFY_DEVICE_LIST);
            if (VERBOSE) Log.w(TAG, "sync with " + connectedDevice.device.deviceName + " FINISHED");
        }
    }

    private void processMessageSyncSendBack(long startTime) {
        syncDevice.setNetworkSpeed((System.nanoTime() - startTime) / 2);
        if (VERBOSE) Log.w(TAG, "network speed: " + syncDevice.getNetworkSpeed());
        SyncController.get().sendNetworkSpeed(this);
    }

    private void processMessageReady(String deviceName) {
        if (WiFiP2pDirector.get().contains(deviceName)) {
            BroadcastManager.get().send(COMM_DISABLE_REC);
            SyncController.get().testNetworkSpeed(this);
        } else {
            super.closeSocketConnection();
        }
    }

    public SyncDevice getSyncDevice() {
        return syncDevice;
    }

    public void sendSessionInfo(Message message, String projectInfo) {
        write(message, projectInfo, (connectedDevice.index + 1));
    }
}
