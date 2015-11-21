package org.ajcm.hiad;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView textHimno;
    private TextView numberHimno;
    private SlidingUpPanelLayout upPanelLayout;
    private Toolbar toolbarPanel;
    private float textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        numberHimno = (TextView) findViewById(R.id.number_himno);
        textHimno = (TextView) findViewById(R.id.text_himno);
        textSize = textHimno.getTextSize();
        toolbarPanel = (Toolbar) findViewById(R.id.toolbar_panel);
        toolbarPanel.inflateMenu(R.menu.menu_himno);
        toolbarPanel.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_plus:
                        Toast.makeText(getApplicationContext(), "plus", Toast.LENGTH_SHORT).show();
                        textSize += 2;
                        textHimno.setTextSize(textSize);
                        return true;
                    case R.id.action_minus:
                        Toast.makeText(getApplicationContext(), "minus", Toast.LENGTH_SHORT).show();
                        textSize -= 2;
                        textHimno.setTextSize(textSize);
                        return true;
                }
                return false;
            }
        });
        upPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.i(TAG, "" + outState.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(this, "search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_about:
                Toast.makeText(this, "about", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void numOk(View view) {
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        toolbarPanel = (Toolbar) findViewById(R.id.toolbar_panel);
        toolbarPanel.setTitle(numberHimno.getText());
    }

    public void inputDelete(View view) {
        if (numberHimno.getText().length() <= 1) {
            numberHimno.setText("");
        } else {
            String num = numberHimno.getText().toString();
            numberHimno.setText(num.substring(0, numberHimno.getText().length() - 1));
        }
    }

    public void number0(View view) {
        numberHimno.setText(numberHimno.getText() + "0");
    }

    public void number9(View view) {
        numberHimno.setText(numberHimno.getText() + "9");
    }

    public void number8(View view) {
        numberHimno.setText(numberHimno.getText() + "8");
    }

    public void number7(View view) {
        numberHimno.setText(numberHimno.getText() + "7");
    }

    public void number6(View view) {
        numberHimno.setText(numberHimno.getText() + "6");
    }

    public void number5(View view) {
        numberHimno.setText(numberHimno.getText() + "5");
    }

    public void number4(View view) {
        numberHimno.setText(numberHimno.getText() + "4");
    }

    public void number3(View view) {
        numberHimno.setText(numberHimno.getText() + "3");
    }

    public void number2(View view) {
        numberHimno.setText(numberHimno.getText() + "2");
    }

    public void number1(View view) {
        numberHimno.setText(numberHimno.getText() + "1");
    }

    @Override
    public void onBackPressed() {
        if (upPanelLayout != null &&
                (upPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        upPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }
}
