package com.nlt.mobileteam.wifidirect.utils.exception;

public class MessageControllerException extends RuntimeException {
    public MessageControllerException() {
    }

    public MessageControllerException(String detailMessage) {
        super(detailMessage);
    }

    public MessageControllerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MessageControllerException(Throwable throwable) {
        super(throwable);
    }
}
