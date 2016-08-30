package com.jaodevelop.google_speech_api_android.google;

import android.os.AsyncTask;
import android.util.Log;

import com.jaodevelop.google_speech_api_android.http.InsecureOkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jao on 8/28/16.
 */
public class GoogleAuth {

    private final String TAG = "GoogleAuth";

    private String mGoogleTokenURL;

    private AccessToken mAccessToken;

    public interface GoogleAuthListener {
        void onGoogleAuthSuccess();
        void onGoogleAuthFailure();
    }

    public GoogleAuth(String googleTokenURL) {

        this.mGoogleTokenURL = googleTokenURL;

    }

    public class AccessToken {

        String accessToken;
        String type = "Bearer";
        long expirationTime;

        AccessToken(String accessToken, int ttlInSeconds) {

            this.accessToken = accessToken;

            this.expirationTime = System.currentTimeMillis() + (long) ttlInSeconds;

        }

    }

    public void obtainAccessToken(GoogleAuthListener listener) {

        GoogleAuthRequest req = null;
        try {
            req = new GoogleAuthRequest(new URL(mGoogleTokenURL));
        } catch (MalformedURLException e) {
            listener.onGoogleAuthFailure();
            e.printStackTrace();
            return;
        }

        if(req != null) {
            GoogleAuthRequestTask task = new GoogleAuthRequestTask(listener);
            task.execute(req);
        }


    }

    private class GoogleAuthRequest {

        private URL mURL;

        GoogleAuthRequest(URL url) {
            mURL = url;
        }

        URL getURL() {
            return mURL;
        }


    }

    private class GoogleAuthRequestTask extends AsyncTask<GoogleAuthRequest, String, Void> {

        final String STATUS_ERROR = "STATUS_ERROR";
        final String STATUS_OK = "STATUS_OK";

        GoogleAuthListener mListener;

        GoogleAuthRequestTask(GoogleAuthListener listener) {
            mListener = listener;
        }

        @Override
        protected Void doInBackground(GoogleAuthRequest... reqs) {

            OkHttpClient client = InsecureOkHttpClient.getInsecureOkHttpClient();

            GoogleAuthRequest req = reqs[0];

            Request request = new Request.Builder()
                    .url(req.getURL())
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress(STATUS_ERROR);
                return null;
            }

            if(response != null) {
                try {

                    //Log.d(LOG_TAG, "Reponse is: " + response.body().string());
                    // Log.d(TAG, "Reponse is:  " + response.body().string());

                    String jsonString = response.body().string();

                    Log.d(TAG, "jsonString is:  " + jsonString);

                    publishProgress(STATUS_OK, jsonString);


                } catch (IOException e) {
                    e.printStackTrace();
                    publishProgress(STATUS_ERROR);
                    return null;
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
                    String token = jsonObject.getString("access_token");
                    Integer ttl = jsonObject.getInt("expires_in");

                    mAccessToken = new AccessToken(token, ttl);

                    mListener.onGoogleAuthSuccess();

                    return;

                } catch (JSONException e) {
                    mListener.onGoogleAuthFailure();
                    e.printStackTrace();
                }

                return;
            }

            mListener.onGoogleAuthFailure();

        }



    }


}
