package org.ajcm.hiad.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.ajcm.hiad.views.WelcomeFragment;

import java.util.ArrayList;

/**
 * Created by jhonlimaster on 08-12-16.
 */

public class WelcomeAdapter extends FragmentPagerAdapter {

    public WelcomeAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return WelcomeFragment.newInstance(position);
            case 1:
                return WelcomeFragment.newInstance(position);
            case 2:
                return WelcomeFragment.newInstance(position);
            case 3:
                return WelcomeFragment.newInstance(position);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
