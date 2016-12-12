package com.hustunique.jianguo.photogallery.model;

import android.graphics.Matrix;
import android.support.annotation.FloatRange;

/**
 * Created by JianGuo on 12/11/16.
 */
@Deprecated
public class Layer {
    /**
     * rotation relative to the view center
     */
    @FloatRange(from = 0.0F, to = 360.0F)
    private float rotationInDegrees;

    private float scale;
    /**
     * top left X coordinate, relative to parent
     */
    private float x;
    /**
     * top left Y coordinate, relative to parent
     */
    private float y;

    private boolean isFlipped;
    private final int width, height;

    public Layer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void reset() {
        this.rotationInDegrees = 0.0F;
        this.scale = 1.0f;
        this.isFlipped = false;
        this.x = 0.0F;
        this.y = 0.0F;
    }

    public void postScale(float scaleDiff) {
        float newVal = scale + scaleDiff;
        if (newVal >= getMinScale() && newVal <= getMaxScale()) {
            scale = newVal;
        }
    }

    protected float getMaxScale() {
        return Limits.MAX_SCALE;
    }

    protected float getMinScale() {
        return Limits.MIN_SCALE;
    }

    public void postRotate(float rotationInDegreesDiff) {
        this.rotationInDegrees += rotationInDegreesDiff;
        this.rotationInDegrees %= 360.0F;
    }

    public void postTranslate(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    public void flip() {
        this.isFlipped = !isFlipped;
    }

    public float initialScale() {
        return Limits.INITIAL_ENTITY_SCALE;
    }

    public float getRotationInDegrees() {
        return rotationInDegrees;
    }

    public void setRotationInDegrees(@FloatRange(from = 0.0, to = 360.0) float rotationInDegrees) {
        this.rotationInDegrees = rotationInDegrees;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
    }

    interface Limits {
        float MIN_SCALE = 0.5F;
        float MAX_SCALE = 4.0F;
        float INITIAL_ENTITY_SCALE = 0.4F;
    }

    public Matrix getMatrix() {
        Matrix matrix = new Matrix();
        float topLeftX = getX() * width;
        float topLeftY = getY() * height;
        float centerX = topLeftX + width * 0.5F;
        float centerY = topLeftY + height * 0.5F;
        float rotationInDegree = getRotationInDegrees();
        float scaleX = getScale();
        float scaleY = getScale();
        if (isFlipped()) {
            rotationInDegree *= -1.0F;
            scaleX *= -1.0F;
        }
        matrix.preScale(scaleX, scaleY, centerX, centerY);
        matrix.preRotate(rotationInDegree, centerX, centerY);
        matrix.preTranslate(topLeftX, topLeftY);
//        matrix.preScale(holyScale, holyScale);
        return matrix;
    }

}
