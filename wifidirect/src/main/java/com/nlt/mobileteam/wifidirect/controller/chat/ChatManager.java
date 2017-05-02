package com.nlt.mobileteam.wifidirect.controller.chat;

import android.util.Log;

import com.nlt.mobileteam.wifidirect.controller.CommunicationController;
import com.nlt.mobileteam.wifidirect.controller.Message;
import com.nlt.mobileteam.wifidirect.utils.Callback;
import com.nlt.mobileteam.wifidirect.utils.SocketUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ChatManager implements Runnable {
    protected static final String TAG = "ChatManager";
    private static final boolean VERBOSE = false;

    static final int MAX_BUFFER_SIZE = 1024 * 1024;
    private static final int DELAY = 1000;

    protected Socket socket = null;
    protected volatile boolean isInterrupted = false;
    protected volatile boolean streamOpen = false;

    protected DataInputStream iStream;
    protected DataOutputStream oStream;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    public ChatManager(Socket socket) {
        this.socket = socket;
        try {
            this.socket.setSoTimeout(0);
            if (this.socket.isConnected()) {
                 SocketUtil.trySetupKeepAliveOptions(this.socket);
                iStream = new DataInputStream(this.socket.getInputStream());
                oStream = new DataOutputStream(this.socket.getOutputStream());
                streamOpen = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(final Message message) {
        try {
            executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        if (streamOpen) {
                            oStream.writeInt(message.ordinal());
                            oStream.flush();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write");
                        return false;
                    }
                    return true;
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "error while sending message", e);
        }
    }

    public void write(final Message message, final String value) {
        try {
            executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        if (streamOpen) {
                            oStream.writeInt(message.ordinal());
                            oStream.writeUTF(value);
                            oStream.flush();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write");
                        return false;
                    }
                    return true;
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "error while sending message", e);
        }
    }

    public void write(final Message message, final String value, final int index) {
        try {
            executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        if (streamOpen) {
                            oStream.writeInt(message.ordinal());
                            oStream.writeUTF(value);
                            oStream.writeInt(index);
                            oStream.flush();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write");
                        return false;
                    }
                    return true;
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "error while sending message", e);
        }
    }

    public void write(final Message message, final int value) {
        try {
            executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        if (streamOpen) {
                            oStream.writeInt(message.ordinal());
                            oStream.writeInt(value);
                            oStream.flush();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write");
                        return false;
                    }
                    return true;
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "error while sending message", e);
        }
    }

    public void write(final Message message, final long value) {
        try {
            executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        if (streamOpen) {
                            oStream.writeInt(message.ordinal());
                            oStream.writeLong(value);
                            oStream.flush();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write");
                        return false;
                    }
                    return true;
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "error while sending message", e);
        }
    }

    public void write(final Message message, final long speed, final long time) {
        try {
            executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        if (streamOpen) {
                            oStream.writeInt(message.ordinal());
                            oStream.writeLong(speed);
                            oStream.writeLong(time);
                            oStream.flush();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write");
                        return false;
                    }
                    return true;
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "error while sending message", e);
        }
    }

    public void write(final Message message, final int length, final byte[] value) {
        try {
            executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        if (streamOpen) {
                            oStream.writeInt(message.ordinal());
                            oStream.writeInt(length);
                            oStream.write(value);
                            oStream.flush();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write");
                        return false;
                    }
                    return true;
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "error while sending message", e);
        }
    }

    public void closeSocketConnection() {
        closeSocketConnection(null);
    }

    public void closeSocketConnection(final Callback callback) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (VERBOSE) Log.w(TAG, "closing chat manager, callback = " + callback);

                try {
                    Log.i(TAG, "closeSocketConnection");
                    CommunicationController.get().removeChatManager(ChatManager.this);
                    streamOpen = false;
                    if (socket.isConnected()) {
                        iStream.close();
                        oStream.close();
                        socket.close();
                    }
                    if (callback != null) {
                        callback.apply();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, new Date(System.currentTimeMillis() + DELAY));

    }

    @Override
    public int hashCode() {
        int result = socket != null ? socket.hashCode() : 0;
        result = 31 * result + (isInterrupted ? 1 : 0);
        result = 31 * result + (streamOpen ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatManager that = (ChatManager) o;

        if (isInterrupted != that.isInterrupted) return false;
        if (streamOpen != that.streamOpen) return false;
        return (socket != null ? !socket.equals(that.socket) : that.socket != null);
    }
}
