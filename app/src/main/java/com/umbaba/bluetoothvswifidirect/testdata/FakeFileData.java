package com.umbaba.bluetoothvswifidirect.testdata;


import android.content.res.Resources;
import android.os.Environment;
import android.support.annotation.RawRes;
import android.util.Log;

import com.umbaba.bluetoothvswifidirect.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FakeFileData implements  TestFileModel {
    private static final String TAG = "FakeFileData";
    private static final int FILE_LITTLE = 1;
    private Resources resources;

    public FakeFileData(Resources resources) {
        this.resources = resources;
    }

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
        return getJavaFile(String.valueOf(
                Environment.getExternalStorageDirectory() + File.separator + "file" + FILE_LITTLE ), R.raw.little);
    }

    @Override
    public File get10MBFile() {
        return getJavaFile(String.valueOf(
                Environment.getExternalStorageDirectory() + File.separator + "file" + FILE_LITTLE ), R.raw.little);
    }

    @Override
    public File get20MBFile() {
        return getJavaFile(String.valueOf(
                Environment.getExternalStorageDirectory() + File.separator + "file" + FILE_LITTLE ), R.raw.little);
    }

    public File getJavaFile(String path, @RawRes int id){
        File file = new File(path);
        try {
            InputStream inputStream = resources.openRawResource(id);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte buf[]=new byte[1024];
            int len;
            while((len=inputStream.read(buf))>0) {
                fileOutputStream.write(buf,0,len);
            }

            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return file;
    }
}
