package com.umbaba.bluetoothvswifidirect.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.umbaba.bluetoothvswifidirect.BaseView;

import java.util.List;
import java.util.Set;

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

        void enableSend();

        void addDevices(Set<BluetoothDevice> bluetoothDevices);
    }

    interface Presenter  {

        void start();

        List<BluetoothDevice> getDevices();

        void stop();

        void itemSelected(int position);

        void sendFile(int size);

        void startAsServer();

        void connectToServer();
    }
}
