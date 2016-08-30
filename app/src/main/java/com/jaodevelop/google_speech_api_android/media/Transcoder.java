package com.jaodevelop.google_speech_api_android.media;

import android.os.AsyncTask;
import android.util.Log;

import com.jaodevelop.google_speech_api_android.http.InsecureOkHttpClient;

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
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by jao on 8/28/16.
 */
public class Transcoder {

    private final String TAG = "Transcoder";

    private String mTranscodingServerURL;

    public interface TranscodingListener {
        void onTranscodingSuccess();
        void onTranscodingFailure();
    }

    public Transcoder(String transcodingServerURL) {

        this.mTranscodingServerURL = transcodingServerURL;

    }

    public int transcode(String threeGPPFilePath, String waveFilePath, TranscodingListener listener) {

        TranscoderRequest req = null;
        try {
            req = new TranscoderRequest(new URL(mTranscodingServerURL), threeGPPFilePath, waveFilePath, listener);
        } catch (MalformedURLException e) {
            listener.onTranscodingFailure();
            e.printStackTrace();
        }

        if(req != null) {
            TranscoderRequestTask task = new TranscoderRequestTask(listener);
            task.execute(req);
        }

        return 0;
    }

    private class TranscoderRequest {

        private URL url;
        private String inputFilePath;
        private String outputFilePath;
        private TranscodingListener listener;

        TranscoderRequest(URL url, String inputFilePath, String outputFilePath, TranscodingListener listener) {
            this.url = url;
            this.inputFilePath = inputFilePath;
            this.outputFilePath = outputFilePath;
            this.listener = listener;
        }

        public URL getURL() {
            return this.url;
        }

        public String getInputFilePath() {
            return this.inputFilePath;
        }

        public String getOutputFilePath() {
            return this.outputFilePath;
        }

        public TranscodingListener getListner() {
            return this.listener;
        }
    }


    // HTTP Request task
    private class TranscoderRequestTask extends AsyncTask<TranscoderRequest, String, Long> {

        TranscodingListener mListener;

        //OkHttpClient client = new OkHttpClient();
        OkHttpClient client = InsecureOkHttpClient.getInsecureOkHttpClient();

        private final String STATUS_OK = "STATUS_OK";
        private final String STATUS_ERROR = "STATUS_ERROR";

        TranscoderRequestTask(TranscodingListener listener) {
            mListener = listener;
        }

        @Override
        protected Long doInBackground(TranscoderRequest... reqs) {

            TranscoderRequest req = reqs[0];

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("audio", "input.3pg",
                            RequestBody.create(MediaType.parse("audo"), new File(req.getInputFilePath())))
                    .build();

            Request request = new Request.Builder()
                    .url(req.getURL())
                    .post(requestBody)
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

                    publishProgress(STATUS_OK);

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

            if(values[0].equals(STATUS_ERROR)) {

                mListener.onTranscodingFailure();
                return;
            }

            mListener.onTranscodingSuccess();

        }

    }



}
