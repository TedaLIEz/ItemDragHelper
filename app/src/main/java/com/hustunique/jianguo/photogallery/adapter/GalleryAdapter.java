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
    private static final int IMAGES_SIZE = 1;
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
