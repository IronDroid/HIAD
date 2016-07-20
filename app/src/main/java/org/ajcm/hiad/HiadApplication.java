package org.ajcm.hiad;

import android.app.Application;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;

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
        Log.e(TAG, "onCreate ");
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        Log.e(TAG, "checkUpdate: ");
        databaseHelper.checkUpdate();
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(1));
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5411285117883478~9340686141");
    }
}
