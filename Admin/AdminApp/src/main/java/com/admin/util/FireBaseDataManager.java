package com.admin.util;

import android.content.Context;

import com.admin.AppConstant;
import com.admin.model.Message;
import com.admin.model.NotificationMessage;
import com.admin.model.PhoneSettings;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public class FireBaseDataManager implements DataManager{
    private Context context;

    public FireBaseDataManager(Context context) {
        this.context = context;
    }

    @Override
    public void saveSettings(PhoneSettings phoneSettings) {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_SETTING).child(phoneSettings.deviceId)
                .setValue(phoneSettings);
    }

    @Override
    public void sendPushNotification(NotificationMessage message, String deviceId) {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_MESSAGES).child(deviceId)
                .setValue(new Message(message.getJsonObject().toString()));
    }

    @Override
    public void sendVolumePushNotification(int volume, String deviceId) {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_MESSAGES).child(deviceId).setValue(new Message(volume));
    }

    @Override
    public void setGpsStatus(String deviceID, Boolean isChecked) {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(deviceID)
                .child("gpsEnabled")
                .setValue(isChecked);
    }

    @Override
    public void clearMessages() {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_ADMIN_MESSAGES)
                .removeValue();
    }
}
