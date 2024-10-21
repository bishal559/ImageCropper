package com.bishal.imagecropper.callback;

import android.graphics.RectF;
/**
 * Author: Bishal Adhikari
 *
 * @Date: 20/10/2024
 *
 * Created by Oleksii Shliama.
 */
public interface OverlayViewChangeListener {

    void onCropRectUpdated(RectF cropRect);

}