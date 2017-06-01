package com.admin.receiver;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.admin.AppConstant;
import com.admin.model.Message;
import com.admin.ui.dialog.NotificationDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;


/**
 * @desc Receiver for movement Detector
 */
public class MovementReceiver {

    private static final String TAG = MovementReceiver.class.getSimpleName();
    private Context context;

    public MovementReceiver(Context context) {
        this.context = context;
        clearMessages();
    }

    private void clearMessages() {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_ADMIN_MESSAGES)
                .removeValue((databaseError, databaseReference) -> init());
    }

    private void init() {
        Log.d(TAG, "init: ");
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_ADMIN_MESSAGES)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded: ");
                        proceedMessage(dataSnapshot.getValue(Message.class));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildChanged: ");
                        proceedMessage(dataSnapshot.getValue(Message.class));
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

    private void proceedMessage(Message message) {
        if (!isAppOpened()) return;
        if (message != null) {
            context.startActivity(new Intent(context, NotificationDialog.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    private boolean isAppOpened() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

}
