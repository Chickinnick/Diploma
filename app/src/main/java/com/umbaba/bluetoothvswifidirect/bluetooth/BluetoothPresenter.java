package com.umbaba.bluetoothvswifidirect.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.devpaul.bluetoothutillib.SimpleBluetooth;
import com.devpaul.bluetoothutillib.dialogs.DeviceDialog;
import com.devpaul.bluetoothutillib.utils.SimpleBluetoothListener;
import com.umbaba.bluetoothvswifidirect.OnWorkFinishedCallback;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationPresenter;
import com.umbaba.bluetoothvswifidirect.testdata.TestFileModel;

import java.io.File;

import at.grabner.circleprogress.CircleProgressView;

import static android.app.Activity.RESULT_OK;
import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;
import static com.umbaba.bluetoothvswifidirect.OnWorkFinishedCallback.MAX_TESTS;

/**
 * Created by Nick on 17.04.2017.
 */

public class BluetoothPresenter implements BluetoothContract.Presenter {

    private static final int REQUEST_ENABLE_BT = 1;
    public static final int BLE_FILE_SEND = 123;
    private final BluetoothContract.View view;

    private static final String TAG = "BluetoothPresenter";
    private final TestFileModel fileModel;
    private final ComparationPresenter comparationPresenter;
    private final CircleProgressView circleProgressView;
    private Activity activity;
    boolean isConnected;

    public static final int SCAN_REQUEST = 119;
    public static final int CHOOSE_SERVER_REQUEST = 120;
    private SimpleBluetooth simpleBluetooth;
    private String curMacAddress;

    int testsCounter = -1;
    private OnWorkFinishedCallback onWorkFinishedCallback;

    public BluetoothPresenter(Activity activity, BluetoothContract.View view, TestFileModel testFileModel, ComparationPresenter comparationPresenter, CircleProgressView circleProgressView) {
        this.activity = activity;
        this.circleProgressView = circleProgressView;
        this.view = checkNotNull(view);
        this.view.setPresenter(this);
        this.start();
        this.fileModel = testFileModel;
        this.comparationPresenter = comparationPresenter;
        testsCounter = 0;
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
            simpleBluetooth.makeDiscoverable(60000);
        }
        simpleBluetooth.initializeSimpleBluetooth();
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
    public void sendFile(final int size) {
        File file = fileModel.getFile(size);
        circleProgressView.setVisibility(View.VISIBLE);
        comparationPresenter.startTransfer(size);
        simpleBluetooth.sendData(file , new SimpleBluetooth.OnProgressUpdateListener() {
            @Override
            public void onProgressUpdate(int progress) {
                circleProgressView.setValue(progress);
            }

            @Override
            public void onTransferSuccess(long fileLength) {
                circleProgressView.setVisibility(View.GONE);
                view.setSuccessedTransfer(size);
                comparationPresenter.stopTransfer(fileLength);
                testsCounter++;
                checkTestFinish();
            }
        });
    }

    private void checkTestFinish() {
        if(testsCounter >= MAX_TESTS){
            if (onWorkFinishedCallback != null) {
                onWorkFinishedCallback.onWorkFinished();
            }
        }
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

    public void setOnWorkFinishedCallback(OnWorkFinishedCallback onWorkFinishedCallback) {
        this.onWorkFinishedCallback = onWorkFinishedCallback;
    }
}
