package org.ajcm.hiad.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.Pair;

import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.fragments.ContenidoFragment;

import java.util.ArrayList;

/**
 * Adaptador del pagerview
 */
public class ContenidoPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "PagerAdapter";
    private Context context;
    private DBAdapter dbAdapter;
    private ArrayList<Pair<Integer, String>> titles;

    public ContenidoPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        dbAdapter = new DBAdapter(this.context);
        Cursor categorias = dbAdapter.getCategorias();
        titles = new ArrayList<>();
        while (categorias.moveToNext()) {
            titles.add(Pair.create(categorias.getInt(0), categorias.getString(1)));
        }
        categorias.close();
        dbAdapter.close();
    }

    @Override
    public Fragment getItem(int position) {
        return ContenidoFragment.newInstance(titles.get(position).first);
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position).second;
    }
}
