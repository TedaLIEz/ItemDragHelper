package com.hustunique.jianguo.photogallery.interpolators;

import android.view.animation.Interpolator;

/**
 * Created by JianGuo on 12/11/16.
 */

public class ReverseInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float v) {
        return Math.abs(v - 1f);
    }
}
