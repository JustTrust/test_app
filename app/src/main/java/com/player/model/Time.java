/**
 * 
 */
package com.player.model;

import android.util.Log;

import com.player.AppConstant;

import java.io.Serializable;

/**
 * @Desc POJO for Time interval manage class
 */
public class Time implements Serializable, Cloneable {
    int m_nHour;
    int m_nMinute;
    public Time(){}
    public Time(int hour, int minute){
        m_nHour = hour;
        m_nMinute = minute;
        Log.i("MINUTE_ISSUE","constr "+minute);
    }

    public void setTimeValue(int hour, int minute){
        m_nHour = hour;
        m_nMinute = minute;
        Log.i("MINUTE_ISSUE","settimevalue "+minute);
    }
    public void setHour(int hour){ m_nHour = hour;}
    public int getHour(){ return m_nHour;}
    public void setMinute(int minute){
        Log.i("MINUTE_ISSUE","set "+minute);
        m_nMinute = minute;}
    public int getMinute(){ return m_nMinute;}
    public String convertString(){
        return String.format("%02d:%02d", m_nHour, m_nMinute);
    }
    public void parseData(String timeStr){
        Log.i("MINUTE_ISSUE","parse "+timeStr);
        if(timeStr != null) {
            String strHour = timeStr.substring(0, timeStr.indexOf(':'));
            String strMinute = timeStr.substring(timeStr.indexOf(':') + 1, timeStr.length());
            m_nHour = Integer.valueOf(strHour);
            m_nMinute = Integer.valueOf(strMinute);
        } else{
            m_nHour = AppConstant.EMPTY_VALUE;
            m_nMinute = AppConstant.EMPTY_VALUE;
        }
    }

    @Override
    public Time clone(){
        Time time = new Time();
        time.setHour(m_nHour);
        time.setMinute(m_nMinute);
        return time;
    }
}
