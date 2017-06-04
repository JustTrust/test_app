package com.player.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.player.AppConstant;
import com.player.R;
import com.player.model.NotificationMessage;
import com.player.ui.activity.NewPlayerActivity;

import org.json.JSONObject;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

/**
 * Created by Ravi on 01/06/15.
 */
public class NotificationUtils {

    private String TAG = NotificationUtils.class.getSimpleName();

    private Context mContext;

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
    }

    private boolean isAppOpened() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

    public void showNotificationMessage(String msg) {
        NotificationMessage playerInfo = new NotificationMessage();
        try {
            if (isAppOpened()) {
                Intent newIntent = new Intent(mContext, NewPlayerActivity.class);
                newIntent.putExtra(AppConstant.INTENT_CATEGORY, AppConstant.INTENT_UPDATE);
                newIntent.putExtra(AppConstant.FIELD_MESSAGE_DATA, msg);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(newIntent);
                Toast.makeText(mContext, "UpdateInfo", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showNotificationMessage(JSONObject data) {
        Intent newIntent = new Intent(mContext, NewPlayerActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            if (isAppOpened()) {
                boolean isGPSSetting = data.getBoolean(AppConstant.NOTIFY_IS_BACKGROUND);
                if (!isGPSSetting) {
                    NotificationMessage playerInfo = new NotificationMessage();
                    playerInfo.parseData(data);
                    newIntent.putExtra(AppConstant.INTENT_CATEGORY, AppConstant.INTENT_UPDATE);
                    newIntent.putExtra(AppConstant.FIELD_MESSAGE_DATA, playerInfo);
                    mContext.startActivity(newIntent);
                    Toast.makeText(mContext, "UpdateInfo", Toast.LENGTH_LONG).show();
                } else {
                    boolean isGpsEnabled = data.getBoolean(AppConstant.FIELD_GPS);
                    newIntent.putExtra(AppConstant.INTENT_CATEGORY, AppConstant.INTENT_GPS);
                    newIntent.putExtra(AppConstant.FIELD_GPS, isGpsEnabled);
                    mContext.startActivity(newIntent);
                    if (isGpsEnabled) {
                        Toast.makeText(mContext, mContext.getString(R.string.gps_enabled), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.gps_disabled), Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}