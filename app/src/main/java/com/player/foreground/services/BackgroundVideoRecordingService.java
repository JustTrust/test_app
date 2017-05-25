package com.player.foreground.services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundVideoRecordingService extends Service{

    private static final int RECORD_DURATION = 30;//secs
    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private Camera camera = null;
    private boolean hasFrontCamera = false;
    private boolean hasBackCamera = false;
    private int idFrontCamera = -1;
    private int idBackCamera = -1;
    private int idCurrentCamera = -1;
    private MediaRecorder mediaRecorder = null;
    private boolean isRecording;
    private String filepath;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Dont start recording is already recording.
        if(!isRecording)
            initRecorder();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startTimer(){
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopSelf();
            }
        },RECORD_DURATION * 1000);
    }

    @Override
    public void onDestroy() {

        stopRecorder();

    }

    private void stopRecorder(){

        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();

        camera.lock();
        camera.release();

        windowManager.removeView(surfaceView);

        isRecording = false;
    }

    private boolean initCamera() {
        // check if the device has camera
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            // Search for the front facing camera
            int numberOfCameras = Camera.getNumberOfCameras();

            for (int i = 0; i < numberOfCameras; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    idFrontCamera = i;
                    hasFrontCamera = true;
                }
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    idBackCamera = i;
                    hasBackCamera = true;
                }
            }

            if(hasFrontCamera)idCurrentCamera = idFrontCamera;

            if(hasBackCamera)idCurrentCamera = idBackCamera;

            return true;

        } else {
            return false;
        }
    }

    private void initRecorder(){


        if(!initCamera()) {

            //TODO Add some exception handling code here
            return;
        }

        // Create new SurfaceView, set its size to 1x1, move it to the top left corner and set this service as a callback
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        surfaceView = new SurfaceView(this);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );

        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;

        windowManager.addView(surfaceView, layoutParams);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

                try {

                    camera = Camera.open(idCurrentCamera);
                    mediaRecorder = new MediaRecorder();
                    camera.unlock();

                    mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                    mediaRecorder.setCamera(camera);
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

                    filepath = Environment.getExternalStoragePublicDirectory (Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +"/"+
                            DateFormat.format("yyyy-MM-dd_HH-mm-ss", new Date().getTime())+
                            ".mp4";

                    mediaRecorder.setOutputFile(filepath);
                    mediaRecorder.prepare();
                    mediaRecorder.start();

                    isRecording = true;

                    startTimer();

                } catch (Exception e) {

                    e.printStackTrace();

                    //TODO Add some exception handling code here

                    return;
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }
}
