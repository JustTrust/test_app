package com.admin.util;


import android.content.Context;
import android.provider.Settings;

public class DataManager {
    private Context context;
    private String deviceId;

    public DataManager(Context context) {
        this.context = context;
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
