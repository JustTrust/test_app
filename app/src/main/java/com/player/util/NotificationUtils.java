package com.player.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.player.AppConstant;
import com.player.PlayerApplication;

import org.json.JSONObject;

import com.player.R;
import com.player.model.NotificationMessage;
import com.player.ui.activity.PlayerActivity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Ravi on 01/06/15.
 */
public class NotificationUtils {

    private String TAG = NotificationUtils.class.getSimpleName();

    private Context mContext;
    final ContentValues values = new ContentValues();
    final ContentResolver resolver = PlayerApplication.getContext().getContentResolver();

    public NotificationUtils() {
    }

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
    }

    public void showNotificationMessage(JSONObject data, Intent intent) {
        int icon = R.mipmap.ic_launcher;
        NotificationMessage playerInfo = new NotificationMessage();
        playerInfo.parseData(data);
        intent.putExtra(AppConstant.INTENT_CATEGORY, AppConstant.INTENT_UPDATE);
        intent.putExtra(AppConstant.FIELD_MESSAGE_DATA, (Serializable) playerInfo);
        try {
            if (isAppIsInBackground(mContext)) {
                /*int mNotificationId = 100;

                PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        mContext);
                Notification notification = mBuilder.setSmallIcon(icon).setTicker("AdminMessage").setWhen(0)
                        .setAutoCancel(true)
                        .setContentTitle("UpdateMessageArrived")
                        .setStyle(inboxStyle)
                        .setContentIntent(resultPendingIntent)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                        .setContentText("MessageArrived")
                        .build();

                NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(mNotificationId, notification);*/
            } else {
                Intent newIntent = new Intent(mContext, PlayerActivity.class);
                newIntent.putExtra(AppConstant.INTENT_CATEGORY, AppConstant.INTENT_UPDATE);
                newIntent.putExtra(AppConstant.FIELD_MESSAGE_DATA, (Serializable)playerInfo);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PlayerApplication.getContext().startActivity(newIntent);
                Toast.makeText(PlayerApplication.getContext(),"UpdateInfo", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showNotificationMessage(JSONObject data) {
        Intent newIntent = new Intent(mContext, PlayerActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            if (!isAppIsInBackground(mContext)) {
                boolean isGPSSetting = data.getBoolean(AppConstant.NOTIFY_IS_BACKGROUND);
                if(isGPSSetting == false) {
                    NotificationMessage playerInfo = new NotificationMessage();
                    playerInfo.parseData(data);
                    newIntent.putExtra(AppConstant.INTENT_CATEGORY, AppConstant.INTENT_UPDATE);
                    newIntent.putExtra(AppConstant.FIELD_MESSAGE_DATA, (Serializable) playerInfo);
                    PlayerApplication.getContext().startActivity(newIntent);
                    Toast.makeText(PlayerApplication.getContext(), "UpdateInfo", Toast.LENGTH_LONG).show();
                }else{
                    boolean isGpsEnabled = data.getBoolean(AppConstant.FIELD_GPS);
                    newIntent.putExtra(AppConstant.INTENT_CATEGORY, AppConstant.INTENT_GPS);
                    newIntent.putExtra(AppConstant.FIELD_GPS, isGpsEnabled);
                    PlayerApplication.getContext().startActivity(newIntent);
                    if(isGpsEnabled == true){
                        PlayerApplication.showToast("Gps Enabled", Toast.LENGTH_LONG);
                    }else{
                        PlayerApplication.showToast("Gps Disabled", Toast.LENGTH_LONG);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}