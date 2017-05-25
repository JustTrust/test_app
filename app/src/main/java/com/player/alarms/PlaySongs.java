/**
 *
 */
package com.player.alarms;

import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

import com.player.AppConstant;
import com.player.model.MusicInfo;
import com.player.model.NotificationMessage;
import com.player.model.Time;
import com.player.ui.activity.PlayerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @desc PlaySongs class for handle medai player instance
 */
@SuppressWarnings("unchecked")
public class PlaySongs {
    private ArrayList<MusicInfo> mlst_musics = PlayerActivity.mlst_Musics;
    private Time m_endTime;
    private Time m_startTime;
    private int m_intervalSong, m_intervalPause;
    private int m_onePassedTime = 0;
    private int m_totalPassedTime = 0;
    private int m_totalTime;
    private MediaPlayer m_player;
    private long mSongStarted = 0;

    public int mPlayerStatus = -1;

    public UpdateCallDurationTask mDurationTask;
    public static Timer mTimer;
    private long t;
    private Time m_currentPauseTime;
    private int minuteStartTime;
    private int minuteEndTime;

    public PlaySongs() {

    }

    private Subscription updateSubscription;
    Subscription playPauseSubscription;

    /**
     * @param intent
     * @desc onStartCommand for initialize timer task & total time object
     */
    public void onStartCommand(Intent intent) {

        NotificationMessage playerInfo = (NotificationMessage) intent.getSerializableExtra(AppConstant.FIELD_MESSAGE_DATA);
        m_endTime = playerInfo.getEndTime();
        m_startTime = playerInfo.getStartTime();
        m_currentPauseTime = playerInfo.getStartTime().clone();

        minuteStartTime = m_startTime.getHour() * 60 + m_startTime.getMinute();
        minuteEndTime  = m_endTime.getHour() * 60 + m_endTime.getMinute();
        Log.e("STARTING PLAY", "PLAYYY "+m_startTime.getHour()+":"+m_startTime.getMinute());
        if (m_endTime.getHour() < m_startTime.getHour()) {
            m_totalTime = (24 - (m_startTime.getHour() - m_endTime.getHour())) * 60 + (m_endTime.getMinute() - m_startTime.getMinute());

        } else {
            m_totalTime = (m_endTime.getHour() - m_startTime.getHour()) * 60 + (m_endTime.getMinute() - m_startTime.getMinute());

        }
        m_totalTime = 60 * m_totalTime;  //convert minutes to second
        Log.e("Total_time", m_totalTime + "");
        m_intervalSong = playerInfo.getSongInterval();
        m_intervalPause = playerInfo.getPauseInterval();

        if(m_currentPauseTime.getHour() < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            m_currentPauseTime.setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
            m_currentPauseTime.setMinute(Calendar.getInstance().get(Calendar.MINUTE));
        }
        else
        if(m_currentPauseTime.getHour() == Calendar.getInstance().get(Calendar.HOUR_OF_DAY) && m_currentPauseTime.getMinute() < Calendar.getInstance().get(Calendar.MINUTE)) {
            m_currentPauseTime.setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
            m_currentPauseTime.setMinute(Calendar.getInstance().get(Calendar.MINUTE));
        }

        calculateNextTime(m_currentPauseTime,m_intervalSong);
        mTimer = new Timer();

        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 1000);

//        playing(AppConstant.PLAYING_START);
        play();
        updateSubscription = startUpdateTimer();
    }

    private void calculateNextTime(Time time, int delta){
        Calendar scheduledTime = Calendar.getInstance();
        scheduledTime.set(Calendar.HOUR_OF_DAY, time.getHour());
        scheduledTime.set(Calendar.MINUTE, time.getMinute());
        scheduledTime.set(Calendar.SECOND, 0);
        scheduledTime.set(Calendar.MILLISECOND,0);
        scheduledTime.add(Calendar.MINUTE, delta);
        time.setHour(scheduledTime.get(Calendar.HOUR_OF_DAY));
        time.setMinute(scheduledTime.get(Calendar.MINUTE));
    }


    private void play() {
        try {
            String filepath = getFilePath(AppConstant.START_FIRST_SONG);
            PlayerActivity.instance.setSongName(getFileName(PlayerActivity.m_currentPlayingSongIndex));

            mSongStarted = Calendar.getInstance().getTimeInMillis();
            m_player = null;
            m_player = new MediaPlayer();
            m_player.setDataSource(filepath);

            m_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playNext();
                }
            });
            m_player.prepare();
            m_player.start();
            mPlayerStatus = AppConstant.PLAYING_SAME;
            SendNotification();

            playPauseSubscription = startPauseTimer();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Subscription startUpdateTimer() {
        return Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<Long, Observable<Integer>>() {

                    @Override
                    public Observable<Integer> call(Long aLong) {
                        try {
                            Calendar instance = Calendar.getInstance();

                            if (minuteStartTime > minuteEndTime) {
                                if (instance.get(Calendar.HOUR_OF_DAY) > m_endTime.getHour() ||
                                        (instance.get(Calendar.HOUR_OF_DAY) == m_endTime.getHour() &&
                                                instance.get(Calendar.MINUTE) >= m_endTime.getMinute())) {

                                    if (instance.get(Calendar.HOUR_OF_DAY) < m_startTime.getHour() ||
                                            (instance.get(Calendar.HOUR_OF_DAY) == m_startTime.getHour() &&
                                                    instance.get(Calendar.MINUTE) < m_startTime.getMinute())) {

                                        stopPlaying();
                                    }
                                }
                            } else if (minuteStartTime < minuteEndTime) {
                                if (instance.get(Calendar.HOUR_OF_DAY) > m_endTime.getHour() ||
                                        (instance.get(Calendar.HOUR_OF_DAY) == m_endTime.getHour() &&
                                                instance.get(Calendar.MINUTE) >= m_endTime.getMinute())) {
                                    stopPlaying();
                                }
                            } else {

                                if ((instance.get(Calendar.HOUR_OF_DAY) * 60 + instance.get(Calendar.MINUTE)) == minuteEndTime) {  // interval 1 second

                                    stopPlaying();

                                }
                            }
                            if (instance.get(Calendar.HOUR_OF_DAY) > m_currentPauseTime.getHour() ||
                                    (instance.get(Calendar.HOUR_OF_DAY) == m_currentPauseTime.getHour() &&
                                            instance.get(Calendar.MINUTE) >= m_currentPauseTime.getMinute())) {
                                Log.e(PlaySongs.class.getName(), "pausing");
                                m_player.pause();
                                mPlayerStatus = AppConstant.PLAYING_STOP;
                                calculateNextTime(m_currentPauseTime, m_intervalSong + m_intervalPause);
                                playPauseSubscription.unsubscribe();
                                playPauseSubscription = startPlayTimer();
                            }
                            int time = m_intervalSong * 60 - (int) ((Calendar.getInstance().getTimeInMillis() - mSongStarted) / 1000);
                            return Observable.just(time);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Observable.error(e);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(PlaySongs.class.getName(), null, e);
                    }

                    @Override
                    public void onNext(Integer remainTime) {

                        switch (mPlayerStatus) {
                            case AppConstant.PLAYING_SAME:
                                PlayerActivity.instance.setAppStatus(AppConstant.PLAYING);
                                PlayerActivity.instance.setRemainTime(remainTime);
                                break;
                            case AppConstant.PLAYING_STOP:
                                PlayerActivity.instance.setAppStatus(AppConstant.PAUSE);
                                PlayerActivity.instance.setRemainTime(m_intervalPause * 60 + remainTime);
                                break;
                        }
                    }
                });
    }

    private Subscription startPauseTimer() {
//        return Observable.interval(m_intervalSong, TimeUnit.MINUTES)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .first()
//                .subscribe(new Subscriber() {
//                    @Override
//                    public void onCompleted() {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e(PlaySongs.class.getName(), null, e);
//                    }
//
//                    @Override
//                    public void onNext(Object t) {
//                        Log.e(PlaySongs.class.getName(), "pausing");
//                        m_player.pause();
//                        mPlayerStatus = AppConstant.PLAYING_STOP;
//
//                        playPauseSubscription.unsubscribe();
//                        playPauseSubscription = startPlayTimer();
//                    }
//
//                });
        return Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .first()
                .subscribe(new Subscriber() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(PlaySongs.class.getName(), null, e);
                    }

                    @Override
                    public void onNext(Object t) {
                        Calendar instance = Calendar.getInstance();
                        if (instance.get(Calendar.HOUR_OF_DAY) > m_currentPauseTime.getHour() ||
                                (instance.get(Calendar.HOUR_OF_DAY) == m_currentPauseTime.getHour() &&
                                        instance.get(Calendar.MINUTE) >= m_currentPauseTime.getMinute())) {
                            m_player.pause();
                            mPlayerStatus = AppConstant.PLAYING_STOP;

                            playPauseSubscription.unsubscribe();
                            playPauseSubscription = startPlayTimer();
                        }

                    }

                });
    }

    private Subscription startPlayTimer() {
        return Observable.interval(m_intervalPause, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .first()
                .subscribe(new Subscriber() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(PlaySongs.class.getName(), null, e);
                    }

                    @Override
                    public void onNext(Object t) {
                        Log.e(PlaySongs.class.getName(), "starting");
                        m_player.start();
                        mSongStarted = Calendar.getInstance().getTimeInMillis();
                        mPlayerStatus = AppConstant.PLAYING_SAME;

                        playPauseSubscription.unsubscribe();
                        playPauseSubscription = startPauseTimer();
                    }
                });
    }

    private void playNext() {
        try {
            String filePath = getFilePath(PlayerActivity.m_currentPlayingSongIndex);
            PlayerActivity.instance.setSongName(getFileName(PlayerActivity.m_currentPlayingSongIndex));

            m_player.stop();
            m_player.reset();
            m_player.setDataSource(filePath);
            m_player.prepare();
            m_player.start();
            SendNotification();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pausePlayer() {

    }

    /**
     * @desc : Destroy PlaySong object instance
     */
    public void onDestroy() {

        try {
            playPauseSubscription.unsubscribe();
        }catch (Exception e){

        }

        updateSubscription.unsubscribe();

        m_player.stop();
        Log.i("TIME_TYU","Stop time "+(System.currentTimeMillis() - t));
        mDurationTask.cancel();

        mTimer.cancel();
        PlayerActivity.instance.mStr_Song = "";
        PlayerActivity.instance.setSongName("--");
        PlayerActivity.instance.setAppStatus("--");
        PlayerActivity.instance.setRemainTime(-1);

    }

    /**
     * @param current : index of current song
     * @return
     * @desc get medai file path from sdcard
     */
    private String getFilePath(int current) {
        current++;
        while (current < mlst_musics.size()) {
            if (mlst_musics.get(current).mIs_enabled == true) {
                PlayerActivity.m_currentPlayingSongIndex = current;
                return mlst_musics.get(current).mStr_musicPath;
            }
            current++;
        }
        return getFilePath(AppConstant.START_FIRST_SONG); //startagain
    }

    /**
     * @param index : index of media file
     * @return Name of media file name
     * @desc get media file name base on index
     */
    private String getFileName(int index) {
        return mlst_musics.get(index).mStr_musicName;
    }

    /**
     * #Desc : Play media player object
     *
     * @param type
     */
    private void playing(int type) {


        try {
            switch (type) {
                case AppConstant.PLAYING_START: {
                    String filepath = getFilePath(AppConstant.START_FIRST_SONG);
                    PlayerActivity.instance.setSongName(getFileName(PlayerActivity.m_currentPlayingSongIndex));
                    m_player = new MediaPlayer();
                    m_player.setDataSource(filepath);
                    m_player.prepare();
                    m_player.start();
                    SendNotification();
                }
                break;
                case AppConstant.PLAYING_SAME: {
                    if (m_player.isPlaying() != true) {
                        m_player.start();
                    }
                    int remainTime = 60 * m_intervalSong - m_onePassedTime;
                    PlayerActivity.instance.setAppStatus(AppConstant.PLAYING);
                    PlayerActivity.instance.setRemainTime(remainTime);
                }
                break;
                case AppConstant.PLAYING_STOP: {
                    m_player.stop();
                    int remainTime = 60 * (m_intervalSong + m_intervalPause) - m_onePassedTime;
                    PlayerActivity.instance.setAppStatus(AppConstant.PAUSE);
                    PlayerActivity.instance.setRemainTime(remainTime);
                }
                break;
                case AppConstant.PLAYING_NEXT: {
                    String filePath = getFilePath(PlayerActivity.m_currentPlayingSongIndex);
                    PlayerActivity.instance.setSongName(getFileName(PlayerActivity.m_currentPlayingSongIndex));
                    m_player = new MediaPlayer();
                    m_player.setDataSource(filePath);
                    m_player.prepareAsync();
                    m_player.start();
                    SendNotification();
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Desc : Handel Play status
     */
    private void checkingPlayStatus() {
//        if (m_onePassedTime < 60 * m_intervalSong) {
//            playing(AppConstant.PLAYING_SAME);
//        } else if ((60 * m_intervalSong <= m_onePassedTime) && (m_onePassedTime <= 60 * (m_intervalSong + m_intervalPause))) {
//            playing(AppConstant.PLAYING_STOP);
//        } else if (m_onePassedTime > 60 * (m_intervalSong + m_intervalPause)) {
//            playing(AppConstant.PLAYING_NEXT);
//            m_onePassedTime = 0;
//        }
//        m_totalPassedTime++;
//        m_onePassedTime++;
//        if (m_totalPassedTime > m_totalTime) {
//            stopPlaying();
//        }
    }

    /**
     * Desc : Stop Media player instance & Destroy it
     */
    public void stopPlaying() {
        onDestroy();
    }

    public class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            PlayerActivity.instance.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkingPlayStatus();
                }
            });
        }
    }

    /**
     * @desc : Sendnotificaion to main view
     */
    public void SendNotification() {

        String str_songInfo = (PlayerActivity.m_currentPlayingSongIndex + 1) + ". " + mlst_musics.get(PlayerActivity.m_currentPlayingSongIndex).mStr_musicName;
        PlayerActivity.instance.mStr_Song = str_songInfo;
    }
}