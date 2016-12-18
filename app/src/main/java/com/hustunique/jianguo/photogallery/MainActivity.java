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

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.hustunique.jianguo.photogallery.adapter.GalleryAdapter;
import com.hustunique.jianguo.photogallery.widget.ItemDragHelper;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    static final String EXTRA_PHOTO_POS = "EXTRA_PHOTO_POS";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        GalleryAdapter galleryAdapter = new GalleryAdapter(this);
        mRecyclerView.setAdapter(galleryAdapter);
        ItemDragHelper dragItemHelper = new ItemDragHelper();
        // TODO: zoom out to show pic
        dragItemHelper.setZoomOutCallback(new ItemDragHelper.ZoomOutCallback() {
            @Override
            public void onZoomOut(RecyclerView.ViewHolder viewHolder) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    viewHolder.itemView.setTransitionName("profile");
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra(EXTRA_PHOTO_POS, viewHolder.getAdapterPosition());
                    startActivity(intent, ActivityOptionsCompat
                            .makeSceneTransitionAnimation(MainActivity.this, viewHolder.itemView, viewHolder.itemView.getTransitionName()).toBundle());
                }
            }
        });
        dragItemHelper.attachToRecyclerView(mRecyclerView);
    }

}
