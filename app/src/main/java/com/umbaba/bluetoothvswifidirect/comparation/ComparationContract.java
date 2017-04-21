package com.umbaba.bluetoothvswifidirect.comparation;

import com.umbaba.bluetoothvswifidirect.BasePresenter;
import com.umbaba.bluetoothvswifidirect.BaseView;
import com.umbaba.bluetoothvswifidirect.data.comparation.Criteria;

import java.util.List;

/**
 * Created by Nick on 17.04.2017.
 */

public interface ComparationContract {

    interface View extends BaseView<Presenter> {


    }

    interface Presenter extends BasePresenter {

        List<Criteria> getCriterion();
    }
}