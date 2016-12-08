package org.ajcm.hiad.activities;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.ajcm.hiad.R;
import org.ajcm.hiad.adapters.WelcomeAdapter;
import org.ajcm.hiad.utils.UserPreferences;
import org.ajcm.hiad.views.WelcomePageTransformer;

import static org.ajcm.hiad.activities.SplashActivity.KEY_WELCOME;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private Button buttonWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new WelcomeAdapter(getSupportFragmentManager()));
        viewPager.setPageTransformer(false, new WelcomePageTransformer());
        buttonWelcome = (Button) findViewById(R.id.button_welcome);
        buttonWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPager.getCurrentItem() == 1) {
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                    new UserPreferences(getApplicationContext()).putBoolean(KEY_WELCOME, true);
                } else {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                }
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position < 1) {
                    buttonWelcome.setText("siguiente");
                } else {
                    buttonWelcome.setText("finalizar");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
