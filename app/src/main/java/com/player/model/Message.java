package com.player.model;


public class Message {
    public String deviceId;
    public String msg;
    public Integer volume;
    public Double latitude;
    public Double longitude;
    public String time;
    public String videoLink;

    public Message() {
    }

    public Message(String msg, Integer volume) {
        this.msg = msg;
        this.volume = volume;
    }

    public Message(Double latitude, Double longitude, String time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }
}
