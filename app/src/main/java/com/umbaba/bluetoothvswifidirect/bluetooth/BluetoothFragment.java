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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umbaba.bluetoothvswifidirect.R;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationFragment;
import com.umbaba.bluetoothvswifidirect.testdata.TestFileModel;

import java.util.ArrayList;
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
        sendGroup = (LinearLayout) inflate.findViewById(R.id.send_group);
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
                initAdaper();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.stop();
            }
        });
    }

    private void initAdaper() {
        adapter = new RVAdapter();
        adapter.setOnItemClickListener(new RVAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, View view) {
                mPresenter.itemSelected(position);
            }
        });
        recyclerView.setAdapter(adapter);
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


    public static class RVAdapter extends RecyclerView.Adapter<BluetoothFragment.RVAdapter.BluetoothViewHolder> {
        private OnItemClickListener onItemClickListener;


        List<String> devices;

        public RVAdapter() {
            this.devices = new ArrayList<>();
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
            holder.bindListener(position, onItemClickListener);


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


            public void bindListener(final int position, final OnItemClickListener onItemClickListener) {
                device.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClicked(position, v);
                        }
                    }
                });
            }
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public interface OnItemClickListener {
            void onItemClicked(int position, View view);
        }
    }
}

