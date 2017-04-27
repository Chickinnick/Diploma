package com.umbaba.bluetoothvswifidirect.data.comparation;


import android.content.Context;

import java.util.List;

public interface ComparationModel {


    public void init(Context context);

    List<Criteria> getCriterion();

    void addCriterion(Criteria criteria);
}
