package com.player.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.player.AppConstant;
import com.player.DataSingleton;
import com.player.PlayerApplication;
import com.player.R;
import com.player.alarms.PlaySongsN;
import com.player.alarms.StartTime;
import com.player.alarms.TimerWakeLock;
import com.player.foreground.events.ConnectivityChangedEvent;
import com.player.foreground.events.StopPlayerEvent;
import com.player.model.MusicInfo;
import com.player.model.NotificationMessage;
import com.player.model.PhoneSettings;
import com.player.model.Time;
import com.player.model.UserConnectionStatus;
import com.player.movedetector.MoveDetector;
import com.player.ui.dialog.ProgressDialog;
import com.player.util.DataManager;
import com.player.util.NetworkUtil;
import com.player.util.NotificationUtils;
import com.player.util.PermissionUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * @desc PlayerActivity for player interface
 */
public class PlayerActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = PlayerActivity.class.getName();
    private static final int PERMISSIONS_REQUEST_LOCATION = 11;
    private static final int PERMISSIONS_REQUEST_READ = 12;
    public static boolean mIs_GPSEnabled = false;
    public static int m_currentPlayingSongIndex = 0;
    public static ArrayList<MusicInfo> mlst_Musics = new ArrayList<>();
    public String mStr_Song = "";

    @BindView(R.id.txt_songInterval)
    TextView mTxt_songInterval;
    @BindView(R.id.txt_pauseInterval)
    TextView mTxt_pauseInterval;
    @BindView(R.id.txt_startTime)
    TextView mTxt_startTime;
    @BindView(R.id.txt_endTime)
    TextView mTxt_endTime;
    @BindView(R.id.txt_appStatus)
    TextView mTxt_appStatus;
    @BindView(R.id.txt_remainTime)
    TextView mTxt_remainTime;
    @BindView(R.id.txt_songName)
    TextView mTxt_songName;
    @BindView(R.id.lst_musics)
    ListView mlst_MusicList;

    @Inject
    DataSingleton dataSingleton;
    @Inject
    DataManager dataManager;
    @Inject
    NotificationUtils notificationUtils;

    AudioManager am;
    String deviceId;
    private MusicListAdapter mAdp_music;
    private PlaySongsN playSongs;
    private MoveDetector mMove_dector;
    private Cursor mCursor;
    private boolean isPlaying;
    private int remainTime;
    private ProgressDialog mPrgDlg;
    private PhoneSettings mDeviceSettings;
    private GoogleApiClient googleApiClient;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private boolean progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerApplication.getAppComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        getMusicsFromStorage();
        initUI();
        initMoveDector();

        FirebaseUser currentUser = dataManager.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authorized");
        } else {
            getActionBar().setTitle(currentUser.getDisplayName());
        }
        TimerWakeLock.acquireCpuWakeLock(this);

        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        startTimer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setSendingConnSetting();
        EventBus.getDefault().register(this);
        updateLocation();
    }

    private void startTimer() {
        compositeSubscription.add(Observable.interval(1, TimeUnit.SECONDS)
                .subscribe(aLong -> {
                    if (playSongs != null) {
                        playSongs.checkPlayStatus();
                    }
                    setSendingConnSetting();
                }));
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
        stopLocationUpdate();
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

    private void initMoveDector() {
        mMove_dector = new MoveDetector(googleApiClient);
    }

    private void setSendingConnSetting() {
        Log.d(LOG_TAG, "setSendingConnSetting: isPlaying " + isPlaying);

        if (progress) return;
        progress = true;

        DatabaseReference connected_dev = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(deviceId);
        connected_dev.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserConnectionStatus status = dataSnapshot.getValue(UserConnectionStatus.class);
                updateConnectionStatus(status);
                progress = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress = false;
            }
        });

    }

    private void updateConnectionStatus(UserConnectionStatus status) {
        if (status != null) {
            status.longitude = mStr_Song;
            status.isPlaying = isPlaying;
            status.remain = remainTime;
            int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            status.volume = String.valueOf(volume_level);
            if (status.gpsEnabled != mIs_GPSEnabled) {
                if (status.gpsEnabled) {
                    Toast.makeText(PlayerActivity.this,
                            PlayerActivity.this.getString(R.string.gps_enabled),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PlayerActivity.this,
                            PlayerActivity.this.getString(R.string.gps_disabled),
                            Toast.LENGTH_SHORT).show();
                }
            }
            mIs_GPSEnabled = status.gpsEnabled;
            mMove_dector.setGPSEnabled(mIs_GPSEnabled);
        }
        dataManager.saveStatus(status);
    }

    // get list of media file from sdcard
    private void getMusicsFromStorage() {

        if (!PermissionUtil.checkReadPermission(PERMISSIONS_REQUEST_READ, this)) return;

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

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
        mlst_MusicList = (ListView) this.findViewById(R.id.lst_musics);
        mAdp_music = new MusicListAdapter(mlst_Musics);
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
        compositeSubscription.clear();
    }

    private void playerDestory() {
        if (playSongs != null) {
            playSongs.onDestroy();
            playSongs = null;
            mStr_Song = "";
            setSongName("--");
            setAppStatus("--");
            setRemainTime(-1);
        }
    }

    private void playNow(Intent playerInfo) {
        if (playSongs == null) {
            playSongs = new PlaySongsN();
            playSongs.onStartCommand(playerInfo);
            if (playSongs.play()) {
                setSongName(playSongs.getFileName(m_currentPlayingSongIndex));
                mStr_Song = (m_currentPlayingSongIndex + 1) + ". " + playSongs.getFileName(m_currentPlayingSongIndex);
            }
        } else {
            playSongs.resume();
        }
    }

    public void updateStatus(final int status, final int remainTime) {

        runOnUiThread(new Runnable() {
            public void run() {
                if (status != PlaySongsN.STATUS_PLAYING) {
                    setAppStatus(AppConstant.PAUSE);
                } else {
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
                StartTime.setAlarm(this, messageData);

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
                    StartTime.setAlarm(this, messageData);
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

        if (data.getRealStartTime() != null) {
            nStartTime = data.getRealStartTime().getHour() * 60 + data.getRealStartTime().getMinute();
        }

        Date now = new Date();
        int nCurrentTime = now.getHours() * 60 + now.getMinutes();
        if ((nCurrentTime >= nStartTime) && (nCurrentTime < nEndTime)) {
            return true;
        } else if ((nStartTime > nEndTime) && (nCurrentTime >= nStartTime)) {
            return true;
        } else if ((nStartTime > nEndTime) && (nCurrentTime <= nEndTime)) {
            return true;
        }
        return false;
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

    private void loadSettingsAndShowDialog() {

        mPrgDlg.show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("connected_devices").child(deviceId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PhoneSettings value = dataSnapshot.getValue(PhoneSettings.class);
                mDeviceSettings = value;
                afterSettingRequest();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                PhoneSettings settings = new PhoneSettings();
                settings.deviceId = deviceId;
                mDeviceSettings = settings;
                afterSettingRequest();
            }
        });

    }

    private void afterSettingRequest() {
        mPrgDlg.dismiss();
        showDialog();
    }

    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View viewRoot = inflater.inflate(R.layout.dialog_setup_time, null);

        final EditText etSongInterval = (EditText) viewRoot.findViewById(R.id.edit_songInterval);
        final TextView tvStartTime = (TextView) viewRoot.findViewById(R.id.txt_startTime);
        final TextView tvEndTime = (TextView) viewRoot.findViewById(R.id.txt_endTime);
        final EditText tvPauseInterval = (EditText) viewRoot.findViewById(R.id.edit_pauseInterval);

        tvStartTime.setOnClickListener(v -> getTime((TextView) v));
        tvEndTime.setOnClickListener(v -> getTime((TextView) v));

        builder.setView(viewRoot);
        builder.setPositiveButton("set", (DialogInterface dialog, int which) -> {

            final String str_startTime = tvStartTime.getText().toString();
            final String str_endTime = tvEndTime.getText().toString();
            final String str_songInterval = etSongInterval.getText().toString();
            final String str_pauseInterval = tvPauseInterval.getText().toString();

            if (str_startTime.isEmpty() || str_endTime.isEmpty() || str_songInterval.isEmpty() || str_pauseInterval.isEmpty()) {
                Toast.makeText(PlayerActivity.this, R.string.empty_field, Toast.LENGTH_LONG).show();
            } else {

                if (mDeviceSettings == null) {
                    mDeviceSettings = new PhoneSettings();
                    mDeviceSettings.deviceId = deviceId;
                }

                mDeviceSettings.endTime = str_endTime;
                mDeviceSettings.songInterval = str_songInterval;
                mDeviceSettings.pauseInterval = str_pauseInterval;
                mDeviceSettings.startTime = str_startTime;
                mDeviceSettings.deviceId = deviceId;
                mPrgDlg.show();

                dataManager.saveSettings(mDeviceSettings);

                Time startTime = new Time();
                Time endTime = new Time();
                startTime.parseData(str_startTime);
                endTime.parseData(str_endTime);

                mPrgDlg.dismiss();

                NotificationMessage message = new NotificationMessage(false, startTime, endTime, str_songInterval, str_pauseInterval);
                notificationUtils.showNotificationMessage(message.getJsonObject());
            }
        });

        builder.setNegativeButton("cancel", (dialog, which) -> {
        });

        builder.create().show();
    }

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

    public synchronized void buildGoogleApiClient() {
        stopLocationUpdate();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();

        if (!googleApiClient.isConnected()) googleApiClient.connect();
    }

    public void stopLocationUpdate() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    public void updateLocation() {
        if (PermissionUtil.checkLocationPermission(PERMISSIONS_REQUEST_LOCATION, this)) {
            buildGoogleApiClient();
            if (!googleApiClient.isConnected()) {
                googleApiClient.connect();
            }
        }
    }

    public boolean checkGPState() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.gps_disabled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtil.verifyPermissions(grantResults)) {
            if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
                buildGoogleApiClient();
            } else if (requestCode == PERMISSIONS_REQUEST_READ) {
                getMusicsFromStorage();
            }
        }
    }


}
