package com.admin.ui;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.admin.AppConstant;
import com.admin.R;
import com.admin.model.NotificationMessage;
import com.admin.model.Time;
import com.admin.parsemodel.ConnectionStatus;
import com.admin.parsemodel.DeviceSettings;
import com.admin.util.Utils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @desc PlayerSettingActivity for set interval & play time for specific player
 */
public class PlayerSettingActivity extends Activity implements View.OnClickListener {
    private TextView mTxt_startTimepicker;
    private TextView mTxt_endTimepicker;
    private Button mBtn_update;
    private EditText mEdit_songInterval;
    private EditText mEdit_pauseInterval;
    private TextView mTxt_SongNum;
    private TextView mTxt_connectStatus;
    private TextView mTxt_remain;
    private Timer mTimer;
    private ConnectionStatus mConnectionStatus;
    private CheckStatusTask mDurationTask;
    private String mStr_DeviceID;
    private DeviceSettings deviceSettings;
    private Button btnVolumeUP, btnVolumeDown;
    private TextView txtVolume;

    int volume_level= 0 ;
    boolean isInitUI = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_setting);
        initUI();

    }

    private void initUI() {
        isInitUI = true;
        mTxt_startTimepicker = (TextView) this.findViewById(R.id.txt_startTime);
        mTxt_endTimepicker = (TextView) this.findViewById(R.id.txt_endTime);
        mEdit_songInterval = (EditText) this.findViewById(R.id.edit_songInterval);
        mEdit_pauseInterval = (EditText) this.findViewById(R.id.edit_pauseInterval);
        mBtn_update = (Button) this.findViewById(R.id.btn_update);
        mTxt_SongNum = (TextView) this.findViewById(R.id.txt_songNum);
        mTxt_connectStatus = (TextView) this.findViewById(R.id.txt_connectStatus);
        mTxt_remain = (TextView) this.findViewById(R.id.txt_remain);
        btnVolumeUP = (Button) this.findViewById(R.id.btnVolumeUp);
        btnVolumeDown = (Button) this.findViewById(R.id.btnVolumeDown);
        txtVolume = (TextView) this.findViewById(R.id.txtVolume);

        mEdit_songInterval.setOnClickListener(this);
        mEdit_pauseInterval.setOnClickListener(this);
        mTxt_startTimepicker.setOnClickListener(this);
        mTxt_endTimepicker.setOnClickListener(this);
        mBtn_update.setOnClickListener(this);
        btnVolumeUP.setOnClickListener(this);
        btnVolumeDown.setOnClickListener(this);


        mTimer = new Timer();
        mDurationTask = new CheckStatusTask();
        mTimer.schedule(mDurationTask, 0, 3000);
        mStr_DeviceID = getIntent().getStringExtra(AppConstant.FIELD_DEVICE_ID);

        ParseQuery<DeviceSettings> deviceSettingsParseQuery = new ParseQuery<>(DeviceSettings.class);
        deviceSettingsParseQuery.whereEqualTo(DeviceSettings.DEVICE_ID, mStr_DeviceID);
        deviceSettingsParseQuery.getFirstInBackground(new GetCallback<DeviceSettings>() {
            @Override
            public void done(DeviceSettings object, ParseException e) {
                if (e == null && object != null) {
                    deviceSettings = object;
                    mTxt_startTimepicker.setText(object.getStartTime());
                    mTxt_endTimepicker.setText(object.getEndTime());
                    mEdit_songInterval.setText(object.getSongInterval());
                    mEdit_pauseInterval.setText(object.getPauseInterval());
                }
            }
        });
    }

    private void getTime(final TextView txt_time) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                String str = Integer.toString(selectedMinute);
                if(str.equals("0")){
                    txt_time.setText(selectedHour + ":" + "00");
                }else {
                    txt_time.setText(selectedHour + ":" + selectedMinute);
                }
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_startTime:
                getTime(mTxt_startTimepicker);
                break;
            case R.id.txt_endTime:
                getTime(mTxt_endTimepicker);
                break;
            case R.id.btn_update:
                NotificationMessage message = getPlayerInfo();
                Utils.sendPushNotification(PlayerSettingActivity.this, message, mStr_DeviceID);
                break;
            case R.id.btnVolumeDown:
                System.out.println("Volume Down" + Integer.valueOf(txtVolume.getText().toString()));

                if(Integer.valueOf(txtVolume.getText().toString())>0)
                    volume_level= Integer.valueOf(txtVolume.getText().toString())-1;

                txtVolume.setText(String.valueOf(volume_level));

                Utils.sendVolumePushNotification(PlayerSettingActivity.this,volume_level,mStr_DeviceID);
                break;
            case R.id.btnVolumeUp:
                System.out.println("Volume UP " + Integer.valueOf(txtVolume.getText().toString()));
                if(Integer.valueOf(txtVolume.getText().toString())<15)
                    volume_level= Integer.valueOf(txtVolume.getText().toString())+1;
                else
                    Toast.makeText(PlayerSettingActivity.this,"Reached Maximum Limit",Toast.LENGTH_SHORT).show();

                txtVolume.setText(String.valueOf(volume_level));
                Utils.sendVolumePushNotification(PlayerSettingActivity.this,volume_level,mStr_DeviceID);

                break;

        }
    }


    private NotificationMessage getPlayerInfo() {
        String str_startTime = mTxt_startTimepicker.getText().toString();
        String str_endTime = mTxt_endTimepicker.getText().toString();
        String str_songInterval = mEdit_songInterval.getText().toString();
        String str_pauseInterval = mEdit_pauseInterval.getText().toString();
        if (deviceSettings == null) {
            deviceSettings = new DeviceSettings();
        }
        deviceSettings.setEndTime(str_endTime);
        deviceSettings.setSongInterval(str_songInterval);
        deviceSettings.setPauseInterval(str_pauseInterval);
        deviceSettings.setStartTime(str_startTime);
        deviceSettings.setDeviceId(mStr_DeviceID);
        deviceSettings.saveEventually();

        Time startTime = new Time();
        Time endTime = new Time();
        startTime.parseData(str_startTime);
        endTime.parseData(str_endTime);
        NotificationMessage message = new NotificationMessage(false, startTime, endTime, str_songInterval, str_pauseInterval, null);
        return message;
    }

    public void checkConnectionStatus() {
        ParseQuery<ConnectionStatus> query = ConnectionStatus.getQuery();
        query.whereEqualTo(AppConstant.FIELD_DEVICE_ID, mStr_DeviceID);
        query.getFirstInBackground(new GetCallback<ConnectionStatus>() {
            @Override
            public void done(ConnectionStatus object, ParseException e) {
                if (e == null) {

                    mConnectionStatus = object;
                    setConnectionStatus();
                }
            }
        });
    }

    public class CheckStatusTask extends TimerTask {

        @Override
        public void run() {
            PlayerSettingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkConnectionStatus();
                }
            });
        }
    }

    public void setConnectionStatus() {
        Date connectionDate = mConnectionStatus.getUpdatedAt();
        Date now = new Date();
        long differ = now.getTime() - connectionDate.getTime();
        boolean isPlaying = mConnectionStatus.isPlaying();
        int remainTime = mConnectionStatus.getRemainTime();
        boolean isPlayingOrPause = remainTime > 0;
        if(isInitUI) {
            txtVolume.setText(mConnectionStatus.getVolume() != null ? mConnectionStatus.getVolume() : "0");
            isInitUI = false;
        }

        if (remainTime <= 0) {
            mTxt_remain.setText(" -- ");
        } else {
            mTxt_remain.setText(remainTime + "");
        }


        if (differ < AppConstant.CONNECTION_CHECK_TIME) {

            if (mConnectionStatus.getSong() != null) {
                mTxt_SongNum.setText(mConnectionStatus.getSong());
            }
            if (isPlayingOrPause) {
                if (isPlaying) {
                    mTxt_connectStatus.setText(Html.fromHtml(getString(R.string.playing)));
                } else {
                    mTxt_connectStatus.setText(Html.fromHtml(getString(R.string.pause)));
                }
            } else {
                if (deviceSettings != null &&
                        !TextUtils.isEmpty(deviceSettings.getStartTime()) &&
                        !TextUtils.isEmpty(deviceSettings.getEndTime())) {
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
}
