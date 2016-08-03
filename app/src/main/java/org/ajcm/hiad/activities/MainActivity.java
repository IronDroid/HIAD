package org.ajcm.hiad.activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.ajcm.hiad.MediaListenService;
import org.ajcm.hiad.R;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.utils.FileUtils;
import org.ajcm.hiad.views.ZoomTextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MediaListenService.MediaServiceCallbacks {

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
    private boolean firstPlay;

    private boolean toastClose;
    private Toast toast;

    private AdView adView;
    private FirebaseAnalytics analytics;

    private Intent intentService;
    private MediaListenService listenService;

    private LinearLayout layoutDownload;
    private LinearLayout layoutPlay;
    private SeekBar seekBar;
    private ImageButton buttonPlay;
    private ImageButton buttonDonwload;
    private ImageButton buttonCancel;
    private TextView musicSize;
    private TextView musicTime;
    private TextView musicDuration;
    private TextView musicProcent;
    private ProgressBar musicProgress;

    private StorageReference reference;
    private FileDownloadTask fileDownloadTask;

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

        layoutDownload = (LinearLayout) findViewById(R.id.layout_download);
        layoutPlay = (LinearLayout) findViewById(R.id.layout_play);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        buttonDonwload = (ImageButton) findViewById(R.id.music_download);
        buttonDonwload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicProcent.setVisibility(View.VISIBLE);
                buttonCancel.setVisibility(View.VISIBLE);
                buttonDonwload.setVisibility(View.GONE);
                donwloadMusic(numero);
            }
        });
        buttonCancel = (ImageButton) findViewById(R.id.music_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicProcent.setVisibility(View.GONE);
                buttonCancel.setVisibility(View.GONE);
                buttonDonwload.setVisibility(View.VISIBLE);
                fileDownloadTask.cancel();

                String number = FileUtils.getStringNumber(numero);
                File dirHimnos = new File(getFilesDir().getAbsolutePath() + "/himnos/");
                File file = new File(dirHimnos.getAbsolutePath() + "/" + number + ".ogg");
                Log.e(TAG, "on Click Cancel: " + file.delete());
            }
        });
        buttonPlay = (ImageButton) findViewById(R.id.button_play);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstPlay) {
                    if (listenService.isPlaying()) {
                        buttonPlay.setImageResource(R.drawable.ic_play_circle_filled_black_36dp);
                        listenService.pauseMedia();
                    } else {
                        listenService.playMedia();
                        buttonPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_36dp);
                    }
                } else {
                    firstPlay = true;
                    listenService.playMedia(numero);
                    buttonPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_36dp);
                }
            }
        });
        musicSize = (TextView) findViewById(R.id.music_size);
        musicTime = (TextView) findViewById(R.id.music_time);
        musicDuration = (TextView) findViewById(R.id.music_duration);
        musicProcent = (TextView) findViewById(R.id.music_procent);
        musicProgress = (ProgressBar) findViewById(R.id.music_progress);

        restoreDataSaved(savedInstanceState);

        intentService = new Intent(this, MediaListenService.class);
        if (!isServiceRunning()) {
            startService(intentService); //Starting the service
            bindService(intentService, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!
        }

        String urlFirebase = "gs://tu-himnario-adventista.appspot.com";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        reference = storage.getReferenceFromUrl(urlFirebase);
    }

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
            seekBar.setMax(savedInstanceState.getInt("seek_max"));
            musicDuration.setText(savedInstanceState.getString("duration"));
            firstPlay = savedInstanceState.getBoolean("first_play");
            setUpPanelControls();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_TEXT_NUMBER, numberHimno.getText().toString());
        outState.putString(TOOLBAR_PANEL_TITLE, toolbarTitle.getText().toString());
        outState.putString(NUM_STRING, numString);
        outState.putString(TEXT_HIMNO, textHimno.getText().toString());
        outState.putInt(NUMERO, numero);
        outState.putFloat(TEXT_SIZE, textSize);
        outState.putInt("seek_max", seekBar.getMax());
        outState.putString("duration", musicDuration.getText().toString());
        outState.putBoolean("first_play", firstPlay);
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

            numberHimno.setText("");
            setPlaceholderHimno();
            setUpPanelControls();
            seekBar.setProgress(0);
            musicTime.setText(getDate(0));
            musicSize.setText(FileUtils.humanReadableByteCount(himnos.get(numero - 1).getSize()));
            String himnoPath = FileUtils.getDirHimnos(getApplicationContext()).getAbsoluteFile() + "/" + FileUtils.getStringNumber(numero) + ".ogg";
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(himnoPath);
                mediaPlayer.prepare();
                musicDuration.setText(getDate(mediaPlayer.getDuration()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    private void setupUpPanel() {
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarPanel = (Toolbar) findViewById(R.id.toolbar_panel);

        upPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        upPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelCollapsed(View panel) {
                cleanNum();
                listenService.stopMedia();
                firstPlay = false;
            }

            @Override
            public void onPanelExpanded(View panel) {
            }

            @Override
            public void onPanelAnchored(View panel) {
            }

            @Override
            public void onPanelHidden(View panel) {
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

    private void setUpPanelControls() {
        if (versionHimno) {
            layoutDownload.setVisibility(View.GONE);
            layoutPlay.setVisibility(View.GONE);
            return;
        }
        if (FileUtils.isHimnoDownloaded(getApplicationContext(), numero)) {
            layoutDownload.setVisibility(View.GONE);
            layoutPlay.setVisibility(View.VISIBLE);
        } else {
            layoutDownload.setVisibility(View.VISIBLE);
            layoutPlay.setVisibility(View.GONE);
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

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.e(TAG, "onService Connected");
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            MediaListenService.LocalBinder binder = (MediaListenService.LocalBinder) service;
            listenService = binder.getServiceInstance(); //Get instance of your service!
            listenService.registerClient(MainActivity.this); //Activity register in the service as client for callabcks!
            if (listenService.isPlaying()) {
                buttonPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_36dp);
            }
//            tvServiceState.setText("Connected to service...");
//            tbStartTask.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "onService Disconnected");
//            tvServiceState.setText("Service disconnected");
//            tbStartTask.setEnabled(false);
        }
    };

    @Override
    public void updateProgress(int mCurrentPosition) {
        seekBar.setProgress(mCurrentPosition / 100);
        musicTime.setText(getDate(mCurrentPosition));
    }

    @Override
    public void durationMedia(int duration) {
        seekBar.setMax(duration / 100);
        musicDuration.setText(getDate(duration));
    }

    @Override
    public void completion() {
        firstPlay = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
//        stopService(intentService);
        unbindService(mConnection);
        Log.e(TAG, "onStop: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
//        startService(intentService); //Starting the service
        bindService(intentService, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!
        Log.e(TAG, "onStart: ");
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.ajcm.hiad.MediaListenService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private void donwloadMusic(int numberInt) {
        String number = FileUtils.getStringNumber(numberInt);
        File dirHimnos = new File(getFilesDir().getAbsolutePath() + "/himnos/");
        dirHimnos.mkdirs();
        StorageReference himnoRef = reference.child("himnos/" + number + ".ogg");

        File file = new File(dirHimnos.getAbsolutePath() + "/" + number + ".ogg");
        if (!file.exists()) {
            musicProgress.setIndeterminate(true);
            musicProgress.setVisibility(View.VISIBLE);
            musicProcent.setVisibility(View.VISIBLE);

            fileDownloadTask = himnoRef.getFile(file);

            fileDownloadTask.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot.getBytesTransferred() > 1000) {
                        musicProgress.setProgress((int) (taskSnapshot.getBytesTransferred() * 100 / taskSnapshot.getTotalByteCount()));
                        musicProgress.setIndeterminate(false);
                        musicSize.setText(FileUtils.humanReadableByteCount(taskSnapshot.getBytesTransferred())
                                + " de " + FileUtils.humanReadableByteCount(taskSnapshot.getTotalByteCount()));
                    }
                    musicProcent.setText(musicProgress.getProgress() + "%");
                }
            }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    musicProgress.setVisibility(View.GONE);
                    musicProcent.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    musicProgress.setProgress(0);
                    musicSize.setText(getDate(himnos.get(numero - 1).getSize()));
                }
            });

        }
    }
}
