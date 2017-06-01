package com.admin.util;

import android.content.Context;

import com.admin.AppConstant;
import com.admin.model.Message;
import com.admin.model.NotificationMessage;
import com.admin.model.PhoneSettings;
import com.google.firebase.database.FirebaseDatabase;

public class DataManager {
    private Context context;

    public DataManager(Context context) {
        this.context = context;
    }

    public void saveSettings(PhoneSettings phoneSettings) {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_SETTING).child(phoneSettings.deviceId)
                .setValue(phoneSettings);
    }

    public void sendPushNotification(NotificationMessage message, String deviceId) {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_MESSAGES).child(deviceId)
                .setValue(new Message(message.getJsonObject().toString()));
    }

    public void sendVolumePushNotification(int volume, String deviceId) {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_MESSAGES).child(deviceId).setValue(new Message(volume));
    }

    public void setGpsStatus(String deviceID, Boolean isChecked) {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(deviceID)
                .child("gpsEnabled")
                .setValue(isChecked);
    }

    public void clearMessages() {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_ADMIN_MESSAGES)
                .removeValue();
    }
}
