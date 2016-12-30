package com.jaodevelop.google_speech_api_android.ui;

/**
 * Created by jao on 9/18/16.
 */
public class ResultListItem {

    private String mRecognizeString;

    private String mTranslateString;

    private boolean mIsAWSPollySupported;

    public void setRecognizeString(String recognizeString) {
        mRecognizeString = recognizeString;
    }
    public String getRecognizeString() {
        return mRecognizeString;
    }

    public void setTranslateString(String translateString) {
        mTranslateString = translateString;
    }
    public String getTranslateString() {
        return mTranslateString;
    }

    public void setIsAWSPollySupported(boolean isAWSPollySupported) {
        mIsAWSPollySupported = isAWSPollySupported;
    }

    public boolean getIsAWSPollySupported() {
        return mIsAWSPollySupported;
    }

}
