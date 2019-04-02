package com.lib.adloader.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lib.adloader.R;

public class PlaylistHolder extends RecyclerView.ViewHolder {

    protected TextView videoName;
    protected ImageView img_Poster;
    protected RelativeLayout mainRelative;
    protected TextView videoDuration;

    public PlaylistHolder(View v) {
        super(v);

        videoName = (TextView)v.findViewById(R.id.txt_videoName);
        img_Poster = (ImageView)v.findViewById(R.id.poster);
        mainRelative = (RelativeLayout)v.findViewById(R.id.mainRelative);
        videoDuration = (TextView)v.findViewById(R.id.txt_videoDuration);

    }
}