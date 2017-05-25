package com.player.movedetector;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;
import com.player.AppConstant;
import com.player.PlayerApplication;
import com.player.foreground.services.BackgroundVideoRecordingService;
import com.player.parseModel.ConnectionStatus;
import com.player.ui.activity.PlayerActivity;

import org.json.JSONObject;

import java.util.Date;

/**
 * desc : MoveDetector class for Accelerometer Sensor
 */
public class MoveDetector implements SensorEventListener {

    public static final double ACCELEROMETER_SENSITIVITY = 6.0;

    private float x;
    private float y;
    private float z;

    private float previousX;
    private float previousY;
    private float previousZ;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long previousUpdate;

    private static boolean mIs_GPSSetting = false;
    private LocationClient m_locationClient;
    private LocationRequest m_locationRequest;
    private boolean mIsSending = false;

    public MoveDetector(String deviceID) {
        init();
        getCountryInfoFromLocation(PlayerApplication.getContext());
    }

    public void setGPSEnabled(boolean isEnabled) {
        mIs_GPSSetting = isEnabled;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mIs_GPSSetting == true && PlayerActivity.mIs_GPSEnabled == true) {
            mAccelerometer = event.sensor;
            if (mAccelerometer.getType() == Sensor.TYPE_ACCELEROMETER) {
                long currentTime = System.currentTimeMillis();
                if(previousUpdate == 0){
                    previousUpdate = currentTime;
                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];
                    return;
                }
                if ((currentTime - previousUpdate) > 100) {
                    previousUpdate = currentTime;
                    previousX = x;
                    previousY = y;
                    previousZ = z;
                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];
                    Log.i("ACCERATION","x "+x+" y "+y+" z "+z);
                    if (Math.abs(previousX - x) > ACCELEROMETER_SENSITIVITY ||
                            Math.abs(previousY - y) > ACCELEROMETER_SENSITIVITY ||
                            Math.abs(previousZ - z) > ACCELEROMETER_SENSITIVITY) {
                        Log.i("ACCERATION2","over threshold "+"x "+x+" y "+y+" z "+z+ "  --- "+previousX+" "+previousY+" "+previousZ);
                        if (mIsSending == false && m_locationRequest != null) {
                            mIsSending = true;
                            m_locationClient.requestLocationUpdates(m_locationRequest, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    Log.i("ACCERATION2",location.toString());
                                    //if (mIs_GPSSetting == true && PlayerActivity.mIs_GPSEnabled == true) {
                                        m_locationClient.removeLocationUpdates(this);
                                        sendPushNotification(location.getLatitude(), location.getLongitude());
                                        saveData(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
										
										//Start recording
                                        Intent videoRecorderService = new Intent(PlayerApplication.getContext(), BackgroundVideoRecordingService.class);
                                        PlayerApplication.getContext().startService(videoRecorderService);
                                //    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }
    private void saveData(final String latitude,final String longitude){
        ParseQuery<ConnectionStatus> m_connectionQuery = ConnectionStatus.getQuery();
        m_connectionQuery.whereEqualTo(AppConstant.FIELD_DEVICE_ID, ParseUser.getCurrentUser().getObjectId());
        m_connectionQuery.cancel();
        m_connectionQuery.getFirstInBackground(new GetCallback<ConnectionStatus>() {
            @Override
            public void done(ConnectionStatus conInfo, ParseException e) {
                if (conInfo != null) {
                    conInfo.setLatitude(latitude);
                    conInfo.setLongitude(longitude);

                    conInfo.saveInBackground();

                }
            }
        });

    }
    /**
     * @param latitude
     * @param longitude
     * @desc Send notification to admin device for location change warning
     */
    private void sendPushNotification(double latitude, double longitude) {
        ParsePush push = new ParsePush();
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo(AppConstant.FIELD_APP_NAME, AppConstant.SERVER);
        JSONObject json_data = new JSONObject();
        try {
            json_data.put(AppConstant.FIELD_LATITUDE, latitude + ",");
            json_data.put(AppConstant.FIELD_LONGITUDE, longitude);
            Date now = new Date();
            int a = now.getHours();
            int b = now.getMinutes();

            json_data.put(AppConstant.FIELD_TIME, a + ":" + b);
            json_data.put(AppConstant.FIELD_DEVICE_NAME, ParseUser.getCurrentUser().getUsername());
            PlayerApplication.showToast(latitude + ", " + longitude, Toast.LENGTH_SHORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        push.setQuery(query);
        push.setData(json_data);
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                mIsSending = false;
            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void init() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
        this.previousUpdate = 0l;

        mSensorManager = (SensorManager) PlayerApplication.getContext().getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void getCountryInfoFromLocation(final Context context) {
        m_locationClient = new LocationClient(context, new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                m_locationRequest = LocationRequest.create();
            }

            @Override
            public void onDisconnected() {
            }
        }, new GooglePlayServicesClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
            }
        });
        m_locationClient.connect();
    }
}