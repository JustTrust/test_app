package com.admin.receiver;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.admin.AppConstant;
import com.admin.model.Message;
import com.admin.ui.dialog.NotificationDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * @desc Broadcast reciver for movement Detector
 */
public class MovementReceiver {

    private static final String TAG = MovementReceiver.class.getSimpleName();
    private Context context;

    public MovementReceiver(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_ADMIN_MESSAGES)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded: ");
                        final Message message = dataSnapshot.getValue(Message.class);
                        proceedMessage(message);
                        FirebaseDatabase.getInstance().getReference()
                                .child(AppConstant.NODE_ADMIN_MESSAGES)
                                .child(message.deviceId).removeValue();
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

    private void proceedMessage(Message message) {
        if (!TextUtils.isEmpty(message.videoLink)) {
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference().child(message.videoLink);
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                context.startActivity(new Intent(context, NotificationDialog.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .putExtra(AppConstant.EXTRA_DEVICE_NAME, message.deviceId)
                        .putExtra(AppConstant.FIELD_MESSAGE_DATA, uri));
            }).addOnFailureListener(exception -> {
                Log.d(TAG, "proceedMessage: "+ exception.getMessage());
            });
        }
    }

}
