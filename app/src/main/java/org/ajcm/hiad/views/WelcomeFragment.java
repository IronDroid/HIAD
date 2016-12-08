package org.ajcm.hiad.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ajcm.hiad.R;

/**
 * Created by jhonlimaster on 08-12-16.
 */

public class WelcomeFragment extends Fragment {
    private static final String KEY_PAGE = "page";
    private int page;

    public static WelcomeFragment newInstance(int page) {
        WelcomeFragment welcomeFragment = new WelcomeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_PAGE, page);
        welcomeFragment.setArguments(bundle);
        return welcomeFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt(KEY_PAGE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout resource file
        View view = getActivity().getLayoutInflater().inflate(R.layout.item_welcome, container, false);
        // Set the current page index as the View's tag (useful in the PageTransformer)
        view.setTag(page);
        return view;
    }

}
