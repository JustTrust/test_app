package com.player.util;


import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.player.AppConstant;
import com.player.model.Message;
import com.player.model.PhoneSettings;
import com.player.model.UserConnectionStatus;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public class FirebaseDataManager implements DataManager{

    private static final String TAG = FirebaseDataManager.class.getSimpleName();
    private static final String CREATED_AT = "createdAt";
    private static final String DEVICE_ID = "deviceID";
    private static final String DEVICE_NAME = "deviceName";
    private static final String STR_SONG = "strSong";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String IS_PLAYING = "isPlaying";
    private static final String REMAIN = "remain";
    private static final String VOLUME = "volume";

    private Context context;
    private String deviceId;

    public FirebaseDataManager(Context context) {
        this.context = context;
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public void saveSettings(PhoneSettings phoneSettings) {
        DatabaseReference settingRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_SETTING).child(deviceId);
        phoneSettings.deviceId = deviceId;
        settingRef.setValue(phoneSettings);
    }

    @Override
    public void saveStatus(UserConnectionStatus status) {
        DatabaseReference devRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(deviceId);
        devRef.child(CREATED_AT).setValue(new Date().getTime());
        devRef.child(DEVICE_ID).setValue(deviceId);
        devRef.child(DEVICE_NAME).setValue(status.deviceName);
        devRef.child(STR_SONG).setValue(status.strSong);
        devRef.child(LATITUDE).setValue(status.latitude);
        devRef.child(LONGITUDE).setValue(status.longitude);
        devRef.child(IS_PLAYING).setValue(status.isPlaying);
        devRef.child(REMAIN).setValue(status.remain);
        devRef.child(VOLUME).setValue(status.volume);
    }

    @Override
    public void saveCoordinateInStatus(final String latitude, final String longitude) {
        DatabaseReference settingRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(deviceId);
        settingRef.child(CREATED_AT).setValue(new Date().getTime());
        settingRef.child(LATITUDE).setValue(latitude);
        settingRef.child(LONGITUDE).setValue(longitude);
    }

    @Override
    public void sendNotificationToAdmin(double latitude, double longitude) {

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);

        Message msg = new Message(latitude, longitude, hours + ":" + min);
        msg.deviceId = deviceId;
        msg.deviceName = getDeviceName();
        msg.timeStamp = cal.getTimeInMillis();
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_ADMIN_MESSAGES).child(deviceId)
                .setValue(msg);
    }

    private String getDeviceName() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            return user.getEmail().replace(AppConstant.USER_EMAIL, "");
        } else {
            return "";
        }
    }

    @Override
    public void storeUserConnection(UserConnectionStatus userConnectionStatus) {
        DatabaseReference connected_dev = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(deviceId);
        userConnectionStatus.createdAt = new Date().getTime();
        userConnectionStatus.deviceID = deviceId;
        connected_dev.setValue(userConnectionStatus);
    }

    @Override
    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public void logout() {
        if (getCurrentUser() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        DatabaseReference connected_dev = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(deviceId);
        connected_dev.child(CREATED_AT).setValue(new Date().getTime());
        connected_dev.child(IS_PLAYING).setValue(Boolean.FALSE);
    }

    @Override
    public void storeFileInStorage(String filepath) {
        if (TextUtils.isEmpty(filepath)) return;
        File file = new File(filepath);
        Uri path = Uri.fromFile(file);
        StorageReference fileRef = FirebaseStorage.getInstance()
                .getReference().child(deviceId).child(file.getName());
        fileRef.putFile(path).addOnCompleteListener(task -> {
            Log.d(TAG, "storeFileInStorage: file was send " + filepath);
            FirebaseDatabase.getInstance().getReference().child(AppConstant.NODE_VIDEO)
                    .child(getDeviceId()).push().setValue(file.getName());
        }).addOnFailureListener(e -> {
            Log.d(TAG, "storeFileInStorage: file wasnt send " + filepath);
        });
    }

    @Override
    public void deleteMessage() {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_MESSAGES)
                .child(getDeviceId()).removeValue();
    }

}
