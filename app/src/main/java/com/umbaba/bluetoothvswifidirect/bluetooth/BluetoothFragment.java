package com.umbaba.bluetoothvswifidirect.bluetooth;


import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.umbaba.bluetoothvswifidirect.R;

import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;


public class BluetoothFragment extends Fragment implements BluetoothContract.View{


    private Button start;
    private Button stop;
    private ListView result;

    private BluetoothContract.Presenter mPresenter;

    public BluetoothFragment() {

    }
    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }


    @Override
    public void setPresenter (@NonNull BluetoothContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflate = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        start = (Button) inflate.findViewById(R.id.start);
        stop = (Button) inflate.findViewById(R.id.stop);
        result = (ListView) inflate.findViewById(R.id.result);

        return inflate;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        start.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mPresenter.start();
                setAdapter(mPresenter.getDevices());

            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mPresenter.stop();
            }
        });
    }

    @Override
    public void discoverStarted() {
        start.setText(R.string.button_searching);
    }


    @Override
    public void discoverFinished() {
        start.setText(R.string.button_restart);
    }

    @Override
    public void stateOn() {
        start.setBackgroundColor(getResources().getColor(R.color.colorActive));
    }

    @Override
    public void stateOff() {
        start.setBackgroundColor(getResources().getColor(R.color.colorInactive));
    }

    @Override
    public void addDevice(BluetoothDevice device) {
        String deviceName;
        deviceName = device.getAddress();
        if (!TextUtils.isEmpty(device.getName())) {
            deviceName += " " + device.getName();
        }
         setAdapter(mPresenter.getDevices());
    }

    private void setAdapter(List<String> list) {
        int itemLayoutId = android.R.layout.simple_list_item_1;
        result.setAdapter(new ArrayAdapter<String>(getActivity(), itemLayoutId, list));
    }


}
