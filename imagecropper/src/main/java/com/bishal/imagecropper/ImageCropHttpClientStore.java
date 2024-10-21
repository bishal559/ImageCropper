package com.bishal.imagecropper;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
/**
 * Author: Bishal Adhikari
 *
 * @Date: 20/10/2024
 */

public class ImageCropHttpClientStore {

    private ImageCropHttpClientStore() {}

    public final static ImageCropHttpClientStore INSTANCE = new ImageCropHttpClientStore();

    private OkHttpClient client;

    @NonNull
    public OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient();
        }
        return client;
    }

    public void setClient(@NonNull OkHttpClient client) {
        this.client = client;
    }
}
