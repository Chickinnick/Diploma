package com.umbaba.bluetoothvswifidirect.comparation;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umbaba.bluetoothvswifidirect.R;

public class ComparationFragment extends Fragment {


    public ComparationFragment() {
        // Required empty public constructor
    }

    public static ComparationFragment newInstance() {
        ComparationFragment fragment = new ComparationFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comparation, container, false);
    }

}
