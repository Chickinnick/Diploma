package com.umbaba.bluetoothvswifidirect.data.comparation;


import java.util.ArrayList;
import java.util.List;

public class FakeComparationRepository implements ComparationModel {


    @Override
    public List<Criteria> getCriterion() {
        List<Criteria> criterias = new ArrayList<>();
        criterias.add(new Criteria("speed" , "123123" , "1231"));
        criterias.add(new Criteria("latency" , "asd" , "sad"));
        criterias.add(new Criteria("zxc" , "234" , "234"));
        criterias.add(new Criteria("jtht" , "sdfsfd" , "y65467"));

        return criterias;
    }
}