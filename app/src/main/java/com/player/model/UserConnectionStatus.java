package com.player.model;


public class UserConnectionStatus {
    public String deviceName;
    public String deviceID;
    public Boolean gpsEnabled;
    public String strSong;
    public String latitude;
    public String longitude;
    public boolean isPlaying;
    public int remain;
    public String volume;
    public Long createdAt;

    public UserConnectionStatus() {
    }

    public UserConnectionStatus(String deviceName, String volume) {
        this.deviceName = deviceName;
        this.gpsEnabled = false;
        this.strSong = "";
        this.isPlaying = false;
        this.remain = -1;
        this.volume = volume;
    }

    public UserConnectionStatus(String deviceName, String deviceID, boolean gpsEnabled,
                                String strSong, String latitude, String longitude,
                                boolean isPlaying, int remain, String volume) {
        this.deviceName = deviceName;
        this.deviceID = deviceID;
        this.gpsEnabled = gpsEnabled;
        this.strSong = strSong;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isPlaying = isPlaying;
        this.remain = remain;
        this.volume = volume;
    }
}
