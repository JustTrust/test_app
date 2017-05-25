package com.admin.util;

import android.app.ActivityManager;
import android.content.*;
import android.os.Build;
import android.widget.Toast;
import com.admin.AdminApplication;
import com.admin.AppConstant;
import com.admin.R;
import com.admin.ui.dialog.NotificationDialog;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Ravi on 01/06/15.
 */
public class NotificationUtils {

    private String TAG = NotificationUtils.class.getSimpleName();

    private Context mContext;
    final ContentValues values = new ContentValues();
    final ContentResolver resolver = AdminApplication.getContext().getContentResolver();

    public NotificationUtils() {
    }

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
    }

    public void showNotificationMessage(String data) {
        int icon = R.mipmap.ic_launcher;
        try {
            JSONObject json_data = new JSONObject(data);
            if (json_data.has(AppConstant.FIELD_UPDATE_COMPLETED) && json_data.getBoolean(AppConstant.FIELD_UPDATE_COMPLETED)) {
                Toast.makeText(mContext, mContext.getString(R.string.updateCompleted), Toast.LENGTH_LONG).show();
                return;
            }
            String deviceName = json_data.getString(AppConstant.FIELD_DEVICE_NAME);
            String latitude = json_data.getString(AppConstant.FIELD_LATITUDE);
            String longitude = json_data.getString(AppConstant.FIELD_LONGITUDE);
            String time = json_data.getString(AppConstant.FIELD_TIME);
            String messageData = deviceName + " moved GPS:" + latitude + " " + longitude + " at " + time;
            Intent intent = new Intent();
            intent.setClass(mContext, NotificationDialog.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(AppConstant.EXTRA_DEVICE_NAME, deviceName);
            intent.putExtra(AppConstant.FIELD_MESSAGE_DATA, messageData);
            mContext.startActivity(intent);
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
            @SuppressWarnings("deprecation")
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}