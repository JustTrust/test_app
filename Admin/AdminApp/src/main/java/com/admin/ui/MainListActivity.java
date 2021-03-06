package com.admin.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.admin.AdminApplication;
import com.admin.AppConstant;
import com.admin.R;
import com.admin.model.NotificationMessage;
import com.admin.model.PhoneSettings;
import com.admin.model.Time;
import com.admin.model.UserConnectionStatus;
import com.admin.ui.adapters.PlayerAppListAdapter;
import com.admin.util.DataManager;
import com.admin.util.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @desc MainListActivity for list of register player device
 */

public class MainListActivity extends BaseActivity {

    @BindView(R.id.imgLeft)
    ImageView imgLeft;
    @BindView(R.id.txtRight)
    TextView txtRight;
    @BindView(R.id.lltHeaderActionbar)
    LinearLayout lltHeaderActionbar;
    @BindView(R.id.lst_playerApps)
    ListView mLst_playerApps;

    @Inject
    DataManager dataManager;
    private PlayerAppListAdapter listAdapter;
    private ArrayList<UserConnectionStatus> playersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_list_activity);
        ButterKnife.bind(this);
        AdminApplication.getAppComponent().inject(this);

        initUI();
        getPlayersInfo();
        authenticateUser();
    }

    private void authenticateUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tryToLogin();
        }
    }

    private void tryToLogin() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(AppConstant.ADMIN_EMAIL, AppConstant.ADMIN_PASS)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        registerNewUser();
                    }
                });
    }

    private void registerNewUser() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(AppConstant.ADMIN_EMAIL, AppConstant.ADMIN_PASS)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(MainListActivity.this, task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getPlayersInfo() {
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference().child(AppConstant.NODE_DEVICES);
        ChildEventListener deviceListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                playersList.add(dataSnapshot.getValue(UserConnectionStatus.class));
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                UserConnectionStatus status = dataSnapshot.getValue(UserConnectionStatus.class);
                int index = playersList.indexOf(status);
                if (index > -1) {
                    playersList.set(index, status);
                } else {
                    playersList.add(status);
                }
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                playersList.remove(dataSnapshot.getValue(UserConnectionStatus.class));
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        deviceRef.addChildEventListener(deviceListener);
        registerFbListener(deviceRef, deviceListener);
    }

    private void initUI() {
        listAdapter = new PlayerAppListAdapter(playersList, (deviceID, isChecked) ->
                dataManager.setGpsStatus(deviceID, isChecked));
        mLst_playerApps.setAdapter(listAdapter);
    }

    @OnClick(R.id.txtRight)
    void onTextClick() {
        showDialog();
    }

    @OnClick(R.id.imgLeft)
    void onImgClick() {
        Intent map_intent = new Intent(MainListActivity.this, MapsActivity.class);
        startActivity(map_intent);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View viewRoot = inflater.inflate(R.layout.dialog_setup_time, null);

        final EditText etSongInterval = (EditText) viewRoot.findViewById(R.id.edit_songInterval);
        final TextView tvStartTime = (TextView) viewRoot.findViewById(R.id.txt_startTime);
        final TextView tvEndTime = (TextView) viewRoot.findViewById(R.id.txt_endTime);

        tvStartTime.setOnClickListener(v -> Utils.getTime((TextView) v, this));
        tvEndTime.setOnClickListener(v -> Utils.getTime((TextView) v, this));

        builder.setView(viewRoot);
        builder.setPositiveButton("set", (dialog, which) -> {

            final String str_startTime = tvStartTime.getText().toString();
            final String str_endTime = tvEndTime.getText().toString();
            final String str_songInterval = etSongInterval.getText().toString();

            if (str_startTime.isEmpty() || str_endTime.isEmpty() || str_songInterval.isEmpty()) {
                Toast.makeText(MainListActivity.this, R.string.empty_field, Toast.LENGTH_LONG).show();
            } else {

                List<UserConnectionStatus> connectedDevices = new ArrayList<>();
                for (UserConnectionStatus connectionStatus : playersList) {
                    if (Utils.isConnected(connectionStatus.createdAt)) {
                        connectedDevices.add(connectionStatus);
                    }
                }

                if (connectedDevices.isEmpty()) {
                    Toast.makeText(MainListActivity.this, R.string.no_connected_device, Toast.LENGTH_LONG).show();
                } else {
                    int songInterval = Integer.valueOf(str_songInterval);
                    int pauseInterval = Integer.valueOf(str_songInterval) * (connectedDevices.size() - 1);
                    final String str_pauseInterval = String.valueOf(pauseInterval);

                    Calendar calendar = Calendar.getInstance();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                    try {
                        Date date = dateFormat.parse(str_startTime);
                        calendar.setTime(date);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }

                    Calendar currentTime = Calendar.getInstance();
                    int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                    int currentMin = currentTime.get(Calendar.MINUTE);
                    Time realStartTime = null;
                    boolean beforeCurrentTime = false;

                    if (currentHour > calendar.get(Calendar.HOUR_OF_DAY) || (currentHour == calendar.get(Calendar.HOUR_OF_DAY) && currentMin >= calendar.get(Calendar.MINUTE))) {
                        beforeCurrentTime = true;
                    }

                    for (UserConnectionStatus device : connectedDevices) {
                        final String mStr_DeviceID = device.deviceID;
                        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        final int minute = calendar.get(Calendar.MINUTE);
                        final Time startTime = new Time(hour, minute);

                        Time endTime = new Time(str_endTime);

                        PhoneSettings deviceSettings = new PhoneSettings();
                        deviceSettings.endTime = str_endTime;
                        deviceSettings.songInterval = str_songInterval;
                        deviceSettings.pauseInterval = str_pauseInterval;
                        deviceSettings.startTime = startTime.convertString();
                        deviceSettings.deviceId = mStr_DeviceID;
                        dataManager.saveSettings(deviceSettings);

                        currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                        currentMin = currentTime.get(Calendar.MINUTE);

                        if (beforeCurrentTime) {
                            realStartTime = new Time(currentHour, currentMin);
                        }

                        NotificationMessage message = new NotificationMessage(false, startTime, endTime, str_songInterval, str_pauseInterval, realStartTime);
                        dataManager.sendPushNotification(message, mStr_DeviceID);

                        calendar.add(Calendar.MINUTE, songInterval);//TODO 1st error pauseInterval used instead of songInterval
                        currentTime.add(Calendar.MINUTE, songInterval);
                    }
                }
            }
        });

        builder.setNegativeButton("cancel", (dialog, which) -> {
        });

        builder.create().show();
    }

}
