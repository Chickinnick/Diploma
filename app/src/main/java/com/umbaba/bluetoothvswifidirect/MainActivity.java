package com.umbaba.bluetoothvswifidirect;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.umbaba.bluetoothvswifidirect.comparation.ComparationFragment;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationPresenter;
import com.umbaba.bluetoothvswifidirect.util.ActivityUtils;

public class MainActivity extends FragmentActivity {

    private ComparationPresenter comparationPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ComparationFragment comparationFragment =
                (ComparationFragment ) getSupportFragmentManager().findFragmentById(R.id.contentFrame);


        if (comparationFragment == null) {
            comparationFragment = ComparationFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    comparationFragment, R.id.contentFrame);
        }

        comparationPresenter = new ComparationPresenter(comparationFragment);
        comparationFragment.setPresenter(comparationPresenter);
    }





}
