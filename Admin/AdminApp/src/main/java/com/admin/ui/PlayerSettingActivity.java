package com.admin.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.admin.AdminApplication;
import com.admin.AppConstant;
import com.admin.R;
import com.admin.model.NotificationMessage;
import com.admin.model.PhoneSettings;
import com.admin.model.Time;
import com.admin.model.UserConnectionStatus;
import com.admin.util.DataManager;
import com.admin.util.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @desc PlayerSettingActivity for set interval & play time for specific player
 */
public class PlayerSettingActivity extends BaseActivity {

    @BindView(R.id.btnVolumeDown)
    Button btnVolumeDown;
    @BindView(R.id.txtVolume)
    TextView txtVolume;
    @BindView(R.id.btnVolumeUp)
    Button btnVolumeUP;
    @BindView(R.id.edit_songInterval)
    EditText mEdit_songInterval;
    @BindView(R.id.edit_pauseInterval)
    EditText mEdit_pauseInterval;
    @BindView(R.id.txt_startTime)
    TextView mTxt_startTimepicker;
    @BindView(R.id.txt_endTime)
    TextView mTxt_endTimepicker;
    @BindView(R.id.btn_update)
    Button mBtn_update;
    @BindView(R.id.btn_hold)
    Button mBtn_hold;
    @BindView(R.id.video)
    Button mBtn_video;
    @BindView(R.id.txt_connectStatus)
    TextView mTxt_connectStatus;
    @BindView(R.id.txt_songNum)
    TextView mTxt_SongNum;
    @BindView(R.id.txt_remain)
    TextView mTxt_remain;

    @Inject
    DataManager dataManager;

    int volume_level = 0;
    boolean isInitUI = false;
    private Timer mTimer;
    private UserConnectionStatus mConnectionStatus;
    private CheckStatusTask mDurationTask;
    private String mStr_DeviceID;
    private PhoneSettings deviceSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AdminApplication.getAppComponent().inject(this);
        setContentView(R.layout.activity_player_setting);
        ButterKnife.bind(this);
        initUI();
    }

    private void initUI() {
        isInitUI = true;
        mStr_DeviceID = getIntent().getStringExtra(AppConstant.FIELD_DEVICE_ID);
        mTxt_startTimepicker.setOnClickListener(v -> Utils.getTime(mTxt_startTimepicker, this));
        mTxt_endTimepicker.setOnClickListener(v -> Utils.getTime(mTxt_endTimepicker, this));
        mBtn_update.setOnClickListener(v -> {
            NotificationMessage message = getPlayerInfo();
            dataManager.sendPushNotification(message, mStr_DeviceID);
            finish();
        });

        btnVolumeUP.setOnClickListener(v -> {
            if (Integer.valueOf(txtVolume.getText().toString()) < 9)
                volume_level = Integer.valueOf(txtVolume.getText().toString()) + 1;
            else
                Toast.makeText(PlayerSettingActivity.this, "Reached Maximum Limit", Toast.LENGTH_SHORT).show();
            txtVolume.setText(String.valueOf(volume_level));
            dataManager.sendVolumePushNotification(volume_level, mStr_DeviceID);
        });

        btnVolumeDown.setOnClickListener(v -> {
            if (Integer.valueOf(txtVolume.getText().toString()) > 0)
                volume_level = Integer.valueOf(txtVolume.getText().toString()) - 1;

            txtVolume.setText(String.valueOf(volume_level));
            dataManager.sendVolumePushNotification(volume_level, mStr_DeviceID);
        });

        mBtn_video.setOnClickListener(v -> startActivity(
                new Intent(PlayerSettingActivity.this, VideoActivity.class)
                        .putExtra(AppConstant.FIELD_DEVICE_ID, mStr_DeviceID)));

        mBtn_hold.setOnClickListener(v -> {
            dataManager.setHoldStatus(mStr_DeviceID, mBtn_hold.getText().toString().equals(getString(R.string.hold)));
            mBtn_hold.setText(mBtn_hold.getText().toString().equals(getString(R.string.hold))
                    ? getString(R.string.resume) : getString(R.string.hold));
        }
        );

        mTimer = new Timer();
        mDurationTask = new CheckStatusTask();
        mTimer.schedule(mDurationTask, 0, 1000);

        DatabaseReference settingRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_SETTING).child(mStr_DeviceID);
        ValueEventListener settingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deviceSettings = dataSnapshot.getValue(PhoneSettings.class);
                if (deviceSettings != null) {
                    mTxt_startTimepicker.setText(deviceSettings.startTime);
                    mTxt_endTimepicker.setText(deviceSettings.endTime);
                    mEdit_songInterval.setText(deviceSettings.songInterval);
                    mEdit_pauseInterval.setText(deviceSettings.pauseInterval);
                    mBtn_hold.setText(deviceSettings.onHold ? getString(R.string.resume) : getString(R.string.hold));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        settingRef.addListenerForSingleValueEvent(settingListener);
        registerFbListener(settingRef, settingListener);
    }

    private NotificationMessage getPlayerInfo() {
        String str_startTime = mTxt_startTimepicker.getText().toString();
        String str_endTime = mTxt_endTimepicker.getText().toString();
        String str_songInterval = mEdit_songInterval.getText().toString();
        String str_pauseInterval = mEdit_pauseInterval.getText().toString();
        if (deviceSettings == null) {
            deviceSettings = new PhoneSettings();
        }
        deviceSettings.endTime = str_endTime;
        deviceSettings.songInterval = str_songInterval;
        deviceSettings.pauseInterval = str_pauseInterval;
        deviceSettings.startTime = str_startTime;
        deviceSettings.deviceId = mStr_DeviceID;
        dataManager.saveSettings(deviceSettings);

        Time startTime = new Time();
        Time endTime = new Time();
        startTime.parseData(str_startTime);
        endTime.parseData(str_endTime);
        NotificationMessage message = new NotificationMessage(false, startTime, endTime, str_songInterval, str_pauseInterval, null);
        return message;
    }

    public void checkConnectionStatus() {
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(mStr_DeviceID);
        ValueEventListener statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mConnectionStatus = dataSnapshot.getValue(UserConnectionStatus.class);
                setConnectionStatus();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        statusRef.addListenerForSingleValueEvent(statusListener);
        registerFbListener(statusRef, statusListener);
    }

    @SuppressLint("DefaultLocale")
    public void setConnectionStatus() {
        if (mConnectionStatus == null) return;
        long differ = new Date().getTime() - mConnectionStatus.createdAt;
        boolean isPlaying = mConnectionStatus.isPlaying;
        int remainTime = mConnectionStatus.remain;
        boolean isPlayingOrPause = remainTime > 0;
        if (isInitUI) {
            txtVolume.setText(mConnectionStatus.volume != null ? mConnectionStatus.volume : "0");
            isInitUI = false;
        }

        if (remainTime <= 0) {
            mTxt_remain.setText(" -- ");
        } else {
            mTxt_remain.setText(String.format("%02d:%02d",
                    TimeUnit.SECONDS.toMinutes(remainTime),
                    remainTime - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(remainTime))
            ));
        }

        if (differ < AppConstant.CONNECTION_CHECK_TIME) {

            if (mConnectionStatus.strSong != null) {
                mTxt_SongNum.setText(mConnectionStatus.strSong);
            }
            if (isPlayingOrPause) {
                if (isPlaying) {
                    mTxt_connectStatus.setText(Html.fromHtml(getString(R.string.playing)));
                } else {
                    mTxt_connectStatus.setText(Html.fromHtml(getString(R.string.pause)));
                }
            } else {
                if (deviceSettings != null &&
                        !TextUtils.isEmpty(deviceSettings.startTime) &&
                        !TextUtils.isEmpty(deviceSettings.endTime)) {
                    mTxt_connectStatus.setText(Html.fromHtml(getString(R.string.pause)));
                } else {
                    mTxt_connectStatus.setText(Html.fromHtml(getString(R.string.connected)));
                }
                mTxt_remain.setText(" -- ");
            }

        } else {
            mTxt_connectStatus.setText(Html.fromHtml(getString(R.string.disconnected)));
            mTxt_remain.setText(" -- ");
        }
    }

    public class CheckStatusTask extends TimerTask {
        @Override
        public void run() {
            PlayerSettingActivity.this.runOnUiThread(() -> checkConnectionStatus());
        }
    }
}
