package com.player.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONObject;

/**
 * Created by dipen on 12/3/16.
 */
public class VolumeChangeReceiver extends ParsePushBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            //showNotificationMessage(context, json);
            Log.e("", "Push received:" + json);
        } catch (Exception e) {
            Log.e("", "Push received:" + e.getMessage());
        }
    }
}
