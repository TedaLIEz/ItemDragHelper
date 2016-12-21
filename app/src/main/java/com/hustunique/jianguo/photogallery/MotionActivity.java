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

package com.hustunique.jianguo.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hustunique.jianguo.photogallery.entity.ImageEntity;
import com.hustunique.jianguo.photogallery.model.Layer;
import com.hustunique.jianguo.photogallery.widget.MotionView;

public class MotionActivity extends AppCompatActivity {
    private MotionView mMotionView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion);
        mMotionView = (MotionView) findViewById(R.id.motion_view);
        mMotionView.post(new Runnable() {
            @Override
            public void run() {
                Layer layer = new Layer();
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hours);
                ImageEntity entity = new ImageEntity(layer, bitmap,
                        mMotionView.getWidth(), mMotionView.getHeight());
                mMotionView.setEntityAndPosition(entity);
            }
        });
    }
}
