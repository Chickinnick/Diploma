package com.umbaba.bluetoothvswifidirect.data.comparation;


import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class FakeComparationRepository implements ComparationModel {


    @Override
    public void init(Context context) {

    }

    @Override
    public List<Criteria> getCriterion() {
        List<Criteria> criterias = new ArrayList<>();

        return criterias;
    }

    @Override
    public void addCriterion(Criteria criteria) {

    }
}
