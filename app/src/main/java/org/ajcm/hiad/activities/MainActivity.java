package org.ajcm.hiad.activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private static final String TAG = "MainActivity";
    private static final String CURRENT_TEXT_NUMBER = "current_text_number";
    private static final String APP_PNAME = "org.ajcm.hiad";
    private static final String TOOLBAR_PANEL_TITLE = "toolbar_panel_title";
    private static final String NUM_STRING = "num_string";
    private static final String NUMERO = "numero";
    private static final int SEARCH_HIMNO = 7;
    private static final String TEXT_HIMNO = "text_himno";
    private static final String TEXT_SIZE = "text_size";

    public static final int ERROR_STATUS = -1;
    public static final int INIT_STATUS = 0;
    public static final int PLAY_STATUS = 1;
    public static final int PAUSE_STATUS = 2;
    public static final int STOP_STATUS = 3;

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
    private SeekBar seekBar;

    private ComponentName service_component_name;
    private Intent intentService;
    private Class<?> serviceClass = null;
    private boolean isBound;
    private Messenger mService;
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));
    int service_status = 0;

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

        restoreDataSaved(savedInstanceState);

        if (isServiceRunning() != null) {
            Handler runDelayedHandler = new Handler();
            runDelayedHandler.postDelayed(runDelayed, 500);
            doBindService();
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_TEXT_NUMBER, numberHimno.getText().toString());
        outState.putString(TOOLBAR_PANEL_TITLE, toolbarTitle.getText().toString());
        outState.putString(NUM_STRING, numString);
        outState.putString(TEXT_HIMNO, textHimno.getText().toString());
        outState.putInt(NUMERO, numero);
        outState.putFloat(TEXT_SIZE, textSize);
        outState.putBoolean("seek_bar", seekBar.getVisibility() == View.VISIBLE);
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
        doUnbindService();
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
    protected void onStop() {
        if (isBound) {
            doUnbindService();
            isBound = false;
            Log.e(TAG, "onStop: unbound");
        }
        super.onStop();
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

                setUpPanelMenu();
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

            setUpPanelMenu();

            numberHimno.setText("");
            setPlaceholderHimno();
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
            seekBar.setVisibility(savedInstanceState.getBoolean("seek_bar") ? View.VISIBLE : View.GONE);
            setUpPanelMenu();
            service_component_name = isServiceRunning();
            if (service_component_name != null){
                try {
                    serviceClass = Class.forName(service_component_name.getClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            intentService = new Intent(this, MediaListenService.class);
        }
    }

    private void setupUpPanel() {
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarPanel = (Toolbar) findViewById(R.id.toolbar_panel);
        toolbarPanel.inflateMenu(R.menu.menu_himno);
        toolbarPanel.setOnMenuItemClickListener(this);

        upPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        upPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        upPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelCollapsed(View panel) {
                if (isBound) {
                    sendMessageToService(3);
                    stopService(intentService);
                }
                cleanNum();
                seekBar.setVisibility(View.GONE);
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

    private void donwloadMusic(int numberInt) {
        String number = FileUtils.getStringNumber(numberInt);

        String url = "gs://tu-himnario-adventista.appspot.com";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReferenceFromUrl(url);
        StorageReference himnoRef = reference.child("himnos/" + number + ".ogg");

        File dirHimnos = new File(getFilesDir().getAbsolutePath() + "/himnos/");
        dirHimnos.mkdirs();

        final TextView musicProcent = (TextView) findViewById(R.id.music_procent);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.music_progress);

        File file = new File(dirHimnos.getAbsolutePath() + "/" + number + ".ogg");
        if (!file.exists()) {
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
            musicProcent.setVisibility(View.VISIBLE);

            himnoRef.getFile(file).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot.getBytesTransferred() > 1000) {
                        progressBar.setProgress((int) (taskSnapshot.getBytesTransferred() * 100 / taskSnapshot.getTotalByteCount()));
                        progressBar.setIndeterminate(false);
                    }
                    musicProcent.setText(progressBar.getProgress() + "%");
                }
            }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    musicProcent.setVisibility(View.GONE);
                    toolbarPanel.getMenu().getItem(0).setVisible(true);
                    toolbarPanel.getMenu().getItem(1).setVisible(false);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: ", e);
                }
            });
        }
    }

    private void setUpPanelMenu() {
        if (FileUtils.isHimnoDownloaded(getApplicationContext(), numero)) {
            toolbarPanel.getMenu().getItem(0).setVisible(true);
            toolbarPanel.getMenu().getItem(1).setVisible(false);
        } else {
            toolbarPanel.getMenu().getItem(0).setVisible(false);
            toolbarPanel.getMenu().getItem(1).setVisible(true);
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_play:
                Log.e(TAG, "action_play");
                String himnoPath = FileUtils.getDirHimnos(getApplicationContext()).getAbsoluteFile() + "/" + FileUtils.getStringNumber(numero) + ".ogg";
                initMediaService(himnoPath);
                seekBar.setVisibility(View.VISIBLE);
//                seekBar.setMax(mediaPlayer.getDuration() / 100);

//                Runnable runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        if (mediaPlayer.isPlaying()) {
//                            int mCurrentPosition = mediaPlayer.getCurrentPosition() / 100;
//                            seekBar.setProgress(mCurrentPosition);
//                            mHandler.postDelayed(this, 100);
//                        }
//                    }
//                };
//                runOnUiThread(runnable);
                return true;
            case R.id.action_download:
                Log.e(TAG, "onMenuItemClick: DOWNLOAD");
                donwloadMusic(numero);
                return true;
        }
        return false;
    }

    private void initMediaService(String himnoPath) {
        intentService = new Intent(this, MediaListenService.class);
        intentService.putExtra("himno_path", himnoPath);
        service_component_name = isServiceRunning();
        if (service_component_name == null){
            service_component_name = startService(intentService);
        }
        try {
            serviceClass = Class.forName(service_component_name.getClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Handler runDelayedHandler = new Handler();
        runDelayedHandler.postDelayed(runDelayed, 500);
        doBindService();
        Log.e(TAG, "initMediaService: " + isServiceRunning());
    }

    Runnable runDelayed = new Runnable() {
        public void run() {
            //Log.e("DEBUG", "Called runDelayed");
            // Query the service if it is running, but only if have not received error message
            if (service_status >= 0) {
                //Log.e("DEBUG", "runDelayed service_status >= 0");
                sendMessageToService(100);
            }
        }
    };

    private void sendMessageToService(int intvaluetosend) {
        if (!isBound)
            doBindService();

        if (isBound) {
            try {
                Message msg = Message.obtain(null, MediaListenService.MSG_SET_INT_VALUE, intvaluetosend, 0);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
            }
        }
    }

    private void doBindService() {
        if (!isBound) {
            // This is non-blocking and returns immediately, even if mConnection is not ready yet and mService is still NULL
            Log.e(TAG, "doBindService: " + serviceClass);
            Log.e(TAG, "doBindService: " + mConnection);
            bindService(new Intent(this, serviceClass), mConnection, Context.BIND_AUTO_CREATE);
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MediaListenService.MSG_REGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // In this case the service has crashed before we could even do anything with it
                }
                isBound = true;
                Log.e(TAG, "doBindService");
            }
        }
    }

    private void doUnbindService() {
        if (isBound) {
            // If we have received the service, and hence registered with it, then unregister
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MediaListenService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            isBound = false;
        }
    }

    static class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        private final WeakReference<MainActivity> mService;

        IncomingHandler(MainActivity service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity service = mService.get();
            switch (msg.what) {
                case MediaListenService.MSG_SET_INT_VALUE:
                    if (msg.arg1 == 1) {
                        service.seekBar.setMax(msg.arg2);
                    } else if (msg.arg1 == 2) {
                        service.seekBar.setProgress(msg.arg2);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.e(TAG, "onServiceConnected: ");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
        }
    };

    private ComponentName isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.ajcm.hiad.MediaListenService".equals(service.service.getClassName())) {
                return service.service;
            }
        }
        return null;
    }
}
