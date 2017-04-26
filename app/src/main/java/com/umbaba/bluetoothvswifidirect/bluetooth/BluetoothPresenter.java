package com.umbaba.bluetoothvswifidirect.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.github.ivbaranov.rxbluetooth.Action;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.umbaba.bluetoothvswifidirect.R;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Nick on 17.04.2017.
 */

public class BluetoothPresenter implements BluetoothContract.Presenter {

    private static final int REQUEST_ENABLE_BT = 1;
    private final BluetoothContract.View view;

    private static final String TAG = "BluetoothPresenter";
    private RxBluetooth rxBluetooth;
    private Subscription deviceSubscription;
    private Subscription discoveryStartSubscription;
    private Subscription discoveryFinishSubscription;
    private Subscription bluetoothStateOnSubscription;
    private Subscription bluetoothStateOtherSubscription;
    private List<BluetoothDevice> devices = new ArrayList<>();


    public BluetoothPresenter(Activity activity, BluetoothContract.View view) {

        rxBluetooth = new RxBluetooth(activity);
        if (!rxBluetooth.isBluetoothEnabled()) {
            rxBluetooth.enableBluetooth(activity, REQUEST_ENABLE_BT);
        }
        this.view = checkNotNull(view);
        this.view.setPresenter(this);


    }

    @Override
    public void subscribe() {
        deviceSubscription = rxBluetooth.observeDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(new Action1<BluetoothDevice>() {
                    @Override public void call(BluetoothDevice bluetoothDevice) {
                        devices.add(bluetoothDevice);
                        view.addDevice(bluetoothDevice);
                    }
                });

        discoveryStartSubscription = rxBluetooth.observeDiscovery()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(Action.isEqualTo(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
                .subscribe(new Action1<String>() {
                    @Override public void call(String action) {
                        view.discoverStarted();
                    }
                });

        discoveryFinishSubscription = rxBluetooth.observeDiscovery()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(Action.isEqualTo(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
                .subscribe(new Action1<String>() {
                    @Override public void call(String action) {
                        view.discoverFinished();
                    }
                });

        bluetoothStateOnSubscription = rxBluetooth.observeBluetoothState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(Action.isEqualTo(BluetoothAdapter.STATE_ON))
                .subscribe(new Action1<Integer>() {
                    @Override public void call(Integer integer) {
                        view.stateOn();
                    }
                });

        bluetoothStateOtherSubscription = rxBluetooth.observeBluetoothState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(Action.isEqualTo(BluetoothAdapter.STATE_OFF, BluetoothAdapter.STATE_TURNING_OFF,
                        BluetoothAdapter.STATE_TURNING_ON))
                .subscribe(new Action1<Integer>() {
                    @Override public void call(Integer integer) {
                        view.stateOff();
                    }
                });
    }

    @Override
    public void unsubscribe() {
        if (rxBluetooth != null) {
            // Make sure we're not doing discovery anymore
            rxBluetooth.cancelDiscovery();
        }

        unsubscribe(deviceSubscription);
        unsubscribe(discoveryStartSubscription);
        unsubscribe(discoveryFinishSubscription);
        unsubscribe(bluetoothStateOnSubscription);
        unsubscribe(bluetoothStateOtherSubscription);
    }

    private static void unsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void start() {
        devices.clear();
        rxBluetooth.startDiscovery();
    }

    @Override
    public void stop() {
        rxBluetooth.cancelDiscovery();
    }

    @Override
    public void itemSelected(int position) {
        BluetoothDevice bluetoothDevice = devices.get(position);
    }

    @Override
    public List<BluetoothDevice> getDevices() {
        return devices;
    }
}
