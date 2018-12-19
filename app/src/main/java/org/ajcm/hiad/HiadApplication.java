package org.ajcm.hiad;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.dataset.DatabaseHelper;
import org.ajcm.hiad.utils.UserPreferences;

import java.util.ArrayList;

/**
 * Created by jhonlimaster on 25-12-15.
 */
public class HiadApplication extends Application {

    private static final String TAG = "HiadApplication";
    private boolean isNightModeEnabled = false;
    public static final String NIGHT_MODE = "night_mode";

    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        UserPreferences preferences = new UserPreferences(this);
        this.isNightModeEnabled = preferences.getBoolean(NIGHT_MODE);
        Log.e(TAG, "onCreate: " + AppCompatDelegate.getDefaultNightMode());
        Log.e(TAG, "onCreate: " + this.isNightModeEnabled);
        if (this.isNightModeEnabled){
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                migrateDB();
                initAds();
            }
        }).run();
    }

    private void migrateDB() {
        DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
        ArrayList<Integer> allHimnoFav = dbAdapter.getAllHimnoFavCursor(true);
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        boolean checkUpdate = databaseHelper.checkUpdate();
        if (checkUpdate) {
            dbAdapter = new DBAdapter(getApplicationContext());
            for (Integer numero : allHimnoFav) {
                dbAdapter.setFav(numero, true, true);
            }
        }
    }

    private void initAds() {
        MobileAds.initialize(getApplicationContext(), getString(R.string.ads_id));
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "onSuccess: sesion anonima");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: falla en la sesion anonima");
            }
        });
    }
}
