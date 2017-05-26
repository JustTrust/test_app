package com.player.ui.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.player.R;
import com.player.model.MusicInfo;

import java.util.ArrayList;


public class MusicListAdapter extends BaseAdapter {

    ArrayList<MusicInfo> mlst_Musics;

    public MusicListAdapter(ArrayList<MusicInfo> mlst_Musics) {
        this.mlst_Musics = mlst_Musics;
    }

    @Override
    public int getCount() {
        return mlst_Musics.size();
    }

    @Override
    public MusicInfo getItem(int pos) {
        return mlst_Musics.get(pos);
    }

    @Override
    public long getItemId(int index) {
        return mlst_Musics.size();
    }

    @Override
    public View getView(final int pos, View view, ViewGroup arg2) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) arg2.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.cell_music_list, null);
            holder.txt__musicName = (TextView) view.findViewById(R.id.txt_musicName);
            holder.chk_music = (CheckBox) view.findViewById(R.id.chk_music);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.txt__musicName.setText((pos + 1) + ". " + mlst_Musics.get(pos).mStr_musicName);
        if (mlst_Musics.get(pos).mIs_enabled) {
            holder.chk_music.setChecked(true);
        } else {
            holder.chk_music.setChecked(false);
        }

        holder.chk_music.setOnCheckedChangeListener((buttonView, isChecked) ->
                mlst_Musics.get(pos).mIs_enabled = isChecked);
        return view;
    }

    class ViewHolder {
        TextView txt__musicName;
        CheckBox chk_music;
    }
}
