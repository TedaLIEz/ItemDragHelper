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

package com.hustunique.jianguo.photogallery.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hustunique.jianguo.photogallery.R;

/**
 * Created by JianGuo on 12/9/16.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    private static final int IMAGES_SIZE = 20;
    private Context mContext;
    private int[] imageIds = {R.drawable.book, R.drawable.bourne, R.drawable.cacw,
            R.drawable.doctor, R.drawable.dory, R.drawable.hours,
            R.drawable.hunger, R.drawable.ipman3, R.drawable.squad, R.drawable.deadpool};

    public GalleryAdapter(Context context) {
        mContext = context;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
        //TODO: add listener
        holder.mImageView.setImageResource(imageIds[position % imageIds.length]);
    }

    @Override
    public int getItemCount() {
        return IMAGES_SIZE;
    }

    public class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
        }
    }
}
