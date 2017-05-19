package com.umbaba.bluetoothvswifidirect.comparation;

import android.content.Context;

import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationModel;
import com.umbaba.bluetoothvswifidirect.data.comparation.ComparationRepository;
import com.umbaba.bluetoothvswifidirect.data.comparation.Criteria;

import java.util.List;

/**
 * Created by Nick on 17.04.2017.
 */

public class ComparationPresenter implements ComparationContract.Presenter {

    private Context context;
    private final ComparationContract.View view;
    private final ComparationModel comparationModel;



    public ComparationPresenter(Context context , ComparationContract.View view) {
        this.context = context;
        this.view = view;
        this.comparationModel = new ComparationRepository(context);
        this.view.setPresenter(this);
    }

    public void loadCriterion() {
        List<Criteria> criterion = comparationModel.getCriterion();

        view.showCriterion(criterion);
    }

    @Override
    public void startTransfer(int size) {

    }

    @Override
    public void stopTransfer(long fileLength) {

    }


}
