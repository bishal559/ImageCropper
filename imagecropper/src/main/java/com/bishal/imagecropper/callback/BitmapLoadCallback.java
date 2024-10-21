package com.bishal.imagecropper.callback;

import android.graphics.Bitmap;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bishal.imagecropper.model.ExifInfo;

/**
 * Author: Bishal Adhikari
 *
 * @Date: 20/10/2024
 */

public interface BitmapLoadCallback {

    void onBitmapLoaded(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo, @NonNull String imageInputPath, @Nullable String imageOutputPath);

    void onFailure(@NonNull Exception bitmapWorkerException);

}