package com.nlt.mobileteam.wifidirect.model.event.transfer;

/**
 * Created by Nick on 06.05.2017.
 */

public class Progress {

    private long videoFileLength;
    private long totalProgress;
    private int deviceIndex;

    public Progress() {
    }

    public Progress(long videoFileLength, long totalProgress, int deviceIndex) {
        this.videoFileLength = videoFileLength;
        this.totalProgress = totalProgress;
        this.deviceIndex = deviceIndex;
    }
}
