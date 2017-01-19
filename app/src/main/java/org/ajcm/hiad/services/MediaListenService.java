package org.ajcm.hiad.services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import org.ajcm.hiad.utils.FileUtils;

import java.io.IOException;

public class MediaListenService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "MediaListenService";
    private MediaPlayer mediaPlayer;
    private final IBinder mBinder = new LocalBinder();

    private MediaServiceCallbacks callbacks;
    private AudioManager audioManager;

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

    Handler handler = new Handler();
    Runnable runnable;

    private void initStream(int numero) {
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
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.e(TAG, "setDataSource IllegalArgumentException");
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        Log.e(TAG, "onDestroy: " + mediaPlayer);
        super.onDestroy();
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
            runnable.run();
        }
    };

    MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.e(TAG, "onCompletion: ");
            callbacks.completion();
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

    public void playMedia(int numero) {
        Log.e(TAG, "playMedia: ");
        initStream(numero);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
    }

    public void stopMedia() {
        Log.e(TAG, "stopMedia: ");
        mediaPlayer.stop();
        handler.removeCallbacks(runnable);
        audioManager.abandonAudioFocus(this);

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
}
