package com.admin.util;

import android.app.TimePickerDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.TextView;

import com.admin.AppConstant;

import java.util.Calendar;
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

    public static void getTime(final TextView txt_time, Context context) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(context, (timePicker, selectedHour, selectedMinute) -> {
            txt_time.setText(String.format("%02d:%02d", selectedHour , selectedMinute));
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }
}