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

package com.hustunique.jianguo.photogallery.evaluators;

import android.animation.TypeEvaluator;
import android.graphics.Matrix;

/**
 * Created by JianGuo on 12/11/16.
 * {@link TypeEvaluator} for {@link Matrix}
 */
@Deprecated
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
