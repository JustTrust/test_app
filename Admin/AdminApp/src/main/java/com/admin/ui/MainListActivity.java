package com.admin.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.admin.AdminApplication;
import com.admin.AppConstant;
import com.admin.R;
import com.admin.model.NotificationMessage;
import com.admin.model.Time;
import com.admin.parsemodel.ConnectionStatus;
import com.admin.parsemodel.DeviceSettings;
import com.admin.util.DataManager;
import com.admin.util.Utils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;


/**
 * @desc MainListActivity for list of register player device
 */
public class MainListActivity extends FragmentActivity implements View.OnClickListener {

    private static final String LOG_TAG = MainListActivity.class.getName();

    public static MainListActivity instance;
    private ListView mLst_playerApps;
    private PlayerAppListAdapter mAdpt_playerApps;
    private ArrayList<ConnectionStatus> mArlst_players;
    private static ArrayList<String> mAry_listMessages = new ArrayList<>();
    private boolean m_isCompleted = true;

    public static Map<ConnectionStatus, DeviceSettings> mIsDeviceScheduleSetUpMap = new ArrayMap<>();
    private Handler mMainThreadHandler = new Handler();
    ImageView imgLeft;
    TextView txtRight;
    ProgressDialog pDialog;

    @Inject
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_list_activity);
        AdminApplication.getAppComponent().inject(this);
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        pDialog = new ProgressDialog(MainListActivity.this );
        pDialog.setMessage("Processing...");
        pDialog.setCancelable(false);
        if(pDialog!=null)
            pDialog.show();

        initUI();
        getPlayersInfo();
    }

    private void getPlayersInfo() {
        ParseQuery<ConnectionStatus> query = ConnectionStatus.getQuery();
        query.findInBackground(new FindCallback<ConnectionStatus>() {

            @Override
            public void done(List<ConnectionStatus> list_connections, ParseException e) {
                if((pDialog!=null)&&pDialog.isShowing()) {
                    pDialog.dismiss();
                    pDialog = null;
                }

                if (e == null) {
                    mArlst_players = (ArrayList<ConnectionStatus>) list_connections;
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            fillDeviceScheduleMapSynchronous();
                            mMainThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mAdpt_playerApps.notifyDataSetChanged();
                                }
                            });
                            mMainThreadHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getPlayersInfo();
                                }
                            }, 10000);
                        }

                    }).start();
                } else {
                    Toast.makeText(MainListActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void fillDeviceScheduleMapSynchronous() {
        mIsDeviceScheduleSetUpMap.clear();
        ParseQuery<DeviceSettings> deviceSettingsParseQuery = ParseQuery.getQuery(DeviceSettings.class);
        try {
            List<DeviceSettings> devicesSettings = deviceSettingsParseQuery.find();
            for (ConnectionStatus connectionStatus : mArlst_players) {
                String connectionDeviceId = connectionStatus.getDeviceID();
                for (DeviceSettings settings : devicesSettings) {
                    if (settings != null) {
                        String settingsDeviceId = settings.getDeviceId();
                        if (settingsDeviceId != null && settingsDeviceId.equals(connectionDeviceId) &&
                                !TextUtils.isEmpty(settings.getStartTime()) &&
                                !TextUtils.isEmpty(settings.getEndTime())) {
                            mIsDeviceScheduleSetUpMap.put(connectionStatus, settings);
                        }
                    }
                }
            }
        } catch (ParseException e1) {
            Log.e(LOG_TAG, e1.toString());
        }
    }


    private void initUI() {
        instance = this;
        mLst_playerApps = (ListView) this.findViewById(R.id.lst_playerApps);
        txtRight        = (TextView) this.findViewById(R.id.txtRight);
        imgLeft         = (ImageView) this.findViewById(R.id.imgLeft);
        mAdpt_playerApps = new PlayerAppListAdapter();
        mArlst_players = new ArrayList<ConnectionStatus>();
        mLst_playerApps.setAdapter(mAdpt_playerApps);
        txtRight.setOnClickListener(this);
        imgLeft.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.txtRight){
            showDialog();
        }
        if(v.getId()==R.id.imgLeft){
            Intent map_intent = new Intent(MainListActivity.this, MapsActivity.class);

            startActivity(map_intent);
        }

    }

    class ViewHolder {

        TextView txt_deviceName;
        TextView txt_connStatus;
        ToggleButton toggle_GPS;
    }

    private class PlayerAppListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mArlst_players.size();
        }

        @Override
        public ConnectionStatus getItem(int position) {
            return mArlst_players.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            final ViewHolder holder;
            if (view == null) {
                holder = new ViewHolder();
                view = View.inflate(MainListActivity.this, R.layout.cell_player_list, null);
                holder.txt_deviceName = (TextView) view.findViewById(R.id.txt_deviceName);
                holder.txt_connStatus = (TextView) view.findViewById(R.id.txt_connStatus);
                holder.toggle_GPS = (ToggleButton) view.findViewById(R.id.toggle_GPS);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.txt_deviceName.setText(mArlst_players.get(position).getDeviceName());

            boolean isSettingsAvailableForDevice;

            if (Utils.isConnected(mArlst_players.get(position).getUpdatedAt())) {
                ConnectionStatus status = mArlst_players.get(position);
                int remainTime = status.getRemainTime();

                if (remainTime > 0) {
                    if (mArlst_players.get(position).isPlaying()) {
                        holder.txt_connStatus.setText(Html.fromHtml(getString(R.string.playing)));
                    } else {
                        holder.txt_connStatus.setText(Html.fromHtml(getString(R.string.pause)));
                    }
                } else {
                    if (mIsDeviceScheduleSetUpMap.containsKey(status)) {
                        holder.txt_connStatus.setText(Html.fromHtml(getString(R.string.pause)));
                    } else {
                        holder.txt_connStatus.setText(Html.fromHtml(getString(R.string.connected)));
                    }
                }
                isSettingsAvailableForDevice = true;
            } else {
                holder.txt_connStatus.setText(Html.fromHtml(getString(R.string.disconnected)));
                isSettingsAvailableForDevice = false;
            }

            holder.toggle_GPS.setOnCheckedChangeListener(null);
            if (m_isCompleted) {
                holder.toggle_GPS.setChecked(mArlst_players.get(position).getGPSEnabled());
            }

            if (isSettingsAvailableForDevice) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainListActivity.this, PlayerSettingActivity.class);
                        intent.putExtra(AppConstant.FIELD_DEVICE_ID, mArlst_players.get(position).getDeviceID());
                        startActivity(intent);
                    }
                });
                holder.toggle_GPS.setEnabled(true);
            } else {
                view.setOnClickListener(null);
                holder.toggle_GPS.setEnabled(false);
            }

            holder.toggle_GPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    m_isCompleted = false;
                    JSONObject jsonSendData = new JSONObject();
                    mArlst_players.get(position).setGPSEnabled(isChecked);
                    mArlst_players.get(position).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            m_isCompleted = true;
                        }
                    });
                }
            });

            return view;
        }
    }

    private void sendNotification(JSONObject jsonData, String deviceID) {
        ParsePush push = new ParsePush();
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo(AppConstant.FIELD_DEVICE_ID, deviceID);
        push.setQuery(query);
        push.setData(jsonData);
        push.sendInBackground();
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
            showDialog();
            return true;
        }
        if (id == android.R.id.home) {
            //do your stuff here
            System.out.println("HOME");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View viewRoot = inflater.inflate(R.layout.dialog_setup_time, null);

        final EditText etSongInterval = (EditText)viewRoot.findViewById(R.id.edit_songInterval);
        final TextView tvStartTime  = (TextView) viewRoot.findViewById(R.id.txt_startTime);
        final TextView tvEndTime  = (TextView) viewRoot.findViewById(R.id.txt_endTime);

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

                if (str_startTime.isEmpty() || str_endTime.isEmpty() || str_songInterval.isEmpty()){
                    Toast.makeText(MainListActivity.this,R.string.empty_field, Toast.LENGTH_LONG).show();
                }
                else {

                    List<ConnectionStatus> connectedDevices = new ArrayList<ConnectionStatus>();
                    for (ConnectionStatus connectionStatus : mArlst_players) {
                        if (Utils.isConnected(connectionStatus.getUpdatedAt())) {
                            connectedDevices.add(connectionStatus);
                        }
                    }

                    if (connectedDevices.isEmpty()){
                        Toast.makeText(MainListActivity.this,R.string.no_connected_device, Toast.LENGTH_LONG).show();
                    }
                    else {
                        int songInterval = Integer.valueOf(str_songInterval);
                        int pauseInterval = Integer.valueOf(str_songInterval) * (connectedDevices.size() - 1);
                        final String str_pauseInterval = String.valueOf(pauseInterval);

                        Calendar calendar = Calendar.getInstance();

                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                        try {
                            Date date = dateFormat.parse(str_startTime);
                            calendar.setTime(date);
                        }
                        catch (java.text.ParseException e) {
                        }

                        int index = 0;

                        Calendar currentTime = Calendar.getInstance();
                        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                        int currentMin = currentTime.get(Calendar.MINUTE);
                        Time realStartTime = null;
                        boolean beforeCurrentTime = false;

                        if(currentHour > calendar.get(Calendar.HOUR_OF_DAY) || (currentHour == calendar.get(Calendar.HOUR_OF_DAY) && currentMin >= calendar.get(Calendar.MINUTE))) {
                            beforeCurrentTime = true;
                        }

                        for (ConnectionStatus device : connectedDevices) {
                            final String mStr_DeviceID = device.getDeviceID();

                            final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            final int minute = calendar.get(Calendar.MINUTE);

                           /* minute = minute + (index * songInterval);

                            if(minute >= 60) {
                                hour++;
                                minute = minute - 60;

                                if(hour >= 24) {
                                    hour = hour - 24;
                                }
                            }*/

                            index++;

                            final Time startTime = new Time(hour,minute);
//                            Time startTime = new Time();
//                            startTime.parseData(str_startTime);

                            Time endTime = new Time();
                            endTime.parseData(str_endTime);

                            ParseQuery<DeviceSettings> deviceSettingsParseQuery = new ParseQuery<>(DeviceSettings.class);
                            deviceSettingsParseQuery.whereEqualTo(DeviceSettings.DEVICE_ID, mStr_DeviceID);
                            deviceSettingsParseQuery.getFirstInBackground(new GetCallback<DeviceSettings>() {
                                @Override
                                public void done(DeviceSettings object, ParseException e) {

                                        DeviceSettings deviceSettings;
                                        if (object == null){
                                            deviceSettings = new DeviceSettings();
                                        }
                                        else{
                                            deviceSettings = object;
                                        }
                                        deviceSettings.setEndTime(str_endTime);
                                        deviceSettings.setSongInterval(str_songInterval);
                                        deviceSettings.setPauseInterval(str_pauseInterval);
                                        //deviceSettings.setStartTime(hour + ":" + minute);
                                        deviceSettings.setStartTime(startTime.convertString()); //TODO my fix
                                        deviceSettings.setDeviceId(mStr_DeviceID);
                                        deviceSettings.saveEventually();

                                }
                            });


                            currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                            currentMin = currentTime.get(Calendar.MINUTE);

                            if(beforeCurrentTime) {
                                realStartTime = new Time(currentHour, currentMin);
                            }

                            NotificationMessage message = new NotificationMessage(false, startTime, endTime, str_songInterval, str_pauseInterval, realStartTime);
                            Log.i("SEND","startTime: "+startTime+" endTime: "+endTime+" songInterval "+str_songInterval+" pauseInterval "+str_pauseInterval);
                            Utils.sendPushNotification(MainListActivity.this, message, mStr_DeviceID);


                            calendar.add(Calendar.MINUTE, songInterval);//TODO 1st error pauseInterval used instead of songInterval
                            currentTime.add(Calendar.MINUTE, songInterval);
                        }
                    }
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


}
