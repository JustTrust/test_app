package com.admin.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import com.admin.AdminApplication;
import com.admin.AppConstant;
import com.admin.R;
import com.admin.model.Message;
import com.admin.ui.adapters.LocationNotificationsAdapter;
import com.admin.util.DataManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;

public class NotificationDialog extends Activity {

    private static MediaPlayer mediaPlayer = null;
    private ListView mList_Notifications;
    private Button mBtn_OK;
    private LocationNotificationsAdapter mAy_adapter;

    @Inject
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AdminApplication.getAppComponent().inject(this);
        setContentView(R.layout.dialog_layout);
        initUI();
        setTitle(getString(R.string.notification));
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                mediaPlayer.setDataSource(this, Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.hangout));
                mediaPlayer.prepare();
                mediaPlayer.setLooping(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void initUI() {
        mList_Notifications = (ListView) this.findViewById(R.id.lst_notifications);
        mBtn_OK = (Button) this.findViewById(R.id.btn_Ok);
        mAy_adapter = new LocationNotificationsAdapter(this);
        mList_Notifications.setAdapter(mAy_adapter);
        mBtn_OK.setOnClickListener(v -> {
            mAy_adapter.clear();
            dataManager.clearMessages();
            finish();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateData();
    }

    private void updateData() {
        FirebaseDatabase.getInstance().getReference().child(AppConstant.NODE_ADMIN_MESSAGES)
                .orderByChild("timeStamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("Notification", "onDataChange: ");
                        if (dataSnapshot != null && dataSnapshot.getChildrenCount() > 0) {
                            mAy_adapter.clear();
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Message msg = postSnapshot.getValue(Message.class);
                                mAy_adapter.addLocation(msg);
                            }
                            mAy_adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

}