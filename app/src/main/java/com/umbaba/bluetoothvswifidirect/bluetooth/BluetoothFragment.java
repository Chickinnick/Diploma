package com.umbaba.bluetoothvswifidirect.bluetooth;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.umbaba.bluetoothvswifidirect.R;
import com.umbaba.bluetoothvswifidirect.testdata.TestFileModel;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;


public class BluetoothFragment extends Fragment implements BluetoothContract.View {

    public static final int ID = 12;
    private static final String TAG = "BluetoothFragment";
    private Button start;
    private Button stop;
    private Button connectToServer;

    private BluetoothContract.Presenter mPresenter;
    private LinearLayout sendGroup;

    public BluetoothFragment() {

    }

    public static BluetoothFragment newInstance() {
        BluetoothFragment fragment = new BluetoothFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void setPresenter(@NonNull BluetoothContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflate = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        start = (Button) inflate.findViewById(R.id.start_server);
        stop = (Button) inflate.findViewById(R.id.stop);
        connectToServer = (Button) inflate.findViewById(R.id.connect_to_server);
        sendGroup = (LinearLayout) inflate.findViewById(R.id.send_group);
        return inflate;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.start();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.startAsServer();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.stop();
            }
        });
        connectToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.connectToServer();
            }
        });
    }


    @Override
    public void setDeviceName(String name) {

    }

    @Override
    public void enableSend() {
        sendGroup.setVisibility(View.VISIBLE);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn5mb:
                        mPresenter.sendFile(TestFileModel.FILE_5);
                        break;
                    case R.id.btn10mb:
                        mPresenter.sendFile(TestFileModel.FILE_10);
                        break;
                    case R.id.btn20mb:
                        mPresenter.sendFile(TestFileModel.FILE_20);
                        break;
                }
            }
        };
        sendGroup.findViewById(R.id.btn5mb).setOnClickListener(onClickListener);
        sendGroup.findViewById(R.id.btn10mb).setOnClickListener(onClickListener);
        sendGroup.findViewById(R.id.btn20mb).setOnClickListener(onClickListener);
    }


}

