package com.player.util;


import android.content.Context;
import android.provider.Settings;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.player.AppConstant;
import com.player.model.Message;
import com.player.model.PhoneSettings;
import com.player.model.UserConnectionStatus;

import java.util.Calendar;
import java.util.Date;

public class DataManager {

    private Context context;
    private String deviceId;

    public DataManager(Context context) {
        this.context = context;
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void saveSettings(PhoneSettings phoneSettings) {
        DatabaseReference settingRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_SETTING).child(deviceId);
        phoneSettings.deviceId = deviceId;
        settingRef.setValue(phoneSettings);
    }

    public void saveStatus(UserConnectionStatus status) {
        DatabaseReference settingRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(deviceId);
        status.createdAt = new Date().getTime();
        status.deviceID = deviceId;
        settingRef.setValue(status);
    }

    public void saveCoordinateInStatus(final String latitude, final String longitude) {
        DatabaseReference settingRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(deviceId);
        settingRef.child("createdAt").setValue(new Date().getTime());
        settingRef.child("latitude").setValue(latitude);
        settingRef.child("longitude").setValue(longitude);
    }

    /**
     * @param latitude
     * @param longitude
     * @desc Send notification to admin device for location change warning
     */
    public void sendNotificationToAdmin(double latitude, double longitude) {

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);

        Message msg = new Message(latitude, longitude, hours + ":" + min);
        DatabaseReference admMsg = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_ADMIN_MESSAGES).child(deviceId);
        admMsg.setValue(msg);
    }

    public void storeUserConnection(UserConnectionStatus userConnectionStatus) {
        DatabaseReference connected_dev = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(deviceId);
        userConnectionStatus.createdAt = new Date().getTime();
        connected_dev.setValue(userConnectionStatus);
    }

    public FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void logout() {
        if (getCurrentUser() != null){
            FirebaseAuth.getInstance().signOut();
        }
        DatabaseReference connected_dev = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(deviceId);
        connected_dev.child("createdAt").setValue(new Date().getTime());
        connected_dev.child("isPlaying").setValue(Boolean.FALSE);
    }
}
