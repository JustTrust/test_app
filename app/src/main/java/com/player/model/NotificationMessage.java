/**
 * 
 */
package com.player.model;

import com.player.AppConstant;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * @desc Pojo for NotificatrionMessage Obj
 */

public class NotificationMessage implements Serializable{
    private JSONObject jsonObject;
    private int m_nSongInterval, m_nPauseInterval;
    private Time m_startTime, m_endTime;
    private Time m_realStartTime;
    private Boolean mIs_Background;
    public NotificationMessage() {

    }

    public void clearRealStartTime() {
        m_realStartTime = null;
    }

    public NotificationMessage(String json){
        try {
            parseData(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public NotificationMessage(boolean isBackground, Time start, Time end, String songInterval, String pauseInterval) {
        makeJSONObject(isBackground, start, end, songInterval, pauseInterval);
    }
   //set json object for send notification
    private void makeJSONObject(boolean isBackground, Time start, Time end, String songInterval, String pauseInterval) {
        JSONObject json_Data = new JSONObject();
        try {
            json_Data.put(AppConstant.FIELD_START_TIME, start.convertString());
            json_Data.put(AppConstant.FIELD_END_TIME, end.convertString());
            json_Data.put(AppConstant.FIELD_INTERVAL_SONGS, songInterval);
            json_Data.put(AppConstant.FIELD_INTERVAL_PAUSE, pauseInterval);
            jsonObject = new JSONObject();
            jsonObject.put(AppConstant.FIELD_PLAY_INFO, json_Data);
            jsonObject.put(AppConstant.NOTIFY_IS_BACKGROUND, isBackground);
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void parseData(JSONObject messageInfo) {
        try {
            JSONObject json_Data = messageInfo.getJSONObject(AppConstant.FIELD_PLAY_INFO);
            mIs_Background = messageInfo.getBoolean(AppConstant.NOTIFY_IS_BACKGROUND);
            String str_StartTime = json_Data.getString(AppConstant.FIELD_START_TIME);
            String str_EndTime = json_Data.getString(AppConstant.FIELD_END_TIME);
            String str_songInterval = json_Data.getString(AppConstant.FIELD_INTERVAL_SONGS);
            String str_pauseInterval = json_Data.getString(AppConstant.FIELD_INTERVAL_PAUSE);
            String str_realStartTime = json_Data.optString(AppConstant.FIELD_REAL_START_TIME);

            m_startTime = new Time(); m_endTime = new Time();
            m_startTime.parseData(str_StartTime);
            m_endTime.parseData(str_EndTime);
            m_nSongInterval = Integer.valueOf(str_songInterval);
            m_nPauseInterval = Integer.valueOf(str_pauseInterval);

            if(str_realStartTime != null && str_realStartTime.length() > 0) {
                m_realStartTime = new Time();
                m_realStartTime.parseData(str_realStartTime);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getSongInterval(){
        return m_nSongInterval;
    }

    public int getPauseInterval(){
        return m_nPauseInterval;
    }

    public Time getStartTime(){
        return m_startTime;
    }

    public Time getEndTime(){
        return m_endTime;
    }

    public Time getRealStartTime(){
        return m_realStartTime;
    }

    public JSONObject getJsonObject() {
        return  jsonObject;
    }
}