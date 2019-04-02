package com.lib.adloader.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lib.adloader.R;
import com.lib.adloader.model.MediaModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistHolder> {

    private final List<MediaModel.Medium> lists;
    Context mContext;
    private onRecyclerViewItemClickListener mItemClickListener;

    public PlaylistAdapter(Context context, List<MediaModel.Medium> lists) {

        this.mContext = context;
        this.lists = lists;
    }

    public void setOnItemClickListener(onRecyclerViewItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public PlaylistHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.row_playlist_video, null);
        PlaylistHolder mh = new PlaylistHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(final PlaylistHolder holder,
                                 final int i) {
        // String title=lists.getJSONObject(i).optString("title","");
        String title=lists.get(i).getTitle();
        title = (title.isEmpty() || title.equals("")) ? "No title" : title;
        holder.videoName.setText(title);

        // if (!lists.getJSONObject(i).getString("image").equals("")) {
        if (!lists.get(i).getThumbnail().equals("")) {
            Picasso.get()
                    .load(lists.get(i).getThumbnail())
                    .into(holder.img_Poster);
        }

        //int t = Integer.parseInt(lists.getJSONObject(i).optString("duration","0"));
        //String time= Utility.timeConversion(t);
        //holder.videoDuration.setText(time);
        String time= String.valueOf(lists.get(i).getDuration());
        holder.videoDuration.setText(time);

        holder.mainRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /////- MToast.makeText(mContext, "clicked at position : "+i, Toast.LENGTH_SHORT).show();
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClickListener(v, i);
                }
            }
        });

    }

    @Override
    public int getItemCount() {

        return (null != lists ? lists.size() : 0);

    }


}

