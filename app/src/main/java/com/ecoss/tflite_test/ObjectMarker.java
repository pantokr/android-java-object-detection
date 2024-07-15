package com.ecoss.tflite_test;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.Map;

public class ObjectMarker extends View {
    private static final String TAG = "VAR-TEST";
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
    int frameCount = 0;

    public ObjectMarker(Activity dstActivity) {
        super(dstActivity);
        this.dstActivity = dstActivity;

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
    }

    public void updateRect(Map<RectF, String> boxes) {
        frameCount++;


        dstActivity.runOnUiThread(() -> {

            layout.removeAllViews();


            for (RectF key : boxes.keySet()
            ) {
                String cat = boxes.get(key);
                RectF rect = transformRect(key);
                draw(rect, cat);
            }
        });
    }

    public void setImageSourceInfo(int imageWidth, int imageHeight) {
        layout = dstActivity.findViewById(R.id.markerContainer);

        if (layout == null) {
            Log.e(TAG, "ConstraintLayout not found");
            return;
        }

        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
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

        return newRect;
    }

    private float translateX(float x) {
        return ((x * scaleFactor - postScaleWidthOffset));
    }

    private float translateY(float y) {
        return ((y * scaleFactor - postScaleHeightOffset));
    }

    private void draw(RectF rect, String cat) {
        if (rect != null) {
            ImageView marker;
            TextView markerInfo;
            ConstraintSet constraintSet;

            constraintSet = new ConstraintSet();
            constraintSet.clone(layout);

            marker = new ImageView(dstActivity);
            marker.setId(View.generateViewId());
            marker.setImageResource(R.drawable.object_frame);
            marker.setScaleType(ImageView.ScaleType.FIT_XY);

            markerInfo = new TextView(dstActivity);
            markerInfo.setId(View.generateViewId());
            markerInfo.setText(cat);

            markerInfo.setBackgroundColor(Color.WHITE);
            markerInfo.setTextColor(Color.BLACK);
            markerInfo.setTextSize(20);

            layout.addView(marker);
            layout.addView(markerInfo);

            // 위치와 크기 변환
            float left = rect.left;
            float top = rect.top;
            float right = rect.right;
            float bottom = rect.bottom;

            // 위치와 크기 설정
            constraintSet.connect(marker.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, (int) left);
            constraintSet.connect(marker.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (int) top);

            constraintSet.connect(markerInfo.getId(), ConstraintSet.BOTTOM, marker.getId(), ConstraintSet.TOP);
            constraintSet.connect(markerInfo.getId(), ConstraintSet.START, marker.getId(), ConstraintSet.START);
            constraintSet.connect(markerInfo.getId(), ConstraintSet.END, marker.getId(), ConstraintSet.END);

            // 크기 설정
            constraintSet.constrainWidth(marker.getId(), (int) (right - left));
            constraintSet.constrainHeight(marker.getId(), (int) (bottom - top));

            constraintSet.constrainWidth(markerInfo.getId(), (200));
            constraintSet.constrainHeight(markerInfo.getId(), (100));

            constraintSet.applyTo(layout);
        }
    }
}
