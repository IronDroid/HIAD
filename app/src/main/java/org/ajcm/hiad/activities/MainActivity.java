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
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
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

import org.ajcm.hiad.BuildConfig;
import org.ajcm.hiad.CallbackFragments;
import org.ajcm.hiad.HiadApplication;
import org.ajcm.hiad.R;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.fragments.ContenidoMainFragment;
import org.ajcm.hiad.fragments.DownloadFragment;
import org.ajcm.hiad.fragments.MainFragment;
import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.models.Himno1962;
import org.ajcm.hiad.models.Himno2008;
import org.ajcm.hiad.services.MediaListenService;
import org.ajcm.hiad.utils.ConnectionUtils;
import org.ajcm.hiad.utils.FileUtils;
import org.ajcm.hiad.utils.UserPreferences;
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
        CallbackFragments {

    private static final String TAG = "MainActivity";
    private static final String CURRENT_TEXT_NUMBER = "current_text_number";
    private static final String APP_PNAME = "org.ajcm.hiad";
    private static final String TOOLBAR_PANEL_TITLE = "toolbar_panel_title";
    private static final String NUM_STRING = "num_string";
    public static final String NUMERO = "numero";
    private static final String VERSION_HIMNOS = "version";
    private static final String TEXT_HIMNO = "text_himno";
    private static final int REQUEST_SEARCH_HIMNO = 777;

    private AdView adView;
    private DBAdapter dbAdapter;

    private Himno2008 himno;
    private Himno1962 himno1962;
    private boolean version2008 = true;

    private boolean toastClose;
    private Toast toast;

    private boolean firstPlay;
    private Intent intentService;
    private MediaListenService listenService;

    private ZoomTextView textHimno;
    private TextView toolbarTitle;
    private MaterialFavoriteButton favoriteButton;
    private SlidingUpPanelLayout upPanelLayout;
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
    private TextView textVersionApp;
    private ProgressBar musicProgress;

    private StorageReference reference;
    private FileDownloadTask fileDownloadTask;
    private FirebaseAnalytics analytics;

    String tagFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        initLayout();

        setupUpPanel();

        dbAdapter = new DBAdapter(getApplicationContext());
        restoreDataSaved(savedInstanceState);

        onListeners();

        initMediaListener();

        initFirebaseStorage();

//        actionNotification();
    }

    private void initFirebaseStorage() {
        String urlFirebase = getResources().getString(R.string.url_firebase);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        reference = storage.getReferenceFromUrl(urlFirebase);
    }

    private void initMediaListener() {
        intentService = new Intent(this, MediaListenService.class);
        if (!isServiceRunning()) {
            Log.e(TAG, "onCreate: create service");
            startService(intentService); //Starting the service
            bindService(intentService, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!
        } else {
            Log.e(TAG, "onCreate: service running");
        }
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

        // color del statusbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        }

        favoriteButton = findViewById(R.id.fav_button);
        textHimno = findViewById(R.id.text_himno);
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

        if (!BuildConfig.DEBUG) {
            notAdsInSaturday();
            analitycsMethod();
        }
    }

    private void onListeners() {
        favoriteButton.setOnFavoriteAnimationEndListener(new MaterialFavoriteButton.OnFavoriteAnimationEndListener() {
            @Override
            public void onAnimationEnd(MaterialFavoriteButton buttonView, boolean favorite) {
                if (upPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED && himno.getNumero() > 0) {
                    dbAdapter.setFav(himno.getNumero(), favorite, version2008);
                    himno.setFavorito(favorite);
                }
            }
        });

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

        // TODO: 16-03-18 navigation version
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        textVersionApp = headerView.findViewById(R.id.text_version_app);
        textVersionApp.setText("v" + BuildConfig.VERSION_NAME);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.nav_version);
        View actionView = MenuItemCompat.getActionView(menuItem);
        SwitchCompat switcher = actionView.findViewById(R.id.switcher);
        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((SwitchCompat) view).isChecked();
                Toolbar toolbarPanel = findViewById(R.id.toolbar_panel);
                MenuItem item = toolbarPanel.getMenu().findItem(R.id.action_partiture);
                if (!checked) {
                    version2008 = true;
                    favoriteButton.setVisibility(View.VISIBLE);
                } else {
                    version2008 = false;
                    favoriteButton.setVisibility(View.GONE);
                    Bundle params = new Bundle();
                    params.putString("Category", "Action");
                    params.putString("Action", "Version_Antiguo");
//                    analytics.logEvent("Change_version", params);
                }
                upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                item.setVisible(version2008);
                setMainFragment();
            }
        });

        // TODO: 16-03-18 modo nocturno
        menuItem = menu.findItem(R.id.nav_mode);
        actionView = MenuItemCompat.getActionView(menuItem);
        switcher = actionView.findViewById(R.id.switcher);
        UserPreferences preferences = new UserPreferences(this);
        switcher.setChecked(preferences.getBoolean(HiadApplication.NIGHT_MODE));
        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: switcher nocturno");
                boolean checked = ((SwitchCompat) view).isChecked();
                UserPreferences preferences = new UserPreferences(getApplicationContext());
                preferences.putBoolean(HiadApplication.NIGHT_MODE, checked);
                if (checked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                recreate();
            }
        });
    }

    private void setMainFragment() {
        Fragment fragment = MainFragment.newInstance(version2008);
        FragmentManager fragmentManager = getSupportFragmentManager();
        tagFragment = MainFragment.class.getSimpleName();
        fragmentManager.beginTransaction().replace(R.id.content, fragment, tagFragment).commit();
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
            Log.e(TAG, "restoreDataSaved: " + version2008);
            int numberHimno = savedInstanceState.getInt(NUMERO, 0);
            if (numberHimno > 0) {
                Himno himno = dbAdapter.getHimno(numberHimno, version2008);
                if (version2008) {
                    openUpPanel(himno);
                } else {
                    openUpPanelOld(himno);
                }
            }
            seekBar.setMax(savedInstanceState.getInt("seek_max"));
            firstPlay = savedInstanceState.getBoolean("first_play");
            if (savedInstanceState.getBoolean("is_playing")) {
                buttonPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_36dp);
            } else {
                buttonPlay.setImageResource(R.drawable.ic_play_circle_filled_black_36dp);
            }
        } else {
            setMainFragment();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(VERSION_HIMNOS, version2008);
        if (version2008) {
            if (himno != null) {
                outState.putInt(NUMERO, himno.getNumero());
            }
        } else {
            if (himno1962 != null) {
                outState.putInt(NUMERO, himno1962.getNumero());
            }
        }
        if (listenService != null) {
            outState.putBoolean("is_playing", listenService.isPlaying());
        }
        outState.putInt("seek_max", seekBar.getMax());
        outState.putBoolean("first_play", firstPlay);
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
            stopService(intentService);
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
            if (version2008) {
                openUpPanel(himno);
            } else {
                openUpPanelOld(himno);
            }
        }
    }

    // configuracion inicial del panelUp
    private void setupUpPanel() {
        Toolbar toolbar = findViewById(R.id.toolbar_panel);
        toolbar.inflateMenu(R.menu.menu_himno);
        MenuItem item = toolbar.getMenu().findItem(R.id.action_partiture);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startActivity(new Intent(MainActivity.this, PartitureActivity.class)
                .putExtra(NUMERO, himno.getNumero()));
                return true;
            }
        });
        toolbarTitle = findViewById(R.id.toolbar_title);
        upPanelLayout = findViewById(R.id.sliding_layout);
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        upPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelCollapsed(View panel) {
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
            Log.e(TAG, "onServiceConnected: playing " + listenService.isPlaying());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "onService Disconnected");
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
        unbindService(mConnection);
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

    private boolean userInteraction;

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Class fragmentClass = null;
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
                userInteraction = true;
                switcher.performClick();
                upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                break;

            case R.id.nav_mode:
                SwitchCompat switcher2 = actionView.findViewById(R.id.switcher);
                switcher2.performClick();
                break;

            case R.id.nav_rate:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                break;

            case R.id.nav_privacy:
                String url = "https://jhonlimaster.wixsite.com/hiad";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
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
        if (fragmentClass != null) {
            if (fragmentClass.getSimpleName().equals(MainFragment.class.getSimpleName())) {
                setMainFragment();
            } else {
                setFragment(fragmentClass);
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setFragment(Class fragmentClass) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            tagFragment = fragmentClass.getSimpleName();
            fragmentManager.beginTransaction().replace(R.id.content, fragment, tagFragment).commit();
        } catch (Exception e) {
            Log.d(TAG, "setFragment: " + e.getMessage());
        }
    }

    @Override
    public void callbackOK(Class fragmentClass, Himno him) {
        if (version2008) {
            openUpPanel(him);
        } else {
            openUpPanelOld(him);
        }
    }

    private void openUpPanel(Himno h) {
        Log.e(TAG, "openUpPanel: " + h.getNumero());
        himno = (Himno2008) h;
        findViewById(R.id.scroll_himno).scrollTo(0, 0);
        String titleShow = himno.getNumero() + ". " + himno.getTitulo();
        toolbarTitle.setText(titleShow);
        textHimno.setText(himno.getLetra());
        titleDownload.setText(titleShow);
        musicSize.setText(FileUtils.humanReadableByteCount(himno.getFileSize()));
        favoriteButton.setFavorite(himno.isFavorito());
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        setUpPanelControls();
    }

    private void openUpPanelOld(Himno him) {
        himno1962 = (Himno1962) him;
        findViewById(R.id.scroll_himno).scrollTo(0, 0);
        String titleShow = himno1962.getNumero() + ". " + himno1962.getTitulo();
        toolbarTitle.setText(titleShow);
        textHimno.setText(himno1962.getLetra());
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        setUpPanelControls();
    }
}
