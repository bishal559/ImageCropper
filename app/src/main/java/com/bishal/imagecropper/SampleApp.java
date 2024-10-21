package com.bishal.imagecropper;

import android.app.Application;


import java.util.Collections;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

/**
 * Author: Bishal Adhikari
 *
 * @Date: 20/10/2024
 */
public class SampleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setImageCropHttpClient();
    }

    private void setImageCropHttpClient() {
        ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .allEnabledCipherSuites()
            .allEnabledTlsVersions()
            .build();

        OkHttpClient client = new OkHttpClient.Builder()
            .connectionSpecs(Collections.singletonList(cs))
            .build();

        ImageCropHttpClientStore.INSTANCE.setClient(client);
    }
}
