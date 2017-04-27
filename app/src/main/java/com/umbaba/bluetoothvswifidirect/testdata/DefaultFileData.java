package com.umbaba.bluetoothvswifidirect.testdata;

import android.util.Log;

import java.io.File;

public class DefaultFileData implements TestFileModel {

    private static final String TAG = "DefaultFileData";


    @Override
    public File getFile(int size){
        switch (size){
            case FILE_5:
                return get5MBFile();
            case FILE_10:
                return get10MBFile();
            case FILE_20:
                return get20MBFile();
        }
        Log.e(TAG, "getFile: failed" );
        return null;
    }

    @Override
    public File get5MBFile() {
        return new File(String.valueOf("file:///android_asset/file5mb.zip"));
    }

    @Override
    public File get10MBFile() {
        return new File(String.valueOf("file:///android_asset/file10mb.zip"));

    }

    @Override
    public File get20MBFile() {
        return new File(String.valueOf("file:///android_asset/file20mb.zip"));

    }
}
