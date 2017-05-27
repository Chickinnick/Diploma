package com.umbaba.bluetoothvswifidirect.comparation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.umbaba.bluetoothvswifidirect.data.comparation.Criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GraphFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 3;
    private HashMap<Integer, List<Criteria>> data;

    public GraphFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return GraphPageFragment.newInstance(data.get(position));
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }


    public void addData(List<Criteria> criterias) {
        List<List<Criteria>> lists = split(criterias, 3);
        for (int i = 0; i < lists.size(); i++) {
            data.put(i, lists.get(i));
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "distance " + data.get(position).get(0).getDistance();
    }

    public <E extends Object> List<List<E>> split(Collection<E> input, int size) {
        List<List<E>> master = new ArrayList<List<E>>();
        if (input != null && input.size() > 0) {
            List<E> col = new ArrayList<E>(input);
            boolean done = false;
            int startIndex = 0;
            int endIndex = col.size() > size ? size : col.size();
            while (!done) {
                master.add(col.subList(startIndex, endIndex));
                if (endIndex == col.size()) {
                    done = true;
                }
                else {
                    startIndex = endIndex;
                    endIndex = col.size() > (endIndex + size) ? (endIndex + size) : col.size();
                }
            }
        }
        return master;
    }



}