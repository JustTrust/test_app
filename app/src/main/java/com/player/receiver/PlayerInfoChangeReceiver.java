/**
 *
 */
package com.player.receiver;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.player.AppConstant;
import com.player.PlayerApplication;
import com.player.model.Message;
import com.player.util.AudioAppManager;
import com.player.util.DataManager;
import com.player.util.NotificationUtils;

import javax.inject.Inject;

/**
 * desc : Broadcast receiver for get player info from admin app
 */
public class PlayerInfoChangeReceiver {

    private final String TAG = PlayerInfoChangeReceiver.class.getSimpleName();

    @Inject
    Context context;
    @Inject
    NotificationUtils notificationUtils;
    @Inject
    DataManager dataManager;
    @Inject
    AudioAppManager audioAppManager;

    public PlayerInfoChangeReceiver() {
        PlayerApplication.getAppComponent().inject(this);
        init();
    }

    private void init() {
        String deviceId = dataManager.getDeviceId();
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_MESSAGES)
                .child(deviceId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Message msg = dataSnapshot.getValue(Message.class);
                if (msg == null) return;
                Log.d(TAG, "onChildAdded: " + msg.toString());
                if (msg.volume != null && msg.volume > -1) {
                    audioAppManager.setVolumeLevel(msg.volume, false);
                } else if (!TextUtils.isEmpty(msg.msg)) {
                    notificationUtils.showNotificationMessage(msg.msg);
                }
                dataManager.deleteMessage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
