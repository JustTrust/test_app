package com.admin.util;

import android.content.Context;

import com.admin.AppConstant;
import com.admin.model.Message;
import com.admin.model.NotificationMessage;
import com.admin.model.PhoneSettings;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public class FireBaseDataManager implements DataManager {
    private Context context;

    public FireBaseDataManager(Context context) {
        this.context = context;
    }

    @Override
    public void saveSettings(PhoneSettings phoneSettings) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_SETTING).child(phoneSettings.deviceId);
        ref.child("startTime").setValue(phoneSettings.startTime);
        ref.child("endTime").setValue(phoneSettings.endTime);
        ref.child("songInterval").setValue(phoneSettings.songInterval);
        ref.child("pauseInterval").setValue(phoneSettings.pauseInterval);
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
    public void setGpsStatus(String deviceID, boolean isChecked) {
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

    @Override
    public void setHoldStatus(String deviceID, boolean hold) {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_SETTING).child(deviceID)
                .child("onHold")
                .setValue(hold);
    }
}
