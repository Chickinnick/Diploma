package com.umbaba.bluetoothvswifidirect.data.comparation;


import android.content.Context;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;

import java.util.ArrayList;
import java.util.List;

public class ComparationRepository implements ComparationModel {

    public static final String CRITERION_KEY = "criteria";

    public List<Criteria> criteriaList;

    public ComparationRepository(Context applicationContext) {
        init(applicationContext);
    }

    @Override
    public void init(Context context) {
        Hawk.init(context)
                .setStorage(HawkBuilder.newSharedPrefStorage(context))
                .setLogLevel(LogLevel.FULL)
                .build();
    }

    @Override
    public List<Criteria> getCriterion() {
        return criteriaList == null ? getFromStorage() : criteriaList;
    }

    private List<Criteria> getFromStorage() {
        criteriaList = Hawk.get(CRITERION_KEY);
        return criteriaList;
    }

    @Override
    public void addCriterion(Criteria criteria) {
        putInStorage(criteria);
    }

    private void putInStorage(Criteria criteria) {
        if (criteriaList == null) {
            criteriaList = new ArrayList<>();
            criteriaList.add(criteria);
        } else {
            criteriaList = Hawk.get(CRITERION_KEY);
            criteriaList.add(criteria);
        }
        Hawk.put(CRITERION_KEY, criteriaList);
    }
}
