/**
 *
 */
package com.player.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.player.AppConstant;
import com.player.foreground.events.StopPlayerEvent;
import com.player.model.NotificationMessage;
import com.player.util.TimeUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.Calendar;

public class StartTime {

    /**
     * @param context
     * @param playerInfo
     * @desc setAlarm for play song at specific time
     */
    public static void setAlarm(Context context, NotificationMessage playerInfo) {
        disableAlert(context);

        int startHour = playerInfo.getStartTime().getHour();
        int startMinute = playerInfo.getStartTime().getMinute();
        int endHour = playerInfo.getEndTime().getHour();
        int endMinute = playerInfo.getEndTime().getMinute();
        int startSeconds = 0;

        if(playerInfo.getRealStartTime() != null) {
            startHour = playerInfo.getRealStartTime().getHour();
            startMinute = playerInfo.getRealStartTime().getMinute();
        }

        long startTime = getScheduleTimeForCurrentDate(startHour, startMinute, startSeconds);
        long endTime = getScheduleTimeForCurrentDate(endHour, endMinute, 0);

        notifyStopPlayerIfScheduleChanged(startTime, endTime);

        long elapsedTime = calculateAlarm(startHour, startMinute);
        Log.e("elapsedTime", elapsedTime + "");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AppConstant.SONG_START);
        intent.putExtra(AppConstant.FIELD_MESSAGE_DATA, (Serializable) playerInfo);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,elapsedTime,sender);
        }else{
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, elapsedTime, TimeUtils.MILLIS_IN_DAY, sender);
        }
    }



    /**
     * @param context
     * @desc disable previous set alearm
     */
    private static void disableAlert(Context context) {
        AlarmManager am = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, new Intent(AppConstant.SONG_START),
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }

    private static long getScheduleTimeForCurrentDate(int hour, int minute, int seconds) {
        Calendar scheduledTime = Calendar.getInstance();
        scheduledTime.set(Calendar.HOUR_OF_DAY, hour);
        scheduledTime.set(Calendar.MINUTE, minute);
        scheduledTime.set(Calendar.SECOND, seconds);
        scheduledTime.set(Calendar.MILLISECOND,0);
        return scheduledTime.getTimeInMillis();
    }

    private static void notifyStopPlayerIfScheduleChanged(long startTime, long endTime) {
        long nowTime = Calendar.getInstance().getTimeInMillis();
        if ((nowTime < startTime && endTime > startTime) || (nowTime > endTime && startTime > nowTime)) {
            EventBus.getDefault().post(new StopPlayerEvent());
        }
    }

    /**
     * @param hour
     * @param min
     * @return
     * @desc calculate elapsed time
     */
    public static long calculateAlarm(int hour, int min) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());


        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMin = calendar.get(Calendar.MINUTE);

        if (hour < nowHour || (hour == nowHour && min <= nowMin)) {
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
