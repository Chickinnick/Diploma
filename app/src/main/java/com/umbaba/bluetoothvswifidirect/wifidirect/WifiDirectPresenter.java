package com.umbaba.bluetoothvswifidirect.wifidirect;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.nlt.mobileteam.wifidirect.WifiDirect;
import com.nlt.mobileteam.wifidirect.controller.wifi.WiFiP2pDirector;
import com.nlt.mobileteam.wifidirect.listeners.AssistantActionListener;
import com.nlt.mobileteam.wifidirect.listeners.DirectorActionListener;
import com.nlt.mobileteam.wifidirect.listeners.TransferingActionListener;
import com.nlt.mobileteam.wifidirect.model.InstanceCode;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;
import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorConnect;
import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorDisconnect;
import com.nlt.mobileteam.wifidirect.model.event.assistant.OwnerName;
import com.nlt.mobileteam.wifidirect.model.event.director.NotifyDeviceList;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Abort;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Progress;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Success;
import com.nlt.mobileteam.wifidirect.utils.DeviceList;
import com.umbaba.bluetoothvswifidirect.OnWorkFinishedCallback;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationPresenter;
import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationModel;
import com.umbaba.bluetoothvswifidirect.testdata.TestFileModel;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;
import static com.umbaba.bluetoothvswifidirect.OnWorkFinishedCallback.MAX_TESTS;


public class WifiDirectPresenter implements WifiDirectContract.Presenter {

    private final WifiDirectContract.View view;
    private ComparationPresenter comparationPresenter;
    private static final String TAG = "WifiDirectPresenter";
    private final TestFileModel fileModel;
    private CircleProgressView circleProgressView;
    private Activity activity;
    private WifiDirect wifiDirect;

    int testsCounter = -1;

    private OnWorkFinishedCallback onWorkFinishedCallback;

    public WifiDirectPresenter(Activity activity, WifiDirectContract.View view, TestFileModel testFileModel, ComparationPresenter comparationPresenter, CircleProgressView circleProgressView) {
        this.activity = activity;
        this.view = checkNotNull(view);
        this.comparationPresenter = comparationPresenter;
        this.view.setPresenter(this);
        this.fileModel = testFileModel;
        this.circleProgressView = circleProgressView;
        testsCounter = 0;

    }

    @Override
    public void pause() {
        wifiDirect.onPause();
    }

    @Override
    public void destroy() {
        wifiDirect.destroy();
    }


    @Override
    public void sendFile(final int size) {
        final File file = fileModel.getFile(size);
        TransferingActionListener transferingActionListener = new TransferingActionListener() {
            @Override
            public void fileAborted(Abort event) {

            }

            @Override
            public void doInProgress(Progress progress) {
                circleProgressView.setValue(progress.totalProgress);
            }

            @Override
            public void onSuccessed(Success event) {
                circleProgressView.setVisibility(View.GONE);
                view.setSuccessedTransfer(size);
                comparationPresenter.stopTransfer(file.length());
                testsCounter++;
                checkTestFinish();
            }
        };
        wifiDirect.sendFile(file, transferingActionListener);
    }

    private void checkTestFinish() {
        if(testsCounter >= MAX_TESTS){
            if (onWorkFinishedCallback != null) {
                onWorkFinishedCallback.onWorkFinished();
            }
        }
    }

    @Override
    public void startDirector() {
        wifiDirect = WifiDirect.init(activity, InstanceCode.DIRECTOR);
        wifiDirect.addOnFileTransferListener(new TransferingActionListener() {
            @Override
            public void fileAborted(Abort event) {
                Log.e(TAG, "fileAborted: " + event.getDeviceIndex());
            }

            @Override
            public void doInProgress(Progress progress) {
                Log.i(TAG, "doInProgress: " + progress);
            }

            @Override
            public void onSuccessed(Success event) {
                Log.i(TAG, "onSuccessed:" + event);
            }
        });
    }

    @Override
    public void startAssistant() {
        wifiDirect = WifiDirect.init(activity, InstanceCode.ASSISTANT);
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
                view.enableSend();
            }
        };
        wifiDirect.setActionListener(actionListener);
    }

    public void setOnWorkFinishedCallback(OnWorkFinishedCallback onWorkFinishedCallback) {
        this.onWorkFinishedCallback = onWorkFinishedCallback;
    }
}
