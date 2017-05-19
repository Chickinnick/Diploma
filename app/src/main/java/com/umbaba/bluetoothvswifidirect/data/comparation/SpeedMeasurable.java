package com.umbaba.bluetoothvswifidirect.data.comparation;


public interface SpeedMeasurable {
    double getSpeed();

    int getFileSize();

    int getDistance();

    void calcSpeed(long startTime, long endTime);
}
