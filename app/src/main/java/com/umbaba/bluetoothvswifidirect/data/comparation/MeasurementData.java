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
        speedMbPs = fileSizeMb / diffInSeconds(startTime, endTime);
    }

    private float diffInSeconds(long startTime, long endTime) {
        return endTime - startTime / 1000f;
    }
}
