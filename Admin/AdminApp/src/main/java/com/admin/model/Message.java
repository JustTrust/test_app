package com.admin.model;


import java.util.Date;

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
        this.time = String.valueOf(new Date().getTime());
    }

    public Message(Integer volume) {
        this.volume = volume;
        this.time = String.valueOf(new Date().getTime());
    }

    public Message(Double latitude, Double longitude, String time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }
}
