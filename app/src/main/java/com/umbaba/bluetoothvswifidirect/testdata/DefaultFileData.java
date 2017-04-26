package com.umbaba.bluetoothvswifidirect.testdata;


import android.net.Uri;

import java.io.File;

public class DefaultFileData implements TestFileModel {


    @Override
    public File get5MBFile() {
        return new File(String.valueOf("file:///android_asset/5MB.zip"));
    }

    @Override
    public File get10MBFile() {
        return new File(String.valueOf("file:///android_asset/10MB.zip"));

    }

    @Override
    public File get20MBFile() {
        return new File(String.valueOf("file:///android_asset/20MB.zip"));

    }
}
