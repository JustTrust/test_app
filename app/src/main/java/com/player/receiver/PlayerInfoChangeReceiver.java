/**
 *
 */
package com.player.receiver;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.player.AppConstant;
import com.player.PlayerApplication;
import com.player.model.Message;
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

    public PlayerInfoChangeReceiver() {
        PlayerApplication.getAppComponent().inject(this);
        init();
    }

    private void init() {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_MESSAGES)
                .child(deviceId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message msg = dataSnapshot.getValue(Message.class);
                Log.d(TAG, "onChildAdded: " + msg.toString());
                if (msg.volume != null && msg.volume > -1) {
                    AudioManager am = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
                    am.setStreamVolume(
                            AudioManager.STREAM_MUSIC, msg.volume, 0);
                } else if (msg.msg != null) {
                    notificationUtils.showNotificationMessage(msg.msg);
                }
                FirebaseDatabase.getInstance().getReference()
                        .child(AppConstant.NODE_MESSAGES)
                        .child(deviceId).removeValue();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
