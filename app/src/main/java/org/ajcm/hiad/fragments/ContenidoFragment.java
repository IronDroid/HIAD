package org.ajcm.hiad.fragments;


import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ajcm.hiad.R;
import org.ajcm.hiad.adapters.SubCategoriaRecyclerviewAdapter;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.models.Himno2008;
import org.zakariya.stickyheaders.StickyHeaderLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContenidoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContenidoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = "ContenidoFragment";
    private static final String ARG_CATEGORY = "param1";

    // TODO: Rename and change types of parameters
    private int mParam1;


    public ContenidoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ContenidoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContenidoFragment newInstance(int categoria) {
        ContenidoFragment fragment = new ContenidoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY, categoria);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_contenido, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;


            StickyHeaderLayoutManager stickyHeaderLayoutManager = new StickyHeaderLayoutManager();
            recyclerView.setLayoutManager(stickyHeaderLayoutManager);

            // set a header position callback to set elevation on sticky headers, because why not
            stickyHeaderLayoutManager.setHeaderPositionChangedCallback(new StickyHeaderLayoutManager.HeaderPositionChangedCallback() {
                @Override
                public void onHeaderPositionChanged(int sectionIndex, View header, StickyHeaderLayoutManager.HeaderPosition oldPosition, StickyHeaderLayoutManager.HeaderPosition newPosition) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        boolean elevated = newPosition == StickyHeaderLayoutManager.HeaderPosition.STICKY;
                        header.setElevation(elevated ? 8 : 0);
                    }
                }
            });

            recyclerView.setLayoutManager(stickyHeaderLayoutManager);
            DBAdapter dbAdapter = new DBAdapter(getContext());
            Cursor himnoByCategoria = dbAdapter.getHimnoByCategoria(mParam1);
            Log.e(TAG, "onCreateView: himnos categoria " + mParam1 + " - " + himnoByCategoria.getCount());
            ArrayList<Section> sections = new ArrayList<>();
            Section section = null;
            while (himnoByCategoria.moveToNext()) {
                Himno2008 himno2008 = Himno2008.fromCursor(himnoByCategoria);
                if (section != null) {
                    if (section.idCat != himno2008.getSubCategoria()) {
                        sections.add(section);
                        section = null;
                    }
                }

                if (section == null) {
                    section = new Section();
                    section.idCat = himno2008.getSubCategoria();
                    section.headers = dbAdapter.getCategoria(himno2008.getSubCategoria());
                }
                section.himno2008s.add(himno2008);
            }
            sections.add(section);

            himnoByCategoria.close();
            dbAdapter.close();
            recyclerView.setAdapter(new SubCategoriaRecyclerviewAdapter(getContext(), sections));
        }

        return view;
    }

    public class Section {
        private int idCat;
        private String headers;
        private ArrayList<Himno2008> himno2008s;

        Section() {
            himno2008s = new ArrayList<>();
        }

        public int getIdCat() {
            return idCat;
        }

        public void setIdCat(int idCat) {
            this.idCat = idCat;
        }

        public String getHeaders() {
            return headers;
        }

        public void setHeaders(String headers) {
            this.headers = headers;
        }

        public ArrayList<Himno2008> getHimno2008s() {
            return himno2008s;
        }

        public void setHimno2008s(ArrayList<Himno2008> himno2008s) {
            this.himno2008s = himno2008s;
        }
    }

}
