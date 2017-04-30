package com.nlt.mobileteam.wifidirect.controller.socket;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;

public abstract class SocketHandler extends Thread {

    protected static final int SERVER_COMMAND_PORT = 6701;
    protected static final int SERVER_VIDEO_PORT = 6702;
    protected static final int SERVER_AUDIO_PORT = 6703;
    protected static final int SERVER_PING_PONG_PORT = 6704;


    public static ClientSocketHandler getClient(InetAddress inetAddress) {
        return new ClientSocketHandler(inetAddress);
    }

    public static AbstractGroupOwnerSocketHandler getServer(DirectorSocketHandlerType handlerType) {
        switch (handlerType) {
            case CINAMAKER_DIRECTOR:
                return new GroupOwnerSocketHandler();
            case DDP_DIRECTOR:
                return new DDPGroupSocketHandler();
            default:
                throw new IllegalArgumentException("wrong handlerType: " +handlerType);
        }
    }

    @Override
    public abstract void run();

    protected void closeSocket(Closeable socket) {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
