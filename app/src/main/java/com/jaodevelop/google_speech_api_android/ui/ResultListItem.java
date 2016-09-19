package com.jaodevelop.google_speech_api_android.ui;

/**
 * Created by jao on 9/18/16.
 */
public class ResultListItem {

    private String mRecognizeString;
    private String mTranslateString;

    public void setRecognizeString(String recognizeString) {
        mRecognizeString = recognizeString;
    }

    public void setTranslateString(String translateString) {
        mTranslateString = translateString;
    }

    public String getRecognizeString() {
        return mRecognizeString;
    }

    public String getTranslateString() {
        return mTranslateString;
    }

}
