package com.player.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;

import com.player.R;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public class PlayHelper {

    private Context context;
    private AudioAppManager audioAppManager;
    private MediaPlayer m_player = null;
    private int nCount;
    private static final int MAX_COUNT = 1;
    private boolean isPaying;
    private OnFinishListener listener;
    private int mainVolume;

    public PlayHelper(Context context, AudioAppManager audioAppManager){
        this.context = context;
        this.audioAppManager = audioAppManager;
    }


    public synchronized void play() {
        if (isPaying) return;
        mainVolume = audioAppManager.getVolumeLevel();
        isPaying = true;
        nCount = 0;
        m_player = MediaPlayer.create(context, R.raw.test);
        m_player.setOnCompletionListener(mp -> {
            if (nCount < MAX_COUNT && m_player != null) {
                m_player.seekTo(0);
                m_player.start();
                nCount += 1;
            }else {
                stop();
            }
        });
        m_player.start();
        audioAppManager.setVolumeLevel(audioAppManager.getMaxLevel());
    }

    public void stop(){
        if(m_player!= null){
            m_player.stop();
            m_player.release();
            m_player.setOnCompletionListener(null);
            m_player = null;
            isPaying = false;
        }
        if (listener != null){
            listener.onFinishPlay();
        }
        audioAppManager.setVolumeLevel(mainVolume);
    }

    public void setOnFinishListener(@Nullable OnFinishListener listener){
        this.listener = listener;
    }

    public interface OnFinishListener {
        void onFinishPlay();
    }
}
