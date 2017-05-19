package com.umbaba.bluetoothvswifidirect.data.comparation;


public class Criteria {

   private String title;

   private String left;

   private String right;


    private Criteria(String title, String left, String right) {
        this.title = title;
        this.left = left;
        this.right = right;
    }

    public String getTitle() {
        return title;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public static Criteria prepareFromData(MeasurementData blMeasdata, MeasurementData wifiMeasdata) {
        return new Criteria(blMeasdata.getTestdataTitle(), String.valueOf(blMeasdata.getSpeed()), String.valueOf(wifiMeasdata.getSpeed()));
    }
}
