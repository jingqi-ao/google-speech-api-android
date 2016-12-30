package com.jaodevelop.google_speech_api_android.google;

import android.os.AsyncTask;
import android.util.Log;

import com.jaodevelop.google_speech_api_android.http.InsecureOkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jao on 12/28/16.
 */

public class AWSPolly {
    private final String TAG = "AWSPolly";

    public static String AWS_POLLY_LANGUAGE_ENGLISH_US = "English US";
    public static String AWS_POLLY_LANGUAGE_JAPANESE = "Japanese";

    public interface AWSPollyListener {

        void onAWSPollySuccess(AWSPollyResult result);
        void onAWSPollyFailure();

    }

    private String mAWSPollyRootURL = null;

    public AWSPolly(String awsPollyRootURL) {

        this.mAWSPollyRootURL = awsPollyRootURL;

    }

    // language = AWS_POLLY_LANGUAGE*
    public void sendSynthesizeRequest(String string, String language, String outputFilePath, AWSPollyListener listener) {

        AWSPollyRequest req = null;

        try {

            req = new AWSPollyRequest(new URL(mAWSPollyRootURL), language, string, outputFilePath);

        } catch (MalformedURLException e) {
            listener.onAWSPollyFailure();
            e.printStackTrace();
            return;
        }

        if(req != null) {
            GoogleTranslateRequestTask task = new GoogleTranslateRequestTask(listener);
            task.execute(req);
        }

    }

    private class AWSPollyRequest {

        private URL mURL;

        private String mLanguage;

        private String mString;

        private String mOutputFilePath;

        AWSPollyRequest(URL url, String language, String string, String outputFilePath) {

            mURL = url;

            mLanguage = language;

            mString = string;

            mOutputFilePath = outputFilePath;

        }

        URL getURL() {
            return mURL;
        }

        String getLanguage() {
            return mLanguage;
        }

        String getString() {
            return mString;
        }

        String getOutputFilePath() {
            return mOutputFilePath;
        }

    }

    private class GoogleTranslateRequestTask extends AsyncTask<AWSPollyRequest, String, Void> {

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        final String STATUS_OK = "STATUS_OK";
        final String STATUS_ERROR = "STATUS_ERROR";

        AWSPollyListener mListener;

        GoogleTranslateRequestTask(AWSPollyListener listener) {
            mListener = listener;
        }

        @Override
        protected Void doInBackground(AWSPollyRequest... reqs) {

            AWSPollyRequest req = reqs[0];

            OkHttpClient client = InsecureOkHttpClient.getInsecureOkHttpClient();

            JSONObject bodyObj = new JSONObject();

            try {

                bodyObj.put("inputString", req.getString());
                bodyObj.put("language", req.getLanguage());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String bodyString = bodyObj.toString();
            RequestBody body = RequestBody.create(JSON, bodyString);

            Request request = new Request.Builder()
                    .url(req.getURL())
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

                    //Log.d(LOG_TAG, "Reponse is: " + response.body().string());
                    Log.d(TAG, "Reponse is here! ");
                    InputStream responseInputStream = response.body().byteStream();

                    FileOutputStream fileOutputStream = new FileOutputStream(req.getOutputFilePath());

                    byte[] buf = new byte[512];
                    int num = 0;
                    while ((num = responseInputStream.read(buf)) != -1) {
                        fileOutputStream.write(buf, 0, num);
                    }

                    Log.d(TAG, "File Writing is done! ");

                    publishProgress(STATUS_OK, req.getOutputFilePath());

                } catch (IOException e) {
                    e.printStackTrace();
                    publishProgress(STATUS_ERROR, null);
                }

            }

            return null;

        } // protected Void doInBackground(GoogleTranslateRequest... reqs)

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if(values[0].equals(STATUS_OK)) {

                AWSPollyResult awsPollyResult = null;
                awsPollyResult = new AWSPollyResult(AWSPollyResult.STATUS_OK, values[1]);

                mListener.onAWSPollySuccess(awsPollyResult);

                return;
            }

            mListener.onAWSPollyFailure();

        }

    }

    public class AWSPollyResult {

        public static final String STATUS_OK = "STATUS_OK";
        public static final String STATUS_ERROR = "STATUS_ERROR";

        String mStatus;
        String mOutputFilePath;

        AWSPollyResult(String status, String outputFilePath) {
            mStatus = status;
            mOutputFilePath = outputFilePath;
        }

        String getStatus() {
            return mStatus;
        }

        String getOutputFilePath() {
            return mOutputFilePath;
        }

    }


}
