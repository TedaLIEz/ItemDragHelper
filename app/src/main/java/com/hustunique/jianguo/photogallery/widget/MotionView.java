/*
 * Copyright 2016 TedaLIEz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hustunique.jianguo.photogallery.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import com.almeros.android.multitouch.MoveGestureDetector;
import com.almeros.android.multitouch.RotateGestureDetector;
import com.hustunique.jianguo.photogallery.entity.ImageEntity;
import com.hustunique.jianguo.photogallery.entity.MotionEntity;
import com.hustunique.jianguo.photogallery.model.Layer;

/**
 * Created by JianGuo on 12/21/16.
 */

public class MotionView extends FrameLayout {
    private static final float DOUBLE_SCALE_DIFF = 1.5F;
    private static final String TAG = "MotionView";
    private MotionEntity mEntity;

    private float mColorScaleDiff;
    private ScaleGestureDetector mScaleGestureDetector;
    private RotateGestureDetector mRotateGestureDetector;
    private MoveGestureDetector mMoveGestureDetector;
    private GestureDetectorCompat mGestureDetectorCompat;

    private Paint mPaint;

    public MotionView(Context context) {
        this(context, null);
    }

    public MotionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MotionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setEntityAndPosition(@DrawableRes final int id) {
        post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
                Layer layer = new Layer();
                ImageEntity entity = new ImageEntity(layer, bitmap,
                        getWidth(), getHeight());
                setEntityAndPosition(entity);
            }
        });
    }

    private void init(@NonNull Context context) {
        setWillNotDraw(false);
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        mRotateGestureDetector = new RotateGestureDetector(context, new RotateListener());
        mMoveGestureDetector = new MoveGestureDetector(context, new MoveListener());
        mGestureDetectorCompat = new GestureDetectorCompat(context, new TapsListener());
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);
        setOnTouchListener(mOnTouchListener);
        updateUI();
    }

    private void setEntityAndPosition(@Nullable MotionEntity entity) {
        if (entity != null) {
            initialTranslateAndScale(entity);
            setEntity(entity);
        }
    }

    private void initialTranslateAndScale(MotionEntity entity) {
        entity.moveToCanvasCenter();
        entity.getLayer().setScale(entity.getLayer().initialScale());
    }


    private void updateUI() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mEntity.draw(canvas, mPaint);
        super.onDraw(canvas);
    }

    @Nullable
    private boolean findEntityAtPoint(float x, float y) {
        PointF p = new PointF(x, y);
        return mEntity.pointInLayerRect(p);
    }

    public void setEntity(@Nullable MotionEntity entity) {
        mEntity = entity;
        invalidate();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MotionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private final View.OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (findEntityAtPoint(event.getX(), event.getY())) {
                if (mScaleGestureDetector != null) {
                    mMoveGestureDetector.onTouchEvent(event);
                    mGestureDetectorCompat.onTouchEvent(event);
                    if (event.getPointerCount() >= 2) {
                        mScaleGestureDetector.onTouchEvent(event);
                        mRotateGestureDetector.onTouchEvent(event);
                    }
                }
            }
            return true;
        }
    };

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            handleScale(detector.getScaleFactor());
            return true;
        }
    }


    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            if (mEntity != null) {
                mEntity.getLayer().postRotate(-detector.getRotationDegreesDelta());
                updateUI();
            }
            return true;
        }
    }


    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            handleTranslate(detector.getFocusDelta());
            return true;
        }
    }

    private void handleTranslate(PointF delta) {
        if (mEntity != null) {
            float newCenterX = mEntity.absoluteCenterX() + delta.x;
            float newCenterY = mEntity.absoluteCenterY() + delta.y;
            // limit entity center to screen bounds
            boolean needUpdateUI = false;
            if (newCenterX >= 0 && newCenterX <= getWidth()) {
                mEntity.getLayer().postTranslate(delta.x / getWidth(), 0.0F);
                needUpdateUI = true;
            }
            if (newCenterY >= 0 && newCenterY <= getHeight()) {
                mEntity.getLayer().postTranslate(0.0F, delta.y / getHeight());
                needUpdateUI = true;
            }
            if (needUpdateUI) {
                updateUI();
            }
        }
    }

    private void handleScale(float scaleFactorDiff) {
        if (mEntity != null) {
            float diff = scaleFactorDiff - 1.0f;
            mColorScaleDiff += diff * 5;
            mEntity.getLayer().postScale(diff);
            float ratio = (float) Math.min(1.0f, Math.max(0, 1.0 - mColorScaleDiff));
            int color = Math.abs((int) (ratio * 255));
            setBackgroundColor(Color.argb(255, color, color, color));
            updateUI();
        }
    }


    private class TapsListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // TODO: zoom out bitmap with animation
            return true;
        }
    }
}
