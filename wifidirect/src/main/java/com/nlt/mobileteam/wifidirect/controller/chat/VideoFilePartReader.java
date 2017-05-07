package com.nlt.mobileteam.wifidirect.controller.chat;

import android.util.Log;
import com.nlt.mobileteam.wifidirect.R;
import com.nlt.mobileteam.wifidirect.WifiDirectCore;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Abort;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Progress;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Success;
import com.nlt.mobileteam.wifidirect.utils.exception.VideoFilePartReaderException;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.nlt.mobileteam.wifidirect.controller.chat.ChatManager.MAX_BUFFER_SIZE;


/**
 * The class provides opportunity to read the video file part by part
 * without foreach open/closing input stream.
 * STARTED_DIRECTOR_MODE limit time for pick up each part is 5 second.
 * If during this time a current part of the video is not pick up by user,
 * the input stream closes automatically.
 *
 * Also the class manages UI by itself. Shows "Inform", "Success" and "Abort" dialogs.
 * @see VideoFilePartReader.Reader#showInformDialog()
 * @see VideoFilePartReader.Reader#showSuccessDialog()
 * @see VideoFilePartReader.Reader#showAbortDialog()
 * */
public class VideoFilePartReader {

    private static final String TAG = "VideoFilePartReader";

    public static final int DIALOG_ABORT = 2;
    public static final int DIALOG_SUCCESS = 1;
    public static final int DIALOG_INFORM = -1;

    private static final long VIDEO_FILE_TRANSFER_TIMEOUT = 15;

    private File video;
    private Reader reader;
    private BlockingQueue<byte[]> buffer;


    public VideoFilePartReader(File video) {
        this.video = video;
        buffer = new ArrayBlockingQueue<>(1);
        reader = new Reader();
        reader.start();
    }

    /**
     * Return a next video part or throws {@code VideoFilePartReaderException}
     */
    public byte[] getNextVideoPart() throws VideoFilePartReaderException {
        try {
            byte[] poll = buffer.poll(VIDEO_FILE_TRANSFER_TIMEOUT, TimeUnit.SECONDS);
            if (poll != null) {
                return poll;
            }

            abort();
            throw new VideoFilePartReaderException("Buffer is empty.");
        } catch (InterruptedException e) {
            abort();
            throw new VideoFilePartReaderException("waiting for new video part for sending to director, was interrupted.",e);
        }
    }

    public void abort() {
        reader.interrupt();
    }

    private class Reader extends Thread {
        private static final String TAG = "Reader";

        @Override
        public void run() {

            long videoFileLength = video.length();
            long totalProgress = 0;

            showInformDialog();

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(video))) {

                int expectedBytes = videoFileLength <= MAX_BUFFER_SIZE ? ((int) videoFileLength) : MAX_BUFFER_SIZE;

                while (totalProgress < videoFileLength && !isInterrupted()) {
                    byte[] videoPart = new byte[expectedBytes];

                    totalProgress += bis.read(videoPart, 0, expectedBytes);
                    expectedBytes = ((double) (videoFileLength - totalProgress)) / ((double) expectedBytes) > 1. ? expectedBytes : ((int) ((videoFileLength - totalProgress) % expectedBytes));

                    if (!buffer.offer(videoPart, VIDEO_FILE_TRANSFER_TIMEOUT, TimeUnit.SECONDS)) {
                        Log.w(TAG, "time for waiting is over, break reading");
                        break;
                    }

                    Log.w(TAG, "video part success put into buffer for sending");
                }

            } catch (FileNotFoundException e) {
                Log.e(TAG, "recorded file doesn't exists. exiting", e);
            } catch (IOException e) {
                Log.e(TAG, "error reading file. exiting", e);
            } catch (InterruptedException e) {
                Log.e(TAG, "running was interrupted.", e);
            }

            if (totalProgress == videoFileLength) {
                showSuccessDialog();
            } else {
                showAbortDialog();
            }
        }

        private void showAbortDialog() {
            EventBus.getDefault().post(new Abort());

        }

        private void showSuccessDialog() {

            EventBus.getDefault().post(new Success());
        }

        private void showInformDialog() {
            EventBus.getDefault().post(new Progress());
        }
    }


}
