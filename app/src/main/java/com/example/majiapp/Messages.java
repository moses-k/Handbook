
package com.example.majiapp;

public class Messages
{
    String date, time,message,type,from;


    public Messages()
    {

    }

    public Messages(String date, String time, String message, String type, String from) {
        this.date = date;
        this.time = time;
        this.message = message;
        this.type = type;
        this.from = from;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
