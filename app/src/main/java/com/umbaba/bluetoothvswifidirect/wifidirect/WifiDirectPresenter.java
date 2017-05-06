package com.umbaba.bluetoothvswifidirect.wifidirect;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.nlt.mobileteam.wifidirect.WifiDirect;
import com.nlt.mobileteam.wifidirect.listeners.AssistantActionListener;
import com.nlt.mobileteam.wifidirect.listeners.DirectorActionListener;
import com.nlt.mobileteam.wifidirect.model.InstanceCode;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;
import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorConnect;
import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorDisconnect;
import com.nlt.mobileteam.wifidirect.model.event.assistant.OwnerName;
import com.nlt.mobileteam.wifidirect.model.event.director.NotifyDeviceList;
import com.nlt.mobileteam.wifidirect.utils.DeviceList;
import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationModel;
import com.umbaba.bluetoothvswifidirect.testdata.TestFileModel;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Nick on 17.04.2017.
 */

public class WifiDirectPresenter implements WifiDirectContract.Presenter {

    private static final int REQUEST_ENABLE_BT = 1;
    public static final int BLE_FILE_SEND = 123;
    private final WifiDirectContract.View view;

    private static final String TAG = "WifiDirectPresenter";
    private final TestFileModel fileModel;
    private final ComparationModel comparationModel;
    private Activity activity;
    private DeviceList devices;
    private WifiDirect wifiDirect;

    public WifiDirectPresenter(Activity activity, WifiDirectContract.View view, TestFileModel testFileModel, ComparationModel comparationModel) {
        this.activity = activity;
        this.view = checkNotNull(view);
        this.view.setPresenter(this);
        this.fileModel = testFileModel;
        this.comparationModel = comparationModel;
    }

    @Override
    public void pause() {
        wifiDirect.onPause();
    }

    @Override
    public void destroy(){
        wifiDirect.destroy();
    }


    @Override
    public void itemSelected(int position) {
        view.enableSend();
    }


    @Override
    public void sendFile(int size) {
        File file = fileModel.getFile(size);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("*/*");
        sharingIntent.setPackage("com.android.bluetooth");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

//        starttime = System.currentTimeMillis();
//        fileSize = Long.valueOf(file.length());
        activity.startActivityForResult(
                Intent.createChooser(sharingIntent, "Share file"),
                BLE_FILE_SEND);
    }

    @Override
    public void startDirector() {
        wifiDirect = WifiDirect.init(activity , InstanceCode.DIRECTOR);
        DirectorActionListener actionListener = new DirectorActionListener() {
            @Override
            public void handleDeviceList(NotifyDeviceList event) {
                devices = event.getDeviceList();
                if (devices == null) {
                    view.refreshDevices();
                } else {
                    view.setDevices(devices);
                }
            }
        };
        wifiDirect.setActionListener(actionListener);
    }

    @Override
    public void startAssistant() {
        wifiDirect = WifiDirect.init(activity , InstanceCode.ASSISTANT);
        AssistantActionListener actionListener = new AssistantActionListener() {
            @Override
            public void directorConnected(DirectorConnect event) {
            }

            @Override
            public void directorDisconnected(DirectorDisconnect event) {

            }

            @Override
            public void ownerName(OwnerName event) {
                view.setDeviceName(event.getOwnerName());

            }
        };
        wifiDirect.setActionListener(actionListener);
    }

    @Override
    public List<WiFiP2pService> getDevices() {
        return devices.getTrimList();
    }
}
