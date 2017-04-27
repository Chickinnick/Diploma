package com.umbaba.bluetoothvswifidirect;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.umbaba.bluetoothvswifidirect.bluetooth.BluetoothFragment;
import com.umbaba.bluetoothvswifidirect.bluetooth.BluetoothPresenter;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationFragment;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationPresenter;
import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationModel;
import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationRepository;
import com.umbaba.bluetoothvswifidirect.testdata.DefaultFileData;
import com.umbaba.bluetoothvswifidirect.testdata.TestFileModel;
import com.umbaba.bluetoothvswifidirect.util.ActivityUtils;

public class MainActivity extends FragmentActivity {

    private Button testWifi;
    private Button testBluetooth;

    private ComparationPresenter comparationPresenter;
    private BluetoothPresenter bluetoothPresenter;
    private TestFileModel testFileModel;
    private ComparationModel comparationModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupComparation();
        initMainFlow();
        testFileModel = new DefaultFileData();
    }

    private void initMainFlow() {
        testBluetooth = (Button) findViewById(R.id.test_bluetooth_btn);
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
        bluetoothPresenter = new BluetoothPresenter(this, bluetoothFragment ,testFileModel, comparationModel);

    }

    private void setupComparation() {
        ComparationFragment comparationFragment =
                (ComparationFragment) getSupportFragmentManager().findFragmentById(ComparationFragment.ID);
        if (comparationFragment == null) {
            comparationFragment = ComparationFragment.newInstance();
            ActivityUtils.replaceFragmentByID(getSupportFragmentManager(), comparationFragment, R.id.contentFrame);
        }
        comparationModel = new ComparationRepository(getApplicationContext());
        comparationPresenter = new ComparationPresenter(comparationFragment, comparationModel);
    }


}
