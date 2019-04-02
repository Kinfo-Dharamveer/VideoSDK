package com.lib.adloader.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lib.adloader.model.MediaModel;
import com.lib.adloader.utils.Utility;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import com.lib.adloader.R;

import java.util.List;

public class PosterAdapater extends RecyclerView.Adapter<PosterHolder> {

    private List<MediaModel.Medium> lists;
    Context mContext;
    private onRecyclerViewItemClickListener mItemClickListener;

    public PosterAdapater(Context context, List<MediaModel.Medium> lists) {

        this.mContext = context;
        this.lists = lists;
    }

    public void setOnItemClickListener(onRecyclerViewItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void setVideos(List<MediaModel.Medium> lists) {
        this.lists = lists;
        notifyDataSetChanged();
    }

    @Override
    public PosterHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.row_poster, null);
        PosterHolder mh = new PosterHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(final PosterHolder holder, final int i) {

        if(i<=3) {

                if (!lists.get(i).getThumbnail().equals("")) {
                    Picasso.get()
                            .load(lists.get(i).getThumbnail())
                            .into(holder.img_Poster1);
                }

                String title=lists.get(i).getTitle();
                holder.txtInfo.setText(title);

//                    int t= Integer.parseInt(lists.getJSONObject(i).getString("duration"));
//                    String time= Utility.timeConversion(t);
                String time= String.valueOf(lists.get(i).getDuration());
                holder.txtTime.setText(time);

            holder.relativeLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /////- MToast.makeText(mContext, "clicked at position : "+i, Toast.LENGTH_SHORT).show();
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClickListener(v, i);
                    }
                }
            });


        } else {
            holder.img_Poster1.setVisibility(View.GONE);
//                holder.txtInfo.setVisibility(View.GONE);
//                holder.txtTime.setVisibility(View.GONE);
//                holder.linearInfo.setVisibility(View.GONE);
            holder.relativeInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return (null != lists ? lists.size() : 0);
    }
}
