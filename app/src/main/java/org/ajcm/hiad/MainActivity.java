package org.ajcm.hiad;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView numberHimno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        numberHimno = (TextView) findViewById(R.id.number_himno);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Toast.makeText(this, "something", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void numOk(View view) {
        Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
    }

    public void inputDelete(View view) {
        numberHimno.setText("");
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
}
