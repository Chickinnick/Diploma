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

    public static String getLowRezFileName() {
        String result;
        //TODO get project name and use it as file name;
        result = "Preview.mp4";
        return result;
    }

    public static String getFinishedVideoName(String outputSize) {
        String result;
        //TODO get project name and use it as file name;
//        result = ProjectController.get().localProject.getTitle().replace(" ", "_") + ".mp4";
//        result = "FinishedVideo.mp4";
        //fix fo bug #4811
        Long timestampLong = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date(timestampLong);
        result = simpleDateFormat.format(date) + "_" + outputSize + ".mp4";
        return result;
    }

    public static void registerFile(String path) {
    /*TODO    MediaScannerConnection.scanFile(
                WifiDirectCore.getAppContext(),
                new String[]{new File(path).getAbsolutePath()},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        try {
                            Log.w("MediaScanner", "file " + path + " was scanned seccessfully: " + uri);
                            BroadcastManager.get().sendString(GET_FILE_CONTENT_URI, uri.toString());
                        } catch (Exception e) {
//                            e.printStackTrace();
                        }
                    }
                });*/
    }

    public static Uri prepareConvertedPath(String inputUri, int index) {
        String fileName = getStorageDirectory(CONVERTED_VIDEO_DIR) + index + "_" + "converted.mp4";
        Uri outputUri = Uri.parse(fileName);

        return outputUri;
    }

    public static boolean checkFileExistence(String filePath) {
        boolean result = false;
        File file = new File(filePath);
        if (file != null && file.exists() && file.length() > 0) {
            result = true;
        }
        return result;
    }



    /**
     * Method used to generate video of black frames to test video players amount.
     *
     * @return path to generated video file
     */



    public static int getFolderType(String path) {
        int result = -1;
        if (path.contains(LOCAL)) {
            result = LOCAL_VIDEO_DIR;
        } else if (path.contains(CONVERTED)) {
            result = CONVERTED_VIDEO_DIR;
        } else if (path.contains(FINISH)) {
            result = FINISH_VIDEO_DIR;
        } else if (path.contains(RECEIVED)) {
            result = RECIEVED_VIDEO_DIR;
        }
        return result;
    }

    public static int getFreeSpaceAvailable(File f) {
        StatFs stat = new StatFs(f.getPath());
        long bytesAvailable = (long) stat.getBlockSizeLong() * (long) stat.getAvailableBlocksLong();
        float freeSpace = bytesAvailable / (1024.f * 1024.f);//MB
        Log.d(TAG, "Free space:" + freeSpace + " Mb");
        return (int) freeSpace;
    }

    public static boolean isNetworkOnline(Context context) {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;

    }



    public static int displayRotationToDegrees(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;

            case Surface.ROTATION_90:
                return 90;

            case Surface.ROTATION_180:
                return 180;

            case Surface.ROTATION_270:
                return 270;
        }

        return -1;
    }


    public static HashMap<Integer, String> sortHashMap(HashMap<Integer, String> countries) {
        if (countries == null) {
            return new HashMap<>();
        }
        List<Integer> mapKeys = new ArrayList<>(countries.keySet());
        LinkedHashMap<Integer, String> sortedMap = new LinkedHashMap<>();
        Collections.sort(mapKeys);
        for (int i = 0; i < countries.size(); i++) {
            Integer key = mapKeys.get(i);
            String s = countries.get(key);
            sortedMap.put(key, s);
        }
        return sortedMap;
    }

    public static Map<String, String> prepareParam(String key, String value) {
        Map<String, String> result = new HashMap<>();
        result.put(key, value);
        return result;
    }

    public static String generateItemTextWithCorrectEnd(int numItem) {
        String item = " item";
        String items = " items";
        String empty = "";
        if (numItem > 0) {
            if (numItem == 1) {
                return numItem + item;
            } else {
                return numItem + items;
            }
        } else {
            return empty;
        }
    }
}
