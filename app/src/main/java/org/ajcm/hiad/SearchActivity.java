package org.ajcm.hiad;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.ajcm.hiad.adapters.HimnoAdapter;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.models.Himno;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = "SearchActivity";
    private ListView listView;
    private ArrayList<Himno> himnos;
    private DBAdapter dbAdapter;
    private HimnoAdapter himnoAdapter;
    private String filter;
    private boolean versionHimno;

    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        analitycsMethod();
        listView = (ListView) findViewById(R.id.listView);

        himnos = new ArrayList<>();
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        versionHimno = getIntent().getBooleanExtra("version", false);
        Cursor himnoASC = dbAdapter.getAllHimnoASC(versionHimno);
        while (himnoASC.moveToNext()){
            himnos.add(Himno.fromCursor(himnoASC));
        }
        dbAdapter.close();

        himnoAdapter = new HimnoAdapter(this, himnos);
        listView.setAdapter(himnoAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "onItemClick: " + i);
                setResult(RESULT_OK, new Intent().putExtra("numero", himnos.get(i).getNumero()));
                finish();
            }
        });
    }

    private void analitycsMethod(){
        HiadApplication application = (HiadApplication) getApplication();
        tracker = application.getDefaultracker();

        Log.i(TAG, "Setting screen name: Main");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        tracker.setScreenName("Search-Himno");
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("SearchHimno")
                .build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        searchView.setQueryHint("Titulo del himno");
        searchView.requestFocus();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filter = !TextUtils.isEmpty(newText) ? newText : null;
        dbAdapter.open();
        himnos = new ArrayList<>();
        Cursor himnoForTitle;
        if (filter != null){
            himnoForTitle = dbAdapter.getHimnoForTitle(filter, versionHimno);
        } else {
            himnoForTitle = dbAdapter.getAllHimnoASC(versionHimno);
        }
        while (himnoForTitle.moveToNext()){
            himnos.add(Himno.fromCursor(himnoForTitle));
        }
        dbAdapter.close();
        himnoAdapter.setData(himnos);
        himnoAdapter.notifyDataSetChanged();

        return false;
    }
}
