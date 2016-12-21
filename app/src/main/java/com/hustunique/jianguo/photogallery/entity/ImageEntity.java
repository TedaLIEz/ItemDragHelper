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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hustunique.jianguo.photogallery.model.Layer;

/**
 * Created by JianGuo on 12/21/16.
 * Entity for Image using {@link Bitmap}
 */

public class ImageEntity extends MotionEntity {
    @NonNull
    private final Bitmap mBitmap;

    public ImageEntity(@NonNull Layer layer,
                       @NonNull Bitmap bitmap,
                       @IntRange(from = 1) int canvasWidth,
                       @IntRange(from = 1) int canvasHeight) {
        super(layer, canvasWidth, canvasHeight);
        mBitmap = bitmap;
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        float widthAspect = 1.0F * canvasWidth / width;
        float heightAspect = 1.0F * canvasHeight / height;
        holyScale = Math.min(widthAspect, heightAspect);

        // initial position of the entity
        srcPoints[0] = 0; srcPoints[1] = 0;
        srcPoints[2] = width; srcPoints[3] = 0;
        srcPoints[4] = width; srcPoints[5] = height;
        srcPoints[6] = 0; srcPoints[7] = height;
        srcPoints[8] = 0; srcPoints[8] = 0;
    }

    @Override
    protected void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        canvas.drawBitmap(mBitmap, mMatrix, drawingPaint);
    }

    @Override
    public int getWidth() {
        return mBitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return mBitmap.getHeight();
    }

    @Override
    public void release() {
        if (!mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
    }
}
