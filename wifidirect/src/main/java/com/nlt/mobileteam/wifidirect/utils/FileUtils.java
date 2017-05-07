package com.nlt.mobileteam.wifidirect.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.nlt.mobileteam.wifidirect.WifiDirectCore;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pAssistant;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pDirector;
import com.nlt.mobileteam.wifidirect.model.InstanceCode;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class FileUtils {

    private static final String TAG = "Utils";

    public static final int FINISH_VIDEO_DIR = 1;
    public static final int RECIEVED_VIDEO_DIR = 2;
    public static final int LOCAL_VIDEO_DIR = 3;
    public static final int CONVERTED_VIDEO_DIR = 4;

    public static final int PREPARED_VIDEO_DIR = 5;
    public static final String FINISH = "Finish/";
    public static final String LOCAL = "Local/";
    public static final String RECEIVED = "Recieved/";
    public static final String CONVERTED = "Converted/";
    public static final int FREE_SPACE_LIMIT_MB = 100;

    public static String getFileName(int instance, @Nullable String uuid) {

        String extension = ".mp4";

        WifiP2pDevice thisDevice = instance == InstanceCode.DIRECTOR ? WiFiP2pDirector.get().getThisDevice()
                : WiFiP2pAssistant.get().getThisDevice();
        String result = TextUtils.isEmpty(uuid) ? "" : uuid;
        if (!TextUtils.isEmpty(result) && thisDevice != null) {
            result += "_" + thisDevice.deviceName.replace(" ", "_");
        } else if (thisDevice != null) {
            result = thisDevice.deviceName.replace(" ", "_") + "_" + (System.currentTimeMillis() % 100000000);
        } else {
            result = String.valueOf((System.currentTimeMillis() % 100000000));
        }
        return result + extension;
    }

    public static String getTrimmedFileName(String deviceName) {
        return "trim_" + deviceName + (System.currentTimeMillis() % 100000000) + ".mp4";
    }

    public static String getStorageDirectory(int type) {

        String result = null;
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "CinaMaker");

        String folder = "";
        switch (type) {
            case FINISH_VIDEO_DIR:
                folder = "Finish/";
                break;
            case RECIEVED_VIDEO_DIR:
                folder = "Recieved/";
                break;
            case LOCAL_VIDEO_DIR:
                folder = "Local/";
                break;
            case CONVERTED_VIDEO_DIR:
                folder = "Converted/";
                break;
            case PREPARED_VIDEO_DIR:
                folder = "Prepared/";
                break;
        }
        File resultDir = new File(directory, folder);
        resultDir.mkdirs();
        return resultDir.getAbsolutePath() + File.separator;
    }

    public static long nanoToMillis(long nanos) {
        return nanos / 1000000;
    }

    public static byte[] toByteArray(FileInputStream fileInputStream) {
        byte[] result = null;
        try {
            int available = fileInputStream.available();
            result = new byte[available];
            fileInputStream.read(result, 0, result.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String inputStreamToString(FileInputStream fileInputStream) {
        byte[] result = null;
        try {
            int available = fileInputStream.available();
            result = new byte[available];
            fileInputStream.read(result, 0, result.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(result);
    }

    public static void logNanos(long nanos, @Nullable String message) {
        long formatedNanos = nanos % 1000000 / 1000;
        long formatedMilis = nanos / 1000000 % 1000;
        long formatedSec = nanos / 1000000000;
        Log.w("SOCKET", (!TextUtils.isEmpty(message) ? message : "") + "Sec: " + formatedSec + " Milis: " + formatedMilis + " nanos: " + formatedNanos);
    }

    public static String getFileNameForAssistant(WiFiP2pService device) {
        return (getStorageDirectory(RECIEVED_VIDEO_DIR) + System.currentTimeMillis() + "_" + device.index + "_" + device.device.deviceName + ".mp4").replaceAll("[\\s]", "_");
    }
}
