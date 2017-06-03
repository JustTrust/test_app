package com.admin.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.admin.R;
import com.admin.model.Message;

import java.util.ArrayList;
import java.util.List;

public class LocationNotificationsAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mDeviceLocations = new ArrayList<>();

    public LocationNotificationsAdapter(Context context) {
        mContext = context;
    }

    public void addLocation(Message msg) {
        mDeviceLocations.add(
                msg.deviceName + " moved GPS:" + msg.latitude + " " + msg.longitude + " at " + msg.time);
    }

    public void clear() {
        mDeviceLocations.clear();
    }

    @Override
    public int getCount() {
        return mDeviceLocations.size();
    }

    @Override
    public String getItem(int position) {
        return mDeviceLocations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LocationNotificationViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.cell_list, null);
            holder = new LocationNotificationViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (LocationNotificationViewHolder) convertView.getTag();
        }
        holder.getMessageTextView().setText(mDeviceLocations.get(position));
        return convertView;
    }

    private class LocationNotificationViewHolder {
        private TextView mMessageTextView;

        public LocationNotificationViewHolder(View view) {
            mMessageTextView = (TextView) view.findViewById(R.id.txt_message);
        }

        public TextView getMessageTextView() {
            return mMessageTextView;
        }
    }

}