package com.umbaba.bluetoothvswifidirect.comparation;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.umbaba.bluetoothvswifidirect.R;

public class ComparationFragment extends Fragment implements ComparationContract.View{

    ComparationContract.Presenter presenter;

    public ComparationFragment() {
        // Required empty public constructor
    }

    @Override
    public void setPresenter(ComparationContract.Presenter presenter) {
        this.presenter = presenter;
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
        View view = inflater.inflate(R.layout.fragment_comparation, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.comparation_recycler);
        return view;
    }


    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ComparationViewHolder>{



        @Override
        public ComparationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ComparationViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public class ComparationViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView criterio;
            TextView left;
            TextView right;

            ComparationViewHolder(View itemView) {
                super(itemView);
               // cv = (CardView) itemView.findViewById(R.id.cv);
            }
        }

    }
}
