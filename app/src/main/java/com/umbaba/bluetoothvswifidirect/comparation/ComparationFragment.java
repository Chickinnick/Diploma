package com.umbaba.bluetoothvswifidirect.comparation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.umbaba.bluetoothvswifidirect.R;
import com.umbaba.bluetoothvswifidirect.data.comparation.Criteria;

import java.util.List;

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
        recyclerView.setAdapter(new RVAdapter(presenter.getCriterion()));
        return view;
    }


    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ComparationViewHolder>{


        List<Criteria> criterias;

        public RVAdapter(List<Criteria> criterias) {
            this.criterias = criterias;
        }

        @Override
        public ComparationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comparation, parent, false);
            return  new ComparationViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ComparationViewHolder holder, int position) {
            Criteria criteria = criterias.get(position);
            holder.title.setText(criteria.getTitle());
            holder.left.setText(criteria.getLeft());
            holder.right.setText(criteria.getRight());

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public class ComparationViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView title;
            TextView left;
            TextView right;

            ComparationViewHolder(View itemView) {
                super(itemView);
                cv = (CardView) itemView.findViewById(R.id.card_view);
                title = (TextView) itemView.findViewById(R.id.title);
                left = (TextView) itemView.findViewById(R.id.left);
                right = (TextView) itemView.findViewById(R.id.right);
            }
        }

    }
}
