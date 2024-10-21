package com.bishal.imagecropper;
/**
 * Author: Bishal Adhikari
 *
 * @Date: 20/10/2024
 */
public interface ImageCropFragmentCallback {

    /**
     * Return loader status
     * @param showLoader
     */
    void loadingProgress(boolean showLoader);

    /**
     * Return cropping result or error
     * @param result
     */
    void onCropFinish(ImageCropFragment.ImageCropResult result);

}
