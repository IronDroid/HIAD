package org.ajcm.hiad;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CURRENT_TEXT_NUMBER = "current_text_number";
    private static final String APP_PNAME = "org.ajcm.hiad";
    private static final String TOOLBAR_PANEL_TITLE = "toolbar_panel_title";

    private TextView textHimno;
    private TextView numberHimno;
    private TextView placeholderHimno;
    private SlidingUpPanelLayout upPanelLayout;
    private Toolbar toolbarPanel;
    private float textSize;

    private String numString;
    private int numero;
    private int himnoLimite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        himnoLimite = 517;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        numberHimno = (TextView) findViewById(R.id.number_himno);
        textHimno = (TextView) findViewById(R.id.text_himno);
        placeholderHimno = (TextView) findViewById(R.id.placeholder_himno);
        textSize = 20;
        textHimno.setTextSize(textSize);
        toolbarPanel = (Toolbar) findViewById(R.id.toolbar_panel);
        toolbarPanel.inflateMenu(R.menu.menu_himno);
        toolbarPanel.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_plus:
                        textSize += 1;
                        textHimno.setTextSize(textSize);
                        return true;
                    case R.id.action_minus:
                        textSize -= 1;
                        textHimno.setTextSize(textSize);
                        return true;
                }
                return false;
            }
        });
        upPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        ImageView backSpaceButton = (ImageView) findViewById(R.id.back_space);
        backSpaceButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                numberHimno.setText("");
                placeholderHimno.setText(R.string.placeholder_himno);
                numero = 0;
                return true;
            }
        });
        restoreDataSaved(savedInstanceState);
    }

    private void restoreDataSaved(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            numberHimno.setText(savedInstanceState.getString(CURRENT_TEXT_NUMBER));
            toolbarPanel.setTitle(savedInstanceState.getString(TOOLBAR_PANEL_TITLE));
            placeholderHimno.setText("");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_TEXT_NUMBER, numberHimno.getText().toString());
        outState.putString(TOOLBAR_PANEL_TITLE, numberHimno.getText().toString());
        Log.i(TAG, "save instance: " + outState.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.action_version_himno:
                if (item.isChecked()) {
                    // himnario Nuevo
                    item.setChecked(false);
                } else {
                    // himanrio antiguo
                    item.setChecked(true);
                }
                // TODO: 04-12-15 cambiar textos
                return true;
            case R.id.action_rate:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                return true;
            case R.id.action_about:
                new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.app_name) + " v1.0")
                        .setMessage("Desarrollado por:" +
                                "\nAlex Jhonny Cruz Mamani" +
                                "\nDesarrollador Android Entusiasta" +
                                "\nEmail: jhonlimaster@gmail.com" +
                                "\nTwitter: @jhonlimaster")
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void numOk(View view) {
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
//        toolbarPanel = (Toolbar) findViewById(R.id.toolbar_panel);
        toolbarPanel.setTitle(numberHimno.getText());
    }

    public void inputDelete(View view) {
        menosUno();
    }

    public void number0(View view) {
        masUno(0);
    }

    public void number9(View view) {
        masUno(9);
    }

    public void number8(View view) {
        masUno(8);
    }

    public void number7(View view) {
        masUno(7);
    }

    public void number6(View view) {
        masUno(6);
    }

    public void number5(View view) {
        masUno(5);
    }

    public void number4(View view) {
        masUno(4);
    }

    public void number3(View view) {
        masUno(3);
    }

    public void number2(View view) {
        masUno(2);
    }

    public void number1(View view) {
        masUno(1);
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

    public void masUno(int num) {
        placeholderHimno.setText("");

        numString = numberHimno.getText().toString() + num;
        numero = Integer.parseInt(numString);

        if (numero > 0 && numero <= himnoLimite) {
            // buscar titulo para mostrar
            numberHimno.setText(numString);
        } else {
            numString = numString.substring(0, numString.length() - 1);
            if (numString.length() > 0) {
                numero = Integer.parseInt(numString);
            } else {
                numero = 0;
                placeholderHimno.setText(R.string.placeholder_himno);
            }
        }
    }

    public void menosUno() {
        if (numString.length() <= 1) {
            numString = "";
            numero = 0;
        } else {
            numString = numString.substring(0, numString.length() - 1);
            numero = Integer.parseInt(numString);
        }
        if (numero > 0 && numero <= himnoLimite) {
            // buscar titulo para mostrar
            numberHimno.setText(numString);
        } else {
            // mostrar placeholder
            numberHimno.setText("");
            placeholderHimno.setText(R.string.placeholder_himno);
        }
    }
}
