package com.kinggo.app.shiftworker.view.calendar.entity;

import java.io.Serializable;

public class WorkInfo implements Serializable {

    private String name;
    private String color;
    private int id;

    public WorkInfo() {
    }

    public WorkInfo(int id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }
}
