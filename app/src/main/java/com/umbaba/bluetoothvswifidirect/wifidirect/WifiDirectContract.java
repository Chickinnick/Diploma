package com.umbaba.bluetoothvswifidirect.wifidirect;

import android.bluetooth.BluetoothDevice;
import android.net.wifi.p2p.WifiP2pDevice;

import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;
import com.nlt.mobileteam.wifidirect.utils.DeviceList;
import com.umbaba.bluetoothvswifidirect.BaseView;

import java.util.List;
import java.util.Set;

/**
 * Created by Nick on 17.04.2017.
 */

public interface WifiDirectContract {
    interface View extends BaseView<Presenter> {


        void discoverStarted();

        void discoverFinished();

        void stateOn();

        void stateOff();

        void enableSend();

        void setDevices(DeviceList devices);

        void refreshDevices();
    }

    interface Presenter {


        List<WiFiP2pService> getDevices();

        void pause();

        void destroy();

        void itemSelected(int position);

        void sendFile(int size);

        void startDirector();

        void startAssistant();
    }
}
