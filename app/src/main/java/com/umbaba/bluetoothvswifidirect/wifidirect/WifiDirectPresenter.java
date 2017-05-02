package com.umbaba.bluetoothvswifidirect.wifidirect;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;

import com.nlt.mobileteam.wifidirect.WifiDirect;
import com.nlt.mobileteam.wifidirect.model.InstanceCode;
import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationModel;
import com.umbaba.bluetoothvswifidirect.testdata.TestFileModel;

import java.io.File;
import java.util.ArrayList;
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
    private List<WifiP2pDevice> devices = new ArrayList<>();
    private WifiDirect wifiDirect;

    public WifiDirectPresenter(Activity activity, WifiDirectContract.View view, TestFileModel testFileModel, ComparationModel comparationModel) {
        this.activity = activity;
        this.view = checkNotNull(view);
        this.view.setPresenter(this);
        this.fileModel = testFileModel;
        this.comparationModel = comparationModel;
    }

    @Override
    public void stop() {
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
    }

    @Override
    public void startAssistant() {
        wifiDirect = WifiDirect.init(activity , InstanceCode.ASSISTANT);
    }

    @Override
    public List<WifiP2pDevice> getDevices() {
        return devices;
    }
}
