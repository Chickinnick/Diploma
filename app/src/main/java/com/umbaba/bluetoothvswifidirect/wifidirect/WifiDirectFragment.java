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
    private RecyclerView recyclerView;

    private WifiDirectContract.Presenter mPresenter;
    private RVAdapter adapter;
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
        recyclerView = (RecyclerView) inflate.findViewById(R.id.result);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
                initAdaper();
            }
        });
        startDirector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDirector.setVisibility(View.GONE);
                startAssistant.setVisibility(View.GONE);
                stop.setVisibility(View.VISIBLE);
                mPresenter.startDirector();
                initAdaper();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.pause();
            }
        });
    }

    private void initAdaper() {
        adapter = new RVAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void discoverStarted() {

    }


    @Override
    public void discoverFinished() {
    }

    @Override
    public void stateOn() {

    }

    @Override
    public void stateOff() {

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
            }
        };
        sendGroup.findViewById(R.id.btn5mb).setOnClickListener(onClickListener);
        sendGroup.findViewById(R.id.btn10mb).setOnClickListener(onClickListener);
        sendGroup.findViewById(R.id.btn20mb).setOnClickListener(onClickListener);
    }

    @Override
    public void setDevices(DeviceList devices) {
        adapter.clear();
        List<WiFiP2pService> trimList = devices.getTrimList();
        if (trimList == null) return;
        for (WiFiP2pService wiFiP2pService : trimList) {
            String instanceName = wiFiP2pService.instanceName;
            adapter.add(instanceName);
            Log.i(TAG, "addDevice: " + instanceName);
        }

    }

    @Override
    public void refreshDevices() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void setDeviceName(String deviceName) {
        stop.setText(deviceName);
    }

    public static class RVAdapter extends RecyclerView.Adapter<WifiDirectFragment.RVAdapter.BluetoothViewHolder> {
        private OnItemClickListener onItemClickListener;


        List<String> devices;

        public RVAdapter() {
            this.devices = new ArrayList<>();
        }

        @Override
        public WifiDirectFragment.RVAdapter.BluetoothViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bluetooth, parent, false);
            return new WifiDirectFragment.RVAdapter.BluetoothViewHolder(v);
        }

        @Override
        public void onBindViewHolder(WifiDirectFragment.RVAdapter.BluetoothViewHolder holder, int position) {
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

        public void clear() {
            devices.clear();
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

