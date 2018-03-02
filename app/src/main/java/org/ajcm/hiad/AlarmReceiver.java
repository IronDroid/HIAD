package org.ajcm.hiad;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.ajcm.hiad.activities.MainActivity;
import org.ajcm.hiad.utils.UserPreferences;

import static org.ajcm.hiad.HiadApplication.ID_NOTIFICATION;

/**
 * Created by jhonlimaster on 31-01-18.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: showNotification");
        Log.e(TAG, "onReceive: " + intent.getExtras().keySet());
        if (intent.getExtras().getBoolean("end_alarm")) {
            UserPreferences userPreferences = new UserPreferences(context);
            userPreferences.putBoolean("alarm", false);
        } else {
            MainActivity.showNotification(context);
        }
    }
}
