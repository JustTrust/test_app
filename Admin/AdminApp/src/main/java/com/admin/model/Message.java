package com.admin.model;


public class Message {
    public String msg;
    public Integer volume;
    public Double latitude;
    public Double longitude;
    public String time;

    public Message() {
    }

    public Message(String msg) {
        this.msg = msg;
    }

    public Message(Integer volume) {
        this.volume = volume;
    }

    public Message(Double latitude, Double longitude, String time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }
}
