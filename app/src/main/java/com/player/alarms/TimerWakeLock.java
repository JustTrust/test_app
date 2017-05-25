/**
 *
 */
package com.player.alarms;

import android.content.Context;
import android.os.PowerManager;

/**
 * @desc Class for player app display screen
 */
public class TimerWakeLock {
    private static PowerManager.WakeLock sCpuWakeLock;
    private static String LOG_TAG = "PlayerApplication";
    public static void acquireCpuWakeLock(Context context) {
        if (sCpuWakeLock != null) {
            return;
        }
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        sCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, LOG_TAG);
        sCpuWakeLock.acquire();
    }

    public static void releaseCpuLock() {
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}
