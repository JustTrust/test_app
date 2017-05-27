package com.admin.model;


public class UserConnectionStatus {
    public String deviceName;
    public String deviceID;
    public boolean gpsEnabled;
    public String strSong;
    public String latitude;
    public String longitude;
    public boolean isPlaying;
    public int remain;
    public String volume;
    public Long createdAt;

    public UserConnectionStatus() {
    }

    public UserConnectionStatus(String deviceID, String deviceName) {
        this.deviceName = deviceName;
        this.deviceID = deviceID;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof UserConnectionStatus))return false;
        final UserConnectionStatus other = (UserConnectionStatus) obj;
        if (other.deviceID == null || this.deviceID == null) return  false;
        if (other.deviceID.equals(this.deviceID)) return true;
        return false;
    }
}
