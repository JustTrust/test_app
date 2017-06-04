package com.player.util;

import android.content.Context;
import android.media.AudioManager;


/**
 * Created by Test-Gupta on 4/13/2017.
 */

public class AudioAppManager {
    private Context context;
    private AudioManager m_AudioManager;

    public AudioAppManager(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        m_AudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public int getMaxLevel() {
        return m_AudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public void setVolumeLevelInPercent(int percentage) {
        int volume = (getMaxLevel() * percentage) / 100;
        setVolumeLevel(volume, true);
    }

    public void setVolumeLevel(int volume) {
        setVolumeLevel(volume, true);
    }

    public void setVolumeLevel(int volume, boolean postEvent) {

        if(volume > getMaxLevel()) {
            volume = getMaxLevel();
        }
        else if (volume < 0) {
            volume = 0;
        }

        m_AudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                volume, 0);
    }

    public int getVolumeLevelInPercentage() {
        return (getVolumeLevel() * 100) / getMaxLevel();
    }

    public int getVolumeLevel() {
        return m_AudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
}
