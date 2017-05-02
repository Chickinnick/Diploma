package com.umbaba.bluetoothvswifidirect.wifidirect;

import android.bluetooth.BluetoothDevice;
import android.net.wifi.p2p.WifiP2pDevice;

import com.umbaba.bluetoothvswifidirect.BaseView;

import java.util.List;
import java.util.Set;

/**
 * Created by Nick on 17.04.2017.
 */

public interface WifiDirectContract {
    interface View extends BaseView<Presenter> {


        void addDevice(WifiP2pDevice wifiP2pDevice);

        void discoverStarted();

        void discoverFinished();

        void stateOn();

        void stateOff();

        void enableSend();

    }

    interface Presenter {


        List<WifiP2pDevice> getDevices();

        void stop();

        void itemSelected(int position);

        void sendFile(int size);

        void startDirector();

        void startAssistant();
    }
}
