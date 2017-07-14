package org.ajcm.hiad.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ajcm.hiad.R;
import org.ajcm.hiad.adapters.MusicViewAdapter;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.models.Himno2008;
import org.ajcm.hiad.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by jhonlimaster on 26-06-17.
 */

public class MusicFragment extends Fragment {
    private static final String TAG = "StatisticFragment";
    private static final String ARG_PARAM1 = "param1";

    private int param;

    public MusicFragment() {
    }

    public static MusicFragment newInstance(int param1) {
        MusicFragment fragment = new MusicFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            param = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_music, container, false);
        if (view instanceof RecyclerView) {
            ArrayList<Himno2008> himnosDescargados = new ArrayList<>();
            ArrayList<Himno2008> himnosPendientes = new ArrayList<>();
            DBAdapter dbAdapter = new DBAdapter(getContext());
            ArrayList<Himno2008> himnos = (ArrayList<Himno2008>) dbAdapter.getAllHimno(true);
            Log.e(TAG, "onCreateView: " + himnos.size());
            for (Himno himno : himnos) {
                String number = FileUtils.getStringNumber(himno.getNumero());
                File dirHimnos = new File(getContext().getFilesDir().getAbsolutePath() + "/himnos/");
                dirHimnos.mkdirs();
                File file = new File(dirHimnos.getAbsolutePath() + "/" + number + ".ogg");
                if (file.exists()) {
                    himnosDescargados.add((Himno2008) himno);
                } else {
                    himnosPendientes.add((Himno2008) himno);
                }
            }
            dbAdapter.close();

            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            MusicViewAdapter recyclerViewAdapter =
                    new MusicViewAdapter(getActivity(), param, himnosDescargados, himnosPendientes);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
        return view;
    }
}
