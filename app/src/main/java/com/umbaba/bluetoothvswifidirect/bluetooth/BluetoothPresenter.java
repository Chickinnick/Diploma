package com.umbaba.bluetoothvswifidirect.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;

import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationModel;
import com.umbaba.bluetoothvswifidirect.testdata.TestFileModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Nick on 17.04.2017.
 */

public class BluetoothPresenter implements BluetoothContract.Presenter {

    private static final int REQUEST_ENABLE_BT = 1;
    public static final int BLE_FILE_SEND = 123;
    private final BluetoothContract.View view;

    private static final String TAG = "BluetoothPresenter";
    private final TestFileModel fileModel;
    private final ComparationModel comparationModel;
    private Activity activity;
    private List<BluetoothDevice> devices = new ArrayList<>();


    public BluetoothPresenter(Activity activity, BluetoothContract.View view, TestFileModel testFileModel, ComparationModel comparationModel) {

        this.activity = activity;

        this.view = checkNotNull(view);
        this.view.setPresenter(this);
        this.fileModel = testFileModel;
        this.comparationModel = comparationModel;
    }



    @Override
    public void start() {


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
    public List<BluetoothDevice> getDevices() {
        return devices;
    }
}
