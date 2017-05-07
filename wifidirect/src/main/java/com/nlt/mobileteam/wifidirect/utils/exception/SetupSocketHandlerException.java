package com.nlt.mobileteam.wifidirect.utils.exception;

public class SetupSocketHandlerException extends RuntimeException {
    public SetupSocketHandlerException() {
    }

    public SetupSocketHandlerException(String detailMessage) {
        super(detailMessage);
    }

    public SetupSocketHandlerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SetupSocketHandlerException(Throwable throwable) {
        super(throwable);
    }
}
