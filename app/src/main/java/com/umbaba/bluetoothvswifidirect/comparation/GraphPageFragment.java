package com.umbaba.bluetoothvswifidirect.comparation;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.umbaba.bluetoothvswifidirect.R;
import com.umbaba.bluetoothvswifidirect.data.comparation.Criteria;

import java.util.ArrayList;
import java.util.List;

public class GraphPageFragment extends Fragment {
    private static final String CRITERIAS = "param1";

    List<Criteria> criteria;


    public GraphPageFragment() {
        // Required empty public constructor
    }

    public static GraphPageFragment newInstance(List<Criteria>  criteria) {
        GraphPageFragment fragment = new GraphPageFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(CRITERIAS, (ArrayList<? extends Parcelable>) criteria);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            criteria = getArguments().getParcelableArrayList(CRITERIAS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph_page, container, false);
        GraphView graph = (GraphView) view.findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3)
        });
        graph.addSeries(series);
        return view;
    }

}
