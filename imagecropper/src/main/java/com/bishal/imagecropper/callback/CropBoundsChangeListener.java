package com.bishal.imagecropper.callback;
/**
 * Author: Bishal Adhikari
 *
 * @Date: 20/10/2024
 *
 * Interface for crop bound change notifying.
 */

public interface CropBoundsChangeListener {

    void onCropAspectRatioChanged(float cropRatio);

}