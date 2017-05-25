package com.player.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.player.AppConstant;
import com.player.DataSingleton;
import com.player.PlayerApplication;
import com.player.R;
import com.player.alarms.PlaySongs;
import com.player.alarms.PlaySongsN;
import com.player.alarms.StartTime;
import com.player.alarms.TimerWakeLock;
import com.player.foreground.events.ConnectivityChangedEvent;
import com.player.foreground.events.StopPlayerEvent;
import com.player.model.MusicInfo;
import com.player.model.NotificationMessage;
import com.player.model.Time;
import com.player.movedetector.MoveDetector;
import com.player.parseModel.ConnectionStatus;
import com.player.parseModel.DeviceSettings;
import com.player.ui.dialog.ProgressDialog;
import com.player.util.NetworkUtil;
import com.player.util.NotificationUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @desc PlayerActivity for player interface
 */
public class PlayerActivity extends Activity {

    private static final String LOG_TAG = PlayerActivity.class.getName();

    public static PlayerActivity instance;
    private TextView mTxt_songInterval;
    private TextView mTxt_pauseInterval;
    private TextView mTxt_startTime;
    private TextView mTxt_endTime;
    private ListView mlst_MusicList;
    private MusicListAdapter mAdp_music;
    private TextView mTxt_appStatus;
    private TextView mTxt_remainTime;
    private TextView mTxt_songName;
    private PlaySongsN playSongs;
    public static boolean mIs_GPSEnabled = false;
    private MoveDetector mMove_dector;
    public String mStr_Song = "";
    public static int m_currentPlayingSongIndex = 0;
    public static ArrayList<MusicInfo> mlst_Musics = new ArrayList<MusicInfo>();
    private ParseQuery<ConnectionStatus> m_connectionQuery = ConnectionStatus.getQuery();
    private Cursor mCursor;
    private boolean isPlaying;
    private int remainTime;
    private ProgressDialog mPrgDlg;
    private DeviceSettings mDeviceSettings;

    private Handler statusHandler = new Handler() {
        public void handleMessage(Message m) {
            switch(m.what) {
                case HANDLER_PLAYER : {
                    if(playSongs != null) {
                        playSongs.checkPlayStatus();
                    }

                    statusHandler.sendEmptyMessageDelayed(HANDLER_PLAYER, 1000);
                }
                break;

                case HANDLER_STATUS : {
                    setSendingConnSetting();
                    statusHandler.sendEmptyMessageDelayed(HANDLER_STATUS, 1000);
                }
                break;
            }
         }
    };

    private static final int HANDLER_PLAYER = 0;
    private static final int HANDLER_STATUS = 1;

    AudioManager am ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        registerInstallation();
        getMusicsFromStorage();
        initUI();
        initMoveDector();
        getActionBar().setTitle(ParseUser.getCurrentUser().getUsername());
        m_connectionQuery.whereEqualTo(AppConstant.FIELD_DEVICE_ID, ParseUser.getCurrentUser().getObjectId());
        TimerWakeLock.acquireCpuWakeLock(PlayerApplication.getContext());

        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        statusHandler.sendEmptyMessage(HANDLER_PLAYER);
        statusHandler.sendEmptyMessage(HANDLER_STATUS);
    }


    @Override
    protected void onStart() {
        super.onStart();
        setSendingConnSetting();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConnectivityChangedEvent event) {
        setSendingConnSetting();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StopPlayerEvent event) {
        /*if(playSongs!=null)
            playSongs.stopPlaying();*/
    }

    // init location moveDector
    private void initMoveDector() {
        mMove_dector = new MoveDetector(ParseUser.getCurrentUser().getObjectId());
    }

    public class UpdateStatusTask extends TimerTask {
        public void run() {

        }
    }

    private boolean progress;

    private void setSendingConnSetting() {
        Log.d(LOG_TAG, "setSendingConnSetting: isPlaying " + isPlaying);

        if (!NetworkUtil.isOnline(instance)) {
            return;
        }

        if(progress)
            return;

        progress = true;
        m_connectionQuery.getFirstInBackground(new GetCallback<ConnectionStatus>() {
            @Override
            public void done(final ConnectionStatus conInfo, ParseException e) {
                progress = false;
                if (conInfo != null) {
                    conInfo.setSong(mStr_Song);
                    conInfo.setIsPlaying(isPlaying);
                    conInfo.setRemain(remainTime);
                    int volume_level= am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    System.out.println("volume_level = " + volume_level);
                    conInfo.setVolume(String.valueOf(volume_level));
                    if (conInfo.getGPSEnabled() != mIs_GPSEnabled) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (conInfo.getGPSEnabled() == true) {
                                    PlayerApplication.showToast("GPS Enabled", Toast.LENGTH_SHORT);
                                } else {
                                    PlayerApplication.showToast("GPS Disabled", Toast.LENGTH_SHORT);
                                }
                            }
                        });
                        mIs_GPSEnabled = conInfo.getGPSEnabled();
                        mMove_dector.setGPSEnabled(mIs_GPSEnabled);
                    }

                    DataSingleton.getInstance().mConnectionStatus = conInfo;
                    conInfo.saveInBackground();
                }
            }
        });
    }




    private void registerInstallation() {
        ParseInstallation currentInstall = ParseInstallation.getCurrentInstallation();
        currentInstall.put(AppConstant.FIELD_DEVICE_ID, ParseUser.getCurrentUser().getObjectId());
        currentInstall.saveInBackground();
    }

    // get list of media file from sdcard
    private void getMusicsFromStorage() {

        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        mCursor = getContentResolver().query(
                uri,
                new String[]{MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DATA},
                MediaStore.Audio.Media.IS_MUSIC + " != 0"
                        + " AND "
                        + MediaStore.MediaColumns.MIME_TYPE + " ='audio/mpeg'",
                null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        mlst_Musics.clear();
        if (mCursor.moveToFirst()) {
            do {
                MusicInfo musicInfo = new MusicInfo();
                musicInfo.mStr_musicName = mCursor.getString(0); //getName of song from external resource
                musicInfo.mStr_musicPath = mCursor.getString(1); //getPath of song form external resource
                musicInfo.mIs_enabled = true;
                mlst_Musics.add(musicInfo);
            } while (mCursor.moveToNext());
        }
        mCursor.close();


    }

    private void initUI() {
        mTxt_songInterval = (TextView) this.findViewById(R.id.txt_songInterval);
        mTxt_pauseInterval = (TextView) this.findViewById(R.id.txt_pauseInterval);
        mTxt_startTime = (TextView) this.findViewById(R.id.txt_startTime);
        mTxt_endTime = (TextView) this.findViewById(R.id.txt_endTime);
        mTxt_appStatus = (TextView) this.findViewById(R.id.txt_appStatus);
        mTxt_remainTime = (TextView) this.findViewById(R.id.txt_remainTime);
        mTxt_songName = (TextView) this.findViewById(R.id.txt_songName);

        mlst_MusicList = (ListView) this.findViewById(R.id.lst_musics);
        mAdp_music = new MusicListAdapter();
        mlst_MusicList.setAdapter(mAdp_music);
        mPrgDlg = new ProgressDialog(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int cateogry_id = intent.getIntExtra(AppConstant.INTENT_CATEGORY, 0);
        if (cateogry_id != AppConstant.INTENT_GPS) {
            setPlayerInfo(intent);
        } else {
            setGpsSetting(intent);
        }
    }

    public void setRemainTime(int remainTime) {
        if (remainTime == -1) {
            mTxt_remainTime.setText("--");
        } else {
            mTxt_remainTime.setText(remainTime + "");
        }
        this.remainTime = remainTime;
    }

    public void setAppStatus(String appstatus) {
        mTxt_appStatus.setText(appstatus);
        if (AppConstant.PLAYING.equals(appstatus)) {
            isPlaying = true;
        } else if (AppConstant.PAUSE.equals(appstatus)) {
            isPlaying = false;
        }
    }

    public void setSongName(String songName) {
        mTxt_songName.setText(songName);
    }

    private void setGpsSetting(Intent intent) {
        boolean isGPSenabled = intent.getBooleanExtra(AppConstant.FIELD_GPS, false);
        mIs_GPSEnabled = isGPSenabled;
        mMove_dector.setGPSEnabled(isGPSenabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerDestory();
        TimerWakeLock.releaseCpuLock();

        statusHandler.removeMessages(HANDLER_PLAYER);
        statusHandler.removeMessages(HANDLER_STATUS);
    }

    private void playerDestory() {
        if (playSongs != null) {
            playSongs.onDestroy();
            playSongs = null;
        }
    }

    private void playNow(Intent playerInfo) {
        if(playSongs == null) {
            playSongs = new PlaySongsN();
            playSongs.onStartCommand(playerInfo);
        }
        else {
            playSongs.resume();
        }
    }

    public void updateStatus(final int status, final int remainTime) {

        runOnUiThread(new Runnable() {
            public void run() {
                if(status != PlaySongsN.STATUS_PLAYING) {
                    setAppStatus(AppConstant.PAUSE);
                }
                else {
                    setAppStatus(AppConstant.PLAYING);
                }
                setRemainTime(remainTime);
            }
        });
    }

    private void setPlayerInfo(Intent intent) {
        int cateogry_id = intent.getIntExtra(AppConstant.INTENT_CATEGORY, 0);
        NotificationMessage messageData = (NotificationMessage) intent.getSerializableExtra(AppConstant.FIELD_MESSAGE_DATA);
        switch (cateogry_id) {
            case AppConstant.INTENT_START: {
                playNow(intent);

                //need to set the alarm for next day as well.
                messageData.clearRealStartTime();
                StartTime.setAlarm(PlayerApplication.getContext(), messageData);

            }
            break;
            case AppConstant.INTENT_UPDATE:
                if (messageData != null) {

                    //stop the current player if there is any update from the
                    //notification and reset the counter back to 0
                    playerDestory();
                    m_currentPlayingSongIndex = 0;

                    if (isPlayNow(messageData)) {
                        playNow(intent);
                        //need to offset the seconds for this

                    }

                    StartTime.setAlarm(PlayerApplication.getContext(), messageData);

                }
                break;
        }
        if (messageData != null) {
            mTxt_songInterval.setText("" + messageData.getSongInterval());
            mTxt_pauseInterval.setText("" + messageData.getPauseInterval());
            System.out.println("messageData.getStartTime() = " + messageData.getStartTime());
            System.out.println("messageData.getEndTime() = " + messageData.getEndTime());
            mTxt_startTime.setText("" + messageData.getStartTime().convertString());
            mTxt_endTime.setText("" + messageData.getEndTime().convertString());
        }
    }


    private boolean isPlayNow(NotificationMessage data) {

        int nStartTime = data.getStartTime().getHour() * 60 + data.getStartTime().getMinute();
        int nEndTime = data.getEndTime().getHour() * 60 + data.getEndTime().getMinute();

        if(data.getRealStartTime() != null) {
            nStartTime = data.getRealStartTime().getHour() * 60 + data.getRealStartTime().getMinute();
        }

        Date now = new Date();
        int nCurrentTime = now.getHours() * 60 + now.getMinutes();
        if ((nCurrentTime >= nStartTime) && (nCurrentTime < nEndTime)) {
            return true;
        } else if ((nStartTime > nEndTime) && (nCurrentTime >= nStartTime)) {
            return true;
        }
        else if ((nStartTime > nEndTime) && (nCurrentTime <= nEndTime)) {
            return true;
        }
        return false;
    }


    class ViewHolder {
        TextView txt__musicName;
        CheckBox chk_music;
    }

    private class MusicListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mlst_Musics.size();
        }

        @Override
        public MusicInfo getItem(int pos) {
            // TODO Auto-generated method stub

            return mlst_Musics.get(pos);
        }

        @Override
        public long getItemId(int index) {
            // TODO Auto-generated method stub
            return mlst_Musics.size();
        }

        @Override
        public View getView(final int pos, View view, ViewGroup arg2) {
            final ViewHolder holder;
            if (view == null) {
                holder = new ViewHolder();
                view = View.inflate(PlayerActivity.this, R.layout.cell_music_list, null);
                holder.txt__musicName = (TextView) view.findViewById(R.id.txt_musicName);
                holder.chk_music = (CheckBox) view.findViewById(R.id.chk_music);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            int index = pos + 1;
            holder.txt__musicName.setText(index + ". " + mlst_Musics.get(pos).mStr_musicName);
            if (mlst_Musics.get(pos).mIs_enabled == true) {
                holder.chk_music.setChecked(true);
            } else {
                holder.chk_music.setChecked(false);
            }

            holder.chk_music.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked == true) {
                        mlst_Musics.get(pos).mIs_enabled = true;
                    } else {
                        mlst_Musics.get(pos).mIs_enabled = false;
                    }
                }
            });
            return view;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_setup_time) {
            loadSettingsAndShowDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadSettingsAndShowDialog(){

        mPrgDlg.show();

        ParseQuery<DeviceSettings> deviceSettingsParseQuery = DeviceSettings.getQuery().whereEqualTo(AppConstant.FIELD_DEVICE_Id, ParseUser.getCurrentUser().getObjectId());
        deviceSettingsParseQuery.getFirstInBackground(new GetCallback<DeviceSettings>() {
            @Override
            public void done(DeviceSettings object, ParseException e) {
                DeviceSettings settings = null;
                if (object != null &&  e==null) {
                    mDeviceSettings = object;
                }
                else {
                    settings = new DeviceSettings();
                    settings.setDeviceId(ParseUser.getCurrentUser().getObjectId());
                    mDeviceSettings = settings;
                }

                mPrgDlg.dismiss();
                showDialog();
            }
        });

    }

    private void showDialog() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View viewRoot = inflater.inflate(R.layout.dialog_setup_time, null);

        final EditText etSongInterval = (EditText)viewRoot.findViewById(R.id.edit_songInterval);
        final TextView tvStartTime  = (TextView) viewRoot.findViewById(R.id.txt_startTime);
        final TextView tvEndTime  = (TextView) viewRoot.findViewById(R.id.txt_endTime);
        final EditText tvPauseInterval  = (EditText) viewRoot.findViewById(R.id.edit_pauseInterval);


        final String deviceID = ParseUser.getCurrentUser().getObjectId();

        tvStartTime.setOnClickListener(onDateViewClickListener);
        tvEndTime.setOnClickListener(onDateViewClickListener);
        //do something with your view

        builder.setView(viewRoot);
        builder.setPositiveButton("set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                final String str_startTime = tvStartTime.getText().toString();
                final String str_endTime = tvEndTime.getText().toString();
                final String str_songInterval = etSongInterval.getText().toString();
                final String str_pauseInterval = tvPauseInterval.getText().toString();

                if (str_startTime.isEmpty() || str_endTime.isEmpty() || str_songInterval.isEmpty() || str_pauseInterval.isEmpty()) {
                    Toast.makeText(PlayerActivity.this, R.string.empty_field, Toast.LENGTH_LONG).show();
                } else {


                    if (mDeviceSettings == null) {
                        mDeviceSettings = new DeviceSettings();
                        mDeviceSettings.setDeviceId(ParseUser.getCurrentUser().getObjectId());
                    }

                    mDeviceSettings.setEndTime(str_endTime);
                    mDeviceSettings.setSongInterval(str_songInterval);
                    mDeviceSettings.setPauseInterval(str_pauseInterval);
                    mDeviceSettings.setStartTime(str_startTime);
                    mDeviceSettings.setDeviceId(deviceID);
                    mPrgDlg.show();
                    mDeviceSettings.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e==null){
                                DataSingleton.getInstance().mDeviceSettings = null;
                            }
                            else{
                                DataSingleton.getInstance().mDeviceSettings = mDeviceSettings;;
                            }
                            mPrgDlg.dismiss();
                        }
                    });



                    Time startTime = new Time();
                    Time endTime = new Time();
                    startTime.parseData(str_startTime);
                    endTime.parseData(str_endTime);
                    NotificationMessage message = new NotificationMessage(false, startTime, endTime, str_songInterval, str_pauseInterval);

                    NotificationUtils notificationUtils = new NotificationUtils(PlayerActivity.this);
                    notificationUtils.showNotificationMessage(message.getJsonObject());


                }
            }

        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

    private View.OnClickListener onDateViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getTime((TextView)v);
        }
    };


    private void getTime(final TextView txt_time) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                txt_time.setText(selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }



}
