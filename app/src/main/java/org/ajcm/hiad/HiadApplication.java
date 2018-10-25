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

import java.util.ArrayList;

/**
 * Created by jhonlimaster on 25-12-15.
 */
public class HiadApplication extends Application {

    private static final String TAG = "HiadApplication";

    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
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
        }).run();
    }
}
