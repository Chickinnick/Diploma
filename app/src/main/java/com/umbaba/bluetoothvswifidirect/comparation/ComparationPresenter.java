package com.umbaba.bluetoothvswifidirect.comparation;

import android.content.Context;

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

    private static final int MAX_FILES_SENT = 3;
    private Context context;
    private final ComparationContract.View view;
    private final ComparationModel comparationModel;
    private MeasurementData measurementData;
    long startTime = 0;
    List<MeasurementData> bluetoothMeasurementData;
    List<MeasurementData> wifiMeasurementData;
    int state;
    private int currentDistance;

    public ComparationPresenter(Context context, ComparationContract.View view) {
        this.context = context;
        this.view = view;
        this.comparationModel = new ComparationRepository(context);
        this.view.setPresenter(this);
        this.bluetoothMeasurementData = new ArrayList<>();
        this.wifiMeasurementData = new ArrayList<>();
    }

    @Override
    public void start(int state) {
        this.state = state;
    }

    public void loadCriterion() {
        List<Criteria> criterion = comparationModel.getCriterion();
        view.showCriterion(criterion);
    }

    @Override
    public void startTransfer(int size) {
        measurementData = new MeasurementData(size, currentDistance);
        startTime = System.currentTimeMillis();
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
    }

    @Override
    public void commitChanges() {
        for (int i = 0; i < MAX_FILES_SENT; i++) {
            MeasurementData blMeasdata = bluetoothMeasurementData.get(i);
            MeasurementData wifiMeasdata = wifiMeasurementData.get(i);
            comparationModel.addCriterion(Criteria.prepareFromData(blMeasdata, wifiMeasdata));
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
