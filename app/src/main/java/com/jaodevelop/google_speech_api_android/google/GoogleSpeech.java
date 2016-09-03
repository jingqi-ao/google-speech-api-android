package com.jaodevelop.google_speech_api_android.google;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.jaodevelop.google_speech_api_android.http.SecureOkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.OkHeaders;

/**
 * Created by jao on 8/29/16.
 */
public class GoogleSpeech {

    final static String TAG="GoogleSpeech";

    String mGoogleSpeechRootURL;

    String mGoogleSpeechSyncRecognizeURL;


    public interface GoogleSpeechListener {
        void onGoogleSpeechSuccess();
        void onGoogleSpeechFailure();
    }

    public GoogleSpeech(String googleSpeechRootURL) {

        this.mGoogleSpeechRootURL = googleSpeechRootURL;

        this.mGoogleSpeechSyncRecognizeURL = mGoogleSpeechRootURL + "/speech:syncrecognize";

    }

    public void sendSyncRecognizeRequest(String accessToken, String audioFileName, GoogleSpeechListener listener) {

        SyncRecognizeRequest req = null;

        InputStream inputStream = null;//You can get an inputStream using any IO API
        try {
            inputStream = new FileInputStream(audioFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] audioBytes;

        byte[] buffer = new byte[8192];

        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        audioBytes = Base64.encode(output.toByteArray(), Base64.NO_WRAP);

        String audioString = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP);

        Log.d(TAG, "audioString.length" + audioString.length());
        Log.d(TAG, "audioString.getBytes().length" + audioString.getBytes().length);

        try {
            req = new SyncRecognizeRequest(new URL(mGoogleSpeechSyncRecognizeURL), accessToken, audioBytes, audioString);
        } catch (MalformedURLException e) {
            listener.onGoogleSpeechFailure();
            e.printStackTrace();
            return;
        }

        if(req != null) {
            SyncRecognizeRequestTask task = new SyncRecognizeRequestTask(listener);
            task.execute(req);
        }

    }

    private class SyncRecognizeRequest {

        private URL mURL;
        private String mAccessToken;

        private byte[] mAudio;

        private String mAudioString;

        SyncRecognizeRequest(URL url, String accessToken, byte[] audio, String audioString) {
            mURL = url;
            mAccessToken = accessToken;
            mAudio = audio;
            mAudioString = audioString;
        }

        public URL getURL() {
            return mURL;
        }

        public String getAccessToken() {
            return mAccessToken;
        }

        public byte[] getAudioBytes() {
            return mAudio;
        }

        public String getAudioString() {
            return mAudioString;
        }

    }

    private class SyncRecognizeRequestTask extends AsyncTask<SyncRecognizeRequest, String, Void> {

        OkHttpClient client = SecureOkHttpClient.getSecureOkHttpClient();

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        final String STATUS_OK = "STATUS_OK";
        final String STATUS_ERROR = "STATUS_ERROR";

        GoogleSpeechListener mListener;

        SyncRecognizeRequestTask(GoogleSpeechListener listener) {
            mListener = listener;
        }

        @Override
        protected Void doInBackground(SyncRecognizeRequest... reqs) {


            SyncRecognizeRequest req = reqs[0];

            // Temporarily create JSON here
            /*
            JSONObject configObj = new JSONObject();
            try {
                configObj.put("encoding", "FLAC");
                //configObj.put("sample_rate", "16000");
                configObj.put("sampleRate", "16000");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject audioObj = new JSONObject();
            try {
                audioObj.put("uri", "gs://cloud-samples-tests/speech/brooklyn.flac");
            } catch (JSONException e) {
                e.printStackTrace();
            }


            */

            JSONObject configObj = new JSONObject();
            try {
                configObj.put("encoding", "LINEAR16");
                configObj.put("sampleRate", "16000");
                configObj.put("languageCode", "en-US");
                configObj.put("maxAlternatives", "5");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject audioObj = new JSONObject();
            try {
                //audioObj.put("content", req.getAudioBytes());
                audioObj.put("content", req.getAudioString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject bodyObj = new JSONObject();
            try {
                bodyObj.put("config", configObj);
                bodyObj.put("audio", audioObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String bodyString = bodyObj.toString();

            RequestBody body = RequestBody.create(JSON, bodyString);

            Request request = new Request.Builder()
                    .url(req.getURL())
                    .header("Authorization", "Bearer " + req.getAccessToken())
                    .addHeader("Content-type", "application/json")
                    .post(body)
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress(STATUS_ERROR);
            }

            if(response != null) {
                try {

                    String jsonString = response.body().string();

                    publishProgress(STATUS_OK, jsonString);

                } catch (IOException e) {
                    e.printStackTrace();
                    publishProgress(STATUS_ERROR);
                }

            }

            return null;

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if(values[0].equals(STATUS_OK)) {

                String jsonString = values[1];

                JSONObject jsonObject = null;
                try {

                    jsonObject = new JSONObject(jsonString);

                    Log.d(TAG, "jsonString: " + jsonString);

                    mListener.onGoogleSpeechSuccess();

                    return;

                } catch (JSONException e) {
                    mListener.onGoogleSpeechFailure();
                    e.printStackTrace();
                }

                return;
            }

            mListener.onGoogleSpeechFailure();



        }
    }



}
