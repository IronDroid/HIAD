package org.ajcm.hiad.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.TimedMetaData;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.ajcm.hiad.R;
import org.ajcm.hiad.utils.FileUtils;
import org.ajcm.hiad.views.ZoomTextView;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.models.Himno;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String CURRENT_TEXT_NUMBER = "current_text_number";
    private static final String APP_PNAME = "org.ajcm.hiad";
    private static final String TOOLBAR_PANEL_TITLE = "toolbar_panel_title";
    private static final String NUM_STRING = "num_string";
    private static final String NUMERO = "numero";
    private static final int SEARCH_HIMNO = 7;
    private static final String TEXT_HIMNO = "text_himno";
    private static final String TEXT_SIZE = "text_size";

    private ZoomTextView textHimno;
    private TextView numberHimno;
    private TextView toolbarTitle;
    private TextView placeholderHimno;
    private SlidingUpPanelLayout upPanelLayout;
    private Toolbar toolbarPanel;
    private DBAdapter dbAdapter;
    private ArrayList<Himno> himnos;

    private boolean versionHimno;
    private float textSize;
    private String numString;
    private int numero;
    private int limit;
    private static final int OLD_LIMIT = 527;
    private static final int NEW_LIMIT = 613;

    private boolean toastClose;
    private Toast toast;

    private AdView adView;
    private FirebaseAnalytics analytics;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adsMethod();
        analitycsMethod();

        limit = NEW_LIMIT;
        numString = "";
        textSize = 20;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        dbAdapter = new DBAdapter(getApplicationContext());
        getData();

        numberHimno = (TextView) findViewById(R.id.number_himno);
        textHimno = (ZoomTextView) findViewById(R.id.text_himno);
        placeholderHimno = (TextView) findViewById(R.id.placeholder_himno);

        setupUpPanel();

        ImageView backSpaceButton = (ImageView) findViewById(R.id.back_space);
        backSpaceButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                cleanNum();
                return true;
            }
        });

        String[] textos = getResources().getStringArray(R.array.textos_alabanza);
        int random = new Random().nextInt(textos.length);
        ((TextView) findViewById(R.id.texto_alabanza)).setText(textos[random]);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        }

        restoreDataSaved(savedInstanceState);

        mediaPlayer = new MediaPlayer();
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Log.e(TAG, "onProgressChanged: " + progress + fromUser);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.e(TAG, "onStartTrackingTouch: " + seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.e(TAG, "onStopTrackingTouch: " + seekBar.getProgress());
            }
        });
    }

    private SeekBar seekBar;

    private void getData() {
        dbAdapter.open();
        himnos = new ArrayList<>();
        Cursor allHimno = dbAdapter.getAllHimno(versionHimno);
        while (allHimno.moveToNext()) {
            himnos.add(Himno.fromCursor(allHimno));
        }
        dbAdapter.close();
    }

    private void adsMethod() {
        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void analitycsMethod() {
        analytics = FirebaseAnalytics.getInstance(this);

        Log.i(TAG, "Setting screen name: Main");
        analytics.setUserProperty("Activity_Main", "inicio");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_TEXT_NUMBER, numberHimno.getText().toString());
        outState.putString(TOOLBAR_PANEL_TITLE, numberHimno.getText().toString());
        outState.putString(NUM_STRING, numString);
        outState.putString(TEXT_HIMNO, textHimno.getText().toString());
        outState.putInt(NUMERO, numero);
        outState.putFloat(TEXT_SIZE, textSize);
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
                startActivityForResult(new Intent(this, SearchActivity.class).putExtra("version", versionHimno), SEARCH_HIMNO);
                return true;

            case R.id.action_version_himno:
                if (item.isChecked()) {
                    // himnario Nuevo
                    versionHimno = false;
                    limit = NEW_LIMIT;
                    item.setChecked(false);
                } else {
                    Bundle params = new Bundle();
                    params.putString("Category", "Action");
                    params.putString("Action", "Version_Antiguo");
                    analytics.logEvent("Change_version", params);
                    // himanrio antiguo
                    versionHimno = true;
                    limit = OLD_LIMIT;
                    item.setChecked(true);
                }
                getData();
                numberHimno.setText("");
                numString = "";
                numero = 0;
                setPlaceholderHimno();
                return true;

            case R.id.action_rate:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                return true;

            case R.id.action_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tu Himnario Adventista");

                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + APP_PNAME);
                startActivity(Intent.createChooser(sharingIntent, "Compartir via..."));
                return true;

            case R.id.action_music:
                startActivity(new Intent(MainActivity.this, MusicActivity.class));
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

    @Override
    public void onBackPressed() {
        if (upPanelLayout != null &&
                (upPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        upPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            if (!toastClose) {
                toast = Toast.makeText(this, "Pulse de nuevo para salir.", Toast.LENGTH_LONG);
                toast.show();
                toastClose = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toastClose = false;
                    }
                }, 3500);
            } else {
                upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                toast.cancel();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            numero = data.getExtras().getInt("numero", 0);
            Log.e(TAG, "result ok: " + numero);
            if (numero > 0) {
                placeholderHimno.setText("");
                numString = "" + numero;
                upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                String titleShow = numString + ". " + himnos.get(numero - 1).getTitulo();
                toolbarTitle.setText(titleShow);
                numberHimno.setText(titleShow);
                textHimno.setText(himnos.get(numero - 1).getLetra());

                if (FileUtils.isHimnoDownloaded(getApplicationContext(), numero)) {
                    toolbarPanel.getMenu().getItem(0).setVisible(true);
                    toolbarPanel.getMenu().getItem(1).setVisible(false);
                } else {
                    toolbarPanel.getMenu().getItem(0).setVisible(false);
                    toolbarPanel.getMenu().getItem(1).setVisible(true);
                }
            }
        }
    }

    private void cleanNum() {
        numberHimno.setText("");
        setPlaceholderHimno();
        numero = 0;
        numString = "";
    }

    public void numOk(View view) {
        if (numero > 0) {
            upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            String titleShow = numString + ". " + himnos.get(numero - 1).getTitulo();
            toolbarTitle.setText(titleShow);
            textHimno.setText(himnos.get(numero - 1).getLetra());

            if (FileUtils.isHimnoDownloaded(getApplicationContext(), numero)) {
                toolbarPanel.getMenu().getItem(0).setVisible(true);
                toolbarPanel.getMenu().getItem(1).setVisible(false);
            } else {
                toolbarPanel.getMenu().getItem(0).setVisible(false);
                toolbarPanel.getMenu().getItem(1).setVisible(true);
            }

            Log.i(TAG, "Setting screen name: Himno " + himnos.get(numero - 1).getSize());
            Bundle params = new Bundle();
            params.putString("Category", "Action");
            params.putString("Action", "ShowHimno");
            analytics.logEvent("Show_Himno", params);

            numberHimno.setText("");
            setPlaceholderHimno();
        }
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

    public void masUno(int num) {
        placeholderHimno.setText("");

        numString = numString + num;
        numero = Integer.parseInt(numString);

        if (numero > 0 && numero <= limit) {
            // buscar titulo para mostrar
            String titleShow = numString + ". " + himnos.get(numero - 1).getTitulo();
            numberHimno.setText(titleShow);
        } else {
            numString = numString.substring(0, numString.length() - 1);
            if (numString.length() > 0) {
                numero = Integer.parseInt(numString);
            } else {
                numero = 0;
                setPlaceholderHimno();
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
        if (numero > 0 && numero <= limit) {
            // buscar titulo para mostrar
            String titleShow = numString + ". " + himnos.get(numero - 1).getTitulo();
            numberHimno.setText(titleShow);
        } else {
            // mostrar placeholder
            numberHimno.setText("");
            setPlaceholderHimno();
        }
    }

    private void restoreDataSaved(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            numberHimno.setText(savedInstanceState.getString(CURRENT_TEXT_NUMBER));
            if (numberHimno.getText().toString().length() > 0) {
                placeholderHimno.setText("");
            } else {
                setPlaceholderHimno();
            }
            toolbarTitle.setText(savedInstanceState.getString(TOOLBAR_PANEL_TITLE));
            numString = savedInstanceState.getString(NUM_STRING);
            textHimno.setText(savedInstanceState.getString(TEXT_HIMNO));
            numero = savedInstanceState.getInt(NUMERO);
            textSize = savedInstanceState.getFloat(TEXT_SIZE);
//            textHimno.setTextSize(textSize);
        }
    }

    private Handler mHandler = new Handler();

    private void setupUpPanel() {
//        textHimno.setTextSize(textSize);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarPanel = (Toolbar) findViewById(R.id.toolbar_panel);
        toolbarPanel.inflateMenu(R.menu.menu_himno);
        toolbarPanel.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_play:
                        Log.e(TAG, "onMenuItemClick: PLAY");
                        String himnoPath = FileUtils.getDirHimnos(getApplicationContext()).getAbsoluteFile() + "/" + FileUtils.getStringNumber(numero) + ".ogg";
                        Log.e(TAG, "onMenuItemClick: " + himnoPath);
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(himnoPath);
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mediaPlayer.start();
                        seekBar.setVisibility(View.VISIBLE);
                        seekBar.setMax(mediaPlayer.getDuration() / 100);
                        Log.e(TAG, "max: " + mediaPlayer.getDuration());
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (mediaPlayer.isPlaying()) {
                                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 100;
                                    seekBar.setProgress(mCurrentPosition);
                                    mHandler.postDelayed(this, 100);
                                }
                            }
                        };
                        runOnUiThread(runnable);
                        return true;
                    case R.id.action_download:
                        Log.e(TAG, "onMenuItemClick: DOWNLOAD");
                        return true;
                }
                return false;
            }
        });
        upPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        upPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelCollapsed(View panel) {
                cleanNum();
                Log.e(TAG, "onPanelCollapsed: ");
                mediaPlayer.stop();
                seekBar.setVisibility(View.GONE);
            }

            @Override
            public void onPanelExpanded(View panel) {
                Log.e(TAG, "onPanelExpanded: ");
            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.e(TAG, "onPanelAnchored: ");
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.e(TAG, "onPanelHidden: ");
            }
        });
    }

    private void setPlaceholderHimno() {
        if (versionHimno) {
            placeholderHimno.setText(R.string.placeholder_himno_old);
        } else {
            placeholderHimno.setText(R.string.placeholder_himno);
        }
    }
}
