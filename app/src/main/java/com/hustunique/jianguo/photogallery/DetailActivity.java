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

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import static com.hustunique.jianguo.photogallery.MainActivity.EXTRA_PHOTO_POS;

public class DetailActivity extends AppCompatActivity {
    ImageView mImageView;
    private int[] imageIds = {R.drawable.book, R.drawable.bourne, R.drawable.cacw,
            R.drawable.doctor, R.drawable.dory, R.drawable.hours,
            R.drawable.hunger, R.drawable.ipman3, R.drawable.squad, R.drawable.deadpool};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }
        mImageView = (ImageView) findViewById(R.id.detail);
        int pos = getIntent().getIntExtra(EXTRA_PHOTO_POS, 0);
        mImageView.setImageResource(imageIds[pos % imageIds.length]);
        schedulePostponedEnterTransition();
    }

    private void schedulePostponedEnterTransition() {
        mImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startPostponedEnterTransition();
                }
                return true;
            }
        });
    }
}
