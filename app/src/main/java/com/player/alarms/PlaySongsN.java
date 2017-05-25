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
import com.player.ui.activity.PlayerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


@SuppressWarnings("unchecked")
public class PlaySongsN {
    private ArrayList<MusicInfo> mlst_musics = PlayerActivity.mlst_Musics;
    private NotificationMessage playerInfo = null;
    private MediaPlayer m_player;

    public static final int STATUS_PLAYING = 0;
    public static final int STATUS_PAUSE = 1;
    public static final int STATUS_STOPPED = 2;
    public static final int STATUS_HARD_STOPPED = 3;
    public static final int STATUS_NONE = -1;
    private int mPlayerStatus = STATUS_NONE;
    private int minuteStartTime;
    private int minuteEndTime;

    public PlaySongsN() {

    }


    /**
     * @param intent
     * @desc onStartCommand for initialize timer task & total time object
     */
    public void onStartCommand(Intent intent) {

        playerInfo = (NotificationMessage) intent.getSerializableExtra(AppConstant.FIELD_MESSAGE_DATA);

        minuteStartTime = playerInfo.getStartTime().getHour() * 60 + playerInfo.getStartTime().getMinute();
        minuteEndTime  = playerInfo.getEndTime().getHour() * 60 + playerInfo.getEndTime().getMinute();
        play();

        if(playerInfo.getRealStartTime() != null) {
            playCounter = Calendar.getInstance().get(Calendar.SECOND);
        }
    }

    private void play() {
        try {
            String filepath = getFilePath(AppConstant.START_FIRST_SONG);
            PlayerActivity.instance.setSongName(getFileName(PlayerActivity.m_currentPlayingSongIndex));

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
            mPlayerStatus = STATUS_PLAYING;
            SendNotification();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            mPlayerStatus = STATUS_PLAYING;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pause() {
        mPlayerStatus = STATUS_PAUSE;
        m_player.pause();
    }

    private void stop() {
        mPlayerStatus = STATUS_STOPPED;
        m_player.pause();
    }

    public void resume() {
        mPlayerStatus = STATUS_PLAYING;
        m_player.start();
    }

    /**
     * @desc : Destroy PlaySong object instance
     */
    public void onDestroy() {
        mPlayerStatus = STATUS_HARD_STOPPED;
        m_player.stop();
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

    //getting the call back after 1 sec
    private int pauseCounter = 0;
    private int playCounter = 0;

    public void checkPlayStatus() {
        int counter = -1;

        switch(mPlayerStatus) {
            case STATUS_PLAYING: {
                playCounter++;

                if(playCounter >= playerInfo.getSongInterval() * 60) {
                    playCounter = 0;
                    pause();
                }

                counter = (playerInfo.getSongInterval() * 60) - playCounter;
            }
            break;

            case STATUS_PAUSE : {
                pauseCounter++;

                if(pauseCounter >= playerInfo.getPauseInterval() * 60) {
                    pauseCounter = 0;
                    resume();
                }

                counter = (playerInfo.getPauseInterval() * 60) - pauseCounter;
            }
            break;
        }

        if(mPlayerStatus != STATUS_PLAYING && mPlayerStatus != STATUS_PAUSE) {
            counter = -1;
        }

        //check here if any time we have reached the end time state
        if(isEndTimeReached()) {
            stop();
        }

        //send the notification to UI to update the time
        PlayerActivity.instance.updateStatus(mPlayerStatus, counter);
    }

    private boolean isEndTimeReached() {

        int endHour = playerInfo.getEndTime().getHour();
        int endMinutes = playerInfo.getEndTime().getMinute();

        int startHour = playerInfo.getStartTime().getHour();
        int startMinutes = playerInfo.getStartTime().getMinute();

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMin = calendar.get(Calendar.MINUTE);

        if (minuteStartTime > minuteEndTime) {
            if (currentHour > endHour ||
                    (currentHour == endHour &&
                            currentMin >= endMinutes)) {

                if (currentHour < startHour ||
                        (currentHour == startHour &&
                                currentMin < startMinutes)) {

                    return true;
                }
            }
        } else if (minuteStartTime < minuteEndTime) {
            if (currentHour > endHour||
                    (currentHour == endHour &&
                            currentMin >= endMinutes)) {
                return true;
            }
        } else {

            if ((currentHour * 60 + currentMin) == minuteEndTime) {  // interval 1 second

                return true;

            }
        }

        return false;
    }

    /**
     * @desc : Sendnotificaion to main view
     */
    public void SendNotification() {

        String str_songInfo = (PlayerActivity.m_currentPlayingSongIndex + 1) + ". " + mlst_musics.get(PlayerActivity.m_currentPlayingSongIndex).mStr_musicName;
        PlayerActivity.instance.mStr_Song = str_songInfo;
    }
}