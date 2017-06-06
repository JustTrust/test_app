package com.admin.ui.adapters;


import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.admin.AppConstant;
import com.admin.R;
import com.admin.model.UserConnectionStatus;
import com.admin.ui.PlayerSettingActivity;
import com.admin.util.Utils;

import java.util.ArrayList;


public class PlayerAppListAdapter extends BaseAdapter {

    private ArrayList<UserConnectionStatus> playersList;
    private GpsChangedListener listener;

    public PlayerAppListAdapter(ArrayList<UserConnectionStatus> playersList, GpsChangedListener listener) {
        this.playersList = playersList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return playersList.size();
    }

    @Override
    public UserConnectionStatus getItem(int position) {
        return playersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        Context context = parent.getContext();
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.cell_player_list, null);
            holder.txt_deviceName = (TextView) view.findViewById(R.id.txt_deviceName);
            holder.txt_connStatus = (TextView) view.findViewById(R.id.txt_connStatus);
            holder.txt_deviceId = (TextView) view.findViewById(R.id.txt_deviceId);
            holder.toggle_GPS = (ToggleButton) view.findViewById(R.id.toggle_GPS);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final UserConnectionStatus status = playersList.get(position);
        holder.txt_deviceName.setText(status.deviceName);
        holder.txt_deviceId.setText(status.deviceID);

        boolean isSettingsAvailableForDevice;

        if (Utils.isConnected(status.createdAt)) {
            if (status.isPlaying) {
                holder.txt_connStatus.setText(Html.fromHtml(context.getString(R.string.playing)));
            } else {
                holder.txt_connStatus.setText(Html.fromHtml(context.getString(R.string.pause)));
            }
            isSettingsAvailableForDevice = true;
        } else {
            holder.txt_connStatus.setText(Html.fromHtml(context.getString(R.string.disconnected)));
            isSettingsAvailableForDevice = false;
        }

        holder.toggle_GPS.setOnCheckedChangeListener(null);
        holder.toggle_GPS.setChecked(status.gpsEnabled);

        if (isSettingsAvailableForDevice) {
            view.setOnClickListener(v -> context.startActivity(
                    new Intent(context, PlayerSettingActivity.class)
                    .putExtra(AppConstant.FIELD_DEVICE_ID, status.deviceID)));
            holder.toggle_GPS.setEnabled(true);
        } else {
            view.setOnClickListener(null);
            holder.toggle_GPS.setEnabled(false);
        }

        holder.toggle_GPS.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onGpsStatusChanged(status.deviceID, isChecked);
            }
        });
        return view;
    }

    public interface GpsChangedListener {
        void onGpsStatusChanged(String deviceID, Boolean isChecked);
    }

    class ViewHolder {
        TextView txt_deviceName;
        TextView txt_connStatus;
        ToggleButton toggle_GPS;
        TextView txt_deviceId;
    }
}
