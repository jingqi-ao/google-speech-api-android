package com.jaodevelop.google_speech_api_android.http;

import java.util.Collections;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

/**
 * Created by jao on 8/29/16.
 */
public class SecureOkHttpClient {

    static OkHttpClient mClient = null;

    public static OkHttpClient getSecureOkHttpClient() {

        if(mClient == null) {

            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                    .build();

            mClient = new OkHttpClient.Builder()
                    .connectionSpecs(Collections.singletonList(spec))
                    .build();

        }

        return mClient;

    }

}
