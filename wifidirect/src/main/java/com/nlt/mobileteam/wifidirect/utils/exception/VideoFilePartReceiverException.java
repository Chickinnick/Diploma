package com.nlt.mobileteam.wifidirect.utils.exception;

public class VideoFilePartReceiverException extends RuntimeException {
    public VideoFilePartReceiverException() {
    }

    public VideoFilePartReceiverException(String detailMessage) {
        super(detailMessage);
    }

    public VideoFilePartReceiverException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public VideoFilePartReceiverException(Throwable throwable) {
        super(throwable);
    }
}
