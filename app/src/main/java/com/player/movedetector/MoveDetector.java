package com.player.movedetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.player.PlayerApplication;
import com.player.ui.activity.NewPlayerActivity;

import javax.inject.Inject;

/**
 * desc : MoveDetector class for Accelerometer Sensor
 */
public class MoveDetector implements SensorEventListener {

    private static final double ACCELEROMETER_SENSITIVITY = 6.0;
    private static final String TAG = MoveDetector.class.getSimpleName();
    private static boolean mIs_GPSSetting = false;

    @Inject
    Context context;

    private float x;
    private float y;
    private float z;
    private float previousX;
    private float previousY;
    private float previousZ;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long previousUpdate;
    private SendLocationListener listener;

    public MoveDetector(SendLocationListener listener) {
        PlayerApplication.getAppComponent().inject(this);
        this.listener = listener;
        init();
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

    public void setGPSEnabled(boolean isEnabled) {
        mIs_GPSSetting = isEnabled;
    }

    public void setListener(@Nullable SendLocationListener listener){
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mIs_GPSSetting && NewPlayerActivity.mIs_GPSEnabled) {
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
                    if (Math.abs(previousX - x) > ACCELEROMETER_SENSITIVITY ||
                            Math.abs(previousY - y) > ACCELEROMETER_SENSITIVITY ||
                            Math.abs(previousZ - z) > ACCELEROMETER_SENSITIVITY) {
                        if (listener != null) {
                            Log.i(TAG, "call location");
                            listener.sendLocation();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface SendLocationListener {
        void sendLocation();
    }
}