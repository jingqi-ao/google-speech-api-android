package com.jaodevelop.google_speech_api_android;

import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jaodevelop.google_speech_api_android.google.GoogleAuth;
import com.jaodevelop.google_speech_api_android.google.GoogleSpeech;
import com.jaodevelop.google_speech_api_android.google.GoogleTranslate;
import com.jaodevelop.google_speech_api_android.media.AudioPlayer;
import com.jaodevelop.google_speech_api_android.media.AudioRecorder;
import com.jaodevelop.google_speech_api_android.media.Transcoder;
import com.jaodevelop.google_speech_api_android.ui.ResultArrayAdapter;
import com.jaodevelop.google_speech_api_android.ui.ResultListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AudioPlayer.AudioPlayingListener,
        Transcoder.TranscodingListener,
        GoogleAuth.GoogleAuthListener,
        GoogleSpeech.GoogleSpeechListener,
        GoogleTranslate.GoogleTranslateListener {

    private final String TAG = "MainActivity";

    private final String STATUS_READY = "Ready";
    private final String STATUS_RECORDING = "Recording";
    private final String STATUS_REPLAYING = "Replaying";
    private final String STATUS_RECOGNIZING = "Recognizing";

    private final String STATUS_SUCCESS_RECOGNIZE = "Speech recognition succeeded";
    private final String STATUS_ERROR_FAIL_TO_GET_GOOGLE_ACCESS_TOKEN = "Fail to get Google access token";

    private String mStatus = STATUS_READY;

    private final String mUser3GPFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/google-speech-api-android-user-sound.3gp";
    private final String mUserWaveFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/google-sppech-api-android-user-sound.wav";


    private String mTranscodingServerURL = "https://192.168.0.24:9443/api/v1/transcode";
    private String mGoogleTokenURL = "https://192.168.0.24:8443/api/v1/accesstoken";

    private String mGoogleSpeechRootURL = "https://speech.googleapis.com/v1beta1";

    // private String mGoogleTranslateRootURL = "https://www.googleapis.com/language/translate/v2";
    private String mGoogleTranslateRootURL = "https://192.168.0.24:8443/api/v1/translate";

    // Media
    AudioRecorder mAudioRecoder;
    AudioPlayer mAudioPlayer;

    Transcoder mTranscoder;

    // Google
    GoogleAuth mGoogleAuth;
    GoogleSpeech mGoogleSpeech;
    GoogleTranslate mGoogleTranslate;

    String[] mLanguages;

    // ArrayList<GoogleSpeech.SynRecognizeAlternative> mSyncRecognizeAlternativeArray;

    ArrayList<ResultListItem> mResultList;

    // Language
    final String LANGUAGE_MADARIN = "\u666e\u901a\u8bdd\u0020\u004d\u0061\u006e\u0064\u0061\u0072\u0069\u006e";
    final String LANGUAGE_ENGLISH = "\u0045\u006e\u0067\u006c\u0069\u0073\u0068";



    ArrayList<String> LANGUAGES = new ArrayList<String>();

    //        (LANGUAGE_ENGLISH);
    //{LANGUAGE_ENGLISH, LANGUAGE_MADARIN};

    String mLanguage = LANGUAGE_ENGLISH; // from LANGUAGE_*

    // Translate language
    final String LANGUAGE_NULL = "\u004e\u006f\u006e\u0065";

    String mTranslateSourceLanguage = LANGUAGE_ENGLISH;
    String mTranslateTargetLanguage = LANGUAGE_NULL;

    ArrayList<String> TRANSLATE_TARGET_LANGUAGES = new ArrayList<String>();

    // UI
    Button mBtnRecord;
    Button mBtnReplay;
    Button mBtnRecognize;

    ListView mLVResult;
    ResultArrayAdapter mResultArrayAdapter;

    Spinner mSpinLanguage;
    Spinner mSpinTranslateTargetLanguage;

    TextView mTVStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Google
        mGoogleAuth = new GoogleAuth(mGoogleTokenURL);
        mGoogleSpeech = new GoogleSpeech(mGoogleSpeechRootURL);
        mGoogleTranslate = new GoogleTranslate(mGoogleTranslateRootURL);


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

                if(mStatus == STATUS_READY || mStatus == STATUS_SUCCESS_RECOGNIZE) {
                    mStatus = STATUS_RECORDING;
                    mAudioRecoder.startRecord();
                    updateStatus();
                    return;
                }

                if(mStatus == STATUS_RECORDING) {

                    //mStatus = STATUS_READY;
                    mAudioRecoder.stopRecord();

                    // Recognize flow:
                    // Speak -> Stop -> Transcode -> Google Speech Sync Recog -> Result (success or failure)

                    mStatus = STATUS_RECOGNIZING;
                    updateStatus();

                    mTranscoder.transcode(mUser3GPFilePath, mUserWaveFilePath, MainActivity.this);

                    return;
                }

            }
        });

        /*
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
                    updateStatus();

                    mTranscoder.transcode(mUser3GPFilePath, mUserWaveFilePath, MainActivity.this);




                }

            }
        });
                */


        //mSyncRecognizeAlternativeArray = new ArrayList<GoogleSpeech.SynRecognizeAlternative>();
        //mResultArrayAdapter = new ResultArrayAdapter(this, mSyncRecognizeAlternativeArray);

        mResultList = new ArrayList<ResultListItem>();
        mResultArrayAdapter = new ResultArrayAdapter(this, mResultList);

        mLVResult = (ListView) findViewById(R.id.lvResult);
        mLVResult.setAdapter(mResultArrayAdapter);

        // Speech recognize spinner
        LANGUAGES.add(LANGUAGE_ENGLISH);
        LANGUAGES.add(LANGUAGE_MADARIN);

        mSpinLanguage = (Spinner) findViewById(R.id.spinLanguage);
        ArrayAdapter<String> spinLanguageAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, LANGUAGES);
        spinLanguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinLanguage.setAdapter(spinLanguageAdapter);
        mSpinLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // Update Google recognition recognition language
                Log.d(TAG, "old mLanguage is:" + mLanguage);
                mLanguage = LANGUAGES.get(i);
                Log.d(TAG, "new mLanguage is:" + mLanguage);

                // Update Google translate source language
                mTranslateSourceLanguage = LANGUAGES.get(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Translate spinner
        TRANSLATE_TARGET_LANGUAGES.add(LANGUAGE_NULL);
        TRANSLATE_TARGET_LANGUAGES.add(LANGUAGE_ENGLISH);
        TRANSLATE_TARGET_LANGUAGES.add(LANGUAGE_MADARIN);

        mSpinTranslateTargetLanguage = (Spinner) findViewById(R.id.spinTranslateTargetLanguage);
        ArrayAdapter<String> spinTranslateTargetLanguageAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, TRANSLATE_TARGET_LANGUAGES);
        spinTranslateTargetLanguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinTranslateTargetLanguage.setAdapter(spinTranslateTargetLanguageAdapter);
        mSpinTranslateTargetLanguage.setSelection(0, false); // Avoid the trigger in onCreate()
        mSpinTranslateTargetLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // Update Google recognition recognition language
                Log.d(TAG, "old mTranslateTargetLanguage is:" + mTranslateTargetLanguage);
                mTranslateTargetLanguage = TRANSLATE_TARGET_LANGUAGES.get(i);
                Log.d(TAG, "new mTranslateTargetLanguage is:" + mTranslateTargetLanguage);

                // Update Google translate source language
                mTranslateTargetLanguage = TRANSLATE_TARGET_LANGUAGES.get(i);

                translate();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        mTVStatus = (TextView) findViewById(R.id.tvStatus);

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
                mBtnRecord.setText("Speak");
                //mBtnReplay.setText("Replay");
                //mBtnRecognize.setText("Recognize");
                mTVStatus.setText(STATUS_READY);
                mTVStatus.setTextColor(Color.argb(255, 0, 0, 0));
                break;
            case STATUS_RECORDING:
                mBtnRecord.setText("Stop");
                mTVStatus.setText(STATUS_RECORDING);
                mTVStatus.setTextColor(Color.argb(255, 0, 0, 102));
                break;
            case STATUS_REPLAYING:
                mBtnReplay.setText("Replaying...");
                break;
            case STATUS_RECOGNIZING:
                // mBtnRecognize.setText("Recognizing...");
                mBtnRecord.setText("Speak");
                mTVStatus.setText(STATUS_RECOGNIZING);
                mTVStatus.setTextColor(Color.argb(255, 0, 0, 102));
                break;
            case STATUS_ERROR_FAIL_TO_GET_GOOGLE_ACCESS_TOKEN:
                mTVStatus.setText(STATUS_ERROR_FAIL_TO_GET_GOOGLE_ACCESS_TOKEN);
                mTVStatus.setTextColor(Color.argb(255, 255, 0, 0));
                break;
            case STATUS_SUCCESS_RECOGNIZE:
                mBtnRecord.setText("Speak");
                mTVStatus.setText(STATUS_SUCCESS_RECOGNIZE);
                mTVStatus.setTextColor(Color.argb(255, 0, 255, 0));
                break;
            default:
        }

    }

    private String getLanguageCode(String language) {

        String languageCode = null;

        switch(language) {
            case LANGUAGE_MADARIN:
                languageCode = GoogleSpeech.LANG_CODE_MANDARIN;
                break;
            default:
                languageCode = GoogleSpeech.LANG_CODE_ENGLISH_US;
        }

        return languageCode;
    }

    private String getTranslateLangugaeCode(String language) {

        String languageCode = null;

        switch(language) {
            case LANGUAGE_MADARIN:
                languageCode = GoogleTranslate.TRANSLATE_LANGUAGE_CODE_CHINESE_SIMPLIFIED;
                break;
            default:
                languageCode = GoogleTranslate.TRANSLATE_LANGUAGE_CODE_ENGLISH;
        }

        return languageCode;

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

        mGoogleSpeech.sendSyncRecognizeRequest(mGoogleAuth.getAccessTokenString(), mUserWaveFilePath, getLanguageCode(mLanguage), MainActivity.this);

        // mStatus = STATUS_READY;
        // updateStatus();
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
        mStatus = STATUS_READY;
        updateStatus();
    }

    @Override
    public void onGoogleAuthFailure() {
        Log.d(TAG, "onGoogleAuthFailure()");
        mStatus = STATUS_ERROR_FAIL_TO_GET_GOOGLE_ACCESS_TOKEN;
        updateStatus();
    }

    @Override
    public void onGoogleSpeechSuccess(GoogleSpeech.SyncRecognizeResult data) {


        GoogleSpeech.SynRecognizeAlternative[] alternatives = data.getAlternatives();

        int oldLength = mResultList.size();
        for(int i = 0; i < oldLength; i++) {
            mResultList.remove(0);
        }

        int newLength = alternatives.length;
        for(int i = 0; i < newLength; i++) {

            ResultListItem item = new ResultListItem();
            item.setRecognizeString(alternatives[i].getTranscript());

            mResultList.add(item);
        }

        if(mTranslateTargetLanguage != LANGUAGE_NULL) {
            translate();
        }

        mResultArrayAdapter.notifyDataSetChanged();

        Log.d(TAG, "onGoogleSpeechSuccess()");

        mStatus = STATUS_SUCCESS_RECOGNIZE;
        updateStatus();

    }

    @Override
    public void onGoogleSpeechFailure() {
        Log.d(TAG, "onGoogleSpeechFailure()");
    }

    // Update translate
    // Translation will be triggered:
    //  (1) Change the target language
    //  (2) New voice recognizeion is done

    public void translate() {

        Log.d(TAG, "translate()");

        if(mTranslateTargetLanguage.equals(LANGUAGE_NULL)) {
            // Remove all translated string
            for(int i = 0; i < mResultList.size(); i++) {
                mResultList.get(i).setTranslateString("");
            }

            mResultArrayAdapter.notifyDataSetChanged();

            return;
        }

        String[] strings = new String[mResultList.size()];

        for(int i = 0; i < strings.length; i++) {
            strings[i] = mResultList.get(i).getRecognizeString();
        }

        mGoogleTranslate.sendTranslateRequest(strings, getTranslateLangugaeCode(mTranslateSourceLanguage), getTranslateLangugaeCode(mTranslateTargetLanguage), MainActivity.this);

    }

    public void onGoogleTranslateSuccess(GoogleTranslate.TranslateResult result) {

        Log.d(TAG, "onGoogleTranslateSuccess()");

        String[] results = result.getResults();

        for(int i = 0; i < results.length; i++) {
            ResultListItem item = mResultList.get(i);
            item.setTranslateString(results[i]);
        }

        mResultArrayAdapter.notifyDataSetChanged();

    }

    public void onGoogleTranslateFailure() {

    }


}
