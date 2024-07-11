package com.ecoss.tflite_test;

import android.app.Activity;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class ObjectMarker extends View {
    private static final String TAG = "AR-HUD";
    private RectF rect;
    private int imageWidth;
    private int imageHeight;
    private int screenWidth;
    private int screenHeight;
    float postScaleWidthOffset = 0;
    float postScaleHeightOffset = 0;
    float scaleFactor = 1;
    float viewAspectRatio;
    float imageAspectRatio;

    private Activity dstActivity;
    private ConstraintLayout layout;
    private ImageView marker;
    private ConstraintSet constraintSet;

    int frameCount = 0;

    public ObjectMarker(Activity dstActivity) {
        super(dstActivity);
        this.dstActivity = dstActivity;

        // Landscape 모드이므로 가로와 세로 크기를 뒤집어서 설정
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
    }

    public void updateRect(RectF newRect) {
        frameCount++;

        rect = transformRect(newRect);

        dstActivity.runOnUiThread(() -> {
            draw();
        });
    }

    public void setImageSourceInfo(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        updateTransformationIfNeeded();
    }

    private void updateTransformationIfNeeded() {
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }
    }

    private RectF transformRect(RectF originalRect) {
        // 가로, 세로 비율 계산

        viewAspectRatio = (float) screenWidth / screenHeight;
        imageAspectRatio = (float) imageWidth / imageHeight;

        float left = originalRect.left;
        float top = originalRect.top;
        float right = originalRect.right;
        float bottom = originalRect.bottom;

        if (viewAspectRatio > imageAspectRatio) {
            scaleFactor = (float) screenWidth / imageWidth;
            postScaleHeightOffset = ((float) screenWidth / imageAspectRatio - screenHeight) / 2;
        } else {
            scaleFactor = (float) screenHeight / imageHeight;
            postScaleWidthOffset = ((float) screenHeight * imageAspectRatio - screenWidth) / 2;
        }
        RectF newRect = new RectF(translateX(left), translateY(top), translateX(right), translateY(bottom));

        // return new Rect(left, top, right, bottom);
        return newRect;
    }

    private float translateX(float x) {
        return ((x * scaleFactor - postScaleWidthOffset));
    }

    private float translateY(float y) {
        return ((y * scaleFactor - postScaleHeightOffset));
    }

    private void draw() {
        if (rect != null) {
            layout = dstActivity.findViewById(R.id.constraintLayout);
            if (layout == null) {
                Log.e(TAG, "ConstraintLayout not found");
                return;
            }

            marker = dstActivity.findViewById(R.id.objectMarker);
            if (marker == null) {
                Log.e(TAG, "Marker ImageView not found");
                return;
            }

            constraintSet = new ConstraintSet();
            constraintSet.clone(layout);

            // 위치와 크기 변환
            float left = rect.left;
            float top = rect.top;
            float right = rect.right;
            float bottom = rect.bottom;

            if (frameCount % 100 == 0) {
                Log.d(TAG, "Marker: " + rect);
            }

            // 위치와 크기 설정
            constraintSet.connect(marker.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, (int) left);
            constraintSet.connect(marker.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (int) top);

            // 크기 설정
            constraintSet.constrainWidth(marker.getId(), (int) (right - left));
            constraintSet.constrainHeight(marker.getId(), (int) (bottom - top));

            constraintSet.applyTo(layout);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 터치 이벤트를 하위 뷰로 전달
        return false;
    }
}
