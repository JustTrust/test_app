package com.player.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import com.player.R;
import com.player.ui.views.CustomViewGroup;


/**
 * Created by Test-Gupta on 4/5/2017.
 */

public class StatusBarUtils {
    private Activity m_Activity;
    private CustomViewGroup m_View = null;
    private boolean bViewAdded;

    public StatusBarUtils(Activity activity) {
        this.m_Activity = activity;
        m_View = new CustomViewGroup(m_Activity);
    }

    public void addStatusBarView() {
        try {
            addView();
        } catch (Exception e) {
        }
    }

    private void addView() {

        if(bViewAdded) {
            return;
        }
        WindowManager manager = ((WindowManager) m_Activity.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));

        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |

                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = getStatusBarHeight(); //m_Activity.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        localLayoutParams.format = PixelFormat.TRANSPARENT;
        m_View.setBackgroundResource(R.drawable.header_bg); //Color(AppUtils.getColor(m_Activity, R.color.shadow_background_color));
        manager.addView(m_View, localLayoutParams);
        bViewAdded = true;
    }



    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = m_Activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = m_Activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void removeView() {

        if (m_View != null && bViewAdded) {
            try {
                WindowManager manager = ((WindowManager) m_Activity
                        .getSystemService(Context.WINDOW_SERVICE));
                manager.removeView(m_View);
                bViewAdded = false;
            } catch (Exception e) {
            }
        }
    }

    public void setStatusBarVisibility(boolean visible) {
        if (m_View != null) {
           m_View.setBarVisibility(visible);
        }
    }

    public void setCurrentTime() {
        if (m_View != null) {
            m_View.setCurrentTime();
        }
    }

    public void setBatteryChangeLevel(int level) {
        if (m_View != null) {
            m_View.setBatteryChangeLevel(level);
        }
    }

    public void setNetworkSignal(int percentage) {
        if (m_View != null) {
            m_View.setNetworkSignal(percentage);
        }
    }

    public void setGpsSettings(boolean on) {
        if (m_View != null) {
            m_View.setGpsSignal(on);
        }
    }
}
