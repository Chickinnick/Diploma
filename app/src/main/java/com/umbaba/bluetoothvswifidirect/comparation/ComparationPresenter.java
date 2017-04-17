package com.umbaba.bluetoothvswifidirect.comparation;

/**
 * Created by Nick on 17.04.2017.
 */

public class ComparationPresenter implements ComparationContract.Presenter {

    private final ComparationContract.View view;

    public ComparationPresenter(ComparationContract.View view) {
        this.view = view;
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {

    }
}
