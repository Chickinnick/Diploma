package com.nlt.mobileteam.wifidirect.controller;

import android.util.Log;

import com.nlt.mobileteam.wifidirect.utils.FileUtils;

import java.io.File;

@Deprecated
public abstract class RecordControllerBase {

    private static final String TAG = RecordControllerBase.class.getSimpleName();
    //other utils
    public static int instanceCode;
    public static String recorded_file_path = "file.mp4";
    protected String videoName;
    protected boolean recording = false;

    public static String getRecordedVideoLink() {
        return recorded_file_path;
    }

    public void renameVideo() {
        File oldName = new File(recorded_file_path);
        Log.w(TAG, "old name: " + recorded_file_path);
        File newName = new File(videoName);
        Log.w(TAG, "new name: " + videoName);

        if (oldName.exists()) {
            Log.w(TAG, "renaming video");
            oldName.renameTo(newName);
            FileUtils.registerFile(newName.getPath());
            recorded_file_path = videoName;
        } else {
            Log.e(TAG, "can't find file " + recorded_file_path);
        }
    }

    public abstract void startRecording();

    public abstract void stopRecording();

    public abstract void onPause();

    public abstract void onDestroy();

    public void deleteRecorded() {
        File toDelete = new File(recorded_file_path);
        if (toDelete.exists()) {
            toDelete.delete();
        }
    }

    public void setVideoName(String uuid) {
        this.videoName = FileUtils.getStorageDirectory(FileUtils.LOCAL_VIDEO_DIR) + FileUtils.getFileName(instanceCode, uuid);
    }

    public String getVideoName() {
        return videoName;
    }

  //public abstract void setCamera(AbstractCamera cameraController);


    public boolean isRecording() {
        return recording;
    }
}
