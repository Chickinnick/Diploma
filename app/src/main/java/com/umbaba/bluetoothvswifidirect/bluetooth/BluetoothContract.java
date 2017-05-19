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

        void setDeviceName(String name);

        void enableSend();

        void setSuccessedTransfer(int size);
    }

    interface Presenter  {

        void start();


        void stop();


        void sendFile(int size);

        void startAsServer();

        void connectToServer();
    }
}
