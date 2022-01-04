package org.ajcm.hiad.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ajcm.hiad.R;
import org.ajcm.hiad.adapters.HistoryAdapter;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.models.Historial;

import java.util.Objects;
import java.util.Random;

public class HistoryFragment extends Fragment {

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        DBAdapter dbAdapter = new DBAdapter(getContext());

        HistoryAdapter historyAdapter = new HistoryAdapter(getActivity(), dbAdapter.getHistorial());

        RecyclerView recyclerView = view.findViewById(R.id.history_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(historyAdapter);

        return view;
    }
}
