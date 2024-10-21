package com.bishal.imagecropper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bishal.imagecropper.callback.BitmapCropCallback;
import com.bishal.imagecropper.model.AspectRatio;
import com.bishal.imagecropper.util.SelectedStateListDrawable;
import com.bishal.imagecropper.view.CropImageView;
import com.bishal.imagecropper.view.GestureCropImageView;
import com.bishal.imagecropper.view.OverlayView;
import com.bishal.imagecropper.view.TransformImageView;
import com.bishal.imagecropper.view.ImageCropView;
import com.bishal.imagecropper.view.widget.AspectRatioTextView;
import com.bishal.imagecropper.view.widget.HorizontalProgressWheelView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import static android.app.Activity.RESULT_OK;
/**
 * Author: Bishal Adhikari
 *
 * @Date: 20/10/2024
 */
@SuppressWarnings("ConstantConditions")
public class ImageCropFragment extends Fragment {

    public static final int DEFAULT_COMPRESS_QUALITY = 90;
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;

    public static final int NONE = 0;
    public static final int SCALE = 1;
    public static final int ROTATE = 2;
    public static final int ALL = 3;

    @IntDef({NONE, SCALE, ROTATE, ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GestureTypes {
    }

    public static final String TAG = "ImageCropFragment";

    private static final long CONTROLS_ANIMATION_DURATION = 50;
    private static final int TABS_COUNT = 3;
    private static final int SCALE_WIDGET_SENSITIVITY_COEFFICIENT = 15000;
    private static final int ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42;
    private ImageCropFragmentCallback callback;

    private int mActiveControlsWidgetColor;

    @ColorInt
    private int mRootViewBackgroundColor;
    private int mLogoColor;

    private boolean mShowBottomControls;

    private Transition mControlsTransition;

    private ImageCropView mImageCropView;
    private GestureCropImageView mGestureCropImageView;
    private OverlayView mOverlayView;
    private ViewGroup mWrapperStateAspectRatio, mWrapperStateRotate, mWrapperStateScale;
    private ViewGroup mLayoutAspectRatio, mLayoutRotate, mLayoutScale;
    private List<ViewGroup> mCropAspectRatioViews = new ArrayList<>();
    private TextView mTextViewRotateAngle, mTextViewScalePercent;
    private View mBlockingView;

    private Bitmap.CompressFormat mCompressFormat = DEFAULT_COMPRESS_FORMAT;
    private int mCompressQuality = DEFAULT_COMPRESS_QUALITY;
    private int[] mAllowedGestures = new int[]{SCALE, ROTATE, ALL};

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static ImageCropFragment newInstance(Bundle imageCrop) {
        ImageCropFragment fragment = new ImageCropFragment();
        fragment.setArguments(imageCrop);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof ImageCropFragmentCallback)
            callback = (ImageCropFragmentCallback) getParentFragment();
        else if (context instanceof ImageCropFragmentCallback)
            callback = (ImageCropFragmentCallback) context;
        else
            throw new IllegalArgumentException(context.toString()
                    + " must implement ImageCropFragmentCallback");
    }

    public void setCallback(ImageCropFragmentCallback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.image_crop_fragment_photobox, container, false);

        Bundle args = getArguments();

        setupViews(rootView, args);
        setImageData(args);
        setInitialState();
        addBlockingView(rootView);

        return rootView;
    }


    public void setupViews(View view, Bundle args) {
        mActiveControlsWidgetColor = args.getInt(ImageCrop.Options.EXTRA_IMAGE_CROP_COLOR_CONTROLS_WIDGET_ACTIVE, ContextCompat.getColor(getContext(), R.color.image_crop_color_widget_active));
        mLogoColor = args.getInt(ImageCrop.Options.EXTRA_IMAGE_CROP_LOGO_COLOR, ContextCompat.getColor(getContext(), R.color.image_crop_color_default_logo));
        mShowBottomControls = !args.getBoolean(ImageCrop.Options.EXTRA_HIDE_BOTTOM_CONTROLS, false);
        mRootViewBackgroundColor = args.getInt(ImageCrop.Options.EXTRA_IMAGE_CROP_ROOT_VIEW_BACKGROUND_COLOR, ContextCompat.getColor(getContext(), R.color.image_crop_color_crop_background));

        initiateRootViews(view);
        callback.loadingProgress(true);

        if (mShowBottomControls) {

            ViewGroup wrapper = view.findViewById(R.id.controls_wrapper);
            wrapper.setVisibility(View.VISIBLE);
            LayoutInflater.from(getContext()).inflate(R.layout.image_crop_controls, wrapper, true);

            mControlsTransition = new AutoTransition();
            mControlsTransition.setDuration(CONTROLS_ANIMATION_DURATION);

            mWrapperStateAspectRatio = view.findViewById(R.id.state_aspect_ratio);
            mWrapperStateAspectRatio.setOnClickListener(mStateClickListener);
            mWrapperStateRotate = view.findViewById(R.id.state_rotate);
            mWrapperStateRotate.setOnClickListener(mStateClickListener);
            mWrapperStateScale = view.findViewById(R.id.state_scale);
            mWrapperStateScale.setOnClickListener(mStateClickListener);

            mLayoutAspectRatio = view.findViewById(R.id.layout_aspect_ratio);
            mLayoutRotate = view.findViewById(R.id.layout_rotate_wheel);
            mLayoutScale = view.findViewById(R.id.layout_scale_wheel);

            setupAspectRatioWidget(args, view);
            setupRotateWidget(view);
            setupScaleWidget(view);
            setupStatesWrapper(view);
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.findViewById(R.id.image_crop_frame).getLayoutParams();
            params.bottomMargin = 0;
            view.findViewById(R.id.image_crop_frame).requestLayout();
        }
    }

    private void setImageData(@NonNull Bundle bundle) {
        Uri inputUri = bundle.getParcelable(ImageCrop.EXTRA_INPUT_URI);
        Uri outputUri = bundle.getParcelable(ImageCrop.EXTRA_OUTPUT_URI);
        processOptions(bundle);

        if (inputUri != null && outputUri != null) {
            try {
                mGestureCropImageView.setImageUri(inputUri, outputUri);
            } catch (Exception e) {
                callback.onCropFinish(getError(e));
            }
        } else {
            callback.onCropFinish(getError(new NullPointerException(getString(R.string.image_crop_error_input_data_is_absent))));
        }
    }

    /**
     * This method extracts {@link com.bishal.imagecropper.ImageCrop.Options #optionsBundle} from incoming bundle
     * and setups fragment, {@link OverlayView} and {@link CropImageView} properly.
     */
    @SuppressWarnings("deprecation")
    private void processOptions(@NonNull Bundle bundle) {
        // Bitmap compression options
        String compressionFormatName = bundle.getString(ImageCrop.Options.EXTRA_COMPRESSION_FORMAT_NAME);
        Bitmap.CompressFormat compressFormat = null;
        if (!TextUtils.isEmpty(compressionFormatName)) {
            compressFormat = Bitmap.CompressFormat.valueOf(compressionFormatName);
        }
        mCompressFormat = (compressFormat == null) ? DEFAULT_COMPRESS_FORMAT : compressFormat;

        mCompressQuality = bundle.getInt(ImageCrop.Options.EXTRA_COMPRESSION_QUALITY, ImageCropActivity.DEFAULT_COMPRESS_QUALITY);

        // Gestures options
        int[] allowedGestures = bundle.getIntArray(ImageCrop.Options.EXTRA_ALLOWED_GESTURES);
        if (allowedGestures != null && allowedGestures.length == TABS_COUNT) {
            mAllowedGestures = allowedGestures;
        }

        // Crop image view options
        mGestureCropImageView.setMaxBitmapSize(bundle.getInt(ImageCrop.Options.EXTRA_MAX_BITMAP_SIZE, CropImageView.DEFAULT_MAX_BITMAP_SIZE));
        mGestureCropImageView.setMaxScaleMultiplier(bundle.getFloat(ImageCrop.Options.EXTRA_MAX_SCALE_MULTIPLIER, CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER));
        mGestureCropImageView.setImageToWrapCropBoundsAnimDuration(bundle.getInt(ImageCrop.Options.EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION, CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION));

        // Overlay view options
        mOverlayView.setFreestyleCropEnabled(bundle.getBoolean(ImageCrop.Options.EXTRA_FREE_STYLE_CROP, OverlayView.DEFAULT_FREESTYLE_CROP_MODE != OverlayView.FREESTYLE_CROP_MODE_DISABLE));

        mOverlayView.setDimmedColor(bundle.getInt(ImageCrop.Options.EXTRA_DIMMED_LAYER_COLOR, getResources().getColor(R.color.image_crop_color_default_dimmed)));
        mOverlayView.setCircleDimmedLayer(bundle.getBoolean(ImageCrop.Options.EXTRA_CIRCLE_DIMMED_LAYER, OverlayView.DEFAULT_CIRCLE_DIMMED_LAYER));

        mOverlayView.setShowCropFrame(bundle.getBoolean(ImageCrop.Options.EXTRA_SHOW_CROP_FRAME, OverlayView.DEFAULT_SHOW_CROP_FRAME));
        mOverlayView.setCropFrameColor(bundle.getInt(ImageCrop.Options.EXTRA_CROP_FRAME_COLOR, getResources().getColor(R.color.image_crop_color_default_crop_frame)));
        mOverlayView.setCropFrameStrokeWidth(bundle.getInt(ImageCrop.Options.EXTRA_CROP_FRAME_STROKE_WIDTH, getResources().getDimensionPixelSize(R.dimen.image_crop_default_crop_frame_stoke_width)));

        mOverlayView.setShowCropGrid(bundle.getBoolean(ImageCrop.Options.EXTRA_SHOW_CROP_GRID, OverlayView.DEFAULT_SHOW_CROP_GRID));
        mOverlayView.setCropGridRowCount(bundle.getInt(ImageCrop.Options.EXTRA_CROP_GRID_ROW_COUNT, OverlayView.DEFAULT_CROP_GRID_ROW_COUNT));
        mOverlayView.setCropGridColumnCount(bundle.getInt(ImageCrop.Options.EXTRA_CROP_GRID_COLUMN_COUNT, OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT));
        mOverlayView.setCropGridColor(bundle.getInt(ImageCrop.Options.EXTRA_CROP_GRID_COLOR, getResources().getColor(R.color.image_crop_color_default_crop_grid)));
        mOverlayView.setCropGridStrokeWidth(bundle.getInt(ImageCrop.Options.EXTRA_CROP_GRID_STROKE_WIDTH, getResources().getDimensionPixelSize(R.dimen.image_crop_default_crop_grid_stoke_width)));

        // Aspect ratio options
        float aspectRatioX = bundle.getFloat(ImageCrop.EXTRA_ASPECT_RATIO_X, -1);
        float aspectRatioY = bundle.getFloat(ImageCrop.EXTRA_ASPECT_RATIO_Y, -1);

        int aspectRationSelectedByDefault = bundle.getInt(ImageCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0);
        ArrayList<AspectRatio> aspectRatioList = bundle.getParcelableArrayList(ImageCrop.Options.EXTRA_ASPECT_RATIO_OPTIONS);

        if (aspectRatioX >= 0 && aspectRatioY >= 0) {
            if (mWrapperStateAspectRatio != null) {
                mWrapperStateAspectRatio.setVisibility(View.GONE);
            }
            float targetAspectRatio = aspectRatioX / aspectRatioY;
            mGestureCropImageView.setTargetAspectRatio(Float.isNaN(targetAspectRatio) ? CropImageView.SOURCE_IMAGE_ASPECT_RATIO : targetAspectRatio);
        } else if (aspectRatioList != null && aspectRationSelectedByDefault < aspectRatioList.size()) {
            float targetAspectRatio = aspectRatioList.get(aspectRationSelectedByDefault).getAspectRatioX() / aspectRatioList.get(aspectRationSelectedByDefault).getAspectRatioY();
            mGestureCropImageView.setTargetAspectRatio(Float.isNaN(targetAspectRatio) ? CropImageView.SOURCE_IMAGE_ASPECT_RATIO : targetAspectRatio);
        } else {
            mGestureCropImageView.setTargetAspectRatio(CropImageView.SOURCE_IMAGE_ASPECT_RATIO);
        }

        // Result bitmap max size options
        int maxSizeX = bundle.getInt(ImageCrop.EXTRA_MAX_SIZE_X, 0);
        int maxSizeY = bundle.getInt(ImageCrop.EXTRA_MAX_SIZE_Y, 0);

        if (maxSizeX > 0 && maxSizeY > 0) {
            mGestureCropImageView.setMaxResultImageSizeX(maxSizeX);
            mGestureCropImageView.setMaxResultImageSizeY(maxSizeY);
        }
    }

    private void initiateRootViews(View view) {
        mImageCropView = view.findViewById(R.id.imageCrop);
        mGestureCropImageView = mImageCropView.getCropImageView();
        mOverlayView = mImageCropView.getOverlayView();

        mGestureCropImageView.setTransformImageListener(mImageListener);

        ((ImageView) view.findViewById(R.id.image_view_logo)).setColorFilter(mLogoColor, PorterDuff.Mode.SRC_ATOP);

        view.findViewById(R.id.image_crop_frame).setBackgroundColor(mRootViewBackgroundColor);
    }

    private TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
        @Override
        public void onRotate(float currentAngle) {
            setAngleText(currentAngle);
        }

        @Override
        public void onScale(float currentScale) {
            setScaleText(currentScale);
        }

        @Override
        public void onLoadComplete() {
            mImageCropView.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateInterpolator());
            mBlockingView.setClickable(false);
            callback.loadingProgress(false);
        }

        @Override
        public void onLoadFailure(@NonNull Exception e) {
            callback.onCropFinish(getError(e));
        }

    };

    /**
     * Use {@link #mActiveWidgetColor} for color filter
     */
    private void setupStatesWrapper(View view) {
        ImageView stateScaleImageView = view.findViewById(R.id.image_view_state_scale);
        ImageView stateRotateImageView = view.findViewById(R.id.image_view_state_rotate);
        ImageView stateAspectRatioImageView = view.findViewById(R.id.image_view_state_aspect_ratio);

        stateScaleImageView.setImageDrawable(new SelectedStateListDrawable(stateScaleImageView.getDrawable(), mActiveControlsWidgetColor));
        stateRotateImageView.setImageDrawable(new SelectedStateListDrawable(stateRotateImageView.getDrawable(), mActiveControlsWidgetColor));
        stateAspectRatioImageView.setImageDrawable(new SelectedStateListDrawable(stateAspectRatioImageView.getDrawable(), mActiveControlsWidgetColor));
    }

    private void setupAspectRatioWidget(@NonNull Bundle bundle, View view) {
        int aspectRationSelectedByDefault = bundle.getInt(ImageCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0);
        ArrayList<AspectRatio> aspectRatioList = bundle.getParcelableArrayList(ImageCrop.Options.EXTRA_ASPECT_RATIO_OPTIONS);

        if (aspectRatioList == null || aspectRatioList.isEmpty()) {
            aspectRationSelectedByDefault = 2;

            aspectRatioList = new ArrayList<>();
            aspectRatioList.add(new AspectRatio(null, 1, 1));
            aspectRatioList.add(new AspectRatio(null, 3, 4));
            aspectRatioList.add(new AspectRatio(getString(R.string.image_crop_label_original).toUpperCase(),
                    CropImageView.SOURCE_IMAGE_ASPECT_RATIO, CropImageView.SOURCE_IMAGE_ASPECT_RATIO));
            aspectRatioList.add(new AspectRatio(null, 3, 2));
            aspectRatioList.add(new AspectRatio(null, 16, 9));
        }

        LinearLayout wrapperAspectRatioList = view.findViewById(R.id.layout_aspect_ratio);

        FrameLayout wrapperAspectRatio;
        AspectRatioTextView aspectRatioTextView;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        for (AspectRatio aspectRatio : aspectRatioList) {
            wrapperAspectRatio = (FrameLayout) getLayoutInflater().inflate(R.layout.image_crop_aspect_ratio, null);
            wrapperAspectRatio.setLayoutParams(lp);
            aspectRatioTextView = ((AspectRatioTextView) wrapperAspectRatio.getChildAt(0));
            aspectRatioTextView.setActiveColor(mActiveControlsWidgetColor);
            aspectRatioTextView.setAspectRatio(aspectRatio);

            wrapperAspectRatioList.addView(wrapperAspectRatio);
            mCropAspectRatioViews.add(wrapperAspectRatio);
        }

        mCropAspectRatioViews.get(aspectRationSelectedByDefault).setSelected(true);

        for (ViewGroup cropAspectRatioView : mCropAspectRatioViews) {
            cropAspectRatioView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mGestureCropImageView.setTargetAspectRatio(
                            ((AspectRatioTextView) ((ViewGroup) v).getChildAt(0)).getAspectRatio(v.isSelected()));
                    mGestureCropImageView.setImageToWrapCropBounds();
                    if (!v.isSelected()) {
                        for (ViewGroup cropAspectRatioView : mCropAspectRatioViews) {
                            cropAspectRatioView.setSelected(cropAspectRatioView == v);
                        }
                    }
                }
            });
        }
    }

    private void setupRotateWidget(View view) {
        mTextViewRotateAngle = view.findViewById(R.id.text_view_rotate);
        ((HorizontalProgressWheelView) view.findViewById(R.id.rotate_scroll_wheel))
                .setScrollingListener(new HorizontalProgressWheelView.ScrollingListener() {
                    @Override
                    public void onScroll(float delta, float totalDistance) {
                        mGestureCropImageView.postRotate(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT);
                    }

                    @Override
                    public void onScrollEnd() {
                        mGestureCropImageView.setImageToWrapCropBounds();
                    }

                    @Override
                    public void onScrollStart() {
                        mGestureCropImageView.cancelAllAnimations();
                    }
                });

        ((HorizontalProgressWheelView) view.findViewById(R.id.rotate_scroll_wheel)).setMiddleLineColor(mActiveControlsWidgetColor);


        view.findViewById(R.id.wrapper_reset_rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRotation();
            }
        });
        view.findViewById(R.id.wrapper_rotate_by_angle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateByAngle(90);
            }
        });
        setAngleTextColor(mActiveControlsWidgetColor);
    }

    private void setupScaleWidget(View view) {
        mTextViewScalePercent = view.findViewById(R.id.text_view_scale);
        ((HorizontalProgressWheelView) view.findViewById(R.id.scale_scroll_wheel))
                .setScrollingListener(new HorizontalProgressWheelView.ScrollingListener() {
                    @Override
                    public void onScroll(float delta, float totalDistance) {
                        if (delta > 0) {
                            mGestureCropImageView.zoomInImage(mGestureCropImageView.getCurrentScale()
                                    + delta * ((mGestureCropImageView.getMaxScale() - mGestureCropImageView.getMinScale()) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT));
                        } else {
                            mGestureCropImageView.zoomOutImage(mGestureCropImageView.getCurrentScale()
                                    + delta * ((mGestureCropImageView.getMaxScale() - mGestureCropImageView.getMinScale()) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT));
                        }
                    }

                    @Override
                    public void onScrollEnd() {
                        mGestureCropImageView.setImageToWrapCropBounds();
                    }

                    @Override
                    public void onScrollStart() {
                        mGestureCropImageView.cancelAllAnimations();
                    }
                });
        ((HorizontalProgressWheelView) view.findViewById(R.id.scale_scroll_wheel)).setMiddleLineColor(mActiveControlsWidgetColor);

        setScaleTextColor(mActiveControlsWidgetColor);
    }

    private void setAngleText(float angle) {
        if (mTextViewRotateAngle != null) {
            mTextViewRotateAngle.setText(String.format(Locale.getDefault(), "%.1f°", angle));
        }
    }

    private void setAngleTextColor(int textColor) {
        if (mTextViewRotateAngle != null) {
            mTextViewRotateAngle.setTextColor(textColor);
        }
    }

    private void setScaleText(float scale) {
        if (mTextViewScalePercent != null) {
            mTextViewScalePercent.setText(String.format(Locale.getDefault(), "%d%%", (int) (scale * 100)));
        }
    }

    private void setScaleTextColor(int textColor) {
        if (mTextViewScalePercent != null) {
            mTextViewScalePercent.setTextColor(textColor);
        }
    }

    private void resetRotation() {
        mGestureCropImageView.postRotate(-mGestureCropImageView.getCurrentAngle());
        mGestureCropImageView.setImageToWrapCropBounds();
    }

    private void rotateByAngle(int angle) {
        mGestureCropImageView.postRotate(angle);
        mGestureCropImageView.setImageToWrapCropBounds();
    }

    private final View.OnClickListener mStateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!v.isSelected()) {
                setWidgetState(v.getId());
            }
        }
    };

    private void setInitialState() {
        if (mShowBottomControls) {
            if (mWrapperStateAspectRatio.getVisibility() == View.VISIBLE) {
                setWidgetState(R.id.state_aspect_ratio);
            } else {
                setWidgetState(R.id.state_scale);
            }
        } else {
            setAllowedGestures(0);
        }
    }

    private void setWidgetState(@IdRes int stateViewId) {
        if (!mShowBottomControls) return;

        mWrapperStateAspectRatio.setSelected(stateViewId == R.id.state_aspect_ratio);
        mWrapperStateRotate.setSelected(stateViewId == R.id.state_rotate);
        mWrapperStateScale.setSelected(stateViewId == R.id.state_scale);

        mLayoutAspectRatio.setVisibility(stateViewId == R.id.state_aspect_ratio ? View.VISIBLE : View.GONE);
        mLayoutRotate.setVisibility(stateViewId == R.id.state_rotate ? View.VISIBLE : View.GONE);
        mLayoutScale.setVisibility(stateViewId == R.id.state_scale ? View.VISIBLE : View.GONE);

        changeSelectedTab(stateViewId);

        if (stateViewId == R.id.state_scale) {
            setAllowedGestures(0);
        } else if (stateViewId == R.id.state_rotate) {
            setAllowedGestures(1);
        } else {
            setAllowedGestures(2);
        }
    }

    private void changeSelectedTab(int stateViewId) {
        if (getView() != null) {
            TransitionManager.beginDelayedTransition((ViewGroup) getView().findViewById(R.id.image_crop_photobox), mControlsTransition);
        }
        mWrapperStateScale.findViewById(R.id.text_view_scale).setVisibility(stateViewId == R.id.state_scale ? View.VISIBLE : View.GONE);
        mWrapperStateAspectRatio.findViewById(R.id.text_view_crop).setVisibility(stateViewId == R.id.state_aspect_ratio ? View.VISIBLE : View.GONE);
        mWrapperStateRotate.findViewById(R.id.text_view_rotate).setVisibility(stateViewId == R.id.state_rotate ? View.VISIBLE : View.GONE);
    }

    private void setAllowedGestures(int tab) {
        mGestureCropImageView.setScaleEnabled(mAllowedGestures[tab] == ALL || mAllowedGestures[tab] == SCALE);
        mGestureCropImageView.setRotateEnabled(mAllowedGestures[tab] == ALL || mAllowedGestures[tab] == ROTATE);
    }

    /**
     * Adds view that covers everything below the Toolbar.
     * When it's clickable - user won't be able to click/touch anything below the Toolbar.
     * Need to block user input while loading and cropping an image.
     */
    private void addBlockingView(View view) {
        if (mBlockingView == null) {
            mBlockingView = new View(getContext());
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mBlockingView.setLayoutParams(lp);
            mBlockingView.setClickable(true);
        }

        ((RelativeLayout) view.findViewById(R.id.image_crop_photobox)).addView(mBlockingView);
    }

    public void cropAndSaveImage() {
        mBlockingView.setClickable(true);
        callback.loadingProgress(true);

        mGestureCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, new BitmapCropCallback() {

            @Override
            public void onBitmapCropped(@NonNull Uri resultUri, int offsetX, int offsetY, int imageWidth, int imageHeight) {
                callback.onCropFinish(getResult(resultUri, mGestureCropImageView.getTargetAspectRatio(), offsetX, offsetY, imageWidth, imageHeight));
                callback.loadingProgress(false);
            }

            @Override
            public void onCropFailure(@NonNull Throwable t) {
                callback.onCropFinish(getError(t));
            }
        });
    }

    protected ImageCropResult getResult(Uri uri, float resultAspectRatio, int offsetX, int offsetY, int imageWidth, int imageHeight) {
        return new ImageCropResult(RESULT_OK, new Intent()
                .putExtra(ImageCrop.EXTRA_OUTPUT_URI, uri)
                .putExtra(ImageCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
                .putExtra(ImageCrop.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(ImageCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
                .putExtra(ImageCrop.EXTRA_OUTPUT_OFFSET_X, offsetX)
                .putExtra(ImageCrop.EXTRA_OUTPUT_OFFSET_Y, offsetY)
        );
    }

    protected ImageCropResult getError(Throwable throwable) {
        return new ImageCropResult(ImageCrop.RESULT_ERROR, new Intent().putExtra(ImageCrop.EXTRA_ERROR, throwable));
    }

    public class ImageCropResult {

        public int mResultCode;
        public Intent mResultData;

        public ImageCropResult(int resultCode, Intent data) {
            mResultCode = resultCode;
            mResultData = data;
        }

    }

}

