package com.umbaba.bluetoothvswifidirect.comparation;

import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationModel;
import com.umbaba.bluetoothvswifidirect.data.comparation.Criteria;

import java.util.List;

/**
 * Created by Nick on 17.04.2017.
 */

public class ComparationPresenter implements ComparationContract.Presenter {

    private final ComparationContract.View view;
    private final ComparationModel comparationModel;



    public ComparationPresenter(ComparationContract.View view, ComparationModel comparationModel) {
        this.view = view;
        this.comparationModel = comparationModel;
        this.view.setPresenter(this);
    }

    @Override
    public void subscribe() {
        loadCriterion();
    }

    public void loadCriterion() {
        List<Criteria> criterion = comparationModel.getCriterion();

        view.showCriterion(criterion);
    }

    @Override
    public void unsubscribe() {

    }


}
