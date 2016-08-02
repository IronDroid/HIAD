package org.ajcm.hiad;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import org.ajcm.hiad.utils.FileUtils;

import java.io.IOException;

public class MediaListenService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "MediaListenService";
    private MediaPlayer mediaPlayer;
    private final IBinder mBinder = new LocalBinder();

    private int playback_status = 0;

    private MediaServiceCallbacks callbacks;

    @Override
    public void onCreate() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            this.audioFocusGranted = true;
//        } else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
//            this.audioFocusGranted = false;
//        }
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        Log.e(TAG, "onStartCommand: ");

        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer.isPlaying()) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 100;
                    handler.postDelayed(this, 100);
                    callbacks.updateProgress(mCurrentPosition);
                }
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
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(himnoPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            callbacks.setMaxProgress(mediaPlayer.getDuration() / 100);
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.e(TAG, "setDataSource IllegalArgumentException");
            playback_status = -1;
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
            playback_status = -1;
//            sendMessageToUI(0, playback_status);
            Log.e(TAG, "onError: ");
            return true;
        }
    };

    MediaPlayer.OnPreparedListener prepListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
            playback_status = 1;
//            sendMessageToUI(0, playback_status);
            Log.e(TAG, "onPrepared: ");
            runnable.run();
        }
    };

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                mediaPlayer.release();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
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

    public class LocalBinder extends Binder {
        public MediaListenService getServiceInstance() {
            return MediaListenService.this;
        }
    }

    public void registerClient(Activity activity) {
        this.callbacks = (MediaServiceCallbacks) activity;
    }

    public void playMedia(int numero) {
        Log.e(TAG, "playMedia: ");
        initStream(numero);
        callbacks.playMedia();
    }

    public void stopMedia() {
        Log.e(TAG, "stopMedia: ");
        mediaPlayer.stop();
        callbacks.stopMedia();
    }

    public interface MediaServiceCallbacks {
        void playMedia();

        void stopMedia();

        void updateProgress(int mCurrentPosition);

        void setMaxProgress(int duration);
    }
}
