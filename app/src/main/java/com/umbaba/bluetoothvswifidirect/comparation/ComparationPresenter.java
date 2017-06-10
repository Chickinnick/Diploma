package com.umbaba.bluetoothvswifidirect.comparation;

import android.content.Context;
import android.util.Log;

import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationModel;
import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationRepository;
import com.umbaba.bluetoothvswifidirect.data.comparation.Criteria;
import com.umbaba.bluetoothvswifidirect.data.comparation.MeasurementData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 17.04.2017.
 */

public class ComparationPresenter implements ComparationContract.Presenter {

    private static final String TAG = "ComparationPresenter";

    private static final int MAX_FILES_SENT = 3;
    private final ComparationContract.View view;
    private final ComparationModel comparationModel;
    private MeasurementData measurementData;
    long startTime = 0;
    List<MeasurementData> bluetoothMeasurementData;
    List<MeasurementData> wifiMeasurementData;
    int state;
    private int currentDistance;

    public ComparationPresenter(Context context, ComparationContract.View view) {
        this.view = view;
        this.comparationModel = new ComparationRepository(context);
        this.view.setPresenter(this);
        this.bluetoothMeasurementData = new ArrayList<>();
        this.wifiMeasurementData = new ArrayList<>();
    }

    @Override
    public void start(int state) {
        this.state = state;
        Log.d(TAG, "start() called with: state = [" + state + "]"  + (state == 0 ? "bl" : "wifi"));
    }

    public void loadCriterion() {
        List<Criteria> criterion = comparationModel.getCriterion();
        view.showCriterion(criterion);
    }

    @Override
    public void startTransfer(int size) {
        measurementData = new MeasurementData(size, currentDistance);
        startTime = System.currentTimeMillis();
        Log.i(TAG, "startTransfer: time: " + startTime);
    }


    @Override
    public void stopTransfer (long fileLength) {
        long stopTime = System.currentTimeMillis();
        measurementData.calcSpeed(startTime, stopTime);
        switch (state) {
            case TYPE_WIFI:
                wifiMeasurementData.add(measurementData);
                break;
            case TYPE_BLUETOOTH:
                bluetoothMeasurementData.add(measurementData);
                break;
        }
        Log.i(TAG, "stopTransfer " + (state == 0 ? "bl" : "wifi") + ": added " + measurementData.toString());
    }

    @Override
    public void commitChanges() {
        for (int i = 0; i < MAX_FILES_SENT; i++) {
            MeasurementData blMeasdata = bluetoothMeasurementData.get(i);
            MeasurementData wifiMeasdata = wifiMeasurementData.get(i);
            comparationModel.addCriterion(Criteria.prepareFromData(blMeasdata, wifiMeasdata));
        }
        Log.i(TAG, "commitChanges: " );
        for (Criteria criteria : comparationModel.getCriterion()) {
            Log.i(TAG, "added: " + criteria.toString());
        }
        releaseTempData();
    }

    private void releaseTempData() {
        bluetoothMeasurementData.clear();
        wifiMeasurementData.clear();
    }


    public void setCurrentDistance(int currentDistance) {
        this.currentDistance = currentDistance;
    }
}
