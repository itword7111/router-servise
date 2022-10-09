package com.example.router_web_service.model;


import java.sql.Time;
import java.sql.Timestamp;

public class ReportSender {
private String userName;
private String task;
private Timestamp timeOfTrack;


    public ReportSender(String userName,  String task, Timestamp timeOfTrack) {
        this.userName = userName;
        this.task = task;
        this.timeOfTrack = timeOfTrack;
    }

    public ReportSender() {
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public Timestamp getTimeOfTrack() {
        return timeOfTrack;
    }

    public void setTimeOfTrack(Timestamp timeOfTrack) {
        this.timeOfTrack = timeOfTrack;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
