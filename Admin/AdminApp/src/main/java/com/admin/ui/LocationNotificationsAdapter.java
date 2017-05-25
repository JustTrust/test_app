package com.admin.ui;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.admin.R;

import java.util.ArrayList;
import java.util.List;

public class LocationNotificationsAdapter extends BaseAdapter {

    public static final int INVALID_COLLECTION_POSITION = -1;

    private Context mContext;
    private List<Pair<String, String>> mDeviceLocations = new ArrayList<>();

    public LocationNotificationsAdapter(Context context) {
        mContext = context;
    }

    public void addLocation(Pair<String, String> deviceLocation) {
        int presentedLocationPosition = getPresentedLocationPosition(deviceLocation);
        if (presentedLocationPosition == INVALID_COLLECTION_POSITION) {
            mDeviceLocations.add(deviceLocation);
        } else {
            mDeviceLocations.remove(presentedLocationPosition);
            mDeviceLocations.add(presentedLocationPosition, deviceLocation);
        }
    }

    public void clear() {
        mDeviceLocations.clear();
    }

    private int getPresentedLocationPosition(Pair<String, String> deviceLocation) {
        int position = INVALID_COLLECTION_POSITION;
        for (int i = 0; i < mDeviceLocations.size(); i++) {
            Pair<String, String> location = mDeviceLocations.get(i);
            if (location.first != null && location.first.equals(deviceLocation.first)) {
                position = i;
            }
        }
        return position;
    }

    @Override
    public int getCount() {
        return mDeviceLocations.size();
    }

    @Override
    public Pair<String, String> getItem(int position) {
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
        holder.getMessageTextView().setText(mDeviceLocations.get(position).second);
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