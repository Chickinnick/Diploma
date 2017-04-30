package com.nlt.mobileteam.wifidirect;

/**
 * Created by developer on 23.11.2016.
 */

public class ConnectSocketException extends RuntimeException {
    public ConnectSocketException() {
        super();
    }

    public ConnectSocketException(String detailMessage) {
        super(detailMessage);
    }

    public ConnectSocketException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ConnectSocketException(Throwable throwable) {
        super(throwable);
    }
}
