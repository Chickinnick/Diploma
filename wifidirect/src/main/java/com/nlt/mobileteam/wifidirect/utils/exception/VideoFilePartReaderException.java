package com.nlt.mobileteam.wifidirect.utils.exception;

public class VideoFilePartReaderException extends RuntimeException {
    public VideoFilePartReaderException() {
    }

    public VideoFilePartReaderException(String detailMessage) {
        super(detailMessage);
    }

    public VideoFilePartReaderException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public VideoFilePartReaderException(Throwable throwable) {
        super(throwable);
    }
}
