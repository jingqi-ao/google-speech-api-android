package com.jaodevelop.google_speech_api_android;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jaodevelop.google_speech_api_android.media.AudioPlayer;
import com.jaodevelop.google_speech_api_android.media.AudioRecorder;
import com.jaodevelop.google_speech_api_android.media.Transcoder;

public class MainActivity extends AppCompatActivity implements AudioPlayer.AudioPlayingListener, Transcoder.TranscodingListener {

    private final String TAG = "MainActivity";

    private final String STATUS_READY = "Ready";
    private final String STATUS_RECORDING = "Recording";
    private final String STATUS_REPLAYING = "Replaying";
    private final String STATUS_RECOGNIZING = "Recognizing";

    private String mStatus = STATUS_READY;

    private final String mUser3GPFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/google-speech-api-android-user-sound.3gp";
    private final String mUserWaveFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/google-sppech-api-android-user-sound.wav";


    private String mTranscodingServerURL = "https://192.168.0.24:9443/api/v1/transcode";

    // Media
    AudioRecorder mAudioRecoder;
    AudioPlayer mAudioPlayer;

    Transcoder mTranscoder;

    // UI
    Button mBtnRecord;
    Button mBtnReplay;
    Button mBtnRecognize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Media
        mAudioRecoder = new AudioRecorder(mUser3GPFilePath);
        mAudioPlayer = new AudioPlayer(mUser3GPFilePath);
        mAudioPlayer.setOnStoppoedListener(this);

        mTranscoder = new Transcoder(mTranscodingServerURL);

        // UI
        mBtnRecord = (Button) findViewById(R.id.btnRecord);
        mBtnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mStatus == STATUS_READY) {
                    mStatus = STATUS_RECORDING;
                    mAudioRecoder.startRecord();
                    updateStatus();
                    return;
                }

                if(mStatus == STATUS_RECORDING) {
                    mStatus = STATUS_READY;
                    mAudioRecoder.stopRecord();
                    updateStatus();
                    return;
                }

            }
        });

        mBtnReplay = (Button) findViewById(R.id.btnReplay);
        mBtnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mStatus == STATUS_READY) {
                    mStatus = STATUS_REPLAYING;
                    mAudioPlayer.startPlaying();
                    updateStatus();
                    return;
                }

                // Handle manual stop
                if(mStatus == STATUS_REPLAYING) {
                    mStatus = STATUS_READY;
                    mAudioPlayer.stopPlaying();
                    updateStatus();
                    return;
                }
            }
        });

        mBtnRecognize = (Button) findViewById(R.id.btnRecognize);
        mBtnRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mStatus == STATUS_READY) {

                    mStatus = STATUS_RECOGNIZING;

                    mTranscoder.transcode(mUser3GPFilePath, mUserWaveFilePath, MainActivity.this);

                    updateStatus();
                    return;
                }


            }
        });


    }

    private void updateStatus() {

        switch(mStatus) {
            case STATUS_READY:
                mBtnRecord.setText("Ready");
                mBtnReplay.setText("Replay");
                break;
            case STATUS_RECORDING:
                mBtnRecord.setText("Recording...");
                break;
            case STATUS_REPLAYING:
                mBtnReplay.setText("Replaying...");
                break;
            case STATUS_RECOGNIZING:
                mBtnReplay.setText("Recognizing...");
                break;
            default:
        }

    }

    @Override
    // Implement AudioPlayer.AudioPlayingListener
    // Handle automatical stop (end of sound file)
    public void onAudioPlayingStopped() {
        mStatus = STATUS_READY;
        updateStatus();
    }

    @Override
    public void onTranscodingSuccess() {

        Log.d(TAG, "onTranscodingSuccess()");

        mStatus = STATUS_READY;
        updateStatus();
    }

    @Override
    // TODO: need different status
    public void onTranscodingFailure() {

        Log.d(TAG, "onTranscodingFailure()");

        mStatus = STATUS_READY;
        updateStatus();
    }
}
