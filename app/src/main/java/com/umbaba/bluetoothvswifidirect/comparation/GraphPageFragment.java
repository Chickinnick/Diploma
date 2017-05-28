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
        if (criteria != null) {
            args.putParcelableArrayList(CRITERIAS, new ArrayList<Parcelable>(criteria));
        }
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
        initGraphData(criteria, graph);
        return view;
    }

    private void initGraphData(List<Criteria> criterias, GraphView graph) {
        if (criterias == null) {
            return;
        }
        DataPoint[] blDataPoints = new DataPoint[criterias.size()];
        DataPoint[] wifiDataPoints = new DataPoint[criterias.size()];

        for (int i = 0; i < criterias.size(); i++) {
            Criteria criteria = criterias.get(i);
            int fileLen = criteria.getFileLen();
            blDataPoints[i] = new DataPoint(fileLen, Double.parseDouble(criteria.getLeft()));
            wifiDataPoints[i] = new DataPoint(fileLen, Double.parseDouble(criteria.getRight()));
        }
        LineGraphSeries<DataPoint> bluetoothSeries = new LineGraphSeries<>(blDataPoints);
        LineGraphSeries<DataPoint> wifiSeries = new LineGraphSeries<>(wifiDataPoints);
        graph.addSeries(bluetoothSeries);
        graph.addSeries(wifiSeries);
    }

}
