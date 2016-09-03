package com.jaodevelop.google_speech_api_android;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jaodevelop.google_speech_api_android.google.GoogleAuth;
import com.jaodevelop.google_speech_api_android.google.GoogleSpeech;
import com.jaodevelop.google_speech_api_android.media.AudioPlayer;
import com.jaodevelop.google_speech_api_android.media.AudioRecorder;
import com.jaodevelop.google_speech_api_android.media.Transcoder;

public class MainActivity extends AppCompatActivity implements AudioPlayer.AudioPlayingListener,
        Transcoder.TranscodingListener,
        GoogleAuth.GoogleAuthListener,
        GoogleSpeech.GoogleSpeechListener {

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
    private String mGoogleTokenURL = "https://192.168.0.24:8443/api/v1/accesstoken";

    private String mGoogleSpeechRootURL = "https://speech.googleapis.com/v1beta1";

    // Media
    AudioRecorder mAudioRecoder;
    AudioPlayer mAudioPlayer;

    Transcoder mTranscoder;

    // Google
    GoogleAuth mGoogleAuth;
    GoogleSpeech mGoogleSpeech;

    // UI
    Button mBtnRecord;
    Button mBtnReplay;
    Button mBtnRecognize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Google
        mGoogleAuth = new GoogleAuth(mGoogleTokenURL);
        mGoogleSpeech = new GoogleSpeech(mGoogleSpeechRootURL);

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

                    /*
                    mGoogleSpeech.sendSyncRecognizeRequest(mGoogleAuth.getAccessTokenString(),
                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio.wav",
                            MainActivity.this);
                    */

                    updateStatus();
                    return;
                }


            }
        });




    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume()");

        mGoogleAuth.obtainAccessToken(this);

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

        mGoogleSpeech.sendSyncRecognizeRequest(mGoogleAuth.getAccessTokenString(), mUserWaveFilePath, MainActivity.this);

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

    @Override
    public void onGoogleAuthSuccess() {
        Log.d(TAG, "onGoogleAuthSuccess()");
    }

    @Override
    public void onGoogleAuthFailure() {
        Log.d(TAG, "onGoogleAuthFailure()");
    }

    @Override
    public void onGoogleSpeechSuccess() {
        Log.d(TAG, "onGoogleSpeechSuccess()");
    }

    @Override
    public void onGoogleSpeechFailure() {
        Log.d(TAG, "onGoogleSpeechFailure()");
    }
}
