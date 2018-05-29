package org.ajcm.hiad.activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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

import org.ajcm.hiad.BuildConfig;
import org.ajcm.hiad.CallbackFragments;
import org.ajcm.hiad.R;
import org.ajcm.hiad.contracts.UpPanelContract;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.fragments.ContenidoMainFragment;
import org.ajcm.hiad.fragments.DownloadFragment;
import org.ajcm.hiad.fragments.MainFragment;
import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.models.Himno1962;
import org.ajcm.hiad.models.Himno2008;
import org.ajcm.hiad.presenters.UpPanelPresenter;
import org.ajcm.hiad.services.MediaListenService;
import org.ajcm.hiad.utils.ConnectionUtils;
import org.ajcm.hiad.utils.FileUtils;
import org.ajcm.hiad.views.ZoomTextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import static org.ajcm.hiad.MyFirebaseMessagingService.OPEN_HIMNO;

public class MainActivity extends AppCompatActivity implements
        MediaListenService.MediaServiceCallbacks,
        NavigationView.OnNavigationItemSelectedListener,
        CallbackFragments,
        UpPanelContract.View {

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

    private TextView placeholderHimno;
    private AdView adView;

    private ZoomTextView textHimno;
    private TextView toolbarTitle;
    private MaterialFavoriteButton favoriteButton;
    private SlidingUpPanelLayout upPanelLayout;
    private DBAdapter dbAdapter;
    private ArrayList<? extends Himno> himnos;

    private Himno2008 himno;
    private boolean version2008 = true;
    private int limit;

    private boolean firstPlay;

    private boolean toastClose;
    private Toast toast;

    private FirebaseAnalytics analytics;

    private Intent intentService;
    private MediaListenService listenService;

    private LinearLayout layoutDownload;
    private LinearLayout layoutPlay;
    private SeekBar seekBar;
    private ImageButton buttonPlay;
    private ImageButton buttonDownload;
    private ImageButton buttonCancel;
    private TextView musicSize;
    private TextView musicTime;
    private TextView musicDuration;
    private TextView musicProcent;
    private TextView titleDownload;
    private ProgressBar musicProgress;

    private StorageReference reference;
    private FileDownloadTask fileDownloadTask;

    private UpPanelContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        initLayout();

        if (!BuildConfig.DEBUG) {
            notAdsInSaturday();
            analitycsMethod();
        }

        limit = NEW_LIMIT;

        setupUpPanel();
//        restoreDataSaved(savedInstanceState);

//        setUpPanelControls();

        dbAdapter = new DBAdapter(getApplicationContext());

        presenter = new UpPanelPresenter(this);
        favoriteButton.setOnFavoriteAnimationEndListener(new MaterialFavoriteButton.OnFavoriteAnimationEndListener() {
            @Override
            public void onAnimationEnd(MaterialFavoriteButton buttonView, boolean favorite) {
                if (upPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED && himno.getNumero() > 0) {
                    dbAdapter.setFav(himno.getNumero(), favorite, version2008);
                    himno.setFavorito(favorite);
                }
            }
        });

//
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
        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectionUtils.hasInternet(getApplicationContext())) {
                    musicProcent.setVisibility(View.VISIBLE);
                    buttonCancel.setVisibility(View.VISIBLE);
                    titleDownload.setVisibility(View.GONE);
                    buttonDownload.setVisibility(View.GONE);
                    downloadMusic(himno.getNumero());
                } else {
                    Toast.makeText(MainActivity.this, "No hay internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                titleDownload.setVisibility(View.VISIBLE);
                buttonDownload.setVisibility(View.VISIBLE);
                musicProcent.setVisibility(View.GONE);
                buttonCancel.setVisibility(View.GONE);
                musicProgress.setVisibility(View.GONE);
                musicProgress.setIndeterminate(false);
                fileDownloadTask.cancel();

                String number = FileUtils.getStringNumber(himno.getNumero());
                File dirHimnos = new File(getFilesDir().getAbsolutePath() + "/himnos/");
                File file = new File(dirHimnos.getAbsolutePath() + "/" + number + ".ogg");
                Log.e(TAG, "on Click Cancel: " + file.delete());

                musicSize.setText(FileUtils.humanReadableByteCount(himno.getFileSize()));
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
                    listenService.playMedia(himno.getNumero());
                    buttonPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_36dp);
                }
            }
        });
//
        intentService = new Intent(this, MediaListenService.class);
        if (!isServiceRunning()) {
            startService(intentService); //Starting the service
            bindService(intentService, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!
        }
//
        String urlFirebase = "gs://tu-himnario-adventista.appspot.com";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        reference = storage.getReferenceFromUrl(urlFirebase);
//
//        actionNotification();

        // TODO: 16-03-18 navigation version
//        Menu menu = navigationView.getMenu();
//        MenuItem menuItem = menu.findItem(R.id.nav_version);
//        View actionView = MenuItemCompat.getActionView(menuItem);
//        SwitchCompat switcher = (SwitchCompat) actionView.findViewById(R.id.switcher);
//        switcher.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(MainActivity.this, "version click " + ((SwitchCompat) view).isChecked(), Toast.LENGTH_SHORT).show();
//            }
//        });
        // TODO: 16-03-18 modo nocturno
//        menuItem = menu.findItem(R.id.nav_mode);
//        actionView = MenuItemCompat.getActionView(menuItem);
//        switcher = (SwitchCompat) actionView.findViewById(R.id.switcher);
//        switcher.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(MainActivity.this, "mode click " + ((SwitchCompat) view).isChecked(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void initLayout() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // color del statusbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        }
        setMainFragment();
        favoriteButton = findViewById(R.id.fav_button);
        textHimno = findViewById(R.id.text_himno);
        placeholderHimno = findViewById(R.id.placeholder_himno);
        layoutDownload = findViewById(R.id.layout_download);
        layoutPlay = findViewById(R.id.layout_play);
        seekBar = findViewById(R.id.seek_bar);
        buttonDownload = findViewById(R.id.music_download);
        buttonCancel = findViewById(R.id.music_cancel);
        buttonPlay = findViewById(R.id.button_play);
        musicSize = findViewById(R.id.music_size);
        musicTime = findViewById(R.id.music_time);
        musicDuration = findViewById(R.id.music_duration);
        musicProcent = findViewById(R.id.music_procent);
        titleDownload = findViewById(R.id.title_download);
        musicProgress = findViewById(R.id.music_progress);
    }

    private void setMainFragment() {
        Fragment fragment = MainFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content, fragment, MainFragment.class.getSimpleName()).commit();
    }

    // esto se activa cuando se abre la notificacion push en primer plano
    public void actionNotification() {
        Random random = new Random();
        if (getIntent().getAction() != null && getIntent().getAction().equalsIgnoreCase(OPEN_HIMNO)) {
            ArrayList<Himno2008> allHimno = (ArrayList<Himno2008>) dbAdapter.getAllHimnoFav(version2008);
            if (allHimno.size() > 0) {
                himno = allHimno.get(random.nextInt(allHimno.size()));
                // TODO: 15-05-18 accion para la notificacion
//                numOk(null);
            }
            dbAdapter.close();
        }
    }

    // crea el banner de publicidad
    private void adsMethod() {
        adView = findViewById(R.id.adView);
        adView.setVisibility(View.VISIBLE);
        adView.loadAd(new AdRequest.Builder().build());
    }

    // inicializa analitics
    private void analitycsMethod() {
        analytics = FirebaseAnalytics.getInstance(this);
    }

    // recupera los datos guardados en onSaveInstanceState()
    private void restoreDataSaved(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            version2008 = savedInstanceState.getBoolean(VERSION_HIMNOS);
            toolbarTitle.setText(savedInstanceState.getString(TOOLBAR_PANEL_TITLE));
            textHimno.setText(savedInstanceState.getString(TEXT_HIMNO));
            seekBar.setMax(savedInstanceState.getInt("seek_max"));
            musicDuration.setText(savedInstanceState.getString("duration"));
            firstPlay = savedInstanceState.getBoolean("first_play");
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
        // TODO: 05-04-18 data saved
//        outState.putString(CURRENT_TEXT_NUMBER, numberHimno.getText().toString());
//        outState.putString(TOOLBAR_PANEL_TITLE, toolbarTitle.getText().toString());
//        outState.putString(NUM_STRING, numString);
//        outState.putString(TEXT_HIMNO, textHimno.getText().toString());
//        outState.putInt(NUMERO, numero);
//        if (listenService != null) {
//            outState.putBoolean("is_playing", listenService.isPlaying());
//        }
//        Log.e(TAG, "onSaveInstanceState: se guarda version " + version2008);
//        outState.putBoolean(VERSION_HIMNOS, version2008);
//        outState.putInt("seek_max", seekBar.getMax());
//        outState.putString("duration", musicDuration.getText().toString());
//        outState.putBoolean("first_play", firstPlay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (upPanelLayout != null &&
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
        } else if (fragmentByTag == null) {
            setMainFragment();
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.getItem(0);
            item.setChecked(true);
        } else {
            Log.e(TAG, "onBackPressed: ");
//            stopService(intentService);
            super.onBackPressed();
        }

    }

    // resultado de la busqueda
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            int numberHimno = data.getExtras().getInt("numero", 0);
            Himno himno = dbAdapter.getHimno(numberHimno, version2008);
            this.openUpPanel(himno);

//            numero = data.getExtras().getInt("numero", 0);
//            if (numero > 0) {
//                if (version2008) {
//                    favoriteButton.setFavorite(((Himno2008) himnos.get(numero - 1)).isFavorito());
//                    musicSize.setText(FileUtils.humanReadableByteCount(((Himno2008) himnos.get(numero - 1)).getFileSize()));
//                } else {
//                    favoriteButton.setFavorite(((Himno1962) himnos.get(numero - 1)).isFavorito());
//                }
//                placeholderHimno.setText("");
//                numString = "" + numero;
//                upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
//                String titleShow = numString + ". " + himnos.get(numero - 1).getTitulo();
//                toolbarTitle.setText(titleShow);
//                titleDownload.setText(titleShow);
//                titleDownload.setText(titleShow);
//                textHimno.setText(himnos.get(numero - 1).getLetra());
//                setUpPanelControls();
//            }
        }
    }

    // configuracion inicial del panelUp
    private void setupUpPanel() {
        toolbarTitle = findViewById(R.id.toolbar_title);
        upPanelLayout = findViewById(R.id.sliding_layout);
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        upPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelCollapsed(View panel) {
                // TODO: 15-05-18 clean number
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

    // controles del panelUp, si se muestra o no segun la version
    private void setUpPanelControls() {
        if (!version2008) {
            layoutDownload.setVisibility(View.GONE);
            layoutPlay.setVisibility(View.GONE);
            return;
        }
        if (FileUtils.isHimnoDownloaded(getApplicationContext(), himno.getNumero())) {
            layoutDownload.setVisibility(View.GONE);
            layoutPlay.setVisibility(View.VISIBLE);
            seekBar.setProgress(0);
            musicTime.setText("00:00");
            String noTime = "--:--";
            if (himno.getDuracion().equalsIgnoreCase(noTime)) {
                musicDuration.setText("--:--");
            } else {
                musicDuration.setText(himno.getDuracion());
            }
        } else {
            layoutDownload.setVisibility(View.VISIBLE);
            layoutPlay.setVisibility(View.GONE);
            titleDownload.setVisibility(View.VISIBLE);
            buttonDownload.setVisibility(View.VISIBLE);
            musicProcent.setVisibility(View.GONE);
            buttonCancel.setVisibility(View.GONE);
            musicProgress.setVisibility(View.GONE);
            musicProgress.setIndeterminate(false);
        }
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
        // TODO: 28-05-18 guardar la duracion en el modelo himno
        if (himno.getDuracion().equalsIgnoreCase("--:--")) {
            String labelDuration = getDate(duration);
            dbAdapter.setDuration(himno.getNumero(), labelDuration, version2008);
            himno.setDuracion(labelDuration);
        }
        musicDuration.setText(himno.getDuracion());
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
        stopService(intentService);
        unbindService(mConnection);
        Log.e(TAG, "onStop: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(intentService); //Starting the service
        bindService(intentService, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!
        Log.e(TAG, "onStart: ");
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        dbAdapter.close();
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
    private void downloadMusic(int numberInt) {
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
                                + " de " + FileUtils.humanReadableByteCount(taskSnapshot.getTotalByteCount()) + " Descargados");
                    }
                    musicProcent.setText(musicProgress.getProgress() + "%");
                }
            }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    layoutPlay.setVisibility(View.VISIBLE);
                    layoutDownload.setVisibility(View.GONE);
                    musicProcent.setVisibility(View.GONE);
                    musicProgress.setProgress(0);
                    musicProcent.setText("");
                    seekBar.setProgress(0);
                    musicTime.setText("00:00");
                    musicDuration.setText(himno.getDuracion());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    musicSize.setText(FileUtils.humanReadableByteCount(himno.getFileSize()));
                }
            });
        }
    }

    private void showLayoutDownload() {
        layoutDownload.setVisibility(View.VISIBLE);
    }

    private void hideLayoutDownload() {
        layoutDownload.setVisibility(View.GONE);
    }

    /*
    evita la publicidad para los sabados
     */
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Class fragmentClass = null;
        String tagFragment = "";
        View actionView = MenuItemCompat.getActionView(item);
        switch (item.getItemId()) {
            case R.id.nav_main:
                fragmentClass = MainFragment.class;
                getSupportActionBar().setTitle(R.string.app_name);
                break;

            case R.id.nav_music:
                if (this.version2008) {
                    fragmentClass = DownloadFragment.class;
//                    startActivityForResult(new Intent(MainActivity.this, MusicActivity.class), REQUEST_SEARCH_HIMNO);
                } else {
                    Toast.makeText(this, "No Disponible en la version antigua del himnario", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.nav_contenido:
                if (this.version2008) {
                    fragmentClass = ContenidoMainFragment.class;
//                    startActivityForResult(new Intent(MainActivity.this, ContenidoActivity.class), REQUEST_SEARCH_HIMNO);
                } else {
                    Toast.makeText(this, "No Disponible en la version antigua del himnario", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_version:
                SwitchCompat switcher = actionView.findViewById(R.id.switcher);
                switcher.performClick();
                if (switcher.isChecked()) {
//                    switcher.setChecked(false);
                    version2008 = true;
                    limit = NEW_LIMIT;
                } else {
//                    switcher.setChecked(true);
                    version2008 = false;
                    limit = OLD_LIMIT;
                    Bundle params = new Bundle();
                    params.putString("Category", "Action");
                    params.putString("Action", "Version_Antiguo");
//                    analytics.logEvent("Change_version", params);
                }
                // TODO: 14-07-17 agrupar estas funciones
                break;

//            case R.id.nav_mode:
//                switcher.performClick();
//                break;

            case R.id.nav_rate:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                break;

            case R.id.nav_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tu Himnario Adventista");

                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + APP_PNAME);
                startActivity(Intent.createChooser(sharingIntent, "Compartir via..."));
                break;

            case R.id.nav_about:
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
                break;
        }
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            tagFragment = fragmentClass.getSimpleName();
            fragmentManager.beginTransaction().replace(R.id.content, fragment, tagFragment).commit();
        } catch (Exception e) {
            Log.e(TAG, "onNavigationItemSelected: " + e.getMessage(), e);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void callbackOK(Class fragmentClass, Himno himno) {
        openUpPanel(himno);
    }

    private void openUpPanel(Himno h) {
        himno = (Himno2008) h;
        findViewById(R.id.scroll_himno).scrollTo(0, 0);
        String titleShow = himno.getNumero() + ". " + himno.getTitulo();
        toolbarTitle.setText(titleShow);
        textHimno.setText(himno.getLetra());
        titleDownload.setText(titleShow);
        musicSize.setText(FileUtils.humanReadableByteCount(((Himno2008) himno).getFileSize()));
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        setUpPanelControls();
    }

    @Override
    public void showTitle(String title) {
        Log.e(TAG, "showTitle: " + title);
    }
}
