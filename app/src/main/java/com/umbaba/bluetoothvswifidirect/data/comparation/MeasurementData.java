package com.umbaba.bluetoothvswifidirect.data.comparation;


public class MeasurementData implements SpeedMeasurable {

    double speedMbPs;

    int fileSizeMb;

    int distanceInMeters;

    public MeasurementData(int fileSizeMb, int distanceInMeters) {
        this.fileSizeMb = fileSizeMb;
        this.distanceInMeters = distanceInMeters;
    }

    @Override
    public double getSpeed() {
        return speedMbPs;
    }

    @Override
    public int getFileSize() {
        return fileSizeMb;
    }

    @Override
    public int getDistance() {
        return distanceInMeters;
    }

    @Override
    public void calcSpeed(long startTime, long endTime) {
        speedMbPs = fileSizeMb / endTime - startTime;
    }

    public String getTestdataTitle() {
        return "File size:" + fileSizeMb + " \t distance: " + distanceInMeters;
    }
}
