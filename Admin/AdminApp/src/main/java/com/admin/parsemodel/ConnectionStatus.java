package com.admin.parsemodel;

import com.admin.AppConstant;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("ConnectionStatus")
public class ConnectionStatus extends ParseObject{
    public void setDeviceID(String deviceID){
        put(AppConstant.FIELD_DEVICE_ID, deviceID);
    }

    public String getDeviceID(){
        return getString(AppConstant.FIELD_DEVICE_ID);
    }

    public void setDeviceName(String deviceID){
        put(AppConstant.FIELD_DEVICE_NAME, deviceID);
    }

    public String getDeviceName(){
        return getString(AppConstant.FIELD_DEVICE_NAME);
    }

    public void setGPSEnabled(boolean gpsEnabled){
        put(AppConstant.FIELD_GPS, gpsEnabled);
    }

    public boolean getGPSEnabled(){
        return getBoolean(AppConstant.FIELD_GPS);
    }

    public void setSong(String strSong){
        put(AppConstant.FIELD_SONG, strSong);
    }

    public String getSong(){
        return getString(AppConstant.FIELD_SONG);
    }


    public void setLatitude(String latitude){
        put(AppConstant.FIELD_LATITUDE, latitude);
    }

    public String getLatitude(){
        return getString(AppConstant.FIELD_LATITUDE);
    }
    public void setLongitude(String longitude){
        put(AppConstant.FIELD_LONGITUDE, longitude);
    }

    public String getLongitude(){
        return getString(AppConstant.FIELD_LONGITUDE);
    }
    public static ParseQuery<ConnectionStatus> getQuery() {
        return ParseQuery.getQuery(ConnectionStatus.class);
    }

    public boolean isPlaying(){
        return getBoolean(AppConstant.FIELD_IS_PLAYING);
    }

    public int getRemainTime() {
        return getInt(AppConstant.FIELD_REMAIN);
    }

    public void setVolume(String volume){
        put(AppConstant.FIELD_VOLUME, volume);
    }

    public String getVolume(){
        return getString(AppConstant.FIELD_VOLUME);
    }

}
