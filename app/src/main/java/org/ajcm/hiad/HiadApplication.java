package org.ajcm.hiad;

import android.app.Application;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.ajcm.hiad.dataset.DatabaseHelper;

import java.util.concurrent.TimeUnit;

/**
 * Created by jhonlimaster on 25-12-15.
 */
public class HiadApplication extends Application {

    private static final String TAG = "HiadApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        databaseHelper.checkUpdate();
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5411285117883478~9340686141");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }
}
