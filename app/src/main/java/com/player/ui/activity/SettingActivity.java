package com.player.ui.activity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.player.AppConstant;
import com.player.PlayerApplication;
import com.player.R;
import com.player.model.NotificationMessage;
import com.player.model.PhoneSettings;
import com.player.model.Time;
import com.player.model.UserConnectionStatus;
import com.player.ui.views.CustomViewGroup;
import com.player.util.DataManager;
import com.player.util.NotificationUtils;

import java.util.Calendar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public class SettingActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btnBack;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.txt_start_time)
    TextView txtStartTime;
    @BindView(R.id.txt_stop_time)
    TextView txtStopTime;
    @BindView(R.id.edt_play_time)
    EditText edtPlayTime;
    @BindView(R.id.edt_pause_time)
    EditText edtPauseTime;
    @BindView(R.id.logout_bt)
    Button logoutBt;
    @BindView(R.id.action_bar)
    CustomViewGroup actionBar;

    @Inject
    DataManager dataManager;
    @Inject
    NotificationUtils notificationUtils;

    PhoneSettings phoneSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerApplication.getAppComponent().inject(this);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        loadSettings();
    }

    @OnClick(R.id.btn_back)
    public void onBackButton() {
        startActivity(new Intent(this, NewPlayerActivity.class));
        finish();
    }

    @OnClick(R.id.txt_start_time)
    public void onStartTimeClick() {
        getTime(txtStartTime);
    }

    @OnClick(R.id.txt_stop_time)
    public void onStopTimeClick() {
        getTime(txtStopTime);
    }

    @OnClick(R.id.logout_bt)
    void logoutBtClick(){
        finish();
    }

    @OnClick(R.id.btn_ok)
    public void onOkClick() {
        String start = txtStartTime.getText().toString().trim();
        String end = txtStopTime.getText().toString().trim();
        String play = edtPlayTime.getText().toString().trim();
        String pause = edtPauseTime.getText().toString().trim();

        if (start.isEmpty() || end.isEmpty() || play.isEmpty() || pause.isEmpty()) {
            Toast.makeText(this, R.string.empty_field, Toast.LENGTH_LONG).show();
        } else {

            if (phoneSettings == null) {
                phoneSettings = new PhoneSettings();
                phoneSettings.deviceId = dataManager.getDeviceId();
            }

            phoneSettings.endTime = end;
            phoneSettings.songInterval = play;
            phoneSettings.pauseInterval = pause;
            phoneSettings.startTime = start;
            phoneSettings.deviceId = dataManager.getDeviceId();

            dataManager.saveSettings(phoneSettings);

            NotificationMessage message = new NotificationMessage(false,
                    new Time(start), new Time(end), play, pause);
            notificationUtils.showNotificationMessage(message.getJsonObject());
        }

    }

    private void loadSettings() {
        DatabaseReference settingRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_SETTING).child(dataManager.getDeviceId());
        ValueEventListener settingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                phoneSettings = dataSnapshot.getValue(PhoneSettings.class);
                if (phoneSettings == null) {
                    phoneSettings = new PhoneSettings();
                    phoneSettings.deviceId = dataManager.getDeviceId();
                } else {
                    updateUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                phoneSettings = new PhoneSettings();
                phoneSettings.deviceId = dataManager.getDeviceId();
            }
        };
        settingRef.addListenerForSingleValueEvent(settingListener);
        registerFbListener(settingRef, settingListener);

        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(dataManager.getDeviceId());
        ValueEventListener statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserConnectionStatus status = dataSnapshot.getValue(UserConnectionStatus.class);
                if (status != null) actionBar.setGpsSignal(status.gpsEnabled);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        statusRef.addValueEventListener(statusListener);
        registerFbListener(statusRef, statusListener);
    }

    private void updateUI() {
        edtPlayTime.setText(phoneSettings.songInterval);
        edtPauseTime.setText(phoneSettings.pauseInterval);
        txtStopTime.setText(phoneSettings.endTime);
        txtStartTime.setText(phoneSettings.startTime);
    }

    private void getTime(final TextView txt_time) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this,
                (timePicker, selectedHour, selectedMinute) ->
                        txt_time.setText(String.format("%02d:%02d", selectedHour, selectedMinute)),
                hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }
}
