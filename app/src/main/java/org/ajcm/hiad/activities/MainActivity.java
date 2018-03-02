package org.ajcm.hiad.activities;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
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

import org.ajcm.hiad.AlarmReceiver;
import org.ajcm.hiad.R;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.models.Himno1962;
import org.ajcm.hiad.models.Himno2008;
import org.ajcm.hiad.services.MediaListenService;
import org.ajcm.hiad.utils.ConnectionUtils;
import org.ajcm.hiad.utils.FileUtils;
import org.ajcm.hiad.utils.UserPreferences;
import org.ajcm.hiad.views.ZoomTextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import static org.ajcm.hiad.HiadApplication.ID_NOTIFICATION;

public class MainActivity extends AppCompatActivity implements MediaListenService.MediaServiceCallbacks {

    private static final String TAG = "MainActivity";
    private static final String CURRENT_TEXT_NUMBER = "current_text_number";
    private static final String APP_PNAME = "org.ajcm.hiad";
    private static final String TOOLBAR_PANEL_TITLE = "toolbar_panel_title";
    private static final String NUM_STRING = "num_string";
    private static final String NUMERO = "numero";
    private static final String VERSION_HIMNOS = "version";
    private static final String TEXT_HIMNO = "text_himno";
    private static final int REQUEST_SEARCH_HIMNO = 777;
    private static final int OLD_LIMIT = 527;
    private static final int NEW_LIMIT = 613;

    private ZoomTextView textHimno;
    private TextView numberHimno;
    private TextView toolbarTitle;
    private TextView placeholderHimno;
    private SlidingUpPanelLayout upPanelLayout;
    private DBAdapter dbAdapter;
    private ArrayList<? extends Himno> himnos;

    private boolean version2008 = true;
    private String numString;
    private int numero;
    private int limit;
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
    private MaterialFavoriteButton favoriteButton;

    private StorageReference reference;
    private FileDownloadTask fileDownloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        notAdsInSaturday();
        setNotification();
        analitycsMethod();

        limit = NEW_LIMIT;
        numString = "";

        numberHimno = (TextView) findViewById(R.id.number_himno);
        textHimno = (ZoomTextView) findViewById(R.id.text_himno);
        favoriteButton = (MaterialFavoriteButton) findViewById(R.id.fav_button);
        placeholderHimno = (TextView) findViewById(R.id.placeholder_himno);
        layoutDownload = (LinearLayout) findViewById(R.id.layout_download);
        layoutPlay = (LinearLayout) findViewById(R.id.layout_play);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        buttonDonwload = (ImageButton) findViewById(R.id.music_download);
        buttonCancel = (ImageButton) findViewById(R.id.music_cancel);
        buttonPlay = (ImageButton) findViewById(R.id.button_play);
        musicSize = (TextView) findViewById(R.id.music_size);
        musicTime = (TextView) findViewById(R.id.music_time);
        musicDuration = (TextView) findViewById(R.id.music_duration);
        musicProcent = (TextView) findViewById(R.id.music_procent);
        musicProgress = (ProgressBar) findViewById(R.id.music_progress);

        setupUpPanel();
        restoreDataSaved(savedInstanceState);
        setUpPanelControls();

        dbAdapter = new DBAdapter(getApplicationContext());
        getData();

        favoriteButton.setOnFavoriteAnimationEndListener(new MaterialFavoriteButton.OnFavoriteAnimationEndListener() {
            @Override
            public void onAnimationEnd(MaterialFavoriteButton buttonView, boolean favorite) {
                // TODO: 14-07-17 guardar el himno favorito
                if (upPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED && numero > 0) {
                    dbAdapter.setFav(numero, favorite, version2008);
                    if (version2008) {
                        ((Himno2008) himnos.get(numero - 1)).setFavorito(favorite);
                    } else {
                        ((Himno1962) himnos.get(numero - 1)).setFavorito(favorite);
                    }
                }
            }
        });

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

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean user) {
                if (user) {
                    Log.e(TAG, "onProgressChanged: " + position);
                    if (listenService.isPlaying()) {
                        buttonPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_36dp);
                    } else {
                        buttonPlay.setImageResource(R.drawable.ic_play_circle_filled_black_36dp);
                    }
                    listenService.setSeek(position * 100);
                    fileDownloadTask = null;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        buttonDonwload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectionUtils.hasInternet(getApplicationContext())) {
                    musicProcent.setVisibility(View.VISIBLE);
                    buttonCancel.setVisibility(View.VISIBLE);
                    buttonDonwload.setVisibility(View.GONE);
                    donwloadMusic(numero);
                } else {
                    Toast.makeText(MainActivity.this, "No hay internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicProcent.setVisibility(View.GONE);
                buttonCancel.setVisibility(View.GONE);
                buttonDonwload.setVisibility(View.VISIBLE);
                musicProgress.setIndeterminate(false);
                fileDownloadTask.cancel();

                String number = FileUtils.getStringNumber(numero);
                File dirHimnos = new File(getFilesDir().getAbsolutePath() + "/himnos/");
                File file = new File(dirHimnos.getAbsolutePath() + "/" + number + ".ogg");
                Log.e(TAG, "on Click Cancel: " + file.delete());
            }
        });
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
        himnos = dbAdapter.getAllHimno(version2008);
        dbAdapter.close();
        if (numero > 0) {
            if (version2008) {
                favoriteButton.setFavorite(((Himno2008) himnos.get(numero - 1)).isFavorito());
            } else {
                layoutDownload.setVisibility(View.GONE);
                favoriteButton.setFavorite(((Himno1962) himnos.get(numero - 1)).isFavorito());
            }
        }
        if (numero > 0 && version2008) {
            musicSize.setText(FileUtils.humanReadableByteCount(((Himno2008) himnos.get(numero - 1)).getFileSize()));
        }
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
            version2008 = savedInstanceState.getBoolean(VERSION_HIMNOS);
            toolbarTitle.setText(savedInstanceState.getString(TOOLBAR_PANEL_TITLE));
            numString = savedInstanceState.getString(NUM_STRING);
            textHimno.setText(savedInstanceState.getString(TEXT_HIMNO));
            numero = savedInstanceState.getInt(NUMERO);
            seekBar.setMax(savedInstanceState.getInt("seek_max"));
            musicDuration.setText(savedInstanceState.getString("duration"));
            firstPlay = savedInstanceState.getBoolean("first_play");
            if (numberHimno.getText().toString().length() > 0) {
                placeholderHimno.setText("");
            } else {
                setPlaceholderHimno();
            }
            if (savedInstanceState.getBoolean("is_playing")) {
                buttonPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_36dp);
            } else {
                buttonPlay.setImageResource(R.drawable.ic_play_circle_filled_black_36dp);
            }
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
        if (listenService != null) {
            outState.putBoolean("is_playing", listenService.isPlaying());
        }
        Log.e(TAG, "onSaveInstanceState: se guarda version " + version2008);
        outState.putBoolean(VERSION_HIMNOS, version2008);
        outState.putInt("seek_max", seekBar.getMax());
        outState.putString("duration", musicDuration.getText().toString());
        outState.putBoolean("first_play", firstPlay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_version_himno);
        if (!version2008) {
            item.setChecked(true);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (upPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivityForResult(new Intent(this, SearchActivity.class).putExtra("version", version2008), REQUEST_SEARCH_HIMNO);
                return true;

            case R.id.action_version_himno:
                if (item.isChecked()) {
                    item.setChecked(false);
                    version2008 = true;
                    limit = NEW_LIMIT;
                } else {
                    item.setChecked(true);
                    version2008 = false;
                    limit = OLD_LIMIT;

                    Bundle params = new Bundle();
                    params.putString("Category", "Action");
                    params.putString("Action", "Version_Antiguo");
                    analytics.logEvent("Change_version", params);
                }
                getData();

                // TODO: 14-07-17 agrupar estas funciones
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
                if (this.version2008) {
                    startActivityForResult(new Intent(MainActivity.this, MusicActivity.class), REQUEST_SEARCH_HIMNO);
                } else {
                    Toast.makeText(this, "No Disponible en la version antigua del himnario", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_contenido:
                if (this.version2008) {
                    startActivityForResult(new Intent(MainActivity.this, ContenidoActivity.class), REQUEST_SEARCH_HIMNO);
                } else {
                    Toast.makeText(this, "No Disponible en la version antigua del himnario", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_about:
                new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.app_name))
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
            stopService(intentService);
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            numero = data.getExtras().getInt("numero", 0);
            Log.e(TAG, "result ok: " + numero);
            if (numero > 0) {
                if (version2008) {
                    favoriteButton.setFavorite(((Himno2008) himnos.get(numero - 1)).isFavorito());
                    musicSize.setText(FileUtils.humanReadableByteCount(((Himno2008) himnos.get(numero - 1)).getFileSize()));
                } else {
                    favoriteButton.setFavorite(((Himno1962) himnos.get(numero - 1)).isFavorito());
                }
                placeholderHimno.setText("");
                numString = "" + numero;
                upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                String titleShow = numString + ". " + himnos.get(numero - 1).getTitulo();
                toolbarTitle.setText(titleShow);
                numberHimno.setText(titleShow);
                textHimno.setText(himnos.get(numero - 1).getLetra());
                setUpPanelControls();
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
            String titleShow = numString + ". " + himnos.get(numero - 1).getTitulo();
            toolbarTitle.setText(titleShow);
            textHimno.setText(himnos.get(numero - 1).getLetra());

            numberHimno.setText("");
            setPlaceholderHimno();
            setUpPanelControls();
            seekBar.setProgress(0);
            musicTime.setText(getDate(0));
            musicProgress.setIndeterminate(false);
            buttonDonwload.setVisibility(View.VISIBLE);
            buttonCancel.setVisibility(View.GONE);
            if (version2008) {
                favoriteButton.setFavorite(((Himno2008) himnos.get(numero - 1)).isFavorito());
                musicSize.setText(FileUtils.humanReadableByteCount(((Himno2008) himnos.get(numero - 1)).getFileSize()));
            } else {
                favoriteButton.setFavorite(((Himno1962) himnos.get(numero - 1)).isFavorito());
            }
            upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            String himnoPath = FileUtils.getDirHimnos(getApplicationContext()).getAbsoluteFile() + "/" + FileUtils.getStringNumber(numero) + ".ogg";
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(himnoPath);
                mediaPlayer.prepare();
                musicDuration.setText(getDate(mediaPlayer.getDuration()));
            } catch (IOException ignored) {
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
                favoriteButton.setFavorite(false);
                buttonPlay.setImageResource(R.drawable.ic_play_circle_filled_black_36dp);
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
        if (version2008) {
            placeholderHimno.setText(R.string.placeholder_himno);
        } else {
            placeholderHimno.setText(R.string.placeholder_himno_old);
        }
    }

    private void setUpPanelControls() {
        if (!version2008) {
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
        buttonPlay.setImageResource(R.drawable.ic_play_circle_filled_black_36dp);
    }

    @Override
    public void playing() {
        buttonPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_36dp);
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

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.ajcm.hiad.services.MediaListenService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // tiempo de la musica en minutos y segundos
    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    // descarga de la musica del himno
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
                    layoutDownload.setVisibility(View.GONE);
                    layoutPlay.setVisibility(View.VISIBLE);
                    musicProgress.setProgress(0);
                    musicProcent.setText("");
                    musicProcent.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (numero > 0) {
                        musicSize.setText(FileUtils.humanReadableByteCount(((Himno2008) himnos.get(numero - 1)).getFileSize()));
                    } else {
                        musicSize.setText("");
                    }
                    musicProgress.setProgress(0);
                }
            });
        }
    }

    public static void showNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                ID_NOTIFICATION, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_music_note_black_36dp)
                .setContentText(context.getResources().getString(R.string.feliz_sabado));
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(ID_NOTIFICATION, mBuilder.build());
    }

    public void notAdsInSaturday() {
        Calendar currentDate = Calendar.getInstance();
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        calendar1.set(Calendar.HOUR_OF_DAY, 18);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        calendar2.set(Calendar.HOUR_OF_DAY, 18);
        calendar2.set(Calendar.MINUTE, 0);
        calendar2.set(Calendar.SECOND, 0);

        if (!currentDate.before(calendar2) || !calendar1.before(currentDate)) {
            adsMethod();
        }
    }

    public void setNotification() {
        UserPreferences userPreferences = new UserPreferences(getApplicationContext());
        Calendar currentDate = Calendar.getInstance();
        Calendar calendarFriday = Calendar.getInstance();
        calendarFriday.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        calendarFriday.set(Calendar.HOUR_OF_DAY, 18);
        calendarFriday.set(Calendar.MINUTE, 0);
        calendarFriday.set(Calendar.SECOND, 0);
        Calendar calendarSaturday = Calendar.getInstance();
        calendarSaturday.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        calendarSaturday.set(Calendar.HOUR_OF_DAY, 18);
        calendarSaturday.set(Calendar.MINUTE, 0);
        calendarSaturday.set(Calendar.SECOND, 0);

//        if (currentDate.before(calendarSaturday) && calendarFriday.before(currentDate)) {
        if (!userPreferences.getBoolean("alarm")) {
            cancelReminder();
            ComponentName receiver = new ComponentName(getApplicationContext(), AlarmReceiver.class);
            PackageManager pm = getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                    ID_NOTIFICATION, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendarFriday.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY * 7, pendingIntent);

            userPreferences.putBoolean("alarm", true);

            Intent intent2 = new Intent(getApplicationContext(), AlarmReceiver.class);
            intent2.putExtra("end_alarm",true);
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getApplicationContext(),
                    ID_NOTIFICATION + 11, intent2,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am2 = (AlarmManager) getSystemService(ALARM_SERVICE);
            am2.set(AlarmManager.RTC_WAKEUP, calendarSaturday.getTimeInMillis(), pendingIntent2);
        }
    }

    public void cancelReminder() {
        // Disable a receiver
        ComponentName receiver = new ComponentName(getApplicationContext(), AlarmReceiver.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Intent intent1 = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                ID_NOTIFICATION, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }
}
