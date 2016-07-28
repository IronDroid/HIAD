package org.ajcm.hiad;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MediaListenService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "MediaListenService";
    private MediaPlayer mediaPlayer;
    Bundle recvBundle;
    String himnoPath;
    WifiLock wifiLock;

    private int playback_status = 0;

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_INT_VALUE = 3;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    final Messenger mMessenger = new Messenger(new IncomingHandler(this)); // Target we publish for clients to send messages to IncomingHandler.
    private boolean audioFocusGranted;

    @Override
    public void onCreate() {

        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            this.audioFocusGranted = true;
        } else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            this.audioFocusGranted = false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        Log.e(TAG, "onStartCommand: ");
        recvBundle = intent.getExtras();
        if (recvBundle != null)
            himnoPath = recvBundle.getString("himno_path");

        initStream();

        return START_NOT_STICKY;
    }

    Handler handler = new Handler();

    private void initStream() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnErrorListener(errListener);
        mediaPlayer.setOnPreparedListener(prepListener);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(himnoPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer.isPlaying()) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 100;
                        sendMessageToUI(2, mCurrentPosition);
                        handler.postDelayed(this, 100);
                    } else {
                        stopSelf();
                    }
                }
            };
            runnable.run();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.e(TAG, "setDataSource IllegalArgumentException");
            playback_status = -1;
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        mediaPlayer.stop();
        mediaPlayer.release();
        wifiLock.release();
        super.onDestroy();
        this.stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    MediaPlayer.OnErrorListener errListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            playback_status = -1;
            sendMessageToUI(0, playback_status);
            Log.e(TAG, "onError: ");
            return true;
        }
    };

    MediaPlayer.OnPreparedListener prepListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
            playback_status = 1;
            sendMessageToUI(0, playback_status);
            Log.e(TAG, "onPrepared: ");
            Toast.makeText(MediaListenService.this, "playing", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.e(TAG, "onAudioFocusChange: " + focusChange);
        Log.e(TAG, "focus garanted: " + this.audioFocusGranted);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.e(TAG, "onAudioFocusChange: AUDIOFOCUS_GAIN" );
                // resume playback
                if (mediaPlayer == null) initStream();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                Log.e(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS");
                // Lost focus for an unbounded amount of time: stop playback and release media player
                mediaPlayer.release();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.e(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT");
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playback_status = 2;
                }

                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.e(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    static class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        private final WeakReference<MediaListenService> mService;

        IncomingHandler(MediaListenService service) {
            mService = new WeakReference<MediaListenService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MediaListenService service = mService.get();
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    service.mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    service.mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_INT_VALUE:
                    Log.e(TAG, "handleMessage: msg " + msg.arg1);
                    if (msg.arg1 == 1) {
                        // Start playback
                        if (service.playback_status == 2) {
                            service.mediaPlayer.start();
                            service.playback_status = 1;
                        }
                        if (service.playback_status == 3) {
                            service.wifiLock.acquire();
                            service.mediaPlayer.reset();
                            service.playback_status = 1;
                        }
                    } else if (msg.arg1 == 2) {
                        // Pause playback
                        if (service.playback_status == 1) {
                            if (service.mediaPlayer == null) {
                                service.initStream();
                            }
                            service.mediaPlayer.pause();
                            service.playback_status = 2;
                        }
                    }
                    if (msg.arg1 == 3) {
                        service.mediaPlayer.stop();
                        service.playback_status = 3;
                    }
                    if (msg.arg1 == 0) {
                        service.wifiLock.acquire();
                        service.initStream();
                    }
                    service.sendMessageToUI(0, service.playback_status);
                    if (msg.arg1 == 100){
                        service.sendMessageToUI(1, service.mediaPlayer.getDuration()/100);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMessageToUI(int keyValue, int value) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i).send(Message.obtain(null, MSG_SET_INT_VALUE, keyValue, value));
            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

} 
