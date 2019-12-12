package org.ajcm.hiad.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.ajcm.hiad.fragments.MusicFragment;

/**
 * Adaptador del pagerview
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "PagerAdapter";

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return MusicFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? "descargados" : "músicas";
    }
}
