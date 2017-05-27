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
    public static boolean isConnected(Long connectionDate){
        Date now = new Date();
        long differ = now.getTime() - connectionDate;
        return differ < AppConstant.CONNECTION_CHECK_TIME;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}