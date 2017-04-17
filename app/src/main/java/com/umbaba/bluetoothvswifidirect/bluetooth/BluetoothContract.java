package com.umbaba.bluetoothvswifidirect.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.umbaba.bluetoothvswifidirect.BasePresenter;
import com.umbaba.bluetoothvswifidirect.BaseView;

import java.util.List;

/**
 * Created by Nick on 17.04.2017.
 */

public interface BluetoothContract {
    interface View extends BaseView<Presenter> {


        void addDevice(BluetoothDevice bluetoothDevice);

        void discoverStarted();

        void discoverFinished();

        void stateOn();

        void stateOff();
    }

    interface Presenter extends BasePresenter{

        void start();

        List<String> getDevices();

        void stop();
    }
}
