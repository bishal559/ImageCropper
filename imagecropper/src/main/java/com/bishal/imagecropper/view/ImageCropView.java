package com.bishal.imagecropper.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.bishal.imagecropper.R;
import com.bishal.imagecropper.callback.CropBoundsChangeListener;
import com.bishal.imagecropper.callback.OverlayViewChangeListener;

import androidx.annotation.NonNull;
/**
 * Author: Bishal Adhikari
 *
 * @Date: 20/10/2024
 */
public class ImageCropView extends FrameLayout {

    private GestureCropImageView mGestureCropImageView;
    private final OverlayView mViewOverlay;

    public ImageCropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.image_crop_view, this, true);
        mGestureCropImageView = findViewById(R.id.image_view_crop);
        mViewOverlay = findViewById(R.id.view_overlay);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.image_crop_ImageCropView);
        mViewOverlay.processStyledAttributes(a);
        mGestureCropImageView.processStyledAttributes(a);
        a.recycle();


        setListenersToViews();
    }

    private void setListenersToViews() {
        mGestureCropImageView.setCropBoundsChangeListener(new CropBoundsChangeListener() {
            @Override
            public void onCropAspectRatioChanged(float cropRatio) {
                mViewOverlay.setTargetAspectRatio(cropRatio);
            }
        });
        mViewOverlay.setOverlayViewChangeListener(new OverlayViewChangeListener() {
            @Override
            public void onCropRectUpdated(RectF cropRect) {
                mGestureCropImageView.setCropRect(cropRect);
            }
        });
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @NonNull
    public GestureCropImageView getCropImageView() {
        return mGestureCropImageView;
    }

    @NonNull
    public OverlayView getOverlayView() {
        return mViewOverlay;
    }

    /**
     * Method for reset state for ImageCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate ImageCropImageView instance and reattach it to layout.
     */
    public void resetCropImageView() {
        removeView(mGestureCropImageView);
        mGestureCropImageView = new GestureCropImageView(getContext());
        setListenersToViews();
        mGestureCropImageView.setCropRect(getOverlayView().getCropViewRect());
        addView(mGestureCropImageView, 0);
    }
}