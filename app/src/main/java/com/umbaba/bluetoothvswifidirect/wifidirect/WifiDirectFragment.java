package com.umbaba.bluetoothvswifidirect.wifidirect;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;
import com.nlt.mobileteam.wifidirect.utils.DeviceList;
import com.umbaba.bluetoothvswifidirect.R;
import com.umbaba.bluetoothvswifidirect.testdata.TestFileModel;

import java.util.ArrayList;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;


public class WifiDirectFragment extends Fragment implements WifiDirectContract.View {

    public static final int ID = 234;
    private static final String TAG = "WifiDirectFragment";

    private Button startAssistant;
    private Button startDirector;
    private Button stop;

    private View btn5Mb;
    private View btn10mb;
    private View btn20mb;

    private WifiDirectContract.Presenter mPresenter;
    private LinearLayout sendGroup;

    public WifiDirectFragment() {

    }

    public static WifiDirectFragment newInstance() {
        WifiDirectFragment fragment = new WifiDirectFragment();
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
        mPresenter.pause();
    }


    @Override
    public void setPresenter(@NonNull WifiDirectContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflate = inflater.inflate(R.layout.fragment_wifi_direct, container, false);
        startAssistant = (Button) inflate.findViewById(R.id.start_assistant);
        startDirector = (Button) inflate.findViewById(R.id.start_director);
        stop = (Button) inflate.findViewById(R.id.stop);
        sendGroup = (LinearLayout) inflate.findViewById(R.id.send_group);
        return inflate;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        startAssistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDirector.setVisibility(View.GONE);
                startAssistant.setVisibility(View.GONE);
                stop.setVisibility(View.VISIBLE);
                mPresenter.startAssistant();
            }
        });
        startDirector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDirector.setVisibility(View.GONE);
                startAssistant.setVisibility(View.GONE);
                stop.setVisibility(View.VISIBLE);
                mPresenter.startDirector();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.pause();
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
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
                view.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_red_dark));

            }
        };
        btn5Mb = sendGroup.findViewById(R.id.btn5mb);
        btn5Mb.setOnClickListener(onClickListener);
        btn10mb = sendGroup.findViewById(R.id.btn10mb);
        btn10mb.setOnClickListener(onClickListener);
        btn20mb = sendGroup.findViewById(R.id.btn20mb);
        btn20mb.setOnClickListener(onClickListener);
    }


    @Override
    public void setDeviceName(String deviceName) {
        stop.setText(deviceName);
    }

    @Override
    public void setSuccessedTransfer(int size) {
        switch (size) {
            case TestFileModel.FILE_5:
                btn5Mb.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
                break;
            case TestFileModel.FILE_10:
                btn10mb.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
                break;
            case TestFileModel.FILE_20:
                btn20mb.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
                break;
        }
    }

}

