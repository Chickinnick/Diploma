package com.umbaba.bluetoothvswifidirect.data.comparation;


import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Criteria implements Parcelable{

    private int fileLen;
    private int distance;
    private String left;

    private String right;


    private Criteria(int fileLen, int distance, String left, String right) {
        this.fileLen = fileLen;
        this.distance = distance;
        this.left = left;
        this.right = right;
    }

    protected Criteria(Parcel in) {
        fileLen = in.readInt();
        distance = in.readInt();
        left = in.readString();
        right = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fileLen);
        dest.writeInt(distance);
        dest.writeString(left);
        dest.writeString(right);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Criteria> CREATOR = new Creator<Criteria>() {
        @Override
        public Criteria createFromParcel(Parcel in) {
            return new Criteria(in);
        }

        @Override
        public Criteria[] newArray(int size) {
            return new Criteria[size];
        }
    };

    public int getFileLen() {
        return fileLen;
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

        return new Criteria(blMeasdata.getFileSize(), blMeasdata.getDistance(), getSpeedValueString(bluetoothSpeed), getSpeedValueString(wifiSpeed));
    }

    public static String getSpeedValueString(double wifiSpeed) {
        double doubleValue = new BigDecimal(wifiSpeed).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return String.valueOf(doubleValue);
    }

    public int getDistance() {
        return distance;
    }


    @Override
    public String toString() {
        return "Criteria{" +
                "fileLen=" + fileLen +
                ", distance=" + distance +
                ", left='" + left + '\'' +
                ", right='" + right + '\'' +
                '}';
    }
}
