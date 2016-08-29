package com.jaodevelop.google_speech_api_android.media;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

/**
 * Created by jao on 7/7/16.
 *
 * This is MediaRecorder based implementation. The advantage is it is out-of-box (ready to use immediately).
 * The disadvantage is it does not support recording in .wav format. Alexa Voice Service require the .wav format.
 */
public class AudioRecorder {

    private final String TAG = "AudioRecorder";

    private String mAudioFileFullPath = null;

    private MediaRecorder mMediaRecorder = null;

    // Primary constructor
    public AudioRecorder(String audioFileFullPath) {
        mAudioFileFullPath = audioFileFullPath == null ?
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/audioFromAudioRecorder.3gp" : audioFileFullPath;
    }

    public AudioRecorder() {
        this(null);
    }

    public void startRecord(String audioFileFullPath) {

        mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        // Alexa Voice Service requirements: 16bit Linear PCM, 16kHz sample rate, Single channel, Little endian byte order
        // https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/reference/speechrecognizer
        mMediaRecorder.setAudioEncodingBitRate(16);
        mMediaRecorder.setAudioSamplingRate(16000);
        mMediaRecorder.setAudioChannels(1);


        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        String filePath = audioFileFullPath == null ? mAudioFileFullPath : audioFileFullPath;
        mMediaRecorder.setOutputFile(filePath);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "startRecord() failed");
        }

        mMediaRecorder.start();

    }

    public void startRecord() {
        this.startRecord(null);
    }

    public void stopRecord() {

        if(mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }

        Log.d(TAG, "stopRecord()");

    }

}
