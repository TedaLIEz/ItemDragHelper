package com.hustunique.jianguo.photogallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.hustunique.jianguo.photogallery.adapter.GalleryAdapter;
import com.hustunique.jianguo.photogallery.widget.ItemDragHelper;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
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
        mRecyclerView.setAdapter(new GalleryAdapter(this));
        ItemDragHelper dragItemHelper = new ItemDragHelper();
        // TODO: zoom out to show pic
        dragItemHelper.setZoomOutCallback(new ItemDragHelper.ZoomOutCallback() {
            @Override
            public void onZoomOut(View view) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(MainActivity.this, view, "profile");
                startActivity(intent, optionsCompat.toBundle());
            }
        });
        dragItemHelper.attachToRecyclerView(mRecyclerView);
    }

}
