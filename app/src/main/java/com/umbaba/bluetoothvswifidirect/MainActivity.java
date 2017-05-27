package com.umbaba.bluetoothvswifidirect;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devpaul.bluetoothutillib.dialogs.DeviceDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.umbaba.bluetoothvswifidirect.bluetooth.BluetoothFragment;
import com.umbaba.bluetoothvswifidirect.bluetooth.BluetoothPresenter;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationContract;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationFragment;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationPresenter;
import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationModel;
import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationRepository;
import com.umbaba.bluetoothvswifidirect.testdata.DefaultFileData;
import com.umbaba.bluetoothvswifidirect.testdata.FakeFileData;
import com.umbaba.bluetoothvswifidirect.testdata.TestFileModel;
import com.umbaba.bluetoothvswifidirect.util.ActivityUtils;
import com.umbaba.bluetoothvswifidirect.wifidirect.WifiDirectFragment;
import com.umbaba.bluetoothvswifidirect.wifidirect.WifiDirectPresenter;

import java.io.File;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;

import static com.umbaba.bluetoothvswifidirect.bluetooth.BluetoothPresenter.BLE_FILE_SEND;
import static com.umbaba.bluetoothvswifidirect.bluetooth.BluetoothPresenter.CHOOSE_SERVER_REQUEST;
import static com.umbaba.bluetoothvswifidirect.bluetooth.BluetoothPresenter.SCAN_REQUEST;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";
    private Button testWifi;
    private LinearLayout navigationBottomLayout;
    private Button testBluetooth;
    private CircleProgressView circleProgressView;

    private ComparationPresenter comparationPresenter;
    private TestFileModel testFileModel;
    private WifiDirectPresenter wifiDirectPresenter;
    private BluetoothPresenter bluetoothPresenter;
    private ComparationFragment comparationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupComparation();
        initMainFlow();
        testFileModel = new FakeFileData(getResources());
//        testFileModel = new DefaultFileData(getResources());
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE).
                withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {}
                }).check();

    }

    private void initMainFlow() {
        testBluetooth = (Button) findViewById(R.id.test_bluetooth_btn);
        circleProgressView = (CircleProgressView) findViewById(R.id.circleView);
        navigationBottomLayout = (LinearLayout) findViewById(R.id.navigation_btns);
        testWifi = (Button) findViewById(R.id.test_wifi_btn);
        testBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBluetoothTest();

            }
        });

        testWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWifiTest();

            }
        });
    }

    private void showBluetoothTest() {
        BluetoothFragment bluetoothFragment =
                (BluetoothFragment) getSupportFragmentManager().findFragmentById(BluetoothFragment.ID);
        if (bluetoothFragment == null) {
            bluetoothFragment = BluetoothFragment.newInstance();
            ActivityUtils.replaceFragmentByID(getSupportFragmentManager(), bluetoothFragment, R.id.contentFrame);
        }
        comparationPresenter.start(ComparationContract.Presenter.TYPE_BLUETOOTH);
        bluetoothPresenter = new BluetoothPresenter(this, bluetoothFragment, testFileModel, comparationPresenter , circleProgressView);
        bluetoothPresenter.setOnWorkFinishedCallback(new OnWorkFinishedCallback() {
            @Override
            public void onWorkFinished() {
                bluetoothPresenter.stop();
                showWifiTest();
            }
        });

    }
    private void showWifiTest() {
        WifiDirectFragment wifiDirectFragment =
                (WifiDirectFragment) getSupportFragmentManager().findFragmentById(WifiDirectFragment.ID);
        if (wifiDirectFragment == null) {
            wifiDirectFragment = WifiDirectFragment.newInstance();
            ActivityUtils.replaceFragmentByID(getSupportFragmentManager(), wifiDirectFragment, R.id.contentFrame);
        }
        comparationPresenter.start(ComparationContract.Presenter.TYPE_WIFI);
        wifiDirectPresenter = new WifiDirectPresenter(this, wifiDirectFragment, testFileModel, comparationPresenter, circleProgressView);
        wifiDirectPresenter.setOnWorkFinishedCallback(new OnWorkFinishedCallback() {
            @Override
            public void onWorkFinished() {
                wifiDirectPresenter.destroy();
                showComparationView();
            }
        });
    }

    private void setupComparation() {
        ComparationFragment comparationFragment = showComparationView();
        comparationPresenter = new ComparationPresenter(this, comparationFragment);
    }

    @NonNull
    private ComparationFragment showComparationView() {
        ComparationFragment comparationFragment =
                (ComparationFragment) getSupportFragmentManager().findFragmentById(ComparationFragment.ID);
        if (comparationFragment == null) {
            comparationFragment = ComparationFragment.newInstance();
            ActivityUtils.replaceFragmentByID(getSupportFragmentManager(), comparationFragment, R.id.contentFrame);
            comparationFragment.setPresenter(comparationPresenter);
        }
        return comparationFragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int distance = 0;
        switch (item.getItemId()) {
            case R.id.distance_1:
                distance = 1;
                break;
            case R.id.distance_5:
                distance = 5;
                break;
            case R.id.distance_10:
                distance = 10;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        comparationPresenter.setCurrentDistance(distance);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SCAN_REQUEST || requestCode == CHOOSE_SERVER_REQUEST) {
            bluetoothPresenter.handleActivityResult(requestCode, resultCode , data);
        }
    };


}
