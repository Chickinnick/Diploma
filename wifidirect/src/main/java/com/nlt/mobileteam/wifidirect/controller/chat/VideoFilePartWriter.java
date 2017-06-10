package com.nlt.mobileteam.wifidirect.controller.chat;

import android.util.Log;

import com.nlt.mobileteam.wifidirect.model.event.transfer.Abort;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Progress;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Success;
import com.nlt.mobileteam.wifidirect.utils.exception.VideoFilePartReceiverException;

import org.greenrobot.eventbus.EventBus;

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
                Log.i(TAG, "addVideoPart: received :   " +receivedByteCount  + " from " + videoFileLength);
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


            try (FileOutputStream fos = new FileOutputStream(video)) {
                while (totalProgress < videoFileLength && !isInterrupted()) {
                    byte[] videoPart = queue.poll(VIDEO_FILE_TRANSFER_TIMEOUT, TimeUnit.SECONDS);

                    if (videoPart == null) {
                        Log.w(TAG, "time for waiting is over, break writing");
                        break;
                    }

                    fos.write(videoPart);
                    totalProgress += videoPart.length;

                    updateProgressBar(videoFileLength, totalProgress);
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

                finishProgressBar();
            }
        }

        private void finishProgressBar() {
            EventBus.getDefault().post(new Success());
        }

        private void abortProgressBar() {
            EventBus.getDefault().post(new Abort(deviceIndex));
        }

        private void updateProgressBar(long videoFileLength, long totalProgress) {
            EventBus.getDefault().post(new Progress(
                    videoFileLength,
                    totalProgress,
                    deviceIndex));
        }

    }
}
