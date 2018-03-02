package org.ajcm.hiad.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.viewpagerindicator.CirclePageIndicator;

import org.ajcm.hiad.AlarmReceiver;
import org.ajcm.hiad.R;
import org.ajcm.hiad.adapters.WelcomeAdapter;
import org.ajcm.hiad.utils.UserPreferences;
import org.ajcm.hiad.views.WelcomePageTransformer;

import java.util.Calendar;

import static org.ajcm.hiad.HiadApplication.ID_NOTIFICATION;
import static org.ajcm.hiad.activities.SplashActivity.KEY_WELCOME;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";
    private ViewPager viewPager;
    private CirclePageIndicator circlePageIndicator;
    private Button buttonWelcome;
    private ImageButton buttonNext;
    private int limitPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new WelcomeAdapter(getSupportFragmentManager()));
        circlePageIndicator = (CirclePageIndicator) findViewById(R.id.circle_pager_indicator);
        circlePageIndicator.setViewPager(viewPager);
        viewPager.setPageTransformer(false, new WelcomePageTransformer());
        buttonWelcome = (Button) findViewById(R.id.button_welcome);
        buttonNext = (ImageButton) findViewById(R.id.button_next);
        limitPage = 3;
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });
        buttonWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
                new UserPreferences(getApplicationContext()).putBoolean(KEY_WELCOME, true);
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position < limitPage) {
                    buttonNext.setVisibility(View.VISIBLE);
                    buttonWelcome.setVisibility(View.GONE);
                } else {
                    buttonNext.setVisibility(View.GONE);
                    buttonWelcome.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
}
