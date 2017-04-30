package com.nlt.mobileteam.wifidirect.controller.socket;

import android.util.Log;

import com.nlt.mobileteam.wifidirect.ConnectSocketException;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.nlt.mobileteam.cinacore.CinaCoreModule.USE_PINGER;

public abstract class AbstractGroupOwnerSocketHandler extends SocketHandler {
    protected static final String TAG = AbstractGroupOwnerSocketHandler.class.getSimpleName();

    protected final static int THREAD_COUNT = 10;
    protected final static int SOCKET_TIMEOUT_LONG = 30 * 1000;

    protected static final int STATE_ACCEPTING_CONN = 0x100 + 1;
    protected static final int STATE_WAITING = 0x100 + 2;
    private static final boolean VERBOSE = false;

    protected Map<Integer, ServerSocket> serverSocketMap = new ConcurrentHashMap<>();
    protected volatile int state = STATE_WAITING;

    protected Queue<WiFiP2pService> connectingServices = new LinkedBlockingQueue<>();
    protected volatile boolean isFinalizing = false;


    public AbstractGroupOwnerSocketHandler() {
        setName("Socket Handler Thread");
        start();
        Log.w(TAG, "Group owner socket handler created");
    }

    /**
     * A ThreadPool for client sockets.
     */
    protected final ThreadPoolExecutor pool =
            new ThreadPoolExecutor(
                    THREAD_COUNT,
                    THREAD_COUNT,
                    10,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());

    @Override
    public void run() {
        Log.w(TAG, "group owner commandServerSocket launched");

        while (!isFinalizing) {

            switch (state) {
                case STATE_ACCEPTING_CONN: {
                    Log.w(TAG, "STATE_ACCEPTING_CONN");
                    processConnecting();
                    break;
                }
                case STATE_WAITING: {
                    Log.w(TAG, "STATE_WAITING");
                    waitingForConnection();
                    break;
                }
            }
        }

        closeServerSockets();

        Log.w(TAG, "exiting connection loop");
    }

    protected void waitingForConnection() {
        if (connectingServices.size() > 0) {
            state = STATE_ACCEPTING_CONN;
            return;
        }

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                if (VERBOSE) Log.e(TAG, "Waiting for connection interrupted cause: ", e);
                isFinalizing = true;
            }
        }
    }

    protected abstract void processConnecting();

    protected int processCommandSocketConnecting() {
        WiFiP2pService deviceToConnect = pollDeviceToConnect();
        int result = -1;
        boolean success = false;

        if (deviceToConnect != null) {

            Socket clientSocket = null;
            Socket pingPongClientSocket = null;
            ServerSocket serverSocket = null;
            ServerSocket pingPongSocket = null;
            try {
                if (USE_PINGER) {
                    pingPongSocket = getOpenServerSocket(SERVER_PING_PONG_PORT);
                    pingPongSocket.setSoTimeout(SOCKET_TIMEOUT_LONG);
                    pingPongClientSocket = pingPongSocket.accept();
                }

                serverSocket = getOpenServerSocket(SERVER_COMMAND_PORT);
                serverSocket.setSoTimeout(SOCKET_TIMEOUT_LONG);
                clientSocket = serverSocket.accept();
                success = true;
            } catch (InterruptedIOException e) {
                Log.w(TAG, "interrupted by commandServerSocket timeout");
            } catch (IOException e) {
                Log.e(TAG, "skipping to wait state case: ", e);
                state = STATE_WAITING;
            } finally {
                closeSocket(pingPongSocket);
                closeSocket(serverSocket);
            }
            if (success) {
                if (USE_PINGER) {
                    PingPonger pingPonger = new PingPonger(pingPongClientSocket, deviceToConnect);
                    startRunnable(pingPonger, "PingPonger Thread");
                    try {
                        pingPonger.getPingerCountDownLatch().await();
                        result = launchChatManager(clientSocket, deviceToConnect, pingPonger.indexToInsert);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pingPonger.delayRate = 1;
                } else {
                    result = launchChatManager(clientSocket, deviceToConnect, -1);
                    Log.w(TAG, "not using pinger!!! ");
                }

            } else {
                closeSocket(pingPongSocket);
                closeSocket(clientSocket);
            }

        } else {
            state = STATE_WAITING;
        }
        if (!success) {
            throw new ConnectSocketException("Connection Socket Failed");
        }
        Log.e(TAG, "resultIndex = " + result);
        return result;
    }

    private Thread startRunnable(PingPonger pingPonger, String threadName) {
        Thread thread = new Thread(pingPonger);
        thread.setName(threadName);
        thread.start();
        return thread;
    }

    protected abstract int launchChatManager(Socket clientSocket, WiFiP2pService deviceToConnect, int indexToInsert);

    protected WiFiP2pService pollDeviceToConnect() {
        Log.w(TAG, "connecting queue.size = " + connectingServices.size());

        WiFiP2pService deviceToConnect = connectingServices.poll();
        if (deviceToConnect != null) {
            Log.w(TAG, "device polled: " + deviceToConnect.device.deviceName);
        }
        return deviceToConnect;
    }

    public void setState(int state) {
        this.state = state;
        switch (state) {
            case STATE_ACCEPTING_CONN: {
                synchronized (this) {
                    this.notify();
                }
                break;
            }
        }
    }

    public void stopServerSocketHandler() {
        isFinalizing = true;
        closeServerSockets();
        pool.shutdownNow();
    }

    protected ServerSocket getOpenServerSocket(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port));
        serverSocketMap.put(port, serverSocket);
        return serverSocket;
    }

    protected void closeServerSockets() {
        for (ServerSocket serverSocket : serverSocketMap.values()) {
            closeSocket(serverSocket);
        }
    }

    public void setConnectingService(WiFiP2pService connectingService) {
        synchronized (this) {
            Log.w(TAG, "adding service to connect queue " + connectingService.device.deviceName);
            state = STATE_ACCEPTING_CONN;
            this.connectingServices.add(connectingService);
            this.notify();
        }
    }

}
