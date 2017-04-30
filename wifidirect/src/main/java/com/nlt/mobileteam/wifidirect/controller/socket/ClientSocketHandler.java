package com.nlt.mobileteam.wifidirect.controller.socket;

import android.util.Log;

import com.nlt.mobileteam.wifidirect.ConnectSocketException;
import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.chat.ChatManager;
import com.nlt.mobileteam.wifidirect.controller.chat.ChatManagerAssistant;
import com.nlt.mobileteam.wifidirect.controller.chat.MediaManager;
import com.nlt.mobileteam.wifidirect.controller.chat.PingPongerAssistant;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pAssistant;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.nlt.mobileteam.cinacore.CinaCoreModule.USE_PINGER;

public class ClientSocketHandler extends SocketHandler {
    protected final String TAG = this.getClass().getSimpleName();

    protected static final int CONNECTION_ATTEMPTS = 10;
    protected static final int CONNECTION_RETRY_TIMEOUT = 1000;
    protected static final int CONNECTION_TIMEOUT = 60 * 60 * 1000;
    public static final String CONNECTION_SOCKET_EXCEPTION_MASSAGE = "Max number of socket connection attempts reached";

    protected volatile InetAddress inetAddress;
    private Lock lock = new ReentrantLock();

    public ClientSocketHandler(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
        setName("Socket Handler Thread");
        setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                if (throwable.getMessage().equals(ClientSocketHandler.CONNECTION_SOCKET_EXCEPTION_MASSAGE)) {
                    try {
                        Log.e(TAG, throwable.getMessage());
                        throwable.printStackTrace();
                        //try re-init connection flow after maxNumber attempts reached
                        WiFiP2pAssistant.get().closeConnection();
                        CommunicationController.get().closeConnections();
                        WiFiP2pAssistant.get().prepareService();
                    } finally {
                        interrupt();
                    }
                }
            }
        });
        start();
    }

    protected void startChatManager(ChatManager chat, String name) {
        Thread thread = new Thread(chat);
        thread.setName(name);
        thread.start();
    }

    protected void connect(Socket socket, InetSocketAddress address) throws IOException {
        socket.setSoTimeout(CONNECTION_TIMEOUT);
        socket.setReuseAddress(true);
        socket.bind(null);
        socket.connect(address, CONNECTION_TIMEOUT);
    }

    protected void waitBeforeTryAgain() throws InterruptedException {
        synchronized (this) {
            this.wait(CONNECTION_RETRY_TIMEOUT);
        }
    }

    protected void setupCommandSocket() {
        Log.w("SOCKET", "client socket launched");
        try {
            if (USE_PINGER) {
                InetSocketAddress pingPongAddress = new InetSocketAddress(getInetAddress().getHostAddress(), SERVER_PING_PONG_PORT);
                Socket pingPongSocket = getConnectedSocket(pingPongAddress);
                Log.v(TAG, "pingPong connected to " + pingPongAddress.toString());

                PingPongerAssistant pingPongChat = new PingPongerAssistant(pingPongSocket);
                startChatManager(pingPongChat, "PingPongChat Thread");
                CommunicationController.get().addPingPongChatManager(pingPongChat);
            }
            InetSocketAddress address = new InetSocketAddress(getInetAddress().getHostAddress(), SERVER_COMMAND_PORT);

            Socket socket = getConnectedSocket(address);
            Log.w(TAG, "connected to " + address.toString());

            ChatManager chat = new ChatManagerAssistant(socket);
            startChatManager(chat, "Chat Manager Thread");
            CommunicationController.get().addMessageChatManager(chat);

            WiFiP2pAssistant.get().setInGroup(true);
            WiFiP2pAssistant.get().broadcastOwnerName();
        } catch (IOException e) {
            throw new ConnectSocketException(CONNECTION_SOCKET_EXCEPTION_MASSAGE, e); //use massage constant only
        } catch (InterruptedException e) {
            e.printStackTrace();
            WiFiP2pAssistant.get().closeConnection();
        }
    }

    /**
     * @return connected socket or throws {@link SocketException}
     * @throws SocketException      if an error occurs while connecting
     * @throws InterruptedException if the one was thrown during waiting next attempt to connect
     * @see #waitBeforeTryAgain()
     */
    protected Socket getConnectedSocket(InetSocketAddress address) throws IOException, InterruptedException {
        Socket socket = null;
        for (int i = 0; i < CONNECTION_ATTEMPTS; ++i) {
            try {
                socket = new Socket();
                connect(socket, address);
                return socket;

            } catch (ConnectException e) {
                Log.w(TAG, "connection failed", e);
                waitBeforeTryAgain();
            } catch (SocketException socEx) {
                if (!socEx.getMessage().contains("connected")) {
                    closeSocket(socket);
                    throw socEx;
                } else {
                    return socket;
                }
            }
        }

        throw new SocketException("Connection attempts have ended.");
    }

    private InetAddress getInetAddress() {
        return inetAddress;
    }

    public MediaManager setupMediaSockets() {
        Socket videoSocket = null;
        Socket audioSocket = null;
        try {
            InetSocketAddress videoAddress = new InetSocketAddress(inetAddress, SERVER_VIDEO_PORT);
            InetSocketAddress audioAddress = new InetSocketAddress(inetAddress, SERVER_AUDIO_PORT);

            videoSocket = getConnectedSocket(videoAddress);
            audioSocket = getConnectedSocket(audioAddress);

            return new MediaManager(videoSocket, audioSocket);
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Now you have to implement Rollback!!!", e);
            closeSocket(videoSocket);
            closeSocket(audioSocket); // audioSocket always is null under catch block - Just for future potential memory leaks after refactoring!!!

            return null;
        }
    }

    private void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            Log.v(TAG, "before lock.tryLock()");
            if (lock.tryLock()) {
                Log.w(TAG, "before setupCommandSocket, checking for connected assistant");
                if (CommunicationController.get().isNoAssistantsConnected()) {
                    Log.w(TAG, "no connected assistant, setupCommandSocket");
                    setupCommandSocket();
                    CommunicationController.get().setClientSocketHandler(this);
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
