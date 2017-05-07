package com.nlt.mobileteam.wifidirect.controller.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface MediaStreamsHandler {

    InputStream getAudioInputStream() throws IOException;

    InputStream getVideoInputStream() throws IOException;

    OutputStream getAudioOutputStream() throws IOException;

    OutputStream getVideoOutputStream() throws IOException;

}
