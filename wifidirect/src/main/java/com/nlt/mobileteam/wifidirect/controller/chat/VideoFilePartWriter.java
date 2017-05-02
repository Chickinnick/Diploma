package com.nlt.mobileteam.wifidirect.controller.chat;

import android.util.Log;

import com.nlt.mobileteam.wifidirect.utils.FileUtils;
import com.nlt.mobileteam.wifidirect.R;
import com.nlt.mobileteam.wifidirect.WifiDirectCore;
import com.nlt.mobileteam.wifidirect.utils.exception.VideoFilePartReceiverException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * The class provides opportunity to receive and write a video file part by part
 * without foreach open/closing output stream.
 * A limit time for adding each part is 5 second.
 * If during this time is not a new part of the video is added,
 * the output stream closes automatically.
 *
 * Also the class manages UI progress bar by itself.
 * @see VideoFilePartWriter.Writer#startProgressBar()
 * @see VideoFilePartWriter.Writer#updateProgressBar()
 * @see VideoFilePartWriter.Writer#finishProgressBar()
 * @see VideoFilePartWriter.Writer#abortProgressBar()
* */
public class VideoFilePartWriter {

    private static final String TAG = "VideoFilePartWriter";
    private static final long VIDEO_FILE_TRANSFER_TIMEOUT = 15;

    private File video;
    private Writer writer;
    private long videoFileLength;
    private int deviceIndex;
    private long totalProgress;
    private long receivedByteCount;

    private BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(3, true);


    public VideoFilePartWriter(File video, long videoFileLength, int deviceIndex) {
        this.video = video;
        this.videoFileLength = videoFileLength;
        this.deviceIndex = deviceIndex;
        writer = new Writer();
        totalProgress = 0;
        receivedByteCount = 0;
        writer.start();
    }

    /**
     * Methods adds a video part to a queue for writing in a file.
     * Order of addition, must be consistent.
     * @throws VideoFilePartReceiverException with a cause if action fault
     * */
    public void addVideoPart(byte[] videoPart) throws VideoFilePartReceiverException{
        try {
            if (queue.offer(videoPart, VIDEO_FILE_TRANSFER_TIMEOUT, TimeUnit.SECONDS)) {
                receivedByteCount += videoPart.length;
            } else {
                abort();
                throw new VideoFilePartReceiverException("a limit for waiting free slots at queue expired.");
            }
        } catch (InterruptedException e) {
            abort();
            throw new VideoFilePartReceiverException("waiting for free slot at queue, was interrupted.", e);
        }
    }

    public void abort() {
        writer.interrupt();
    }

    public boolean isAllVideoPartReceived() {
        return videoFileLength == receivedByteCount;
    }

    private class Writer extends Thread {

        private static final String TAG = "Writer";

        @Override
        public void run() {

            startProgressBar();

            try (FileOutputStream fos = new FileOutputStream(video)) {
                while (totalProgress < videoFileLength && !isInterrupted()) {
                    byte[] videoPart = queue.poll(VIDEO_FILE_TRANSFER_TIMEOUT, TimeUnit.SECONDS);

                    if (videoPart == null) {
                        Log.w(TAG, "time for waiting is over, break writing");
                        break;
                    }

                    fos.write(videoPart);
                    totalProgress += videoPart.length;

                    updateProgressBar();
                }
                fos.flush();
            } catch (IOException e) {
                Log.e(TAG, "error in write file process.", e);
            } catch (InterruptedException e) {
                Log.e(TAG, "writing file was interrupted.", e);
            }

            if (totalProgress != videoFileLength) {
                video.delete();

                abortProgressBar();

                Log.e(TAG, "video file deleted.");
            } else {
                FileUtils.registerFile(video.getPath());

                finishProgressBar();
            }
        }

        private void finishProgressBar() {
          /*  BroadcastManager
                    .get()
                    .sendStringAndInt(
                            Action.COMM_PROJECT_VIDEO_RECEIVED,
                            video.getPath(),
                            deviceIndex);*/
        }

        private void abortProgressBar() {
           /* TODO BroadcastManager
                    .get()
                    .sendInt(
                            Action.COMM_PROJECT_VIDEO_RECEIVE_ABORTED,
                            deviceIndex);
        */}

        private void updateProgressBar() {
            /*BroadcastManager
                    .get()
                    .sendSerializable(
                            Action.COMM_PROJ_PART_RECEIVED,
                            new VideoReceivingProgress(
                                    videoFileLength,
                                    totalProgress,
                                    deviceIndex, null));*/
        }

        private void startProgressBar() {
      /*      BroadcastManager
                    .get()
                    .sendStringAndInt(
                            Action.COMM_SET_PROGRESS_STATUS,
                            WifiDirectCore.getAppContext().getResources().getString(R.string.receiving),
                            deviceIndex);*/
        }
    }
}
