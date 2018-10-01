package com.kinggo.app.shiftworker.view.calendar.entity;

import java.io.Serializable;

public class User implements Serializable{
    private int id;
    private String workinfo;
    private String workfirstday;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWorkinfo() {
        return workinfo;
    }

    public void setWorkinfo(String workinfo) {
        this.workinfo = workinfo;
    }

    public String getWorkfirstday() {
        return workfirstday;
    }

    public void setWorkfirstday(String workfirstday) {
        this.workfirstday = workfirstday;
    }

    public String[] getWorkSplit() {
        return workinfo.split(";");
    }
}
