package org.ajcm.hiad.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.ajcm.hiad.R;
import org.ajcm.hiad.utils.UserPreferences;

public class SplashActivity extends AppCompatActivity {


    public static final String KEY_WELCOME = "welcome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        UserPreferences userPreferences = new UserPreferences(getApplicationContext());
        boolean welcome = userPreferences.getBoolean(KEY_WELCOME);
        if (welcome) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
        }
//        startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
        finish();
    }
}
