package com.player.parseModel;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created: Belozerov
 * Company: APPGRANULA LLC
 * Date: 27.10.2015
 */
@ParseClassName("DeviceSettings")
public class DeviceSettings extends ParseObject {
    private static final String START_TIME = "startTime";
    private static final String END_TIME = "endTime";
    private static final String SONG_INTERVAL = "songInterval";
    private static final String PAUSE_INTERVAL = "pauseInterval";
    public static final String DEVICE_ID = "deviceId";

    public void setStartTime(String startTime) {
        put(START_TIME, startTime);
    }

    public String getStartTime() {
        return getString(START_TIME);
    }

    public String getEndTime() {
        return getString(END_TIME);
    }

    public void setEndTime(String endTime) {
        put(END_TIME, endTime);
    }

    public void setSongInterval(String interval) {
        put(SONG_INTERVAL, interval);
    }

    public String getSongInterval() {
        return getString(SONG_INTERVAL);
    }

    public void setPauseInterval(String interval) {
        put(PAUSE_INTERVAL, interval);
    }

    public String getPauseInterval() {
        return getString(PAUSE_INTERVAL);
    }

    public String getDeviceId() {
        return getString(DEVICE_ID);
    }

    public void setDeviceId(String deviceId) {
        put(DEVICE_ID, deviceId);
    }

    public static ParseQuery<DeviceSettings> getQuery() {
        return ParseQuery.getQuery(DeviceSettings.class);
    }
}
