package com.nlt.mobileteam.wifidirect.utils;


import android.util.Log;

import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;

public class SocketUtil {

    private static final String TAG = "SocketUtil";

    private final static int SOL_TCP = 6;

    private final static int TCP_KEEPIDLE = 4;
    private final static int TCP_KEEPINTVL = 5;
    private final static int TCP_KEEPCNT = 6;

    public static void trySetupKeepAliveOptions(Socket socket) {
        try {
            if (!socket.getKeepAlive()) {
                socket.setKeepAlive(true);
                try {
                    Field socketImplField = Class.forName("java.net.Socket").getDeclaredField("impl");
                    socketImplField.setAccessible(true);
                    if (socketImplField != null) {
                        Object plainSocketImpl = socketImplField.get(socket);
                        Field fileDescriptorField = Class.forName("java.net.SocketImpl").getDeclaredField("fd");
                        if (fileDescriptorField != null) {
                            fileDescriptorField.setAccessible(true);
                            FileDescriptor fileDescriptor = (FileDescriptor) fileDescriptorField.get(plainSocketImpl);
                            Class libCoreClass = Class.forName("libcore.io.Libcore");
                            Field osField = libCoreClass.getDeclaredField("os");
                            osField.setAccessible(true);
                            Object libcoreOs = osField.get(libCoreClass);
                            Method setSocketOptsMethod = Class.forName("libcore.io.ForwardingOs").getDeclaredMethod("setsockoptInt", FileDescriptor.class, int.class, int.class, int.class);
                            if (setSocketOptsMethod != null) {
                                setSocketOptsMethod.invoke(libcoreOs, fileDescriptor, SOL_TCP, TCP_KEEPIDLE, 10); //10 - idleTimeout
                                setSocketOptsMethod.invoke(libcoreOs, fileDescriptor, SOL_TCP, TCP_KEEPINTVL, 2); //2 - interval
                                setSocketOptsMethod.invoke(libcoreOs, fileDescriptor, SOL_TCP, TCP_KEEPCNT, 4); //4 - count
                            }
                        }
                        Log.i(TAG, "trySetupKeepAliveOptions success!!!");
                    }
                } catch (Exception reflectionException) {
                    Log.e(TAG, "ReflectionException while configuring keepAlive Options", reflectionException);
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "SocketException while configuring keepAlive Options", e);
        }
    }

}
