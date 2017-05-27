package com.player.movedetector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.player.PlayerApplication;
import com.player.foreground.services.BackgroundVideoRecordingService;
import com.player.ui.activity.PlayerActivity;
import com.player.util.DataManager;

import javax.inject.Inject;

/**
 * desc : MoveDetector class for Accelerometer Sensor
 */
public class MoveDetector implements SensorEventListener {

    private static final double ACCELEROMETER_SENSITIVITY = 6.0;
    private static boolean mIs_GPSSetting = false;
    @Inject
    Context context;
    @Inject
    DataManager dataManager;

    private float x;
    private float y;
    private float z;
    private float previousX;
    private float previousY;
    private float previousZ;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long previousUpdate;
    private GoogleApiClient googleApiClient;
    private boolean mIsSending = false;

    public MoveDetector(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
        PlayerApplication.getAppComponent().inject(this);
        init();
    }

    public void setGPSEnabled(boolean isEnabled) {
        mIs_GPSSetting = isEnabled;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mIs_GPSSetting && PlayerActivity.mIs_GPSEnabled) {
            mAccelerometer = event.sensor;
            if (mAccelerometer.getType() == Sensor.TYPE_ACCELEROMETER) {
                long currentTime = System.currentTimeMillis();
                if (previousUpdate == 0) {
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
                    Log.i("ACCERATION", "x " + x + " y " + y + " z " + z);
                    if (Math.abs(previousX - x) > ACCELEROMETER_SENSITIVITY ||
                            Math.abs(previousY - y) > ACCELEROMETER_SENSITIVITY ||
                            Math.abs(previousZ - z) > ACCELEROMETER_SENSITIVITY) {
                        Log.i("ACCERATION1",
                                "over threshold " + "x " + x + " y " + y + " z " + z + "  --- " + previousX + " " + previousY + " " + previousZ);
                        if (!mIsSending && googleApiClient != null && googleApiClient.isConnected()) {
                            mIsSending = true;

                            @SuppressLint("MissingPermission")
                            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                            if (currentLocation != null) {
                                Log.i("ACCERATION2", "send data");
                                dataManager.saveCoordinateInStatus(String.valueOf(currentLocation.getLatitude()),
                                        String.valueOf(currentLocation.getLongitude()));
                                dataManager.sendNotificationToAdmin(currentLocation.getLatitude(), currentLocation.getLongitude());

                                //Start recording
                                context.startService(new Intent(context, BackgroundVideoRecordingService.class));
                            }
                            mIsSending = false;
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void init() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
        this.previousUpdate = 0l;

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}