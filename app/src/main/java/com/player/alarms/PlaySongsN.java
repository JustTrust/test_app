/**
 *
 */
package com.player.alarms;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import com.player.AppConstant;
import com.player.model.MusicInfo;
import com.player.model.NotificationMessage;
import com.player.ui.activity.PlayerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


@SuppressWarnings("unchecked")
public class PlaySongsN {
    public static final int STATUS_PLAYING = 0;
    public static final int STATUS_PAUSE = 1;
    public static final int STATUS_STOPPED = 2;
    public static final int STATUS_HARD_STOPPED = 3;
    public static final int STATUS_NONE = -1;
    private PlayListener listener;
    private ArrayList<MusicInfo> mlst_musics = PlayerActivity.mlst_Musics;
    private NotificationMessage playerInfo = null;
    private MediaPlayer m_player;
    private int mPlayerStatus = STATUS_NONE;
    private int minuteStartTime;
    private int minuteEndTime;

    private int pauseCounter = 0;
    private int playCounter = 0;

    public PlaySongsN(@NonNull PlayListener listener) {
        this.listener = listener;
    }

    /**
     * @param intent
     * @desc onStartCommand for initialize timer task & total time object
     */
    public void onStartCommand(Intent intent) {

        playerInfo = (NotificationMessage) intent.getSerializableExtra(AppConstant.FIELD_MESSAGE_DATA);

        minuteStartTime = playerInfo.getStartTime().getHour() * 60 + playerInfo.getStartTime().getMinute();
        minuteEndTime = playerInfo.getEndTime().getHour() * 60 + playerInfo.getEndTime().getMinute();

        if (playerInfo.getRealStartTime() != null) {
            playCounter = Calendar.getInstance().get(Calendar.SECOND);
        }
    }

    public boolean play() {
        try {
            String filepath = getFilePath(AppConstant.START_FIRST_SONG);

            m_player = null;
            m_player = new MediaPlayer();
            m_player.setDataSource(filepath);

            m_player.setOnCompletionListener(mp -> playNext());
            m_player.prepare();
            m_player.start();
            mPlayerStatus = STATUS_PLAYING;
            if (listener != null){
                listener.updatePlayStatus(STATUS_PLAYING,
                        m_player.getDuration()>0 ? m_player.getDuration() : 0);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null){
                listener.updatePlayStatus(STATUS_HARD_STOPPED,0);
            }
            return false;
        }
    }

    private boolean playNext() {
        try {
            String filePath = getFilePath(PlayerActivity.m_currentPlayingSongIndex);

            m_player.stop();
            m_player.reset();
            m_player.setDataSource(filePath);
            m_player.prepare();
            m_player.start();
            mPlayerStatus = STATUS_PLAYING;
            if (listener != null){
                listener.updatePlayStatus(STATUS_PLAYING,
                        m_player.getDuration()>0 ? m_player.getDuration() : 0);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null){
                listener.updatePlayStatus(STATUS_HARD_STOPPED,0);
            }
            return false;
        }
    }

    private void pause() {
        mPlayerStatus = STATUS_PAUSE;
        if (listener != null){
            listener.updatePlayStatus(STATUS_PAUSE, 0);
        }
        m_player.pause();
    }

    private void stop() {
        mPlayerStatus = STATUS_STOPPED;
        if (listener != null){
            listener.updatePlayStatus(STATUS_STOPPED, 0);
        }
        m_player.pause();
    }

    public void resume() {
        mPlayerStatus = STATUS_PLAYING;
        m_player.start();
        if (listener != null){
            listener.updatePlayStatus(STATUS_PLAYING,
                    m_player.getDuration()>0 ? m_player.getDuration() : 0);
        }
    }

    /**
     * @desc : Destroy PlaySong object instance
     */
    public void onDestroy() {
        if (listener != null){
            listener.updatePlayStatus(STATUS_HARD_STOPPED,0);
        }
        mPlayerStatus = STATUS_HARD_STOPPED;
        m_player.stop();
    }

    /**
     * @param current : index of current song
     * @return
     * @desc get medai file path from sdcard
     */
    private String getFilePath(int current) {
        current++;
        while (current < mlst_musics.size()) {
            if (mlst_musics.get(current).mIs_enabled) {
                PlayerActivity.m_currentPlayingSongIndex = current;
                return mlst_musics.get(current).mStr_musicPath;
            }
            current++;
        }
        return "";
    }

    /**
     * @param index : index of media file
     * @return Name of media file name
     * @desc get media file name base on index
     */
    public String getFileName(int index) {
        return mlst_musics.get(index).mStr_musicName;
    }

    public void checkPlayStatus() {
        int counter = -1;

        switch (mPlayerStatus) {
            case STATUS_PLAYING: {
                playCounter++;

                if (playCounter >= playerInfo.getSongInterval() * 60) {
                    playCounter = 0;
                    pause();
                }
                counter = (playerInfo.getSongInterval() * 60) - playCounter;
            }
            break;

            case STATUS_PAUSE: {
                pauseCounter++;

                if (pauseCounter >= playerInfo.getPauseInterval() * 60) {
                    pauseCounter = 0;
                    resume();
                }
                counter = (playerInfo.getPauseInterval() * 60) - pauseCounter;
            }
            break;
        }
        if (mPlayerStatus != STATUS_PLAYING && mPlayerStatus != STATUS_PAUSE) {
            counter = -1;
        }

        //check here if any time we have reached the end time state
        if (isEndTimeReached()) {
            stop();
        }
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
            if (currentHour > endHour ||
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

    public interface PlayListener{
        void updatePlayStatus(int status, int remainTime);
    }

}