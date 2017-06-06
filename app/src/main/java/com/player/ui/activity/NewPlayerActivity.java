package com.player.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.player.AppConstant;
import com.player.PlayerApplication;
import com.player.R;
import com.player.alarms.PlaySongsN;
import com.player.alarms.StartTime;
import com.player.alarms.TimerWakeLock;
import com.player.foreground.events.ConnectivityChangedEvent;
import com.player.foreground.events.StopPlayerEvent;
import com.player.foreground.services.BackgroundVideoRecordingService;
import com.player.model.MusicInfo;
import com.player.model.NotificationMessage;
import com.player.model.PhoneSettings;
import com.player.model.Time;
import com.player.model.UserConnectionStatus;
import com.player.movedetector.MoveDetector;
import com.player.ui.views.CustomViewGroup;
import com.player.ui.views.VerticalSeekBar;
import com.player.util.AudioAppManager;
import com.player.util.DataManager;
import com.player.util.NotificationUtils;
import com.player.util.PermissionUtil;
import com.player.util.PlayHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

/**
 * @desc NewPlayerActivity for player interface
 */
public class NewPlayerActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int PERMISSIONS_REQUEST_LOCATION = 11;
    private static final int PERMISSIONS_REQUEST_CAMERA = 13;
    private static final String TAG = NewPlayerActivity.class.getSimpleName();
    public static boolean mIs_GPSEnabled = false;
    public static int m_currentPlayingSongIndex = 0;
    public static ArrayList<MusicInfo> listMusics = new ArrayList<>();
    public String songName = "";
    private static final int NUM_PARTS = 10;

    @BindView(R.id.txt_start_end_time)
    TextView mTxt_startTime;

    @BindView(R.id.txt_song_name)
    TextView mTxt_songName;

    @BindView(R.id.txt_remaining_time)
    TextView mTxt_remainTime;

    @BindView(R.id.txt_status)
    TextView mTxt_appStatus;

    @BindView(R.id.btn_settings)
    ImageButton mBtn_settings;

    @BindView(R.id.seek_volume)
    VerticalSeekBar mSeekBar;

    @BindView(R.id.action_bar)
    CustomViewGroup actionBar;

    @Inject
    DataManager dataManager;
    @Inject
    NotificationUtils notificationUtils;
    @Inject
    AudioAppManager audioAppManager;
    @Inject
    PlayHelper playHelper;

    private PlaySongsN playSongs;
    private MoveDetector moveDetector;
    private boolean isPlaying;
    private int remainTime;
    private int m_level;
    private PhoneSettings phoneSettings;
    private GoogleApiClient googleApiClient;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private boolean progress;
    private ContentObserver contentObserver;
    private LocationRequest locationRequest = LocationRequest.create()
            .setInterval(1600)
            .setMaxWaitTime(12000)
            .setNumUpdates(1)
            .setPriority(PRIORITY_HIGH_ACCURACY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerApplication.getAppComponent().inject(this);
        setContentView(R.layout.activity_view_home);
        ButterKnife.bind(this);
        FirebaseUser currentUser = dataManager.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authorized");
        }
        initUI();
        initMoveDetector();
        initVolumeControl();

        TimerWakeLock.acquireCpuWakeLock(this);
        startTimer();
        getMusicsFromStorage();
        checkPermissions();
    }

    private void checkPermissions() {
        Single.just(0).delay(3, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer ->
                        PermissionUtil.requestCameraPermissions(NewPlayerActivity.this, PERMISSIONS_REQUEST_CAMERA));
    }

    @Override
    protected void onStart() {
        super.onStart();
        setSendingConnSetting();
        EventBus.getDefault().register(this);
        buildGoogleApiClient();
    }

    private void startTimer() {
        compositeSubscription.add(Observable.interval(3, 1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
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

    @OnClick(R.id.btn_settings)
    public void onSettingsClick() {
        startActivity(new Intent(this, SettingActivity.class));
        finish();
    }

    @OnClick(R.id.btn_volume_up)
    public void onVolumeIncrease() {
        int currentLevel = audioAppManager.getVolumeLevel();
        currentLevel += m_level;
        updateVolume(currentLevel);

    }

    @OnClick(R.id.btn_volume_down)
    public void onVolumeDecrease() {
        int currentLevel = audioAppManager.getVolumeLevel();
        currentLevel -= m_level;
        updateVolume(currentLevel);
    }

    private void updateVolume(int level) {
        audioAppManager.setVolumeLevel(level);
        if (mSeekBar != null) {
            mSeekBar.setProgress(audioAppManager.getVolumeLevel());
            mSeekBar.invalidate();
        }
    }

    private void initVolumeControl() {
        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return false;
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (mSeekBar != null) {
                    mSeekBar.setProgress(audioAppManager.getVolumeLevel());
                    mSeekBar.invalidate();
                }
            }
        };
        getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, contentObserver);
        mSeekBar.setMax(audioAppManager.getMaxLevel());
        mSeekBar.setProgress(audioAppManager.getVolumeLevel());
        m_level = audioAppManager.getMaxLevel() / NUM_PARTS;

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                audioAppManager.setVolumeLevel(progress);
            }
        });
    }

    private void initMoveDetector() {
        moveDetector = new MoveDetector(() -> {
            getAndSendLocation();
            starPlayMoveSound();
        });
    }

    private void starPlayMoveSound() {
        playHelper.play();
        audioAppManager.setVolumeLevel(audioAppManager.getMaxLevel());
    }

    private void getAndSendLocation() {
        if (PermissionUtil.checkLocationPermission(PERMISSIONS_REQUEST_LOCATION, this)) {
            if (googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "send location data");
        if (location != null) {
            dataManager.saveCoordinateInStatus(String.valueOf(location.getLatitude()),
                    String.valueOf(location.getLongitude()));
            dataManager.sendNotificationToAdmin(location.getLatitude(), location.getLongitude());

            //Start recording
            this.startService(new Intent(this, BackgroundVideoRecordingService.class));
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    private void setSendingConnSetting() {
        if (progress) return;
        progress = true;

        DatabaseReference connected_dev = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_DEVICES).child(dataManager.getDeviceId());
        ValueEventListener deviceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserConnectionStatus status = dataSnapshot.getValue(UserConnectionStatus.class);
                if (status != null) updateConnectionStatus(status);
                progress = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress = false;
            }
        };
        connected_dev.addListenerForSingleValueEvent(deviceListener);
        registerFbListener(connected_dev, deviceListener);
    }

    private void updateConnectionStatus(UserConnectionStatus status) {
        if (status != null) {
            status.strSong = songName;
            status.isPlaying = isPlaying;
            status.remain = remainTime;
            int volume_level = audioAppManager.getVolumeLevel();
            status.volume = String.valueOf(volume_level);
            if (status.gpsEnabled != mIs_GPSEnabled) {
                if (status.gpsEnabled) {
                    Toast.makeText(NewPlayerActivity.this,
                            NewPlayerActivity.this.getString(R.string.gps_enabled),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NewPlayerActivity.this,
                            NewPlayerActivity.this.getString(R.string.gps_disabled),
                            Toast.LENGTH_SHORT).show();
                }

            }
            mIs_GPSEnabled = status.gpsEnabled;
            actionBar.setGpsSignal(mIs_GPSEnabled);
            moveDetector.setGPSEnabled(mIs_GPSEnabled);
        }
        dataManager.saveStatus(status);
    }

    private void getMusicsFromStorage() {

        if (!PermissionUtil.checkReadPermission(this)) return;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor mCursor = getContentResolver().query(
                uri,
                new String[]{MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DATA},
                MediaStore.Audio.Media.IS_MUSIC + " != 0"
                        + " AND "
                        + MediaStore.MediaColumns.MIME_TYPE + " ='audio/mpeg'",
                null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        listMusics.clear();
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                MusicInfo musicInfo = new MusicInfo();
                musicInfo.mStr_musicName = mCursor.getString(0);
                musicInfo.mStr_musicPath = mCursor.getString(1);
                musicInfo.mIs_enabled = true;
                listMusics.add(musicInfo);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) mCursor.close();
    }

    private void initUI() {
        DatabaseReference settingRef = FirebaseDatabase.getInstance().getReference()
                .child(AppConstant.NODE_SETTING).child(dataManager.getDeviceId());
        ValueEventListener settingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                phoneSettings = dataSnapshot.getValue(PhoneSettings.class);
                if (phoneSettings != null) {
                    NotificationMessage message = new NotificationMessage(false, new Time(phoneSettings.startTime),
                            new Time(phoneSettings.endTime), phoneSettings.songInterval, phoneSettings.pauseInterval);
                    notificationUtils.showNotificationMessage(message.getJsonObject());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        settingRef.addListenerForSingleValueEvent(settingListener);
        registerFbListener(settingRef, settingListener);
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

    @SuppressLint("DefaultLocale")
    public void setRemainTime(int remainTime) {
        if (remainTime == -1) {
            mTxt_remainTime.setText("--");
        } else {
            mTxt_remainTime.setText(String.format("%02d:%02d",
                    TimeUnit.SECONDS.toMinutes(remainTime),
                    remainTime - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(remainTime))
            ));
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
        actionBar.setGpsSignal(isGPSenabled);
        moveDetector.setGPSEnabled(isGPSenabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerDestroy();
        TimerWakeLock.releaseCpuLock();
        getContentResolver().unregisterContentObserver(contentObserver);
        compositeSubscription.clear();
    }

    private void playerDestroy() {
        if (playSongs != null) {
            playSongs.onDestroy();
            playSongs = null;
            songName = "";
            setSongName("--");
            setAppStatus("--");
            setRemainTime(-1);
        }
    }

    private void playNow(Intent playerInfo) {
        if (playSongs == null) {
            playSongs = new PlaySongsN((status, remainTime) -> updateStatus(status, remainTime));
            playSongs.onStartCommand(playerInfo);
            if (playSongs.play()) {
                setSongName(playSongs.getFileName(m_currentPlayingSongIndex));
                songName = (m_currentPlayingSongIndex + 1) + ". " + playSongs.getFileName(m_currentPlayingSongIndex);
            }
        } else {
            playSongs.resume();
        }
    }

    public void updateStatus(final int status, final int remainTime) {
        if (status != PlaySongsN.STATUS_PLAYING) {
            setAppStatus(AppConstant.PAUSE);
            setSongName("--");
            songName = "";
            mTxt_appStatus.setTextColor(ContextCompat.getColor(this, R.color.pause_text_color));
            mTxt_remainTime.setTextColor(ContextCompat.getColor(this, R.color.pause_text_color));
        } else {
            setAppStatus(AppConstant.PLAYING);
            setSongName(playSongs.getFileName(m_currentPlayingSongIndex));
            songName = (m_currentPlayingSongIndex + 1) + ". " + playSongs.getFileName(m_currentPlayingSongIndex);
            mTxt_appStatus.setTextColor(ContextCompat.getColor(this, R.color.play_text_color));
            mTxt_remainTime.setTextColor(ContextCompat.getColor(this, R.color.play_text_color));
        }
        setRemainTime(remainTime);
    }

    private void setPlayerInfo(Intent intent) {
        int cateogry_id = intent.getIntExtra(AppConstant.INTENT_CATEGORY, 0);
        Object obj = intent.getSerializableExtra(AppConstant.FIELD_MESSAGE_DATA);
        NotificationMessage messageData;
        if (obj instanceof NotificationMessage) {
            messageData = (NotificationMessage) obj;
        } else if (obj instanceof String) {
            messageData = new NotificationMessage((String) obj);
        } else {
            return;
        }
        intent.removeExtra(AppConstant.FIELD_MESSAGE_DATA);
        intent.putExtra(AppConstant.FIELD_MESSAGE_DATA, messageData);
        switch (cateogry_id) {
            case AppConstant.INTENT_START: {
                playNow(intent);

                //need to set the alarm for next day as well.
                messageData.clearRealStartTime();
                StartTime.setAlarm(this, messageData);
            }
            break;
            case AppConstant.INTENT_UPDATE:
                //stop the current player if there is any update from the
                //notification and reset the counter back to 0
                playerDestroy();
                m_currentPlayingSongIndex = 0;

                if (isPlayNow(messageData)) {
                    playNow(intent); //need to offset the seconds for this
                }
                StartTime.setAlarm(this, messageData);
                break;
        }

        mTxt_startTime.setText(String.format("%s - %s", messageData.getStartTime().convertString(),
                messageData.getEndTime().convertString()));
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

    public void buildGoogleApiClient() {
        stopLocationUpdate();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    public void stopLocationUpdate() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
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
            } else {
                getMusicsFromStorage();
            }
        }
    }

}
