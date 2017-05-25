/**
 *
 */
package com.player.receiver;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.player.AppConstant;
import com.player.util.NotificationUtils;

import org.json.JSONObject;

/**
 * desc : Broadcast receiver for get player info from admin app
 */
public class PlayerInfoReceiver extends ParsePushBroadcastReceiver {
    private String msg = "";
    private NotificationUtils notificationUtils;
    private Intent parseIntent;
    private final String TAG = PlayerInfoReceiver.class.getSimpleName();

    public PlayerInfoReceiver() {

    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        if (intent == null)
            return;
        try {
            //{"parsePushId":"hBvAjswywf","volume":6,"push_hash":"d41d8cd98f00b204e9800998ecf8427e"}
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            Log.e(TAG, "Push received:" + json);
            if(json.has("volume")){
                AudioManager am = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
                am.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        json.getInt("volume"),
                        0);
            }else {
                showNotificationMessage(context, json);
            }

        } catch (Exception e) {
            Log.e(TAG, "Push received:" + e.getMessage());
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }


    private void showNotificationMessage(Context context, JSONObject messageInfo) {
        notificationUtils = new NotificationUtils(context);
        notificationUtils.showNotificationMessage(messageInfo);
        sendSuccessPush();
    }

    private void sendSuccessPush() {
        ParsePush push = new ParsePush();
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo(AppConstant.FIELD_APP_NAME, AppConstant.SERVER);
        JSONObject json_data = new JSONObject();
        try {
            json_data.put(AppConstant.FIELD_UPDATE_COMPLETED, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        push.setQuery(query);
        push.setData(json_data);
        push.sendInBackground();
    }
}
