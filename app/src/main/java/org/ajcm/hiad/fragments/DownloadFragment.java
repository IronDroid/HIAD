package org.ajcm.hiad.fragments;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ajcm.hiad.R;
import org.ajcm.hiad.adapters.PagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadFragment extends Fragment {

    private static final String TAG = "DownloadFragment";
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private PagerAdapter adapter;

    public DownloadFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        // Inflate the layout for this fragment
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.sliding_tabs);
        adapter = new PagerAdapter(getFragmentManager());

        // TODO: 03-10-17 en esta parte se tiene que hacer el refresh cuando se boora o se descarga un himnos
        MusicFragment aa = (MusicFragment) adapter.getItem(1);
        aa.refreshList();

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }
}
