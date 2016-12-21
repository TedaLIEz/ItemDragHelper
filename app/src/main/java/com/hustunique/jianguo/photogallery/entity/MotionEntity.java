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

package com.hustunique.jianguo.photogallery.entity;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hustunique.jianguo.photogallery.model.Layer;
import com.hustunique.jianguo.photogallery.utils.MathUtils;

/**
 * Created by JianGuo on 12/21/16.
 */

public abstract class MotionEntity {
    @NonNull
    protected final Layer mLayer;

    protected final Matrix mMatrix = new Matrix();


    protected float holyScale;

    /**
     * width of canvas the entity is drawn in
     */
    @IntRange(from = 0)
    protected int canvasWidth;
    /**
     * height of canvas the entity is drawn in
     */
    @IntRange(from = 0)
    protected int canvasHeight;

    private final float[] destPoints = new float[10];

    protected final float[] srcPoints = new float[10];


    public MotionEntity(@NonNull Layer layer,
                        @IntRange(from = 1) int canvasWidth,
                        @IntRange(from = 1) int canvasHeight) {
        mLayer = layer;
        this.canvasHeight = canvasHeight;
        this.canvasWidth = canvasWidth;
    }




    protected void updateMatrix() {
        // init matrix to E - identity matrix
        mMatrix.reset();

        float topLeftX = mLayer.getX() * canvasWidth;
        float topLeftY = mLayer.getY() * canvasHeight;

        float centerX = topLeftX + getWidth() * holyScale * 0.5F;
        float centerY = topLeftY + getHeight() * holyScale * 0.5F;

        // calculate params
        float rotationInDegree = mLayer.getRotationInDegrees();
        float scaleX = mLayer.getScale();
        float scaleY = mLayer.getScale();
        if (mLayer.isFlipped()) {
            // flip (by X-coordinate) if needed
            rotationInDegree *= -1.0F;
            scaleX *= -1.0F;
        }

        // applying transformations : L = S * R * T

        // scale
        mMatrix.preScale(scaleX, scaleY, centerX, centerY);

        // rotate
        mMatrix.preRotate(rotationInDegree, centerX, centerY);

        // translate
        mMatrix.preTranslate(topLeftX, topLeftY);

        // applying holy scale - S`, the result will be : L = S * R * T * S`
        mMatrix.preScale(holyScale, holyScale);
    }

    public float absoluteCenterX() {
        float topLeftX = mLayer.getX() * canvasWidth;
        return topLeftX + getWidth() * holyScale * 0.5F;
    }

    public float absoluteCenterY() {
        float topLeftY = mLayer.getY() * canvasHeight;

        return topLeftY + getHeight() * holyScale * 0.5F;
    }

    public PointF absoluteCenter() {
        float topLeftX = mLayer.getX() * canvasWidth;
        float topLeftY = mLayer.getY() * canvasHeight;

        float centerX = topLeftX + getWidth() * holyScale * 0.5F;
        float centerY = topLeftY + getHeight() * holyScale * 0.5F;

        return new PointF(centerX, centerY);
    }

    public void moveToCanvasCenter() {
        moveCenterTo(new PointF(canvasWidth * 0.5F, canvasHeight * 0.5F));
    }

    public void moveCenterTo(PointF moveToCenter) {
        PointF currentCenter = absoluteCenter();
        mLayer.postTranslate(1.0F * (moveToCenter.x - currentCenter.x) / canvasWidth,
                1.0F * (moveToCenter.y - currentCenter.y) / canvasHeight);
    }

    private final PointF pA = new PointF();
    private final PointF pB = new PointF();
    private final PointF pC = new PointF();
    private final PointF pD = new PointF();

    /**
     * For more info:
     * <a href="http://math.stackexchange.com/questions/190111/how-to-check-if-a-point-is-inside-a-rectangle">StackOverflow: How to check point is in rectangle</a>
     * <p>NOTE: it's easier to apply the same transformation matrix (calculated before) to the original source points, rather than
     * calculate the result points ourselves
     * @param point point
     * @return true if point (x, y) is inside the triangle
     */
    public boolean pointInLayerRect(PointF point) {

        updateMatrix();
        // map rect vertices
        mMatrix.mapPoints(destPoints, srcPoints);

        pA.x = destPoints[0];
        pA.y = destPoints[1];
        pB.x = destPoints[2];
        pB.y = destPoints[3];
        pC.x = destPoints[4];
        pC.y = destPoints[5];
        pD.x = destPoints[6];
        pD.y = destPoints[7];

        return MathUtils.pointInTriangle(point, pA, pB, pC) || MathUtils.pointInTriangle(point, pA, pD, pC);
    }

    /**
     * http://judepereira.com/blog/calculate-the-real-scale-factor-and-the-angle-of-rotation-from-an-android-matrix/
     *
     * @param canvas Canvas to draw
     * @param drawingPaint Paint to use during drawing
     */
    public final void draw(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {

        updateMatrix();

        canvas.save();

        drawContent(canvas, drawingPaint);


        canvas.restore();
    }


    @NonNull
    public Layer getLayer() {
        return mLayer;
    }


    protected abstract void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint);

    public abstract int getWidth();

    public abstract int getHeight();

    public void release() {
        // free resources here
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            release();
        } finally {
            //noinspection ThrowFromFinallyBlock
            super.finalize();
        }
    }

}
