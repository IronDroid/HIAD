package org.ajcm.hiad;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.ajcm.hiad.activities.MainActivity;
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                databaseHelper.checkUpdate();
                MobileAds.initialize(getApplicationContext(), "ca-app-pub-5411285117883478~9340686141");
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

        fridayNotification();
    }

    public final static int ID_NOTIFICATION = 77;

    public void fridayNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent test = PendingIntent.getActivity(getApplicationContext(), ID_NOTIFICATION, notificationIntent, PendingIntent.FLAG_NO_CREATE);
        if (test != null) {
            Log.e(TAG, "fridayNotification: notificacion activa");
        }
    }
}
