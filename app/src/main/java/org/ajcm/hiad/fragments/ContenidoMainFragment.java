package org.ajcm.hiad.fragments;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ajcm.hiad.R;
import org.ajcm.hiad.adapters.ContenidoPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContenidoMainFragment extends Fragment {

    private static final String TAG = "ContenidoMainFragment";
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ContenidoPagerAdapter adapter;

    public ContenidoMainFragment() {
    }

    public static ContenidoMainFragment newInstance() {
        Bundle args = new Bundle();
        ContenidoMainFragment fragment = new ContenidoMainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contenido_main, container, false);

        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.sliding_tabs);
        adapter = new ContenidoPagerAdapter(getContext(), getFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

}
