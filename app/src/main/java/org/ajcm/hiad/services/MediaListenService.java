package org.ajcm.hiad.services;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import org.ajcm.hiad.R;
import org.ajcm.hiad.activities.MainActivity;
import org.ajcm.hiad.utils.FileUtils;

import java.io.IOException;

public class MediaListenService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "MediaListenService";
    public static final String CHANNEL_ID = "MusicServiceChannel";

    private MediaPlayer mediaPlayer;
    private final IBinder mBinder = new LocalBinder();

    private MediaServiceCallbacks callbacks;
    private AudioManager audioManager;

    Handler handler = new Handler();
    Runnable runnable;

    @Override
    public void onCreate() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        Log.e(TAG, "onStartCommand: ");

        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 100);
                callbacks.updateProgress(mediaPlayer.getCurrentPosition());
            }
        };


        return START_NOT_STICKY;
    }

    private void initStream(int numero, String titulo) {
        String himnoPath = FileUtils.getDirHimnos(getApplicationContext()).getAbsoluteFile() + "/" + FileUtils.getStringNumber(numero) + ".ogg";

        mediaPlayer.setOnErrorListener(errListener);
        mediaPlayer.setOnPreparedListener(prepListener);
        mediaPlayer.setOnCompletionListener(completionListener);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(himnoPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            callbacks.durationMedia(mediaPlayer.getDuration());

            startForegroundService(numero, titulo);

        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.e(TAG, "setDataSource IllegalArgumentException");
        }
    }

    private void startForegroundService(int numero, String titulo) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_SCREEN_ON);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.putExtra("number", numero);
        notificationIntent.putExtra("duration", mediaPlayer.getDuration());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Reproduciendo música")
                .setContentText("Himno " + numero + ": " + titulo)
                .setSmallIcon(R.drawable.ic_music_note_black_36dp)
                .setContentIntent(pendingIntent)
                .setDefaults(0)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        audioManager.abandonAudioFocus(this);
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    MediaPlayer.OnErrorListener errListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e(TAG, "onError: ");
            return true;
        }
    };

    MediaPlayer.OnPreparedListener prepListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Log.e(TAG, "onPrepared: ");
            try {
                runnable.run();
            } catch (Exception ex) {
                Log.e(TAG, "onPrepared: ", ex);
            }
        }
    };

    MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.e(TAG, "onCompletion: ");
            callbacks.completion();
            stopForeground(true);
        }
    };

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                Log.e(TAG, "onAudioFocusChange: Lost");
                callbacks.completion();
                stopForeground(true);
                handler.removeCallbacks(runnable);
                mediaPlayer.reset();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }

                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pauseMedia() {
        mediaPlayer.pause();
    }

    public void playMedia() {
        mediaPlayer.start();
        callbacks.playing();
    }

    public void registerClient(Activity activity) {
        this.callbacks = (MediaServiceCallbacks) activity;
    }

    public void playMedia(int numero, String titulo) {
        Log.e(TAG, "playMedia: ");
        initStream(numero, titulo);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
    }

    public void stopMedia() {
        Log.e(TAG, "stopMedia: ");
        mediaPlayer.stop();
        handler.removeCallbacks(runnable);
        audioManager.abandonAudioFocus(this);
        stopForeground(true);

    }

    public void setSeek(final int position) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(position);
            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    mp.start();
                    callbacks.playing();
                }
            });
        }
    }

    public interface MediaServiceCallbacks {
        void updateProgress(int mCurrentPosition);

        void durationMedia(int duration);

        void completion();

        void playing();
    }

    public class LocalBinder extends Binder {
        public MediaListenService getServiceInstance() {
            return MediaListenService.this;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setSound(null,null);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
