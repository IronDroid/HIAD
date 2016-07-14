package org.ajcm.hiad;

import android.app.Application;
import android.os.SystemClock;

import com.google.android.gms.ads.MobileAds;

import java.util.concurrent.TimeUnit;

/**
 * Created by jhonlimaster on 25-12-15.
 */
public class HiadApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(1));
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5411285117883478~9340686141");
    }
}
