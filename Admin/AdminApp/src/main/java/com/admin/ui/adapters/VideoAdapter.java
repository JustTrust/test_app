package com.admin.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.admin.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<String> videoList;
    private View.OnClickListener listener;

    public VideoAdapter(@NonNull List<String> videoList, @NonNull View.OnClickListener listener) {
        this.videoList = videoList;
        this.listener = listener;
    }

    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new VideoAdapter.ViewHolder(LayoutInflater.
                from(viewGroup.getContext()).inflate(R.layout.item_video, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(VideoAdapter.ViewHolder viewHolder, int i) {
        viewHolder.videoName.setText(videoList.get(i));
        viewHolder.videoName.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

     class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_name)
        TextView videoName;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
