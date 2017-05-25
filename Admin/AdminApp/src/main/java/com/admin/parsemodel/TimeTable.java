package com.admin.parsemodel;

import com.admin.AppConstant;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("ConnectionStatus")
public class TimeTable extends ParseObject{



    public void setDeviceID(String deviceID){
        put(AppConstant.FIELD_DEVICE_ID, deviceID);
    }

    public String getDeviceID(){
        return getString(AppConstant.FIELD_DEVICE_ID);
    }

    public void setSongInterval(String songInterval){
        put(AppConstant.FIELD_INTERVAL_SONGS, songInterval);
    }

    public String getSongInterval(){
        return getString(AppConstant.FIELD_INTERVAL_SONGS);
    }

    public void setPauseInterval(String pauseInterval){
        put(AppConstant.FIELD_INTERVAL_PAUSE, pauseInterval);
    }

    public String getPauseInterval(){
        return getString(AppConstant.FIELD_INTERVAL_PAUSE);
    }

    public static ParseQuery<TimeTable> getQuery() {
        return ParseQuery.getQuery(TimeTable.class);
    }
}
