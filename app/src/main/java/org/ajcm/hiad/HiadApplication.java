package org.ajcm.hiad;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.dataset.DatabaseHelper;
import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.models.Himno2008;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jhonlimaster on 25-12-15.
 */
public class HiadApplication extends Application {

    private static final String TAG = "HiadApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
                ArrayList<Integer> allHimnoFav = dbAdapter.getAllHimnoFavCursor(true);
                Log.e(TAG, "run: himnos fav " + allHimnoFav.size());
                DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                boolean checkUpdate = databaseHelper.checkUpdate();
                Log.e(TAG, "run: checkUpdate: " + checkUpdate);
                if (checkUpdate) {
                    dbAdapter = new DBAdapter(getApplicationContext());
                    for (Integer numero: allHimnoFav) {
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
