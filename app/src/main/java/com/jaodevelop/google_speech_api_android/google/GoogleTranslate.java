package com.jaodevelop.google_speech_api_android.google;

/**
 * Created by jao on 9/18/16.
 */

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.jaodevelop.google_speech_api_android.http.InsecureOkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Note: currently the translate api key is stored on the server side (because translation is paid-service), this module
 * does not directly connect to Google translate API. Instead, it connects to the custom web server and obtains the result
 * from the same web server.
  */


public class GoogleTranslate {

    final static String TAG="GoogleTranslate";

    String mGoogleTranslateRootURL = null;

    public static String TRANSLATE_LANGUAGE_CODE_ENGLISH = "en";
    public static String TRANSLATE_LANGUAGE_CODE_CHINESE_SIMPLIFIED = "zh-CN";
    public static String TRANSLATE_LANTUAGE_CODE_JAPANESE = "ja";

    public interface GoogleTranslateListener {

        void onGoogleTranslateSuccess(TranslateResult result);
        void onGoogleTranslateFailure();

    }

    public GoogleTranslate(String googleTranslateRootURL) {

        // https://www.googleapis.com/language/translate/v2
        this.mGoogleTranslateRootURL = googleTranslateRootURL;

    }

    // language = TRANSLATE_LANGUAGE_CODE_*
    public void sendTranslateRequest(String[] strings, String sourceLangCode, String targetLangCode, GoogleTranslateListener listener) {

        GoogleTranslateRequest req = null;

        try {

            req = new GoogleTranslateRequest(new URL(mGoogleTranslateRootURL), sourceLangCode, targetLangCode, strings);

        } catch (MalformedURLException e) {
            listener.onGoogleTranslateFailure();
            e.printStackTrace();
            return;
        }

        if(req != null) {
            GoogleTranslateRequestTask task = new GoogleTranslateRequestTask(listener);
            task.execute(req);
        }

    }

    public class TranslateResult {

        private String[] mResults;

        TranslateResult(JSONArray results) {

            mResults = new String[results.length()];

            for(int i = 0; i < results.length(); i++) {

                try {
                    JSONObject alt = results.getJSONObject(i);

                    String translatedText = alt.optString("translatedText", "N/A");

                    mResults[i] = translatedText;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        } // TranslateResult(JSONArray results)

        public String[] getResults() {
            return mResults;
        }

    }

    private class GoogleTranslateRequest {

        private URL mURL;

        private String mSourceLang;

        private String mTargetLang;

        private ArrayList<String> mStrings;

        GoogleTranslateRequest(URL url, String sourceLanguage, String targetLanguage, String[] strings) {
            mURL = url;
            mSourceLang = sourceLanguage;
            mTargetLang = targetLanguage;

            mStrings = new ArrayList<String>();

            for(int i = 0; i < strings.length; i++) {
                mStrings.add(strings[i]);
            }

        }

        URL getURL() {
            return mURL;
        }

        String getSourceLang() {
            return mSourceLang;
        }

        String getTargetLang() {
            return mTargetLang;
        }

        ArrayList<String> getStrings() {
            return mStrings;
        }

    }

    private class GoogleTranslateRequestTask extends AsyncTask<GoogleTranslateRequest, String, Void> {

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        final String STATUS_OK = "STATUS_OK";
        final String STATUS_ERROR = "STATUS_ERROR";

        GoogleTranslateListener mListener;

        GoogleTranslateRequestTask(GoogleTranslateListener listener) {
            mListener = listener;
        }

        @Override
        protected Void doInBackground(GoogleTranslateRequest... reqs) {

            GoogleTranslateRequest req = reqs[0];

            OkHttpClient client = InsecureOkHttpClient.getInsecureOkHttpClient();

            JSONObject bodyObj = new JSONObject();

            try {

                bodyObj.put("sourceLang", req.getSourceLang());
                bodyObj.put("targetLang", req.getTargetLang());

                JSONArray stringArray = new JSONArray(req.getStrings());
                bodyObj.put("strings", stringArray);


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

                    String jsonString = response.body().string();

                    // byte[] jsonBytes = response.body().bytes();

                    // String jsonString = new String(jsonBytes,"UTF-8");

                    publishProgress(STATUS_OK, jsonString);

                } catch (IOException e) {
                    e.printStackTrace();
                    publishProgress(STATUS_ERROR);
                }

            }

            return null;

        } // protected Void doInBackground(GoogleTranslateRequest... reqs)

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if(values[0].equals(STATUS_OK)) {

                String jsonString = values[1];

                JSONObject jsonObject = null;
                TranslateResult translateResult = null;

                try {

                    jsonObject = new JSONObject(jsonString);

                    Log.d(TAG, "jsonString: " + jsonString);

                    JSONArray results = jsonObject.getJSONArray("translations");

                    translateResult = new TranslateResult(results);

                    mListener.onGoogleTranslateSuccess(translateResult);

                    return;

                } catch (JSONException e) {
                    mListener.onGoogleTranslateFailure();
                    e.printStackTrace();
                }

                return;
            }

            mListener.onGoogleTranslateFailure();

        }

    }


}
