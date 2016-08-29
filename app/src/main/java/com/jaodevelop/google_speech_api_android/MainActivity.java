package com.jaodevelop.google_speech_api_android;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jaodevelop.google_speech_api_android.media.AudioPlayer;
import com.jaodevelop.google_speech_api_android.media.AudioRecorder;

public class MainActivity extends AppCompatActivity implements AudioPlayer.OnStoppedListener {

    private final String STATUS_READY = "Ready";
    private final String STATUS_RECORDING = "Recording";
    private final String STATUS_REPLAYING = "Replaying";

    private String mStatus = STATUS_READY;

    private final String mAudioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/google-speech-api-android-user-sound.3gp";

    // Media
    AudioRecorder mAudioRecoder;
    AudioPlayer mAudioPlayer;

    // UI
    Button mBtnRecord;
    Button mBtnReplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Media
        mAudioRecoder = new AudioRecorder(mAudioFilePath);
        mAudioPlayer = new AudioPlayer(mAudioFilePath);
        mAudioPlayer.setOnStoppoedListener(this);

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
            default:
        }

    }


    @Override
    // Implement AudioPlayer.OnStoppedListener
    // Handle automatical stop (end of sound file)
    public void onStopped() {
        mStatus = STATUS_READY;
        updateStatus();
    }

}
