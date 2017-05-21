package com.umbaba.bluetoothvswifidirect.data.comparation;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

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
        double bluetoothSpeed = blMeasdata.getSpeed();
        double wifiSpeed = wifiMeasdata.getSpeed();

        return new Criteria(blMeasdata.getTestdataTitle(), getSpeedValueString(bluetoothSpeed), getSpeedValueString(wifiSpeed));
    }

    public static String getSpeedValueString(double wifiSpeed) {
        double doubleValue = new BigDecimal(wifiSpeed).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return doubleValue + "MBps";
    }

}
