package com.nlt.mobileteam.wifidirect.controller.chat;


import com.nlt.mobileteam.wifidirect.controller.socket.MediaStreamsHandler;
import com.nlt.mobileteam.wifidirect.utils.SocketUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MediaManager implements MediaStreamsHandler {
    private static final String TAG = MediaManager.class.getSimpleName();

    private Socket videoSocket;
    private Socket audioSocket;

    public MediaManager(Socket videoSocket, Socket audioSocket) {
        this.videoSocket = videoSocket;
        this.audioSocket = audioSocket;
        setupKeepAlive(audioSocket, videoSocket);
    }

    private void setupKeepAlive(Socket videoSocket, Socket audioSocket) {
        SocketUtil.trySetupKeepAliveOptions(videoSocket);
        SocketUtil.trySetupKeepAliveOptions(audioSocket);
    }

    @Override
    public InputStream getAudioInputStream() throws IOException {
        return audioSocket.getInputStream();
    }

    @Override
    public InputStream getVideoInputStream() throws IOException {
        return videoSocket.getInputStream();
    }

    @Override
    public OutputStream getAudioOutputStream() throws IOException {
        return audioSocket.getOutputStream();
    }

    @Override
    public OutputStream getVideoOutputStream() throws IOException {
        return videoSocket.getOutputStream();
    }

    public void closeSocketConnection() {
        try {
            videoSocket.close();
            audioSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaManager that = (MediaManager) o;

        if (videoSocket != null
                ? videoSocket.getRemoteSocketAddress() != null && !videoSocket.getRemoteSocketAddress().equals(that.videoSocket.getRemoteSocketAddress())
                : that.videoSocket != null)
            return false;
        return audioSocket != null
                ? audioSocket.getRemoteSocketAddress() != null && audioSocket.getRemoteSocketAddress().equals(that.audioSocket.getRemoteSocketAddress())
                : that.audioSocket == null;

    }

    @Override
    public int hashCode() {
        int result = videoSocket != null ? videoSocket.hashCode() : 0;
        result = 31 * result + (audioSocket != null ? audioSocket.hashCode() : 0);
        return result;
    }
}
