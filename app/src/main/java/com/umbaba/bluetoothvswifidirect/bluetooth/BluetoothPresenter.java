package com.umbaba.bluetoothvswifidirect.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.devpaul.bluetoothutillib.SimpleBluetooth;
import com.devpaul.bluetoothutillib.dialogs.DeviceDialog;
import com.devpaul.bluetoothutillib.utils.BluetoothUtility;
import com.devpaul.bluetoothutillib.utils.SimpleBluetoothListener;
import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationModel;
import com.umbaba.bluetoothvswifidirect.testdata.TestFileModel;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
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
    boolean isConnected;

    public static final int SCAN_REQUEST = 119;
    public static final int CHOOSE_SERVER_REQUEST = 120;
    private SimpleBluetooth simpleBluetooth;
    private String curMacAddress;


    public BluetoothPresenter(Activity activity, BluetoothContract.View view, TestFileModel testFileModel, ComparationModel comparationModel) {

        this.activity = activity;

        this.view = checkNotNull(view);
        this.view.setPresenter(this);
        this.fileModel = testFileModel;
        this.comparationModel = comparationModel;
    }



    @Override
    public void start() {
        if(simpleBluetooth == null) {
            simpleBluetooth = new SimpleBluetooth(activity, new SimpleBluetoothListener() {
                @Override
                public void onBluetoothDataReceived(byte[] bytes, String data) {
                    isConnected = false;
                    Log.i(TAG, "onBluetoothDataReceived:"+ data +"b:  "+ bytes);
                }

                @Override
                public void onDeviceConnected(BluetoothDevice device) {
                    view.setDeviceName(device.getName() + " / "+ device.getAddress());
                    view.enableSend();
                    isConnected = true;
                }

                @Override
                public void onDeviceDisconnected(BluetoothDevice device) {
                    // device was disconnected so connect it again?

                }
            });
        }
        simpleBluetooth.initializeSimpleBluetooth();
        simpleBluetooth.setInputStreamType(BluetoothUtility.InputStreamType.BUFFERED);

    }

    @Override
    public void stop() {
        simpleBluetooth.endSimpleBluetooth();
    }


    @Override
    public void startAsServer() {
        simpleBluetooth.createBluetoothServerConnection();
    }

    @Override
    public void connectToServer() {
        if(curMacAddress != null) {
            simpleBluetooth.connectToBluetoothServer(curMacAddress);
        } else {
            simpleBluetooth.scan(CHOOSE_SERVER_REQUEST);
        }
    }

    @Override
    public void sendFile(int size) {
        File file = fileModel.getFile(size);
        simpleBluetooth.sendData(file);
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            curMacAddress = data.getStringExtra(DeviceDialog.DEVICE_DIALOG_DEVICE_ADDRESS_EXTRA);
            boolean paired = simpleBluetooth.getBluetoothUtility()
                    .checkIfPaired(simpleBluetooth.getBluetoothUtility()
                            .findDeviceByMacAddress(curMacAddress));
            String message = paired ? "is paired" : "is not paired";
            Log.i("ActivityResult", "Device " + message);
            if(requestCode == SCAN_REQUEST) {
                simpleBluetooth.connectToBluetoothDevice(curMacAddress);
            } else {
                simpleBluetooth.connectToBluetoothServer(curMacAddress);
            }
        }
    }
}
