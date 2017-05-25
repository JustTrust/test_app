package com.admin.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import com.admin.AppConstant;
import com.admin.util.NotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @desc Broadcast reciver for movement Detector
 */
public class MovementReceiver extends ParsePushBroadcastReceiver {
    private NotificationUtils notificationUtils;
    private final String TAG = MovementReceiver.class.getSimpleName();

    public MovementReceiver() {

    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        if(intent == null)
            return;
        try{
            String strData = intent.getExtras().getString("com.parse.Data");
            parsePushJson(context, strData);
            Log.e(TAG, "Push received:" + strData);
        }catch (Exception e) {
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

    private void parsePushJson(Context context, String data) {
        notificationUtils = new NotificationUtils(context);
        notificationUtils.showNotificationMessage(data);
    }

}
