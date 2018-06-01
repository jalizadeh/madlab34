package com.example.sergio.madlab;

public class Msg {

    private String time;
    private String message;
    private String who; //0 = me, 1 = other user

    public Msg(){}

    public String getTime() {
        return time;
    }

    public void setTime(String tiemstamp) {
        this.time = tiemstamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }






}
