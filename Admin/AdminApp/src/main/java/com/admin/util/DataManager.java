package com.admin.util;


import android.content.Context;
import android.provider.Settings;

import com.admin.AppConstant;
import com.admin.model.Message;
import com.admin.model.NotificationMessage;
import com.admin.model.PhoneSettings;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DataManager {
    private Context context;

    public DataManager(Context context) {
        this.context = context;
    }

    public void saveSettings(PhoneSettings phoneSettings) {
        DatabaseReference settingRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_SETTING).child(phoneSettings.deviceId);
        settingRef.setValue(phoneSettings);
    }

    public void sendPushNotification(NotificationMessage message, String deviceId) {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_MESSAGES).child(deviceId).setValue(new Message(message.getJsonObject().toString()));
    }

    public void sendVolumePushNotification(int volume, String deviceId) {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_MESSAGES).child(deviceId).setValue(new Message(volume));
    }
}
