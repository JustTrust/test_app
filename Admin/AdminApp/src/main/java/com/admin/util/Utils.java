package com.admin.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.admin.AppConstant;
import com.admin.R;
import com.admin.model.NotificationMessage;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Ravi on 01/06/15.
 */
public class Utils {
    public static boolean isConnected(Date connectionDate){
        Date now = new Date();
        long differ = now.getTime() - connectionDate.getTime();
        if(differ < AppConstant.CONNECTION_CHECK_TIME){
            return  true;
        }else{
            return false;
        }
    }


    public static void sendPushNotification(final Context ctx, NotificationMessage message, String deviceId) {
        ParsePush push = new ParsePush();
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo(AppConstant.FIELD_DEVICE_ID, deviceId);
        push.setQuery(query);
        push.setData(message.getJsonObject());
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, ctx.getString(R.string.updateSent), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void sendVolumePushNotification(final Context ctx, int message, String deviceId) {
        ParsePush push = new ParsePush();
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo(AppConstant.FIELD_DEVICE_ID, deviceId);
        JSONObject data = new JSONObject();
        try {
            data.put("volume", message);
        } catch (JSONException e) {
            Log.e("", "JSONException in setMessage", e);
        }
        push.setQuery(query);
        push.setData(data);
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, ctx.getString(R.string.updateSent), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }

    }
}