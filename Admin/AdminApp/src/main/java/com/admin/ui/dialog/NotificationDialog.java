package com.admin.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.admin.AdminApplication;
import com.admin.AppConstant;
import com.admin.R;
import com.admin.ui.LocationNotificationsAdapter;

public class NotificationDialog extends Activity {

    private ListView mList_Notifications;
    private Button mBtn_OK;
    private LocationNotificationsAdapter mAy_adapter;
    private Context mContext = AdminApplication.getContext();
    private static MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout);
        initUI();
        setTitle(AppConstant.WARNING);
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                mediaPlayer.setDataSource(mContext, Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.hangout));
                mediaPlayer.prepare();
                mediaPlayer.setLooping(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Intent intent = getIntent();
        String deviceName = intent.getStringExtra(AppConstant.EXTRA_DEVICE_NAME);
        String message = intent.getStringExtra(AppConstant.FIELD_MESSAGE_DATA);
        if (message != null && !message.equals("")) {
            setData(new Pair<>(deviceName, message));
        }
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
        mBtn_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAy_adapter.clear();
                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String deviceName = intent.getStringExtra(AppConstant.EXTRA_DEVICE_NAME);
        String messageData = intent.getStringExtra(AppConstant.FIELD_MESSAGE_DATA);
        setData(new Pair<>(deviceName, messageData));
    }

    private void setData(Pair<String, String> messageData) {
        mAy_adapter.addLocation(messageData);
        mAy_adapter.notifyDataSetChanged();
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

}
