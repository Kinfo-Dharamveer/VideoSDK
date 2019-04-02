package com.lib.adloader.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lib.adloader.R;

public class PosterHolder extends RecyclerView.ViewHolder {

    protected ImageView img_Poster1;
    protected TextView txtInfo;
    protected TextView txtTime;
    protected LinearLayout linearInfo;
    protected RelativeLayout relativeInfo;
    protected RelativeLayout relativeLinear;

    public PosterHolder(View v) {
        super(v);

        relativeLinear=(RelativeLayout) v.findViewById(R.id.liner);

        img_Poster1=(ImageView)v.findViewById(R.id.poster1);
        txtInfo=(TextView) v.findViewById(R.id.txt_Info);
        txtTime=(TextView) v.findViewById(R.id.txt_Time);
        linearInfo=(LinearLayout)v.findViewById(R.id.linearInfo);
        relativeInfo=(RelativeLayout) v.findViewById(R.id.relativeInfo);

    }
}