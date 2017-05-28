package com.admin.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.admin.AppConstant;
import com.admin.util.NotificationUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;


/**
 * @desc Broadcast reciver for movement Detector
 */
public class MovementReceiver {

    private static final String TAG = MovementReceiver.class.getSimpleName();

    public MovementReceiver() {
        init();
    }

    private void init() {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_ADMIN_MESSAGES)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded: ");
                        //parsePushJson(context, strData);

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

    private void parsePushJson(Context context, String data) {
        NotificationUtils notificationUtils = new NotificationUtils(context);
        notificationUtils.showNotificationMessage(data);
    }

}
