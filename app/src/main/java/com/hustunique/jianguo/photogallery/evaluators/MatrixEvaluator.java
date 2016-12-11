package com.hustunique.jianguo.photogallery.evaluators;

import android.animation.TypeEvaluator;
import android.graphics.Matrix;

/**
 * Created by JianGuo on 12/11/16.
 * {@link TypeEvaluator} for {@link Matrix}
 */

public class MatrixEvaluator implements TypeEvaluator<Matrix>{

    @Override
    public Matrix evaluate(float fraction, Matrix startValue, Matrix endValue) {
        float[] startEntries = new float[9];
        float[] endEntries = new float[9];
        float[] currentEntries = new float[9];
        startValue.getValues(startEntries);
        endValue.getValues(endEntries);
        for (int i = 0; i < 9; i++) {
            currentEntries[i] = (1 - fraction) * startEntries[i] + fraction * endEntries[i];
        }
        Matrix matrix = new Matrix();
        matrix.setValues(currentEntries);
        return matrix;
    }
}
