package com.example.messengerprm;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {
    private static final String TAG = "ZoomableImageView";

    public interface OnImageClickListener {
        void onImageClick();
    }

    private final Matrix matrix = new Matrix();
    private float scaleFactor = 1.0f;
    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 2.5f;
    private OnImageClickListener clickListener;

    private long lastTapTime = 0;
    private static final long DOUBLE_TAP_TIME = 300;
    private boolean isZoomed = false;

    private float initialScale = 1.0f;
    private final PointF initialPosition = new PointF();

    public ZoomableImageView(Context context) {
        super(context);
        init();
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            centerImage();
        }
    }

    private void centerImage() {
        if (getDrawable() == null) return;

        float imageWidth = getDrawable().getIntrinsicWidth();
        float imageHeight = getDrawable().getIntrinsicHeight();
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        if (imageWidth <= 0 || imageHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) return;

        float scaleX = viewWidth / imageWidth;
        float scaleY = viewHeight / imageHeight;
        float scale = Math.min(scaleX, scaleY);

        float dx = (viewWidth - imageWidth * scale) / 2;
        float dy = (viewHeight - imageHeight * scale) / 2;

        matrix.reset();
        matrix.setScale(scale, scale);
        matrix.postTranslate(dx, dy);

        setImageMatrix(matrix);
        scaleFactor = scale;
        initialScale = scale;
        initialPosition.set(dx, dy);

        Log.d(TAG, "Image centered: scale=" + scale + ", dx=" + dx + ", dy=" + dy);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTapTime < DOUBLE_TAP_TIME) {
                    if (isZoomed) {
                        centerImage();
                        isZoomed = false;
                        Log.d(TAG, "Zoomed out to fit screen");
                    } else {
                        float viewWidth = getWidth();
                        float viewHeight = getHeight();
                        float focusX = viewWidth / 2f;
                        float focusY = viewHeight / 2f;
                        float newScale = MAX_SCALE * initialScale;
                        matrix.setScale(newScale, newScale, focusX, focusY);
                        fixTrans();
                        scaleFactor = newScale;
                        isZoomed = true;
                        Log.d(TAG, "Zoomed in to " + newScale + "x at center");
                    }
                } else {
                    if (clickListener != null && scaleFactor <= initialScale * 1.1f) {
                        Log.d(TAG, "Single tap - closing image viewer");
                        performClick();
                        clickListener.onImageClick();
                    } else {
                    }
                }
                lastTapTime = currentTime;
                setImageMatrix(matrix);
                invalidate();
                return true;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error in onTouchEvent", e);
            return false;
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void fixTrans() {
        float[] values = new float[9];
        matrix.getValues(values);
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, getViewWidth(), getImageWidth());
        float fixTransY = getFixTrans(transY, getViewHeight(), getImageHeight());

        if (fixTransX != 0 || fixTransY != 0) {
            matrix.postTranslate(fixTransX, fixTransY);
        }
    }

    private float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans) {
            return -trans + minTrans;
        }
        if (trans > maxTrans) {
            return -trans + maxTrans;
        }
        return 0;
    }

    private float getImageWidth() {
        return getDrawable().getIntrinsicWidth() * scaleFactor;
    }

    private float getImageHeight() {
        return getDrawable().getIntrinsicHeight() * scaleFactor;
    }

    private float getViewWidth() {
        return getWidth();
    }

    private float getViewHeight() {
        return getHeight();
    }
} 