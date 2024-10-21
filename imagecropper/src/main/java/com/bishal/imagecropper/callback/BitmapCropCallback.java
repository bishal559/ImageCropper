package com.bishal.imagecropper.callback;

import android.net.Uri;

import androidx.annotation.NonNull;
/**
 * Author: Bishal Adhikari
 *
 * @Date: 20/10/2024
 */

public interface BitmapCropCallback {

    void onBitmapCropped(@NonNull Uri resultUri, int offsetX, int offsetY, int imageWidth, int imageHeight);

    void onCropFailure(@NonNull Throwable t);

}