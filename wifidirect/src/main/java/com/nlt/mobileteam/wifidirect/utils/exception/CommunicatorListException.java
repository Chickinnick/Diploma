package com.nlt.mobileteam.wifidirect.utils.exception;

public class CommunicatorListException extends RuntimeException {
    public CommunicatorListException() {
    }

    public CommunicatorListException(String detailMessage) {
        super(detailMessage);
    }

    public CommunicatorListException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CommunicatorListException(Throwable throwable) {
        super(throwable);
    }
}
