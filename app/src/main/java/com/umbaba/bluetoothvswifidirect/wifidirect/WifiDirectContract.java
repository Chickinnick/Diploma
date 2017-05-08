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

        void enableSend();

        void setDeviceName(String deviceName);
    }

    interface Presenter {

        void pause();

        void destroy();

        void sendFile(int size);

        void startDirector();

        void startAssistant();
    }
}
