package com.jaodevelop.google_speech_api_android.media;

import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

/**
 * Created by jao on 7/7/16.
 */
public class AudioPlayer {

    private final String TAG = "AudioPlayer";

    private String mAudioFileFullPath = null;

    private MediaPlayer mMediaPlayer = null;

    // Set AudioPlayingListener interface for "onStopped" callback
    public interface AudioPlayingListener {
        public void onAudioPlayingStopped();
    }

    private AudioPlayingListener mOnStoppedListner = null;

    public void setOnStoppoedListener(AudioPlayingListener eventListner) {
        mOnStoppedListner = eventListner;
    }

    // Primary constructor
    public AudioPlayer(String audioFileFullPath) {
        mAudioFileFullPath = audioFileFullPath == null ?
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/audioForAudioPlayer.3gp" : audioFileFullPath;
    }

    public AudioPlayer() {
        this(null);
    }

    public void startPlaying(String audioFileFullPath) {
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }

        });

        try {
            String filePath = audioFileFullPath == null ? mAudioFileFullPath : audioFileFullPath;
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();

        } catch (IOException e) {
            Log.e(TAG, "startPlaying() failed");
        }
    }

    public void startPlaying() {
        startPlaying(null);
    }

    public void stopPlaying() {

        if(mMediaPlayer.isPlaying())
        {
            mMediaPlayer.stop();
        }

        if(mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        Log.e(TAG, "stopPlaying()");

        if(mOnStoppedListner != null) {
            mOnStoppedListner.onAudioPlayingStopped();
        }

    }



}
