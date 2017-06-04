package com.player.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

/**
 * Created by Test-Gupta on 3/31/2017.
 */

public class AppUtils {

    public static String getSimCardSerialNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getSimSerialNumber();
    }

    public static String getMobileNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }


    public static String getCurrentTimeInHHmm() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        return simpleDateFormat.format(new Date());
    }

    public static String getCurrentDateInISOFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.ENGLISH);
        return simpleDateFormat.format(new Date());
    }

    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    public static String getRemainTime(int counter) {
        int minutes = counter / 60;
        int seconds = counter % 60;

        StringBuilder builder = new StringBuilder();

        if (minutes < 10) {
            builder.append("0");
        }

        builder.append(minutes);
        builder.append(":");

        if (seconds < 10) {
            builder.append("0");
        }

        builder.append(seconds);
        return builder.toString();
    }

    public static boolean isNullOrEmpty(String txt) {
        return txt == null || txt.trim().length() == 0;
    }

    public static int getColor(Context context, int colorId) {
        return context.getResources().getColor(colorId);
    }

    public static String[] split(String original, String separator) {

        final Vector nodes = new Vector();

        int index = original.indexOf(separator);

        while (index >= 0) {
            nodes.addElement(original.substring(0, index));
            original = original.substring(index + separator.length());
            index = original.indexOf(separator);
        }

        nodes.addElement(original);

        // Create splitted string array
        final String[] result = new String[nodes.size()];

        if (nodes.size() > 0) {
            for (int loop = 0; loop < nodes.size(); loop++) {
                result[loop] = (String) nodes.elementAt(loop);
            }
        }

        return result;
    }

    public static void showDebugAlert(Context context, String message) {
        //Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        //Crashlytics.log(Log.ERROR, "AppUtils", message);

    }
}
