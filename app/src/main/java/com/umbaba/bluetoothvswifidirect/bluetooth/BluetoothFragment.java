package com.umbaba.bluetoothvswifidirect.bluetooth;


import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.umbaba.bluetoothvswifidirect.R;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationFragment;

import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;
import static java.util.Collections.EMPTY_LIST;


public class BluetoothFragment extends Fragment implements BluetoothContract.View {

    public static final int ID = 12;
    private static final String TAG = "BluetoothFragment";
    private Button start;
    private Button stop;
    private RecyclerView recyclerView;

    private BluetoothContract.Presenter mPresenter;
    private RVAdapter adapter;

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
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }


    @Override
    public void setPresenter(@NonNull BluetoothContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflate = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        start = (Button) inflate.findViewById(R.id.start);
        stop = (Button) inflate.findViewById(R.id.stop);
        recyclerView = (RecyclerView) inflate.findViewById(R.id.result);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return inflate;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.start();

                adapter = new BluetoothFragment.RVAdapter(mPresenter.getDevices());
                recyclerView.setAdapter(adapter);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        Log.i(TAG, "addDevice: " + deviceName);
         adapter.add(deviceName);
    }


    public class RVAdapter extends RecyclerView.Adapter<BluetoothFragment.RVAdapter.BluetoothViewHolder> {


        List<String> devices;

        public RVAdapter(List<String> devices) {
            this.devices = devices;
        }

        @Override
        public BluetoothFragment.RVAdapter.BluetoothViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bluetooth, parent, false);
            return new BluetoothFragment.RVAdapter.BluetoothViewHolder(v);
        }

        @Override
        public void onBindViewHolder(BluetoothFragment.RVAdapter.BluetoothViewHolder holder, int position) {
            String device = devices.get(position);
            holder.device.setText(device);

        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        public void setData(List<String> data) {
            this.devices = data;
        }

        public void add(String device) {
            this.devices.add(device);
            notifyDataSetChanged();
        }

        public class BluetoothViewHolder extends RecyclerView.ViewHolder {

            TextView device;

            BluetoothViewHolder(View itemView) {
                super(itemView);
                device = (TextView) itemView.findViewById(R.id.device);
            }
        }


    }
}

