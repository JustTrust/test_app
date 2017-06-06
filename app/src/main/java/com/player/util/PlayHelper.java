package com.player.util;

import android.content.Context;
import android.media.MediaPlayer;

import com.player.R;

/**
 * Created by Anton
 * mail to a.belichenko@gmail.com
 */

public class PlayHelper {

    Context context;
    private MediaPlayer m_player = null;
    private int nCount;
    private static final int MAX_COUNT = 1;
    private boolean isPaying;

    public PlayHelper(Context context){
        this.context = context;
    }


    public synchronized void play() {
        if (isPaying) return;
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
    }

    public void stop(){
        if(m_player!= null){
            m_player.stop();
            m_player.release();
            m_player.setOnCompletionListener(null);
            m_player = null;
            isPaying = false;
        }
    }
}
